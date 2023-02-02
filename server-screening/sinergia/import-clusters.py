#! /usr/bin/env python
"""
  Script for uploading cluster data. Data is organized in directories called as cluster numbers (from 1 to 17). Each cluster
  directory has 1 pdf file and 10 TIF image files. There is also a text file that contains the list of genes contained in each cluster.
  
  
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

SPACE_CODE = "PUBLISHED_DATA"
PROJECT_CODE = "ANALYSIS"
PROJECT_ID = "/%(SPACE_CODE)s/%(PROJECT_CODE)s" % vars()
EXPERIMENT_CODE = "CLUSTERS"
EXPERIMENT_ID = "/%(SPACE_CODE)s/%(PROJECT_CODE)s/%(EXPERIMENT_CODE)s" % vars()


def create_project_if_needed(transaction):
  project = transaction.getProject(PROJECT_ID)
  if None == project:
	project = transaction.createNewProject(PROJECT_ID)
    
    
def create_experiment_if_needed(transaction):
  """ Get the specified experiment or register it if necessary """
  exp = transaction.getExperiment(EXPERIMENT_ID)
  if None == exp:
    create_project_if_needed(transaction)
    exp = transaction.createNewExperiment(EXPERIMENT_ID, 'SIRNA_HCS')
    
  return exp

def process(transaction):
	if transaction.getIncoming().isFile():
		processFile(transaction)
	else:
		processDirectory(transaction)
   

def processFile(transaction):
	incoming = transaction.getIncoming()
	
#Check if project and experiment called "CLUSTERS" already exist, if not create them	
	
	dataSetClusterPNGGlob = transaction.createNewDataSet()
	dataSetClusterPNGGlob.setDataSetType("CLUSTER_PNG")
	dataSetClusterPNGGlob.setExperiment(create_experiment_if_needed(transaction))
	transaction.moveFile(incoming.getPath(), dataSetClusterPNGGlob)	

def processDirectory(transaction):
	incoming = transaction.getIncoming()
	clusterName = os.path.basename(incoming.getPath())	

#create samples called Cluster1 to 17. Take the name from the directory (in the incoming folder there are 17 directories called cluster1-17)		
	newClusterSample=transaction.createNewSample("/PUBLISHED_DATA/" + clusterName,'CLUSTER') 
	newClusterSample.setExperiment(create_experiment_if_needed(transaction))

 
	for clusterPNG in glob.glob(os.path.join(incoming.getPath(), 'clusterProfile.png')):
		dataSetClusterPNG = transaction.createNewDataSet()
		dataSetClusterPNG.setDataSetType("CLUSTER_PNG")
		dataSetClusterPNG.setSample(newClusterSample)
		transaction.moveFile(clusterPNG, dataSetClusterPNG)



# upload the 10 tif images for each cluster as a dataset in the corresponding Cluster sample		
 	tifDir = incoming.getPath() + "/tiffs"
 	if not os.path.exists(tifDir):
 		os.makedirs(tifDir)
 	if glob.glob(os.path.join(incoming.getPath(), '*.tif')):	     		
 		for tif in glob.glob(os.path.join(incoming.getPath(), '*.tif')):
 			shutil.move(tif, tifDir)
		dataSetTIF = transaction.createNewDataSet()
		dataSetTIF.setDataSetType("TIF_IMAGES")
		dataSetTIF.setSample(newClusterSample)
		transaction.moveFile(tifDir, dataSetTIF)


# upload the 10 tif images for each cluster as a dataset in the corresponding Cluster sample		
 	pngDir = incoming.getPath() + "/pngs"
 	if not os.path.exists(pngDir):
 		os.makedirs(pngDir)
 	if glob.glob(os.path.join(incoming.getPath(), '*.png')):	     		
 		for png in glob.glob(os.path.join(incoming.getPath(), '*.png')):
 			shutil.move(png, pngDir)
		dataSetPNG = transaction.createNewDataSet()
		dataSetPNG.setDataSetType("PNG_IMAGES")
		dataSetPNG.setSample(newClusterSample)
		transaction.moveFile(pngDir, dataSetPNG)



#Open the geneList text file and create samples with the name of the genes. These samples have to be contained in the corresponding cluster.
	for textfile in glob.glob(os.path.join(incoming.getPath(), 'geneList.txt')):
		text = open(textfile, "r")
		lines = text.readlines()[1:]
		for line in lines:
			print line
			gene_list = re.split(r"[,]",line)
			gene_list = [ item.strip() for item in gene_list ]
			gene_list = filter(lambda x: len(x) > 0, gene_list)
			for gene in gene_list:
				newGeneSample = transaction.createNewSample("/PUBLISHED_DATA/" + gene,'GENE')
				newGeneSample.setContainer(newClusterSample) 
				newGeneSample.setExperiment(create_experiment_if_needed(transaction))





