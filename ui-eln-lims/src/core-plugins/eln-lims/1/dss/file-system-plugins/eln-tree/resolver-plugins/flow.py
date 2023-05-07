from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id import SamplePermId
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions import SampleFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search import DataSetSearchCriteria

import script


def addSampleChildNodes(path, samplePermId, sampleType, response, acceptor, context):
    dataSets = getDataSetsOfSampleAndItsDescendants(samplePermId, False, context)
    filteredDataSets = []
    for dataSet in dataSets:
        if acceptor.acceptDataSet(dataSet):
            filteredDataSets.append(dataSet)
    script.addDataSetFileNodesFor(path, filteredDataSets, response, acceptor, context)

def addSampleChildNodesWithPlates(path, samplePermId, sampleType, response, acceptor, context):
    dataSets = getDataSetsOfSampleAndItsDescendants(samplePermId, True, context)
    filteredDataSets = []
    for dataSet in dataSets:
        sampleTypeCode = dataSet.getSample().getType().getCode()
        if not sampleTypeCode.endswith("_WELL"):
            filteredDataSets.append(dataSet)
    script.addDataSetFileNodesFor(path, filteredDataSets, response, acceptor, context)
    script.addSampleSampleChildNodes(path, samplePermId, response, acceptor, context)

def getDataSetsOfSampleAndItsDescendants(samplePermId, supressWells, context):
    samplePermIds = []
    gatherAllDescendants(samplePermIds, samplePermId, supressWells, context)
    dataSetSearchCriteria = DataSetSearchCriteria()
    dataSetSearchCriteria.withOrOperator()
    for id in samplePermIds:
        dataSetSearchCriteria.withSample().withPermId().thatEquals(id)
    return script.getDataSets(dataSetSearchCriteria, context)

def gatherAllDescendants(samplePermIds, samplePermId, supressWells, context):
    samplePermIds.append(samplePermId)
    id = SamplePermId(samplePermId)
    fetchOptions = SampleFetchOptions()
    fetchOptions.withChildren().withType()
    children = context.getApi().getSamples(context.getSessionToken(), [id], fetchOptions)[id].getChildren()
    for child in children:
        sampleTypeCode = child.getType().getCode()
        if not supressWells or not sampleTypeCode.endswith("_WELL"):
            gatherAllDescendants(samplePermIds, child.getPermId().getPermId(), supressWells, context)

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
