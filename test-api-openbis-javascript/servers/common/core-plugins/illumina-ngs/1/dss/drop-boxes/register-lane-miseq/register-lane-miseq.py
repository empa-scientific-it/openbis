'''
Processes each flow lane of a Sequencing run

Note: 
print statements go to: ~openbis/sprint/datastore_server/log/startup_log.txt
'''

import os
import fnmatch
import time
import shutil
from time import *
from datetime import *
from itertools import islice, chain
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria

FASTQ_GZ_PATTERN = "*.fastq.gz"
METADATA_FILE_SUFFIX = "_metadata.tsv"

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

def create_openbis_timestamp ():
  '''
  Create an openBIS conform timestamp
  '''
  tz=localtime()[3]-gmtime()[3]
  d=datetime.now()
  return d.strftime("%Y-%m-%d %H:%M:%S GMT"+"%+.2d" % tz+":00")

def extraCopy (affiliationName, path):
  '''
  Handles the extra copies of the data for transfer with datamover via the
  bc2 network to the FMI and BIOCENTER
  For the BIOCENTER there is a folder created in which all data gets into
  '''
  if (affiliation_name in AFFILIATION):
    if (affiliation_name == 'BIOCENTER_BASEL'):
      dirname = AFFILIATION[affiliation_name] + datetime.now().strftime("%Y-%m-%d")
      if not os.path.exists(dirname):
        os.mkdir(dirname)
      shutil.copy(path, dirname)
    else:
      shutil.copy(path, AFFILIATION[affiliation_name])
# -------------------------------------------------------------------------------

def getFlowCellMetaData (flowCellId):
  
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

def searchForLane(lane, search_service):

  sc = SearchCriteria()
  print('Processing sample: '+ str(lane))
  sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, lane));
  foundSamples = search_service.searchForSamples(sc)

  # there should be only one sample because it is unique within one Flow Cell 
  if (len(foundSamples) > 1):
    raise Exception("More than one sample found! No unique code: " + lane)
  elif (len(foundSamples) == 0):
    raise Exception("No matching sample found for: " + lane)
  else :
    foundLane = foundSamples[0]

  return foundLane


# -------------------------------------------------------------------------------

def searchForParents (parents, search_service):
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
  return foundParents


# -------------------------------------------------------------------------------
'''
def batch(iterable, size):
    sourceiter = iter(iterable)
    while True:
        batchiter = islice(sourceiter, size)
        yield chain([batchiter.next()], batchiter)
'''
# -------------------------------------------------------------------------------

def process(transaction):

  # useful for debugging:
  print(datetime.now())

  incomingPath = transaction.getIncoming().getAbsolutePath()

  # Get the incoming name 
  name = transaction.getIncoming().getName()
  miSeqLane = name + ':1' 

  # Get the search service
  search_service = transaction.getSearchService()
  
  lane = searchForLane(miSeqLane, search_service)

  # get all fastqs in this dataSet
  fastqFileList=getFileNames(incomingPath)
  fastqFileList.sort()

  while fastqFileList:

    # Create a data set and set type
    dataSet = transaction.createNewDataSet("FASTQ_GZ")
    dataSet.setMeasuredData(False)
    if len(fastqFileList) == 1:
      transaction.moveFile(fastqFileList.pop(0) , dataSet)
    else:
      try:  
	 currentFullPath , currentFilename = os.path.split(fastqFileList[0])
	 nextFullPath , nextFilename = os.path.split(fastqFileList[1])
	 
         newpath =  currentFullPath + "/fastq"
         if not os.path.exists(newpath):
           os.makedirs(newpath)

	 if currentFilename.rsplit('_',2)[0] == nextFilename.rsplit('_',2)[0]:
           shutil.move(fastqFileList.pop(0), newpath)
           shutil.move(fastqFileList.pop(0), newpath)
	   transaction.moveFile(newpath , dataSet)  
	 else:
	   transaction.moveFile(fastqFileList.pop(0) , dataSet)
      except:
	pass 
    dataSet.setSample(lane)

  sa = transaction.getSampleForUpdate(lane.getSampleIdentifier())
  sa.setPropertyValue("DATA_TRANSFERRED", create_openbis_timestamp())
