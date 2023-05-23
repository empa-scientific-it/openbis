import script

acceptor.hideDataSetType("MICROSCOPY_IMG_CONTAINER")
acceptor.hideDataSetType("MICROSCOPY_IMG_OVERVIEW")
acceptor.hideDataSetType("MICROSCOPY_IMG_THUMBNAIL")
#acceptor.hideSampleType("MICROSCOPY_SAMPLE_TYPE")

def addSampleChildNodes(path, samplePermId, sampleType, response, acceptor, context):
    dataSets = script.getDataSetsOfSampleAndItsChildren(samplePermId, context)
    filteredDataSets = []
    for dataSet in dataSets:
        if acceptor.acceptDataSet(dataSet):
            filteredDataSets.append(dataSet)
#    print("ADD SAMPLE CHILD NODES: %s of %s, sample: %s" % (len(filteredDataSets),len(dataSets),samplePermId))
    script.addDataSetFileNodesFor(path, filteredDataSets, response, acceptor, context)

acceptor.sampleChildrenHandlers["MICROSCOPY_EXPERIMENT"] = addSampleChildNodes
