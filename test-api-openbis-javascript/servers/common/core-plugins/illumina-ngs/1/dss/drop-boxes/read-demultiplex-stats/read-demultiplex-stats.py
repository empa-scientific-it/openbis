'''
  @copyright: 2012 ETH Zuerich, CISD
  
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


@author: Manuel Kohler

XML Structur which is processed:

<?xml version="1.0"?>
<Summary>
  <Lane index="8">
    <Sample index="lane8">
      <Barcode index="Undetermined">
        <Tile index="1101">
          <Read index="1">
            <Raw>
              <Yield>1921250</Yield>
              <YieldQ30>949680</YieldQ30>
              <ClusterCount>38425</ClusterCount>
              <ClusterCount0MismatchBarcode>0</ClusterCount0MismatchBarcode>
              <ClusterCount1MismatchBarcode>0</ClusterCount1MismatchBarcode>
              <QualityScoreSum>40995660</QualityScoreSum>
            </Raw>
            <Pf>
              <Yield>945450</Yield>
              <YieldQ30>854815</YieldQ30>
              <ClusterCount>18909</ClusterCount>
              <ClusterCount0MismatchBarcode>0</ClusterCount0MismatchBarcode>
              <ClusterCount1MismatchBarcode>0</ClusterCount1MismatchBarcode>
              <QualityScoreSum>33815505</QualityScoreSum>
            </Pf>
          </Read>
        </Tile>
        [...]

@note: 
print statements go to: <openBIS_HOME>/datastore_server/log/startup_log.txt
'''

import time
import os
import fnmatch
import xml.etree.ElementTree as etree
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchSubCriteria


class parseXmlFile:

  def __init__(self, xmlFile):
    self.xmlFile = xmlFile
    self.tree = etree.parse(self.xmlFile)
    self.root = self.tree.getroot()

# -----------------------------------------------------------------------------

class qcValues(object):
  def __init__(self, Yield = 0, YieldQ30 = 0, ClusterCount = 0,
               ClusterCount0MismatchBarcode = 0, ClusterCount1MismatchBarcode = 0,
               QualityScoreSum = 0, *args, **kwargs):
    self.Yield = Yield
    self.YieldQ30 = YieldQ30
    self.ClusterCount = ClusterCount 
    self.ClusterCount0MismatchBarcode = ClusterCount0MismatchBarcode
    self.ClusterCount1MismatchBarcode = ClusterCount1MismatchBarcode
    self.QualityScoreSum = QualityScoreSum
    
  def __str__(self):
        return "Yield: %s, YieldQ30: %s, ClusterCount: %s, ClusterCount0MismatchBarcode: %s," \
          " CusterCount1MismatchBarcode: %s, QualityScoreSum: %s" \
          % (self.Yield, self.YieldQ30, self.ClusterCount, self.ClusterCount0MismatchBarcode,
          self.ClusterCount1MismatchBarcode, self.QualityScoreSum)    

class sample:
  def __init__(self, Lane = 0, Sample = '', Barcode = '', Tile = '', Read = '', rawqc = qcValues([]),
                pfqc = qcValues([]), *args, **kwargs):
    self.Lane = Lane
    self.Sample = Sample
    self.Barcode = Barcode
    self.Tile = Tile
    self.Read = Read
    self.rawqc = rawqc
    self.pfqc = pfqc
    
  def __str__(self):
    return "Lane: %s, Sample: %s, Barcode: %s, Tile: %s, Read: %s, rawqc: %s, pfqc: %s" \
      % (self.Lane, self.Sample, self.Barcode, self.Tile, self.Read, self.rawqc, self.pfqc)

# -----------------------------------------------------------------------------

class Statistics:
  def __init__(self, lane = 0, sampleName = "", index1 = "NoIndex", index2 = "NoIndex", pfYieldSum = 0,
                 rawYieldSum = 0, pfPercentage = 0.0, rawReadsSum = 0, pfReadsSum = 0,
                 pfYieldQ30Sum = 0, qualityScoreSum = 0, rawPercentageReadsPerLane = 0.0,
                 pfYieldQ30Percentage = 0.0, pfsumQualityScore = 0, pfmeanQualityScore = 0.0):
      self.lane = lane
      self.sampleName = sampleName
      self.index1 = index1
      self.index2 = index2
      self.pfYieldSum = pfYieldSum
      self.rawYieldSum = rawYieldSum
      self.pfPercentage = pfPercentage
      self.rawReadsSum = rawReadsSum
      self.pfReadsSum = pfReadsSum
      self.pfYieldQ30Sum = pfYieldQ30Sum
      self.qualityScoreSum = qualityScoreSum
      self.rawPercentageReadsPerLane = rawPercentageReadsPerLane
      self.pfYieldQ30Percentage = pfYieldQ30Percentage
      self.pfsumQualityScore = pfsumQualityScore
      self.pfmeanQualityScore = pfmeanQualityScore
      
  def __str__(self):
    return "lane: %s, sampleName: %s, index1: %s, index2: %s, pfYieldSum: %s, pfPercentage: %s," \
           " rawReadsSum: %s, pfReadsSum: %s," \
           " rawPercentageReadsPerLane: %s, pfYieldQ30Percentage: %s," \
           " pfmeanQualityScore: %s" \
        % (self.lane, self.sampleName, self.index1, self.index2, self.pfYieldSum, self.pfPercentage,
           self.rawReadsSum, self.pfReadsSum,
           self.rawPercentageReadsPerLane, self.pfYieldQ30Percentage, self.pfmeanQualityScore)
         
  def calculatePercentagePF (self, rawYield = 0, pfYield = 1):
    try:
      return round(float(pfYield) / float(rawYield) * 100, 2)
    except:
      return 0.0
  
  def calulateMeanQualityScore (self, pfqualityScoreSum = 0, pfYield = 1):
    try:
      return round (float(pfqualityScoreSum) / float(pfYield), 2)
    except:
      return 0.0
    
  def calculateYieldQ30Percentage (self, pfYieldQ30 = 0, pfYield = 1):
    try:
      return round (float(pfYieldQ30) / float(pfYield) * 100, 2)
    except:
      return 0.0

# -----------------------------------------------------------------------------

def xml2Memory(DEMULTIPLEX_XML):
  '''
  Parse the XML file and put all values in a memory structure:
  List of: 
  lane, sample, barcode, tile, read, qcRawList, qcPfList
  '''

  RAW_TAG = "Raw"
  PF_TAG = "Pf"

  sampleList = []
  
  xml = parseXmlFile(DEMULTIPLEX_XML)
  r = xml.tree.getroot()

  for lane in r.getchildren():
    for mysample in lane:  
      for barcode in mysample:       
        for tile in barcode:
          for read in tile:
            
            qcRaw = qcValues()
            qcPf = qcValues()  
            qcRawList = []
            qcPfList = []

            # Read out the Raw fields
            raw = read.find(RAW_TAG) 
            for child in raw.getchildren():
              # equivalent to a Java reflection
              setattr(qcRaw, child.tag, int(child.text))
              
            # Read out the Pf fields
            pf = read.find(PF_TAG)
            for child in pf.getchildren():
              # equivalent to a Java reflection
              setattr(qcPf, child.tag, int(child.text))

            qcRawList.append(qcRaw)
            qcPfList.append(qcPf)
            
            singleElement = sample ()
            
            setattr(singleElement, lane.tag, lane.attrib)
            setattr(singleElement, mysample.tag, mysample.attrib)
            setattr(singleElement, barcode.tag, barcode.attrib)
            setattr(singleElement, tile.tag, tile.attrib)
            setattr(singleElement, read.tag, read.attrib)
            singleElement.rawqc = qcRawList
            singleElement.pfqc = qcPfList
            
            sampleList.append(singleElement)
  return sampleList

# -----------------------------------------------------------------------------

def calculateStatistics(listofSamples):
  '''
  Structure of 'listofSamples'
  Lane: {'index': '6'}, Sample: {'index': 'BSSE-QGF-3524_C0NKPACXX'}, Barcode: {'index': 'TGACCA'},
  Tile: {'index': '2307'}, Read: {'index': '1'}, rawqc:<mem>, pfqc:<mem> 
  '''

  numberOfTiles = len(listofSamples)
  
  tile = sample()
  raw = qcValues ()
  pf = qcValues ()
  stats = Statistics()
 
  for tile in listofSamples:
    raw = tile.rawqc[0]
    pf = tile.pfqc[0]
    
    stats.pfYieldSum += pf.Yield
    stats.rawYieldSum += raw.Yield
    stats.rawReadsSum += raw.ClusterCount 
    stats.pfReadsSum += pf.ClusterCount
    stats.pfYieldQ30Sum += pf.YieldQ30
    stats.qualityScoreSum += pf.QualityScoreSum
     
  # Can not be set here, needs to be calculated later   
  #stats.rawPercentageReadsPerLane = rawPercentageReadsPerLane
  stats.pfPercentage = stats.calculatePercentagePF(stats.rawYieldSum, stats.pfYieldSum)
  stats.pfYieldQ30Percentage = stats.calculateYieldQ30Percentage(stats.pfYieldQ30Sum, stats.pfYieldSum)
  stats.pfmeanQualityScore = stats.calulateMeanQualityScore(stats.qualityScoreSum, stats.pfYieldSum)
  stats.lane = listofSamples[0].Lane.values()[0]
  stats.sampleName = listofSamples[0].Sample.values()[0]
  index = listofSamples[0].Barcode.values()[0]
  try:
    stats.index1, stats.index2 = index.split("-")
  except:
    stats.index1 = index
  return stats

# -----------------------------------------------------------------------------


def rawReadSumPerSamples(stat):
  '''
  Creates a dictionary with the lanes as keys
  The values are a list where the elements are a dictionary again.
  This dictionary has the sample names as key and the RawReadSum as value.  
  
  Example:
  {4': [{'BSSE-QGF-3434_C0NKPACXX': 248999502}], '7': [{'lane7': 123921974}, 
  {'BSSE-QGF-3527_C0NKPACXX': 38587703}, {'BSSE-QGF-3529_C0NKPACXX': 30130893},
  {'BSSE-QGF-3528_C0NKPACXX': 34519296}, {'BSSE-QGF-3526_C0NKPACXX': 34980179}]}
  '''
  
  laneDict = {}
  for e in stat:
    if e.lane not in laneDict:
      laneDict[e.lane] = [{e.sampleName:e.rawReadsSum}]
    else:
      laneDict[e.lane].append({e.sampleName:e.rawReadsSum})
  return laneDict

# -----------------------------------------------------------------------------

def createSumRawReadsPerLane(laneDict):
  '''
  Creates a dictionary with lane as key and sum of Raw Reads as value:
  {'1': 183180877, '3': 244968562, '2': 191496395, '5': 193466239, '4': 248999502, 
  '7': 262140045, '6': 257136830, '8': 209948449}
  '''
  sumRawReadsDict = {}
  for lane in laneDict:
    sumRawReads = 0
    for sampleNameDict in laneDict[lane]:
      sumRawReads += sampleNameDict.values()[0]
    
    sumRawReadsDict[lane] = sumRawReads 
  return sumRawReadsDict

# -----------------------------------------------------------------------------

def createPercentagePerLane(laneDict, sumRawReadsDict):
  '''
  Creates a dictionary with the sample Name as key and the percentage of raw reads related to 
  all reads in the same lane
  {'lane7': 47.27, 'BSSE-QGF-3433_C0NKPACXX': 100.0, 'BSSE-QGF-3666_C0NKPACXX': 54.12}
  '''
  
  relRawReadsDict = {}
  for lane in laneDict:
    for sampleName in laneDict[lane]:
      relRawReadsDict[sampleName.keys()[0]] = round(float(sampleName.values()[0]) / 
                                                    float(sumRawReadsDict[lane]) * 100, 2)
  return relRawReadsDict

# -----------------------------------------------------------------------------

def locate(pattern, root):
    '''Locate all files matching supplied filename pattern in and below
    supplied root directory.'''
    for path, dirs, files in os.walk(os.path.abspath(root)):
        for filename in fnmatch.filter(files, pattern):
            yield os.path.join(path, filename)

# -----------------------------------------------------------------------------

def getVocabulary(transaction, vocabularyCode):
 
  vocabularyTermList = [] 
  vocabulary = transaction.getSearchService().searchForVocabulary(vocabularyCode)
  if (vocabulary is None):
    print 'VOCABULARY %s does not exist' % (vocabularyCode)
  else:
    print "Getting VOCABULARY: " + vocabulary.getCode() 
    for term in vocabulary.getTerms():
            vocabularyTermList.append(term.getCode())           
  vocabularyTermList.sort()
  return vocabularyTermList

# -----------------------------------------------------------------------------

def getFlowCellMetaData (transaction,flowCellId):

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
    print (str(foundFlowCells.size()) + \
          ' flow cells found which match the criterias: '+ flowCellId)

  fcPropertiesDict = {}
  fcPropertyTypes = []

  fcProperties = foundFlowCells[0].getSample().getProperties()
  for property in fcProperties:
      code = property.getPropertyType().getSimpleCode()
      fcPropertyTypes.append(code)
      fcPropertiesDict[code] = property.getValue()

  fcPropertyTypes.sort()
  return fcPropertiesDict, fcPropertyTypes


# -----------------------------------------------------------------------------

def process(transaction):
  '''
  Main
  '''

  FASTQ_DATA_SET_TYPE='FASTQ_GZ'
  DEMUX_FILE='Flowcell_demux_summary.xml'
  NO_INDEX='NOINDEX'
  UNDETERMINED='UNDETERMINED'

  incomingPath = transaction.getIncoming().getPath()
  name = transaction.getIncoming().getName()

  print('\n'+time.ctime())

  fcPropertiesDict, fcPropertyTypes = getFlowCellMetaData(transaction, name)
  print fcPropertiesDict
  print fcPropertyTypes

  search_service = transaction.getSearchService()

  FileGenerator= locate(DEMUX_FILE, incomingPath)
  DEMULTIPLEX_XML = FileGenerator.next()

  sampleList = xml2Memory(DEMULTIPLEX_XML)    

  sa = sample()
  sampleDict = {}

  # key = sample name, value = sample()  
  for element in range(0, len(sampleList)):
    sa = sampleList[element]
    # Check if new sample 
    if (sa.Sample is not sampleList[element - 1].Sample):
      sampleName = sa.Sample.values()[0]
      sampleDict[sampleName] = [sa]
    else:
      sampleDict[sampleName].append(sa)
      
  stat = [calculateStatistics(sampleDict[mysample]) for mysample in sampleDict]

  # calculate the relative amount of reads per index
  laneDict = rawReadSumPerSamples(stat)
  sumRawReadsDict = createSumRawReadsPerLane(laneDict)
  relRawReadsDict = createPercentagePerLane(laneDict, sumRawReadsDict)

  # set the values in the object
  for mye in stat:
    mye.rawPercentageReadsPerLane = relRawReadsDict[mye.sampleName]
   
  def sampleSearch(Code=''):
    sc = SearchCriteria()
    numberOfLanes = 0
    sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, Code));
    search_service = transaction.getSearchService()
    foundSample = search_service.searchForSamples(sc)
    if foundSample.size() > 0:
      # Search for contained samples
      sampleSc = SearchCriteria()
      sampleSc.addSubCriteria(SearchSubCriteria.createSampleContainerCriteria(sc))
      foundContainedSamples = search_service.searchForSamples(sampleSc)
      numberOfLanes = foundContainedSamples.size()
    return foundSample, foundContainedSamples, numberOfLanes

#--------------------------------------------------------------------------------------------------------------------------------------

  def searchDataSetsofSample(sample, index1, index2, DATA_SET_TYPE):
    sc = SearchCriteria()
    sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, sample));
    search_service = transaction.getSearchService()
    foundSample = search_service.searchForSamples(sc)

    dataSetSc = SearchCriteria()
    # set the Search Criteria to an OR condition, default is AND
    #dataSetSc.setOperator(SearchCriteria.SearchOperator.MATCH_ANY_CLAUSES)
    dataSetSc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.TYPE, DATA_SET_TYPE))
    dataSetSc.addMatchClause(SearchCriteria.MatchClause.createPropertyMatch("BARCODE", index1 ))
    dataSetSc.addMatchClause(SearchCriteria.MatchClause.createPropertyMatch("INDEX2", index2))
    dataSetSc.addSubCriteria(SearchSubCriteria.createSampleCriteria(sc))
    foundDataSets = search_service.searchForDataSets(dataSetSc)
    print "foundDataSets.size() "+ str(foundDataSets.size())
    for ds in foundDataSets:
      print "Index1 for found Data Set" + ds.getDataSetCode() + " " + ds.getPropertyValue('BARCODE')
      print "Index2 for found Data Set" + ds.getDataSetCode() + " " + ds.getPropertyValue('INDEX2')
      
    return foundDataSets

#--------------------------------------------------------------------------------------------------------------------------------------

  def getIndexesofDataSetsofSample(sample, DATA_SET_TYPE):

    index1List = []
    index2List = []
    sc = SearchCriteria()
    sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, sample));
    search_service = transaction.getSearchService()
    foundSample = search_service.searchForSamples(sc)

    dataSetSc = SearchCriteria()
    dataSetSc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.TYPE, DATA_SET_TYPE))
    dataSetSc.addSubCriteria(SearchSubCriteria.createSampleCriteria(sc))
    foundDataSets = search_service.searchForDataSets(dataSetSc)
    for ds in foundDataSets:
      index1List.append(ds.getPropertyValue('BARCODE'))
      index2List.append(ds.getPropertyValue('INDEX2'))
    return index1List, index2List  


  flowcell, lanes, numberOfLanes  = sampleSearch(name)

  index1Length = fcPropertiesDict['INDEXREAD']
  index2Length = fcPropertiesDict['INDEXREAD2']

  for mystat in stat:
    laneCode = flowcell[0].getCode() + ":" + mystat.lane
    searchIndex1 = mystat.index1.upper()
    searchIndex2 = mystat.index2.upper()
    print '\n'
    print mystat
    
    index1List, index2List = getIndexesofDataSetsofSample(laneCode, FASTQ_DATA_SET_TYPE)
    print "Searching for "+ searchIndex1 + " in " + str(index1List)
    print "Searching for "+ searchIndex2 + " in " + str(index2List)
   
    if searchIndex1 not in (NO_INDEX): 
      if searchIndex1 not in (UNDETERMINED):
        if index1Length > 7:
          searchIndex1 = [ index1 for index1 in index1List if searchIndex1 in index1]
        else:
          searchIndex1 = [ index1 for index1 in index1List if searchIndex1 in index1[:-1]]
        try:
          searchIndex1 = searchIndex1[0]
        except:
          searchIndex1 = 'MISSING'
      else:
        searchIndex1 = NO_INDEX
    if searchIndex2 not in (NO_INDEX):
      if searchIndex2 not in (UNDETERMINED):
        if index2Length > 7:
          searchIndex2 = [ index2 for index2 in index2List if searchIndex2 in index2]
        else:
          searchIndex2 = [ index2 for index2 in index2List if searchIndex2 in index2[:-1]]
        try:
          searchIndex2 = searchIndex2[0]
        except:
          searchIndex1 = 'MISSING'
      else:
        searchIndex2 = NO_INDEX
    
    print "searchIndex1 " + str(searchIndex1)
    print "searchIndex2 " + str(searchIndex2)

    # Search for a data set with those two indices
    DataSet = searchDataSetsofSample(laneCode, searchIndex1, searchIndex2, FASTQ_DATA_SET_TYPE)
    try:
      assert DataSet.size() == 1
    except AssertionError:
      print (str(DataSet.size()) + ' data sets found which match the criterias: '+ 
	      str(laneCode), searchIndex1, searchIndex2)
      continue

    sa = transaction.getDataSetForUpdate(DataSet[0].getDataSetCode())
    sa.setPropertyValue('YIELD_MBASES', str(mystat.pfYieldSum))
    sa.setPropertyValue('RAW_YIELD_MBASES', str(mystat.rawYieldSum))
    sa.setPropertyValue('PERCENTAGE_PASSED_FILTERING',str(mystat.pfPercentage))
    sa.setPropertyValue('PF_READS_SUM',str(mystat.pfReadsSum))
    sa.setPropertyValue('RAW_READS_SUM',str(mystat.rawReadsSum))
    sa.setPropertyValue('PERCENTAGE_RAW_CLUSTERS_PER_LANE', str(mystat.rawPercentageReadsPerLane))
    sa.setPropertyValue('PFYIELDQ30PERCENTAGE', str(mystat.pfYieldQ30Percentage))
    sa.setPropertyValue('PFMEANQUALITYSCORE', str(mystat.pfmeanQualityScore))

    print "Modified data sets properties of: " + DataSet[0].getDataSetCode()

  print "DONE"
