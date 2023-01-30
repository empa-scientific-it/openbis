#! /usr/bin/env python

import commonImageDropbox
import commonDropbox
import os
from java.util import Properties

# Global variable storing AbstractPropertiesParser
datasetMetadataParser = None

def rollback_service(service, throwable):
    global datasetMetadataParser
    commonDropbox.createFailureStatus(datasetMetadataParser, throwable, incoming)
    
def findPerWellCSVFile(dir):
    suffix = ".per-well-classified.csv"
    for file in os.listdir(dir):
            if file.endswith(suffix):
                    return dir + "/" + file
    raise Exception("No file with suffix '" + suffix + "' has been found in "+dir)

def getConfigurationProperties():
    config = Properties()
    config.setProperty("separator", ",")
    config.setProperty("well-name-row", "Well_Name")
    config.setProperty("well-name-col", "Well_Name")
    config.setProperty("well-name-col-is-alphanum", "true")
    return config
    
def register(incomingPath):
    global datasetMetadataParser
    datasetMetadataParser = commonDropbox.DerivedDatasetMetadataParser(incomingPath)
    openbisDatasetParent = datasetMetadataParser.getParentDatasetPermId()
    datasetTypeCode = 'HCS_ANALYSIS_WELL_QUALITY_SUMMARY'

    transaction = service.transaction(incoming, factory)
    #vincent 09-08-2011 added comment
    #commonDropbox.ensureOrDieNoChildrenOfType(openbisDatasetParent, datasetTypeCode, incomingPath, transaction)

    configProps = getConfigurationProperties()
    incomingCsvFile = findPerWellCSVFile(incomingPath)
    analysisRegistrationDetails = factory.createFeatureVectorRegistrationDetails(incomingCsvFile, configProps)
    analysisProcedure = datasetMetadataParser.getAnalysisProcedure()
    analysisRegistrationDetails.getDataSetInformation().setAnalysisProcedure(analysisProcedure)
    
    dataset = transaction.createNewDataSet(analysisRegistrationDetails)
    dataset.setDataSetType(datasetTypeCode)
    dataset.setFileFormatType('CSV')
    commonDropbox.registerDerivedDataset(state, service, transaction, dataset, incoming, datasetMetadataParser)
 
if incoming.isDirectory():
    register(incoming.getPath())
