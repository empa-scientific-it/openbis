import os
import glob
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import SimpleImageDataConfig
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import ImageMetadata
from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import Geometry
from ch.systemsx.cisd.openbis.dss.shared import CellomicsMDBMetadataReader

SPACE_CODE = "TEST"
PROJECT_CODE = "TEST-PROJECT"
PROJECT_ID = "/%(SPACE_CODE)s/%(PROJECT_CODE)s" % vars()
EXPERIMENT_CODE = "DEMO-EXP-HCS"
EXPERIMENT_ID = "/%(SPACE_CODE)s/%(PROJECT_CODE)s/%(EXPERIMENT_CODE)s" % vars()

PLATE_CODE = "PLATE1"
PLATE_ID = "/%(SPACE_CODE)s/%(PLATE_CODE)s" % vars()
PLATE_GEOMETRY_PROPERTY_CODE = "$PLATE_GEOMETRY"
PLATE_GEOMETRY = "384_WELLS_16X24"


def create_space_if_needed(transaction):
    space = transaction.getSpace(SPACE_CODE)
    if None == space:
        space = transaction.createNewSpace(SPACE_CODE, None)
        space.setDescription("A demo space")

def create_project_if_needed(transaction):
    project = transaction.getProject(PROJECT_ID)
    if None == project:
        create_space_if_needed(transaction)
        project = transaction.createNewProject(PROJECT_ID)
        project.setDescription("A demo project")
        
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

     
class MyImageDataSetConfig(SimpleImageDataConfig):
    def extractImageMetadata(self, imagePath):
     
        basename = os.path.splitext(imagePath)[0]        
        value = basename.split("_")[2]
        plate = basename.split("_")[1]
        well = value[0:3]
        tile = value[4:6]
        tile = int(tile) + 1
        channelCode = value[6:8]     
        
        image_tokens = ImageMetadata()
        image_tokens.well = well
        try:
            image_tokens.tileNumber = int(tile)
        except ValueError:
            raise Exception("Cannot parse field number from '" + tile + "' in '" + basename + "' file name.")
     
        image_tokens.channelCode = channelCode
        return image_tokens
    
    def getTileGeometry(self, imageTokens, maxTileNumber):
        return Geometry.createFromRowColDimensions((maxTileNumber - 1) / 3 + 1, 3)    

def extractMetadataFromMDBFile(incoming):
    mdbFiles = glob.glob("%s/*.mdb" % incoming.getAbsoluteFile())
    
    if (len(mdbFiles) > 0):
        mdbReader = CellomicsMDBMetadataReader(mdbFiles[0])
        mdbReader.writeAllMetadata(incoming)

if incoming.isDirectory():
    extractMetadataFromMDBFile(incoming)
    imageDataset = MyImageDataSetConfig()
    imageDataset.setRawImageDatasetType()
    imageDataset.setGenerateThumbnails(True)
    imageDataset.setUseImageMagicToGenerateThumbnails(False)
    imageDataset.setImageLibrary('BioFormats', 'CellomicsReader##SINGLE_IMAGE')
    registrationDetails = factory.createImageRegistrationDetails(imageDataset, incoming)  
    transaction = service.transaction(incoming, factory)
    plate = create_plate_if_needed(transaction)
    dataset = transaction.createNewDataSet(registrationDetails);
    dataset.setSample(plate)
    transaction.moveFile(incoming.getPath(), dataset);
