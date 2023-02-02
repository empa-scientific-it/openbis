#! /usr/bin/env python
# This is an example Jython dropbox for importing feature vectors coming from analysis of image datasets from Biozentrum

import os
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import *

SEPARATOR = ","

def defineFeaturesBiozentrum(featuresBuilder, incoming):
    file = open(incoming.getPath())
    for header in file:
        headerTokens = header.split(SEPARATOR)
        featureCode = headerTokens[0]
        featureValues = featuresBuilder.defineFeature(featureCode)
        for rowValues in file:
            rowTokens = rowValues.split(SEPARATOR)
            rowLabel = rowTokens[0].strip()
            if len(rowLabel) == 0:
                break
            for column in range(1,len(headerTokens)):
                value = rowTokens[column].strip()
                well = rowLabel + str(column)
                #print featureCode, well, value
                featureValues.addValue(well, value)


# Specific code which defines the feature vector values for the dataset..
# Usually you will parse the content of the incoming file or directory to get the values.
# Here all the values are hard-coded for simplicity, 
# but the example shows which calls you need to perform in your parser.
# Parameters 
#     incoming: java.io.File which points to the incoming dataset
def defineFeatures(featuresBuilder, incoming):
        # define INFECTION_INDEX feature
        infectionFeature = featuresBuilder.defineFeature("INFECTION_INDEX")
        # optionally you can set the label and description of the feature
        infectionFeature.setFeatureLabel("Infection Index")
        infectionFeature.setFeatureDescription("What percentage of the cells in the well has been infected?")
        # set values for each well
        infectionFeature.addValue("A1", "3.432")
        # Instead of the well code you can use row and column numbers. For B1 it would be (2,1)
        infectionFeature.addValue(2, 1, "5.343")
        infectionFeature.addValue("C1", "0.987")

        # define QUALITY feature
        qualityFeature = featuresBuilder.defineFeature("QUALITY")
        qualityFeature.addValue("A1", "GOOD")
        qualityFeature.addValue("B1", "BAD")
        qualityFeature.addValue("C1", "GOOD")

def defineFeaturesForTimepoints(featuresBuilder, incoming):
        # define INFECTION_INDEX feature
        infectionFeature = featuresBuilder.defineFeature("INFECTION_INDEX")
        # Define the feature values for the timepoint 100. 
        # The second argument is the depth and can be used if depth-scans are performed.
        infectionFeature.changeSeries(100, None)
        infectionFeature.addValue("A1", "3.432")
        infectionFeature.addValue("B1", "5.343")
        infectionFeature.addValue("C1", "0.987")
        # Define the feature values for the timepoint 200. 
        infectionFeature.changeSeries(200, None)
        infectionFeature.addValue("A1", "1.652")
        infectionFeature.addValue("B1", "2.321")
        infectionFeature.addValue("C1", "0.121")

# Returns the code of the plate to which the dataset should be connected.
# Parameters 
#     incoming: java.io.File which points to the incoming dataset
def extractPlateCode(incoming):
    return os.path.splitext(incoming.getName())[0]

def extractSpaceCode(incoming):
    return "TEST"
                
# --- boilerplate code which register one dataset with image analysis results on the well level
featuresBuilder = factory.createFeaturesBuilder()
defineFeaturesBiozentrum(featuresBuilder, incoming) 
#defineFeatures(featuresBuilder, incoming) 
#defineFeaturesForTimepoints(featuresBuilder, incoming) 

analysisRegistrationDetails = factory.createFeatureVectorRegistrationDetails(featuresBuilder, incoming)
tr = service.transaction(incoming, factory)
analysisDataset = tr.createNewDataSet(analysisRegistrationDetails)

# set plate to which the dataset should be connected
sampleIdentifier = "/"+extractSpaceCode(incoming)+"/"+extractPlateCode(incoming)
plate = tr.getSample(sampleIdentifier)
analysisDataset.setSample(plate)

# store the original file in the dataset.
tr.moveFile(incoming.getPath(), analysisDataset)

# --- optional: other standard operations on analysisDataset can be performed (see IDataSet interface)
#analysisDataset.setFileFormatType("CSV")
#analysisDataset.setDataSetType("HCS_ANALYSIS_WELL_FEATURES")
#analysisDataset.setPropertyValue("DESCRIPTION", incoming.getName())
#analysisDataset.setParentDatasets(["20110302085840150-90"])
