import re
import uuid

from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions import ExperimentFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id import ExperimentIdentifier
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions import SampleFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id import SampleIdentifier
from ch.systemsx.cisd.common.mail import EMailAddress
from ch.systemsx.cisd.openbis.generic.client.web.client.exception import UserFailureException
from ch.systemsx.cisd.openbis.dss.generic.shared import ServiceProvider
from java.io import File
from java.nio.file import Files, Paths, StandardCopyOption
from java.util import List
from org.json import JSONObject
from org.apache.commons.io import FileUtils

INVALID_FORMAT_ERROR_MESSAGE = "Invalid format for the folder name, should follow the pattern <ENTITY_KIND>+<SPACE_CODE>+<PROJECT_CODE>+[<EXPERIMENT_CODE>|<SAMPLE_CODE>]+<OPTIONAL_DATASET_TYPE>+<OPTIONAL_NAME>";
ILLEGAL_CHARACTERS_IN_FILE_NAMES_ERROR_MESSAGE = "Directory or its content contain illegal characters: \"', ~, $, %\"";
FAILED_TO_PARSE_ERROR_MESSAGE = "Failed to parse folder name";
FAILED_TO_PARSE_SAMPLE_ERROR_MESSAGE = "Failed to parse sample";
FAILED_TO_PARSE_EXPERIMENT_ERROR_MESSAGE = "Failed to parse experiment";
FOLDER_CONTAINS_NON_DELETABLE_FILES_ERROR_MESSAGE = "Folder contains non-deletable files";
SAMPLE_MISSING_ERROR_MESSAGE = "Sample not found";
EXPERIMENT_MISSING_ERROR_MESSAGE = "Experiment not found";
NAME_PROPERTY_SET_IN_TWO_PLACES_ERROR_MESSAGE = "$NAME property specified twice, it should just be in either folder name or metadata.json"
EMAIL_SUBJECT = "ELN LIMS Dropbox Error";
ILLEGAL_FILES = ["desktop.ini", "IconCache.db", "thumbs.db"];
ILLEGAL_FILES_ERROR_MESSAGE = "Directory or contains illegal files: " + str(ILLEGAL_FILES);
HIDDEN_FILES_ERROR_MESSAGE = "Directory or contains hidden files: files starting with '.'";

def process(transaction):
    incoming = transaction.getIncoming();
    folderName = incoming.getName();

    if not folderName.startswith('.'):
        datasetInfo = folderName.split("+");
        entityKind = None;
        sample = None;
        experiment = None;
        datasetType = None;
        name = None;

        # Parse entity Kind
        if len(datasetInfo) >= 1:
            entityKind = datasetInfo[0];
        else:
            raise UserFailureException(INVALID_FORMAT_ERROR_MESSAGE + ":" + FAILED_TO_PARSE_ERROR_MESSAGE);

        v3 = ServiceProvider.getV3ApplicationService();
        sessionToken = transaction.getOpenBisServiceSessionToken();
        projectSamplesEnabled = v3.getServerInformation(sessionToken)['project-samples-enabled'] == 'true'

        # Parse entity Kind Format
        if entityKind == "O":
            if len(datasetInfo) >= 4 and projectSamplesEnabled:
                sampleSpace = datasetInfo[1];
                projectCode = datasetInfo[2];
                sampleCode = datasetInfo[3];

                emailAddress = getSampleRegistratorsEmail(transaction, sampleSpace, projectCode, sampleCode)
                sample = transaction.getSample("/" + sampleSpace + "/" + projectCode + "/" + sampleCode);
                if sample is None:
                    reportIssue(transaction,
                                INVALID_FORMAT_ERROR_MESSAGE + ":" + SAMPLE_MISSING_ERROR_MESSAGE,
                                None);
                if len(datasetInfo) >= 5:
                    datasetType = datasetInfo[4];
                if len(datasetInfo) >= 6:
                    name = datasetInfo[5];
                if len(datasetInfo) > 6:
                    reportIssue(transaction,
                                INVALID_FORMAT_ERROR_MESSAGE + ":" + FAILED_TO_PARSE_SAMPLE_ERROR_MESSAGE,
                                emailAddress)
            elif len(datasetInfo) >= 3 and not projectSamplesEnabled:
                sampleSpace = datasetInfo[1];
                sampleCode = datasetInfo[2];

                emailAddress = getSampleRegistratorsEmail(transaction, sampleSpace, None, sampleCode)
                sample = transaction.getSample("/" + sampleSpace + "/" + sampleCode);
                if sample is None:
                    reportIssue(transaction,
                                INVALID_FORMAT_ERROR_MESSAGE + ":" + SAMPLE_MISSING_ERROR_MESSAGE,
                                None);
                if len(datasetInfo) >= 4:
                    datasetType = datasetInfo[3];
                if len(datasetInfo) >= 5:
                    name = datasetInfo[4];
                if len(datasetInfo) > 5:
                    reportIssue(transaction,
                                INVALID_FORMAT_ERROR_MESSAGE + ":" + FAILED_TO_PARSE_SAMPLE_ERROR_MESSAGE,
                                emailAddress)
            else:
                raise UserFailureException(INVALID_FORMAT_ERROR_MESSAGE + ":" + FAILED_TO_PARSE_SAMPLE_ERROR_MESSAGE);

            if hasFolderHiddenFiles(incoming):
                reportIssue(transaction, HIDDEN_FILES_ERROR_MESSAGE + ":"
                            + FAILED_TO_PARSE_SAMPLE_ERROR_MESSAGE, emailAddress);
            if hasFolderIllegalFiles(incoming):
                reportIssue(transaction, ILLEGAL_FILES_ERROR_MESSAGE + ":"
                            + FAILED_TO_PARSE_SAMPLE_ERROR_MESSAGE, emailAddress);
            if hasFolderIllegalCharacters(incoming):
                reportIssue(transaction, ILLEGAL_CHARACTERS_IN_FILE_NAMES_ERROR_MESSAGE + ":"
                            + FAILED_TO_PARSE_SAMPLE_ERROR_MESSAGE, emailAddress);
            if hasFolderReadOnlyFiles(incoming):
                reportIssue(transaction, FOLDER_CONTAINS_NON_DELETABLE_FILES_ERROR_MESSAGE + ":"
                            + FAILED_TO_PARSE_SAMPLE_ERROR_MESSAGE, emailAddress);
        if entityKind == "E":
            if len(datasetInfo) >= 4:
                experimentSpace = datasetInfo[1];
                projectCode = datasetInfo[2];
                experimentCode = datasetInfo[3];

                emailAddress = getExperimentRegistratorsEmail(transaction, experimentSpace, projectCode,
                                                              experimentCode);
                experiment = transaction.getExperiment("/" + experimentSpace + "/" + projectCode + "/" + experimentCode);
                if experiment is None:
                    reportIssue(transaction,
                                INVALID_FORMAT_ERROR_MESSAGE + ":" + EXPERIMENT_MISSING_ERROR_MESSAGE,
                                None);
                if len(datasetInfo) >= 5:
                    datasetType = datasetInfo[4];
                if len(datasetInfo) >= 6:
                    name = datasetInfo[5];
                if len(datasetInfo) > 6:
                    reportIssue(transaction,
                                INVALID_FORMAT_ERROR_MESSAGE + ":" + FAILED_TO_PARSE_EXPERIMENT_ERROR_MESSAGE,
                                emailAddress);
            else:
                raise UserFailureException(INVALID_FORMAT_ERROR_MESSAGE + ":" + FAILED_TO_PARSE_EXPERIMENT_ERROR_MESSAGE);

            if hasFolderHiddenFiles(incoming):
                reportIssue(transaction, HIDDEN_FILES_ERROR_MESSAGE + ":"
                            + FAILED_TO_PARSE_EXPERIMENT_ERROR_MESSAGE, emailAddress);
            if hasFolderIllegalFiles(incoming):
                reportIssue(transaction, ILLEGAL_FILES_ERROR_MESSAGE + ":"
                            + FAILED_TO_PARSE_EXPERIMENT_ERROR_MESSAGE, emailAddress);
            if hasFolderIllegalCharacters(incoming):
                reportIssue(transaction, ILLEGAL_CHARACTERS_IN_FILE_NAMES_ERROR_MESSAGE + ":"
                            + FAILED_TO_PARSE_EXPERIMENT_ERROR_MESSAGE, emailAddress);
            if hasFolderReadOnlyFiles(incoming):
                reportIssue(transaction, FOLDER_CONTAINS_NON_DELETABLE_FILES_ERROR_MESSAGE + ":"
                            + FAILED_TO_PARSE_EXPERIMENT_ERROR_MESSAGE, emailAddress);

        # Create dataset
        dataSet = None;
        if datasetType is not None:  # Set type if found
            dataSet = transaction.createNewDataSet(datasetType);
        else:
            dataSet = transaction.createNewDataSet();

        if name is not None:
            dataSet.setPropertyValue("$NAME", name);  # Set name if found

        # Set sample or experiment
        if sample is not None:
            dataSet.setSample(sample);
        else:
            dataSet.setExperiment(experiment);

        # Move folder to dataset
        filesInFolder = incoming.listFiles();

        itemsInFolder = 0;
        datasetItem = None;
        for item in filesInFolder:
            fileName = item.getName()
            if fileName == "metadata.json":
                root = JSONObject(FileUtils.readFileToString(item, "UTF-8"))
                properties = root.get("properties")
                for propertyKey in properties.keys():
                    if propertyKey == "$NAME" and name is not None:
                        raise UserFailureException(NAME_PROPERTY_SET_IN_TWO_PLACES_ERROR_MESSAGE)
                    propertyValue = properties.get(propertyKey)
                    if propertyValue is not None:
                        propertyValueString = str(propertyValue)
                        dataSet.setPropertyValue(propertyKey, propertyValueString)
            else:
                itemsInFolder = itemsInFolder + 1;
                datasetItem = item;

        if itemsInFolder > 1:
            tmpPath = incoming.getAbsolutePath() + "/default";
            tmpDir = File(tmpPath);
            tmpDir.mkdir();

            try:
                for inputFile in filesInFolder:
                    Files.move(inputFile.toPath(), Paths.get(tmpPath, inputFile.getName()),
                               StandardCopyOption.ATOMIC_MOVE);
                transaction.moveFile(tmpDir.getAbsolutePath(), dataSet);
            finally:
                if tmpDir is not None:
                    tmpDir.delete();
        else:
            transaction.moveFile(datasetItem.getAbsolutePath(), dataSet);


def getContactsEmailAddresses(transaction):
    emailString = getThreadProperties(transaction).get("mail.addresses.dropbox-errors")
    return re.split("[,;]", emailString) if emailString is not None else []


def reportIssue(transaction, errorMessage, emailAddress):
    contacts = getContactsEmailAddresses(transaction);
    allAddresses = [emailAddress] + contacts if emailAddress is not None else contacts;
    sendMail(transaction, map(lambda address: EMailAddress(address), allAddresses), EMAIL_SUBJECT, errorMessage);
    raise UserFailureException(errorMessage);


def hasFolderIllegalCharacters(incoming):
    if bool(re.search(r"['~$%]", incoming.getName())):
        return True;

    files = incoming.listFiles()
    if files is not None:
        for f in files:
            if hasFolderIllegalCharacters(f):
                return True;

    return False;


def hasFolderHiddenFiles(incoming):
    if incoming.getName().startswith("."):
        return True;

    files = incoming.listFiles()
    if files is not None:
        for f in files:
            if hasFolderHiddenFiles(f):
                return True;

    return False;

def hasFolderIllegalFiles(incoming):
    if incoming.getName() in ILLEGAL_FILES:
        return True;

    files = incoming.listFiles()
    if files is not None:
        for f in files:
            if hasFolderIllegalFiles(f):
                return True;

    return False;


def hasFolderReadOnlyFiles(incoming):
    if not incoming.renameTo(incoming):
        return True;

    files = incoming.listFiles()
    if files is not None:
        for f in files:
            if hasFolderReadOnlyFiles(f):
                return True;

    return False;


def sendMail(transaction, emailAddresses, subject, body):
    transaction.getGlobalState().getMailClient().sendEmailMessage(subject, body, None, None, emailAddresses);


def getSampleRegistratorsEmail(transaction, spaceCode, projectCode, sampleCode):
    v3 = ServiceProvider.getV3ApplicationService();
    sampleIdentifier = SampleIdentifier(spaceCode, projectCode, None, sampleCode);
    fetchOptions = SampleFetchOptions();
    fetchOptions.withRegistrator();
    foundSample = v3.getSamples(transaction.getOpenBisServiceSessionToken(), List.of(sampleIdentifier), fetchOptions)\
        .get(sampleIdentifier)
    return foundSample.getRegistrator().getEmail() if foundSample is not None else None


def getExperimentRegistratorsEmail(transaction, spaceCode, projectCode, experimentCode):
    v3 = ServiceProvider.getV3ApplicationService();
    experimentIdentifier = ExperimentIdentifier(spaceCode, projectCode, experimentCode);
    fetchOptions = ExperimentFetchOptions();
    fetchOptions.withRegistrator();
    foundExperiment = v3.getExperiments(transaction.getOpenBisServiceSessionToken(), List.of(experimentIdentifier),
                                        fetchOptions).get(experimentIdentifier)
    return foundExperiment.getRegistrator().getEmail() if foundExperiment is not None else None


def getThreadProperties(transaction):
    threadPropertyDict = {}
    threadProperties = transaction.getGlobalState().getThreadParameters().getThreadProperties()
    for key in threadProperties:
        try:
            threadPropertyDict[key] = threadProperties.getProperty(key)
        except:
            pass
    return threadPropertyDict