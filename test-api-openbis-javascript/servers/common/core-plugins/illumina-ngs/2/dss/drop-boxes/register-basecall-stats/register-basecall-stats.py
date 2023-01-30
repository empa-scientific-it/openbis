'''
@copyright:
2013 ETH Zuerich, CISD
    
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
Registers an incoming directory as a 'BASECALL_STATS' data set in openBIS.
The name of the directory is used to search for the matching sample. 

@note: 
print statements go to: <openBIS_HOME>/datastore_server/log/startup_log.txt
expected incoming Name for HiSeq runs: C035RACXX_1
expected incoming Name for MiSeq runs: A0T19_1

@author:
Manuel Kohler
'''

import os
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchSubCriteria

# -----------------------------------------------------------------------------

def extractFlowCellName (runFolderName):
  flowCell, flowLane =  runFolderName.split('_')
  return flowCell, flowLane

# -----------------------------------------------------------------------------

def process(transaction):

  runFolderName = transaction.getIncoming().getName()
  incomingPath = transaction.getIncoming().getPath()
  flowCellName, flowLaneName = extractFlowCellName(runFolderName)
  
  # Create a data set and set type
  dataSet = transaction.createNewDataSet("BASECALL_STATS")
  dataSet.setMeasuredData(False)

  dataSet.setPropertyValue("MISMATCH_IN_INDEX", "NONE")
    
  search_service = transaction.getSearchService()
  transaction.moveFile(incomingPath, dataSet)
  sc = SearchCriteria()
  sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, flowCellName + ':' + flowLaneName))
  foundSamples = search_service.searchForSamples(sc)

  if foundSamples.size() > 0:
    dataSet.setSample(foundSamples[0])
