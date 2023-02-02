"""
An Jython dropbox for importing HCS image datasets produced by the scripts that generate platonic screening data.

The folder loaded to the dropbox folder should have the same name as the plate that the data will be attached to.
"""

import os
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import SimpleImageDataConfig, ImageMetadata, Location, Channel, ChannelColor, ChannelColorComponent
from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import Geometry
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.transformations import ImageTransformationBuffer
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.thumbnails import ResolutionBasedThumbnailsConfiguration

SPACE_CODE = "TEST"
PROJECT_CODE = "TEST-PROJECT"
PROJECT_ID = "/%(SPACE_CODE)s/%(PROJECT_CODE)s" % vars()
EXPERIMENT_CODE = "PLATONIC_PLATE_IMAGE_DROPBOX_TEST"
EXPERIMENT_ID = "/%(SPACE_CODE)s/%(PROJECT_CODE)s/%(EXPERIMENT_CODE)s" % vars()

PLATE_CODE = "PLATONIC_PLATE_IMAGE_DROPBOX_TEST"
PLATE_ID = "/%(SPACE_CODE)s/%(PROJECT_CODE)s/%(PLATE_CODE)s" % vars()
PLATE_GEOMETRY_PROPERTY_CODE = "$PLATE_GEOMETRY"
PLATE_GEOMETRY = "96_WELLS_8x12"


def create_space_if_needed(transaction):
    space = transaction.getSpace(SPACE_CODE)
    if None == space:
        space = transaction.createNewSpace(SPACE_CODE, None)
        space.setDescription("A test space")

def create_project_if_needed(transaction):
    project = transaction.getProject(PROJECT_ID)
    if None == project:
        create_space_if_needed(transaction)
        project = transaction.createNewProject(PROJECT_ID)
        project.setDescription("A test project")
        
def create_experiment_if_needed(transaction):
    """ Get the specified experiment or register it if necessary """
    exp = transaction.getExperiment(EXPERIMENT_ID)
    if None == exp:
        create_project_if_needed(transaction)
        print 'Creating new experiment : ' + EXPERIMENT_ID
        exp = transaction.createNewExperiment(EXPERIMENT_ID, 'SIRNA_HCS')
        exp.setPropertyValue("DESCRIPTION", "A sample experiment")
        
    return exp
    
def create_plate_if_needed(transaction):
    """ Get the specified sample or register it if necessary """

    samp = transaction.getSample(PLATE_ID)

    if None == samp:
        exp = create_experiment_if_needed(transaction)
        samp = transaction.createNewSample(PLATE_ID, 'PLATE')
        samp.setPropertyValue(PLATE_GEOMETRY_PROPERTY_CODE, PLATE_GEOMETRY)
        samp.setExperiment(exp)
        
    return samp


class ImageDataSetFlexible(SimpleImageDataConfig):
  def extractImageMetadata(self, imagePath):
    """
    Extracts tile number, channel code and well code for a given relative path to an image.
    Will be called for each file found in the incoming directory which has the allowed image extension.
    
    Example file name: bDZ01-1A_wD17_s3_z123_t321_cGFP
    Returns:
      ImageMetadata
    """
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

  def getTileGeometry(self, imageTokens, maxTileNumber):
    """
    Overrides the default implementation which returns (1, maxTileNumber) geometry.
  
    Calculates the width and height of the matrix of tiles (a.k.a. fields or sides) in the well.
  
    Parameter imageMetadataList: a list of metadata for each encountered image
    Parameter maxTileNumber: the biggest tile number among all encountered images
    Returns:
      Geometry
    """
    return Geometry.createFromRowColDimensions(maxTileNumber / 3, 3);
  
  def getTileCoordinates(self, tileNumber, tileGeometry):
    """
    Overrides the default implementation which does the same thing (to demonstrate how this can be done). 
  
    For a given tile number and tiles geometry returns (x,y) which describes where the tile is
    located on the well.
  
    Parameter tileNumber: number of the tile
    Parameter tileGeometry: the geometry of the well matrix
    Returns:
       Location
    """
    columns = tileGeometry.getWidth()
    row = ((tileNumber - 1) / columns) + 1
    col = ((tileNumber - 1) % columns) + 1
    return Location(row, col)
    

def process(transaction):
  incoming = transaction.getIncoming()
  if incoming.isDirectory(): 
    imageDataset = ImageDataSetFlexible()
    imageDataset.setRawImageDatasetType()
    imageDataset.setUseImageMagicToGenerateThumbnails(False)
    for resolution in ['64x64', '128x128']:
      representation = imageDataset.addGeneratedImageRepresentationWithResolution(resolution)
      representation.setFileFormat('JPEG')
    imageDataset.addGeneratedImageRepresentationWithResolution('256x256')

    channels = [ Channel(code, code) for code in ["DAPI", "GFP", "CY3"]]
    colorComponents = [ ChannelColorComponent.BLUE, ChannelColorComponent.GREEN, ChannelColorComponent.RED]
    imageDataset.setChannels(channels, colorComponents)
    
    # Create the data set
    dataSet = transaction.createNewImageDataSet(imageDataset, incoming)
    plate = create_plate_if_needed(transaction)
    dataSet.setSample(plate)
    transaction.moveFile(incoming.getPath(), dataSet)