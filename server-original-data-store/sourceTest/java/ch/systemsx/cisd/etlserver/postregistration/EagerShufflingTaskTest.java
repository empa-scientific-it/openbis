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

package ch.systemsx.cisd.etlserver.postregistration;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.hamcrest.Matchers;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.etlserver.plugins.IDataSetMover;
import ch.systemsx.cisd.openbis.dss.generic.shared.IChecksumProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IConfigProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.MockFreeSpaceProvider;
import ch.systemsx.cisd.openbis.generic.shared.Constants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = EagerShufflingTask.class)
public class EagerShufflingTaskTest extends AbstractFileSystemTestCase
{
    private static final String SHARDING = "sharding/";

    private static final String DATA_STORE_SERVER_CODE = "DSS";

    private static final String DATA_SET_CODE1 = "ds-1";

    private static final String DATA_SET_CODE2 = "ds-2";

    private BufferedAppender logRecorder;

    private Mockery context;

    private IEncapsulatedOpenBISService service;

    private IShareIdManager shareIdManager;

    private MockFreeSpaceProvider freeSpaceProvider;

    private IDataSetMover dataSetMover;

    private IChecksumProvider checksumProvider;

    private IConfigProvider configProvider;

    private ISimpleLogger logger;

    private ISimpleLogger notifyer;

    private File store;

    private File share1;

    private File share2;

    private File share3;

    private File share4;

    private File ds1File;

    @BeforeMethod
    public void beforeMethod()
    {
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.INFO);
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        shareIdManager = context.mock(IShareIdManager.class);
        freeSpaceProvider = new MockFreeSpaceProvider();
        configProvider = context.mock(IConfigProvider.class);
        dataSetMover = context.mock(IDataSetMover.class);
        checksumProvider = context.mock(IChecksumProvider.class);
        logger = context.mock(ISimpleLogger.class, "logger");
        notifyer = context.mock(ISimpleLogger.class, "notifyer");
        store = new File(workingDirectory.getAbsolutePath(), "store");
        store.mkdirs();
        share1 = new File(store, "1");
        share1.mkdir();
        new File(share1, SHARDING).mkdirs();
        ds1File = new File(share1, SHARDING + DATA_SET_CODE1);
        FileUtilities.writeToFile(ds1File, "hello data set 1");
        share2 = new File(store, "2");
        share2.mkdir();
        share3 = new File(store, "3");
        share3.mkdir();
        share4 = new File(store, "4");
        share4.mkdir();
    }

    @AfterMethod
    public void afterMethod()
    {
        logRecorder.reset();
        // The following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testShufflingIntoAnExtensionShare()
    {
        prepareConfigProvider();
        Properties properties = createDefaultProperties();
        properties.setProperty(EagerShufflingTask.VERIFY_CHECKSUM_KEY, "true");
        EagerShufflingTask task = createTask(properties);
        freeSpaceProvider.setFreeSpaceValues(200, 100, 300, 400, 400, 300);
        prepareListDataSets();
        prepareGetShareId();
        RecordingMatcher<String> infoMessageMatcher = prepareLogging(LogLevel.INFO);
        context.checking(new Expectations()
            {
                {
                    one(dataSetMover).moveDataSetToAnotherShare(ds1File, share4, checksumProvider,
                            logger);
                }
            });

        IPostRegistrationTaskExecutor executor = task.createExecutor(DATA_SET_CODE1, false);
        executor.createCleanupTask();
        executor.execute();

        assertEquals("Data set ds-1 successfully moved from share 1 to 4.",
                infoMessageMatcher.recordedObject());
        assertEquals("[1, 2, 3, 4, 4, 4]", freeSpaceProvider.getShares().toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testShufflingIntoAnotherIncomingShare()
    {
        prepareConfigProvider();
        EagerShufflingTask task = createTask();
        freeSpaceProvider.setFreeSpaceValues(100, 200, 10, 0);
        prepareListDataSets();
        prepareGetShareId();
        RecordingMatcher<String> infoMessageMatcher = prepareLogging(LogLevel.INFO);
        context.checking(new Expectations()
            {
                {
                    one(dataSetMover).moveDataSetToAnotherShare(ds1File, share2, checksumProvider,
                            logger);
                }
            });

        IPostRegistrationTaskExecutor executor = task.createExecutor(DATA_SET_CODE1, false);
        executor.createCleanupTask();
        executor.execute();

        assertEquals("Data set ds-1 successfully moved from share 1 to 2.",
                infoMessageMatcher.recordedObject());
        assertEquals("[1, 2, 3, 4, 2, 2]", freeSpaceProvider.getShares().toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testShufflingButNoShareFoundExceptTheOwnOne()
    {
        prepareConfigProvider();
        EagerShufflingTask task = createTask();
        freeSpaceProvider.setFreeSpaceValues(200, 10, 10, 0);
        prepareListDataSets();

        RecordingMatcher<String> logMessageMatcher = prepareLogging(LogLevel.WARN);

        IPostRegistrationTaskExecutor executor = task.createExecutor(DATA_SET_CODE1, false);
        executor.createCleanupTask();
        executor.execute();

        assertEquals("No share found for shuffling data set ds-1.",
                logMessageMatcher.recordedObject());
        assertEquals("[1, 2, 3, 4, 1, 2, 3, 4]", freeSpaceProvider.getShares().toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testShufflingWithNotification()
    {
        prepareConfigProvider();
        Properties properties = createDefaultProperties();
        properties.setProperty(EagerShufflingTask.FREE_SPACE_LIMIT_KEY, "1");
        properties.setProperty(EagerShufflingTask.VERIFY_CHECKSUM_KEY, "false");
        EagerShufflingTask task = createTask(properties);
        freeSpaceProvider.setFreeSpaceValues(200, 1234, 10, 0, 1234, 900);
        prepareListDataSets();
        prepareGetShareId();
        RecordingMatcher<String> infoMessageMatcher = prepareLogging(LogLevel.INFO);
        final RecordingMatcher<String> notificationRecorder = new RecordingMatcher<String>();
        context.checking(new Expectations()
            {
                {
                    one(dataSetMover).moveDataSetToAnotherShare(ds1File, share2, null, logger);
                    one(notifyer).log(with(LogLevel.WARN), with(notificationRecorder));
                }
            });

        IPostRegistrationTaskExecutor executor = task.createExecutor(DATA_SET_CODE1, false);
        executor.createCleanupTask();
        executor.execute();

        assertEquals("Data set ds-1 successfully moved from share 1 to 2.",
                infoMessageMatcher.recordedObject());
        assertEquals(
                "After moving data set ds-1 to share 2 that share has only 900.00 KB free space. "
                        + "It might be necessary to add a new share.",
                notificationRecorder.recordedObject());
        assertEquals("[1, 2, 3, 4, 2, 2]", freeSpaceProvider.getShares().toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testShufflingOfArchivedDataSet()
    {
        prepareConfigProvider();
        Properties properties = createDefaultProperties();
        properties.setProperty(EagerShufflingTask.FREE_SPACE_LIMIT_KEY, "1");
        properties.setProperty(EagerShufflingTask.VERIFY_CHECKSUM_KEY, "false");
        EagerShufflingTask task = createTask(properties);
        freeSpaceProvider.setFreeSpaceValues(200, 1234, 10, 0, 1234, 900);
        prepareListDataSets();
        RecordingMatcher<String> infoMessageMatcher = prepareLogging(LogLevel.WARN);

        IPostRegistrationTaskExecutor executor = task.createExecutor(DATA_SET_CODE2, false);
        executor.createCleanupTask();
        executor.execute();

        assertEquals("Data set ds-2 couldn't been shuffled because its archiving status is ARCHIVE_PENDING",
                infoMessageMatcher.recordedObject());
        context.assertIsSatisfied();
    }

    @Test
    public void testStopOnNoShareFound()
    {
        prepareConfigProvider();
        Properties properties = createDefaultProperties();
        properties.setProperty(EagerShufflingTask.STOP_ON_NO_SHARE_FOUND_KEY, "true");
        EagerShufflingTask task = createTask(properties);
        freeSpaceProvider.setFreeSpaceValues(200, 10, 10, 0);
        prepareListDataSets();
        final RecordingMatcher<String> notificationRecorder = new RecordingMatcher<String>();
        context.checking(new Expectations()
            {
                {
                    one(notifyer).log(with(LogLevel.ERROR), with(notificationRecorder));
                }
            });

        IPostRegistrationTaskExecutor executor = task.createExecutor(DATA_SET_CODE1, false);
        try
        {
            executor.createCleanupTask();
            fail("EnvironmentFailureException expected.");
        } catch (EnvironmentFailureException ex)
        {
            assertEquals("No share found for shuffling data set ds-1.", ex.getMessage());
        }

        assertEquals("No share found for shuffling data set ds-1.",
                notificationRecorder.recordedObject());
        assertEquals("[1, 2, 3, 4, 1, 2, 3, 4]", freeSpaceProvider.getShares().toString());
        context.assertIsSatisfied();
    }

    private RecordingMatcher<String> prepareLogging(final LogLevel level)
    {
        final RecordingMatcher<String> logMessageMatcher = new RecordingMatcher<String>();
        context.checking(new Expectations()
            {
                {
                    one(logger).log(with(level), with(logMessageMatcher));
                }
            });
        return logMessageMatcher;
    }

    private void prepareListDataSets()
    {
        context.checking(new Expectations()
            {
                {
                    one(service).listPhysicalDataSets();
                    SimpleDataSetInformationDTO ds1 = dataSet("1", DATA_SET_CODE1);
                    SimpleDataSetInformationDTO ds2 = dataSet("2", DATA_SET_CODE2);
                    ds2.setStatus(DataSetArchivingStatus.ARCHIVE_PENDING);
                    will(returnValue(Arrays.asList(ds1, ds2)));
                    one(logger).log(
                            with(LogLevel.INFO),
                            with(Matchers.startsWith("Obtained the list of all "
                                    + "datasets in all shares")));
                    allowing(service).isDataSetOnTrashCanOrDeleted(DATA_SET_CODE1);
                }
            });
    }

    private void prepareConfigProvider()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(configProvider).getDataStoreCode();
                    will(returnValue(DATA_STORE_SERVER_CODE));
                    allowing(configProvider).getStoreRoot();
                    will(returnValue(store));
                }
            });
    }

    private void prepareGetShareId()
    {
        context.checking(new Expectations()
            {
                {
                    one(shareIdManager).getShareId(DATA_SET_CODE1);
                    will(returnValue("1"));
                }
            });
    }

    private SimpleDataSetInformationDTO dataSet(String shareId, String dataSetCode)
    {
        SimpleDataSetInformationDTO dataSet = new SimpleDataSetInformationDTO();
        dataSet.setDataSetCode(dataSetCode);
        dataSet.setDataSetShareId(shareId);
        dataSet.setDataStoreCode(DATA_STORE_SERVER_CODE);
        dataSet.setDataSetLocation(SHARDING + dataSetCode);
        dataSet.setDataSetSize(47 * FileUtils.ONE_KB);
        dataSet.setSpeedHint(Constants.DEFAULT_SPEED_HINT);
        dataSet.setStatus(DataSetArchivingStatus.AVAILABLE);
        return dataSet;
    }

    private EagerShufflingTask createTask()
    {
        return createTask(createDefaultProperties());
    }

    protected Properties createDefaultProperties()
    {
        Properties properties = new Properties();
        properties.setProperty(EagerShufflingTask.SHARE_FINDER_KEY + ".class",
                SimpleShareFinder.class.getName());
        return properties;
    }

    private EagerShufflingTask createTask(Properties properties)
    {
        return new EagerShufflingTask(properties,
                new LinkedHashSet<String>(Arrays.asList("1", "2")), service, shareIdManager,
                freeSpaceProvider, dataSetMover, configProvider, checksumProvider, logger, notifyer);
    }

}
