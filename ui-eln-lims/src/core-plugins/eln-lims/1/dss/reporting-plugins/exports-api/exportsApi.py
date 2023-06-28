#
# Copyright 2016 ETH Zuerich, Scientific IT Services
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
import json
from collections import deque

import jarray
# To obtain the openBIS URL
from ch.systemsx.cisd.openbis.dss.generic.server import DataStoreServer
from ch.systemsx.cisd.openbis.generic.client.web.client.exception import UserFailureException
# Zip Format
from java.io import File, BufferedInputStream
from java.io import FileInputStream
from java.io import FileOutputStream
from java.lang import String
from java.lang import StringBuilder
from java.util import ArrayList
from java.util.zip import ZipEntry, Deflater, ZipOutputStream, CRC32
from org.apache.commons.io import FileUtils
# Java Core
from org.apache.commons.io import IOUtils

OPENBISURL = DataStoreServer.getConfigParameters().getServerURL() + "/openbis/openbis"
V3_DSS_BEAN = "data-store-server_INTERNAL"

#V3 API - Metadata

from HTMLParser import HTMLParser

from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search import SpaceSearchCriteria;
from ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search import ProjectSearchCriteria;
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search import ExperimentSearchCriteria;
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search import SampleSearchCriteria;
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search import DataSetSearchCriteria;

from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions import SpaceFetchOptions;
from ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions import ProjectFetchOptions;
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions import ExperimentFetchOptions;
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions import SampleFetchOptions;
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions import DataSetFetchOptions;

from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search import ExperimentTypeSearchCriteria;
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions import ExperimentTypeFetchOptions;

from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search import SampleTypeSearchCriteria;
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions import SampleTypeFetchOptions;

from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search import DataSetTypeSearchCriteria;
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions import DataSetTypeFetchOptions;

from ch.ethz.sis.openbis.generic.asapi.v3.dto.property import DataType
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset import DataSetKind

#V3 API - Files
from ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.search import DataSetFileSearchCriteria;
from ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fetchoptions import DataSetFileFetchOptions;
from ch.systemsx.cisd.openbis.dss.generic.shared import ServiceProvider;
from ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id import DataSetFilePermId;
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id import DataSetPermId;
from ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download import DataSetFileDownloadOptions;
from ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download import DataSetFileDownloadReader

#JSON
from com.fasterxml.jackson.databind import SerializationFeature

#Logging
from ch.systemsx.cisd.common.logging import LogCategory;
from org.apache.log4j import Logger;
operationLog = Logger.getLogger(str(LogCategory.OPERATION) + ".exportsApi.py");

#AVI API - DTO
from ch.ethz.sis.openbis.generic.asapi.v3.dto.project import Project
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment import Experiment
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample import Sample
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset import DataSet

#DOCX
from ch.ethz.sis import DOCXBuilder

#Images export for word
from org.jsoup import Jsoup;

from com.github.freva.asciitable import AsciiTable

class MLStripper(HTMLParser):
    def __init__(self):
        self.reset()
        self.fed = []
    def handle_data(self, d):
        self.fed.append(d)
    def get_data(self):
        return ''.join(self.fed)


def strip_tags(html):
    s = MLStripper()
    s.feed(html)
    return s.get_data()


def displayResult(isOk, tableBuilder, result=None, errorMessage="Operation Failed"):
    if isOk:
        tableBuilder.addHeader("STATUS");
        tableBuilder.addHeader("MESSAGE");
        tableBuilder.addHeader("RESULT");
        row = tableBuilder.addRow();
        row.setCell("STATUS", "OK");
        row.setCell("MESSAGE", "Operation Successful");
        row.setCell("RESULT", result);
    else:
        tableBuilder.addHeader("STATUS");
        tableBuilder.addHeader("Error");
        row = tableBuilder.addRow();
        row.setCell("STATUS", "FAIL");
        row.setCell("Error", errorMessage);


def addToExportWithoutRepeating(entitiesToExport, entityFound):
    found = False;
    for entityToExport in entitiesToExport:
        if entityToExport["type"] != "FILE" and entityToExport["permId"] == entityFound["permId"] and entityToExport["type"] == entityFound["type"]:
            found = True;
            break;
    if not found:
        entitiesToExport.append(entityFound);


def validateDataSize(entitiesToExport, tr):
    limitDataSizeInMegabytes = getConfigurationProperty(tr, 'limit-data-size-megabytes')
    if limitDataSizeInMegabytes is None:
        limitDataSizeInMegabytes = 500;
    else:
        limitDataSizeInMegabytes = int(limitDataSizeInMegabytes);
    limitDataSizeInBytes = 1000000 * limitDataSizeInMegabytes;
    estimatedSizeInBytes = 0;
    for entityToExport in entitiesToExport:
        if entityToExport["type"] == "FILE" and entityToExport["isDirectory"] == False:
            estimatedSizeInBytes += entityToExport["length"];
        elif entityToExport["type"] != "FILE":
            estimatedSizeInBytes += 12000;  # AVG File Metadata size
    estimatedSizeInMegabytes = estimatedSizeInBytes / 1000000;
    operationLog.info(
        u"Size Limit check - limitDataSizeInBytes: " + str(limitDataSizeInBytes).encode('utf-8') +
        u" > estimatedSizeInBytes: " + str(estimatedSizeInBytes).encode('utf-8'));
    if estimatedSizeInBytes > limitDataSizeInBytes:
        raise UserFailureException(u"The selected data is " + estimatedSizeInMegabytes +
                                   u" MB that is bigger than the configured limit of " +
                                   limitDataSizeInMegabytes + u" MB");


def findEntitiesToExport(params):
    sessionToken = params.get("sessionToken");
    v3 = ServiceProvider.getV3ApplicationService();
    v3d = ServiceProvider.getApplicationContext().getBean(V3_DSS_BEAN);
    metadataOnly = params.get("metadataOnly");
    entitiesToExpand = deque([]);
    entitiesToExport = [];
    entities = params.get("entities");

    for entity in entities:
        entityAsPythonMap = { "type" : entity.get("type"), "permId" : entity.get("permId"), "expand" : entity.get("expand") };
        entitiesToExport.append(entityAsPythonMap);
        if entity.get("expand"):
            entitiesToExpand.append(entityAsPythonMap);

    operationLog.info(u"Found %d entities to expand." % len(entitiesToExpand))
    while entitiesToExpand:
        entityToExpand = entitiesToExpand.popleft();
        type = entityToExpand["type"];
        permId = entityToExpand["permId"];
        operationLog.info(u"Expanding type: " + type.encode("utf-8") + u" permId: " + permId.encode("utf-8"));

        if type == "ROOT":
            criteria = SpaceSearchCriteria();
            results = v3.searchSpaces(sessionToken, criteria, SpaceFetchOptions());
            operationLog.info(u"Found: %d spaces" % results.getTotalCount());
            for space in results.getObjects():
                entityFound = {"type": "SPACE", "permId": space.getCode(), "registrationDate": space.getRegistrationDate()};
                addToExportWithoutRepeating(entitiesToExport, entityFound);
                entitiesToExpand.append(entityFound);
        if type == "SPACE":
            criteria = ProjectSearchCriteria();
            criteria.withSpace().withCode().thatEquals(permId);
            results = v3.searchProjects(sessionToken, criteria, ProjectFetchOptions());
            operationLog.info(u"Found: %d projects" % results.getTotalCount());
            for project in results.getObjects():
                entityFound = {"type": "PROJECT", "permId": project.getPermId().getPermId(), "registrationDate": project.getRegistrationDate()};
                addToExportWithoutRepeating(entitiesToExport, entityFound);
                entitiesToExpand.append(entityFound);
        if type == "PROJECT":
            criteria = ExperimentSearchCriteria();
            criteria.withProject().withPermId().thatEquals(permId);
            results = v3.searchExperiments(sessionToken, criteria, ExperimentFetchOptions());
            operationLog.info(u"Found: %d experiments" % results.getTotalCount());
            for experiment in results.getObjects():
                entityFound = {"type": "EXPERIMENT", "permId": experiment.getPermId().getPermId(),
                               "registrationDate": experiment.getRegistrationDate()};
                addToExportWithoutRepeating(entitiesToExport, entityFound);
                entitiesToExpand.append(entityFound);
        if type == "EXPERIMENT":
            criteria = SampleSearchCriteria();
            criteria.withExperiment().withPermId().thatEquals(permId);
            results = v3.searchSamples(sessionToken, criteria, SampleFetchOptions());
            operationLog.info(u"Found: %d samples" % results.getTotalCount());

            dCriteria = DataSetSearchCriteria();
            dCriteria.withExperiment().withPermId().thatEquals(permId);
            dCriteria.withoutSample();
            fetchOptions = DataSetFetchOptions()
            fetchOptions.withDataStore()
            dResults = v3.searchDataSets(sessionToken, dCriteria, fetchOptions);
            operationLog.info(u"Found: %d datasets" % dResults.getTotalCount());
            for dataset in dResults.getObjects():
                entityFound = {"type": "DATASET", "permId": dataset.getPermId().getPermId(), "registrationDate": dataset.getRegistrationDate()};
                addToExportWithoutRepeating(entitiesToExport, entityFound);
                entitiesToExpand.append(entityFound);

            operationLog.info(u"Found: %d samples" % results.getTotalCount());
            for sample in results.getObjects():
                entityFound = {"type": "SAMPLE", "permId": sample.getPermId().getPermId(), "registrationDate": sample.getRegistrationDate()};
                addToExportWithoutRepeating(entitiesToExport, entityFound);
                entitiesToExpand.append(entityFound);
        if type == "SAMPLE":
            criteria = DataSetSearchCriteria();
            criteria.withSample().withPermId().thatEquals(permId);
            fetchOptions = DataSetFetchOptions()
            fetchOptions.withDataStore()
            results = v3.searchDataSets(sessionToken, criteria, fetchOptions);
            operationLog.info(u"Found: %d datasets" % results.getTotalCount());
            for dataset in results.getObjects():
                entityFound = {"type": "DATASET", "permId": dataset.getPermId().getPermId(), "registrationDate": dataset.getRegistrationDate()};
                addToExportWithoutRepeating(entitiesToExport, entityFound);
                entitiesToExpand.append(entityFound);
        if type == "DATASET" and not metadataOnly:
            criteria = DataSetSearchCriteria()
            criteria.withPermId().thatEquals(permId)
            fetchOptions = DataSetFetchOptions()
            results = v3.searchDataSets(sessionToken, criteria, fetchOptions)

            if results.getTotalCount() == 0:
                raise UserFailureException(u"No dataset found with permId %s" % permId)

            dataSet = results.getObjects().get(0)

            if dataSet.getKind() != DataSetKind.LINK:
                criteria = DataSetFileSearchCriteria();
                criteria.withDataSet().withPermId().thatEquals(permId);
                results = v3d.searchFiles(sessionToken, criteria, DataSetFileFetchOptions());
                operationLog.info(u"Found: %d files" % results.getTotalCount());

                for file in results.getObjects():
                    entityFound = {"type": "FILE", "permId": permId, "path": file.getPath(), "isDirectory": file.isDirectory(),
                                   "length": file.getFileLength()};
                    addToExportWithoutRepeating(entitiesToExport, entityFound);
            else:
                operationLog.info(u"Omitted data export for link dataset with permId: %s" % permId)
    return entitiesToExport


# Removes temporal folder and zip
def cleanUp(tempDirPath, tempZipFilePath):
    FileUtils.forceDelete(File(tempDirPath));
    FileUtils.forceDelete(File(tempZipFilePath));

# Return Name (Code) if it exists, otherwise return just Code
def extractName(entity):
    name = None
    if "$NAME" in entity.getProperties():
        name = entity.getProperties().get("$NAME") + " (" + entity.getCode() + ")"
    if name is not None and len(name) > 0:
        return name
    else:
        return entity.getCode()


def generateFilesInZip(zos, entities, includeRoot, sessionToken, tempDirPath, deflated=True):
    # Services used during the export process
    v3 = ServiceProvider.getV3ApplicationService();
    v3d = ServiceProvider.getApplicationContext().getBean(V3_DSS_BEAN);
    objectCache = {};
    objectMapper = ServiceProvider.getObjectMapperV3()
    # To avoid empty directories on the zip file, it makes the first found entity the base directory
    baseDirToCut = None;
    fileMetadata = []
    emptyZip = True

    for entity in entities:
        type = entity["type"];
        permId = entity["permId"];
        operationLog.info(u"exporting type: " + type.encode("utf-8") + u" permId: " + permId.encode("utf-8"));
        entityObj = None;
        entityFilePath = None;

        if type == "SPACE":
            pass  # Do nothing
        if type == "PROJECT":
            criteria = ProjectSearchCriteria();
            criteria.withPermId().thatEquals(permId);
            fetchOps = ProjectFetchOptions();
            fetchOps.withSpace();
            fetchOps.withRegistrator();
            fetchOps.withModifier();
            entityObj = v3.searchProjects(sessionToken, criteria, fetchOps).getObjects().get(0);
            entityFilePath = getFilePath(entityObj.getSpace().getCode(), entityObj.getCode(), None, None, None);
        if type == "EXPERIMENT":
            criteria = ExperimentSearchCriteria();
            criteria.withPermId().thatEquals(permId);
            fetchOps = ExperimentFetchOptions();
            fetchOps.withType();
            fetchOps.withProject().withSpace();
            fetchOps.withRegistrator();
            fetchOps.withModifier();
            fetchOps.withProperties();
            fetchOps.withTags();
            entityObj = v3.searchExperiments(sessionToken, criteria, fetchOps).getObjects().get(0);
            entityFilePath = getFilePath(entityObj.getProject().getSpace().getCode(), entityObj.getProject().getCode(), extractName(entityObj),
                                         None, None);
        if type == "SAMPLE":
            criteria = SampleSearchCriteria();
            criteria.withPermId().thatEquals(permId);
            fetchOps = SampleFetchOptions();
            fetchOps.withType();
            fetchOps.withExperiment().withProject().withSpace();
            fetchOps.withExperiment().withProperties();
            fetchOps.withRegistrator();
            fetchOps.withModifier();
            fetchOps.withProperties();
            fetchOps.withTags();
            fetchOps.withParents().withProperties();
            fetchOps.withChildren().withProperties();
            entityObj = v3.searchSamples(sessionToken, criteria, fetchOps).getObjects().get(0)
            entityFilePath = getFilePath(entityObj.getExperiment().getProject().getSpace().getCode(),
                                         entityObj.getExperiment().getProject().getCode(), extractName(entityObj.getExperiment()),
                                         extractName(entityObj), None)
        if type == "DATASET":
            criteria = DataSetSearchCriteria();
            criteria.withPermId().thatEquals(permId);
            fetchOps = DataSetFetchOptions();
            fetchOps.withType();
            fetchOps.withSample().withProperties();
            fetchOps.withExperiment().withProject().withSpace();
            fetchOps.withExperiment().withProperties();
            fetchOps.withRegistrator();
            fetchOps.withModifier();
            fetchOps.withProperties();
            fetchOps.withTags();
            fetchOps.withParents().withProperties();
            fetchOps.withChildren().withProperties();
            entityObj = v3.searchDataSets(sessionToken, criteria, fetchOps).getObjects().get(0);

            sampleCode = None
            if (entityObj.getSample() is not None):
                sampleCode = extractName(entityObj.getSample());

            entityFilePath = getFilePath(entityObj.getExperiment().getProject().getSpace().getCode(),
                                         entityObj.getExperiment().getProject().getCode(), extractName(entityObj.getExperiment()), sampleCode,
                                         extractName(entityObj));
        if type == "FILE" and not entity["isDirectory"]:
            datasetEntityObj = objectCache[entity["permId"]];
            sampleCode = None
            if (datasetEntityObj.getSample() is not None):
                sampleCode = extractName(datasetEntityObj.getSample());

            datasetEntityFilePath = getFilePath(datasetEntityObj.getExperiment().getProject().getSpace().getCode(),
                                                datasetEntityObj.getExperiment().getProject().getCode(), extractName(datasetEntityObj.getExperiment()),
                                                sampleCode, extractName(datasetEntityObj));
            filePath = datasetEntityFilePath + "/" + entity["path"];

            if not includeRoot:
                filePath = filePath[
                           len(baseDirToCut):]  # To avoid empty directories on the zip file, it makes the first found entity the base directory

            rawFileInputStream = DataSetFileDownloadReader(v3d.downloadFiles(sessionToken, [DataSetFilePermId(DataSetPermId(permId), entity["path"])],
                                                                             DataSetFileDownloadOptions())).read().getInputStream();
            rawFile = File(tempDirPath + filePath + ".json");
            rawFile.getParentFile().mkdirs();
            IOUtils.copyLarge(rawFileInputStream, FileOutputStream(rawFile));
            addToZipFile(filePath, rawFile, zos, deflated=deflated);
            emptyZip = False

        # To avoid empty directories on the zip file, it makes the first found entity the base directory
        if not includeRoot:
            if baseDirToCut is None and entityFilePath is not None:
                baseDirToCut = entityFilePath[:entityFilePath.rfind('/')];
            if entityFilePath is not None:
                entityFilePath = entityFilePath[len(baseDirToCut):]

        if entityObj is not None:
            objectCache[permId] = entityObj;

        operationLog.info(u"--> Entity type: %s permId: %s obj: %s path: %s before files." % (type, permId, str(entityObj is not None), entityFilePath));
        if entityObj is not None and entityFilePath is not None:
            # JSON
            entityJson = String(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(entityObj));
            fileMetadatum = addFile(tempDirPath, entityFilePath, "json", entityJson.getBytes(), zos, deflated=deflated);
            fileMetadata.append(fileMetadatum)
            emptyZip = False
            # TEXT
            entityTXT = String(getTXT(entityObj, v3, sessionToken, False));
            fileMetadatum = addFile(tempDirPath, entityFilePath, "txt", entityTXT.getBytes(), zos, deflated=deflated);
            fileMetadata.append(fileMetadatum)
            # DOCX
            entityDOCX = getDOCX(entityObj, v3, sessionToken, False);
            fileMetadatum = addFile(tempDirPath, entityFilePath, "docx", entityDOCX, zos, deflated=deflated);
            fileMetadata.append(fileMetadatum)
            # HTML
            entityHTML = getDOCX(entityObj, v3, sessionToken, True);
            fileMetadatum = addFile(tempDirPath, entityFilePath, "html", entityHTML, zos, deflated=deflated);
            fileMetadata.append(fileMetadatum)
            operationLog.info(u"--> Entity type: " + type.encode("utf-8") + u" permId: " + permId.encode("utf-8") +
                              u" post html.");
    if emptyZip:
        raise IOError("Nothing added to ZIP file.")
    return fileMetadata


def generateDownloadUrl(sessionToken, tempZipFileName, tempZipFilePath):
    # Store on workspace to be able to generate a download link
    operationLog.info(u"Zip file can be found on the temporal directory: " + tempZipFilePath.encode("utf-8"));
    dssService = ServiceProvider.getApplicationContext().getBean("dss-service-rpc-generic")
    dssService.putFileToSessionWorkspace(sessionToken, tempZipFileName, FileInputStream(File(tempZipFilePath)))
    tempZipFileWorkspaceURL = DataStoreServer.getConfigParameters().getDownloadURL() + "/datastore_server/session_workspace_file_download?sessionID=" + sessionToken + "&filePath=" + tempZipFileName;
    operationLog.info(u"Zip file can be downloaded from the workspace: " + tempZipFileWorkspaceURL.encode("utf-8"));
    return tempZipFileWorkspaceURL


def getDOCX(entityObj, v3, sessionToken, isHTML):
    docxBuilder = DOCXBuilder();
    docxBuilder.addTitle(entityObj.getCode());
    docxBuilder.addHeader("Identification Info");
    
    typeObj = None
    if isinstance(entityObj, Project):
        docxBuilder.addProperty("Kind", "Project");
    if isinstance(entityObj, Experiment):
        docxBuilder.addProperty("Kind", "Experiment");
        searchCriteria = ExperimentTypeSearchCriteria();
        searchCriteria.withCode().thatEquals(entityObj.getType().getCode());
        fetchOptions = ExperimentTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withPropertyType();
        results = v3.searchExperimentTypes(sessionToken, searchCriteria, fetchOptions);
        typeObj = results.getObjects().get(0);
    if isinstance(entityObj, Sample):
        docxBuilder.addProperty("Kind", "Sample");
        searchCriteria = SampleTypeSearchCriteria();
        searchCriteria.withCode().thatEquals(entityObj.getType().getCode());
        fetchOptions = SampleTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withPropertyType();
        results = v3.searchSampleTypes(sessionToken, searchCriteria, fetchOptions);
        typeObj = results.getObjects().get(0);
    if isinstance(entityObj, DataSet):
        docxBuilder.addProperty("Kind", "DataSet");
        searchCriteria = DataSetTypeSearchCriteria();
        searchCriteria.withCode().thatEquals(entityObj.getType().getCode());
        fetchOptions = DataSetTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withPropertyType();
        results = v3.searchDataSetTypes(sessionToken, searchCriteria, fetchOptions);
        typeObj = results.getObjects().get(0);
    
    if not isinstance(entityObj, Project):
        docxBuilder.addProperty("Type", entityObj.getType().getCode());
    
    if(entityObj.getRegistrator() is not None):
        docxBuilder.addProperty("Registrator", entityObj.getRegistrator().getUserId());
        docxBuilder.addProperty("Registration Date", str(entityObj.getRegistrationDate()));
    
    if entityObj.getModifier() is not None:
        docxBuilder.addProperty("Modifier", entityObj.getModifier().getUserId());
        docxBuilder.addProperty("Modification Date", str(entityObj.getModificationDate()));
    
    
    if isinstance(entityObj, Project):
        description = entityObj.getDescription();
        if description is not None:
            docxBuilder.addHeader("Description");
            docxBuilder.addParagraph(entityObj.getDescription());
    
    if isinstance(entityObj, Sample) or isinstance(entityObj, DataSet):
        docxBuilder.addHeader("Parents");
        parents = entityObj.getParents();
        for parent in parents:
            relCodeName = parent.getCode();
            if "NAME" in parent.getProperties():
                relCodeName = relCodeName + " (" + parent.getProperties()["NAME"] + ")";
            docxBuilder.addParagraph(relCodeName);
        
        docxBuilder.addHeader("Children");
        children = entityObj.getChildren();
        for child in children:
            relCodeName = child.getCode();
            if "NAME" in child.getProperties():
                relCodeName = relCodeName + " (" + child.getProperties()["NAME"] + ")";
            docxBuilder.addParagraph(relCodeName);
    
    if not isinstance(entityObj, Project):
        docxBuilder.addHeader("Properties");
        propertyAssigments = typeObj.getPropertyAssignments();
        properties = entityObj.getProperties();
        for propertyAssigment in propertyAssigments:
            propertyType = propertyAssigment.getPropertyType();
            if propertyType.getCode() in properties:
                propertyValue = properties[propertyType.getCode()];
                if propertyType.getDataType() is DataType.MULTILINE_VARCHAR and propertyType.getMetaData().get("custom_widget") == "Word Processor":
                    doc = Jsoup.parse(propertyValue);
                    imageElements = doc.select("img");
                    for imageElement in imageElements:
                        imageSrc = imageElement.attr("src");
                        propertyValue = propertyValue.replace(imageSrc, DataStoreServer.getConfigParameters().getServerURL() + imageSrc + "?sessionID=" + sessionToken);
                if propertyType.getDataType() is DataType.XML and propertyType.getMetaData().get("custom_widget") == "Spreadsheet" \
                        and propertyValue.upper().startswith("<DATA>") and propertyValue.upper().endswith("</DATA>"):
                    propertyValue = propertyValue[6:-7].decode('base64')
                    propertyValue = convertJsonToHtml(json.loads(propertyValue.encode("utf8")))

                if propertyValue != u"\uFFFD(undefined)":
                    docxBuilder.addProperty(propertyType.getLabel(), propertyValue);
    
    if isHTML:
        return docxBuilder.getHTMLBytes();
    else:
        return docxBuilder.getDocBytes();


def convertJsonToHtml(json):
    data = json["data"]
    styles = json["style"]

    commonStyle = "border: 1px solid black;"
    tableStyle = commonStyle + " border-collapse: collapse;"

    tableBody = StringBuilder()
    for i, dataRow in enumerate(data):
        tableBody.append("<tr>\n")
        for j, cell in enumerate(dataRow):
            stylesKey = convertNumericToAlphanumeric(i, j)
            style = styles[stylesKey]
            tableBody.append("  <td style='").append(commonStyle).append(" ").append(style).append("'> ").append(cell).append(" </td>\n")
        tableBody.append("</tr>\n")
    return ("<table style='%s'>\n" % tableStyle) + tableBody.toString() + "</table>"


def convertNumericToAlphanumeric(row, col):
    aCharCode = ord("A")
    ord0 = col % 26
    ord1 = col / 26
    char0 = chr(aCharCode + ord0)
    char1 = chr(aCharCode + ord1 - 1) if ord1 > 0 else ""
    return char1 + char0 + str(row + 1)


def getTXT(entityObj, v3, sessionToken, isRichText):
    txtBuilder = StringBuilder();
    txtBuilder.append(entityObj.getCode()).append("\n");
    txtBuilder.append("# Identification Info:").append("\n");
    
    typeObj = None
    if isinstance(entityObj, Project):
        txtBuilder.append("- Kind: Project").append("\n");
    if isinstance(entityObj, Experiment):
        txtBuilder.append("- Kind: Experiment").append("\n");
        searchCriteria = ExperimentTypeSearchCriteria();
        searchCriteria.withCode().thatEquals(entityObj.getType().getCode());
        fetchOptions = ExperimentTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withPropertyType();
        results = v3.searchExperimentTypes(sessionToken, searchCriteria, fetchOptions);
        typeObj = results.getObjects().get(0);
    if isinstance(entityObj, Sample):
        txtBuilder.append("- Kind: Sample").append("\n");
        searchCriteria = SampleTypeSearchCriteria();
        searchCriteria.withCode().thatEquals(entityObj.getType().getCode());
        fetchOptions = SampleTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withPropertyType();
        results = v3.searchSampleTypes(sessionToken, searchCriteria, fetchOptions);
        typeObj = results.getObjects().get(0);
    if isinstance(entityObj, DataSet):
        txtBuilder.append("- Kind: DataSet").append("\n");
        searchCriteria = DataSetTypeSearchCriteria();
        searchCriteria.withCode().thatEquals(entityObj.getType().getCode());
        fetchOptions = DataSetTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withPropertyType();
        results = v3.searchDataSetTypes(sessionToken, searchCriteria, fetchOptions);
        typeObj = results.getObjects().get(0);
    
    if not isinstance(entityObj, Project):
        txtBuilder.append("- Type: " + entityObj.getType().getCode()).append("\n");

    if(entityObj.getRegistrator() is not None):
        txtBuilder.append("- Registrator: ").append(entityObj.getRegistrator().getUserId()).append("\n");
        txtBuilder.append("- Registration Date: ").append(str(entityObj.getRegistrationDate())).append("\n");
    
    if entityObj.getModifier() is not None:
        txtBuilder.append("- Modifier: ").append(entityObj.getModifier().getUserId()).append("\n");
        txtBuilder.append("- Modification Date: ").append(str(entityObj.getModificationDate())).append("\n");
    
    
    if isinstance(entityObj, Project):
        description = entityObj.getDescription();
        if description is not None:
            txtBuilder.append("# Description:").append("\n");
            txtBuilder.append(entityObj.getDescription()).append("\n");
    
    if isinstance(entityObj, Sample) or isinstance(entityObj, DataSet):
        txtBuilder.append("# Parents:").append("\n");
        parents = entityObj.getParents();
        for parent in parents:
            relCodeName = parent.getCode();
            if "NAME" in parent.getProperties():
                relCodeName = relCodeName + " (" + parent.getProperties()["NAME"] + ")";
            txtBuilder.append("- ").append(relCodeName).append("\n");
        txtBuilder.append("# Children:").append("\n");
        children = entityObj.getChildren();
        for child in children:
            relCodeName = child.getCode();
            if "NAME" in child.getProperties():
                relCodeName = relCodeName + " (" + child.getProperties()["NAME"] + ")";
            txtBuilder.append("- ").append(relCodeName).append("\n");
    
    if not isinstance(entityObj, Project):
        txtBuilder.append("# Properties:").append("\n");
        propertyAssigments = typeObj.getPropertyAssignments();
        properties = entityObj.getProperties();
        for propertyAssigment in propertyAssigments:
            propertyType = propertyAssigment.getPropertyType();
            if propertyType.getCode() in properties:
                propertyValue = properties[propertyType.getCode()];
                if propertyValue != u"\uFFFD(undefined)":
                    if propertyType.getDataType() is DataType.XML and propertyType.getMetaData().get("custom_widget") == "Spreadsheet" \
                            and propertyValue.upper().startswith("<DATA>") and propertyValue.upper().endswith("</DATA>"):
                        propertyValue = propertyValue[6:-7].decode('base64')
                        propertyValue = "\n" + convertJsonToText(json.loads(propertyValue.encode("utf8")))
                    elif(propertyType.getDataType() == DataType.MULTILINE_VARCHAR and isRichText is False):
                        propertyValue = strip_tags(propertyValue).strip();
                    txtBuilder.append("- ").append(propertyType.getLabel()).append(": ").append(propertyValue).append("\n");

    return txtBuilder.toString();


def convertJsonToText(json):
    data = json["data"]
    return doConvertJsonToText(data)


def doConvertJsonToText(json):
    data = jsonArrayToArray(json)
    return AsciiTable.getTable(objToStrArray(data))


def jsonArrayToArray(json):
    stringList = ArrayList()
    for s in json:
        stringList.add(s)
    return stringList.toArray()


def objToStrArray(objArray):
    result = []
    for subObjArray in objArray:
        row = []
        for obj in subObjArray:
            row.append(obj)
        result.append(row)
    return result


def addFile(tempDirPath, entityFilePath, extension, fileContent, zos, deflated=True):
    entityFileNameWithExtension = entityFilePath + "." + extension
    entityFile = File(tempDirPath + entityFileNameWithExtension);
    entityFile.getParentFile().mkdirs();
    IOUtils.write(fileContent, FileOutputStream(entityFile));
    addToZipFile(entityFileNameWithExtension, entityFile, zos, deflated=deflated);
    FileUtils.forceDelete(entityFile);

    extensionToMimeType = {
        'json': 'application/json',
        'txt': 'text/plain',
        'docx': 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
        'html': 'text/html',
    }

    return {
        'fileName': entityFileNameWithExtension[1:],
        'mimeType': extensionToMimeType.get(extension, 'application/octet-stream')
    }

def getFilePath(spaceCode, projCode, expCode, sampCode, dataCode):
    fileName = "";
    if spaceCode is not None:
        fileName += "/" + spaceCode;
    if projCode is not None:
        fileName += "/" + projCode;
    if expCode is not None:
        fileName += "/" + expCode;
    if sampCode is not None:
        fileName += "/" + sampCode;
    if dataCode is not None:
        fileName += "/" + dataCode;
    return fileName;

def addToZipFile(path, file, zos, deflated=True):
    zipEntry = ZipEntry(path[1:]) # Making paths relative to make them compatible with Windows zip implementation
    if not deflated:
        zipEntry.setMethod(ZipOutputStream.STORED)
        zipEntry.setSize(file.length())
        zipEntry.setCompressedSize(-1)
        crc = getFileCRC(file)
        zipEntry.setCrc(crc)
    else:
        zipEntry.setMethod(ZipOutputStream.DEFLATED)

    zos.putNextEntry(zipEntry)

    try:
        bis = BufferedInputStream(FileInputStream(file))
        bytes = jarray.zeros(1024, "b")
        length = bis.read(bytes)
        while length >= 0:
            zos.write(bytes, 0, length)
            length = bis.read(bytes)
    finally:
        zos.closeEntry()
        if bis is not None:
            bis.close()

def getFileCRC(file):
    bis = None
    crc = CRC32()
    try:
        bis = BufferedInputStream(FileInputStream(file))
        b = jarray.zeros(1024, "b")
        length = bis.read(b)
        while length != -1:
            crc.update(b, 0, length)
            length = bis.read(b)
    finally:
        if bis is not None:
            bis.close()
    return crc.getValue()

def getConfigurationProperty(transaction, propertyName):
    threadProperties = transaction.getGlobalState().getThreadParameters().getThreadProperties();
    try:
        return threadProperties.getProperty(propertyName);
    except:
        return None


def generateZipFile(entities, params, tempDirPath, tempZipFilePath, deflated=True):
    # Generates ZIP file with selected item for export

    sessionToken = params.get('sessionToken')
    includeRoot = params.get('includeRoot')

    fos = None
    zos = None
    try:
        fos = FileOutputStream(tempZipFilePath)
        zos = ZipOutputStream(fos)
        if not deflated:
            zos.setMethod(ZipOutputStream.STORED)
            zos.setLevel(Deflater.NO_COMPRESSION)

        fileMetadata = generateFilesInZip(zos, entities, includeRoot, sessionToken, tempDirPath, deflated=deflated)
    finally:
        if zos is not None:
            zos.close()
        if fos is not None:
            fos.close()

    return fileMetadata


def checkResponseStatus(response):
    status = response.getStatus()
    if status >= 300:
        reason = response.getReason()
        raise ValueError('Unsuccessful response from the server: %s %s' % (status, reason))