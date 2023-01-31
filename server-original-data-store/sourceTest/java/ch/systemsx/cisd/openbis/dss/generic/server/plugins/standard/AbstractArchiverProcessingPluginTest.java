/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.beans.factory.BeanFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.BooleanStatus;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.logging.LogUtils;
import ch.systemsx.cisd.common.time.TimingParameters;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractArchiverProcessingPlugin.DatasetProcessingStatuses;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractArchiverProcessingPlugin.Operation;
import ch.systemsx.cisd.openbis.dss.generic.shared.ArchiverTaskContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IConfigProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDeleter;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetStatusUpdater;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataStoreServiceInternal;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ProcessingStatus;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProviderTestWrapper;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.DatasetDescriptionBuilder;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * @author Kaloyan Enimanev
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = AbstractArchiverProcessingPlugin.class)
public class AbstractArchiverProcessingPluginTest extends AbstractFileSystemTestCase
{
    interface IAbstractArchiverMethods
    {
        public DatasetProcessingStatuses doArchive(List<DatasetDescription> datasets,
                ArchiverTaskContext context, boolean removeFromDataStore);

        public DatasetProcessingStatuses doUnarchive(List<DatasetDescription> datasets,
                ArchiverTaskContext context);

        public DatasetProcessingStatuses doDeleteFromArchive(
                List<? extends IDatasetLocation> datasets);

        public BooleanStatus isDataSetSynchronizedWithArchive(DatasetDescription dataset,
                ArchiverTaskContext context);

        public BooleanStatus isDataSetPresentInArchive(DatasetDescription dataset);
    }

    private static final class SimpleArchiver implements IAbstractArchiverMethods
    {
        private final Set<String> dataSetsSynchronizedWithArchive = new LinkedHashSet<String>();

        private final Set<String> dataSetsInArchive = new LinkedHashSet<String>();

        private final Set<String> dataSetsToFailToArchive = new LinkedHashSet<String>();

        private final Set<String> dataSetsToFailToUnarchive = new LinkedHashSet<String>();

        private final Set<String> dataSetsThrowException = new LinkedHashSet<String>();

        private final List<String> dataSetsToBeArchived = new ArrayList<String>();

        private final List<String> dataSetsToBeUnarchived = new ArrayList<String>();

        private final List<String> dataSetsToBeDeletedFromArchive = new ArrayList<String>();

        SimpleArchiver dataSetsSynchronizedWithArchive(String... dataSetCodes)
        {
            dataSetsSynchronizedWithArchive.addAll(Arrays.asList(dataSetCodes));
            return this;
        }

        SimpleArchiver dataSetsInArchive(String... dataSetCodes)
        {
            dataSetsInArchive.addAll(Arrays.asList(dataSetCodes));
            return this;
        }

        SimpleArchiver dataSetsToFailToArchive(String... dataSetCodes)
        {
            dataSetsToFailToArchive.addAll(Arrays.asList(dataSetCodes));
            return this;
        }

        SimpleArchiver dataSetsToFailToUnarchive(String... dataSetCodes)
        {
            dataSetsToFailToUnarchive.addAll(Arrays.asList(dataSetCodes));
            return this;
        }

        SimpleArchiver dataSetsThrowException(String... dataSetCodes)
        {
            dataSetsThrowException.addAll(Arrays.asList(dataSetCodes));
            return this;
        }

        List<String> getSortedDataSetsToBeArchived()
        {
            Collections.sort(dataSetsToBeArchived);
            return dataSetsToBeArchived;
        }

        List<String> getSortedDataSetsToBeUnarchived()
        {
            Collections.sort(dataSetsToBeUnarchived);
            return dataSetsToBeUnarchived;
        }

        List<String> getSortedDataSetsToBeDeletedFromArchive()
        {
            Collections.sort(dataSetsToBeDeletedFromArchive);
            return dataSetsToBeDeletedFromArchive;
        }

        @Override
        public DatasetProcessingStatuses doArchive(List<DatasetDescription> datasets,
                ArchiverTaskContext context, boolean removeFromDataStore)
        {
            DatasetProcessingStatuses result = new DatasetProcessingStatuses();
            for (DatasetDescription datasetDescription : datasets)
            {
                String dataSetCode = datasetDescription.getDataSetCode();
                dataSetsToBeArchived.add(dataSetCode);
                Status status;
                if (dataSetsToFailToArchive.contains(dataSetCode))
                {
                    status = Status.createError();
                } else if (dataSetsThrowException.contains(dataSetCode))
                {
                    throw new RuntimeException("Data set " + dataSetCode + " throws exception.");
                } else
                {
                    dataSetsSynchronizedWithArchive.add(dataSetCode);
                    status = Status.OK;
                }
                result.addResult(dataSetCode, status, Operation.ARCHIVE);
            }
            return result;
        }

        @Override
        public DatasetProcessingStatuses doUnarchive(List<DatasetDescription> datasets,
                ArchiverTaskContext context)
        {
            DatasetProcessingStatuses result = new DatasetProcessingStatuses();
            for (DatasetDescription datasetDescription : datasets)
            {
                String dataSetCode = datasetDescription.getDataSetCode();
                dataSetsToBeUnarchived.add(dataSetCode);
                Status status;
                if (dataSetsToFailToUnarchive.contains(dataSetCode))
                {
                    status = Status.createError();
                } else if (dataSetsThrowException.contains(dataSetCode))
                {
                    throw new RuntimeException("Data set " + dataSetCode + " throws exception.");
                } else
                {
                    status = Status.OK;
                }
                result.addResult(dataSetCode, status, Operation.UNARCHIVE);
            }
            return result;
        }

        @Override
        public DatasetProcessingStatuses doDeleteFromArchive(
                List<? extends IDatasetLocation> datasets)
        {
            for (IDatasetLocation datasetLocation : datasets)
            {
                dataSetsToBeDeletedFromArchive.add(datasetLocation.getDataSetCode());
            }
            return null;
        }

        @Override
        public BooleanStatus isDataSetSynchronizedWithArchive(DatasetDescription dataset,
                ArchiverTaskContext context)
        {
            return isInSet(dataSetsSynchronizedWithArchive, dataset);
        }

        @Override
        public BooleanStatus isDataSetPresentInArchive(DatasetDescription dataset)
        {
            return isInSet(dataSetsInArchive, dataset);
        }

        private BooleanStatus isInSet(Set<String> set, DatasetDescription dataset)
        {
            return set.contains(dataset.getDataSetCode()) ? BooleanStatus.createTrue()
                    : BooleanStatus.createFalse();
        }
    }

    @Friend(toClasses = AbstractArchiverProcessingPlugin.class)
    private static final class MockArchiver extends AbstractArchiverProcessingPlugin implements
            IAbstractArchiverMethods
    {
        private static final long serialVersionUID = 1L;

        private final IAbstractArchiverMethods methods;

        private final String pauseFile;

        private long pollingTime;

        @Override
        public DatasetProcessingStatuses doArchive(List<DatasetDescription> datasets,
                ArchiverTaskContext context, boolean removeFromDataStore)
        {
            return methods.doArchive(datasets, context, false);
        }

        @Override
        public DatasetProcessingStatuses doUnarchive(List<DatasetDescription> datasets,
                ArchiverTaskContext context)
        {
            return methods.doUnarchive(datasets, context);
        }

        @Override
        public DatasetProcessingStatuses doDeleteFromArchive(
                List<? extends IDatasetLocation> datasets)
        {
            return methods.doDeleteFromArchive(datasets);
        }

        @Override
        public BooleanStatus isDataSetSynchronizedWithArchive(DatasetDescription dataset,
                ArchiverTaskContext context)
        {
            return methods.isDataSetSynchronizedWithArchive(dataset, context);
        }

        @Override
        public BooleanStatus isDataSetPresentInArchive(DatasetDescription dataset)
        {
            return methods.isDataSetPresentInArchive(dataset);
        }

        @Override
        protected void pauseStarting(long waitingTime)
        {
            this.pollingTime = waitingTime;
            File f = new File(pauseFile);
            if (f.exists())
            {
                f.delete();
            }
        }

        public MockArchiver(Properties properties, File storeRoot,
                IAbstractArchiverMethods methods, IStatusChecker archivePrerequisiteOrNull,
                IStatusChecker unarchivePrerequisiteOrNull)
        {
            super(properties, storeRoot, archivePrerequisiteOrNull, unarchivePrerequisiteOrNull);
            pauseFile = properties.getProperty(PAUSE_FILE_KEY);
            this.methods = methods;
        }
    }

    private static final String DATA_STORE_CODE = "DSS1";

    private IDataSetStatusUpdater statusUpdater;

    private IEncapsulatedOpenBISService service;

    private IConfigProvider configProvider;

    private BufferedAppender logRecorder;

    private Mockery context;

    private IDataStoreServiceInternal dataStoreService;

    private IDataSetDeleter dataSetDeleter;

    private IShareIdManager shareIdManager;

    private IStatusChecker statusChecker;

    private File pauseFile;

    @BeforeMethod
    public void beforeMethod()
    {
        LogInitializer.init();

        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.DEBUG);
        context = new Mockery();
        statusChecker = context.mock(IStatusChecker.class);
        statusUpdater = context.mock(IDataSetStatusUpdater.class);
        dataSetDeleter = context.mock(IDataSetDeleter.class);

        final BeanFactory beanFactory = context.mock(BeanFactory.class);
        ServiceProviderTestWrapper.setApplicationContext(beanFactory);

        service = ServiceProviderTestWrapper.mock(context, IEncapsulatedOpenBISService.class);
        configProvider = ServiceProviderTestWrapper.mock(context, IConfigProvider.class);
        dataStoreService =
                ServiceProviderTestWrapper.mock(context, IDataStoreServiceInternal.class);
        shareIdManager = ServiceProviderTestWrapper.mock(context, IShareIdManager.class);

        pauseFile = new File(workingDirectory, "pause-file");

        File dataSet1 = new File(workingDirectory, "1/ds1");
        dataSet1.mkdirs();
        FileUtilities.writeToFile(new File(dataSet1, "hello.txt"), "hello world");

        context.checking(new Expectations()
            {
                {
                    allowing(configProvider).getDataStoreCode();
                    will(returnValue(DATA_STORE_CODE));

                    allowing(dataStoreService).getDataSetDeleter();
                    will(returnValue(dataSetDeleter));
                }
            });

    }

    @AfterMethod
    public void afterMethod()
    {
        ServiceProviderTestWrapper.restoreApplicationContext();
        logRecorder.reset();
        context.assertIsSatisfied();
    }

    @Test
    public void testArchiveUnsynchronizedDataSetsSuccessfullyAndRemoveThemFromStore()
    {
        final DatasetDescription ds1 =
                new DatasetDescriptionBuilder("ds1").size(42).getDatasetDescription();
        final DatasetDescription ds2 =
                new DatasetDescriptionBuilder("ds2").size(43).getDatasetDescription();
        final ArchiverTaskContext archiverContext = new ArchiverTaskContext(null, null);
        context.checking(new Expectations()
            {
                {
                    one(statusChecker).check(2);
                    will(returnValue(Status.OK));

                    one(dataSetDeleter).scheduleDeletionOfDataSets(Arrays.asList(ds1, ds2),
                            TimingParameters.DEFAULT_MAXIMUM_RETRY_COUNT,
                            TimingParameters.DEFAULT_INTERVAL_TO_WAIT_AFTER_FAILURE_SECONDS);
                    one(statusUpdater).update(Arrays.asList("ds1", "ds2"),
                            DataSetArchivingStatus.ARCHIVED, true);
                }
            });
        SimpleArchiver simpleArchiver = new SimpleArchiver().dataSetsSynchronizedWithArchive("ds2");
        MockArchiver mockArchiver =
                new MockArchiver(new Properties(), workingDirectory, simpleArchiver, statusChecker,
                        null);
        mockArchiver.statusUpdater = statusUpdater;

        ProcessingStatus status =
                mockArchiver.archive(Arrays.asList(ds1, ds2), archiverContext, true);

        assertEquals("[ds1]", simpleArchiver.getSortedDataSetsToBeArchived().toString());
        assertEquals("[]", simpleArchiver.getSortedDataSetsToBeDeletedFromArchive().toString());
        assertEquals("[]", status.getErrorStatuses().toString());
        assertEquals(
                "INFO  OPERATION.AbstractDatastorePlugin - "
                        + "Archiving of the following datasets has been requested: [Dataset 'ds1', Dataset 'ds2']",
                logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testArchiveDataSetThrowsException()
    {
        final DatasetDescription ds1 =
                new DatasetDescriptionBuilder("ds1").size(42).getDatasetDescription();
        final DatasetDescription ds2 =
                new DatasetDescriptionBuilder("ds2").size(43).getDatasetDescription();
        final ArchiverTaskContext archiverContext = new ArchiverTaskContext(null, null);
        context.checking(new Expectations()
            {
                {
                    one(statusUpdater).update(Arrays.asList("ds1", "ds2"),
                            DataSetArchivingStatus.AVAILABLE, false);
                }
            });
        SimpleArchiver simpleArchiver = new SimpleArchiver().dataSetsThrowException("ds1");
        MockArchiver mockArchiver =
                new MockArchiver(new Properties(), workingDirectory, simpleArchiver, null, null);
        mockArchiver.statusUpdater = statusUpdater;

        ProcessingStatus status =
                mockArchiver.archive(Arrays.asList(ds1, ds2), archiverContext, true);

        assertEquals("[ds1]", simpleArchiver.getSortedDataSetsToBeArchived().toString());
        assertEquals("[]", simpleArchiver.getSortedDataSetsToBeDeletedFromArchive().toString());
        assertEquals("[ERROR: \"Archiving failed :Data set ds1 throws exception.\"]", status
                .getErrorStatuses().toString());
        assertEquals(
                "INFO  OPERATION.AbstractDatastorePlugin - "
                        + "Archiving of the following datasets has been requested: [Dataset 'ds1', Dataset 'ds2']\n"
                        + "ERROR OPERATION.AbstractDatastorePlugin - "
                        + "Archiving failed :Data set ds1 throws exception.\n"
                        + "java.lang.RuntimeException: Data set ds1 throws exception.\n"
                        + "ERROR NOTIFY.AbstractDatastorePlugin - "
                        + "Archiving for dataset ds1 finished with the status: "
                        + "ERROR: \"Archiving failed :Data set ds1 throws exception.\".\n"
                        + "ERROR NOTIFY.AbstractDatastorePlugin - "
                        + "Archiving for dataset ds2 finished with the status: "
                        + "ERROR: \"Archiving failed :Data set ds1 throws exception.\".",
                LogUtils.removeEmbeddedStackTrace(logRecorder.getLogContent()));
        context.assertIsSatisfied();
    }

    @Test
    public void testArchiveDataSetsWhichDoNotFulfillPrerequests()
    {
        final DatasetDescription ds1 =
                new DatasetDescriptionBuilder("ds1").size(42).getDatasetDescription();
        final DatasetDescription ds2 =
                new DatasetDescriptionBuilder("ds2").size(43).getDatasetDescription();
        final ArchiverTaskContext archiverContext = new ArchiverTaskContext(null, null);
        context.checking(new Expectations()
            {
                {
                    one(statusChecker).check(2);
                    will(returnValue(Status.createError()));

                    one(statusUpdater).update(Arrays.asList("ds1", "ds2"),
                            DataSetArchivingStatus.AVAILABLE, false);
                }
            });
        SimpleArchiver simpleArchiver = new SimpleArchiver().dataSetsSynchronizedWithArchive("ds2");
        MockArchiver mockArchiver =
                new MockArchiver(new Properties(), workingDirectory, simpleArchiver, statusChecker,
                        null);
        mockArchiver.statusUpdater = statusUpdater;

        ProcessingStatus status =
                mockArchiver.archive(Arrays.asList(ds1, ds2), archiverContext, true);

        assertEquals("[]", simpleArchiver.getSortedDataSetsToBeArchived().toString());
        assertEquals("[]", simpleArchiver.getSortedDataSetsToBeDeletedFromArchive().toString());
        assertEquals("[ERROR]", status.getErrorStatuses().toString());
        assertEquals("[ds1, ds2]", status.getDatasetsByStatus(Status.createError()).toString());
        assertEquals(
                "INFO  OPERATION.AbstractDatastorePlugin - "
                        + "Archiving of the following datasets has been requested: [Dataset 'ds1', Dataset 'ds2']\n"
                        + "ERROR NOTIFY.AbstractDatastorePlugin - "
                        + "Archiving for dataset ds1 finished with the status: ERROR.\n"
                        + "ERROR NOTIFY.AbstractDatastorePlugin - "
                        + "Archiving for dataset ds2 finished with the status: ERROR.",
                logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testArchiveDataSetWithUnknownSizeAndPause() throws Exception
    {
        final DatasetDescription ds1 =
                new DatasetDescriptionBuilder("ds1").location("ds1").getDatasetDescription();
        final ArchiverTaskContext archiverContext = new ArchiverTaskContext(null, null);
        context.checking(new Expectations()
            {
                {
                    one(shareIdManager).getShareId(ds1.getDataSetCode());
                    will(returnValue("1"));

                    one(service).updateShareIdAndSize(ds1.getDataSetCode(), "1", 11L);

                    one(dataSetDeleter).scheduleDeletionOfDataSets(Arrays.asList(ds1),
                            TimingParameters.DEFAULT_MAXIMUM_RETRY_COUNT,
                            TimingParameters.DEFAULT_INTERVAL_TO_WAIT_AFTER_FAILURE_SECONDS);
                    one(statusUpdater).update(Arrays.asList("ds1"),
                            DataSetArchivingStatus.ARCHIVED, true);
                }
            });
        SimpleArchiver simpleArchiver = new SimpleArchiver();
        Properties properties = new Properties();
        properties.setProperty(AbstractArchiverProcessingPlugin.PAUSE_FILE_KEY, pauseFile.getAbsolutePath());
        properties.setProperty(AbstractArchiverProcessingPlugin.PAUSE_FILE_POLLING_TIME_KEY, "3 min");
        MockArchiver mockArchiver =
                new MockArchiver(properties, workingDirectory, simpleArchiver, null, null);
        mockArchiver.statusUpdater = statusUpdater;
        pauseFile.createNewFile();

        ProcessingStatus status = mockArchiver.archive(Arrays.asList(ds1), archiverContext, true);

        assertEquals("[ds1]", simpleArchiver.getSortedDataSetsToBeArchived().toString());
        assertEquals("[]", simpleArchiver.getSortedDataSetsToBeDeletedFromArchive().toString());
        assertEquals("[]", status.getErrorStatuses().toString());
        assertEquals("[]", status.getDatasetsByStatus(Status.createError()).toString());
        assertEquals("INFO  OPERATION.AbstractDatastorePlugin - "
                + "Archiving of the following datasets has been requested: [Dataset 'ds1']\n"
                + "INFO  OPERATION.AbstractDatastorePlugin - Presence of pause file '"
                + pauseFile.getAbsolutePath()
                + "' prevents starting archiving the following data sets: [Dataset 'ds1']",
                logRecorder.getLogContent());
        assertEquals(180000, mockArchiver.pollingTime);
        context.assertIsSatisfied();
    }

    @Test
    public void testArchiveUnsynchronizedDataSetsSuccessfullyButDoNotRemoveThemFromStore()
    {
        final DatasetDescription ds1 =
                new DatasetDescriptionBuilder("ds1").size(42).getDatasetDescription();
        final DatasetDescription ds2 =
                new DatasetDescriptionBuilder("ds2").size(43).getDatasetDescription();
        final ArchiverTaskContext archiverContext = new ArchiverTaskContext(null, null);
        context.checking(new Expectations()
            {
                {
                    one(statusUpdater).update(Arrays.asList("ds1", "ds2"),
                            DataSetArchivingStatus.AVAILABLE, true);
                }
            });
        SimpleArchiver simpleArchiver = new SimpleArchiver().dataSetsSynchronizedWithArchive("ds2");
        MockArchiver mockArchiver =
                new MockArchiver(new Properties(), workingDirectory, simpleArchiver, null, null);
        mockArchiver.statusUpdater = statusUpdater;

        ProcessingStatus status =
                mockArchiver.archive(Arrays.asList(ds1, ds2), archiverContext, false);

        assertEquals("[ds1]", simpleArchiver.getSortedDataSetsToBeArchived().toString());
        assertEquals("[]", simpleArchiver.getSortedDataSetsToBeDeletedFromArchive().toString());
        assertEquals("[]", status.getErrorStatuses().toString());
        assertEquals(
                "INFO  OPERATION.AbstractDatastorePlugin - "
                        + "Archiving of the following datasets has been requested: [Dataset 'ds1', Dataset 'ds2']",
                logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testArchiveUnarchivedDataSetsSuccessfullyAndRemoveThemFromStore()
    {
        final DatasetDescription ds1 =
                new DatasetDescriptionBuilder("ds1").size(42).getDatasetDescription();
        final DatasetDescription ds2 =
                new DatasetDescriptionBuilder("ds2").size(43).getDatasetDescription();
        final ArchiverTaskContext archiverContext = new ArchiverTaskContext(null, null);
        context.checking(new Expectations()
            {
                {
                    one(dataSetDeleter).scheduleDeletionOfDataSets(Arrays.asList(ds1, ds2),
                            TimingParameters.DEFAULT_MAXIMUM_RETRY_COUNT,
                            TimingParameters.DEFAULT_INTERVAL_TO_WAIT_AFTER_FAILURE_SECONDS);
                    one(statusUpdater).update(Arrays.asList("ds1", "ds2"),
                            DataSetArchivingStatus.ARCHIVED, true);
                }
            });
        SimpleArchiver simpleArchiver = new SimpleArchiver().dataSetsInArchive("ds2");
        Properties properties = new Properties();
        properties.setProperty(AbstractArchiverProcessingPlugin.SYNCHRONIZE_ARCHIVE, "false");
        MockArchiver mockArchiver =
                new MockArchiver(properties, workingDirectory, simpleArchiver, null, null);
        mockArchiver.statusUpdater = statusUpdater;

        ProcessingStatus status =
                mockArchiver.archive(Arrays.asList(ds1, ds2), archiverContext, true);

        assertEquals("[ds1]", simpleArchiver.getSortedDataSetsToBeArchived().toString());
        assertEquals("[]", simpleArchiver.getSortedDataSetsToBeDeletedFromArchive().toString());
        assertEquals("[]", status.getErrorStatuses().toString());
        assertEquals(
                "INFO  OPERATION.AbstractDatastorePlugin - "
                        + "Archiving of the following datasets has been requested: [Dataset 'ds1', Dataset 'ds2']",
                logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testArchiveDataSetsSomeSuccessfullySomeNot()
    {
        final DatasetDescription ds1 =
                new DatasetDescriptionBuilder("ds1").size(42).getDatasetDescription();
        final DatasetDescription ds2 =
                new DatasetDescriptionBuilder("ds2").size(43).getDatasetDescription();
        final ArchiverTaskContext archiverContext = new ArchiverTaskContext(null, null);
        context.checking(new Expectations()
            {
                {
                    one(dataSetDeleter).scheduleDeletionOfDataSets(Arrays.asList(ds1),
                            TimingParameters.DEFAULT_MAXIMUM_RETRY_COUNT,
                            TimingParameters.DEFAULT_INTERVAL_TO_WAIT_AFTER_FAILURE_SECONDS);
                    one(statusUpdater).update(Arrays.asList("ds1"),
                            DataSetArchivingStatus.ARCHIVED, true);
                    one(statusUpdater).update(Arrays.asList("ds2"),
                            DataSetArchivingStatus.AVAILABLE, false);
                }
            });
        SimpleArchiver simpleArchiver = new SimpleArchiver().dataSetsToFailToArchive("ds2");
        MockArchiver mockArchiver =
                new MockArchiver(new Properties(), workingDirectory, simpleArchiver, null, null);
        mockArchiver.statusUpdater = statusUpdater;

        ProcessingStatus status =
                mockArchiver.archive(Arrays.asList(ds1, ds2), archiverContext, true);

        assertEquals("[ds1, ds2]", simpleArchiver.getSortedDataSetsToBeArchived().toString());
        assertEquals("[ds2]", simpleArchiver.getSortedDataSetsToBeDeletedFromArchive().toString());
        assertEquals("[ERROR]", status.getErrorStatuses().toString());
        assertEquals("[ds1]", status.getDatasetsByStatus(Status.OK).toString());
        assertEquals("[ds2]", status.getDatasetsByStatus(Status.createError()).toString());
        assertEquals(
                "INFO  OPERATION.AbstractDatastorePlugin - "
                        + "Archiving of the following datasets has been requested: [Dataset 'ds1', Dataset 'ds2']\n"
                        + "ERROR NOTIFY.AbstractDatastorePlugin - "
                        + "Archiving for dataset ds2 finished with the status: ERROR.",
                logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testArchiveAnUnarchivedDataSetFailing()
    {
        final DatasetDescription ds1 =
                new DatasetDescriptionBuilder("ds1").size(42).getDatasetDescription();
        final ArchiverTaskContext archiverContext = new ArchiverTaskContext(null, null);
        context.checking(new Expectations()
            {
                {
                    one(statusUpdater).update(Arrays.asList("ds1"),
                            DataSetArchivingStatus.AVAILABLE, false);
                }
            });
        SimpleArchiver simpleArchiver = new SimpleArchiver().dataSetsToFailToArchive("ds1");
        MockArchiver mockArchiver =
                new MockArchiver(new Properties(), workingDirectory, simpleArchiver, null, null);
        mockArchiver.statusUpdater = statusUpdater;

        ProcessingStatus status = mockArchiver.archive(Arrays.asList(ds1), archiverContext, false);

        assertEquals("[ds1]", simpleArchiver.getSortedDataSetsToBeArchived().toString());
        assertEquals("[ds1]", simpleArchiver.getSortedDataSetsToBeDeletedFromArchive().toString());
        assertEquals("[ERROR]", status.getErrorStatuses().toString());
        assertEquals("INFO  OPERATION.AbstractDatastorePlugin - "
                + "Archiving of the following datasets has been requested: [Dataset 'ds1']\n"
                + "ERROR NOTIFY.AbstractDatastorePlugin - "
                + "Archiving for dataset ds1 finished with the status: ERROR.",
                logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testArchiveAnUnarchivedDataSetAndRemoveItFromStoreFailing()
    {
        final DatasetDescription ds1 =
                new DatasetDescriptionBuilder("ds1").size(42).getDatasetDescription();
        final ArchiverTaskContext archiverContext = new ArchiverTaskContext(null, null);
        context.checking(new Expectations()
            {
                {
                    one(dataSetDeleter).scheduleDeletionOfDataSets(
                            Arrays.<DatasetDescription> asList(),
                            TimingParameters.DEFAULT_MAXIMUM_RETRY_COUNT,
                            TimingParameters.DEFAULT_INTERVAL_TO_WAIT_AFTER_FAILURE_SECONDS);
                    one(statusUpdater).update(Arrays.asList("ds1"),
                            DataSetArchivingStatus.AVAILABLE, false);
                }
            });
        SimpleArchiver simpleArchiver = new SimpleArchiver().dataSetsToFailToArchive("ds1");
        MockArchiver mockArchiver =
                new MockArchiver(new Properties(), workingDirectory, simpleArchiver, null, null);
        mockArchiver.statusUpdater = statusUpdater;

        ProcessingStatus status = mockArchiver.archive(Arrays.asList(ds1), archiverContext, true);

        assertEquals("[ds1]", simpleArchiver.getSortedDataSetsToBeArchived().toString());
        assertEquals("[ds1]", simpleArchiver.getSortedDataSetsToBeDeletedFromArchive().toString());
        assertEquals("[ERROR]", status.getErrorStatuses().toString());
        assertEquals("[ds1]", status.getDatasetsByStatus(Status.createError()).toString());
        assertEquals("INFO  OPERATION.AbstractDatastorePlugin - "
                + "Archiving of the following datasets has been requested: [Dataset 'ds1']\n"
                + "ERROR NOTIFY.AbstractDatastorePlugin - "
                + "Archiving for dataset ds1 finished with the status: ERROR.",
                logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testUnarchivDataSetsSuccessfully()
    {
        final DatasetDescription ds1 =
                new DatasetDescriptionBuilder("ds1").size(42).getDatasetDescription();
        final DatasetDescription ds2 =
                new DatasetDescriptionBuilder("ds2").size(43).getDatasetDescription();
        final ArchiverTaskContext archiverContext = new ArchiverTaskContext(null, null);
        context.checking(new Expectations()
            {
                {
                    one(statusUpdater).update(Arrays.asList("ds1", "ds2"),
                            DataSetArchivingStatus.AVAILABLE, true);
                }
            });
        SimpleArchiver simpleArchiver = new SimpleArchiver().dataSetsInArchive("ds2");
        MockArchiver mockArchiver =
                new MockArchiver(new Properties(), workingDirectory, simpleArchiver, null, null);
        mockArchiver.statusUpdater = statusUpdater;

        ProcessingStatus status = mockArchiver.unarchive(Arrays.asList(ds1, ds2), archiverContext);

        assertEquals("[]", simpleArchiver.getSortedDataSetsToBeArchived().toString());
        assertEquals("[ds1, ds2]", simpleArchiver.getSortedDataSetsToBeUnarchived().toString());
        assertEquals("[]", simpleArchiver.getSortedDataSetsToBeDeletedFromArchive().toString());
        assertEquals("[]", status.getErrorStatuses().toString());
        assertEquals(
                "INFO  OPERATION.AbstractDatastorePlugin - "
                        + "Unarchiving of the following datasets has been requested: [Dataset 'ds1', Dataset 'ds2']\n"
                        + "INFO  OPERATION.AbstractDatastorePlugin - "
                        + "Obtained the list of all datasets in all shares in ?.?? s.",
                replaceTimeInfo(logRecorder.getLogContent()));
        context.assertIsSatisfied();
    }

    @Test
    public void testUnarchivDataSetsWhichDoNotFulfillPrerequests()
    {
        final DatasetDescription ds1 =
                new DatasetDescriptionBuilder("ds1").size(42).getDatasetDescription();
        final ArchiverTaskContext archiverContext = new ArchiverTaskContext(null, null);
        context.checking(new Expectations()
            {
                {
                    one(statusChecker).check(1);
                    will(returnValue(Status.createError()));

                    one(statusUpdater).update(Arrays.asList("ds1"),
                            DataSetArchivingStatus.ARCHIVED, true);
                }
            });
        SimpleArchiver simpleArchiver = new SimpleArchiver().dataSetsThrowException("ds1");
        MockArchiver mockArchiver =
                new MockArchiver(new Properties(), workingDirectory, simpleArchiver, null,
                        statusChecker);
        mockArchiver.statusUpdater = statusUpdater;

        ProcessingStatus status = mockArchiver.unarchive(Arrays.asList(ds1), archiverContext);

        assertEquals("[]", simpleArchiver.getSortedDataSetsToBeArchived().toString());
        assertEquals("[]", simpleArchiver.getSortedDataSetsToBeUnarchived().toString());
        assertEquals("[]", simpleArchiver.getSortedDataSetsToBeDeletedFromArchive().toString());
        assertEquals("[ERROR]", status.getErrorStatuses().toString());
        assertEquals("INFO  OPERATION.AbstractDatastorePlugin - "
                + "Unarchiving of the following datasets has been requested: [Dataset 'ds1']\n"
                + "ERROR NOTIFY.AbstractDatastorePlugin - "
                + "Unarchiving for dataset ds1 finished with the status: ERROR.",
                replaceTimeInfo(LogUtils.removeEmbeddedStackTrace(logRecorder.getLogContent())));
        context.assertIsSatisfied();
    }

    @Test
    public void testUnarchivDataSetFailing()
    {
        final DatasetDescription ds1 =
                new DatasetDescriptionBuilder("ds1").size(42).getDatasetDescription();
        final DatasetDescription ds2 =
                new DatasetDescriptionBuilder("ds2").size(43).getDatasetDescription();
        final ArchiverTaskContext archiverContext = new ArchiverTaskContext(null, null);
        context.checking(new Expectations()
            {
                {
                    one(statusChecker).check(2);
                    will(returnValue(Status.OK));

                    one(statusUpdater).update(Arrays.asList("ds1"),
                            DataSetArchivingStatus.ARCHIVED, true);
                    one(statusUpdater).update(Arrays.asList("ds2"),
                            DataSetArchivingStatus.AVAILABLE, true);
                }
            });
        SimpleArchiver simpleArchiver = new SimpleArchiver().dataSetsToFailToUnarchive("ds1");
        MockArchiver mockArchiver =
                new MockArchiver(new Properties(), workingDirectory, simpleArchiver, null,
                        statusChecker);
        mockArchiver.statusUpdater = statusUpdater;

        ProcessingStatus status = mockArchiver.unarchive(Arrays.asList(ds1, ds2), archiverContext);

        assertEquals("[]", simpleArchiver.getSortedDataSetsToBeArchived().toString());
        assertEquals("[ds1, ds2]", simpleArchiver.getSortedDataSetsToBeUnarchived().toString());
        assertEquals("[]", simpleArchiver.getSortedDataSetsToBeDeletedFromArchive().toString());
        assertEquals("[ERROR]", status.getErrorStatuses().toString());
        assertEquals(
                "INFO  OPERATION.AbstractDatastorePlugin - "
                        + "Unarchiving of the following datasets has been requested: [Dataset 'ds1', Dataset 'ds2']\n"
                        + "INFO  OPERATION.AbstractDatastorePlugin - "
                        + "Obtained the list of all datasets in all shares in ?.?? s.\n"
                        + "ERROR NOTIFY.AbstractDatastorePlugin - "
                        + "Unarchiving for dataset ds1 finished with the status: ERROR.",
                replaceTimeInfo(LogUtils.removeEmbeddedStackTrace(logRecorder.getLogContent())));
        context.assertIsSatisfied();
    }

    @Test
    public void testUnarchivDataSetsThrowsException()
    {
        final DatasetDescription ds1 =
                new DatasetDescriptionBuilder("ds1").size(42).getDatasetDescription();
        final ArchiverTaskContext archiverContext = new ArchiverTaskContext(null, null);
        context.checking(new Expectations()
            {
                {
                    one(statusChecker).check(1);
                    will(returnValue(Status.OK));

                    one(statusUpdater).update(Arrays.asList("ds1"),
                            DataSetArchivingStatus.ARCHIVED, true);
                }
            });
        SimpleArchiver simpleArchiver = new SimpleArchiver().dataSetsThrowException("ds1");
        MockArchiver mockArchiver =
                new MockArchiver(new Properties(), workingDirectory, simpleArchiver, null,
                        statusChecker);
        mockArchiver.statusUpdater = statusUpdater;

        ProcessingStatus status = mockArchiver.unarchive(Arrays.asList(ds1), archiverContext);

        assertEquals("[]", simpleArchiver.getSortedDataSetsToBeArchived().toString());
        assertEquals("[ds1]", simpleArchiver.getSortedDataSetsToBeUnarchived().toString());
        assertEquals("[]", simpleArchiver.getSortedDataSetsToBeDeletedFromArchive().toString());
        assertEquals("[ERROR: \"Unarchiving failed: Data set ds1 throws exception.\"]", status
                .getErrorStatuses().toString());
        assertEquals("INFO  OPERATION.AbstractDatastorePlugin - "
                + "Unarchiving of the following datasets has been requested: [Dataset 'ds1']\n"
                + "INFO  OPERATION.AbstractDatastorePlugin - "
                + "Obtained the list of all datasets in all shares in ?.?? s.\n"
                + "ERROR OPERATION.AbstractDatastorePlugin - "
                + "Unarchiving failed: Data set ds1 throws exception.\n"
                + "java.lang.RuntimeException: Data set ds1 throws exception.\n"
                + "ERROR NOTIFY.AbstractDatastorePlugin - "
                + "Unarchiving for dataset ds1 finished with the status: "
                + "ERROR: \"Unarchiving failed: Data set ds1 throws exception.\".",
                replaceTimeInfo(LogUtils.removeEmbeddedStackTrace(logRecorder.getLogContent())));
        context.assertIsSatisfied();
    }

    private String replaceTimeInfo(String logContent)
    {
        return logContent.replaceAll("0\\.[0-9]{2,2} s", "?.?? s");
    }

}
