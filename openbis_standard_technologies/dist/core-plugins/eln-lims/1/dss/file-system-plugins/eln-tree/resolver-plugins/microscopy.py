acceptor.hideDataSetType("MICROSCOPY_IMG_CONTAINER")
acceptor.hideSampleType("MICROSCOPY_SAMPLE_TYPE")

acceptor.dataSetSearchCriteriaExtender.append(lambda c, id: extendDataSetSearchCriteriaForMicroscopy(c, id))

def extendDataSetSearchCriteriaForMicroscopy(dataSetSearchCriteria, samplePermId):
    dataSetSearchCriteria.withOrOperator()
    parentsSearchCriteria = dataSetSearchCriteria.withSample().withParents()
    parentsSearchCriteria.withType().withCode().thatEquals("MICROSCOPY_EXPERIMENT")
    parentsSearchCriteria.withPermId().thatEquals(samplePermId)
