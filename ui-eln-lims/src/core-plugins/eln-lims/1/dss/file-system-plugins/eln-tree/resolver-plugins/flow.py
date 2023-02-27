from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search import DataSetSearchCriteria
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions import DataSetFetchOptions

import script

def addSampleChildNodes(path, samplePermId, sampleType, response, acceptor, context):
    dataSetSearchCriteria = DataSetSearchCriteria()
    dataSetSearchCriteria.withOrOperator()
    dataSetSearchCriteria.withSample().withPermId().thatEquals(samplePermId)
    parentsSearchCriteria = dataSetSearchCriteria.withSample().withParents()
    parentsSearchCriteria.withPermId().thatEquals(samplePermId)
    fetchOptions = DataSetFetchOptions()
    fetchOptions.withType()
    fetchOptions.withProperties()
    fetchOptions.withSample()
    dataSets = context.getApi().searchDataSets(context.getSessionToken(), dataSetSearchCriteria, fetchOptions).getObjects()
    for dataSet in dataSets:
        if acceptor.acceptDataSet(dataSet):
            dataSetCode = dataSet.getCode()
            content = context.getContentProvider().asContent(dataSetCode)
            contentNode = content.getRootNode()
            script.addDataSetFileNodes(path, dataSetCode, contentNode, response, acceptor, context)

for t in ["FACS_ARIA", "INFLUX", "LSR_FORTESSA", "CYTOFLEX_S", "MOFLO_XDP", "S3E", "SONY_SH800S", "SONY_MA900"]:
    acceptor.hideSampleType("%s_SPECIMEN" % t)
    acceptor.sampleChildrenHandlers["%s_EXPERIMENT" % t] = addSampleChildNodes
