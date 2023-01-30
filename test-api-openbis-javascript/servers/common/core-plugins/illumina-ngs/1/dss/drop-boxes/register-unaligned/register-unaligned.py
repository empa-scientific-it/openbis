'''
@copyright:
2012 ETH Zuerich, CISD
    
@license: 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    
http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

@description:
Expects as incoming folder: <FlowCell>/Unaligned_no_mismatch

@note: 

@author:
Manuel Kohler
'''

import os
import glob
import shutil
import time
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchSubCriteria

BASECALL_STATS_FOLDER = 'Basecall_Stats_'
REGEX_FILES = '*.*'
REGEX_MAKEFILE = 'Make*'
REGEX_LANES='/P*'
REGEX_UNDETERMINED = '/U*/Sample*'
UNALIGNED_FOLDER='Unaligned_no_mismatch'
LANE_FOLDER='/links/shared/dsu/dss/register-lane-hiseq/'
MARKER_STRING='.MARKER_is_finished_'

def touch_markerfile(filename):
  try:  
    # do a touch
    open(filename, 'w').close()
  except:
    print('Could not touch ' + filename)  

def searchForLaneParents(transaction, sampleCode):

  search_service = transaction.getSearchService()
  sc = SearchCriteria()
  sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, sampleCode))
  foundSamples = search_service.searchForSamples(sc)
  # there should be only one sample because it is unique within one Flow Cell 
  if (len(foundSamples) > 1):
    raise Exception("More than one sample found! No unique code: " + sampleCode)
  elif (len(foundSamples) == 0):
    raise Exception("No matching sample found for: " + sampleCode)
  else :
    sample = foundSamples[0].getSample()
    parents = sample.getParents()

  # search for the parents
  sc = SearchCriteria()
  # set the Search Criteria to an OR condition, default is AND
  sc.setOperator(SearchCriteria.SearchOperator.MATCH_ANY_CLAUSES)
  # Get the codes for all parents
  for parent in parents:
    parentSubCode = parent.getSubCode()
    sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, parentSubCode));
  # all parents of the flow lane
  foundParents = search_service.searchForSamples(sc)
  
  parentCodeList = []

  for foundParent in foundParents:
    parent = foundParent.getSample()
    # ArrayList 
    parentProperties = parent.getProperties()
    # just get the current code
    parentCode = parent.getCode()
    parentCodeList.append(parentCode)
    #print("Found parent code: "+ parentCode)

  return parentCodeList


def renameFiles(transaction, dir, flowcellName):
  # Limit of Samples when the Sample Code is still used in the renaming. If Number of Samples is
  # bigger than this number, we just write the number in the file name.

  MAX_SAMPLES = 20
  FACILITY_CODE = "BSSE_QGF_"

  for root, dirs, files in os.walk(dir):
    for file in files:
      lane = file.split("_")[0][-1]      
      if lane.isdigit():
        sampleCode = flowcellName + ":" + lane 
        print sampleCode
        parentCodeList = searchForLaneParents(transaction, sampleCode)
        print parentCodeList
        length = len(parentCodeList)
        if length > MAX_SAMPLES:
          # BSSE_QGF_96_samples_C1A8YACXX_Undetermined_L008_R1_001.fastq.gz
          os.rename(root + '/' + file, root + "/" + FACILITY_CODE + str(length) + "_samples_" + flowcellName + "_" + file)
        else:
          # BSSE_QGF_10001_10002_10003_10004_C1A8YACXX_Undetermined_L008_R1_001.fastq.gz
          SampleCodeString = "_".join([(e.split("-")[-1]) for e in parentCodeList])
          os.rename(root + '/' + file, root + "/" + FACILITY_CODE + SampleCodeString + "_" + flowcellName + "_" + file)

def process(transaction):

  incomingPath = transaction.getIncoming().getPath()
  name = transaction.getIncoming().getName()

  split=name.split("_")
  if (len(split) == 4):
    DSTYPE='ILLUMINA_HISEQ_OUTPUT'
    flowcell=name.split("_")[-1][1:]
  if (len(split) ==2):
    DSTYPE='ILLUMINA_GA_OUTPUT'
    flowcell=name.split("_")[-1]

  #move Lanes into a different drop box
  laneList=glob.glob(incomingPath + '/'+ UNALIGNED_FOLDER + REGEX_LANES)
  laneList.sort()

  undeterminedList=glob.glob(incomingPath + '/'+ UNALIGNED_FOLDER + REGEX_UNDETERMINED)
  undeterminedList.sort()

  # add the Flow Cell Name to the Undetermined FASTQ files
  [renameFiles(transaction, dir, name) for dir in undeterminedList]

  # Multiplexing:
  # First move the Undetermined reads to the other ones 
  [shutil.move(undeterminedLane, laneList[int(undeterminedLane.split('/')[-1][-1])-1] +'/' + undeterminedLane.split('/')[-1]) for undeterminedLane in undeterminedList]

  [shutil.move(lane, LANE_FOLDER+lane.split('/')[-1]) for lane in laneList]
  markerFileList = [touch_markerfile(LANE_FOLDER+MARKER_STRING+lane.split('/')[-1]) for lane in laneList]

  # Create a data set and set type
  dataSet = transaction.createNewDataSet("BASECALL_STATS")
  dataSet.setMeasuredData(False)
    
  # Build up a list of file which are part of the data set
  fileList=glob.glob(incomingPath + '/'+ UNALIGNED_FOLDER + '/' + REGEX_FILES)
  [fileList.append(i) for i in glob.glob(incomingPath + '/' + UNALIGNED_FOLDER +'/' +REGEX_MAKEFILE)]

  # Add the incoming file into the data set
  transaction.createNewDirectory(dataSet, UNALIGNED_FOLDER) 
  # move all files is data set
  [transaction.moveFile(file, dataSet, UNALIGNED_FOLDER)  for file in fileList]
  # move base call stat dir into data set
  print("flowcell: "+flowcell)
  transaction.moveFile(incomingPath + '/' + UNALIGNED_FOLDER + '/' +  BASECALL_STATS_FOLDER + flowcell, dataSet, UNALIGNED_FOLDER + '/')
    
  # Get the search service
  search_service = transaction.getSearchService()

  # Search for the sample
  sc = SearchCriteria()
  sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, name))
  foundSamples = search_service.searchForSamples(sc)

  if foundSamples.size() > 0:
    dataSet.setSample(foundSamples[0])

    # Search for another data set of the same sample and make it a child of it 
    dataSetSc = SearchCriteria()
    dataSetSc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.TYPE, DSTYPE))
    dataSetSc.addSubCriteria(SearchSubCriteria.createSampleCriteria(sc))
    foundDataSets = search_service.searchForDataSets(dataSetSc)
    if foundDataSets.size() > 0:
      dataSet.setParentDatasets([ds.getDataSetCode() for ds in foundDataSets])

  shutil.rmtree(incomingPath)
