'''
@copyright:
2013 ETH Zuerich, CISD

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
Processes each flow lane of a Sequencing run and attaches the fastq files to 
the correct corresponding library samples

@note:
print statements go to: <openBIS_HOME>/datastore_server/log/startup_log.txt
Expects as incoming folder:
Project_<Flow Cell>_<Lane>
e.g.Project_110715_SN792_0054_BC035RACXX_1


@author:
Manuel Kohler

Note: 
print statements go to: ~openbis/sprint/datastore_server/log/startup_log.txt
'''

import os
import fnmatch
import time
import shutil
from time import *
from datetime import *
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria

FASTQ_GZ_PATTERN = "*.fastq.gz"
METADATA_FILE_SUFFIX = "_metadata.tsv"
INDEX1='INDEX1'
INDEX2='INDEX2'
EXTERNAL_SAMPLE_NAME='EXTERNAL_SAMPLE_NAME'

DEFAULT_INDEX='NoIndex'

# -------------------------------------------------------------------------------

def getFileNames(path):
  '''
  Gets all files matching a PATTERN in a path recursively
  and returns the result as a list
  '''
  matches = []
  for root, dirnames, filenames in os.walk(path):
    for filename in fnmatch.filter(filenames, FASTQ_GZ_PATTERN):
        matches.append(os.path.join(root, filename))
  matches.sort()
  return(matches)

# -------------------------------------------------------------------------------

def writeMetadataFile (fileName, parentPropertyTypes, parentPropertiesMap, fcMetaDataDict, fcMetaDataList):
  '''
  Writes a file of meta date related to one sample
  '''
  try:
    metaDataFile = open(fileName,'w')
    for propertyType in parentPropertyTypes:
      metaDataFile.write(propertyType.encode('utf-8') + "\t" +
                         parentPropertiesMap[propertyType].tryGetAsString().encode('utf-8') + "\n")
    
    metaDataFile.write("\nFLOWCELL PROPERTIES\n".encode('utf-8'))

    for fcMetaData in fcMetaDataList:
      metaDataFile.write(fcMetaData.encode('utf-8') + "\t" +
                         fcMetaDataDict[fcMetaData].tryGetAsString().encode('utf-8') + "\n")
      pass
  except IOError:
    print ('File error, could not write '+ fileName)
  finally:
    metaDataFile.close()

# -------------------------------------------------------------------------------

def create_openbis_timestamp ():
  '''
  Create an openBIS conform timestamp
  '''
  tz=localtime()[3]-gmtime()[3]
  d=datetime.now()
  return d.strftime("%Y-%m-%d %H:%M:%S GMT"+"%+.2d" % tz+":00")

# -------------------------------------------------------------------------------

def getFlowCellMetaData (transaction, flowCellId):
  
  def sortedDictValues(adict):
    keys = adict.keys()
    keys.sort()
    return map(adict.get, keys)
  
  search = transaction.getSearchService()
  sc = SearchCriteria()
  print('Searching FlowCell: '+ str(flowCellId))
  sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, flowCellId));
  foundFlowCells = search.searchForSamples(sc)

  try:
    assert foundFlowCells.size() == 1
  except AssertionError:
    print (str(foundFlowCells.size()) + ' flow cells found which match the criterias: '+ flowCellId)
  
  fcPropertiesDict = {}
  fcPropertyTypes = []

  fcProperties = foundFlowCells[0].getSample().getProperties()
  for property in fcProperties:
      code = property.getPropertyType().getSimpleCode()
      fcPropertyTypes.append(code)
      fcPropertiesDict[code] = property
  
  fcPropertyTypes.sort()
  return fcPropertiesDict, fcPropertyTypes

# -------------------------------------------------------------------------------

def searchSample(search_service, sampleCode):

  sc = SearchCriteria()
  sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, sampleCode));
  foundSamples = search_service.searchForSamples(sc)

  # there should be only one sample because it is unique
  if (len(foundSamples) > 1):
    raise Exception("More than one sample found! No unique code: " + sampleCode)
  elif (len(foundSamples) == 0):
    raise Exception("No matching sample found for: " + sampleCode)
  else :
    sample = foundSamples[0]
  return sample

# -------------------------------------------------------------------------------
def process(transaction):

  # useful for debugging:
  print ("\n")
  print(datetime.now())

  search_service = transaction.getSearchService()

  incomingPath = transaction.getIncoming().getAbsolutePath()
  name = transaction.getIncoming().getName()

  folders=[]
  folders=os.listdir(incomingPath)

  # expected incoming Name, e.g.: Project_110715_SN792_0054_BC035RACXX_1
  split=name.split("_")
  if (len(split) == 6):
    runningDate = split[1]
    sequencerId = split[2]
    sequentialNumber = split[3]
    hiseqTray = split[4][0]
    flowCellId = split[4][1:]
    flowLane = split[-1]
    incoming_sample = flowCellId + ':' + flowLane
  # expected Project_120112_63537AAXX_1
  if (len(split) == 4):
    runningDate = split[1]
    flowCellId = split[2]
    flowLane = split[-1]
    incoming_sample = flowCellId + ':' + flowLane

  # Search for the incoming_sample which is a Flow Lane
  sc = SearchCriteria()
  print('Processing sample: '+ str(incoming_sample))
  sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, incoming_sample));
  foundSamples = search_service.searchForSamples(sc)

  # there should be only one sample because it is unique within one Flow Cell 
  if (len(foundSamples) > 1):
    raise Exception("More than one sample found! No unique code: " + incoming_sample)
  elif (len(foundSamples) == 0):
    raise Exception("No matching sample found for: " + incoming_sample)
  else :
    sample = foundSamples[0].getSample()
    parents = sample.getParents()

  # -------------------------------------------------------------------------------

  # loop over each Sample folder within a lane
  for f in range(0,len(folders)):
    # Create a data set and set type
    dataSet = transaction.createNewDataSet("FASTQ_GZ")
    dataSet.setMeasuredData(False)
    dataSet.setPropertyValue(INDEX1, DEFAULT_INDEX)
    dataSet.setPropertyValue(INDEX2, DEFAULT_INDEX)
    dirName = transaction.createNewDirectory(dataSet,folders[f])

    # if multiplexed samples then there is more than one folder
    pathPerLane = incomingPath + '/' + folders[f]
    
    # get all fastqs in this dataSet
    fastqFileList=getFileNames(pathPerLane)
    
    # put the files into the dataSet 
    for file in fastqFileList:
      sampleCode = '-'.join(file.split('/')[-1].split('_')[:4])
      print "Searching for " + sampleCode
      sample = searchSample(search_service, sampleCode)

      # finally add the files to the data set     
      transaction.moveFile(file , dataSet, folders[f])
   
      #sa = transaction.getSampleForUpdate(sample.getSampleIdentifier())
      #sa.setPropertyValue("DATA_TRANSFERRED", create_openbis_timestamp())
      dataSet.setSample(sample)
