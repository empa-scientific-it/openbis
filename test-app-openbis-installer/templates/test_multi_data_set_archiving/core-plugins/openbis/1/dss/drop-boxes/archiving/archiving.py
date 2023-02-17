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
from ch.systemsx.cisd.openbis.dss.generic.shared import ServiceProvider
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id import DataSetPermId
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update import DataSetUpdate
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update import PhysicalDataUpdate


def process(transaction):
    incoming_path = transaction.getIncoming()
    executeOperation(incoming_path.name)
    
def executeOperation(file_name):
    parts = file_name.split()
    data_set_codes = parts[1:]
    service = ServiceProvider.getOpenBISService()
    
    if parts[0] == 'archive':
        service.archiveDataSets(data_set_codes, True)
    elif parts[0] == 'addToArchive':
        service.archiveDataSets(data_set_codes, False)
    elif parts[0] == 'unarchive':
        service.unarchiveDataSets(data_set_codes)
    elif parts[0] == 'requestToArchive':
        v3 = ServiceProvider.getV3ApplicationService()
        update = DataSetUpdate()
        update.setDataSetId(DataSetPermId(data_set_codes[0]))
        physicalData = PhysicalDataUpdate()
        physicalData.setArchivingRequested(True)
        update.setPhysicalData(physicalData)
        v3.updateDataSets(service.getSessionToken(), [update])
