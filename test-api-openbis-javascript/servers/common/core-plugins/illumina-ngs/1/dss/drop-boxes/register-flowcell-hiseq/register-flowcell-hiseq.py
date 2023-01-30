'''
@copyright:
2012 ETH Zuerich, CISD
    
@license: 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    
http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

@description:
Registers an incoming directory as a data set in openBIS. The name of the directory is used to
search for the matching sample. 

@note: 
print statements go to: <openBIS_HOME>/datastore_server/log/startup_log.txt
expected incoming Name for HiSeq runs: 110715_SN792_0054_BC035RACXX
expected incoming Name for GAII runs: 110812_6353WAAXX

@author:
Manuel Kohler
'''

import os
import shutil
import glob
import xml.etree.ElementTree as etree
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria

IS_HISEQ_RUN=False

RUNINFO_FOLDER='/links/shared/dsu/dss/register-runstatistics/'
REGEX_RUNINFO_SAMPLE = '/Data/Status*'
REGEX_RUNINFO_REPORTS = '/Data/reports'
MARKER_STRING='.MARKER_is_finished_'

def touch_markerfile(filename):
  try:
    # do a touch
    open(filename, 'w').close()
  except:
    print('Could not touch ' + filename)


def process(transaction):

  incomingPath = transaction.getIncoming().getPath()
  name = transaction.getIncoming().getName()

  folders=[]
  folders=os.listdir(incomingPath)

  split=name.split("_")
  if (len(split) == 4):
    dataSet = transaction.createNewDataSet("ILLUMINA_HISEQ_OUTPUT")
    IS_HISEQ_RUN=True
  if (len(split) == 2):
    dataSet = transaction.createNewDataSet("ILLUMINA_GA_OUTPUT")

  #move RunInfo into a different drop box
  runInfoSample=glob.glob(incomingPath + REGEX_RUNINFO_SAMPLE)
  runInfoReport=glob.glob(incomingPath + REGEX_RUNINFO_REPORTS)
  runInfoList = runInfoSample + runInfoReport
  os.makedirs(RUNINFO_FOLDER + name + '/Data/')
  for runInfo in runInfoList:
    try:
      if os.path.isdir(runInfo):
	shutil.copytree(runInfo, RUNINFO_FOLDER + name + '/Data/' + os.path.basename(runInfo))
      else:
	shutil.copy2(runInfo, RUNINFO_FOLDER + name + '/Data/')
    except (IOError, os.error), why:
      print (runInfo, RUNINFO_FOLDER + name, str(why))

  touch_markerfile(RUNINFO_FOLDER+MARKER_STRING+name)

  # Create a data set and set type
  dataSet.setMeasuredData(False)
    
  # Get the search service
  search_service = transaction.getSearchService()
   
  # Search for the sample
  sc = SearchCriteria()
  sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, name));
  foundSamples = search_service.searchForSamples(sc)

  if foundSamples.size() > 0:
    # Add the incoming file into the data set
    transaction.moveFile(incomingPath, dataSet)
    dataSet.setSample(foundSamples[0])
