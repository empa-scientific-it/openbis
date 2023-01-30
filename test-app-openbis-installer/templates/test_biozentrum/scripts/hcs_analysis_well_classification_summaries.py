#! /usr/bin/env python
# This is an example Jython dropbox for importing feature vectors coming from analysis of image datasets
 
import commonImageDropbox
import commonDropbox

# Global variable storing AbstractPropertiesParser
datasetMetadataParser = None

def rollback_service(service, throwable):
    global datasetMetadataParser
    commonDropbox.createFailureStatus(datasetMetadataParser, throwable, incoming)
    
if incoming.isDirectory():
    datasetTypeCode = 'HCS_ANALYSIS_WELL_CLASSIFICATION_SUMMARIES'
    incomingPath = incoming.getPath()
    datasetMetadataParser = commonDropbox.DerivedDatasetMetadataParser(incomingPath)
    commonDropbox.registerFeaturesFromCsvMatrix(service, factory, state, incoming, datasetMetadataParser, datasetTypeCode)
