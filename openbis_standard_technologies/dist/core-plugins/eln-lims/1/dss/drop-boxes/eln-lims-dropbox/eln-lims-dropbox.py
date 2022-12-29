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
FAILED_TO_PARSE_ERROR_MESSAGE = "Failed to parse folder name";
FAILED_TO_PARSE_SAMPLE_ERROR_MESSAGE = "Failed to parse sample";
FAILED_TO_PARSE_EXPERIMENT_ERROR_MESSAGE = "Failed to parse experiment";
SAMPLE_MISSING_ERROR_MESSAGE = "Sample not found";
EXPERIMENT_MISSING_ERROR_MESSAGE = "Experiment not found";
NAME_PROPERTY_SET_IN_TWO_PLACES_ERROR_MESSAGE = "$NAME property specified twice, it should just be in either folder name or metadata.json"
EMAIL_SUBJECT = "ELN LIMS Dropbox Error";


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
                sample = transaction.getSample("/" + sampleSpace + "/" + projectCode + "/" + sampleCode);
                if sample is None:
                    raise UserFailureException(INVALID_FORMAT_ERROR_MESSAGE + ":" + SAMPLE_MISSING_ERROR_MESSAGE);
                if len(datasetInfo) >= 5:
                    datasetType = datasetInfo[4];
                if len(datasetInfo) >= 6:
                    name = datasetInfo[5];
                if len(datasetInfo) > 6:
                    reportSampleError(transaction, 
                                      INVALID_FORMAT_ERROR_MESSAGE + ":" + FAILED_TO_PARSE_SAMPLE_ERROR_MESSAGE,
                                      sampleSpace, projectCode, sampleCode)
            elif len(datasetInfo) >= 3 and not projectSamplesEnabled:
                sampleSpace = datasetInfo[1];
                sampleCode = datasetInfo[2];
                sample = transaction.getSample("/" + sampleSpace + "/" + sampleCode);
                if sample is None:
                    raise UserFailureException(INVALID_FORMAT_ERROR_MESSAGE + ":" + SAMPLE_MISSING_ERROR_MESSAGE);
                if len(datasetInfo) >= 4:
                    datasetType = datasetInfo[3];
                if len(datasetInfo) >= 5:
                    name = datasetInfo[4];
                if len(datasetInfo) > 5:
                    raise UserFailureException(INVALID_FORMAT_ERROR_MESSAGE + ":" + FAILED_TO_PARSE_SAMPLE_ERROR_MESSAGE);
            else:
                raise UserFailureException(INVALID_FORMAT_ERROR_MESSAGE + ":" + FAILED_TO_PARSE_SAMPLE_ERROR_MESSAGE);
        if entityKind == "E":
            if len(datasetInfo) >= 4:
                experimentSpace = datasetInfo[1];
                projectCode = datasetInfo[2];
                experimentCode = datasetInfo[3];
                experiment = transaction.getExperiment("/" + experimentSpace + "/" + projectCode + "/" + experimentCode);
                if experiment is None:
                    raise UserFailureException(INVALID_FORMAT_ERROR_MESSAGE + ":" + EXPERIMENT_MISSING_ERROR_MESSAGE);
                if len(datasetInfo) >= 5:
                    datasetType = datasetInfo[4];
                if len(datasetInfo) >= 6:
                    name = datasetInfo[5];
                if len(datasetInfo) > 6:
                    reportExperimentError(transaction,
                                          INVALID_FORMAT_ERROR_MESSAGE + ":" + FAILED_TO_PARSE_EXPERIMENT_ERROR_MESSAGE,
                                          experimentSpace, projectCode, experimentCode);
            else:
                raise UserFailureException(INVALID_FORMAT_ERROR_MESSAGE + ":" + FAILED_TO_PARSE_EXPERIMENT_ERROR_MESSAGE);

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

        # Discard folders started with a . (hidden files)
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
            elif (not fileName.startswith('.')) and (not fileName == "Thumbs.db"):
                # Exclude files starting with .
                # Exclude Mac .DS_Store
                # Exclude Windows Thumbs.db
                itemsInFolder = itemsInFolder + 1;
                datasetItem = item;

        if itemsInFolder > 1:
            tmpPath = incoming.getAbsolutePath() + "/" + str(uuid.uuid4());
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


def reportSampleError(transaction, errorMessage, sampleSpace, projectCode, sampleCode):
    v3 = ServiceProvider.getV3ApplicationService();
    sampleIdentifier = SampleIdentifier(sampleSpace, projectCode, None, sampleCode);
    fetchOptions = SampleFetchOptions();
    fetchOptions.withRegistrator();
    foundSample = v3.getSamples(transaction.getOpenBisServiceSessionToken(), List.of(sampleIdentifier), fetchOptions) \
        .get(sampleIdentifier)
    if foundSample is not None:
        sendMail(transaction, foundSample.getRegistrator().getEmail(), EMAIL_SUBJECT, errorMessage);

    # TODO: add mail report to lab contact person / lab instance admins

    raise UserFailureException(errorMessage);


def reportExperimentError(transaction, errorMessage, experimentSpace, projectCode, experimentCode):
    v3 = ServiceProvider.getV3ApplicationService();
    experimentIdentifier = ExperimentIdentifier(experimentSpace, projectCode, experimentCode);
    fetchOptions = ExperimentFetchOptions();
    fetchOptions.withRegistrator();
    foundExperiment = v3.getExperiments(transaction.getOpenBisServiceSessionToken(), List.of(experimentIdentifier),
                                        fetchOptions).get(experimentIdentifier)
    if foundExperiment is not None:
        sendMail(transaction, foundExperiment.getRegistrator().getEmail(), EMAIL_SUBJECT, errorMessage);

    # TODO: add mail report to lab contact person / lab instance admins

    raise UserFailureException(errorMessage);


def sendMail(tr, emailAddress, subject, body):
    tr.getGlobalState().getMailClient().sendEmailMessage(subject, body, None, None, EMailAddress(emailAddress));
