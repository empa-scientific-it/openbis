#! /usr/bin/env python
"""
  The implementation of the Sinergia dropbox.
  
  Sinergia data is uploaded in a format where many files are provided in a single folder containing images and a metadata file (with the ".nd" extension). The dropbox implementation takes this format, extracts metadata and converts the file structure to a different one that is more manageable. The resulting file structure contains diretories for each well, containing directories for each channel. The images are located inside the chanel directory.  
"""

import os
import glob
import re
import time
import shutil

from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import SimpleImageDataConfig
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import ImageMetadata
from ch.systemsx.cisd.hdf5.h5ar import HDF5ArchiverFactory
from java.io import File


ORIGINAL_THUMBNAILS_FOLDER="thumbnails-original"
Uncomplete_Experiments = "Uncomplete_Experiments"

class SinergiaPlate:
      def __init__(self):
            self.stages = []

class SinergiaStage:
      def __init__(self):
            self.channels = []
            self.well = ""
            # dummy value generated underneath
            self.tileNumber = 0
            self.siteString = 0

class SinergiaChannel:
      def __init__(self):
            self.name = []
            self.timepoints = []

def move_file_to_dir(srcFile, destDir):
      destFile = destDir + "/" + os.path.basename(srcFile)
      if srcFile == destFile:
            return
      if os.path.exists(destFile):
            raise Exception("Cowardly refusing to override existing file %(destFile)s with source file %(srcFile)s." % vars())
      os.rename(srcFile, destFile)

            
def process_stage(plate, files):
      """Process all the files for one stage of a plate, returning the stage"""
      stage = SinergiaStage()
      
      red = SinergiaChannel()
      red.name = "red"
      redfiles = filter(lambda x: re.match('.*red_.*', x), files) #find all files for the red channel
      red.timepoints = redfiles
      stage.channels.append(red)
      
      green = SinergiaChannel()
      green.name = "green"
      greenfiles = filter(lambda x: re.match('.*green_.*', x), files) #find all files for the green channel
      green.timepoints = greenfiles
      stage.channels.append(green)
      
      return stage

def parse_plate_metadata(incomingPath, pattern_start, sinergia_plate):
      dummyTileCounter = 0;
      for ndFileName in glob.glob( os.path.join(incomingPath, pattern_start + '.nd')):
            ndfile = open(ndFileName, "r")
            lineIndex = 0
            for line in ndfile:
                  lineIndex = lineIndex + 1
                  if re.match('"Stage', line):
                        token_list = re.split(r"[\"\:\\\n\,]",line)
                        token_list = [ item.strip() for item in token_list ]
                        token_list = filter(lambda x: len(x) > 0, token_list)
                        
                        stageString = token_list[0]
                        
                        
                        
                        try:
                              stageIdx = int( stageString[len("Stage"):] ) - 1
                        except ValueError:
                              raise Exception("Cannot parse stage number from '%(stageString)s: %(ndFileName)s, line %(lineIndex)i'" % vars())

                        try:
                               stage = sinergia_plate.stages[stageIdx]
                        except IndexError:
                              raise Exception("Invalid stage number '%(stageString)s: %(ndFileName)s, line %(lineIndex)i'. No corresponding TIF file was found." % vars())

                        stage.well = token_list[1]
                        stage.siteString = token_list[2]
                        stage.tileNumber = (dummyTileCounter % 10) + 1
                        dummyTileCounter = dummyTileCounter + 1
                        
                        
            ndfile.close()

def move_to_original_thumbnail_folder(incomingPath, thumbFiles):
      thumbFolder = incomingPath + "/" + ORIGINAL_THUMBNAILS_FOLDER
      if not os.path.exists(thumbFolder):
            os.makedirs(thumbFolder)
      for thumbFile in thumbFiles:
            move_file_to_dir(thumbFile, thumbFolder)



def create_glob_pattern_start(incomingPath):
    """
        Return the Experiment{*} part of a pattern used by functions that need 
        to process only the valid experiment
    """
    for ndfile in glob.glob(os.path.join(incomingPath, '*.nd')):
      (incomingPath, file) = os.path.split(ndfile)
      (filename, extension) = os.path.splitext(file)
      pattern = filename + '*.TIF'
      match_count = len(glob.glob1(incomingPath, pattern))
     # if match_count < 29000:
      if match_count < 46560:
          unwanted = incomingPath+ "/../../" + Uncomplete_Experiments
          if not os.path.exists(unwanted):
            os.makedirs(unwanted)
          shutil.move(ndfile, unwanted)  
          #continue
      else:
          ret = filename
    return ret


def is_thumbnail(fileName):
      return "_thumb_" in fileName
  
def remove_uncomplete_experiments(incomingPath):
      pattern_start = create_glob_pattern_start(incomingPath)
      for tif in glob.glob(os.path.join(incomingPath, '*.TIF')):
        (incomingPath, file) = os.path.split(tif)
        (filename, extension) = os.path.splitext(file)
        token_list = re.split('_', file)
        for token in token_list:
            if re.match('Exp', token):
                Experiment = token
                if Experiment != pattern_start:
                    unwanted = incomingPath+"/../../" + Uncomplete_Experiments
                    if not os.path.exists(unwanted):
                       os.makedirs(unwanted)
                    shutil.move(tif, unwanted)  
                    shutil.rmtree(unwanted)
                    
remove_uncomplete_experiments(incoming.getPath())     
     


def process_plate(incomingPath):
    """Look at all the files in the incoming path and group them into plates"""
    sinergia_plate = SinergiaPlate()
    remove_uncomplete_experiments(incomingPath)
    
    for stage_number in range(1, 241):
        pattern_start = create_glob_pattern_start(incomingPath)
        sinergia_plate.pattern_start = pattern_start
        pattern = pattern_start + "*_s" + str(stage_number) +'_*.TIF'
        files = glob.glob(os.path.join(incomingPath, pattern))
        thumbFiles = filter(is_thumbnail, files)
        imageFiles = filter(lambda x: not is_thumbnail(x), files)
        stage = process_stage(sinergia_plate, imageFiles)
        move_to_original_thumbnail_folder(incomingPath, thumbFiles)
        sinergia_plate.stages.append(stage)
        
    parse_plate_metadata(incomingPath, pattern_start, sinergia_plate)
    
    
    return sinergia_plate

def get_directory_for_image_file(stageIdx, channelName):
      fullPath = incoming.getPath() + "/" + str(stageIdx) + "/" + channelName
      if not os.path.exists(fullPath):
            os.makedirs(fullPath)
      return fullPath

def transform_plate_file_structure(plate):
       for idx, stage in enumerate(plate.stages):
          for channel in stage.channels:
                  for imageFile in channel.timepoints:
                        directory = get_directory_for_image_file(idx + 1, channel.name)
                        move_file_to_dir(imageFile, directory)          
     
class SinergiaImageDataSetConfig(SimpleImageDataConfig):
    def __init__(self, sinergia_plate):
        self.sinergia_plate = sinergia_plate
            
    def extractImageMetadata(self, imagePath):
        (dirName, filename) = os.path.split(imagePath)
        (basename, extension) = os.path.splitext(filename)
                
        if is_thumbnail(basename):
            return None
        if not basename.startswith(self.sinergia_plate.pattern_start):
            return None
        
        image_tokens = ImageMetadata()
        
        token_dict = {}
        for token in basename.split("_"):
            token_dict[token[:1]] = token[1:]

        channelName = token_dict["w"]
        if "1LED green" == channelName:
              channelCode = "LIFE ACT-GFP"
        elif "2LED red" == channelName:
              channelCode = "NLS-mCHERRY"
        else:
              channelCode = channelName
        image_tokens.channelCode = channelCode
        
        stageIdx = int(token_dict["s"]) - 1
        image_tokens.well = sinergia_plate.stages[stageIdx].well
        image_tokens.tileNumber = sinergia_plate.stages[stageIdx].tileNumber
        image_tokens.timepoint = int(token_dict["t"])
        
        
        return image_tokens

def get_or_create_bis_plate(tr, plateName,incomingPath):
      spaceCode = "SINERGIA"
      #plateIdentifier = "/" + spaceCode + "/" + plateName + "-REP"
      plateIdentifier =  "/" + spaceCode + "/" + plateName + "-10x"
      plate = tr.getSample(plateIdentifier)
      (pathName, dirName) = os.path.split(incomingPath)	      

      if not plate:
	    token_list = re.split('-', dirName)
	    for token in token_list:
		if re.match('G', token):
			groupNum=token[1:]
			groupIdentifier = "/" + spaceCode + "/SIRNA_TIMELAPSES_10X/GROUP-" + groupNum
            plate = tr.createNewSample(plateIdentifier, 'PLATE')
            plate.setPropertyValue("$PLATE_GEOMETRY", "24_WELLS_4X6")
            exp = tr.getExperiment(groupIdentifier)
            if not exp:
	    	exp = tr.createNewExperiment(groupIdentifier, 'SIRNA_HCS')
	    	exp.setPropertyValue("DESCRIPTION", "siRNA screening: timelapses")
            #exp = tr.getExperiment("/SINERGIA/SIRNA_MOVIES/GROUP-1")
            plate.setExperiment(exp)
      return plate;


def archive_thumbnails(incomingPath):
    thumbnailDir = os.path.join(incomingPath, "thumbnails-original")
    thumbnailContainer = os.path.join(incomingPath, "thumbnails-original.h5ar")
    if os.path.isdir(thumbnailDir):
        archiver = HDF5ArchiverFactory.open(thumbnailContainer)
        archiver.archiveFromFilesystem(File(thumbnailDir))
        archiver.close()
        shutil.rmtree(thumbnailDir)


sinergia_plate = process_plate(incoming.getPath())
transform_plate_file_structure(sinergia_plate)


tr = service.transaction(incoming, factory)

if incoming.isDirectory():
    archive_thumbnails(incoming.getPath())  
    imageDatasetConfig = SinergiaImageDataSetConfig(sinergia_plate)
    imageDatasetConfig.setImageLibrary("IJ")
    imageDatasetConfig.setRawImageDatasetType()
    imageDatasetConfig.setGenerateImageRepresentationsUsingImageResolutions(["64x52"])
    imageDataSetDetails = factory.createImageRegistrationDetails(imageDatasetConfig, incoming)

    plate = get_or_create_bis_plate(tr, incoming.getName().upper(), incoming.getPath())
    dataSet = tr.createNewDataSet(imageDataSetDetails)
    dataSet.setSample(plate)
    tr.moveFile(incoming.getPath(), dataSet)
