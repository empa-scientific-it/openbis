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
Registers an incoming directory as a data set in openBIS. The name of the directory is used to
search for the matching sample. 

@note: 
print statements go to: <openBIS_HOME>/datastore_server/log/startup_log.txt
expected incoming Name for HiSeq runs: 110715_SN792_0054_BC035RACXX
expected incoming Name for GAII runs: 110812_6353WAAXX

@author:
Manuel Kohler
'''

import os
import shutil
import glob
import xml.etree.ElementTree as etree
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria

REGEX_RUNINFO_SAMPLE = '/Data/Status*'
REGEX_RUNINFO_REPORTS = '/Data/reports'
MARKER_STRING = '.MARKER_is_finished_'
RUNSTATISTICS_DROPBOX = 'register-runstatistics'

# -----------------------------------------------------------------------------

def extractRunData (runFolderName, transaction):
  '''
  Splits the incoming folder name and extracts infos out of it
  HiSeq example:
  ['130227', 'SN792', '0211', 'BC1RPGACXX']
  MiSeq example:
  ['130227', 'M00721', '0036', '000000000-A3BAV']
  '''
  runDataDict = {}
  date, sequencerID, runningNumber, flowCellId = runFolderName.split('_')
  runDataDict['date'] = date
  runDataDict['sequencerID'] = sequencerID
  runDataDict['runningNumber'] = runningNumber

  if not sequencerID.startswith('M'):
    runDataDict['compartment'] = flowCellId[0]
    runDataDict['flowCellId'] =  flowCellId[1:]
    dataSet = transaction.createNewDataSet("ILLUMINA_HISEQ_OUTPUT")
  else:
    runDataDict['flowCellId'] =  flowCellId
    dataSet = transaction.createNewDataSet("ILLUMINA_MISEQ_OUTPUT")
  return runDataDict, dataSet

# -----------------------------------------------------------------------------

def touch_markerfile(filename):
  try:
    # do a touch
    open(filename, 'w').close()
  except:
    print('Could not touch ' + filename)

# -----------------------------------------------------------------------------

def moveFiles(incomingPath, runDataDict, runStatisticsFolder):
  #move RunInfo into a different drop box
  runInfoSample=glob.glob(incomingPath + REGEX_RUNINFO_SAMPLE)
  runInfoReport=glob.glob(incomingPath + REGEX_RUNINFO_REPORTS)
  runInfoList = runInfoSample + runInfoReport

  newPath = runStatisticsFolder + '/' + runDataDict['flowCellId'] + '/Data/'

  os.makedirs(newPath)
  for runInfo in runInfoList:
    try:
      if os.path.isdir(runInfo):
        shutil.copytree(runInfo, newPath + os.path.basename(runInfo))
      else:
        shutil.copy2(runInfo, newPath)
    except (IOError, os.error), why:
      print (runInfo, newPath, str(why))

# -----------------------------------------------------------------------------

def getThreadProperties(transaction):

  threadPropertyDict = {}
  threadProperties = transaction.getGlobalState().getThreadParameters().getThreadProperties()

  for key in threadProperties:
    try:
      threadPropertyDict[key] = threadProperties.getProperty(key)
    except:
      pass
  return threadPropertyDict 

# -----------------------------------------------------------------------------

def process(transaction):

  threadPropertyDict = getThreadProperties(transaction)

  incomingPath = transaction.getIncoming().getPath()
  runFolderName = transaction.getIncoming().getName()
  runDataDict, dataSet = extractRunData(runFolderName, transaction)

  incomingRootDir = threadPropertyDict[u'incoming-root-dir']
  runStatisticsFolder = incomingRootDir + '/' + RUNSTATISTICS_DROPBOX

  moveFiles(incomingPath, runDataDict, runStatisticsFolder)

  touch_markerfile( runStatisticsFolder + '/' + MARKER_STRING + runDataDict['flowCellId'])

  dataSet.setMeasuredData(False)
    
  search_service = transaction.getSearchService()
  sc = SearchCriteria()
  sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch( \
  SearchCriteria.MatchClauseAttribute.CODE, runDataDict['flowCellId']));
  foundSamples = search_service.searchForSamples(sc)

  if foundSamples.size() > 0:
    # Add the incoming file into the data set
    transaction.moveFile(incomingPath, dataSet)
    dataSet.setSample(foundSamples[0])
