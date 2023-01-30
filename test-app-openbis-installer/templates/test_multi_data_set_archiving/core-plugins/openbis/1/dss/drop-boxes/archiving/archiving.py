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
