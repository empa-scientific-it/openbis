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
expected incoming Name for MiSeq runs: 120726_M00721_0011_A000000000-A1FVF

@author:
Manuel Kohler
'''

from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria

DATASET_TYPE_MISEQ = "ILLUMINA_MISEQ_OUTPUT"

def process(transaction):

  incomingPath = transaction.getIncoming().getAbsolutePath()
  flowCellId = transaction.getIncoming().getName()
  dataSet = transaction.createNewDataSet(DATASET_TYPE_MISEQ)

  # Create a data set and set type
  dataSet.setMeasuredData(False)
  
  # Get the search service
  search_service = transaction.getSearchService()
 
  # Search for the sample
  sc = SearchCriteria()
  sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, flowCellId));
  foundFlowCells = search_service.searchForSamples(sc)

  # Make sure there is only one Flow cell registered with this Flow Cell Name / ID
  try:
    assert foundFlowCells.size() == 1
  except AssertionError:
    print (str(foundFlowCells.size()) + ' flow cells found which match the criterias: '+ flowCellId)

  print (foundFlowCells)
  # Add the incoming file into the data set
  transaction.moveFile(incomingPath, dataSet)
  dataSet.setSample(foundFlowCells[0])
