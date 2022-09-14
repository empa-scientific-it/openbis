/*
 * Copyright 2014 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.beans.factory.BeanFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.common.time.TimingParameters;
import ch.systemsx.cisd.common.utilities.ITimeAndWaitingProvider;
import ch.systemsx.cisd.common.utilities.MockTimeProvider;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.IMultiDataSetArchiverDBTransaction;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.IMultiDataSetArchiverReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverContainerDTO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDeleter;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataStoreServiceInternal;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ProcessingStatus;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProviderTestWrapper;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetCodesWithStatus;
import ch.systemsx.cisd.openbis.generic.shared.Constants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.DatasetDescriptionBuilder;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * @author Franz-Josef Elmer
 */
public class MultiDataSetArchivingFinalizerTest extends AbstractFileSystemTestCase
{

    private static final String MY_GROUP = "my-group";

    private static final long START_TIME = 123456789012l;

    private static final String START_TIME_AS_STRING = new SimpleDateFormat(MultiDataSetArchivingFinalizer.TIME_STAMP_FORMAT).format(START_TIME);

    private static final String USER_ID = "test-user";

    private static final String USER_EMAIL = "a@bc.de";

    private BufferedAppender logRecorder;

    private Mockery context;

    private IDataStoreServiceInternal dssService;

    private Map<String, String> parameterBindings;

    private DataSetProcessingContext processingContext;

    private IDataSetDeleter dataSetDeleter;

    private List<DataSetCodesWithStatus> updatedStatus;

    private File dataFileInArchive;

    private File dataFileReplicated;

    private File dataFilePartiallyReplicated;

    private IEncapsulatedOpenBISService openBISService;

    private IMultiDataSetArchiverDBTransaction transaction;

    private MockCleaner cleaner;

    private File pauseFile;

    private IMultiDataSetArchiverReadonlyQueryDAO queryDAO;

    @BeforeMethod
    public void setUpTestEnvironment()
    {
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.INFO, "OPERATION.*");
        context = new Mockery();
        BeanFactory beanFactory = context.mock(BeanFactory.class);
        ServiceProviderTestWrapper.setApplicationContext(beanFactory);
        dssService = ServiceProviderTestWrapper.mock(context, IDataStoreServiceInternal.class);
        openBISService = ServiceProviderTestWrapper.mock(context, IEncapsulatedOpenBISService.class);
        dataSetDeleter = context.mock(IDataSetDeleter.class);
        transaction = context.mock(IMultiDataSetArchiverDBTransaction.class);
        queryDAO = context.mock(IMultiDataSetArchiverReadonlyQueryDAO.class);
        pauseFile = new File(workingDirectory, "pause");
        File archive = new File(workingDirectory, "archive");
        archive.mkdirs();
        dataFileInArchive = new File(archive, "data.txt");
        FileUtilities.writeToFile(dataFileInArchive, "hello archive");
        File replicate = new File(workingDirectory, "replicate");
        replicate.mkdirs();
        dataFileReplicated = new File(replicate, "data.txt");
        FileUtilities.writeToFile(dataFileReplicated, "hello archive");
        dataFilePartiallyReplicated = new File(replicate, "data2.txt");
        FileUtilities.writeToFile(dataFilePartiallyReplicated, "hello");
        parameterBindings = new LinkedHashMap<String, String>();
        parameterBindings.put(MultiDataSetArchivingFinalizer.CONTAINER_PATH_KEY, "data.txt");
        parameterBindings.put(MultiDataSetArchivingFinalizer.ORIGINAL_FILE_PATH_KEY, dataFileInArchive.getPath());
        parameterBindings.put(MultiDataSetArchivingFinalizer.REPLICATED_FILE_PATH_KEY, dataFileReplicated.getPath());
        parameterBindings.put(MultiDataSetArchivingFinalizer.START_TIME_KEY, START_TIME_AS_STRING);
        parameterBindings.put(MultiDataSetArchiver.WAIT_FOR_SANITY_CHECK_KEY, Boolean.toString(MultiDataSetArchiver.DEFAULT_WAIT_FOR_SANITY_CHECK));
        parameterBindings.put(MultiDataSetArchiver.WAIT_FOR_SANITY_CHECK_INITIAL_WAITING_TIME_KEY,
                Long.toString(MultiDataSetArchiver.DEFAULT_WAIT_FOR_SANITY_CHECK_INITIAL_WAITING_TIME));
        parameterBindings.put(MultiDataSetArchiver.WAIT_FOR_SANITY_CHECK_MAX_WAITING_TIME_KEY,
                Long.toString(MultiDataSetArchiver.DEFAULT_WAIT_FOR_SANITY_CHECK_MAX_WAITING_TIME));
        parameterBindings.put(MultiDataSetArchivingFinalizer.FINALIZER_POLLING_TIME_KEY, "20000");
        parameterBindings.put(MultiDataSetArchivingFinalizer.FINALIZER_MAX_WAITING_TIME_KEY, "300000");
        parameterBindings.put(MultiDataSetArchivingFinalizer.FINALIZER_WAIT_FOR_T_FLAG_KEY,
                Boolean.toString(MultiDataSetArchiver.DEFAULT_FINALIZER_WAIT_FOR_T_FLAG));
        parameterBindings.put(MultiDataSetArchivingFinalizer.FINALIZER_SANITY_CHECK_KEY, "false");
        parameterBindings.put(MultiDataSetArchivingFinalizer.STATUS_KEY, DataSetArchivingStatus.ARCHIVED.toString());
        parameterBindings.put(Constants.SUB_DIR_KEY, MY_GROUP);
        processingContext = new DataSetProcessingContext(null, null,
                parameterBindings, null, USER_ID, USER_EMAIL);
        updatedStatus = new ArrayList<DataSetCodesWithStatus>();
        cleaner = new MockCleaner();
        context.checking(new Expectations()
        {
            {
                allowing(dssService).getDataSetDeleter();
                will(returnValue(dataSetDeleter));
            }
        });
    }

    @AfterMethod
    public void checkMockExpectations(ITestResult result)
    {
        ServiceProviderTestWrapper.restoreApplicationContext();
        if (result.getStatus() == ITestResult.FAILURE)
        {
            String logContent = logRecorder.getLogContent();
            fail(result.getName() + " failed. Log content:\n" + logContent);
        }
        logRecorder.reset();
        // To following line of code should also be called at the end of each test method.
        // Otherwise one does not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testReplicationFailDueToMissingArchiveFile()
    {
        final DatasetDescription ds1 = new DatasetDescriptionBuilder("ds1").getDatasetDescription();
        final RecordingMatcher<Map<String, String>> optionsMatcher = new RecordingMatcher<Map<String, String>>();

        dataFileInArchive.delete();

        context.checking(new Expectations()
            {
                {
                    one(transaction).deleteContainer(dataFileInArchive.getName());
                    one(transaction).commit();
                    one(transaction).close();
                    one(openBISService).archiveDataSets(with(Arrays.asList(ds1.getDataSetCode())), with(true),
                            with(optionsMatcher));
                }
            });

        ProcessingStatus status = createFinalizer().process(Arrays.asList(ds1), processingContext);

        AssertionUtil.assertContainsLines("INFO  OPERATION.MultiDataSetArchivingFinalizer - "
                        + "Parameters: {container-path=" + dataFileInArchive.getName() + ", original-file-path=" + dataFileInArchive.getPath()
                        + ", replicated-file-path=" + dataFileReplicated.getPath() + ", "
                        + "start-time=" + START_TIME_AS_STRING + ", "
                        + "wait-for-sanity-check=false, wait-for-sanity-check-initial-waiting-time=10000, wait-for-sanity-check-max-waiting-time=1800000, "
                        + "finalizer-polling-time=20000, finalizer-max-waiting-time=300000, finalizer-wait-for-t-flag=false, finalizer-sanity-check=false, status=ARCHIVED, sub-directory=my-group}\n"
                        + "ERROR OPERATION.MultiDataSetArchivingFinalizer - Replication of "
                        + "'" + dataFileInArchive.getPath() + "' failed because the original file does not exist.",
                logRecorder.getLogContent());
        assertEquals("ERROR: \"Replication of '" + dataFileInArchive.getPath() + "' failed because the original file does not exist.\"",
                status.tryGetStatusByDataset(ds1.getDataSetCode()).toString());
        assertEquals("[[ds1] - AVAILABLE]", updatedStatus.toString());
        assertEquals(false, updatedStatus.get(0).isPresentInArchive());
        assertEquals(Arrays.asList(dataFileInArchive, dataFileReplicated).toString(), cleaner.toString());
        assertEquals("{" + Constants.SUB_DIR_KEY + "=" + MY_GROUP + "}", optionsMatcher.recordedObject().toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testReplicationForArchiving()
    {
        DatasetDescription ds1 = new DatasetDescriptionBuilder("ds1").getDatasetDescription();
        prepareScheduleDeletion(ds1);
        prepareMultiDataArchiveDatabase(42, dataFileInArchive.getName(), ds1.getDataSetCode());

        ProcessingStatus status = createFinalizer().process(Arrays.asList(ds1), processingContext);

        AssertionUtil.assertContainsLines("INFO  OPERATION.MultiDataSetArchivingFinalizer - "
                        + "Parameters: {container-path=data.txt, original-file-path=" + dataFileInArchive.getPath()
                        + ", replicated-file-path=" + dataFileReplicated.getPath() + ", "
                        + "start-time=" + START_TIME_AS_STRING + ", "
                        + "wait-for-sanity-check=false, wait-for-sanity-check-initial-waiting-time=10000, wait-for-sanity-check-max-waiting-time=1800000, "
                        + "finalizer-polling-time=20000, finalizer-max-waiting-time=300000, finalizer-wait-for-t-flag=false, finalizer-sanity-check=false, status=ARCHIVED, sub-directory=my-group, container-id=42}\n"
                        + "INFO  OPERATION.MultiDataSetArchivingFinalizer - Waiting for replication of archive "
                        + "'" + dataFileInArchive.getPath() + "' containing the following data sets: [ds1]\n"
                        + "INFO  OPERATION.MultiDataSetArchivingFinalizer - Condition fulfilled after < 1sec, condition: "
                        + "13 bytes of 13 bytes are replicated for " + dataFileInArchive,
                logRecorder.getLogContent());
        assertEquals("OK", status.tryGetStatusByDataset(ds1.getDataSetCode()).toString());
        assertEquals("[[ds1] - ARCHIVED]", updatedStatus.toString());
        assertEquals(true, updatedStatus.get(0).isPresentInArchive());
        assertEquals("[]", cleaner.toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testReplicationForArchivingMissingContainerInDb()
    {
        DatasetDescription ds1 = new DatasetDescriptionBuilder("ds1").getDatasetDescription();
        prepareMultiDataArchiveDatabase(42, null, ds1.getDataSetCode());
        RecordingMatcher<Map<String, String>> optionsMatcher = prepareDeletionAndReArchiving(42L, ds1);

        ProcessingStatus status = createFinalizer().process(Arrays.asList(ds1), processingContext);

        AssertionUtil.assertContainsLines("INFO  OPERATION.MultiDataSetArchivingFinalizer - "
                        + "Parameters: {container-path=data.txt, original-file-path=" + dataFileInArchive.getPath()
                        + ", replicated-file-path=" + dataFileReplicated.getPath() + ", "
                        + "start-time=" + START_TIME_AS_STRING + ", "
                        + "wait-for-sanity-check=false, wait-for-sanity-check-initial-waiting-time=10000, wait-for-sanity-check-max-waiting-time=1800000, "
                        + "finalizer-polling-time=20000, finalizer-max-waiting-time=300000, finalizer-wait-for-t-flag=false, finalizer-sanity-check=false, status=ARCHIVED, sub-directory=my-group, container-id=42}\n"
                        + "INFO  OPERATION.MultiDataSetArchivingFinalizer - Waiting for replication of archive "
                        + "'" + dataFileInArchive.getPath() + "' containing the following data sets: [ds1]\n"
                        + "INFO  OPERATION.MultiDataSetArchivingFinalizer - Condition fulfilled after < 1sec, condition: "
                        + "13 bytes of 13 bytes are replicated for " + dataFileInArchive + "\n"
                        + "WARN  OPERATION.MultiDataSetArchivingFinalizer - "
                        + "No container found in Multi Data Set Archive database with container ID 42.\n",
                logRecorder.getLogContent());
        assertEquals("ERROR: \"Replication of '" + dataFileInArchive.getPath() + "' failed.\"",
                status.tryGetStatusByDataset(ds1.getDataSetCode()).toString());
        assertEquals("[[ds1] - AVAILABLE]", updatedStatus.toString());
        assertEquals(false, updatedStatus.get(0).isPresentInArchive());
        assertEquals(Arrays.asList(dataFileInArchive, dataFileReplicated).toString(), cleaner.toString());
        assertEquals("{" + Constants.SUB_DIR_KEY + "=" + MY_GROUP + "}", optionsMatcher.recordedObject().toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testReplicationForArchivingWrongContainerPathInDb()
    {
        DatasetDescription ds1 = new DatasetDescriptionBuilder("ds1").getDatasetDescription();
        prepareMultiDataArchiveDatabase(42, "wrong/path", ds1.getDataSetCode());
        RecordingMatcher<Map<String, String>> optionsMatcher = prepareDeletionAndReArchiving(42L, ds1);
        
        ProcessingStatus status = createFinalizer().process(Arrays.asList(ds1), processingContext);
        
        AssertionUtil.assertContainsLines("INFO  OPERATION.MultiDataSetArchivingFinalizer - "
                        + "Parameters: {container-path=data.txt, original-file-path=" + dataFileInArchive.getPath()
                        + ", replicated-file-path=" + dataFileReplicated.getPath() + ", "
                        + "start-time=" + START_TIME_AS_STRING + ", "
                        + "wait-for-sanity-check=false, wait-for-sanity-check-initial-waiting-time=10000, wait-for-sanity-check-max-waiting-time=1800000, "
                        + "finalizer-polling-time=20000, finalizer-max-waiting-time=300000, finalizer-wait-for-t-flag=false, finalizer-sanity-check=false, status=ARCHIVED, sub-directory=my-group, container-id=42}\n"
                        + "INFO  OPERATION.MultiDataSetArchivingFinalizer - Waiting for replication of archive "
                        + "'" + dataFileInArchive.getPath() + "' containing the following data sets: [ds1]\n"
                        + "INFO  OPERATION.MultiDataSetArchivingFinalizer - Condition fulfilled after < 1sec, condition: "
                        + "13 bytes of 13 bytes are replicated for " + dataFileInArchive + "\n"
                        + "WARN  OPERATION.MultiDataSetArchivingFinalizer - "
                        + "Archive file '" + dataFileInArchive.getPath() + "' doesn't ends with 'wrong/path'.\n",
                logRecorder.getLogContent());
        assertEquals("ERROR: \"Replication of '" + dataFileInArchive.getPath() + "' failed.\"",
                status.tryGetStatusByDataset(ds1.getDataSetCode()).toString());
        assertEquals("[[ds1] - AVAILABLE]", updatedStatus.toString());
        assertEquals(false, updatedStatus.get(0).isPresentInArchive());
        assertEquals(Arrays.asList(dataFileInArchive, dataFileReplicated).toString(), cleaner.toString());
        assertEquals("{" + Constants.SUB_DIR_KEY + "=" + MY_GROUP + "}", optionsMatcher.recordedObject().toString());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testReplicationForArchivingWithUnknown()
    {
        DatasetDescription ds1 = new DatasetDescriptionBuilder("ds1").getDatasetDescription();
        prepareMultiDataArchiveDatabase(42, dataFileInArchive.getName(), "unknown");
        RecordingMatcher<Map<String, String>> optionsMatcher = prepareDeletionAndReArchiving(42L, ds1);
        
        ProcessingStatus status = createFinalizer().process(Arrays.asList(ds1), processingContext);
        
        AssertionUtil.assertContainsLines("INFO  OPERATION.MultiDataSetArchivingFinalizer - "
                        + "Parameters: {container-path=data.txt, original-file-path=" + dataFileInArchive.getPath()
                        + ", replicated-file-path=" + dataFileReplicated.getPath() + ", "
                        + "start-time=" + START_TIME_AS_STRING + ", "
                        + "wait-for-sanity-check=false, wait-for-sanity-check-initial-waiting-time=10000, wait-for-sanity-check-max-waiting-time=1800000, "
                        + "finalizer-polling-time=20000, finalizer-max-waiting-time=300000, finalizer-wait-for-t-flag=false, finalizer-sanity-check=false, status=ARCHIVED, sub-directory=my-group, container-id=42}\n"
                        + "INFO  OPERATION.MultiDataSetArchivingFinalizer - Waiting for replication of archive "
                        + "'" + dataFileInArchive.getPath() + "' containing the following data sets: [ds1]\n"
                        + "INFO  OPERATION.MultiDataSetArchivingFinalizer - Condition fulfilled after < 1sec, condition: "
                        + "13 bytes of 13 bytes are replicated for " + dataFileInArchive + "\n"
                        + "WARN  OPERATION.MultiDataSetArchivingFinalizer - "
                        + "Data sets in Multi Data Set Archive database are different from provided data sets: "
                        + "Provided data sets: [ds1]. Data sets in Multi Data Set Archive database: [unknown]\n",
                logRecorder.getLogContent());
        assertEquals("ERROR: \"Replication of '" + dataFileInArchive.getPath() + "' failed.\"",
                status.tryGetStatusByDataset(ds1.getDataSetCode()).toString());
        assertEquals("[[ds1] - AVAILABLE]", updatedStatus.toString());
        assertEquals(false, updatedStatus.get(0).isPresentInArchive());
        assertEquals(Arrays.asList(dataFileInArchive, dataFileReplicated).toString(), cleaner.toString());
        assertEquals("{" + Constants.SUB_DIR_KEY + "=" + MY_GROUP + "}", optionsMatcher.recordedObject().toString());
        context.assertIsSatisfied();
    }
    
    private RecordingMatcher<Map<String, String>> prepareDeletionAndReArchiving(long containerId, DatasetDescription dataSet)
    {
        RecordingMatcher<Map<String, String>> optionsMatcher = new RecordingMatcher<Map<String, String>>();
        context.checking(new Expectations()
            {
                {
                    one(transaction).deleteContainer(containerId);
                    one(transaction).commit();
                    one(transaction).close();
                    one(openBISService).archiveDataSets(with(Arrays.asList(dataSet.getDataSetCode())), with(true),
                            with(optionsMatcher));
                }
            });
        return optionsMatcher;
    }

    @Test
    public void testReplicationForAddToArchiv()
    {
        DatasetDescription ds1 = new DatasetDescriptionBuilder("ds1").getDatasetDescription();
        parameterBindings.put(MultiDataSetArchivingFinalizer.STATUS_KEY, DataSetArchivingStatus.AVAILABLE.toString());
        prepareMultiDataArchiveDatabase(42, dataFileInArchive.getName(), ds1.getDataSetCode());

        ProcessingStatus status = createFinalizer().process(Arrays.asList(ds1), processingContext);

        AssertionUtil.assertContainsLines("INFO  OPERATION.MultiDataSetArchivingFinalizer - "
                        + "Parameters: {container-path=data.txt, original-file-path=" + dataFileInArchive.getPath()
                        + ", replicated-file-path=" + dataFileReplicated.getPath() + ", "
                        + "start-time=" + START_TIME_AS_STRING + ", "
                        + "wait-for-sanity-check=false, wait-for-sanity-check-initial-waiting-time=10000, wait-for-sanity-check-max-waiting-time=1800000, "
                        + "finalizer-polling-time=20000, finalizer-max-waiting-time=300000, finalizer-wait-for-t-flag=false, finalizer-sanity-check=false, status=AVAILABLE, sub-directory=my-group, container-id=42}\n"
                        + "INFO  OPERATION.MultiDataSetArchivingFinalizer - Waiting for replication of archive "
                        + "'" + dataFileInArchive.getPath() + "' containing the following data sets: [ds1]\n"
                        + "INFO  OPERATION.MultiDataSetArchivingFinalizer - Condition fulfilled after < 1sec, condition: "
                        + "13 bytes of 13 bytes are replicated for " + dataFileInArchive,
                logRecorder.getLogContent());
        assertEquals("OK", status.tryGetStatusByDataset(ds1.getDataSetCode()).toString());
        assertEquals("[[ds1] - AVAILABLE]", updatedStatus.toString());
        assertEquals(true, updatedStatus.get(0).isPresentInArchive());
        assertEquals("[]", cleaner.toString());
        context.assertIsSatisfied();
    }

    private void prepareMultiDataArchiveDatabase(long containerId, String containerPathOrNull, String... dataSetCodes)
    {
        parameterBindings.put(MultiDataSetArchivingFinalizer.CONTAINER_ID_KEY, Long.toString(containerId));
        List<MultiDataSetArchiverDataSetDTO> dataSets = Arrays.asList(dataSetCodes).stream()
                .map(c -> new MultiDataSetArchiverDataSetDTO(1L, c, containerId, 1L)).collect(Collectors.toList());
        context.checking(new Expectations()
            {
                {
                    allowing(queryDAO).getContainerForId(containerId);
                    if (containerPathOrNull != null)
                    {
                        will(returnValue(new MultiDataSetArchiverContainerDTO(containerId, containerPathOrNull)));
                    }
                    allowing(queryDAO).listDataSetsForContainerId(containerId);
                    will(returnValue(dataSets));
                }
            });
    }

    @Test
    public void testReplicationFailedForArchivingWithPause() throws IOException
    {
        final DatasetDescription ds1 = new DatasetDescriptionBuilder("ds1").getDatasetDescription();
        final DatasetDescription ds2 = new DatasetDescriptionBuilder("ds2").getDatasetDescription();
        final RecordingMatcher<Map<String, String>> optionsMatcher = new RecordingMatcher<Map<String, String>>();
        parameterBindings.put(MultiDataSetArchivingFinalizer.REPLICATED_FILE_PATH_KEY, dataFilePartiallyReplicated.getPath());
        context.checking(new Expectations()
            {
                {
                    one(transaction).deleteContainer(dataFileInArchive.getName());
                    one(transaction).commit();
                    one(transaction).close();
                    one(openBISService).archiveDataSets(with(Arrays.asList(ds1.getDataSetCode(), ds2.getDataSetCode())),
                            with(true), with(optionsMatcher));
                }
            });
        MockTimeProviderWithActions timeProviderWithActions = new MockTimeProviderWithActions(START_TIME, 1000);
        timeProviderWithActions.bindAction(START_TIME + 24000, new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createPauseFile();
                }
            });
        timeProviderWithActions.bindActionAfter(START_TIME + 50000, new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    pauseFile.delete();
                }
            });
        MultiDataSetArchivingFinalizer finalizer = createFinalizer(timeProviderWithActions);

        ProcessingStatus status = finalizer.process(Arrays.asList(ds1, ds2), processingContext);

        AssertionUtil.assertContainsLines("INFO  OPERATION.MultiDataSetArchivingFinalizer - "
                + "Parameters: {container-path=data.txt, original-file-path=" + dataFileInArchive.getPath()
                + ", replicated-file-path=" + dataFilePartiallyReplicated.getPath() + ", "
                + "start-time=" + START_TIME_AS_STRING + ", "
                + "wait-for-sanity-check=false, wait-for-sanity-check-initial-waiting-time=10000, wait-for-sanity-check-max-waiting-time=1800000, "
                + "finalizer-polling-time=20000, finalizer-max-waiting-time=300000, finalizer-wait-for-t-flag=false, finalizer-sanity-check=false, status=ARCHIVED, sub-directory=my-group}\n"
                + "INFO  OPERATION.MultiDataSetArchivingFinalizer - Waiting for replication of archive "
                + "'" + dataFileInArchive.getPath() + "' containing the following data sets: [ds1, ds2]\n"
                + "INFO  OPERATION.MultiDataSetArchivingFinalizer - Condition still not fulfilled after < 1sec, "
                + "condition: 5 bytes of 13 bytes are replicated for " + dataFileInArchive + "\n"
                + "INFO  OPERATION.MultiDataSetArchivingFinalizer - Condition still not fulfilled after 1sec, "
                + "condition: Pause file '" + pauseFile + "' is present. "
                + "The following action is paused: Waiting for replicated file " + dataFilePartiallyReplicated + "\n"
                + "INFO  OPERATION.MultiDataSetArchivingFinalizer - Condition fulfilled after 12sec, "
                + "condition: Pause file '" + pauseFile + "' has been removed. "
                + "The following action continues: Waiting for replicated file " + dataFilePartiallyReplicated + "\n"
                + "INFO  OPERATION.MultiDataSetArchivingFinalizer - Condition still not fulfilled after 2min, "
                + "condition: 5 bytes of 13 bytes are replicated for " + dataFileInArchive + "\n"
                + "INFO  OPERATION.MultiDataSetArchivingFinalizer - Condition still not fulfilled after 5min, "
                + "condition: 5 bytes of 13 bytes are replicated for " + dataFileInArchive + "\n"
                + "ERROR OPERATION.MultiDataSetArchivingFinalizer - Replication of '"
                + dataFileInArchive.getPath() + "' failed.", logRecorder.getLogContent());
        assertEquals("ERROR: \"Replication of '" + dataFileInArchive.getPath() + "' failed.\"",
                status.tryGetStatusByDataset(ds1.getDataSetCode()).toString());
        assertEquals("ERROR: \"Replication of '" + dataFileInArchive.getPath() + "' failed.\"",
                status.tryGetStatusByDataset(ds2.getDataSetCode()).toString());
        assertEquals("[[ds1, ds2] - AVAILABLE]", updatedStatus.toString());
        assertEquals(false, updatedStatus.get(0).isPresentInArchive());
        assertEquals(Arrays.asList(dataFileInArchive, dataFilePartiallyReplicated).toString(), cleaner.toString());
        assertEquals("{" + Constants.SUB_DIR_KEY + "=" + MY_GROUP + "}", optionsMatcher.recordedObject().toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testReplicationFailedForAddToArchive()
    {
        final DatasetDescription ds1 = new DatasetDescriptionBuilder("ds1").getDatasetDescription();
        final DatasetDescription ds2 = new DatasetDescriptionBuilder("ds2").getDatasetDescription();
        final RecordingMatcher<Map<String, String>> optionsMatcher = new RecordingMatcher<Map<String, String>>();
        parameterBindings.put(MultiDataSetArchivingFinalizer.REPLICATED_FILE_PATH_KEY, dataFilePartiallyReplicated.getPath());
        parameterBindings.put(MultiDataSetArchivingFinalizer.STATUS_KEY, DataSetArchivingStatus.AVAILABLE.toString());
        context.checking(new Expectations()
            {
                {
                    one(transaction).deleteContainer(dataFileInArchive.getName());
                    one(transaction).commit();
                    one(transaction).close();
                    one(openBISService).archiveDataSets(with(Arrays.asList(ds1.getDataSetCode(), ds2.getDataSetCode())),
                            with(false), with(optionsMatcher));
                }
            });

        ProcessingStatus status = createFinalizer().process(Arrays.asList(ds1, ds2), processingContext);

        AssertionUtil.assertContainsLines("INFO  OPERATION.MultiDataSetArchivingFinalizer - "
                + "Parameters: {container-path=data.txt, original-file-path=" + dataFileInArchive.getPath()
                + ", replicated-file-path=" + dataFilePartiallyReplicated.getPath() + ", "
                + "start-time=" + START_TIME_AS_STRING + ", "
                + "wait-for-sanity-check=false, wait-for-sanity-check-initial-waiting-time=10000, wait-for-sanity-check-max-waiting-time=1800000, "
                + "finalizer-polling-time=20000, finalizer-max-waiting-time=300000, finalizer-wait-for-t-flag=false, finalizer-sanity-check=false, status=AVAILABLE, sub-directory=my-group}\n"
                + "INFO  OPERATION.MultiDataSetArchivingFinalizer - Waiting for replication of archive "
                + "'" + dataFileInArchive.getPath() + "' containing the following data sets: [ds1, ds2]\n"
                + "INFO  OPERATION.MultiDataSetArchivingFinalizer - Condition still not fulfilled after < 1sec, "
                + "condition: 5 bytes of 13 bytes are replicated for " + dataFileInArchive + "\n"
                + "INFO  OPERATION.MultiDataSetArchivingFinalizer - Condition still not fulfilled after 2min, "
                + "condition: 5 bytes of 13 bytes are replicated for " + dataFileInArchive + "\n"
                + "INFO  OPERATION.MultiDataSetArchivingFinalizer - Condition still not fulfilled after 5min, "
                + "condition: 5 bytes of 13 bytes are replicated for " + dataFileInArchive + "\n"
                + "ERROR OPERATION.MultiDataSetArchivingFinalizer - Replication of '"
                + dataFileInArchive.getPath() + "' failed.", logRecorder.getLogContent());
        assertEquals("ERROR: \"Replication of '" + dataFileInArchive.getPath() + "' failed.\"",
                status.tryGetStatusByDataset(ds1.getDataSetCode()).toString());
        assertEquals("ERROR: \"Replication of '" + dataFileInArchive.getPath() + "' failed.\"",
                status.tryGetStatusByDataset(ds2.getDataSetCode()).toString());
        assertEquals("[[ds1, ds2] - AVAILABLE]", updatedStatus.toString());
        assertEquals(false, updatedStatus.get(0).isPresentInArchive());
        assertEquals(Arrays.asList(dataFileInArchive, dataFilePartiallyReplicated).toString(), cleaner.toString());
        assertEquals("{" + Constants.SUB_DIR_KEY + "=" + MY_GROUP + "}", optionsMatcher.recordedObject().toString());
        context.assertIsSatisfied();
    }

    private void prepareScheduleDeletion(final DatasetDescription... descriptions)
    {
        context.checking(new Expectations()
            {
                {
                    one(dataSetDeleter).scheduleDeletionOfDataSets(Arrays.asList(descriptions),
                            TimingParameters.DEFAULT_MAXIMUM_RETRY_COUNT,
                            TimingParameters.DEFAULT_INTERVAL_TO_WAIT_AFTER_FAILURE_SECONDS);
                }
            });
    }

    private void createPauseFile()
    {
        try
        {
            pauseFile.createNewFile();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private MultiDataSetArchivingFinalizer createFinalizer()
    {
        return createFinalizer(new MockTimeProvider(START_TIME, 1000));
    }

    private MultiDataSetArchivingFinalizer createFinalizer(ITimeAndWaitingProvider timeProvider)
    {
        return new MultiDataSetArchivingFinalizer(null, pauseFile, 10000, timeProvider)
            {
                private static final long serialVersionUID = 1L;

                @Override
                protected void updateStatus(DataSetCodesWithStatus codesWithStatus)
                {
                    updatedStatus.add(codesWithStatus);
                }

                @Override
                IMultiDataSetArchiverDBTransaction getTransaction()
                {
                    return transaction;
                }

                @Override
                protected IMultiDataSetArchiveCleaner getCleaner()
                {
                    return cleaner;
                }

                @Override
                protected IMultiDataSetArchiverReadonlyQueryDAO getReadonlyQuery()
                {
                    return queryDAO;
                }
            };
    }

    private static final class MockTimeProviderWithActions extends MockTimeProvider
    {
        private final Map<Long, IDelegatedAction> actions = new HashMap<Long, IDelegatedAction>();

        private long afterTimeStamp;

        private IDelegatedAction afterAction;

        public MockTimeProviderWithActions(long startTime, long... timeSteps)
        {
            super(startTime, timeSteps);
        }

        void bindAction(long timeStamp, IDelegatedAction action)
        {
            actions.put(timeStamp, action);
        }

        void bindActionAfter(long timeStamp, IDelegatedAction action)
        {
            this.afterTimeStamp = timeStamp;
            this.afterAction = action;
        }

        @Override
        public long getTimeInMilliseconds()
        {
            long time = super.getTimeInMilliseconds();
            IDelegatedAction action = actions.get(time);
            if (action != null)
            {
                action.execute();
            } else if (time > afterTimeStamp)
            {
                afterAction.execute();
            }
            return time;
        }

    }

}
