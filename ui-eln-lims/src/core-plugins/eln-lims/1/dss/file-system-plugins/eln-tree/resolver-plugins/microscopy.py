import script

acceptor.hideDataSetType("MICROSCOPY_IMG_CONTAINER")
acceptor.hideDataSetType("MICROSCOPY_IMG_OVERVIEW")
acceptor.hideDataSetType("MICROSCOPY_IMG_THUMBNAIL")
#acceptor.hideSampleType("MICROSCOPY_SAMPLE_TYPE")

def addSampleChildNodes(path, samplePermId, sampleType, response, acceptor, context):
    dataSets = script.getDataSetsOfSampleAndItsChildren(samplePermId, context)
    for dataSet in dataSets:
        if acceptor.acceptDataSet(dataSet):
            dataSetCode = dataSet.getCode()
            content = context.getContentProvider().asContent(dataSetCode)
            contentNode = content.getRootNode()
            script.addDataSetFileNodes(path, dataSetCode, contentNode, response, acceptor, context)

acceptor.sampleChildrenHandlers["MICROSCOPY_EXPERIMENT"] = addSampleChildNodes
