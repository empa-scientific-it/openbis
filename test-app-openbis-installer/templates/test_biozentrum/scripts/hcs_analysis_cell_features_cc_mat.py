#! /usr/bin/env python

import commonDropbox
#reload(commonDropbox)

datasetType = 'HCS_ANALYSIS_CELL_FEATURES_CC_MAT'
fileType = 'MAT'

# Global variable storing AbstractPropertiesParser
datasetMetadataParser = None

def rollback_service(service, throwable):
    global datasetMetadataParser
    commonDropbox.createFailureStatus(datasetMetadataParser, throwable, incoming)
            
if incoming.isDirectory():
    datasetMetadataParser = commonDropbox.DerivedDatasetMetadataParser(incoming.getPath())
    commonDropbox.registerDerivedBlackBoxDataset(state, service, factory, incoming, datasetMetadataParser, datasetType, fileType)