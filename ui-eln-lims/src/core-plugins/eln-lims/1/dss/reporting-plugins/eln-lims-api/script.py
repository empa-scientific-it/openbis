#
# Copyright 2014 ETH Zuerich, Scientific IT Services
#
# Licensed under the Apache License, Version 2.0 (the "License");
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
from ch.ethz.sis.openbis.generic.server.dss.plugins import DataSetRegistrationCleanUpTask
from ch.systemsx.cisd.openbis.dss.generic.shared import ServiceProvider
from ch.systemsx.cisd.openbis.dss.client.api.v1 import DssComponentFactory
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria import MatchClause, SearchOperator, MatchClauseAttribute
from ch.systemsx.cisd.openbis.generic.shared.basic.dto import DataTypeCode;

from java.util import ArrayList
from java.util import Date;
from java.text import SimpleDateFormat;

from ch.systemsx.cisd.common.spring import HttpInvokerUtils;
from org.apache.commons.io import IOUtils
from java.io import File
from java.io import FileOutputStream
from java.lang import System
from net.lingala.zip4j.core import ZipFile
from ch.systemsx.cisd.common.exceptions import UserFailureException

import json
import time
import subprocess
import os.path
import re

from java.io import StringWriter
from org.htmlcleaner import HtmlCleaner
from org.htmlcleaner import SimpleHtmlSerializer
from org.htmlcleaner import CleanerProperties

#AS API
from ch.systemsx.cisd.openbis.generic.shared.api.v1 import IGeneralInformationService;

#For Screeening API
from ch.systemsx.cisd.openbis.common.api.client import ServiceFinder;
from java.util import Arrays;

from ch.ethz.sis.openbis.generic.asapi.v3 import IApplicationServerApi
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions import SampleFetchOptions;
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search import SampleSearchCriteria;
from ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search import SearchResult;
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id import SampleIdentifier;
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id import SamplePermId
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id import ExperimentIdentifier;
from ch.systemsx.cisd.openbis.dss.generic.shared import ServiceProvider
from ch.systemsx.cisd.openbis.dss.generic.server import DataStoreServer
from ch.systemsx.cisd.common.shared.basic.string import StringUtils 
#from ch.systemsx.cisd.common.ssl import SslCertificateHelper;
from ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions import PropertyTypeFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id import EntityTypePermId
from ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype import EntityKind
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions import ExperimentTypeFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions import SampleTypeFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions import DataSetTypeFetchOptions

#Plasmapper server used
OPENBISURL = DataStoreServer.getConfigParameters().getServerURL() + "/openbis/openbis"
def getConfigParameterAsString(propertyKey):
	properties = DataStoreServer.getConfigParameters().getProperties();
	property = properties.getProperty(propertyKey);
	if StringUtils.isBlank(property):
		return None;
	else:
		return property;

def getDirectLinkURL():
	#CIFS
	cifsServerEnable = getConfigParameterAsString("cifs.server.enable");
	cifsServerPort = getConfigParameterAsString("cifs.server.smb-port");
	
	#SFTP
	sftpServerEnable = getConfigParameterAsString("ftp.server.enable");
	sftpPort = getConfigParameterAsString("ftp.server.sftp-port");
	
	cifsConfig = None;
	if (cifsServerEnable == "true") and (cifsServerPort is not None):
		cifsConfig = {
					"port" : cifsServerPort,
					"UNCsuffix" : "STORE/"
		}
	
	sftpConfig = None;
	if (sftpServerEnable == "true") and (sftpPort is not None):
		sftpConfig = {
					"port" : sftpPort,
					"UNCsuffix" : ""
		}

	return getJsonForData({
							"cifs" : cifsConfig,
							"sftp" : sftpConfig
						});
	
def createSampleIdentifier(sampleSpace, sampleProject, sampleCode, projectSamplesEnabled):
	template = '/%(sampleSpace)s/%(sampleProject)s/%(sampleCode)s' if projectSamplesEnabled and (sampleProject is not None)  else '/%(sampleSpace)s/%(sampleCode)s'
	return template % vars()
	
def isSampleTypeAvailable(sampleTypes, sampleTypeCode):
	for sampleType in sampleTypes:
		if sampleType.getCode() == sampleTypeCode:
			return True
	return False

def getSampleTypes(tr, parameters):
	sessionToken = parameters.get("sessionToken");
	servFinder = ServiceFinder("openbis", IGeneralInformationService.SERVICE_URL);
	infService = servFinder.createService(IGeneralInformationService, OPENBISURL);
	types = infService.listSampleTypes(sessionToken);
	return types

def getProperties(tr, parameters, entityTypeCode, entityKind):
	v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi, OPENBISURL + IApplicationServerApi.SERVICE_URL, 30 * 1000);
	sessionToken = parameters.get("sessionToken");
	propertyTypeFetchOptions = PropertyTypeFetchOptions();
	typeId = EntityTypePermId(entityTypeCode, entityKind);
	entityType = None
	if entityKind == EntityKind.EXPERIMENT:
		fetchOptions = ExperimentTypeFetchOptions();
		fetchOptions.withPropertyAssignments().withPropertyType();
		entityType = v3.getExperimentTypes(sessionToken, [typeId], fetchOptions).get(typeId);
	if entityKind == EntityKind.SAMPLE:
		fetchOptions = SampleTypeFetchOptions();
		fetchOptions.withPropertyAssignments().withPropertyType();
		entityType = v3.getSampleTypes(sessionToken, [typeId], fetchOptions).get(typeId);
	if entityKind == EntityKind.DATA_SET:
		fetchOptions = DataSetTypeFetchOptions();
		fetchOptions.withPropertyAssignments().withPropertyType();
		entityType = v3.getDataSetTypes(sessionToken, [typeId], fetchOptions).get(typeId);
	entityTypeProperties = {};
	for propertyAssignment in entityType.getPropertyAssignments():
		propertyType = propertyAssignment.getPropertyType();
		entityTypeProperties[propertyType.getCode()] = propertyType;
	return entityTypeProperties;

def isPropertyRichText(properties, propertyCode):
	if propertyCode in properties:
		metaData = properties[propertyCode].getMetaData();
		return ("custom_widget" in metaData) and metaData["custom_widget"] == "Word Processor";
	return False;

def updateIfIsPropertyRichText(properties, propertyCode, propertyValue):
	if isPropertyRichText(properties, propertyCode):
		if propertyValue is not None:
			if re.search( r'src=\"data:image\/([a-zA-Z]*);base64,([^\"]*)\"', propertyValue) is not None:
				raise UserFailureException("Base64 Image detected, please upload images using the upload form.")
			cleanerProperties = CleanerProperties();
			cleanerProperties.setPruneTags("meta, link, script");
			cleaner = HtmlCleaner(cleanerProperties);
			htmlSerializer = SimpleHtmlSerializer(cleanerProperties);
			propertytagNode = cleaner.clean(propertyValue);
			return htmlSerializer.getAsString(propertytagNode);
	return propertyValue;

def getPropertyValue(propertiesInfo, metadata, key):
	propertyValue = metadata[key];
	if propertyValue != None:
		propertyValue = unicode(propertyValue);
	if propertyValue == "":
		propertyValue = None;
	else:
		propertyValue = updateIfIsPropertyRichText(propertiesInfo, key, propertyValue);
	return propertyValue;

def getProjectCodeFromSampleIdentifier(sampleIdentifier):
	projectCode = None;
	sampleIdentifierParts = sampleIdentifier.split("/");
	if len(sampleIdentifierParts) == 4:
		projectCode = sampleIdentifierParts[2];
	return projectCode;

def getSampleByIdentifierForUpdate(tr, identifier):
	space = identifier.split("/")[1];
	projectCode = getProjectCodeFromSampleIdentifier(identifier);
	code = identifier.split("/")[-1];
	
	criteria = SearchCriteria();
	criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.SPACE, space));
	criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, code));
	criteria.setOperator(SearchOperator.MATCH_ALL_CLAUSES);
	
   	searchService = tr.getSearchService();
   	found = list(searchService.searchForSamples(criteria));
   	
   	# The search service can return more than one sample with project samples enabled, projects need to be filtered manually
   	for sample in found:
   		foundProjectCode = getProjectCodeFromSampleIdentifier(sample.getSampleIdentifier());
   		if foundProjectCode == projectCode:
   			return tr.makeSampleMutable(sample);
   	
def username(sessiontoken):
    m = re.compile('(.*)-[^-]*').match(sessiontoken)
    if m:
        return m.group(1)

def process(tr, parameters, tableBuilder):
	method = parameters.get("method");
	
	isOk = False;
	result = None;
	# Obtain the user using the dropbox
	sessionToken = parameters.get("sessionToken"); #String
	sessionId = username(sessionToken); #String
	tr.setUserId(userId);
	v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi, OPENBISURL + IApplicationServerApi.SERVICE_URL, 30 * 1000);
	projectSamplesEnabled = v3.getServerInformation(sessionToken)['project-samples-enabled'] == 'true'
	
	if method == "init":
		isOk = True;
	if method == "isFileAuthUser":
		result = isFileAuthUser(tr, parameters, tableBuilder);
		isOk = True;
	if method == "searchSamples":
		result = searchSamples(tr, v3, parameters, tableBuilder, sessionId);
		isOk = True;
	if method == "registerUserPassword":
		isOk = registerUserPassword(tr, parameters, tableBuilder);
	if method == "updateUserInformation":
		isOk = updateUserInformation(tr, parameters, tableBuilder);
	if method == "getDirectLinkURL":
		result = getDirectLinkURL();
		isOk = True;
	if method == "copyAndLinkAsParent":
		isOk = copyAndLinkAsParent(tr, parameters, tableBuilder);
	if method == "batchOperation":
		isOk = batchOperation(tr, projectSamplesEnabled, parameters, tableBuilder);
	if method == "listAvailableFeatures":
		result = listAvailableFeatures(tr, parameters, tableBuilder);
		isOk = True;
	if method == "getDiskSpace":
		result = getDiskSpace(tr, parameters, tableBuilder)
		isOk = True
	if method == "insertProject":
		isOk = insertUpdateProject(tr, parameters, tableBuilder);
	if method == "updateProject":
		isOk = insertUpdateProject(tr, parameters, tableBuilder);
	if method == "copySample":
		result = copySample(tr, projectSamplesEnabled, parameters, tableBuilder);
		isOk = True;
	if method == "moveSample":
		isOk = moveSample(tr, parameters, tableBuilder);
	##
	## Insert/Update Entities with properties
	##
	if method == "insertSample":
		result = insertUpdateSample(tr, projectSamplesEnabled, parameters, tableBuilder);
		isOk = True;
	if method == "updateSample":
		result = insertUpdateSample(tr, projectSamplesEnabled, parameters, tableBuilder);
		isOk = True;
	if method == "insertExperiment":
		isOk = insertUpdateExperiment(tr, parameters, tableBuilder);
	if method == "updateExperiment":
		isOk = insertUpdateExperiment(tr, parameters, tableBuilder);
	if method == "dropDataSet":
		isOk = dropDataSet(tr, parameters, tableBuilder);
	if method == "insertDataSet":
		isOk = insertDataSet(tr, parameters, tableBuilder);
	if method == "updateDataSet":
		isOk = updateDataSet(tr, parameters, tableBuilder);

	##
	## Deprecated
	##
	if method == "getFeaturesFromFeatureVector":
		result = getFeaturesFromFeatureVector(tr, parameters, tableBuilder);
		isOk = True;
	if method == "listFeatureVectorDatasetsPermIds":
		result = listFeatureVectorDatasetsPermIds(tr, parameters, tableBuilder);
		isOk = True;

	if isOk:
		tableBuilder.addHeader("STATUS");
		tableBuilder.addHeader("MESSAGE");
		tableBuilder.addHeader("RESULT");
		row = tableBuilder.addRow();
		row.setCell("STATUS","OK");
		row.setCell("MESSAGE", "Operation Successful");
		row.setCell("RESULT", result);
	else :
		tableBuilder.addHeader("STATUS");
		tableBuilder.addHeader("MESSAGE");
		row = tableBuilder.addRow();
		row.setCell("STATUS","FAIL");
		row.setCell("MESSAGE", "Operation Failed");

def listFeatureVectorDatasets(sessionToken, samplePlatePermId):
	from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1 import IScreeningApiServer;
	from ch.systemsx.cisd.openbis.dss.screening.shared.api.v1 import IDssServiceRpcScreening;
	from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import PlateIdentifier;
	from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import FeatureVectorDatasetReference;
	
	screeningFinder = ServiceFinder("openbis", IScreeningApiServer.SERVICE_URL);
	screeningServiceAS = screeningFinder.createService(IScreeningApiServer, OPENBISURL);
	
	plateIdentifier = PlateIdentifier.createFromPermId(samplePlatePermId);
	featureVectorDatasets = screeningServiceAS.listFeatureVectorDatasets(sessionToken, Arrays.asList(plateIdentifier));
	return featureVectorDatasets;

def getJsonForData(data):
	jsonValue = ServiceProvider.getObjectMapperV3().writeValueAsString(data);
	return jsonValue;

def listFeatureVectorDatasetsPermIds(tr, parameters, tableBuilder):
	from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1 import IScreeningApiServer;
	from ch.systemsx.cisd.openbis.dss.screening.shared.api.v1 import IDssServiceRpcScreening;
	from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import PlateIdentifier;
	from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import FeatureVectorDatasetReference;
	
	sessionToken = parameters.get("sessionToken");
	samplePlatePermId = parameters.get("samplePlatePermId");

	featureVectorDatasets = listFeatureVectorDatasets(sessionToken, samplePlatePermId);
	featureVectorDatasetCodes = [];
	for featureVectorDataset in featureVectorDatasets:
		featureVectorDatasetCodes.append(featureVectorDataset.getDatasetCode());
	
	return getJsonForData(featureVectorDatasetCodes);

def listAvailableFeatures(tr, parameters, tableBuilder):
	from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1 import IScreeningApiServer;
	from ch.systemsx.cisd.openbis.dss.screening.shared.api.v1 import IDssServiceRpcScreening;
	from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import PlateIdentifier;
	from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import FeatureVectorDatasetReference;
		
	sessionToken = parameters.get("sessionToken");
	samplePlatePermId = parameters.get("samplePlatePermId");
	featureVectorDatasetPermId = parameters.get("featureVectorDatasetPermId");
	
	featureVectorDataset = None;
	featureVectorDatasets = listFeatureVectorDatasets(sessionToken, samplePlatePermId);
	for featureVectorDataset in featureVectorDatasets:
		if featureVectorDataset.getDatasetCode() == featureVectorDatasetPermId:
			featureVectorDataset = featureVectorDataset;
	
	from ch.systemsx.cisd.openbis.dss.generic.server import DssScreeningApplicationContext
	screeningServiceDSS = DssScreeningApplicationContext.getInstance().getBean("data-store-rpc-service-screening-logic-target")

	featureInformationList = screeningServiceDSS.listAvailableFeatures(sessionToken, [featureVectorDataset]);
	features = {};
	for featureInformation in featureInformationList:
		features[featureInformation.getCode()] = featureInformation.getLabel();
	
	return getJsonForData(features);

def getFeaturesFromFeatureVector(tr, parameters, tableBuilder):
	from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1 import IScreeningApiServer;
	from ch.systemsx.cisd.openbis.dss.screening.shared.api.v1 import IDssServiceRpcScreening;
	from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import PlateIdentifier;
	from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import FeatureVectorDatasetReference;
	
	sessionToken = parameters.get("sessionToken");
	samplePlatePermId = parameters.get("samplePlatePermId");
	featureVectorDatasetPermId = parameters.get("featureVectorDatasetPermId");
	featuresCodesFromFeatureVector = parameters.get("featuresCodesFromFeatureVector");
	
	featureVectorDataset = None;
	featureVectorDatasets = listFeatureVectorDatasets(sessionToken, samplePlatePermId);
	for featureVectorDatasetAux in featureVectorDatasets:
		if featureVectorDatasetAux.getDatasetCode() == featureVectorDatasetPermId:
			featureVectorDataset = featureVectorDatasetAux;
	
	from ch.systemsx.cisd.openbis.dss.generic.server import DssScreeningApplicationContext
	screeningServiceDSS = DssScreeningApplicationContext.getInstance().getBean("data-store-rpc-service-screening-logic-target")
	
	featuresFromFeatureVector = screeningServiceDSS.loadFeatures(sessionToken, [featureVectorDataset], featuresCodesFromFeatureVector);
	return getJsonForData(featuresFromFeatureVector);

def getDiskSpace(tr, parameters, tableBuilder):
	storerootDir = getConfigParameterAsString("storeroot-dir")
	diskSpaceValues = []
	diskSpaceValues.append(getDiskSpaceForDirectory(storerootDir))
	# find symlinks to different drives
	findLinks = subprocess.check_output(["find", storerootDir, "-type", "l", "-maxdepth", "1"])
	for symlink in findLinks.splitlines():
		linkPath = os.path.realpath(symlink)
		if os.path.exists(linkPath):
			diskSpaceForDir = getDiskSpaceForDirectory(linkPath)
			if diskSpaceForDir not in diskSpaceValues:
				diskSpaceValues.append(diskSpaceForDir)
	# find mountpoints within the storeroot-dir
	findDirs = subprocess.check_output(["find", storerootDir, "-type", "d", "-maxdepth", "1"])
	for dir in findDirs.splitlines():
		diskSpaceForDir = getDiskSpaceForDirectory(dir)
		if diskSpaceForDir not in diskSpaceValues:
			diskSpaceValues.append(diskSpaceForDir)
	return getJsonForData(diskSpaceValues)

def getDiskSpaceForDirectory(dir):
	df = subprocess.check_output(["df", '-h', dir])
	return extractDiskSpaceValues(df)

def extractDiskSpaceValues(df):
	values = {}
	dfLines = df.splitlines()
	#reverse because the first column (which we don't need) can contain spaces
	headerSplitReversed = dfLines[0].replace("Mounted on", "Mounted_on").split()[::-1]
	valuesSplitReversed = dfLines[1].split()[::-1]
	for i, column in enumerate(headerSplitReversed):
		if column in ["Mounted_on", "Size", "Used", "Avail"]:
			values[column] = valuesSplitReversed[i]
		elif column in ["Capacity", "Use%"]: # Mac OS X or GNU/Linux
			values["UsedPercentage"] = valuesSplitReversed[i]
	return values

def isFileAuthUser(tr, parameters, tableBuilder):
	userId = parameters.get("userId"); #String
	path = '../openBIS-server/jetty/bin/passwd.sh';
	if os.path.isfile(path):
		isFileAuthUser = False;
		try:
			result = subprocess.check_output([path, 'show', userId]) #Checks if the user is available on the file
			resultLines = result.split("\n")
			isFileAuthUser = (len(resultLines) == 3) and str(resultLines[1]).startswith(str(userId)) #String conversions to handle unicode
		except:
			pass
		return isFileAuthUser
	else:
		return False;

def registerUserPassword(tr, parameters, tableBuilder):
	userId = parameters.get("userId"); #String
	password = parameters.get("password"); #String
	path = '../openBIS-server/jetty/bin/passwd.sh';
	if os.path.isfile(path):
		subprocess.call([path, 'add', userId, '-p', password]) #Adds the user, if the user exists, will fail
		subprocess.call([path, 'change', userId, '-p', password]) #Changes the user pass, works always
		return True;
	else:
		return False;

def updateUserInformation(tr, parameters, tableBuilder):
	userId = parameters.get("userId"); #String
	firstName = parameters.get("firstName"); #String
	lastName = parameters.get("lastName"); #String
	email = parameters.get("email"); #String
	path = '../openBIS-server/jetty/bin/passwd.sh';
	if os.path.isfile(path):
		subprocess.call([path, 'change', userId, '-f', firstName, '-l', lastName, '-e', email]) #Changes the user info, fails silently if the user doesnt exist
		return True;
	else:
		return False;
	
def getThreadProperties(transaction):
  threadPropertyDict = {}
  threadProperties = transaction.getGlobalState().getThreadParameters().getThreadProperties()
  for key in threadProperties:
    try:
      threadPropertyDict[key] = threadProperties.getProperty(key)
    except:
      pass
  return threadPropertyDict
  
def insertUpdateProject(tr, parameters, tableBuilder):
	method = parameters.get("method"); #String
	projectIdentifier = parameters.get("projectIdentifier"); #String
	projectDescription = parameters.get("projectDescription"); #String
	
	project = None;
	if method == "insertProject":
		project = tr.createNewProject(projectIdentifier);
	if method == "updateProject":
		project = tr.getProjectForUpdate(projectIdentifier);
	
	project.setDescription(projectDescription);
	
	#Return from the call
	return True;
	
def updateDataSet(tr, parameters, tableBuilder):
	dataSetCode = parameters.get("dataSetCode"); #String
	dataSetParents = parameters.get("dataSetParents"); #List<String>
	metadata = parameters.get("metadata"); #java.util.LinkedHashMap<String, String> where the key is the name
	dataSet = tr.getDataSetForUpdate(dataSetCode);
	dataSet.setParentDatasets(dataSetParents);
	properties = getProperties(tr, parameters, dataSet.getDataSetType(), EntityKind.DATA_SET);
	#Hack - Fix Sample Lost bug from openBIS, remove when SSDM-1979 is fix
	#Found in S211: In new openBIS versions if you set the already existing sample when doing a dataset update is deleted
	#sampleIdentifier = parameters.get("sampleIdentifier"); #String
	#dataSetSample = getSampleByIdentifierForUpdate(tr, sampleIdentifier);
	#dataSet.setSample(dataSetSample);
	#Assign Data Set properties
	for key in metadata.keySet():
		propertyValue = getPropertyValue(properties, metadata, key);
		dataSet.setPropertyValue(key,propertyValue);
	
	#Return from the call
	return True;

def dropDataSet(tr, parameters, tableBuilder):
	sessionToken = parameters.get("sessionToken")
	dropBoxName = parameters.get("dropBoxName")
	fileName = parameters.get("fileName")
	ServiceProvider.getDssServiceRpcGeneric().getService().putDataSet(sessionToken, dropBoxName, fileName)
	return True;

def insertDataSet(tr, parameters, tableBuilder):
	#Mandatory parameters
	sampleIdentifier = parameters.get("sampleIdentifier"); #String
	experimentIdentifier = parameters.get("experimentIdentifier"); #String
	
	dataSetType = parameters.get("dataSetType"); #String
	folderName = parameters.get("folderName"); #String
	fileNames = parameters.get("filenames"); #List<String>
	dataSetParents = parameters.get("dataSetParents"); #List<String>
	isZipDirectoryUpload = parameters.get("isZipDirectoryUpload"); #String
	metadata = parameters.get("metadata"); #java.util.LinkedHashMap<String, String> where the key is the name
	properties = getProperties(tr, parameters, dataSetType, EntityKind.DATA_SET);

	#Create Dataset
	dataSet = tr.createNewDataSet(dataSetType);
	dataSet.setParentDatasets(dataSetParents);
	
	if sampleIdentifier is not None:
		dataSetSample = getSampleByIdentifierForUpdate(tr, sampleIdentifier);
		dataSet.setSample(dataSetSample);
	elif experimentIdentifier is not None:
		dataSetExperiment = tr.getExperiment(experimentIdentifier);
		dataSet.setExperiment(dataSetExperiment);
	
	#Assign Data Set properties
	for key in metadata.keySet():
		propertyValue = getPropertyValue(properties, metadata, key);
		dataSet.setPropertyValue(key,propertyValue);
	
	#Move All Files using a tmp directory close to the datastore
	threadProperties = getThreadProperties(tr);
	tempDir =  threadProperties[u'incoming-dir'] + "/" + DataSetRegistrationCleanUpTask.ELN_TEMP_DIR_FOR_REGISTRATION + "/" + str(time.time());
	tempDirFile = File(tempDir);
	tempDirFile.mkdirs();
	
	#tempDir = System.getProperty("java.io.tmpdir");
	dss_component = ServiceProvider.getDssServiceRpcGeneric().getService();
	
	for fileName in fileNames:
		folderFile = File(tempDir + "/" + folderName);
		folderFile.mkdir();
		temFile = File(tempDir + "/" + folderName + "/" + fileName);
		inputStream = dss_component.getFileFromSessionWorkspace(parameters.get("sessionID"), fileName);
		outputStream = FileOutputStream(temFile);
		IOUtils.copyLarge(inputStream, outputStream);
		IOUtils.closeQuietly(inputStream);
		IOUtils.closeQuietly(outputStream);
		
	#CASE - 1: Only one file as zip, uncompressed on the folder
	if fileNames.size() == 1 and isZipDirectoryUpload:
		temFile = File(tempDir + "/" + folderName + "/" + fileNames.get(0));
		tempFolder = tempDir + "/" +  folderName;
		zipFile = ZipFile(temFile.getAbsolutePath());
		zipFile.extractAll(tempFolder);
		temFile.delete();
		tr.moveFile(tempFolder, dataSet);
	elif fileNames.size() > 1: #CASE - 2: Multiple files on the folder
		temFile = File(tempDir + "/"+ folderName);
		tr.moveFile(temFile.getAbsolutePath(), dataSet);
	else: #CASE - 3: One file only
		temFile = File(tempDir + "/" + folderName + "/" + fileNames.get(0));
		tr.moveFile(temFile.getAbsolutePath(), dataSet);
	#Clean Files from workspace
	for fileName in fileNames:
		dss_component.deleteSessionWorkspaceFile(parameters.get("sessionID"), fileName);
	
	#Return from the call
	return True;

def copyAndLinkAsParent(tr, parameters, tableBuilder):
	newSampleIdentifier = parameters.get("newSampleIdentifier"); #String
	sampleIdentifierToCopyAndLinkAsParent = parameters.get("sampleIdentifierToCopyAndLinkAsParent"); #String
	experimentIdentifierToAssignToCopy = parameters.get("experimentIdentifierToAssignToCopy"); #String
	
	#Create new Sample
	sampleToCopyAndLinkAsParent = getSampleByIdentifierForUpdate(tr, sampleIdentifierToCopyAndLinkAsParent); #Retrieve Sample
	newSample = tr.createNewSample(newSampleIdentifier, sampleToCopyAndLinkAsParent.getSampleType()); #Create Sample given his id
	
	#Assign Parent
	newSample.setParentSampleIdentifiers([sampleIdentifierToCopyAndLinkAsParent]);
	
	#Assign properties
	propertiesDefinitions = searchService.listPropertiesDefinitionsForSampleType(sampleToCopyAndLinkAsParent.getSampleType());
	for propertyDefinition in propertiesDefinitions:
		propCode = propertyDefinition.getPropertyTypeCode();
		propValue = sampleToCopyAndLinkAsParent.getPropertyValue(propCode);
		if propValue is not None:
			newSample.setPropertyValue(propCode, propValue);
	
	#Assign Experiment
	experimentToAssignToCopy = tr.getExperiment(experimentIdentifierToAssignToCopy);
	newSample.setExperiment(experimentToAssignToCopy);
	
	return True;

def copySample(tr, projectSamplesEnabled, parameters, tableBuilder):
	#Store Children to copy later
	sampleSpace = parameters.get("sampleSpace"); #String
	sampleProject = parameters.get("sampleProject"); #String
	sampleCode = parameters.get("sampleCode"); #String
	sampleIdentifier = createSampleIdentifier(sampleSpace, sampleProject, sampleCode, projectSamplesEnabled)

	sampleChildren = parameters.get("sampleChildren"); #List<String> Identifiers are in SPACE/CODE format
	parameters.put("sampleChildren", []); #List<String> Identifiers are in SPACE/CODE format

	#Create new Sample
	parameters.put("method", "insertSample"); #List<String> Identifiers are in SPACE/CODE format
	permId = insertUpdateSample(tr, projectSamplesEnabled, parameters, tableBuilder);
	#Copy children and attach to Sample
	sampleIdentifierOriginal = createSampleIdentifier(sampleSpace, sampleProject, parameters.get("sampleCodeOrig"), projectSamplesEnabled)
	sampleOriginal = getSampleByIdentifierForUpdate(tr, sampleIdentifierOriginal); #Retrieve Sample
	
	if sampleChildren != None and parameters.get("copyChildrenOnCopy") != False:
		for sampleChildIdentifier in sampleChildren:
			child = getSampleByIdentifierForUpdate(tr, sampleChildIdentifier); #Retrieve Sample child to copy
			copyChildCode = None
			try: #For autogenerated children that have the code as sufix
				indexFromCopiedChildrenParentCode = child.getCode().index('_')
				copyChildCode = parameters.get("sampleCode") + child.getCode()[indexFromCopiedChildrenParentCode:];
			except: #For all other children
				copyChildCode = parameters.get("sampleCode") + "_" + child.getCode();
			
			if parameters.get("copyChildrenOnCopy") == "ToParentCollection":
				copyChildIdentifier = createSampleIdentifier(sampleSpace, sampleProject, copyChildCode, projectSamplesEnabled)
			if parameters.get("copyChildrenOnCopy") == "ToOriginalCollection":
				copyChildIdentifier = createSampleIdentifier(sampleChildIdentifier.split("/")[1], sampleChildIdentifier.split("/")[2], copyChildCode, projectSamplesEnabled)
			# Create new sample children
			childCopy = tr.createNewSample(copyChildIdentifier, child.getSampleType()); #Create Sample given his id
			childParents = childCopy.getParentSampleIdentifiers();
			childParents.add(sampleIdentifier);
			childCopy.setParentSampleIdentifiers(childParents);

			if parameters.get("copyChildrenOnCopy") == "ToParentCollection" and sampleOriginal.getExperiment() is not None:
				childCopy.setExperiment(sampleOriginal.getExperiment());
			if parameters.get("copyChildrenOnCopy") == "ToOriginalCollection" and child.getExperiment() is not None:
				childCopy.setExperiment(child.getExperiment());


			searchService = tr.getSearchService();
			propertiesDefinitions = searchService.listPropertiesDefinitionsForSampleType(child.getSampleType());
			for propertyDefinition in propertiesDefinitions:
				propCode = propertyDefinition.getPropertyTypeCode();
				propValue = getCopySampleChildrenPropertyValue(
					propCode,
					child.getPropertyValue(propCode),
					parameters.get("notCopyProperties"),
					parameters.get("defaultBenchPropertyList"),
					parameters.get("defaultBenchProperties")
				);
				if propValue != None:
					childCopy.setPropertyValue(propCode, propValue);

	return permId;

#This method is used to return the properties, deleting the storage ones and setting the default storage
def getCopySampleChildrenPropertyValue(propCode, propValue, notCopyProperties, defaultBenchPropertyList, defaultBenchProperties):
	isPropertyToSkip = any(propCode == s for s in notCopyProperties);
	isDefaultBenchProperty = any(propCode == s for s in defaultBenchPropertyList);
	if isPropertyToSkip:
		return None;
	elif isDefaultBenchProperty:
		return str(defaultBenchProperties[propCode]);
	else:
		return propValue;

def batchOperation(tr, projectSamplesEnabled, parameters, tableBuilder):
	for operationParameters in parameters.get("operations"):
		if operationParameters.get("method") == "updateSample":
			operationParameters["sessionToken"] = parameters.get("sessionToken");
			insertUpdateSample(tr, projectSamplesEnabled, operationParameters, tableBuilder);
	return True;
	
def insertUpdateSample(tr, projectSamplesEnabled, parameters, tableBuilder):
	#Mandatory parameters
	sampleSpace = parameters.get("sampleSpace"); #String
	sampleProject = parameters.get("sampleProject"); #String
	sampleExperiment = parameters.get("sampleExperiment"); #String
	sampleCode = parameters.get("sampleCode"); #String
	sampleType = parameters.get("sampleType"); #String
	sampleProperties = parameters.get("sampleProperties"); #java.util.LinkedHashMap<String, String> where the key is the name
	changesToDo = parameters.get("changesToDo");
	
	#Optional parameters
	sampleParents = parameters.get("sampleParents"); #List<String> Identifiers are in SPACE/CODE format
	sampleChildrenNew = parameters.get("sampleChildrenNew"); #List<java.util.LinkedHashMap<String, String>>
	sampleChildrenAdded = parameters.get("sampleChildrenAdded"); #List<String> Identifiers are in SPACE/CODE format
	sampleChildrenRemoved = parameters.get("sampleChildrenRemoved"); #List<String> Identifiers are in SPACE/CODE format
	sampleParentsNew = parameters.get("sampleParentsNew");

	#Create/Get for update sample	
	sampleIdentifier = createSampleIdentifier(sampleSpace, sampleProject, sampleCode, projectSamplesEnabled)
	print "sampleIdentifier: " + sampleIdentifier
	method = parameters.get("method");
	if method == "insertSample":
		sample = tr.createNewSample(sampleIdentifier, sampleType); #Create Sample given his id
		
	if method == "updateSample":
		sample = getSampleByIdentifierForUpdate(tr, sampleIdentifier); #Retrieve Sample
	
	#Obtain space
	space = None;
	if sampleSpace != None:
		space = tr.getSpace(sampleSpace);
		if space == None:
			space = tr.createNewSpace(sampleSpace, None);
	
	#Obtain experiment
	experiment = None;
	if sampleSpace != None and sampleProject != None and sampleExperiment != None:
		experimentIdentifier = "/" + sampleSpace + "/" + sampleProject + "/" + sampleExperiment;
		experiment = tr.getExperiment(experimentIdentifier);
	
	#Assign experiment
	if experiment != None:
		sample.setExperiment(experiment);
	
	#Add sample parents
	parentsToAdd = [];
	if sampleParentsNew != None:
		for newSampleParent in sampleParentsNew:
			parent = tr.createNewSample(newSampleParent.get("identifier"), newSampleParent.get("sampleTypeCode")); #Create Sample given his id
			properties = getProperties(tr, parameters, newSampleParent.get("sampleTypeCode"), EntityKind.SAMPLE);
			if newSampleParent.get("annotations") != None and sampleProperties.get("$ANNOTATIONS_STATE") != None:
				sample_ANNOTATIONS_STATE = sampleProperties.get("$ANNOTATIONS_STATE");
				sample_ANNOTATIONS_STATE = sample_ANNOTATIONS_STATE.replace("PERM_ID_PLACEHOLDER_FOR" + newSampleParent.get("identifier"), parent.getPermId());
				sampleProperties.put("$ANNOTATIONS_STATE", sample_ANNOTATIONS_STATE);
			if newSampleParent.get("experimentIdentifierOrNull") != None:
				parentExperiment = tr.getExperiment(newSampleParent.get("experimentIdentifierOrNull"));
				parent.setExperiment(parentExperiment);
			for key in newSampleParent.get("properties").keySet():
				propertyValue = getPropertyValue(properties, newSampleParent.get("properties"), key);
				parent.setPropertyValue(key, propertyValue);
			
			if newSampleParent.get("parentsIdentifiers") != None:
				parent.setParentSampleIdentifiers(newSampleParent.get("parentsIdentifiers"));
			
			parentsToAdd.append(parent);
			if sampleParents == None:
				sampleParents = [];
			sampleParents = sampleParents + [parent.getSampleIdentifier()];
	
	#Assign sample properties
	properties = getProperties(tr, parameters, sampleType, EntityKind.SAMPLE);
	if sampleProperties != None:
		for key in sampleProperties.keySet():
			propertyValue = getPropertyValue(properties, sampleProperties, key);
			sample.setPropertyValue(key, propertyValue);
	
	#Add sample parents
	if sampleParents != None:
		sample.setParentSampleIdentifiers(sampleParents);
	
	#Create new sample children
	sampleChildrenNewIdentifiers = [];
	if sampleChildrenNew != None:
		for newSampleChild in sampleChildrenNew:
			child = getSampleByIdentifierForUpdate(tr, newSampleChild["identifier"]); #Retrieve Sample
			properties = getProperties(tr, parameters, newSampleChild["sampleTypeCode"], EntityKind.SAMPLE);
			if child is None:
				child = tr.createNewSample(newSampleChild["identifier"], newSampleChild["sampleTypeCode"]); #Create Sample given his id
			sampleChildrenNewIdentifiers.append(newSampleChild["identifier"]);
			child.setParentSampleIdentifiers([sampleIdentifier]);
			childExperimentIdentifier = None
			childExperiment = None
			if "experimentIdentifier" in newSampleChild:
				childExperimentIdentifier = newSampleChild["experimentIdentifier"];
				childExperiment = tr.getExperiment(childExperimentIdentifier);
			if childExperiment != None:
				child.setExperiment(childExperiment);
			for key in newSampleChild["properties"].keySet():
				propertyValue = getPropertyValue(properties, newSampleChild["properties"], key);
				child.setPropertyValue(key, propertyValue);
			if ("children" in newSampleChild) and (newSampleChild["children"] != None):
				for childChildrenData in newSampleChild["children"]:
					childChildren = tr.createNewSample(childChildrenData["identifier"], childChildrenData["sampleTypeCode"]); #Create Sample given his id
					properties = getProperties(tr, parameters, childChildrenData["sampleTypeCode"], EntityKind.SAMPLE);
					childChildren.setParentSampleIdentifiers([newSampleChild["identifier"]]);
					childChildrenExperimentIdentifier = None
					childChildrenExperiment = None
					if "experimentIdentifier" in childChildrenData:
						childChildrenExperimentIdentifier = childChildrenData["experimentIdentifier"];
						childChildrenExperiment = tr.getExperiment(childChildrenExperimentIdentifier);
					if childChildrenExperiment != None:
						childChildren.setExperiment(childChildrenExperiment);
					for key in childChildrenData["properties"].keySet():
						propertyValue = getPropertyValue(properties, childChildrenData["properties"], key);
						childChildren.setPropertyValue(key, propertyValue);
	
	#Add sample children that are not newly created
	if sampleChildrenAdded != None:
		for sampleChildIdentifier in sampleChildrenAdded:
			if sampleChildIdentifier not in sampleChildrenNewIdentifiers:
				child = getSampleByIdentifierForUpdate(tr, sampleChildIdentifier); #Retrieve Sample
				childParents = child.getParentSampleIdentifiers();
				childParents.add(sampleIdentifier);
				child.setParentSampleIdentifiers(childParents);

	#Remove sample children
	if sampleChildrenRemoved != None:
		for sampleChildIdentifier in sampleChildrenRemoved:
			child = getSampleByIdentifierForUpdate(tr, sampleChildIdentifier); #Retrieve Sample
			if child != None: #The new created ones will not be found
				childParents = child.getParentSampleIdentifiers();
				childParents.remove(sampleIdentifier);
				child.setParentSampleIdentifiers(childParents);
	
	#Changes to do
	if changesToDo is not None:
		for change in changesToDo:
			sampleWithChanges = getSampleByIdentifierForUpdate(tr, change["identifier"]); #Retrieve Sample
			properties = getProperties(tr, parameters, sampleWithChanges.getSampleType(), EntityKind.SAMPLE);
			for key in change["properties"].keySet():
					propertyValue = getPropertyValue(properties, change["properties"], key);
					sampleWithChanges.setPropertyValue(key,propertyValue);
		
	#Return from the call
	return sample.getPermId();
	
def moveSample(tr, parameters, tableBuilder):
	sampleIdentifier = parameters.get("sampleIdentifier"); #String
	experimentIdentifier = parameters.get("experimentIdentifier"); #String
	experimentType = parameters.get("experimentType"); #String
	
	sample = getSampleByIdentifierForUpdate(tr, sampleIdentifier); #Retrieve Sample
	experiment = tr.getExperiment(experimentIdentifier); #Retrieve Experiment
	
	if experiment is None:
		experiment = tr.createNewExperiment(experimentIdentifier, experimentType);
	
	sample.setExperiment(experiment);
	return True

def insertUpdateExperiment(tr, parameters, tableBuilder):
	#Mandatory parameters
	experimentType = parameters.get("experimentType"); #String
	experimentIdentifier = parameters.get("experimentIdentifier"); #String
	experimentProperties = parameters.get("experimentProperties"); #java.util.LinkedHashMap<String, String> where the key is the name
	properties = getProperties(tr, parameters, experimentType, EntityKind.EXPERIMENT);
	
	experiment = None;
	method = parameters.get("method");
	if method == "insertExperiment":
		experiment = tr.createNewExperiment(experimentIdentifier, experimentType); #Create Experiment given his id
	if method == "updateExperiment":
		experiment = tr.getExperimentForUpdate(experimentIdentifier); #Retrieve Experiment
	
	for key in experimentProperties.keySet():
		propertyValue = getPropertyValue(properties, experimentProperties, key);
		experiment.setPropertyValue(key,propertyValue);
	
	return True;

def searchSamples(tr, v3, parameters, tableBuilder, sessionId):
	###############
	############### V3 Search
	###############
	fechOptions = parameters;
	
	# FreeText
	anyFieldContains = fechOptions.get("anyFieldContains");
	
	# Attributes
	samplePermId = fechOptions.get("samplePermId");
	withExperimentWithProjectPermId = fechOptions.get("withExperimentWithProjectPermId");
	sampleIdentifier = fechOptions.get("sampleIdentifier");
	sampleCode = fechOptions.get("sampleCode");
	sampleTypeCode = fechOptions.get("sampleTypeCode");
	registrationDate = fechOptions.get("registrationDate");
	modificationDate = fechOptions.get("modificationDate");
		
	# Properties
	properyKeyValueList = fechOptions.get("properyKeyValueList");
	
	# Sub Queries
	sampleExperimentIdentifier = fechOptions.get("sampleExperimentIdentifier");
	sampleContainerPermId = fechOptions.get("sampleContainerPermId");
	
	# Hierarchy Options
	withProperties = fechOptions.get("withProperties");
	withParents = fechOptions.get("withParents");
	withChildren = fechOptions.get("withChildren");
	withAncestors = fechOptions.get("withAncestors");
	withDescendants = fechOptions.get("withDescendants");

	#Search Setup
	criterion = SampleSearchCriteria();
	criterion.withAndOperator();
	fetchOptions = SampleFetchOptions();
	
	#Free Text
	if anyFieldContains is not None:
		words = anyFieldContains.split();
		for word in words:
			if (word != None) and (word != ""):
				criterion.withAnyField().thatContains(word);
	
	#Attributes
	if samplePermId is not None:
		criterion.withPermId().thatEquals(samplePermId);
	if withExperimentWithProjectPermId is not None:
		criterion.withExperiment().withProject().withPermId().thatEquals(withExperimentWithProjectPermId);
	if sampleIdentifier is not None:
		criterion.withId().thatEquals(SampleIdentifier(sampleIdentifier));
	if sampleCode is not None:
		criterion.withCode().thatEquals(sampleCode);
	if sampleTypeCode is not None:
		criterion.withType().withCode().thatEquals(sampleTypeCode);
	if registrationDate is not None:
		formatter = SimpleDateFormat("yyyy-MM-dd");
		registrationDateObject = formatter.parse(registrationDate);
		criterion.withRegistrationDate().thatEquals(registrationDateObject);
	if modificationDate is not None:
		formatter = SimpleDateFormat("yyyy-MM-dd");
		modificationDateObject = formatter.parse(modificationDate);
		criterion.withModificationDate().thatEquals(modificationDateObject);
	
	#Properties
	if properyKeyValueList is not None:
		for keyValuePair in properyKeyValueList:
			for propertyTypeCode in keyValuePair.keySet():
				propertyValue = keyValuePair.get(propertyTypeCode);
				criterion.withProperty(propertyTypeCode).thatEquals(propertyValue);
	
	#Sub queries
	if sampleExperimentIdentifier is not None:
		criterion.withExperiment().withId().thatEquals(ExperimentIdentifier(sampleExperimentIdentifier));
	if sampleContainerPermId is not None:
		criterion.withContainer().withPermId().thatEquals(sampleContainerPermId);

	#Hierarchy Fetch Options
	if withProperties:
		fetchOptions.withProperties();
	if withParents:	
		fetchOptionsParents = SampleFetchOptions();
		fetchOptionsParents.withProperties();
		fetchOptionsParents.withType();
		fetchOptionsParents.withSpace();
		fetchOptionsParents.withExperiment();
		fetchOptionsParents.withRegistrator();
		fetchOptionsParents.withModifier();
		fetchOptions.withParentsUsing(fetchOptionsParents);
	if withChildren:
		fetchOptionsChildren = SampleFetchOptions();
		fetchOptionsChildren.withProperties();
		fetchOptionsChildren.withType();
		fetchOptionsChildren.withSpace();
		fetchOptionsChildren.withExperiment();
		fetchOptionsChildren.withRegistrator();
		fetchOptionsChildren.withModifier();
		fetchOptions.withChildrenUsing(fetchOptionsChildren);
	if withAncestors:
		fetchOptionsAncestors = SampleFetchOptions();
		fetchOptionsAncestors.withProperties();
		fetchOptionsAncestors.withType();
		fetchOptionsAncestors.withSpace();
		fetchOptionsAncestors.withExperiment();
		fetchOptionsAncestors.withRegistrator();
		fetchOptionsAncestors.withModifier();
		fetchOptionsAncestors.withParentsUsing(fetchOptionsAncestors);
		fetchOptions.withParentsUsing(fetchOptionsAncestors);
	if withDescendants:
		fetchOptionsDescendants = SampleFetchOptions();
		fetchOptionsDescendants.withProperties();
		fetchOptionsDescendants.withType();
		fetchOptionsDescendants.withSpace();
		fetchOptionsDescendants.withExperiment();
		fetchOptionsDescendants.withRegistrator();
		fetchOptionsDescendants.withModifier();
		fetchOptionsDescendants.withChildrenUsing(fetchOptionsDescendants);
		fetchOptions.withChildrenUsing(fetchOptionsDescendants);
	
	#Standard Fetch Options, always use
	fetchOptions.withType();
	fetchOptions.withSpace();
	fetchOptions.withExperiment();
	fetchOptions.withRegistrator();
	fetchOptions.withModifier();
	
	###############
	###############
	###############
	
	
	##
	## Custom (Interceptor to modify standard results)
	##
	result = None;
	
	isCustom = parameters.get("custom"); #Boolean
	if isCustom:
		result = searchSamplesCustom(tr, parameters, tableBuilder, v3, criterion, fetchOptions);
	else:
		result = v3.searchSamples(parameters.get("sessionToken"), criterion, fetchOptions);
	
	##
	##
	##
	
	###
	### Json Conversion
	###
	resultAsString = ServiceProvider.getObjectMapperV3().writeValueAsString(result);
	return resultAsString;

def searchSamplesCustom(tr, parameters, tableBuilder, v3, criterion, fetchOptions):
	# SEARCH CUSTOM PLACEHOLDER START
	return [];
	# SEARCH CUSTOM PLACEHOLDER END
	
def searchSamplesNexus(tr, parameters, tableBuilder, v3, criterion, fetchOptions):
	
	toReturnPermIds = []; #
	#Right Givers: The sample with all his descendants
	#1. Request user search with all right givers
	descendantsFetchOptions = SampleFetchOptions();
	descendantsFetchOptions.withChildrenUsing(descendantsFetchOptions);
	requestedResults = v3.searchSamples(tr.getOpenBisServiceSessionToken(), criterion, descendantsFetchOptions);
 	
	if requestedResults.getTotalCount() > 0:
		#Prepare data structures for the rights givers to accelerate the process
		requestedToRigthsGivers = {};
		allRightsGivers = set();
		for requestedResult in requestedResults.getObjects():
			rigthsGivers = getDescendantsTreePermIdsStringSet([requestedResult]);
			allRightsGivers = allRightsGivers | rigthsGivers;
			requestedToRigthsGivers[requestedResult.getPermId().getPermId()] = rigthsGivers;
 		
		#2. Search for the visible right givers
 		
		visibleRightGivers = v3.getSamples(parameters.get("sessionToken"), getSamplePermIdsObjFromPermIdStrings(allRightsGivers), SampleFetchOptions());
		visibleRightGiversPermIds = getDescendantsTreePermIdsStringSet(visibleRightGivers.values());
		#3. Intersect what the user wants and is available to see and keep matches
		for requestedResultPermIdString in requestedToRigthsGivers:
			rigthsGiversPermIds = requestedToRigthsGivers[requestedResultPermIdString];
			intersection = rigthsGiversPermIds & visibleRightGiversPermIds;
			if len(intersection) > 0:
				toReturnPermIds.append(SamplePermId(requestedResultPermIdString));
 	
	#Now we complete those permIds with all information available for them using a search by the ETL server
	systemResultAsMap = v3.getSamples(tr.getOpenBisServiceSessionToken(), toReturnPermIds, fetchOptions);
	systemResult = ArrayList(systemResultAsMap.values());
	systemSearchResult = SearchResult(systemResult, systemResult.size());
 	
	return systemSearchResult
 
def getSamplePermIdsObjFromPermIdStrings(samplePermIds):
	values = [];
	for samplePermId in samplePermIds:
		values.append(SamplePermId(samplePermId));
	return values;
 	
def getDescendantsTreePermIdsStringSet(samples):
	descendantsPermIds = set();
	for sample in samples:
		descendantsQueue = [sample];
		while len(descendantsQueue) > 0:
			queueSample = descendantsQueue.pop();
			if queueSample.getPermId().getPermId() not in descendantsPermIds:
				descendantsPermIds.add(queueSample.getPermId().getPermId());
				if queueSample.getFetchOptions().hasChildren():
					for child in queueSample.getChildren():
						descendantsQueue.append(child);
	return descendantsPermIds;
