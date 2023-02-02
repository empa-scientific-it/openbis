import os, glob, re, csv, time, shutil
from java.io import File
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import SimpleImageDataConfig, ImageMetadata, Location
from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import Geometry
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import SimpleImageContainerDataConfig, ChannelColor
from loci.formats import ImageReader



from time import *
from datetime import *

'''
Dropbox script for uploading a .czi file. Images contained in the file are displayed in openBIS and metadata is extracted from the czi file and 
stored in a file called "samplename-metadata.txt" uploaded in the dataset. The czi file needs to be put in a directory inside the incoming folder. 
The name of this directory should be Y-X, where Y is name of the project (already existing in openBIS) and X the name of the experiment 
where the sample will be uploaded. If the experiment does not exist yet, it will be created.
As many samples are created as many czi files are contained in the incoming directory. The samples are named like the czi file.
 
'''


print '###################################'
tz=localtime()[3]-gmtime()[3]
d=datetime.now()
print d.strftime("%Y-%m-%d %H:%M:%S GMT"+"%+.2d" % tz+":00")

class BioFormatsHelper(ImageReader):
	def tryToCreateReaderForFile(fileName):
		for reader in ImageReader().getReaders():
			if reader.isThisType(fileName):
				return reader
		return None

def process(transaction):
	
	incoming = transaction.getIncoming()


#read each czi file contained in the incoming directory and extract all the metadata to a metadata.txt file
#extract also some specific metadata which will be set as properties in openBIS
	def readCzi(incoming):
		filename_list = {}
		for cziFile in glob.glob(os.path.join(incoming, '*.czi')):
			objective =''
			scaling = None
			z_step_size = None
			pixel_dwell_time = None
			channel_name_0 = ''
			channel_name_1 = ''
			channel_name_2 = ''
			channel_name_3 = ''
			channel_name_4 = ''
			channel_name_5 = ''
			channel_name_6 = ''
			channel_name_7 = ''
			detector_gain_0 = None
			detector_gain_1 = None
			detector_gain_2 = None
			detector_gain_3 = None
			detector_gain_4 = None
			detector_gain_5 = None
			detector_gain_6 = None
			detector_gain_7 = None
			(dir, file) = os.path.split(cziFile)
			(filename, extension) = os.path.splitext(file)
			helper = BioFormatsHelper()
			reader = helper.getReader(cziFile)
			reader.setId(cziFile)
			
			globalMetadata = str(reader.getGlobalMetadata().toString())
			
			list_globalMetadata = re.split(r"[,]",globalMetadata)
			metadataFile = dir+"/" + filename + "-metadata.txt"
			f = open(metadataFile, "a")
			for item in list_globalMetadata:
				f.write(item)
				f.write('\n')
				if re.search ("=", item):
					item_total = re.split(r"[=]", item)
					item_label = item_total[0]
					item_value = item_total[1]
					if re.search ("Metadata Experiment ExperimentBlocks AcquisitionBlock AcquisitionModeSetup Objective 0", item_label):
						objective = item_value
						
					if re.search ("Metadata Experiment ExperimentBlocks AcquisitionBlock AcquisitionModeSetup ScalingX 0", item_label):
						scaling = item_value
					
					if re.search ("Metadata Information Image Dimensions Z Positions Interval Increment 0", item_label):
						z_step_size = item_value
					
					if re.search ("Metadata Experiment ExperimentBlocks AcquisitionBlock AcquisitionModeSetup PixelPeriod 0", item_label):
						pixel_dwell_time = item_value
					
					if re.search ("Metadata Experiment ExperimentBlocks AcquisitionBlock MultiTrackSetup TrackSetup Detectors Detector ImageChannelName 0", item_label):
						channel_name_0 = item_value
					
					if re.search ("Metadata Experiment ExperimentBlocks AcquisitionBlock MultiTrackSetup TrackSetup Detectors Detector ImageChannelName 1", item_label):
						channel_name_1 = item_value
					
					if re.search ("Metadata Experiment ExperimentBlocks AcquisitionBlock MultiTrackSetup TrackSetup Detectors Detector ImageChannelName 2", item_label):
						channel_name_2 = item_value
					
					if re.search ("Metadata Experiment ExperimentBlocks AcquisitionBlock MultiTrackSetup TrackSetup Detectors Detector ImageChannelName 3", item_label):
						channel_name_3 = item_value
					
					if re.search ("Metadata Experiment ExperimentBlocks AcquisitionBlock MultiTrackSetup TrackSetup Detectors Detector ImageChannelName 4", item_label):
						channel_name_4 = item_value
					
					if re.search ("Metadata Experiment ExperimentBlocks AcquisitionBlock MultiTrackSetup TrackSetup Detectors Detector ImageChannelName 5", item_label):
						channel_name_5 = item_value
					
					if re.search ("Metadata Experiment ExperimentBlocks AcquisitionBlock MultiTrackSetup TrackSetup Detectors Detector ImageChannelName 6", item_label):
						channel_name_6 = item_value
					
					if re.search ("Metadata Experiment ExperimentBlocks AcquisitionBlock MultiTrackSetup TrackSetup Detectors Detector ImageChannelName 7", item_label):
						channel_name_7 = item_value
					
					if re.search ("Metadata Information Image Dimensions Channels Channel DetectorSettings Gain 0", item_label):
						detector_gain_0 = item_value
					
					if re.search ("Metadata Information Image Dimensions Channels Channel DetectorSettings Gain 1", item_label):
						detector_gain_1 = item_value
					
					if re.search ("Metadata Information Image Dimensions Channels Channel DetectorSettings Gain 2", item_label):
						detector_gain_2 = item_value
					
					if re.search ("Metadata Information Image Dimensions Channels Channel DetectorSettings Gain 3", item_label):
						detector_gain_3 = item_value
					
					if re.search ("Metadata Information Image Dimensions Channels Channel DetectorSettings Gain 4", item_label):
						detector_gain_4 = item_value
					
					if re.search ("Metadata Information Image Dimensions Channels Channel DetectorSettings Gain 5", item_label):
						detector_gain_5 = item_value
					
					if re.search ("Metadata Information Image Dimensions Channels Channel DetectorSettings Gain 6", item_label):
						detector_gain_6 = item_value
					
					if re.search ("Metadata Information Image Dimensions Channels Channel DetectorSettings Gain 7", item_label):
						detector_gain_7 = None
 					
 					
 					
			f.close()
			reader.close();
			filename_list[filename] = { "objective" : objective, "scaling" : scaling, "z_step_size" : z_step_size, "pixel_dwell_time" : pixel_dwell_time, "channel_name_0" : channel_name_0, "channel_name_1" : channel_name_1, "channel_name_2" : channel_name_2, "channel_name_3" : channel_name_3, "channel_name_4" : channel_name_4, "channel_name_5" : channel_name_5, "channel_name_6" : channel_name_6, "channel_name_7" : channel_name_7, "detector_gain_0" : detector_gain_0, "detector_gain_1" : detector_gain_1, "detector_gain_2" : detector_gain_2, "detector_gain_3" : detector_gain_3, "detector_gain_4" : detector_gain_4, "detector_gain_5" : detector_gain_5, "detector_gain_6" : detector_gain_6, "detector_gain_7" : detector_gain_7  }
			
			
		return filename_list
			   
	cziInfo = readCzi(incoming.getPath())


	directoryName = os.path.basename(incoming.getPath())
	names = re.split(r"[-]",directoryName)
	projectName = names[0] + "-IMG"
	experimentName = "-".join(names[1:])
	experiment = "/PANTAZIS_GROUP/" + projectName + "/" + experimentName  
	exp = transaction.getExperiment(experiment)
	if not exp:
		exp = transaction.createNewExperiment(experiment, 'IMAGING')

	
	for name in cziInfo.keys():
		sampleIdentifier = "/PANTAZIS_GROUP/" + name
		sample = transaction.createNewSample(sampleIdentifier, "MICROSCOPY_IMG")

		sample.setExperiment(exp)
		sample.setPropertyValue("OBJECTIVE", cziInfo[name]["objective"])
		sample.setPropertyValue("SCALING", cziInfo[name]["scaling"])
		sample.setPropertyValue("Z_STEP_SIZE", cziInfo[name]["z_step_size"])
		sample.setPropertyValue("PIXEL_DWELL_TIME", cziInfo[name]["pixel_dwell_time"])
		sample.setPropertyValue("CHANNEL_NAME_0", cziInfo[name]["channel_name_0"])
		sample.setPropertyValue("CHANNEL_NAME_1", cziInfo[name]["channel_name_1"])
		sample.setPropertyValue("CHANNEL_NAME_2", cziInfo[name]["channel_name_2"])
		sample.setPropertyValue("CHANNEL_NAME_3", cziInfo[name]["channel_name_3"])
		sample.setPropertyValue("CHANNEL_NAME_4", cziInfo[name]["channel_name_4"])
		sample.setPropertyValue("CHANNEL_NAME_5", cziInfo[name]["channel_name_5"])
		sample.setPropertyValue("CHANNEL_NAME_6", cziInfo[name]["channel_name_6"])
		sample.setPropertyValue("CHANNEL_NAME_7", cziInfo[name]["channel_name_7"])
		sample.setPropertyValue("DETECTOR_GAIN_0", cziInfo[name]["detector_gain_0"])
		sample.setPropertyValue("DETECTOR_GAIN_1", cziInfo[name]["detector_gain_1"])
		sample.setPropertyValue("DETECTOR_GAIN_2", cziInfo[name]["detector_gain_2"])
		sample.setPropertyValue("DETECTOR_GAIN_3", cziInfo[name]["detector_gain_3"])
		sample.setPropertyValue("DETECTOR_GAIN_4", cziInfo[name]["detector_gain_4"])
		sample.setPropertyValue("DETECTOR_GAIN_5", cziInfo[name]["detector_gain_5"])
		sample.setPropertyValue("DETECTOR_GAIN_6", cziInfo[name]["detector_gain_6"])
		sample.setPropertyValue("DETECTOR_GAIN_7", cziInfo[name]["detector_gain_7"])

		imageDataset = SimpleImageContainerDataConfig()
		imageDataset.setPlate("PANTAZIS_GROUP", name)
		imageDataset.setMicroscopyData(True)
		imageDataset.setDataSetType("MICROSCOPY_IMG")
		imageDataset.setRecognizedImageExtensions(['lif', 'dv', 'tif', 'tiff','nd2', 'lsm', 'czi'])
		imageDataset.setImageLibrary("BioFormats")
		imageDataset.setMeasuredData(True)
		
		sampleNamePath = incoming.getPath() + '/' + name  + '.czi'
		metadataPath = incoming.getPath() + '/' + name  + '-metadata.txt'
 		dataSet = transaction.createNewImageDataSet(imageDataset, File(sampleNamePath))
		
		transaction.moveFile(metadataPath, dataSet)
		transaction.moveFile(sampleNamePath, dataSet)

 

	
	
	
  		
  	
 
	  
  
  
  
  
  
  
  
  
  
