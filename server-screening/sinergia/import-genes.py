#! /usr/bin/env python
"""
  Script for uploading gene data. Data is organized in directories called as gene names. In each of these are:
  two pdf files (gene and RNA); 10 mp4 gene files; 10 mp4 control files.
  Samples with gene names must already exist in openBIS (created with import-clusters.py script). All data should go in the corresponding sample.  
"""

import os, re, glob, shutil
from time import *
from datetime import *


from java.io import File
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import SimpleImageDataConfig, ImageMetadata, Location
from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import Geometry
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import SimpleImageContainerDataConfig, ChannelColor
from loci.formats import ImageReader

print "###################################################"
tz=localtime()[3]-gmtime()[3]
d=datetime.now()
print d.strftime("%Y-%m-%d %H:%M:%S GMT"+"%+.2d" % tz+":00")


def process(transaction):
	incoming = transaction.getIncoming()
	geneName = os.path.basename(incoming.getPath())

	print "geneName", geneName

#Set project/experiment/sample
	project = transaction.getProject("/PUBLISHED_DATA/ANALYSIS")
	exp = transaction.getExperiment("/PUBLISHED_DATA/ANALYSIS/CLUSTERS")
	sample = transaction.getSample("/PUBLISHED_DATA/" + geneName)

	print "sample", sample

#upload the gene png and the RNA png
	for png in glob.glob(os.path.join(incoming.getPath(), '*.png')):
		dataSetPNG = transaction.createNewDataSet()
		dataSetPNG.setDataSetType("PNG_IMAGES")
		dataSetPNG.setSample(sample)
		transaction.moveFile(png, dataSetPNG)
 
# upload the 10 tif images for each gene	
	tifDir = incoming.getPath() + "/tiffs"
	if not os.path.exists(tifDir):
		os.makedirs(tifDir)
	for tif in glob.glob(os.path.join(incoming.getPath(), '*.tif')):
		shutil.move(tif, tifDir)
	dataSetTIF = transaction.createNewDataSet()
	dataSetTIF.setDataSetType("TIF_IMAGES")
	dataSetTIF.setSample(sample)
	transaction.moveFile(tifDir, dataSetTIF)

#upload the 10 control movies
	videoControlDir = incoming.getPath() + "/controlVideos"
	if not os.path.exists(videoControlDir):
		os.makedirs(videoControlDir)
	for mp4 in glob.glob(os.path.join(incoming.getPath(), 'control*.mp4')):
		shutil.move(mp4, videoControlDir)
	dataSetMP4Control = transaction.createNewDataSet()
 	dataSetMP4Control.setDataSetType("VIDEOS")
 	dataSetMP4Control.setSample(sample)
 	transaction.moveFile(videoControlDir, dataSetMP4Control)

#upload the 10 gene synthetic movies
	videoGeneDir = incoming.getPath() + "/geneVideos"
	if not os.path.exists(videoGeneDir):
		os.makedirs(videoGeneDir)
	for mp4 in glob.glob(os.path.join(incoming.getPath(), '*.mp4')):
		shutil.move(mp4, videoGeneDir)
	dataSetMP4Gene = transaction.createNewDataSet()
 	dataSetMP4Gene.setDataSetType("VIDEOS")
 	dataSetMP4Gene.setSample(sample)
 	transaction.moveFile(videoGeneDir, dataSetMP4Gene)



