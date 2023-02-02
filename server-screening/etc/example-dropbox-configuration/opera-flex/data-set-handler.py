import os
from datetime import datetime

from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import ImageMetadata, SimpleImageDataConfig, Location
from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import Geometry

from java.io import File
from ch.systemsx.cisd.imagereaders import ImageReaderFactory, ImageID
from ch.systemsx.cisd.imagereaders.bioformats import FlexHelper
from ch.systemsx.cisd.openbis.dss.etl import TileGeometryOracle
from ch.systemsx.cisd.openbis.plugin.screening.shared.basic import PlateUtils

class OperaFlexImageDataSet(SimpleImageDataConfig):

    def extractImagesMetadata(self, imagePath, imageIdentifiers):
        tokens = []
        self.flexHelper = FlexHelper(incoming.getAbsolutePath() + '/' + imagePath)
        for image in imageIdentifiers:
            
            token = ImageMetadata()
            
            ix = image.getTimeSeriesIndex()
            
            basename = os.path.basename(imagePath)
            fileName = os.path.splitext(basename)[0]
            row = int(fileName[0:3])
            col = int(fileName[3:6])
            token.well = PlateUtils.translateRowNumberIntoLetterCode(row) + str(col)
                        
            token.tileNumber = self.flexHelper.getTileNumber(ix)
            token.channelCode = self.flexHelper.getChannelCode(ix)
            token.imageIdentifier = image
            #print token
            tokens.append(token)
        return tokens

    def getTileGeometry(self, imageTokens, maxTileNumber):
        tileLocations = self.getTileLocationsMap().values()
        return TileGeometryOracle.figureGeometry(tileLocations)
    
    def getTileCoordinates(self, tileNumber, tileGeometry):
        return self.getTileLocationsMap().get(tileNumber)
    
    def getTileLocationsMap(self):
        tileSpatialPoints = self.flexHelper.getTileCoordinates()
        return TileGeometryOracle.tryFigureLocations(tileSpatialPoints, 1e-7)
    

def getExperiment(tr, expId):
    exp = tr.getExperiment(expId)
    if not exp:
        exp = tr.createNewExperiment(expId, "SIRNA_HCS")
        exp.setPropertyValue("DESCRIPTION", "Test description")
    return exp

def createPlate(tr, space, expId, plateGeometry):
    now_str = datetime.today().strftime('%Y%m%d-%H%M%S')
    plateId = '/' + space + '/' + now_str
    plate = tr.getSample(plateId)
    if not plate:
        exp = getExperiment(tr, expId)
        plate = tr.createNewSample(plateId, "PLATE")
        plate.setExperiment(exp)
        plate.setPropertyValue("$PLATE_GEOMETRY", plateGeometry)
    return plate
    
    
if incoming.isDirectory():
    imageDataset = OperaFlexImageDataSet()
    imageDataset.setRawImageDatasetType()
    imageDataset.setImageLibrary("BioFormats", "TiffDelegateReader")
    imageDataset.setRecognizedImageExtensions(["flex"])
    
    tr = service.transaction()
    
    dataSetRegistrationDetails = factory.createImageRegistrationDetails(imageDataset, incoming)
    dataSet = tr.createNewDataSet(dataSetRegistrationDetails)
    
    spaceId = 'TEST'
    experimentId = '/TEST/TEST-PROJECT/EXP1'
    plateGeometry = factory.figureGeometry(dataSetRegistrationDetails)
    plate = createPlate(tr, spaceId, experimentId, plateGeometry)
    dataSet.setSample(plate)
    
    tr.moveFile(incoming.getAbsolutePath(), dataSet)
