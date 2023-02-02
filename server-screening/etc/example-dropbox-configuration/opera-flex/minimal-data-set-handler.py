import os
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import ImageMetadata, SimpleImageDataConfig, Location
from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import Geometry

from ch.systemsx.cisd.imagereaders.bioformats import FlexHelper
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.utils import DropboxUtils

""" 
The code needed to import Evotec/PerkinElmer Opera Flex files with images.
"""
class OperaFlexImageDataSet(SimpleImageDataConfig):

    """
    Returns meta-data for each image in the specified image container. 

    Parameter imagePath: path to the flex container with many images
    Parameter imageIdentifiers: identifiers of all images contained in the image file.
    """
    def extractImagesMetadata(self, imagePath, imageIdentifiers):
        tokens = []
        self.flexHelper = FlexHelper(incoming.getAbsolutePath() + '/' + imagePath)
        for image in imageIdentifiers:
            token = ImageMetadata()
            
            token.well = self.extractWellCode(imagePath)
            imageIndex = image.getTimeSeriesIndex()
            token.tileNumber = self.flexHelper.getTileNumber(imageIndex)
            token.channelCode = self.flexHelper.getChannelCode(imageIndex)
            
            token.imageIdentifier = image
            tokens.append(token)
        return tokens

    """ Extracts well code (e.g. C2) from the flex file path. """
    def extractWellCode(self, imagePath):
        basename = os.path.basename(imagePath)
        fileName = os.path.splitext(basename)[0]
        row = int(fileName[0:3])
        col = int(fileName[3:6])
        well = DropboxUtils.translateRowNumberIntoLetterCode(row) + str(col)
        return well

    """
    Calculates the width and height of the matrix of tiles (a.k.a. fields or sides) in the well 
    using the content of the Flex file. Passed parameters are not used.

    Returns:
        Geometry
    """
    def getTileGeometry(self, imageTokens, maxTileNumber):
        tileLocations = self._getTileLocationsMap().values()
        return DropboxUtils.figureGeometry(tileLocations)
    
    """
    For a given tile number and tiles geometry returns (x,y) which describes where the tile is
    located on the well.
    Uses flex file to find this out.
     
    Parameter tileNumber: number of the tile
    Parameter tileGeometry: the geometry of the well matrix
    Returns:
         Location
    """
    def getTileCoordinates(self, tileNumber, tileGeometry):
        return self._getTileLocationsMap().get(tileNumber)
    
    def _getTileLocationsMap(self):
        tileSpatialPoints = self.flexHelper.getTileCoordinates()
        # a maximal distance between two points so that they are still considered to describe one point
        precision = 1e-7
        return DropboxUtils.tryFigureLocations(tileSpatialPoints, precision)
    

if incoming.isDirectory():
    imageDataset = OperaFlexImageDataSet()
    imageDataset.setRawImageDatasetType()
    # we use BioFormat library to interpret images with .flex extension
    imageDataset.setImageLibrary("BioFormats", "TiffDelegateReader")
    imageDataset.setRecognizedImageExtensions(["flex"])
    
    # Here one can add more specific configuration code.
    # For simplisity we just assume that the plate sample "/MY-SPACE/MY-PLATE" already exists.
    imageDataset.setPlate("MY-SPACE", "MY-PLATE")
    
    # boilerplate code
    tr = service.transaction()
    dataSetRegistrationDetails = factory.createImageRegistrationDetails(imageDataset, incoming)
    dataSet = tr.createNewDataSet(dataSetRegistrationDetails)
    tr.moveFile(incoming.getAbsolutePath(), dataSet)

