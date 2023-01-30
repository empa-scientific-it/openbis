#! /usr/bin/env python
# Jython dropbox which is not used by iBrain2.
# It is suitable to upload CSV files with feature vectors with the API.

import commonDropbox
#reload(commonDropbox)

datasetTypeCode = 'HCS_ANALYSIS_WELL_CLASSIFICATION_SUMMARIES'

transaction = service.transaction()
featuresBuilder = commonDropbox.defineFeaturesFromCsvMatrix(incoming.getPath(), factory)
analysisRegistrationDetails = factory.createFeatureVectorRegistrationDetails(featuresBuilder, incoming)
dataset = transaction.createNewDataSet(analysisRegistrationDetails)
dataset.setFileFormatType('CSV')
dataset.setDataSetType(datasetTypeCode)
transaction.moveFile(incoming.getPath(), dataset)
