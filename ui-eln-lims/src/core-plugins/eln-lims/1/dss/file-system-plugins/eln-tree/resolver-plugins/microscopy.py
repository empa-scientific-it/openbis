from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search import DataSetSearchCriteria
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions import DataSetFetchOptions

import script

acceptor.hideDataSetType("MICROSCOPY_IMG_CONTAINER")
acceptor.hideDataSetType("MICROSCOPY_IMG_OVERVIEW")
acceptor.hideDataSetType("MICROSCOPY_IMG_THUMBNAIL")
#acceptor.hideSampleType("MICROSCOPY_SAMPLE_TYPE")

def addSampleChildNodes(path, samplePermId, sampleType, response, acceptor, context):
    dataSetSearchCriteria = DataSetSearchCriteria()
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

acceptor.sampleChildrenHandlers["MICROSCOPY_EXPERIMENT"] = addSampleChildNodes
