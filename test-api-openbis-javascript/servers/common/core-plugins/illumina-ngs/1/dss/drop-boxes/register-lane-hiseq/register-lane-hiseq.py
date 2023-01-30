'''
Processes each flow lane of a Sequencing run

Expects as incoming folder:
Project_<Flow Cell>_<Lane>
e.g.Project_110715_SN792_0054_BC035RACXX_1 or Project_110816_6354LAAXX_1

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
AFFILIATION= {'FMI': '/links/shared/dsu/dss/customers/fmi/drop-box/','BIOCENTER_BASEL': '/links/shared/dsu/dss/customers/biozentrum/drop-box/'}
AFFILIATION_PROPERTY_NAME='AFFILIATION'
INDEX1='BARCODE'
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

def extraCopy (affiliation_name, path):
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

def searchParents (search_service, parents):

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


def process(transaction):

  # useful for debugging:
  print(datetime.now())

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
    incoming_sample=runningDate+ '_'+ sequencerId + '_' + sequentialNumber + '_' + hiseqTray + flowCellId + ':' + flowLane
  # expected Project_120112_63537AAXX_1
  if (len(split) ==4):
    runningDate = split[1]
    flowCellId = split[2]
    flowLane = split[-1]
    incoming_sample=runningDate+ '_'+ flowCellId + ':' + flowLane

  # Get the search service
  search_service = transaction.getSearchService()

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

  foundParents = searchParents(search_service, parents)

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
    print ("pathPerLane: " + pathPerLane)
    
    # get all properties of the parent samples
    for foundParent in foundParents:
      parent = foundParent.getSample()
      # ArrayList 
      parentProperties = parent.getProperties()
      # just get the current code
      parentCode = parent.getCode()
      #print("Found parent code: "+ parentCode)

      # reformat Java ArrayList and Sort
      parentPropertyTypes = []
      parentPropertiesMap = {}
      for property in parentProperties:
	code = property.getPropertyType().getSimpleCode()
	parentPropertyTypes.append(code)
	parentPropertiesMap[code] = property
	try:
	  barcode = parentPropertiesMap[INDEX1].tryGetAsString()
	  if barcode == "NOINDEX":
	    barcode = DEFAULT_INDEX
	  else:
	    barcode.split()[-1][:-1]
	except:
	  barcode = DEFAULT_INDEX
	
	try:
	  index2 = parentPropertiesMap[INDEX2].tryGetAsString()
	  if index2 == "NOINDEX":
	    index2 = DEFAULT_INDEX
	  else:
	    index2.split()[-1][:-1]
	except:
	  index2 = DEFAULT_INDEX

	# just use the first six nucleotides for the naming 
	completeBarcode=barcode + "-" + index2

      parentPropertyTypes.sort()
      # BSSE-1754_C0364ACXX_CTTGTAA-AACC_L007_R1_001.fastq.gz
      # BSSE_QGF_10002_121130_SN792_0189_AD1FBTACXX_1_NoIndex_L001_R1_001.fastq.gz
      nameOfFile = parentCode.replace('-','_') + "_" + incoming_sample.replace(':', '_') + "_" + completeBarcode + "_L00" + flowLane +METADATA_FILE_SUFFIX

      print folders[f]
      folderSplit = '-'.join(folders[f].split('_')[1:4])
      print "Folder split: " + folderSplit
      print str(parentCode)
   
      if (parentCode == folderSplit):
	dataSet.setPropertyValue(INDEX1, barcode)
	dataSet.setPropertyValue(INDEX2, index2)
        print parentPropertiesMap[EXTERNAL_SAMPLE_NAME].tryGetAsString()
        dataSet.setPropertyValue(EXTERNAL_SAMPLE_NAME, parentPropertiesMap[EXTERNAL_SAMPLE_NAME].tryGetAsString())
	print("Creating metadata file:" + nameOfFile)
	# get a file from the IDataSetRegistrationTransaction so it is automatically part of the data set
	pathToFile = transaction.createNewFile(dataSet, folders[f], nameOfFile)
	
	fcMetaDataDict, fcMetaDataList = getFlowCellMetaData(transaction, incoming_sample.split(":")[0])
	writeMetadataFile(pathToFile, parentPropertyTypes, parentPropertiesMap, fcMetaDataDict, fcMetaDataList)
   
	affiliation_name = parentPropertiesMap[AFFILIATION_PROPERTY_NAME].tryGetAsString()
	extraCopy (affiliation_name, pathToFile) 
   
    # get all fastqs in this dataSet
    fastqFileList=getFileNames(pathPerLane)
    
    # put the files into the dataSet 
    affiliation_name = parentPropertiesMap[AFFILIATION_PROPERTY_NAME].tryGetAsString()
    for file in fastqFileList:
      extraCopy (affiliation_name, file)
      # finally add the files to the data set     
      transaction.moveFile(file , dataSet, folders[f])
   
    if foundSamples.size() > 0:
      sa = transaction.getSampleForUpdate(foundSamples[0].getSampleIdentifier())
      sa.setPropertyValue("DATA_TRANSFERRED", create_openbis_timestamp())
      dataSet.setSample(foundSamples[0])

  shutil.rmtree(incomingPath)
