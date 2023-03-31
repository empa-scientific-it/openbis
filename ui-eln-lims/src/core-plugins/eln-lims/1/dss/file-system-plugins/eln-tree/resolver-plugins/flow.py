import script

def addSampleChildNodes(path, samplePermId, sampleType, response, acceptor, context):
    dataSets = script.getDataSetsOfSampleAndItsChildren(samplePermId, context)
    filteredDataSets = []
    for dataSet in dataSets:
        if acceptor.acceptDataSet(dataSet):
            filteredDataSets.append(dataSet)
    script.addDataSetFileNodesFor(path, filteredDataSets, response, acceptor, context)

def addSampleChildNodesWithPlates(path, samplePermId, sampleType, response, acceptor, context):
    dataSets = script.getDataSetsOfSampleAndItsChildren(samplePermId, context)
    filteredDataSets = []
    for dataSet in dataSets:
        sampleTypeCode = dataSet.getSample().getType().getCode()
        if not sampleTypeCode.endswith("_WELL"):
            filteredDataSets.append(dataSet)
    script.addDataSetFileNodesFor(path, filteredDataSets, response, acceptor, context)
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
