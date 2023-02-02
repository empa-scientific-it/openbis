#! /usr/bin/env python
"""
  Import analysis data in two datasets: one dataset for videos and one dataset for matlab files.  
  
"""  

from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchSubCriteria
  
import os
import glob
import re
import time
import shutil, sys
from time import *
from datetime import *



print '###################################'
tz=localtime()[3]-gmtime()[3]
d=datetime.now()
print d.strftime("%Y-%m-%d %H:%M:%S GMT"+"%+.2d" % tz+":00")





def copyTextFile(incomingPath):
  for textfile in glob.glob(os.path.join(incomingPath, 'OriginalDataDirectory.txt')):
	rawDataFile = incomingPath + '/RawDataDirectory.txt'
	shutil.copyfile(textfile, rawDataFile)
   
copyTextFile(incoming.getPath())

def extractSpaceCode(incomingPath):
    spaceCode = "SINERGIA" 
    return spaceCode    


def extractPlateCode(incomingPath):
	for textfile in glob.glob(os.path.join(incomingPath, 'RawDataDirectory.txt')):
	  text = open(textfile, "r")
	  lineIndex =0
	  for line in text:
		lineIndex=lineIndex+1
		if re.match('PLATE', line):
		  token_list = re.split(r"[ ]",line)
		  token_list = [ item.strip() for item in token_list ]
		  token_list = filter(lambda x: len(x) > 0, token_list)
		  plateCode = token_list[0]
	return plateCode      

extractPlateCode(incoming.getPath())  


def extractDataSetCode(incomingPath):
	dataSetCode = ''
	for textfile in glob.glob(os.path.join(incomingPath, 'RawDataDirectory.txt')):
	  text = open(textfile, "r")
	  lineIndex =0
	  for line in text:
		lineIndex=lineIndex+1
	  #  if re.match('/raid', line):
		if re.match('/Users', line):
		  token_list = re.split(r"[/]",line)
		  token_list = [ item.strip() for item in token_list ]
		  token_list = filter(lambda x: len(x) > 0, token_list)
		 # dataSetCode = token_list[8] # right position for /raid
		  dataSetCode = token_list[10] #right position for local use
		   #plateCode = line
	return dataSetCode      
	  
extractDataSetCode(incoming.getPath())          




def get_videos(incomingPath):
	directory =  incomingPath + '/videos'
	if not os.path.exists(directory):
	  os.makedirs(directory)
	for mp4 in glob.glob(os.path.join(incomingPath, '*.mp4')):
	  (incomingPath, file) = os.path.split(mp4)
	  (filename, extension) = os.path.splitext(file)
	  stage=  filename
	  shutil.move(incomingPath +'/'+file, directory)
	for webm in glob.glob(os.path.join(incomingPath,  '*.webm' )):
	  (incomingPath, file) = os.path.split(webm)
	  (filename, extension) = os.path.splitext(file)
	  stage=  filename
	  shutil.move(incomingPath +'/'+file, directory)
	for jpg in glob.glob(os.path.join(incomingPath, '*.jpg')):
	  (incomingPath, file) = os.path.split(jpg)
	  (filename, extension) = os.path.splitext(file)
	  stage=  filename
	  shutil.move(incomingPath +'/'+file, directory)
	for html in glob.glob(os.path.join(incomingPath, '*.html')):
		  (incomingPath, file) = os.path.split(html)
		  (filename, extension) = os.path.splitext(file)
		  stage=  filename
		  shutil.move(incomingPath +'/'+file, directory)

get_videos(incoming.getPath())  


def get_matfiles(incomingPath):
    matDir =  incomingPath +  '/matfiles'
    if not os.path.exists(matDir):
      os.makedirs(matDir)     
    for mat in glob.glob(os.path.join(incomingPath, '*.mat')):
	  (incomingPath, file) = os.path.split(mat)
	  (filename, extension) = os.path.splitext(file)
	  stage = filename[:3]
	  shutil.move(incomingPath +'/'+file, matDir)
    for txt in glob.glob(os.path.join(incomingPath, 'OriginalDataDirectory.txt')):
          (incomingPath, file) = os.path.split(txt)
          (filename, extension) = os.path.splitext(file)
          stage = filename[:3]
          shutil.move(incomingPath +'/'+file, matDir)


get_matfiles(incoming.getPath())

tr = service.transaction(incoming, factory)
incoming = tr.getIncoming()

data_set = tr.createNewDataSet()
data_set.setDataSetType("HCS_IMAGE_SEGMENTATION_TRACKING_FEATURES")

data_set2 = tr.createNewDataSet()
data_set2.setDataSetType("HCS_ANALYSIS_SEGMENTATION_AND_FEATURES")


sampleIdentifier = "/"+extractSpaceCode(incoming.getPath())+"/"+extractPlateCode(incoming.getPath())
print sampleIdentifier
plate = tr.getSample(sampleIdentifier)
data_set.setSample(plate)
data_set2.setSample(plate)
# Get the search service
search_service = tr.getSearchService()

sc = SearchCriteria()
sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, extractDataSetCode(incoming.getPath()) ));
foundDataSets = search_service.searchForDataSets(sc)
if foundDataSets.size() > 0:
  data_set.setParentDatasets([ds.getDataSetCode() for ds in foundDataSets])
  data_set2.setParentDatasets([ds.getDataSetCode() for ds in foundDataSets])
  
videoPath = incoming.getPath() + '/videos'
tr.moveFile(videoPath, data_set)

matPath = incoming.getPath() + '/matfiles'
tr.moveFile(matPath, data_set2)