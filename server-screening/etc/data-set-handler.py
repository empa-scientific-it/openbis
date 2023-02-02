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

if incoming.isDirectory(): 
    imageDataset = ImageDataSetFlexible()
    imageDataset.setRawImageDatasetType()
    imageDataset.setPlate("TEST", incoming.getName())
    factory.registerImageDataset(imageDataset, incoming, service)

