'''
This is handling bowtie-BAM files and extracts some properties from the BAM header and
the samtools flagstat command. The results are formatted and attached  as a property
to the openBIS DataSet.
Prerequisites are the DataSetType: ALIGNMENT and
the following properties assigned to the DataSetType mentioned above:
ALIGNMENT_SOFTWARE, ISSUED_COMMAND, SAMTOOLS_FLAGSTAT,
TOTAL_READS, MAPPED_READS

Obviously you need a working samtools binary

Uses 'flagstat' and 'view -H'
'''

import os
import fnmatch
import re
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchSubCriteria

FOLDER='/links/shared/dsu/dss/register-bowtie/'
SAMTOOLS='/links/application/dsu/samtools/samtools'
BAM_PATTERN='*.bam'

matches = []
searchStrings = ['@PG']
programList = []

# -----------------------------------------------------------------------------

def listSearch (myList, searchString):
  '''
  Searches for a given String in a list.
  Only lines matching  the start of a line a considerd as a match
  '''
  matches = []
  for i in range (0, len(myList)):
    if(re.match(searchString, myList[i])):
      matches.append(myList[i])
  return (matches)

# -----------------------------------------------------------------------------

def programParameters (programList):
  '''
  Extracts the aligner datils from the bam header
  '''
  elements = {}
  for program in range(0, len(programList)):
    line = programList[program].split('\t')
 
  for element in range (1, len(line)):
    key, value = line[element].split(":")
    elements[key] = value
    
  return elements  

# -----------------------------------------------------------------------------

def process(transaction):

  incomingPath = transaction.getIncoming().getName()

  dataSet = transaction.createNewDataSet("ALIGNMENT")
  dataSet.setMeasuredData(False)

  # Get the incoming name 
  # expected: 
  # Project_110907_SN792_0059_AC012FACXX_3/Sample_BSSE-DSU-1662/BSSE-DSU-1662_CGATGTA_L003_R1_001_sorted.bam
  split=incomingPath.split('_')
  if (len(split) == 6):
    incoming_sample=split[1]+ '_'+ split[2] + '_' + split[3] + '_' + split[4]+ ':' + split[-1]
  if (len(split) ==4):
    incoming_sample=split[1]+ '_'+ split[2] + ':' + split[-1]

  # Looking for BAMS:
  for root, dirnames, filenames in os.walk(FOLDER + incomingPath):
    for filename in fnmatch.filter(filenames, BAM_PATTERN):
        matches.append(os.path.join(root, filename))

  # Extract values from a samtools view and set the results as DataSet properties 
  # Command: samtools view -H ETHZ_BSSE_110429_63558AAXX_1_sorted.bam

  arguments = SAMTOOLS + ' view -H ' + matches[0]
  print('Arguments: '+ arguments)
  cmdResult=os.popen(arguments).read()

  properties=cmdResult.split("\n")
  for s in range (0, len(searchStrings)):
    programList = listSearch (properties, searchStrings[s])
  print(programList)  

  e = programParameters (programList)  

  dataSet.setPropertyValue("ALIGNMENT_SOFTWARE", e['ID'])
  dataSet.setPropertyValue("VERSION", e['VN'])
  dataSet.setPropertyValue("ISSUED_COMMAND", e['CL'])


  arguments = SAMTOOLS + ' flagstat ' + matches[0]

  cmdResult=os.popen(arguments).read()
  totalReads=cmdResult.split('\n')[0].split(' ')[0]
  mappedReads=cmdResult.split('\n')[2].split(' ')[0]

  dataSet.setPropertyValue("SAMTOOLS_FLAGSTAT", cmdResult)
  dataSet.setPropertyValue("TOTAL_READS", totalReads)
  dataSet.setPropertyValue("MAPPED_READS", mappedReads)
   
  # Add the incoming file into the data set
  transaction.moveFile(incomingPath, dataSet)

  # Get the search service
  search_service = transaction.getSearchService()

  # Search for the sample
  sc = SearchCriteria()
  sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, incoming_sample));
  foundSamples = search_service.searchForSamples(sc)

  if foundSamples.size() > 0:
    dataSet.setSample(foundSamples[0])
    
    # Search for parent data set of the same sample
    dataSetSc = SearchCriteria()
    dataSetSc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.TYPE, 'FASTQ_GZ'))
    dataSetSc.addSubCriteria(SearchSubCriteria.createSampleCriteria(sc))
    foundDataSets = search_service.searchForDataSets(dataSetSc)
    if foundDataSets.size() > 0:
      dataSet.setParentDatasets([ds.getDataSetCode() for ds in foundDataSets])
