#! /usr/bin/env python

#   Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
# 
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
# 
#        http://www.apache.org/licenses/LICENSE-2.0
#   
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#

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
