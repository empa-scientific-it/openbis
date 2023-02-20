#   Copyright ETH 2014 - 2023 Zürich, Scientific IT Services
# 
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
# 
#        http://www.apache.org/licenses/LICENSE-2.0
#   
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#
def process(transaction):
    incoming = transaction.getIncoming()
    parts = incoming.name.split()
    dataSetType = parts[0]
    id = parts[1].replace(':', '/')
   
    dataSet = transaction.createNewDataSet()
    dataSet.setDataSetType(dataSetType)
    if id.count('/') > 2:
        dataSet.setExperiment(transaction.getExperiment(id))
    else:
        dataSet.setSample(transaction.getSample(id))
          
    transaction.moveFile(incoming.getAbsolutePath(), dataSet, 'data')
