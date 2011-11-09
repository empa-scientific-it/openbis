#! /usr/bin/env python

import commonImageDropbox
import commonDropbox

#reload(commonDropbox)
#reload(commonImageDropbox)

""" sample type code of the plate, needed if a new sample is registered automatically """
PLATE_TYPE_CODE = "PLATE"
SIRNA_EXP_TYPE = "SIRNA_HCS"
PLATE_GEOMETRY_PROPERTY_CODE = "$PLATE_GEOMETRY"

# Global variable storing AbstractPropertiesParser
datasetMetadataParser = None

def rollback_service(service, throwable):
    global datasetMetadataParser
    commonDropbox.createFailureStatus(datasetMetadataParser, throwable, incoming)

def createPlateWithExperimentIfNeeded(transaction, assayParser, plateCode, spaceCode):
    projectCode = assayParser.get(assayParser.EXPERIMENTER_PROPERTY)
    experiment = assayParser.get(assayParser.ASSAY_ID_PROPERTY)
    experimentDesc = assayParser.get(assayParser.ASSAY_DESC_PROPERTY)   
    experimentType = assayParser.get(assayParser.ASSAY_TYPE_PROPERTY)
    
    if transaction.getSpace(spaceCode) == None:
        transaction.createNewSpace(spaceCode, None)
    
    sampleIdentifier = "/"+spaceCode+"/"+plateCode
    plate = transaction.getSample(sampleIdentifier)
    if plate == None:
        projectIdent = "/" + spaceCode +"/" + projectCode
        if transaction.getProject(projectIdent) == None:
            transaction.createNewProject(projectIdent)
        expIdentifier = projectIdent + "/"+experiment
        experiment = transaction.getExperiment(expIdentifier)
        if experiment == None:
            experiment = transaction.createNewExperiment(expIdentifier, SIRNA_EXP_TYPE)
            openbisExpDesc = experimentDesc + " (type: "+experimentType + ")"
            experiment.setPropertyValue("DESCRIPTION", openbisExpDesc)

        plate = transaction.createNewSample(sampleIdentifier, PLATE_TYPE_CODE)
        plate.setExperiment(experiment)
    return plate

if incoming.isDirectory():
    incomingPath = incoming.getPath()
    datasetMetadataParser = commonDropbox.AcquiredDatasetMetadataParser(incomingPath)
    iBrain2DatasetId = datasetMetadataParser.getIBrain2DatasetId()
    assayParser = commonDropbox.AssayParser(incomingPath)

    imageDataset = commonImageDropbox.IBrain2ImageDataSetConfig()
    imageDataset.setRawImageDatasetType()
    imageDataset.setFileFormatType("TIFF")
    imageDataset.setRecognizedImageExtensions(["tif", "tiff"])    
    imageDataset.setStoreChannelsOnExperimentLevel(False)
    imageDataset.setGenerateThumbnails(True)
    imageDataset.setMaxThumbnailWidthAndHeight(imageDataset.THUMBANAIL_SIZE)
    imageDataset.setUseImageMagicToGenerateThumbnails(False)
    imageDataset.setAllowedMachineLoadDuringThumbnailsGeneration(1/2.0)
    imageDataset.setImageLibrary("BioFormats", "TiffDelegateReader")

    tr = service.transaction(incoming, factory)

    plateCode = datasetMetadataParser.getPlateCode()
    space = assayParser.get(assayParser.LAB_LEADER_PROPERTY)
    plate = createPlateWithExperimentIfNeeded(tr, assayParser, plateCode, space)        
    imageDataset.setPlate(space, plateCode)
    
    imageRegistrationDetails = factory.createImageRegistrationDetails(imageDataset, incoming)
    for propertyCode, value in datasetMetadataParser.getDatasetPropertiesIter():
        imageRegistrationDetails.setPropertyValue(propertyCode, value)

    plateGeometry = plate.getPropertyValue(PLATE_GEOMETRY_PROPERTY_CODE)
    if plateGeometry is None or len(plateGeometry) == 0:
        plateGeometry = factory.figureGeometry(imageRegistrationDetails)
        plate.setPropertyValue(PLATE_GEOMETRY_PROPERTY_CODE, plateGeometry)
    
    dataset = tr.createNewDataSet(imageRegistrationDetails)
    if tr.commit():
        commonDropbox.createSuccessStatus(iBrain2DatasetId, dataset, incomingPath)
