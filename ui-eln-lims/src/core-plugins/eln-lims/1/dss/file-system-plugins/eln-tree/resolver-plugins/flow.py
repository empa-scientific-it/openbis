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

def addSampleChildNodesWithPlates(path, samplePermId, sampleType, response, acceptor, context):
    dataSetSearchCriteria = DataSetSearchCriteria()
    dataSetSearchCriteria.withOrOperator()
    dataSetSearchCriteria.withSample().withPermId().thatEquals(samplePermId)
    parentsSearchCriteria = dataSetSearchCriteria.withSample().withParents()
    parentsSearchCriteria.withPermId().thatEquals(samplePermId)
    fetchOptions = DataSetFetchOptions()
    fetchOptions.withType()
    fetchOptions.withProperties()
    fetchOptions.withSample().withType()
    dataSets = context.getApi().searchDataSets(context.getSessionToken(), dataSetSearchCriteria, fetchOptions).getObjects()
    for dataSet in dataSets:
        sampleTypeCode = dataSet.getSample().getType().getCode()
        if not sampleTypeCode.endswith("_WELL"):
            dataSetCode = dataSet.getCode()
            content = context.getContentProvider().asContent(dataSetCode)
            contentNode = content.getRootNode()
            script.addDataSetFileNodes(path, dataSetCode, contentNode, response, acceptor, context)
    script.addSampleSampleChildNodes(path, samplePermId, response, acceptor, context)

for t in ["FACS_ARIA", "INFLUX", "MOFLO_XDP", "S3E", "SONY_SH800S", "SONY_MA900"]:
    acceptor.hideSampleType("%s_SPECIMEN" % t)
    acceptor.sampleChildrenHandlers["%s_EXPERIMENT" % t] = addSampleChildNodes

for t in ["LSR_FORTESSA", "CYTOFLEX_S"]:
    acceptor.hideSampleType("%s_SPECIMEN" % t)
    acceptor.hideSampleType("%s_WELL" % t)
    acceptor.hideSampleType("%s_TUBE" % t)
    acceptor.hideSampleType("%s_TUBESET" % t)
    acceptor.sampleChildrenHandlers["%s_EXPERIMENT" % t] = addSampleChildNodesWithPlates
    acceptor.sampleChildrenHandlers["%s_PLATE" % t] = addSampleChildNodes
