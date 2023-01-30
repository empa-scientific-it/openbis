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
Reads out the time stamp of the file 'RTAComplete.txt' and sets the 
property for a finished sequencer called 'SEQUENCER_FINISHED' to the
time stamp of this file.

@note:
print statements go to: <openBIS_HOME>/datastore_server/log/startup_log.txt
expected incoming Name for HiSeq runs: 110715_SN792_0054_BC035RACXX

structure:
110715_SN792_0054_BC035RACXX/
        RTAComplete.txt

@author:
Manuel Kohler
'''

import os
import shutil
from time import *
from datetime import *
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria

MarkerHiSeqComplete = 'RTAComplete.txt'

def createOpenbisTimeStamp(file):
  '''
  Creates a openBIS compatible time stamp of a file time stamp
  '''
  mtime = os.path.getmtime(file)
  lt = localtime(mtime)
  tz = localtime().tm_hour - gmtime().tm_hour
  return (strftime("%Y-%m-%d %H:%M:%S GMT" + "%+.2d" % tz + ":00", lt))

# -----------------------------------------------------------------------------

def extractFlowCellName (runFolderName):
  return runFolderName.split('_')[-1][1:]

# -----------------------------------------------------------------------------

def process(transaction):

  incomingPath = transaction.getIncoming().getAbsolutePath()
  # Get the incoming name 
  runFolderName = transaction.getIncoming().getName()
  flowCellName = extractFlowCellName(runFolderName)

  Markerfile = incomingPath + "/" + MarkerHiSeqComplete

  # Search for the sample and check if there is already sample with this name
  search_service = transaction.getSearchService()
  sc = SearchCriteria()
  sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, flowCellName));
  foundSamples = search_service.searchForSamples(sc)

  if foundSamples.size() > 0:
      sa = transaction.getSampleForUpdate(foundSamples[0].getSampleIdentifier())
      sa.setPropertyValue("SEQUENCER_FINISHED", createOpenbisTimeStamp(Markerfile))
