import os
import shutil
from time import *
from datetime import *
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria

MarkerGAComplete = 'RTAComplete.txt'  
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

def process(transaction):

  incomingPath = transaction.getIncoming().getAbsolutePath()
  # Get the incoming name 
  name = transaction.getIncoming().getName()

  split=name.split("_")
  if (len(split) == 4):
    IS_HISEQ_RUN=True
    Markerfile = incomingPath + "/" + MarkerHiSeqComplete
  if (len(split) == 2):
    Markerfile = incomingPath + "/" + MarkerGAComplete 


  # Search for the sample and check if there is already sample with this name
  search_service = transaction.getSearchService()
  sc = SearchCriteria()
  sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, name));
  foundSamples = search_service.searchForSamples(sc)

  if foundSamples.size() > 0:
      sa = transaction.getSampleForUpdate(foundSamples[0].getSampleIdentifier())
      sa.setPropertyValue("SEQUENCER_FINISHED", createOpenbisTimeStamp(Markerfile))

  shutil.rmtree(incomingPath)
