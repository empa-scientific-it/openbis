#! /usr/bin/env python

import os
from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import Geometry
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import SimpleImageDataConfig
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import ImageMetadata   
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import ChannelColor

class IBrain2ImageDataSetConfig(SimpleImageDataConfig):
    THUMBANAIL_SIZE = 200
    
    def extractImageMetadata(self, imagePath):
        basename = self.getFileBasename(imagePath)
        token_dict = {}
        for token in basename.split("_"):
            token_dict[token[:1]] = token[1:]

        image_tokens = ImageMetadata()
        image_tokens.well = token_dict["w"]
        image_tokens.tileNumber = self.fieldAsInt(token_dict["s"], basename)
        image_tokens.channelCode = self.extractChannelCode(token_dict, basename)
        return image_tokens

    def extractChannelCode(self, token_dict, basename):
        return token_dict["c"]
    
    def getFileBasename(self, filePath):
        return os.path.splitext(filePath)[0]
    
    def fieldAsInt(self, fieldText, basename):
        try:
            return int(fieldText)
        except ValueError:
            raise Exception("Cannot parse field number from '" + fieldText + "' in '" + basename + "' file name.")

    def geom(self, row, col):
		return Geometry.createFromRowColDimensions(row, col)

    """ 
    Parameters:
        image_tokens_list - list of ImageTokens
    Returns:  (rows, columns) tuple describing the matrix of tiles (aka fields or sides) in the well  
    """
    def getTileGeometry(self, imageTokens, maxTileNumber):
        # if a number of tiles is strange, assume that one tile is missing
        if maxTileNumber == 5 or maxTileNumber == 7 or maxTileNumber == 11 or maxTileNumber == 13:
            maxTileNumber = maxTileNumber + 1
    
        if maxTileNumber % 4 == 0 and maxTileNumber != 4:
            return self.geom(4, maxTileNumber / 4) # (4,2), (4,4)
        elif maxTileNumber % 3 == 0:
            return self.geom(maxTileNumber / 3, 3) # (3,3), (4,3), (5,3)
        elif maxTileNumber % 2 == 0:
            return self.geom(maxTileNumber / 2, 2) # (2,2), (3,2), (5,2), (7,2)
        else:
            return self.geom(maxTileNumber, 1)

    def getChannelColor(self, channelCode):
        # codes should be in upper case
        dict = { "GFP" : ChannelColor.GREEN,
                 "DAPI" : ChannelColor.BLUE,
                 "CY5" : ChannelColor.RED }
        if channelCode in dict:
            return dict[channelCode]
        else:
            return None
           
class IBrain2SegmentationImageDataSetConfig(IBrain2ImageDataSetConfig):
    def extractChannelCode(self, token_dict, basename):
        return basename.split("_")[-1] + " ("+ token_dict["c"] + ")"
    
            
