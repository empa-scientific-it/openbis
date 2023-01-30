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
expected incoming Name for MiSeq runs: 121218_M00721_0017_000000000-A0T19

@author:
Manuel Kohler
'''

import os
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchSubCriteria

def process(transaction):

  incomingPath = transaction.getIncoming().getPath()
  incomingFolder = transaction.getIncoming().getName()
  
  folders=os.listdir(incomingPath)

  # expected incoming Name, e.g.: 110715_SN792_0054_BC035RACXX

  # Create a data set and set type
  dataSet = transaction.createNewDataSet("BASECALL_STATS")
  dataSet.setMeasuredData(False)

  dataSet.setPropertyValue("MISMATCH_IN_INDEX", "NONE")
    
  # Get the search service
  search_service = transaction.getSearchService()
   
  # Add the incoming file into the data set
  transaction.moveFile(incomingPath, dataSet)
    
  # Search for the sample
  sc = SearchCriteria()
  sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, incomingPath))
  foundSamples = search_service.searchForSamples(sc)
  

  if foundSamples.size() > 0:
    dataSet.setSample(foundSamples[0])

    # Search for another data set of the same sample
    #dataSetSc = SearchCriteria()
    #dataSetSc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.TYPE, 'ILLUMINA_HISEQ_OUTPUT'))
    #dataSetSc.addSubCriteria(SearchSubCriteria.createSampleCriteria(sc))
    #foundDataSets = search_service.searchForDataSets(dataSetSc)
    #if foundDataSets.size() > 0:
    #  dataSet.setParentDatasets([ds.getDataSetCode() for ds in foundDataSets])
