#!/usr/bin/env python

"""
An Jython dropbox for importing microscopy image datasets produced by the scripts that generate platonic microscopy data.

The folder loaded to the dropbox folder should have the same name as the sample that the data will be attached to.
"""

import os
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import SimpleImageDataConfig, ImageMetadata, Location
from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import Geometry

    
class ImageDataSetFlexible(SimpleImageDataConfig):
    """
    Extracts tile number, channel code and well code for a given relative path to an image.
    Will be called for each file found in the incoming directory which has the allowed image extension.
      
    Example file name: bDZ01-1A_w_s3_z123_t321_cGFP
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
        
        image_tokens.well = None
        image_tokens.tileNumber = 1    
        image_tokens.channelCode = token_dict["c"]
        image_tokens.timepoint = float(token_dict["t"])
        image_tokens.depth = float(token_dict["z"])
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
        return Geometry.createFromRowColDimensions(1, 1);
    
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
        return Location(1, 1)

def process(transaction):
  incoming = transaction.getIncoming()
  if incoming.isDirectory(): 
    imageDataset = ImageDataSetFlexible()
    imageDataset.setMicroscopyData(True)
    imageDataset.setDataSetType("MICROSCOPY_IMG")
    imageDataset.addGeneratedImageRepresentationWithResolution('128x128')
    imageDataset.setMeasuredData(True)
    imageDataset.setPlate("PLATONIC", incoming.getName())
    dataSet = transaction.createNewImageDataSet(imageDataset, incoming)
    transaction.moveFile(incoming.getPath(), dataSet)

