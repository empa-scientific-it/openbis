import os
from ch.systemsx.cisd.openbis.dss.etl.dto.api import ChannelColor
from ch.systemsx.cisd.openbis.dss.etl.dto.api import SimpleImageDataConfig
from ch.systemsx.cisd.openbis.dss.etl.dto.api import ImageMetadata
from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import Geometry

SPACE_CODE = "TEST"
PROJECT_CODE = "TEST-PROJECT"
PROJECT_ID = "/%(SPACE_CODE)s/%(PROJECT_CODE)s" % vars()
EXPERIMENT_CODE = "DEMO-EXP-HCS"
EXPERIMENT_ID = "/%(SPACE_CODE)s/%(PROJECT_CODE)s/%(EXPERIMENT_CODE)s" % vars()

PLATE_CODE = "PLATE"
PLATE_ID = "/%(SPACE_CODE)s/%(PLATE_CODE)s" % vars()
PLATE_ID_WITH_PROJECT = "/%(SPACE_CODE)s/%(PROJECT_CODE)s/%(PLATE_CODE)s" % vars()
PLATE_GEOMETRY_PROPERTY_CODE = "$PLATE_GEOMETRY"
PLATE_GEOMETRY = "384_WELLS_16X24"


def create_space_if_needed(transaction):
    space = transaction.getSpace(SPACE_CODE)
    if None == space:
        space = transaction.createNewSpace(SPACE_CODE, None)
        transaction.getLogger().info('Creating new space: ' + SPACE_CODE)
        space.setDescription("A demo space")

def create_project_if_needed(transaction):
    project = transaction.getProject(PROJECT_ID)
    if None == project:
        create_space_if_needed(transaction)
        project = transaction.createNewProject(PROJECT_ID)
        transaction.getLogger().info('Creating new project: ' + PROJECT_ID)
        project.setDescription("A demo project")
        
def create_experiment_if_needed(transaction):
    """ Get the specified experiment or register it if necessary """
    exp = transaction.getExperiment(EXPERIMENT_ID)
    if None == exp:
        create_project_if_needed(transaction)
        transaction.getLogger().info('Creating new experiment: ' + EXPERIMENT_ID)
        exp = transaction.createNewExperiment(EXPERIMENT_ID, 'SIRNA_HCS')
        
    return exp
    
def create_plate_if_needed(transaction):
    """ Get the specified sample or register it if necessary """
    if transaction.serverInformation.get('project-samples-enabled') == 'true':
        plate_id = PLATE_ID_WITH_PROJECT
    else:
        plate_id = PLATE_ID

    samp = transaction.getSample(plate_id)

    if None == samp:
        exp = create_experiment_if_needed(transaction)
        samp = transaction.createNewSample(plate_id, 'PLATE')
        transaction.getLogger().info('Creating new plate: ' + plate_id)
        samp.setPropertyValue(PLATE_GEOMETRY_PROPERTY_CODE, PLATE_GEOMETRY)
        samp.setExperiment(exp)
        
    return samp

     
class MyImageDataSetConfig(SimpleImageDataConfig):
    def extractImageMetadata(self, imagePath):
     
        basename = os.path.splitext(imagePath)[0]
        (plate, well, tile, channelCode) = basename.split("_")
         
        image_tokens = ImageMetadata()
        image_tokens.well = well
        try:
            image_tokens.tileNumber = int(tile)
        except ValueError:
            raise Exception("Cannot parse field number from '" + tile + "' in '" + basename + "' file name.")
     
        image_tokens.channelCode = channelCode
        return image_tokens
    
    def getChannelColor(self, channelCode):
        dict = { "GFP" : ChannelColor.GREEN, "DAPI" : ChannelColor.BLUE, "CY3" : ChannelColor.RED }
        if channelCode in dict:
            return dict[channelCode]
        else:
            return None    
        
    def getTileGeometry(self, imageTokens, maxTileNumber):
        return Geometry.createFromRowColDimensions(maxTileNumber / 3, 3)    

def process(transaction): 
    incoming = transaction.getIncoming()
    if incoming.isDirectory():
        imageDataset = MyImageDataSetConfig()
        imageDataset.setRawImageDatasetType()
        imageDataset.setGenerateThumbnails(True)
        imageDataset.setUseImageMagicToGenerateThumbnails(False)
        imageDataset.addGeneratedImageRepresentationWithResolution("512x512")
        plate = create_plate_if_needed(transaction)
        dataset = transaction.createNewImageDataSet(imageDataset, incoming);
        dataset.setSample(plate)
        transaction.moveFile(incoming.getPath(), dataset);
