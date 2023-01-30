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
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchSubCriteria

def process(transaction):

  incomingPath = transaction.getIncoming().getPath()
  incomingFolder = transaction.getIncoming().getName()

  folders=[]
  folders=os.listdir(incomingPath)

  split=incomingFolder.split("_")
  if (len(split) == 4):
    IS_HISEQ_RUN=True
  if (len(split) == 2):
    IS_HISEQ_RUN=False

  # Get the search service
  search_service = transaction.getSearchService()
   
  # Search for the sample
  sc = SearchCriteria()
  sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, incomingFolder));
  foundSamples = search_service.searchForSamples(sc)

  if foundSamples.size() > 0:
   
    # Search for another data set of the same sample and make it a child of it 
    sampleSc = SearchCriteria()
    sampleSc.addSubCriteria(SearchSubCriteria.createSampleContainerCriteria(sc))
    foundContainedSamples = search_service.searchForSamples(sampleSc)
    if foundContainedSamples.size() > 0:
      for fcs in range(0,foundContainedSamples.size()):
	dataSet = transaction.createNewDataSet("RUNINFO")
	dataSet.setMeasuredData(False)
	dataSet.setSample(foundContainedSamples[fcs])
	# Add the incoming file into the data set
	shutil.copytree(incomingPath, incomingPath + "_" + str(fcs+1))
	transaction.moveFile(incomingPath + "_" + str(fcs+1), dataSet)
    
  shutil.rmtree(incomingPath)
