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

@note
print statements go to: <openBIS_HOME>/datastore_server/log/startup_log.txt
Expects as incoming folder: Project_120427_SN792_0110_AD0YCGACXX_1

@author:
Manuel Kohler
'''

import os
import shutil
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchSubCriteria

def renameFiles(dir, flowcellName):
  print dir 
  print flowcellName
  for root, dirs, files in os.walk(dir):
    for file in files:
      print root + file
      os.rename(root + '/' + file, root + "/" + flowcellName + "_" + file)


def process(transaction):

  incomingPath = transaction.getIncoming().getPath()
  incomingFolder = transaction.getIncoming().getName()

  fcAndLane = incomingFolder.split("_",1)[-1]
  flowCell, flowLane = fcAndLane.rsplit("_",1)
  print flowLane

  # Get the search service
  search_service = transaction.getSearchService()

  # Search for the sample
  sc = SearchCriteria()
  sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, flowCell))
  foundSamples = search_service.searchForSamples(sc)

  if foundSamples.size() > 0:

    # Search for another data set of the same sample and make it a child of it 
    sampleSc = SearchCriteria()
    sampleSc.addSubCriteria(SearchSubCriteria.createSampleContainerCriteria(sc))
    foundContainedSamples = search_service.searchForSamples(sampleSc)
    if foundContainedSamples.size() > 0:
      dataSet = transaction.createNewDataSet("FASTQC")
      dataSet.setMeasuredData(False)
      for indx in range(0, len(foundContainedSamples)):
	lane = foundContainedSamples[indx].getCode().split(':')[-1]
	print lane
	if (flowLane == lane):
	  dataSet.setSample(foundContainedSamples[indx])
	  # Add the incoming file into the data set
	  transaction.moveFile(incomingPath + "/fastqc/", dataSet)
	  break
