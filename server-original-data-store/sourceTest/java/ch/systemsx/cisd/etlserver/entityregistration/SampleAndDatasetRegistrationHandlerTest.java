/*
 * Copyright 2010 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.etlserver.entityregistration;

import static ch.systemsx.cisd.etlserver.entityregistration.SampleAndDataSetRegistrationHandler.DATA_SET_TYPE_PROPERTIES_KEY;
import static ch.systemsx.cisd.etlserver.entityregistration.SampleAndDataSetRegistrationHandler.DATA_SET_KIND_PROPERTIES_KEY;
import static ch.systemsx.cisd.etlserver.entityregistration.SampleAndDataSetRegistrationHandler.SAMPLE_TYPE_PROPERTIES_KEY;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;

import org.apache.log4j.Logger;
import org.hamcrest.core.IsNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileConstants;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.etlserver.DataSetRegistrationAlgorithm;
import ch.systemsx.cisd.etlserver.IDataSetHandler;
import ch.systemsx.cisd.etlserver.IExtensibleDataSetHandler;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class SampleAndDatasetRegistrationHandlerTest extends AbstractFileSystemTestCase
{
    private static final String SPACE_CODE = "MYSPACE";

    private static final String HANDLER_PROP_PREFIX = IDataSetHandler.DATASET_HANDLER_KEY + ".";

    protected Mockery context;

    protected IEncapsulatedOpenBISService openbisService;

    protected IExtensibleDataSetHandler delegator;

    protected IMailClient mailClient;

    protected SampleAndDataSetRegistrationHandler handler;

    protected BufferedAppender logAppender;

    private File markerFile;

    private File workingCopy;

    @Override
    @BeforeMethod
    public void setUp() throws IOException
    {
        super.setUp();

        logAppender = LogRecordingUtils.createRecorder();

        context = new Mockery();
        openbisService = context.mock(IEncapsulatedOpenBISService.class);
        delegator = context.mock(IExtensibleDataSetHandler.class);
        mailClient = context.mock(IMailClient.class);

        setupMarkerFileExpectations();

    }

    @AfterMethod
    public void tearDown()
    {
        logAppender.resetLogContent();
        context.assertIsSatisfied();
    }

    @Test
    public void testRegisteringMissingSampleIdentifier()
    {
        final RecordingMatcher<DataSetInformation> dataSetInfoMatcher =
                new RecordingMatcher<DataSetInformation>();
        final RecordingMatcher<NewSample> newSampleMatcher = new RecordingMatcher<NewSample>();

        setupOpenBisExpectations();
        NewExternalData externalData = setupDataSetHandlerExpectations(dataSetInfoMatcher, 2);
        setupUpdateSampleExistsExpectations("S1", false);
        setupUpdateSampleExistsExpectations("S3", false);
        setupRegisterSampleAndDataSetExpectations(externalData, newSampleMatcher, 2);

        // Error email on the line with the missing sample identifier
        final RecordingMatcher<DataHandler> attachmentMatcher = new RecordingMatcher<DataHandler>();
        final RecordingMatcher<EMailAddress[]> addressesMatcher =
                new RecordingMatcher<EMailAddress[]>();
        final String folderName = "missing-sample-identifier";
        setupPartialSuccessErrorEmailExpectations(attachmentMatcher, addressesMatcher, folderName);

        createWorkingCopyOfTestFolder(folderName);

        initializeDefaultDataSetHandler();
        handler.handleDataSet(markerFile);

        String logText =
                "Global properties extracted from file 'control.tsv': SAMPLE_TYPE(MY_SAMPLE_TYPE) DATA_SET_TYPE(MY_DATA_SET_TYPE) DATA_SET_KIND(PHYSICAL) USER(test@test.test)\n"
                        + "Registered sample/data set pair SampleDataSetPair[sampleIdentifier=/MYSPACE/S1,sampleProperties={prop1: VAL10,prop2: VAL20,prop3: VAL30},dataSetInformation=User::test;Data Set Type::MY_DATA_SET_TYPE;Data Set Kind::PHYSICAL;Experiment Identifier::/MYSPACE/MYPROJ/EXP1;Is complete::U]\n"
                        + "Registered sample/data set pair SampleDataSetPair[sampleIdentifier=/MYSPACE/S3,sampleProperties={prop1: VAL12,prop2: VAL22,prop3: VAL32},dataSetInformation=User::test;Data Set Type::MY_DATA_SET_TYPE;Data Set Kind::PHYSICAL;Experiment Identifier::/MYSPACE/MYPROJ/EXP3;Is complete::U]\n"
                        + "Encountered errors in the following lines:\n"
                        + "# Illegal empty identifier\n"
                        + "\t/MYSPACE/MYPROJ/EXP2\tVAL11\tVAL21\tVAL31\tFILE_TYPE\tVAL41\tVAL51\tds2/\n\n"
                        + "The following lines were successfully processed:\n"
                        + "# Registered sample and data set\n"
                        + "# /MYSPACE/S1\t/MYSPACE/MYPROJ/EXP1\tVAL10\tVAL20\tVAL30\tFILE_TYPE\tVAL40\tVAL50\tds1/\n"
                        + "# Registered sample and data set\n"
                        + "# /MYSPACE/S3\t/MYSPACE/MYPROJ/EXP3\tVAL12\tVAL22\tVAL32\tFILE_TYPE\tVAL42\tVAL52\tds3/\n";
        checkAppenderContent(logText, folderName);

        context.assertIsSatisfied();
    }

    @Test
    public void testNotAllFoldersMentioned()
    {
        final RecordingMatcher<DataSetInformation> dataSetInfoMatcher =
                new RecordingMatcher<DataSetInformation>();
        final RecordingMatcher<NewSample> newSampleMatcher = new RecordingMatcher<NewSample>();

        setupOpenBisExpectations();
        NewExternalData externalData = setupDataSetHandlerExpectations(dataSetInfoMatcher, 1);
        setupUpdateSampleExistsExpectations("S1", false);
        setupRegisterSampleAndDataSetExpectations(externalData, newSampleMatcher, 1);

        final String folderName = "not-all-subfolders-mentioned";
        final RecordingMatcher<DataHandler> attachmentMatcher = new RecordingMatcher<DataHandler>();
        final RecordingMatcher<EMailAddress[]> addressesMatcher =
                new RecordingMatcher<EMailAddress[]>();
        setupPartialSuccessErrorEmailExpectations(attachmentMatcher, addressesMatcher, folderName);

        createWorkingCopyOfTestFolder(folderName);

        initializeDefaultDataSetHandler();
        handler.handleDataSet(markerFile);

        String logText =
                "Global properties extracted from file 'control.tsv': SAMPLE_TYPE(MY_SAMPLE_TYPE) DATA_SET_TYPE(MY_DATA_SET_TYPE) DATA_SET_KIND(PHYSICAL) USER(test@test.test)\n"
                        + "Registered sample/data set pair SampleDataSetPair[sampleIdentifier=/MYSPACE/S1,sampleProperties={prop1: VAL10,prop2: VAL20,prop3: VAL30},dataSetInformation=User::test;Data Set Type::MY_DATA_SET_TYPE;Data Set Kind::PHYSICAL;Experiment Identifier::/MYSPACE/MYPROJ/EXP1;Is complete::U]\n"
                        + "The following subfolders were in the uploaded folder, but were not mentioned in the control file:\n"
                        + "ds2,ds3\n"
                        + "The following lines were successfully processed:\n"
                        + "# Registered sample and data set\n"
                        + "# /MYSPACE/S1\t/MYSPACE/MYPROJ/EXP1\tVAL10\tVAL20\tVAL30\tFILE_TYPE\tVAL40\tVAL50\tds1/\n";
        checkAppenderContent(logText, folderName);

        context.assertIsSatisfied();
    }

    @Test
    public void testRegisteringBasicControlFile()
    {
        final RecordingMatcher<DataSetInformation> dataSetInfoMatcher =
                new RecordingMatcher<DataSetInformation>();
        final RecordingMatcher<NewSample> newSampleMatcher = new RecordingMatcher<NewSample>();

        setupOpenBisExpectations();
        NewExternalData externalData = setupDataSetHandlerExpectations(dataSetInfoMatcher, 3);
        setupUpdateSampleExistsExpectations("S1", false);
        setupUpdateSampleExistsExpectations("S2", false);
        setupUpdateSampleExistsExpectations("S3", false);
        setupRegisterSampleAndDataSetExpectations(externalData, newSampleMatcher, 3);

        final String folderName = "basic-example";
        final RecordingMatcher<DataHandler> attachmentMatcher = new RecordingMatcher<DataHandler>();
        final RecordingMatcher<EMailAddress[]> addressesMatcher =
                new RecordingMatcher<EMailAddress[]>();
        setupSuccessEmailExpectations(attachmentMatcher, addressesMatcher, folderName);

        createWorkingCopyOfTestFolder(folderName);

        initializeDefaultDataSetHandler();
        handler.handleDataSet(markerFile);

        String logText =
                "Global properties extracted from file 'control.tsv': SAMPLE_TYPE(MY_SAMPLE_TYPE) DATA_SET_TYPE(MY_DATA_SET_TYPE) DATA_SET_KIND(PHYSICAL) USER(test@test.test)\n"
                        + "Registered sample/data set pair SampleDataSetPair[sampleIdentifier=/MYSPACE/S1,sampleProperties={prop1: VAL10,prop2: VAL20,prop3: VAL30},dataSetInformation=User::test;Data Set Type::MY_DATA_SET_TYPE;Data Set Kind::PHYSICAL;Experiment Identifier::/MYSPACE/MYPROJ/EXP1;Is complete::U]\n"
                        + "Registered sample/data set pair SampleDataSetPair[sampleIdentifier=/MYSPACE/S2,sampleProperties={prop1: VAL11,prop2: VAL21,prop3: VAL31},dataSetInformation=User::test;Data Set Type::MY_DATA_SET_TYPE;Data Set Kind::PHYSICAL;Experiment Identifier::/MYSPACE/MYPROJ/EXP2;Is complete::U]\n"
                        + "Registered sample/data set pair SampleDataSetPair[sampleIdentifier=/MYSPACE/S3,sampleProperties={prop1: VAL12,prop2: VAL22,prop3: VAL32},dataSetInformation=User::test;Data Set Type::MY_DATA_SET_TYPE;Data Set Kind::PHYSICAL;Experiment Identifier::/MYSPACE/MYPROJ/EXP3;Is complete::U]";
        checkAppenderContent(logText, folderName);

        context.assertIsSatisfied();
    }

    @Test
    public void testDefaultSampleAndDataSetTypes()
    {
        final RecordingMatcher<DataSetInformation> dataSetInfoMatcher =
                new RecordingMatcher<DataSetInformation>();
        final RecordingMatcher<NewSample> newSampleMatcher = new RecordingMatcher<NewSample>();

        setupOpenBisExpectations();
        NewExternalData externalData = setupDataSetHandlerExpectations(dataSetInfoMatcher, 3);
        setupUpdateSampleExistsExpectations("S1", false);
        setupUpdateSampleExistsExpectations("S2", false);
        setupUpdateSampleExistsExpectations("S3", false);
        setupRegisterSampleAndDataSetExpectations(externalData, newSampleMatcher, 3);

        final String folderName = "no-global-config";
        final RecordingMatcher<DataHandler> attachmentMatcher = new RecordingMatcher<DataHandler>();
        final RecordingMatcher<EMailAddress[]> addressesMatcher =
                new RecordingMatcher<EMailAddress[]>();
        setupSuccessEmailExpectations(attachmentMatcher, addressesMatcher, folderName);

        createWorkingCopyOfTestFolder(folderName);

        Properties props = createDefaultHandlerProps();
        props.put(HANDLER_PROP_PREFIX + SAMPLE_TYPE_PROPERTIES_KEY, "DEF_SAMPLE_TYPE");
        props.put(HANDLER_PROP_PREFIX + DATA_SET_TYPE_PROPERTIES_KEY, "DEF_DS_TYPE");
        props.put(HANDLER_PROP_PREFIX + DATA_SET_KIND_PROPERTIES_KEY, "PHYSICAL");
        initializeDataSetHandler(props);
        handler.handleDataSet(markerFile);

        String logText =
                "Global properties extracted from file 'control.tsv': SAMPLE_TYPE(null) DATA_SET_TYPE(null) DATA_SET_KIND(null) USER(test@test.test)\n"
        				+ "Registered sample/data set pair SampleDataSetPair[sampleIdentifier=/MYSPACE/S1,sampleProperties={prop1: VAL10,prop2: VAL20,prop3: VAL30},dataSetInformation=User::test;Data Set Type::DEF_DS_TYPE;Data Set Kind::PHYSICAL;Experiment Identifier::/MYSPACE/MYPROJ/EXP1;Is complete::U]\n"
                        + "Registered sample/data set pair SampleDataSetPair[sampleIdentifier=/MYSPACE/S2,sampleProperties={prop1: VAL11,prop2: VAL21,prop3: VAL31},dataSetInformation=User::test;Data Set Type::DEF_DS_TYPE;Data Set Kind::PHYSICAL;Experiment Identifier::/MYSPACE/MYPROJ/EXP2;Is complete::U]\n"
                        + "Registered sample/data set pair SampleDataSetPair[sampleIdentifier=/MYSPACE/S3,sampleProperties={prop1: VAL12,prop2: VAL22,prop3: VAL32},dataSetInformation=User::test;Data Set Type::DEF_DS_TYPE;Data Set Kind::PHYSICAL;Experiment Identifier::/MYSPACE/MYPROJ/EXP3;Is complete::U]";
        checkAppenderContent(logText, folderName);

        context.assertIsSatisfied();
    }

    @Test
    public void testRegisteringWithEmptyLinesInControlFile()
    {
        final RecordingMatcher<DataSetInformation> dataSetInfoMatcher =
                new RecordingMatcher<DataSetInformation>();
        final RecordingMatcher<NewSample> newSampleMatcher = new RecordingMatcher<NewSample>();

        setupOpenBisExpectations();
        NewExternalData externalData = setupDataSetHandlerExpectations(dataSetInfoMatcher, 3);
        setupUpdateSampleExistsExpectations("S1", false);
        setupUpdateSampleExistsExpectations("S2", false);
        setupUpdateSampleExistsExpectations("S3", false);
        setupRegisterSampleAndDataSetExpectations(externalData, newSampleMatcher, 3);

        final String folderName = "empty-lines";
        final RecordingMatcher<DataHandler> attachmentMatcher = new RecordingMatcher<DataHandler>();
        final RecordingMatcher<EMailAddress[]> addressesMatcher =
                new RecordingMatcher<EMailAddress[]>();
        setupSuccessEmailExpectations(attachmentMatcher, addressesMatcher, folderName);

        createWorkingCopyOfTestFolder(folderName);

        initializeDefaultDataSetHandler();
        handler.handleDataSet(markerFile);

        String logText =
                "Global properties extracted from file 'control.tsv': SAMPLE_TYPE(MY_SAMPLE_TYPE) DATA_SET_TYPE(MY_DATA_SET_TYPE) DATA_SET_KIND(PHYSICAL) USER(test@test.test)\n"
                        + "Registered sample/data set pair SampleDataSetPair[sampleIdentifier=/MYSPACE/S1,sampleProperties={prop1: VAL10,prop2: VAL20,prop3: VAL30},dataSetInformation=User::test;Data Set Type::MY_DATA_SET_TYPE;Data Set Kind::PHYSICAL;Experiment Identifier::/MYSPACE/MYPROJ/EXP1;Is complete::U]\n"
                        + "Registered sample/data set pair SampleDataSetPair[sampleIdentifier=/MYSPACE/S2,sampleProperties={prop1: VAL11,prop2: VAL21,prop3: VAL31},dataSetInformation=User::test;Data Set Type::MY_DATA_SET_TYPE;Data Set Kind::PHYSICAL;Experiment Identifier::/MYSPACE/MYPROJ/EXP2;Is complete::U]\n"
                        + "Registered sample/data set pair SampleDataSetPair[sampleIdentifier=/MYSPACE/S3,sampleProperties={prop1: VAL12,prop2: VAL22,prop3: VAL32},dataSetInformation=User::test;Data Set Type::MY_DATA_SET_TYPE;Data Set Kind::PHYSICAL;Experiment Identifier::/MYSPACE/MYPROJ/EXP3;Is complete::U]";
        checkAppenderContent(logText, folderName);

        context.assertIsSatisfied();
    }

    @Test
    public void testOnlyUpdates()
    {
        final RecordingMatcher<DataSetInformation> dataSetInfoMatcher =
                new RecordingMatcher<DataSetInformation>();
        final RecordingMatcher<SampleUpdatesDTO> sampleUpdatesMatcher =
                new RecordingMatcher<SampleUpdatesDTO>();

        setupOpenBisExpectations();
        NewExternalData externalData = setupDataSetHandlerExpectations(dataSetInfoMatcher, 3);
        setupUpdateSampleExistsExpectations("S1", true);
        setupUpdateSampleExistsExpectations("S2", true);
        setupUpdateSampleExistsExpectations("S3", true);
        setupUpdateSampleAndRegisterDataSetExpectations(externalData, sampleUpdatesMatcher, 3);

        final String folderName = "basic-example";
        final RecordingMatcher<DataHandler> attachmentMatcher = new RecordingMatcher<DataHandler>();
        final RecordingMatcher<EMailAddress[]> addressesMatcher =
                new RecordingMatcher<EMailAddress[]>();
        setupSuccessEmailExpectations(attachmentMatcher, addressesMatcher, folderName);

        createWorkingCopyOfTestFolder(folderName);

        initializeDefaultDataSetHandler();
        handler.handleDataSet(markerFile);

        String logText =
                "Global properties extracted from file 'control.tsv': SAMPLE_TYPE(MY_SAMPLE_TYPE) DATA_SET_TYPE(MY_DATA_SET_TYPE) DATA_SET_KIND(PHYSICAL) USER(test@test.test)\n"
                        + "Updated sample, registered data set SampleDataSetPair[sampleIdentifier=/MYSPACE/S1,sampleProperties={prop1: VAL10,prop2: VAL20,prop3: VAL30},dataSetInformation=User::test;Data Set Type::MY_DATA_SET_TYPE;Data Set Kind::PHYSICAL;Experiment Identifier::/MYSPACE/MYPROJ/EXP1;Is complete::U]\n"
                        + "Updated sample, registered data set SampleDataSetPair[sampleIdentifier=/MYSPACE/S2,sampleProperties={prop1: VAL11,prop2: VAL21,prop3: VAL31},dataSetInformation=User::test;Data Set Type::MY_DATA_SET_TYPE;Data Set Kind::PHYSICAL;Experiment Identifier::/MYSPACE/MYPROJ/EXP2;Is complete::U]\n"
                        + "Updated sample, registered data set SampleDataSetPair[sampleIdentifier=/MYSPACE/S3,sampleProperties={prop1: VAL12,prop2: VAL22,prop3: VAL32},dataSetInformation=User::test;Data Set Type::MY_DATA_SET_TYPE;Data Set Kind::PHYSICAL;Experiment Identifier::/MYSPACE/MYPROJ/EXP3;Is complete::U]";
        checkAppenderContent(logText, folderName);

        context.assertIsSatisfied();
    }

    @Test
    public void testRegisteringAndUpdates()
    {
        final RecordingMatcher<DataSetInformation> dataSetInfoMatcher =
                new RecordingMatcher<DataSetInformation>();
        final RecordingMatcher<NewSample> newSampleMatcher = new RecordingMatcher<NewSample>();
        final RecordingMatcher<SampleUpdatesDTO> sampleUpdatesMatcher =
                new RecordingMatcher<SampleUpdatesDTO>();

        setupOpenBisExpectations();
        NewExternalData externalData = setupDataSetHandlerExpectations(dataSetInfoMatcher, 3);
        setupUpdateSampleExistsExpectations("S1", false);
        setupUpdateSampleExistsExpectations("S2", true);
        setupUpdateSampleExistsExpectations("S3", false);
        setupRegisterSampleAndDataSetExpectations(externalData, newSampleMatcher, 2);
        setupUpdateSampleAndRegisterDataSetExpectations(externalData, sampleUpdatesMatcher, 1);

        final String folderName = "basic-example";

        final RecordingMatcher<DataHandler> attachmentMatcher = new RecordingMatcher<DataHandler>();
        final RecordingMatcher<EMailAddress[]> addressesMatcher =
                new RecordingMatcher<EMailAddress[]>();
        setupSuccessEmailExpectations(attachmentMatcher, addressesMatcher, folderName);

        createWorkingCopyOfTestFolder(folderName);

        initializeDefaultDataSetHandler();
        handler.handleDataSet(markerFile);

        String logText =
                "Global properties extracted from file 'control.tsv': SAMPLE_TYPE(MY_SAMPLE_TYPE) DATA_SET_TYPE(MY_DATA_SET_TYPE) DATA_SET_KIND(PHYSICAL) USER(test@test.test)\n"
                        + "Registered sample/data set pair SampleDataSetPair[sampleIdentifier=/MYSPACE/S1,sampleProperties={prop1: VAL10,prop2: VAL20,prop3: VAL30},dataSetInformation=User::test;Data Set Type::MY_DATA_SET_TYPE;Data Set Kind::PHYSICAL;Experiment Identifier::/MYSPACE/MYPROJ/EXP1;Is complete::U]\n"
                        + "Updated sample, registered data set SampleDataSetPair[sampleIdentifier=/MYSPACE/S2,sampleProperties={prop1: VAL11,prop2: VAL21,prop3: VAL31},dataSetInformation=User::test;Data Set Type::MY_DATA_SET_TYPE;Data Set Kind::PHYSICAL;Experiment Identifier::/MYSPACE/MYPROJ/EXP2;Is complete::U]\n"
                        + "Registered sample/data set pair SampleDataSetPair[sampleIdentifier=/MYSPACE/S3,sampleProperties={prop1: VAL12,prop2: VAL22,prop3: VAL32},dataSetInformation=User::test;Data Set Type::MY_DATA_SET_TYPE;Data Set Kind::PHYSICAL;Experiment Identifier::/MYSPACE/MYPROJ/EXP3;Is complete::U]";
        checkAppenderContent(logText, folderName);

        context.assertIsSatisfied();
    }

    @Test
    public void testRegisteringFolderWithoutControlFiles() throws IOException
    {
        final RecordingMatcher<DataHandler> attachmentMatcher = new RecordingMatcher<DataHandler>();
        final RecordingMatcher<EMailAddress[]> addressesMatcher =
                new RecordingMatcher<EMailAddress[]>();

        setupFailureErrorEmailExpectations(attachmentMatcher, addressesMatcher, "no-control");

        createWorkingCopyOfTestFolder("no-control");

        initializeDefaultDataSetHandler();
        handler.handleDataSet(markerFile);

        String errorText =
                "Folder (no-control) for sample/dataset registration contains no control files matching the configured pattern: .*\\.[Tt][Ss][Vv].\n"
                        + "Folder contents:\n" + "\tnot-a-tsv.txt\n";

        // Check the log
        checkAppenderContent(errorText, "no-control");

        checkEmailContent(attachmentMatcher, addressesMatcher, errorText);

        context.assertIsSatisfied();
    }

    @Test
    public void testRegisteringEmptyFolder() throws IOException
    {
        final RecordingMatcher<DataHandler> attachmentMatcher = new RecordingMatcher<DataHandler>();
        final RecordingMatcher<EMailAddress[]> addressesMatcher =
                new RecordingMatcher<EMailAddress[]>();

        setupFailureErrorEmailExpectations(attachmentMatcher, addressesMatcher, "empty-folder");

        createWorkingCopyOfTestFolder("empty-folder");

        initializeDefaultDataSetHandler();
        handler.handleDataSet(markerFile);

        String errorText =
                "Folder (empty-folder) for sample/dataset registration contains no control files matching the configured pattern: .*\\.[Tt][Ss][Vv].\n"
                        + "Folder contents:\n";
        // Check the log
        checkAppenderContent(errorText, "empty-folder");

        // Check the email
        checkEmailContent(attachmentMatcher, addressesMatcher, errorText);

        context.assertIsSatisfied();
    }

    @Test
    public void testUndefinedSampleRegistrationMode()
    {
        final Properties props = new Properties();
        props.setProperty("dataset-handler.sample-registration-mode", "UNKNOWN");
        initializeDataSetHandler(props);

        // Check that the configuration failure was logged
        assertEquals("UNKNOWN is an unknown registration mode, defaulting to ACCEPT_ALL",
                logAppender.getLogContent());

        context.assertIsSatisfied();
    }

    private Properties createDefaultHandlerProps()
    {
        final Properties props = new Properties();
        props.setProperty("dataset-handler.data-space", "store");
        return props;
    }

    private void initializeDefaultDataSetHandler()
    {
        Properties props = createDefaultHandlerProps();
        initializeDataSetHandler(props);
    }

    private void initializeDataSetHandler(Properties props)
    {
        props.setProperty("processor", "ch.systemsx.cisd.etlserver.DefaultStorageProcessor");
        handler = new SampleAndDataSetRegistrationHandler(props, delegator, openbisService);
        handler.initializeMailClient(mailClient);
    }

    private void createWorkingCopyOfTestFolder(String folderName)
    {
        File dataSetFile =
                new File("sourceTest/java/ch/systemsx/cisd/etlserver/entityregistration/test-data/"
                        + folderName);

        dataSetFile.mkdir();

        workingCopy = new File(workingDirectory, folderName);
        FileOperations.getInstance().copy(dataSetFile, workingCopy);

        markerFile = new File(workingDirectory, FileConstants.IS_FINISHED_PREFIX + folderName);
        FileUtilities.writeToFile(markerFile, "");
    }

    private void setupFailureErrorEmailExpectations(
            final RecordingMatcher<DataHandler> attachmentMatcher,
            final RecordingMatcher<EMailAddress[]> addressesMatcher, final String folderName)
    {
        context.checking(new Expectations()
            {
                {
                    Person admin = new Person();
                    admin.setEmail("test@test.test");
                    admin.setUserId("test");
                    one(openbisService).listAdministrators();
                    will(returnValue(Collections.singletonList(admin)));
                }
            });
        setupEmailExpectations(
                attachmentMatcher,
                addressesMatcher,
                "Sample / Data Set Registration Error -- targets/unit-test-wd/ch.systemsx.cisd.etlserver.entityregistration.SampleAndDatasetRegistrationHandlerTest/"
                        + folderName,
                "When trying to process the files in the folder, targets/unit-test-wd/ch.systemsx.cisd.etlserver.entityregistration.SampleAndDatasetRegistrationHandlerTest/"
                        + folderName
                        + ", errors were encountered. These errors are detailed in the attachment.",
                "errors.txt", folderName);
    }

    private void setupPartialSuccessErrorEmailExpectations(
            final RecordingMatcher<DataHandler> attachmentMatcher,
            final RecordingMatcher<EMailAddress[]> addressesMatcher, final String folderName)
    {
        setupEmailExpectations(
                attachmentMatcher,
                addressesMatcher,
                "Sample / Data Set Registration Error -- targets/unit-test-wd/ch.systemsx.cisd.etlserver.entityregistration.SampleAndDatasetRegistrationHandlerTest/"
                        + folderName + "/control.tsv",
                "Not all samples and data sets specified in the control file, targets/unit-test-wd/ch.systemsx.cisd.etlserver.entityregistration.SampleAndDatasetRegistrationHandlerTest/"
                        + folderName
                        + "/control.tsv, could be registered / updated. The errors are detailed in the attachment. Each faulty line is reproduced, preceded by a comment explaining the cause of the error.",
                "errors.txt", folderName);
    }

    private void setupSuccessEmailExpectations(
            final RecordingMatcher<DataHandler> attachmentMatcher,
            final RecordingMatcher<EMailAddress[]> addressesMatcher, final String folderName)
    {
        setupEmailExpectations(
                attachmentMatcher,
                addressesMatcher,
                "Sample / Data Set Registration Succeeded -- targets/unit-test-wd/ch.systemsx.cisd.etlserver.entityregistration.SampleAndDatasetRegistrationHandlerTest/"
                        + folderName + "/control.tsv",
                "The registration/update of samples and the registration of data sets specified in the control file, targets/unit-test-wd/ch.systemsx.cisd.etlserver.entityregistration.SampleAndDatasetRegistrationHandlerTest/"
                        + folderName + "/control.tsv, was successful.", "registered.txt",
                folderName);
    }

    private void setupEmailExpectations(final RecordingMatcher<DataHandler> attachmentMatcher,
            final RecordingMatcher<EMailAddress[]> addressesMatcher, final String emailSubject,
            final String emailBody, final String attachmentName, final String folderName)
    {
        context.checking(new Expectations()
            {
                {
                    one(mailClient).sendEmailMessageWithAttachment(with(emailSubject),
                            with(emailBody), with(attachmentName), with(attachmentMatcher),
                            with(new IsNull<EMailAddress>()), with(new IsNull<EMailAddress>()),
                            with(addressesMatcher));
                }
            });
    }

    private void setupOpenBisExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    Person admin = new Person();
                    admin.setEmail("test@test.test");
                    admin.setUserId("test");
                    oneOf(openbisService).tryPersonWithUserIdOrEmail(admin.getEmail());
                    will(returnValue(admin));
                }
            });
    }

    private void setupMarkerFileExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    exactly(1).of(delegator).isUseIsFinishedMarkerFile();
                    will(returnValue(true));

                    exactly(1).of(delegator).getStoreRootDir();
                    will(returnValue(workingDirectory));
                }
            });
    }

    private NewExternalData setupDataSetHandlerExpectations(
            final RecordingMatcher<DataSetInformation> dataSetInfoMatcher, final int count)
    {
        final NewExternalData externalData = new NewExternalData();
        context.checking(new Expectations()
            {
                {
                    final RecordingMatcher<File> fileMatcher = new RecordingMatcher<File>();
                    final RecordingMatcher<DataSetRegistrationAlgorithm.IDataSetInApplicationServerRegistrator> registratorMatcher =
                            new RecordingMatcher<DataSetRegistrationAlgorithm.IDataSetInApplicationServerRegistrator>();

                    for (int i = 1; i < 4; ++i)
                    {
                        ExperimentIdentifier experimentId =
                                new ExperimentIdentifier(SPACE_CODE, "MYPROJ", "EXP" + i);
                        allowing(openbisService).tryGetExperiment(experimentId);
                        Experiment exp = new Experiment();
                        exp.setIdentifier(experimentId.toString());
                        will(returnValue(exp));
                    }

                    exactly(count).of(delegator).handleDataSet(with(fileMatcher),
                            with(dataSetInfoMatcher), with(registratorMatcher));
                    will(new CustomAction("Call registrator")
                        {
                            @Override
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                List<DataSetRegistrationAlgorithm.IDataSetInApplicationServerRegistrator> recordedObjects =
                                        registratorMatcher.getRecordedObjects();
                                DataSetRegistrationAlgorithm.IDataSetInApplicationServerRegistrator registrator =
                                        recordedObjects.get(recordedObjects.size() - 1);
                                fillExternalDataFromInformation(externalData, dataSetInfoMatcher
                                        .getRecordedObjects().get(recordedObjects.size() - 1));
                                registrator.registerDataSetInApplicationServer(externalData);

                                return null;
                            }
                        });
                }
            });
        return externalData;
    }

    private void setupRegisterSampleAndDataSetExpectations(final NewExternalData externalData,
            final RecordingMatcher<NewSample> newSampleMatcher, final int count)
    {
        context.checking(new Expectations()
            {
                {
                    exactly(count).of(openbisService).registerSampleAndDataSet(
                            with(newSampleMatcher), with(externalData), with("test"));
                    will(returnValue(new Sample()));
                }
            });
    }

    private void setupUpdateSampleExistsExpectations(final String sampleCode, final boolean exists)
    {
        final SpaceIdentifier spaceId =
                new SpaceIdentifier(SPACE_CODE);
        // Decide if the samples exist
        context.checking(new Expectations()
            {
                {

                    oneOf(openbisService).tryGetSampleWithExperiment(
                            new SampleIdentifier(spaceId, sampleCode));
                    if (exists)
                    {
                        will(returnValue(new Sample()));
                    } else
                    {
                        will(returnValue(null));
                    }
                }
            });
    }

    private void setupUpdateSampleAndRegisterDataSetExpectations(
            final NewExternalData externalData,
            final RecordingMatcher<SampleUpdatesDTO> sampleUpdatesMatcher, final int count)
    {
        context.checking(new Expectations()
            {
                {
                    exactly(count).of(openbisService).updateSampleAndRegisterDataSet(
                            with(sampleUpdatesMatcher), with(externalData));
                    will(returnValue(new Sample()));
                }
            });
    }

    private void fillExternalDataFromInformation(NewExternalData externalData,
            DataSetInformation dataSetInformation)
    {
        externalData.setUserId(dataSetInformation.getUploadingUserIdOrNull());
        externalData.setUserEMail(dataSetInformation.tryGetUploadingUserEmail());
        externalData.setExtractableData(dataSetInformation.getExtractableData());
    }

    private void checkEmailContent(final RecordingMatcher<DataHandler> attachmentMatcher,
            final RecordingMatcher<EMailAddress[]> addressesMatcher, String errorText)
            throws IOException
    {
        for (EMailAddress address : addressesMatcher.recordedObject())
        {
            assertEquals("test@test.test", address.tryGetEmailAddress());
        }
        assertEquals(errorText, attachmentMatcher.recordedObject().getContent());
    }

    private void checkAppenderContent(String logText, String folderName)
    {
        String mainLogText = String.format("%s\nDeleting file '%s'.", logText, folderName);

        String theLogText;
        if (Logger.getRootLogger().isDebugEnabled())
        {
            String formatString =
                    "Getting incoming data set path 'targets/unit-test-wd/ch.systemsx.cisd.etlserver.entityregistration.SampleAndDatasetRegistrationHandlerTest/%s' "
                            + "from is-finished path 'targets/unit-test-wd/ch.systemsx.cisd.etlserver.entityregistration.SampleAndDatasetRegistrationHandlerTest/%s'\n"
                            + "%s" + "\nMarker file '%s' has been removed.";
            String markerFileName = FileConstants.IS_FINISHED_PREFIX + folderName;
            theLogText =
                    String.format(formatString, folderName, markerFileName, mainLogText,
                            markerFile.getAbsolutePath());
        } else
        {
            theLogText = mainLogText;
        }
        // Check the text
        AssertionUtil.assertContainsLines(theLogText, logAppender.getLogContent());
    }
}
