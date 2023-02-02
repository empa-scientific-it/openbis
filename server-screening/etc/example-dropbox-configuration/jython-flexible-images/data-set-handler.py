#! /usr/bin/env python
# This is an example Jython dropbox for importing HCS image datasets

import os
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import *
from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import Geometry

    
class ImageDataSetFlexible(SimpleImageDataConfig):
    """
    Extracts tile number, channel code and well code for a given relative path to an image.
    Will be called for each file found in the incoming directory which has the allowed image extension.
      
    Example file name: bDZ01-1A_wD17_s3_z123_t321_cGFP
    Returns:
        ImageMetadata
    """
    def extractImageMetadata(self, imagePath):
        image_tokens = ImageMetadata()
    
        basename = os.path.splitext(imagePath)[0]
        # 
        token_dict = {}
        for token in basename.split("_"):
            token_dict[token[:1]] = token[1:]
        
        image_tokens.well = token_dict["w"]
        fieldText = token_dict["s"]
        try:
            image_tokens.tileNumber = int(fieldText)
        except ValueError:
            raise Exception("Cannot parse field number from '" + fieldText + "' in '" + basename + "' file name.")
    
        image_tokens.channelCode = token_dict["c"]
        return image_tokens

    """
    Overrides the default implementation which returns (1, maxTileNumber) geometry.
    
    Calculates the width and height of the matrix of tiles (a.k.a. fields or sides) in the well.
    
    Parameter imageMetadataList: a list of metadata for each encountered image
    Parameter maxTileNumber: the biggest tile number among all encountered images
    Returns:
        Geometry
    """
    def getTileGeometry(self, imageTokens, maxTileNumber):
        return Geometry.createFromRowColDimensions(maxTileNumber / 3, 3);
    
    """
    Overrides the default implementation which does the same thing (to demonstrate how this can be done). 
    
    For a given tile number and tiles geometry returns (x,y) which describes where the tile is
    located on the well.
    
    Parameter tileNumber: number of the tile
    Parameter tileGeometry: the geometry of the well matrix
    Returns:
         Location
    """
    def getTileCoordinates(self, tileNumber, tileGeometry):
        columns = tileGeometry.getWidth()
        row = ((tileNumber - 1) / columns) + 1
        col = ((tileNumber - 1) % columns) + 1
        return Location(row, col)

""" sample type code of the plate, needed if a new sample is registered automatically """
PLATE_TYPE_CODE = "PLATE"
SIRNA_EXP_TYPE = "SIRNA_HCS"
DEFAULT_SPACE = "TEST"
""" project and experiment where new plates will be registered """
DEFAULT_PROJECT_CODE = "TEST-PROJECT"
DEFAULT_EXPERIMENT_CODE = "E1"

PLATE_GEOMETRY_PROPERTY_CODE = "$PLATE_GEOMETRY"
PLATE_GEOMETRY = "384_WELLS_16X24"

def createTransactionAndSampleWithExperiment(plateCode, experimentCode):
    tr = service.transaction(incoming, factory)
    
    sampleIdentifier = "/"+DEFAULT_SPACE+"/"+plateCode
    plate = tr.getSample(sampleIdentifier)
    if plate == None:
        expIdentifier = "/"+DEFAULT_SPACE+"/"+DEFAULT_PROJECT_CODE+"/"+experimentCode
        experiment = tr.getExperiment(expIdentifier)
        if experiment == None:
            experiment = tr.createNewExperiment(expIdentifier, SIRNA_EXP_TYPE)
            experiment.setPropertyValue("MICROSCOPE", "BD_PATHWAY_855")
            experiment.setPropertyValue("DESCRIPTION", "koko")

        plate = tr.createNewSample(sampleIdentifier, PLATE_TYPE_CODE)
        plate.setPropertyValue(PLATE_GEOMETRY_PROPERTY_CODE, PLATE_GEOMETRY)
        plate.setExperiment(experiment)
        tr.commit()
        tr = service.transaction(incoming, factory)
    return tr, plate

if incoming.isDirectory(): 
    tokens = incoming.getName().split(".")
    if len(tokens) < 2:
        print "Invalid name:", incoming.getName()
        exit
    plateCode = tokens[0]
    experimentCode = tokens[1]
    
    config = ImageDataSetFlexible()
    config.setRawImageDatasetType()
    config.setPlate(DEFAULT_SPACE, plateCode)
    #factory.registerImageDataset(config, incoming, service)
    imageRegistrationDetails = factory.createImageRegistrationDetails(config, incoming)

    tr, plate = createTransactionAndSampleWithExperiment(plateCode, experimentCode)
    
    imageDataset = tr.createNewDataSet(imageRegistrationDetails)
    imageDataset.setSample(plate)
    imageDataSetFolder = tr.moveFile(incoming.getPath(), imageDataset)
    imageDatasetCode = imageDataset.getDataSetCode()
    print "Registered dataset:", imageDatasetCode
    tr.commit()

