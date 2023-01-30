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
Parses the two Illumina provided files 'runParameters.xml' and 'RunInfo.xml'
and creates one Sample of type 'ILLUMINA_FLOW_CELL' and sets Sample properties 
from those two XML files. Additionally the number of lanes are read out and
are created as contained samples of type 'ILLUMINA_FLOW_LANE'.

@note: 
print statements go to: <openBIS_HOME>/datastore_server/log/startup_log.txt
expected incoming Name for HiSeq runs: 110715_SN792_0054_BC035RACXX
expected incoming Name for GAII runs: 110812_6353WAAXX

@author:
Manuel Kohler
'''

import os
import shutil
from time import *
from datetime import *
import xml.etree.ElementTree as etree
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria

IS_HISEQ_RUN=False
RUNPARAMETERS = 'runParameters.xml'
RUNINFO = 'RunInfo.xml'
FLOWCELL_SPACE='BSSE_FLOWCELLS'
FLOWCELL_PROJECT='FLOWCELLS'
EXPERIMENT_TYPE_CODE='HT_SEQUENCING'

FLOWCELL_PROJECT_ID = "/%(FLOWCELL_SPACE)s/%(FLOWCELL_PROJECT)s" % vars()

# Mapping between XML file naming and used in here
RUNPARAMETERS_XML = {'FLOWCELL':'Flowcell', 'RTAVERSION':'RTAVersion',
  'CONTROLLANE':'ControlLane', 'SBS':'Sbs', 'INDEX':'Index',
  'CYCLES_REQUESTED_BY_CUSTOMER':'Read1', 'PE':'Pe'}
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

def registerFlowLane(transaction, a_lane, name, newFlowCell):
  '''
  Registers a new Flow lane 
  '''
  newFlowLane = transaction.createNewSample('/' + FLOWCELL_SPACE + '/' + name + ':' + str(a_lane), "ILLUMINA_FLOW_LANE")
  newFlowLane.setContainer(newFlowCell)

# -----------------------------------------------------------------------------

def process(transaction):

  incoming = transaction.getIncoming()
  incomingPath = incoming.getAbsolutePath()

  # Get the incoming name 
  name = incoming.getName()

  split=name.split("_")
  if (len(split) == 4):
    IS_HISEQ_RUN=True
  if (len(split) == 2):
    pass

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
  project = transaction.getProject(FLOWCELL_PROJECT_ID)
  if project == None:
    space = transaction.getSpace(FLOWCELL_SPACE)
    if space == None:
      space = transaction.createNewSpace(FLOWCELL_SPACE, None)
      space.setDescription("A test space")
    project = transaction.createNewProject(FLOWCELL_PROJECT_ID)
    project.setDescription("A demo project")
  expID = FLOWCELL_PROJECT_ID + '/' + datetime.now().strftime("%Y.%m")
  exp = transaction.getExperiment(expID)
  if exp == None:
    exp = transaction.createNewExperiment(expID, EXPERIMENT_TYPE_CODE)
  newFlowCell = transaction.createNewSample('/' + FLOWCELL_SPACE + '/' + name, "ILLUMINA_FLOW_CELL")
  newFlowCell.setExperiment(exp)
 
  if IS_HISEQ_RUN:
    run = runInfo.getAllchildren('Run')[0].attrib
    if (run['Id'] != name):
      raise NameError('Flowcell names do not match between directory name '+ name +
            ' and ' + RUNINFO + 'property file: ' + run['Id'])

    # The HiSeq is providing more infos, which we will parse here:
    runParameters = parseXmlFile(incomingPath + '/' + RUNPARAMETERS)
    
    newFlowCell.setPropertyValue("ILLUMINA_PIPELINE_VERSION", runParameters.getXmlElement(RUNPARAMETERS_XML['RTAVERSION']))
    newFlowCell.setPropertyValue("FLOWCELLTYPE", runParameters.getXmlElement(RUNPARAMETERS_XML['FLOWCELL']))
    newFlowCell.setPropertyValue("CONTROL_LANE", runParameters.getXmlElement(RUNPARAMETERS_XML['CONTROLLANE']))
    newFlowCell.setPropertyValue("SBS_KIT", runParameters.getXmlElement(RUNPARAMETERS_XML['SBS']))
    
    read1 = runParameters.getAllchildren('Read1')
    newFlowCell.setPropertyValue("CYCLES_REQUESTED_BY_CUSTOMER", read1[0].text)
    
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
  if IS_HISEQ_RUN:
    maxLanes = runInfo.getAllchildren('FlowcellLayout')[0].attrib[RUNINFO_XML['LANECOUNT']]
  else:
    maxLanes = len(runInfo.getAllchildren('Tiles')[0])

  [registerFlowLane(transaction, lane, name, newFlowCell) for lane in range(1,int(maxLanes)+1)]

  shutil.rmtree(incomingPath)
