#! /usr/bin/env python

import commonDropbox
#reload(commonDropbox)

datasetType = 'CLUSTER_JOB_LOGS'
fileType = 'TXT'

# Global variable storing AbstractPropertiesParser
datasetMetadataParser = None

def rollback_service(service, throwable):
    global datasetMetadataParser
    commonDropbox.createFailureStatus(datasetMetadataParser, throwable, incoming)
            
if incoming.isDirectory():
    datasetMetadataParser = commonDropbox.DerivedDatasetMetadataParser(incoming.getPath())
    commonDropbox.registerDerivedBlackBoxDataset(state, service, factory, incoming, datasetMetadataParser, datasetType, fileType)
