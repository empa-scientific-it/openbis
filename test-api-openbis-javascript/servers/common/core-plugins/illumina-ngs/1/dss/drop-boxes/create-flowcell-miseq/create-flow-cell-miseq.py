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
Parses the two Illumina provided files 'RunParameters.xml' and 'RunInfo.xml'
and creates one Sample of type 'ILLUMINA_FLOW_CELL' and sets Sample properties 
from those two XML files. Additionally the number of lanes are read out and
are created as contained samples of type 'ILLUMINA_FLOW_LANE'.

@note: 
print statements go to: <openBIS_HOME>/datastore_server/log/startup_log.txt
expected incoming Name for MiSeq runs: 120726_M00721_0011_A000000000-A1FVF

@author:
Manuel Kohler
'''

import os
import shutil
from time import *
from datetime import *
import xml.etree.ElementTree as etree
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria

RUNPARAMETERS = 'RunParameters.xml'
#RUNPARAMETERS = 'runParameters.xml'
RUNINFO = 'RunInfo.xml'
FLOWCELL_SPACE='/BSSE_FLOWCELLS/'
FLOWCELL_PROJECT='FLOWCELLS/'
EXPERIMENT_TYPE_CODE='HT_SEQUENCING'

# Mapping between XML file naming and openBIS properties

RUNINFO_XML = {'LANECOUNT':'LaneCount', 'SURFACECOUNT':'SurfaceCount',
   'SWATHCOUNT':'SwathCount', 'TILECOUNT':'TileCount'}

#------------------------------------------------------------------------------

class parseXmlFile: 
    
  def __init__(self, xmlFile):
    self.xmlFile = xmlFile
    self.tree = etree.parse(self.xmlFile)
    self.root = self.tree.getroot()

  def getXmlElement (self, elementName):
    '''
    Returns the text value of a given XML element
    '''
    for e in self.root.getchildren():
      element = e.find(elementName)
      if element is None:
	return 'None'
      else:
	return element.text

  def getAllchildren (self, elementName):
    '''
    finds all children of a given XML Element and returns them as list
    '''
    for e in self.root.getchildren():
      # the '//' means look recursively for all children not only direct ones
      childList  = self.tree.findall('//' + elementName)
    return childList 

# -----------------------------------------------------------------------------

def create_openbis_timestamp (): 
  ''' 
  Create an openBIS conform timestamp
  '''
  tz=localtime()[3]-gmtime()[3]
  d=datetime.now()
  return d.strftime("%Y-%m-%d %H:%M:%S GMT"+"%+.2d" % tz+":00")

# -----------------------------------------------------------------------------

def registerFlowLane(a_lane, transaction, name, newFlowCell):
  '''
  Registers a new Flow lane 
  '''
  newFlowLane = transaction.createNewSample(FLOWCELL_SPACE + name + ':' + str(a_lane), "ILLUMINA_FLOW_LANE")
  newFlowLane.setContainer(newFlowCell)
  
# -----------------------------------------------------------------------------
def process(transaction):

  incoming = transaction.getIncoming()
  incomingPath = incoming.getAbsolutePath()

  name = incoming.getName()
  # ['120726', 'M00721', '0011', 'A000000000-A1FVF']
  runDate, MiseqID, runningNumber, trayAndFcId = name.split("_")
  tray = trayAndFcId[0]
  fcId = trayAndFcId[1:]

  # -----------------------------------------------------------------------------

  # Search for the sample and check if there is already sample with this name
  search_service = transaction.getSearchService()
  sc = SearchCriteria()
  sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, name));
  foundSamples = search_service.searchForSamples(sc)
  if foundSamples.size() > 0:
    raise NameError('Already found a Flow Cell with the following name: '+ name)  

  # Parse the RunInfo.xml file
  runInfo = parseXmlFile(incomingPath + '/' + RUNINFO) 

  # Create a new Flow Cell and set the experiment
  newFlowCell = transaction.createNewSample(FLOWCELL_SPACE + name, "ILLUMINA_FLOW_CELL")
  exp = transaction.getExperiment(FLOWCELL_SPACE + FLOWCELL_PROJECT + datetime.now().strftime("%Y.%m"))
  if exp == None:
    exp = transaction.createNewExperiment(FLOWCELL_SPACE + FLOWCELL_PROJECT + datetime.now().strftime("%Y.%m"),
	       EXPERIMENT_TYPE_CODE)
  newFlowCell.setExperiment(exp)
   
  run = runInfo.getAllchildren('Run')[0].attrib
  if (run['Id'] != name):
    raise NameError('Flowcell names do not match between directory name '+ name +
	  ' and ' + RUNINFO + 'property file: ' + run['Id'])

  runParameters = parseXmlFile(incomingPath + '/' + RUNPARAMETERS)
  RTAversion = (runParameters.getAllchildren('RTAVersion'))[0].text
  print RTAversion
  newFlowCell.setPropertyValue("ILLUMINA_PIPELINE_VERSION", RTAversion)
    
  def setFcProperty(searchId, dict):
    children = runInfo.getAllchildren(searchId) 
    for element in (dict):
      if (element <> '') and (dict[element] <> ''):
	newFlowCell.setPropertyValue(element, children[0].attrib[dict[element]])
  
  # Reading out <FlowcellLayout LaneCount="1" SurfaceCount="1" SwathCount="1" TileCount="12" />
  setFcProperty('FlowcellLayout', RUNINFO_XML)

  sequencer = runInfo.getAllchildren('Instrument')
  newFlowCell.setPropertyValue("SEQUENCER", sequencer[0].text)

  readMap = {}
  reads = runInfo.getAllchildren('Reads')
  read = reads[0].findall('Read')

  for r in read:
    cycles = r.get('NumCycles', 'str')
    number = r.get('Number', 'str')
    isIndexed = r.get('IsIndexedRead', 'str')
    readMap[number] = [cycles, isIndexed]

    # example of readMap: {'1': ['151', 'N'], '2': ['8', 'Y'], '3': ['8', 'Y'], '4': ['151', 'N']}
   
    newFlowCell.setPropertyValue("CYCLES_REQUESTED_BY_CUSTOMER", readMap['1'][0])
   
    indexCount = 0
    readCount = 0

    for e in readMap:
      if readMap[e][1] == 'Y':
	indexCount += 1
      else:
	readCount += 1

    if indexCount == 2:
      newFlowCell.setPropertyValue("END_TYPE", "PAIRED_END")
    else:
      newFlowCell.setPropertyValue("END_TYPE", "SINGLE_READ")

    try:
      newFlowCell.setPropertyValue("INDEXREAD", readMap['2'][0])
    except:
      newFlowCell.setPropertyValue("INDEXREAD", '0') 

    try:
      newFlowCell.setPropertyValue("INDEXREAD2", readMap['3'][0])
    except:
      newFlowCell.setPropertyValue("INDEXREAD2", '0')

  newFlowCell.setPropertyValue("FLOW_CELL_SEQUENCED_ON", create_openbis_timestamp())

  # get the number of lanes
  maxLanes = runInfo.getAllchildren('FlowcellLayout')[0].attrib[RUNINFO_XML['LANECOUNT']]

  [registerFlowLane(lane, transaction, name, newFlowCell) for lane in range(1,int(maxLanes)+1)]

  shutil.rmtree(incomingPath)
