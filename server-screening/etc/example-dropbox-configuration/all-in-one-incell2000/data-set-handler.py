#! /usr/bin/env python
# This is an example Jython dropbox for importing HCS image datasets

import os
import shutil
import random

import ch.systemsx.cisd.openbis.generic.shared.basic.dto as dto
from ch.systemsx.cisd.openbis.generic.shared.basic.dto import SampleType, NewSample
from ch.systemsx.cisd.openbis.generic.shared.dto.identifier import SampleIdentifier
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import *
from ch.systemsx.cisd.openbis.dss.etl.custom.geexplorer import GEExplorerImageAnalysisResultParser
from java.io import File

# ------------
# Dropbox specific image dataset registration. You may want to modify this part.
# ------------

""" type of the new image dataset """
IMAGE_DATASET_TYPE = "HCS_IMAGE"
""" file format code of files in a new image dataset """
IMAGE_FILE_FORMAT = "TIFF"

""" type of the new analysis dataset """
ANALYSIS_DATASET_TYPE = "HCS_IMAGE_ANALYSIS_DATA"
""" file format of the analysis dataset """
ANALYSIS_FILE_FORMAT = "CSV"

""" type of the new image overlay dataset """
OVERLAY_IMAGE_DATASET_TYPE = "HCS_IMAGE_SEGMENTATION_OVERLAY"
""" file format of the image overlay dataset """
OVERLAY_IMAGE_FILE_FORMAT = "PNG"

""" space where the plate for which the dataset has been acquired exist """
PLATE_SPACE = "DEMO"

""" only files with these extensions will be recognized as images """
RECOGNIZED_IMAGES_EXTENSIONS = ["tiff", "tif", "png", "gif", "jpg", "jpeg"]

# ---------

""" sample type code of the plate, needed if a new sample is registered automatically """
PLATE_TYPE_CODE = "PLATE"
""" project and experiment where new plates will be registered """
DEFAULT_PROJECT_CODE = "TEST"
DEFAULT_EXPERIMENT_CODE = "SANOFI"
PLATE_GEOMETRY_PROPERTY_CODE = "$PLATE_GEOMETRY"
PLATE_GEOMETRY = "384_WELLS_16X24"

# ---------

""" extracts code of the sample from the directory name """
def extract_sample_code(incoming_name):
    file_basename = extract_file_basename(incoming_name)
    #return file_basename.split(".")[0]
    code = file_basename[file_basename.find("plates_") + 7 : file_basename.rfind("_") ]
    if code == "":
        code = file_basename
    return code

""" 
For a given tile number and tiles geometry returns a (x,y) tuple which describes where the tile
is located on the well.
"""
def get_tile_coords(tile_num, tile_geometry):
    columns = tile_geometry[1]
    row = ((tile_num - 1) / columns) + 1
    col = ((tile_num - 1) % columns) + 1
    return (row, col)

""" 
Parameters:
    image_tokens_list - list of ImageTokens
Returns:  (rows, columns) tuple describing the matrix of tiles (aka fields or sides) in the well  
"""
def get_tile_geometry(image_tokens_list):
    max_tile = get_max_tile_number(image_tokens_list)
    if max_tile % 4 == 0 and max_tile != 4:
        return (max_tile / 4, 4)
    elif max_tile % 3 == 0:
        return (max_tile / 3, 3)
    elif max_tile % 2 == 0:
        return (max_tile / 2, 2)
    else:
        return (max_tile, 1)

"""
Creates ImageFileInfo for a given ImageTokens.
Converts tile number to coordinates on the 'well matrix'.
Example file name: A - 1(fld 1 wv Cy5 - Cy5).tif
Returns:
    ImageTokens
"""
def create_image_tokens(path):
    image_tokens = ImageTokens()
    image_tokens.path = path

    basename = os.path.splitext(path)[0]

    wellText = basename[0:find(basename, "(")] # A - 1
    image_tokens.well = wellText.replace(" - ", "")
    
    fieldText = basename[find(basename, "fld ") + 4 : find(basename, " wv")]
    try:
        image_tokens.tile = int(fieldText)
        #print "image_tokens.tile", image_tokens.tile
    except ValueError:
        raise Exception("Cannot parse field number from '" + fieldText + "' in '" + basename + "' file name.")

    image_tokens.channel = basename[rfind(basename, " - ") + 3 :-1]
    return image_tokens

# ------------
# END of the part which you will probably need to modify
# ------------

# ------------
# Generic utility
# ------------

""" 
Finds first occurence of the patter from the right.
Throws exception if the pattern cannot be found.
"""
def rfind(text, pattern):
    ix = text.rfind(pattern)
    ensurePatternFound(ix, text, pattern)
    return ix

""" 
Finds first occurence of the patter from the left. 
Throws exception if the pattern cannot be found.
"""
def find(text, pattern):
    ix = text.find(pattern)
    ensurePatternFound(ix, text, pattern)
    return ix

def ensurePatternFound(ix, file, pattern):
    if ix == -1:
        raise Exception("Cannot find '" + pattern + "' pattern in file name '" + file + "'")    

""" Returns: name of the file without the extension """
def extract_file_basename(filename):
    lastDot = filename.rfind(".")
    if lastDot != -1:
        return filename[0:lastDot]
    else:
        return filename

""" Returns: extension of the file """
def get_file_ext(file):
    return os.path.splitext(file)[1][1:].lower()

""" Returns: java.io.File - first file with the specified extension or None if no file matches """
def find_file_by_ext(incoming_file, expected_ext):
    if not incoming_file.isDirectory():
        return None
    incoming_path = incoming_file.getPath()
    for file in os.listdir(incoming_path):
        ext = get_file_ext(file)
        if ext.upper() == expected_ext.upper():
            return File(incoming_path, file)
    return None

""" Returns: java.io.File - subdirectory which contains the specified marker in the name """
def find_dir(incoming_file, dir_name_marker):
    if not incoming_file.isDirectory():
        return None
    incoming_path = incoming_file.getPath()
    for file in os.listdir(incoming_path):
        if dir_name_marker.upper() in file.upper():
            return File(incoming_path, file)
    return None

def get_random_string():
    return str(int(random.random()*1000000000))

""" 
Creates a temporary directory two levels above the specified incoming file.
The name of the directory will contain the specified label and a random text. 
Returns:
    java.io.File - path to the temporary directory
"""
def get_tmp_dir(incoming, label):
    dropbox_parent_dir = incoming.getParentFile().getParent()
    tmp_dir = File(dropbox_parent_dir, "tmp")
    if not os.path.exists(tmp_dir.getPath()):
        os.mkdir(tmp_dir.getPath())
    tmp_labeled_dir = File(tmp_dir, label + ".tmp." + get_random_string())
    os.mkdir(tmp_labeled_dir.getPath())
    return tmp_labeled_dir

# ------------
# Image dataset registration
# ------------

"""
Auxiliary function to extract all channel codes used by specified images.
The channel label will be equal to channel code.
Parameters:
    images - list of ImageFileInfo
Returns: 
    list of Channel
"""
def get_available_channels(images):
    channel_codes = {}
    for image in images:
        channel_codes[image.getChannelCode()] = 1
    channels = []
    for channelCode in channel_codes.keys():
        channels.append(Channel(channelCode, channelCode))
    return channels

"""
Parameters:
    dataset - BasicDataSetInformation
    registration_details - DataSetRegistrationDetails
"""
def set_dataset_details(dataset, registration_details):
    registration_details.setDataSetInformation(dataset)
    registration_details.setFileFormatType(dataset.getFileFormatTypeCode())
    registration_details.setDataSetType(dataset.getDataSetType())
    registration_details.setMeasuredData(dataset.isMeasured())

"""
Parameters:
    dataset - BasicDataSetInformation
Returns: 
    DataSetRegistrationDetails
"""
def create_image_dataset_details(incoming):
    registration_details = factory.createImageRegistrationDetails()
    image_dataset = registration_details.getDataSetInformation()
    set_image_dataset(incoming, image_dataset)

    set_dataset_details(image_dataset, registration_details)
    return registration_details


""" Returns: integer - maximal tile number """
def get_max_tile_number(image_tokens_list):
    max_tile = 0
    for image_tokens in image_tokens_list:
        max_tile = max(max_tile, image_tokens.tile)
    return max_tile

""" Auxiliary structure to store tokens of the image file name.  """
class ImageTokens:
    # channel code
    channel = None
    # tile number
    tile = -1
    # path to the image
    path = ""
    # well code, e.g. A1
    well = ""

""" 
Creates ImageFileInfo for a given path to an image
Example of the accepted file name: A - 1(fld 1 wv Cy5 - Cy5).tif
Returns:
   ImageFileInfo 
"""
def create_image_info(image_tokens, tile_geometry):
    tileCoords = get_tile_coords(image_tokens.tile, tile_geometry)
    img = ImageFileInfo(image_tokens.channel, tileCoords[0], tileCoords[1], image_tokens.path)
    img.setWell(image_tokens.well)
    return img

"""
Tokenizes file names of all images in the directory.
Returns: 
  list of ImageTokens
"""
def parse_image_tokens(dir):
    image_tokens_list = []
    dir_path = dir.getPath()
    for file in os.listdir(dir_path):
        ext = get_file_ext(file)
        try:
            extIx = RECOGNIZED_IMAGES_EXTENSIONS.index(ext)
            # not reached if extension not found
            image_tokens = create_image_tokens(file)
            #print "tile", image_tokens.tile, "path", image_tokens.path, "well", image_tokens.well
            image_tokens_list.append(image_tokens)    
        except ValueError:
            pass # extension not recognized    
    return image_tokens_list

"""
Parameters:
- image_tokens_list - list of ImageTokens for each image
- tile_geometry - (rows, columns) tuple describing the matrix of tiles (aka fields or sides) in the well  
Returns: 
  list of ImageFileInfo
"""    
def create_image_infos(image_tokens_list, tile_geometry):
    images = []
    for image_tokens in image_tokens_list:
        image = create_image_info(image_tokens, tile_geometry)
        images.append(image)    
    return images

# ---------------------

"""
Extracts all images from the incoming directory.
Parameters:
    incoming - java.io.File, folder with images
    dataset - ImageDataSetInformation where the result will be stored
"""
def set_image_dataset(incoming, dataset):
    dataset.setDatasetTypeCode(IMAGE_DATASET_TYPE)
    dataset.setFileFormatCode(IMAGE_FILE_FORMAT)

    sample_code = extract_sample_code(incoming.getName())
    dataset.setSample(PLATE_SPACE, sample_code)
    dataset.setMeasured(True)

    image_tokens_list = parse_image_tokens(incoming)
    tile_geometry = get_tile_geometry(image_tokens_list)
    images = create_image_infos(image_tokens_list, tile_geometry)
    channels = get_available_channels(images)
    
    dataset.setImages(images)
    dataset.setChannels(channels)
    dataset.setTileGeometry(tile_geometry[0], tile_geometry[1])

    return dataset

"""
Extracts all overlay images from the overlays_dir directory.
Parameters:
    overlays_dir - java.io.File, folder with 
    image_dataset - ImageDataSetInformation, image dataset to which the overlay dataset belongs
    img_dataset_code - string, code of the  image dataset to which the overlay dataset belongs
    overlay_dataset - ImageDataSetInformation where the result will be stored
"""
def set_overlay_dataset(overlays_dir, image_dataset, img_dataset_code, overlay_dataset):
    overlay_dataset.setDatasetTypeCode(OVERLAY_IMAGE_DATASET_TYPE)
    overlay_dataset.setFileFormatCode(OVERLAY_IMAGE_FILE_FORMAT)

    overlay_dataset.setSample(image_dataset.getSpaceCode(), image_dataset.getSampleCode())
    overlay_dataset.setMeasured(False)
    overlay_dataset.setParentDatasetCode(img_dataset_code)

    image_tokens_list = parse_image_tokens(overlays_dir)
    tile_geometry = (image_dataset.getTileRowsNumber(), image_dataset.getTileColumnsNumber())
    images = create_image_infos(image_tokens_list, tile_geometry)
    channels = get_available_channels(images)

    overlay_dataset.setImages(images)
    overlay_dataset.setChannels(channels)
    overlay_dataset.setTileGeometry(tile_geometry[0], tile_geometry[1])

"""
Creates registration details of the image overlays dataset.
Parameters:
    overlays_dir - java.io.File, folder with 
    image_dataset - ImageDataset, image dataset to which the overlay dataset belongs
    img_dataset_code - string, code of the  image dataset to which the overlay dataset belongs
Returns:
    DataSetRegistrationDetails
"""
def create_overlay_dataset_details(overlays_dir, image_dataset, img_dataset_code):
    overlay_dataset_details = factory.createImageRegistrationDetails()
    overlay_dataset = overlay_dataset_details.getDataSetInformation()
    set_overlay_dataset(overlays_dir, image_dataset, img_dataset_code, overlay_dataset)
    set_dataset_details(overlay_dataset, overlay_dataset_details)

    config = ImageStorageConfiguraton.createDefault()
    # channels will be connected to the dataset
    config.setStoreChannelsOnExperimentLevel(False)
    overlay_dataset.setImageStorageConfiguraton(config)
    return overlay_dataset_details

# ---------------------

"""
Creates the analysis dataset description. 
The dataset will be connected to the specified sample and parent dataset.
Parameters:
    dataset - BasicDataSetInformation where the result will be stored
"""
def set_analysis_dataset(sample_space, sample_code, parent_dataset_code, dataset):
    dataset.setDatasetTypeCode(ANALYSIS_DATASET_TYPE)
    dataset.setFileFormatCode(ANALYSIS_FILE_FORMAT)
    dataset.setSample(sample_space, sample_code)
    dataset.setMeasured(False)
    dataset.setParentDatasetCode(parent_dataset_code)

"""
Creates registration details of the analysis dataset.
Returns:
    DataSetRegistrationDetails
"""
def create_analysis_dataset_details(sample_space, sample_code, parent_dataset_code):
    registration_details = factory.createBasicRegistrationDetails()
    dataset = registration_details.getDataSetInformation()
    set_analysis_dataset(sample_space, sample_code, parent_dataset_code, dataset)
    set_dataset_details(dataset, registration_details)
    return registration_details

""" registers sample if it does not exist already """
def register_sample_if_necessary(space_code, project_code, experiment_code, sample_code):   
    openbis = state.getOpenBisService()
    sampleIdentifier = SampleIdentifier.create(space_code, sample_code)
    if (openbis.tryGetSampleWithExperiment(sampleIdentifier) == None):
        sample = NewSample()
        sampleType = SampleType()
        sampleType.setCode(PLATE_TYPE_CODE)
        sample.setSampleType(sampleType)
        sample.setIdentifier(sampleIdentifier.toString())
        
        property = dto.VocabularyTermEntityProperty();
        vocabularyTerm = dto.VocabularyTerm();
        vocabularyTerm.setCode(PLATE_GEOMETRY);
        property.setVocabularyTerm(vocabularyTerm);
        propertyType = dto.PropertyType();
        dataType = dto.DataType();
        dataType.setCode(dto.DataTypeCode.CONTROLLEDVOCABULARY);
        propertyType.setDataType(dataType);
        propertyType.setCode(PLATE_GEOMETRY_PROPERTY_CODE);
        property.setPropertyType(propertyType);
        sample.setProperties([ property ])
        
        sample.setExperimentIdentifier("/" + space_code + "/" + project_code + "/" + experiment_code)
        openbis.registerSample(sample, None)

# ---------------------
       
"""
Allows to recognize that the subdirectory of the incoming dataset directory contains overlay images.
This text has to appear in the subdirectory name. 
"""
OVERLAYS_DIR_PATTERN = "overlays"

def register_images_with_overlays_and_analysis(incoming):
    if not incoming.isDirectory():
        return
    
    tr = service.transaction(incoming, factory)
        
    image_dataset_details = create_image_dataset_details(incoming)
    plate_code = image_dataset_details.getDataSetInformation().getSampleCode()
    space_code = image_dataset_details.getDataSetInformation().getSpaceCode()
    register_sample_if_necessary(space_code, DEFAULT_PROJECT_CODE, DEFAULT_EXPERIMENT_CODE, plate_code)

    # create the image data set and put everything in it initially
    image_data_set = tr.createNewDataSet(image_dataset_details)
    image_data_set_folder = tr.moveFile(incoming.getPath(), image_data_set)
    img_dataset_code = image_data_set.getDataSetCode()
          
        
    # move overlays folder
    overlays_dir = find_dir(File(image_data_set_folder), OVERLAYS_DIR_PATTERN)
    if overlays_dir != None:
        tr_overlays = service.transaction(overlays_dir, factory)
        overlay_dataset_details = create_overlay_dataset_details(overlays_dir, 
                                     image_dataset_details.getDataSetInformation(), img_dataset_code)
        overlays_data_set = tr_overlays.createNewDataSet(overlay_dataset_details)
        tr_overlays.moveFile(overlays_dir.getPath(), overlays_data_set, "overlays")
        tr_overlays.commit()
        
    # transform and move analysis file
    analysis_file = find_file_by_ext(File(image_data_set_folder), "xml")
    if analysis_file != None:
        tr_analysis = service.transaction(analysis_file, factory)
        analysis_registration_details = create_analysis_dataset_details(space_code, plate_code, img_dataset_code)
        analysis_data_set = tr_analysis.createNewDataSet(analysis_registration_details)
        analysis_data_set_file = tr_analysis.createNewFile(analysis_data_set, analysis_file.getName())
        GEExplorerImageAnalysisResultParser(analysis_file.getPath()).writeCSV(File(analysis_data_set_file))
        tr_analysis.commit()
            
    service.commit()
    
register_images_with_overlays_and_analysis(incoming)
