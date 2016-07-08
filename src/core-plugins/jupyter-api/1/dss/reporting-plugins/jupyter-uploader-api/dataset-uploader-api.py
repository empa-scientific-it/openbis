#
# Copyright 2014 ETH Zuerich, Scientific IT Services
#
# Licensed under the Apache License, Version 2.0 (the "License")
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# IDataSetRegistrationTransactionV2 Class
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria import MatchClause, SearchOperator, MatchClauseAttribute
from ch.systemsx.cisd.openbis.dss.generic.shared import ServiceProvider

from org.apache.commons.io import IOUtils
from java.io import File
from java.io import FileOutputStream
from java.lang import System
#from net.lingala.zip4j.core import ZipFile
from ch.systemsx.cisd.common.exceptions import UserFailureException

import time
import subprocess
import os
import re
import sys
import shutil
import errno



def getSampleByIdentifier(transaction, identifier):
    space = identifier.split("/")[1]
    code = identifier.split("/")[2]

    criteria = SearchCriteria()
    criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.SPACE, space))
    criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, code))
    criteria.setOperator(SearchOperator.MATCH_ALL_CLAUSES)

    search_service = transaction.getSearchService()
    found = list(search_service.searchForSamples(criteria))
    if len(found) == 1:
        return transaction.makeSampleMutable(found[0]);
    else:
        return None


def get_dataset_for_name(transaction, dataset_name):

    search_service = transaction.getSearchService()
    criteria = SearchCriteria()
    criteria.addMatchClause(MatchClause.createPropertyMatch('NAME', dataset_name))

    found = list(search_service.searchForDataSets(criteria))
    if len(found) == 1:
        return found[0]
    else:
        return None


def get_dataset_for_permid(transaction, permid):

    search_service = transaction.getSearchService()
    criteria = SearchCriteria()
    criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, permid));

    found = list(search_service.searchForDataSets(criteria))
    if len(found) == 1:
        return found[0]
    else:
        return None


def process(transaction, parameters, tableBuilder):
    ''' 
    This method is called from openBIS DSS.
    The transaction object has a number of methods described in ...
    The parameters are passed with the createReportFromAggregationService method
    and need to be accessed like this:
       parameters.get('my_param')
    tableBuilder is needed to create an appropiate return message.
    A number of magic variables are present, described in PluginScriptRunnerFactory:
    - userSessionToken : the Session Token used by every call
    - userId           : the username
    - searchService    :
    - searchServiceUnfiltered :
    - queryService     :
    - mailService      :
    - authorizationService :
    - contentProvider  :
    - contentProviderUnfiltered 

    '''
    transaction.setUserId(userId)
    print(dir())
    # check for mandatory parameters
    for param in ['sample']:
        if parameters.get(param) is None:
            raise UserFailureException("mandatory parameter " + param + " is missing")

    # all print statements are written to openbis/servers/datastore_server/log/startup_log.txt
    print('userSessionToken: ' + userSessionToken)

    # get mandatory sample to connect the container to
    sampleIdentifier = parameters.get("sample").get("identifier")
    print('looking for sample with identifier: ' + sampleIdentifier)
    if sampleIdentifier is None:
        raise UserFailureException('mandatory parameter sample["identifier"] is missing')
    sample = getSampleByIdentifier(transaction, sampleIdentifier)
    if sample == None: 
        raise Exception("no sample found with this identifier: " + sampleIdentifier)

    everything_ok = True

    dataset_codes= []
    if parameters.get("dataSets") is not None:
        for ds in parameters.get("dataSets"):
            dataset_code = register_dataset(
                transaction, 
                ds.get("dataSetType"),
                sample, 
                ds.get("properties"),
                ds.get("sessionWorkspaceFolder"),
                ds.get("fileNames")
            )
            dataset_codes.append(dataset_code)

#    if parameters.get("result").get("fileNames") is not None:
#        dataset_code = register_dataset(
#            transaction, 
#            "JUPYTER_RESULT",
#            sample, 
#            'container_permId', 
#            
#            parameters.get("result").get("fileNames")
#        )
#        dataset_codes.append(dataset_code)

#    if parameters.get("containers") is not None:
#        for container in parameters.get("containers"):
#            container_code = register_container(
#                transaction,
#                container.get("dataSetType"),
#                sample,
#                container.get("properties"),
#                dataset_codes
#            )


   # 
    # create the dataset
    if everything_ok:
        # Success message
        tableBuilder.addHeader("STATUS")
        tableBuilder.addHeader("MESSAGE")
        tableBuilder.addHeader("RESULT")
        row = tableBuilder.addRow()
        row.setCell("STATUS","OK")
        row.setCell("MESSAGE", "Dataset registration successful")
        row.setCell("RESULT", None)

    else:
        # Error message
        tableBuilder.addHeader("STATUS")
        tableBuilder.addHeader("MESSAGE")
        row = tableBuilder.addRow()
        row.setCell("STATUS","FAIL")
        row.setCell("MESSAGE", "Dataset registration failed")


def register_container(transaction, dataset_type, sample, properties, contained_dataset_codes ):

    # make sure container dataset doesn't exist yet
    container = get_dataset_for_name(transaction, properties.get("name"))

    if container is None:
        print("creating new JUPYTER_CONTAINER dataset...")
        # Create new container (a dataset of type "JUPYTER_CONTAINER")
        container = transaction.createNewDataSet(dataset_type)
        container.setSample(sample)
        container.setRegistrator(userId)

    for key in properties.keySet():
        propertyValue = unicode(properties[key])
        print("container: setting "+key+"="+propertyValue)

        if propertyValue == "":
            propertyValue = None
        container.setPropertyValue(key,propertyValue)
    
    container.setContainedDataSetCodes(contained_dataset_codes)
    print("JUPYTER_CONTAINER permId: " + container.getDataSetCode())

    return container


def register_dataset(transaction, dataset_type, sample, properties, ws_folder, file_names):
    """ creates a new dataset of type JUPYTER_RESULT.
    the parent dataset is the JUPYTER_CONTAINER we just created
    - the result files are copied from the session workspace
      to a temp dir close to the DSS: prepareFilesForRegistration()
    - from there, the files are moved to the DSS: transaction.moveFile()
    - finally, the remaining files are deleted from the session workspace
    """
    print("dataset_type = " +dataset_type)
    
    print("creating " + dataset_type + " dataset...")
    dataset = transaction.createNewDataSet(dataset_type)
    dataset.setSample(sample)

    for key in properties.keySet():
        propertyValue = unicode(properties[key]);
        print("setting propertyValue: "+key + " = " + propertyValue)
        if propertyValue == "":
            propertyValue = None;
        dataset.setPropertyValue(key,propertyValue);

    print("JUPYTER_RESULT permId: " + dataset.getDataSetCode())

    
    # create temporary folder in incoming-dir ( openbis/servers/datastore_server/data/incoming )
    threadProperties = getThreadProperties(transaction)
    print(threadProperties)
    dst_dir =  os.path.join( threadProperties[u'incoming-dir'], str(time.time()) )
    print("incoming folder: " + dst_dir)

    dss_service = ServiceProvider.getDssServiceRpcGeneric().getService()

    for file_name in file_names:
        print("copying file: " + file_name)
        file_path = os.path.join(dst_dir, file_name)
        print("to: "+file_path)

        # ensure that all necessary folders exist
        try:
            os.makedirs(os.path.dirname(file_path))
            print("subdir created: " + os.path.dirname(file_path))
        except:
            pass


        # create input and output stream
        inputStream = dss_service.getFileFromSessionWorkspace(userSessionToken, file_name)
        outputStream = FileOutputStream(File(file_path))
        IOUtils.copyLarge(inputStream, outputStream)
        IOUtils.closeQuietly(inputStream)
        IOUtils.closeQuietly(outputStream)

    dst_dir2 = os.path.join(dst_dir, ws_folder)
    transaction.moveFile(File(dst_dir2).getAbsolutePath(), dataset);
#    temp_dir = prepareFilesForRegistration(transaction, file_names)

    # ...and delete all files from the session workspace
    # TODO: delete it later
    #dss_service = ServiceProvider.getDssServiceRpcGeneric().getService()
    #for file_name in file_names:
    #    file_path = os.path.join(temp_dir, file_name)
    #    dss_service.deleteSessionWorkspaceFile(userSessionToken, file_name)

    return dataset.getDataSetCode()


def getThreadProperties(transaction):
  threadPropertyDict = {}
  threadProperties = transaction.getGlobalState().getThreadParameters().getThreadProperties()
  for key in threadProperties:
    try:
      threadPropertyDict[key] = threadProperties.getProperty(key)
    except:
      pass
  return threadPropertyDict



def registerNotebook(transaction, sample, container, parameters):
    """ creates a new dataset of type JUPYTER_NOTEBOOK.
    the parent dataset is the container.
    """
    dataSet = transaction.createNewDataSet("JUPYTER_NOTEBOOK")
    dataSet.setSample(sample)
    parent_codes = [container.getDataSetCode()]
    dataSet.setParentDatasets(parent_codes)

    files = parameters.get("notebook").get("fileNames")
    for file in files:
        pass

    return dataSet


def getThreadProperties(transaction):
  threadPropertyDict = {}
  threadProperties = transaction.getGlobalState().getThreadParameters().getThreadProperties()
  for key in threadProperties:
    try:
      threadPropertyDict[key] = threadProperties.getProperty(key)
    except:
      pass
  return threadPropertyDict

