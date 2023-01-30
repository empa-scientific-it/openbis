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
Parses the two Illumina provided files 'runParameters.xml' and 'RunInfo.xml'
and creates one Sample of type 'ILLUMINA_FLOW_CELL' and sets Sample properties 
from those two XML files. Additionally the number of lanes are read out and
are created as contained samples of type 'ILLUMINA_FLOW_LANE'.

@note: 
print statements go to: <openBIS_HOME>/datastore_server/log/startup_log.txt
expected incoming Name for HiSeq runs: 110715_SN792_0054_BC035RACXX

structure:
110715_SN792_0054_BC035RACXX/
	runParameters.xml
	RunInfo.xml

The run folder created by the HiSeq sequencer is used to create a new 
ILLUMINA_FLOW_CELL in openBIS. The properties are then set by parsing the
two XML files in this folder.

@author:
Manuel Kohler
'''

import os
import shutil
from time import *
from datetime import *
import xml.etree.ElementTree as etree
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria

RUNPARAMETERS = 'runParameters.xml'
RUNINFO = 'RunInfo.xml'
FLOWCELL_SPACE='BSSE_FLOWCELLS'
FLOWCELL_PROJECT='FLOWCELLS'
EXPERIMENT_TYPE_CODE='HT_SEQUENCING'
FLOW_CELL='ILLUMINA_FLOW_CELL'
FLOW_LANE='ILLUMINA_FLOW_LANE'

FLOWCELL_PROJECT_ID = "/%(FLOWCELL_SPACE)s/%(FLOWCELL_PROJECT)s" % vars()

# Mapping between XML file naming and used in here
RUNPARAMETERS_XML = {'FLOWCELL':'Flowcell', 'RTAVERSION':'RTAVersion',
  'CONTROLLANE':'ControlLane', 'SBS':'Sbs', 'INDEX':'Index',
  'CYCLES':'Read1', 'PE':'Pe'}
RUNINFO_XML = {'LANECOUNT':'LaneCount', 'SURFACECOUNT':'SurfaceCount',
   'SWATHCOUNT':'SwathCount', 'TILECOUNT':'TileCount'}

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

def registerFlowLane(transaction, a_lane, flowCellName, newFlowCell):
  '''
  Registers a new Flow lane 
  '''
  newFlowLane = transaction.createNewSample('/' + FLOWCELL_SPACE + '/' + flowCellName +
                                            ':' + str(a_lane), FLOW_LANE)
  newFlowLane.setContainer(newFlowCell)

# -----------------------------------------------------------------------------

def extractFlowCellName (runFolderName):
  return runFolderName.split('_')[-1][1:]

# -----------------------------------------------------------------------------

def searchFlowCell (transaction, flowCellName):
  '''
  Search for the sample and check if there is already sample with this name
  '''
  search_service = transaction.getSearchService()
  sc = SearchCriteria()
  sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch( \
                    SearchCriteria.MatchClauseAttribute.CODE, flowCellName));
  foundSamples = search_service.searchForSamples(sc)
  if foundSamples.size() > 0:
    raise NameError('Already found a flow cell with the following name: '+ flowCellName)
  return foundSamples

# -----------------------------------------------------------------------------

def process(transaction):

  incoming = transaction.getIncoming()
  incomingPath = incoming.getAbsolutePath()

  runFolderName = incoming.getName()
  flowCellName = runFolderName.split('_')[-1][1:]

  searchFlowCell (transaction, flowCellName)

  # Parse the RunInfo.xml file
  runInfo = parseXmlFile(incomingPath + '/' + RUNINFO) 

  # Create a new Flow Cell and set the experiment
  project = transaction.getProject(FLOWCELL_PROJECT_ID)
  if project == None:
    space = transaction.getSpace(FLOWCELL_SPACE)
    if space == None:
      space = transaction.createNewSpace(FLOWCELL_SPACE, None)
    project = transaction.createNewProject(FLOWCELL_PROJECT_ID)
  expID = FLOWCELL_PROJECT_ID + '/' + datetime.now().strftime("%Y.%m")
  exp = transaction.getExperiment(expID)
  if exp == None:
    exp = transaction.createNewExperiment(expID, EXPERIMENT_TYPE_CODE)
  newFlowCell = transaction.createNewSample('/' + FLOWCELL_SPACE + '/' + flowCellName, FLOW_CELL)
  newFlowCell.setExperiment(exp)
 
  run = runInfo.getAllchildren('Run')[0].attrib
  if (extractFlowCellName(run['Id']) != flowCellName):
    raise NameError('Flowcell names do not match between directory name '+ flowCellName +
          ' and ' + RUNINFO + 'property file: ' + extractFlowCellName(run['Id']))


  # The HiSeq is providing more infos in the runParameters.xml, which we will parse here:
  runParameters = parseXmlFile(incomingPath + '/' + RUNPARAMETERS)
    
  newFlowCell.setPropertyValue("RUN_FOLDER_NAME", runFolderName)
  newFlowCell.setPropertyValue("ILLUMINA_PIPELINE_VERSION", runParameters.getXmlElement(RUNPARAMETERS_XML['RTAVERSION']))
  newFlowCell.setPropertyValue("FLOWCELLTYPE", runParameters.getXmlElement(RUNPARAMETERS_XML['FLOWCELL']))
  newFlowCell.setPropertyValue("CONTROL_LANE", runParameters.getXmlElement(RUNPARAMETERS_XML['CONTROLLANE']))
  newFlowCell.setPropertyValue("SBS_KIT", runParameters.getXmlElement(RUNPARAMETERS_XML['SBS']))
    
  read1 = runParameters.getAllchildren('Read1')
  newFlowCell.setPropertyValue("CYCLES", read1[0].text)
   
  read2 = runParameters.getAllchildren('Read2')
  if (str(read2[0].text) == '0'):
    newFlowCell.setPropertyValue("END_TYPE", "SINGLE_READ") 
  else:
    newFlowCell.setPropertyValue("END_TYPE", "PAIRED_END") 
    newFlowCell.setPropertyValue("PAIRED_END_KIT", runParameters.getXmlElement(RUNPARAMETERS_XML['PE']))
    
  indexRead1 = runParameters.getAllchildren('IndexRead1')
  newFlowCell.setPropertyValue("INDEXREAD", indexRead1[0].text) 
    
  indexRead2 = runParameters.getAllchildren('IndexRead2')
  newFlowCell.setPropertyValue("INDEXREAD2", indexRead2[0].text) 
  
  def setFcProperty(searchId, dict):
    children = runInfo.getAllchildren(searchId) 
    for element in (dict):
      if (element <> '') and (dict[element] <> ''):
        newFlowCell.setPropertyValue(element, children[0].attrib[dict[element]])

  setFcProperty('FlowcellLayout', RUNINFO_XML)

  sequencer = runInfo.getAllchildren('Instrument')
  newFlowCell.setPropertyValue("SEQUENCER", sequencer[0].text)

  newFlowCell.setPropertyValue("FLOW_CELL_SEQUENCED_ON", create_openbis_timestamp())
  try:
    maxLanes = runInfo.getAllchildren('FlowcellLayout')[0].attrib[RUNINFO_XML['LANECOUNT']]
  except:
    maxLanes = len(runInfo.getAllchildren('Tiles')[0])

  [registerFlowLane(transaction, lane, flowCellName, newFlowCell) for lane in range(1,int(maxLanes)+1)]
