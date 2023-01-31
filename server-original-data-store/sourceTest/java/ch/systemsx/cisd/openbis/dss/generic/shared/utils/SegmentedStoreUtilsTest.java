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

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus.ARCHIVED;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus.AVAILABLE;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.concurrent.MessageChannel;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.HostAwareFile;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.common.logging.MockLogger;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IChecksumProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ProxyShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.SegmentedStoreUtils.FilterOptions;
import ch.systemsx.cisd.openbis.generic.shared.Constants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = SegmentedStoreUtils.class)
@Test
public class SegmentedStoreUtilsTest extends AbstractFileSystemTestCase
{
    private static final String MOVED = "Moved";

    private static final String LOCK = "lock";

    private static final String START_DELETION = "Start deletion";

    private static final String DATA_STORE_CODE = "ds-code";

    private Mockery context;

    private IEncapsulatedOpenBISService service;

    private IShareIdManager shareIdManager;

    private MockLogger log;

    private IFreeSpaceProvider freeSpaceProvider;

    private ITimeProvider timeProvider;

    private IDatasetLocation datasetLocation;

    private IDataSetDirectoryProvider dataSetDirectoryProvider;

    private IChecksumProvider checksumProvider;

    private File store;

    private int accessTimestamp;

    private File shareFolder;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        shareIdManager = context.mock(IShareIdManager.class);
        freeSpaceProvider = context.mock(IFreeSpaceProvider.class);
        timeProvider = context.mock(ITimeProvider.class);
        datasetLocation = context.mock(IDatasetLocation.class);
        dataSetDirectoryProvider = context.mock(IDataSetDirectoryProvider.class);
        checksumProvider = context.mock(IChecksumProvider.class);

        context.checking(new Expectations()
            {
                {
                    allowing(timeProvider).getTimeInMilliseconds();
                    will(returnValue(0L));
                }
            });
        log = new MockLogger();
        store = new File(workingDirectory, "store");
        store.mkdirs();
        shareFolder = new File(store, "1");
        new File(store, "blabla").mkdirs();
        new File(store, "error").mkdirs();
    }

    @AfterMethod
    public void tearDown(Method method)
    {
        try
        {
            context.assertIsSatisfied();
        } catch (Throwable t)
        {
            // assert expectations were met, including the name of the failed method
            throw new Error(method.getName() + "() : ", t);
        }
    }

    @Test
    public void testFreeSpaceForAShareWhichIsNotAnUnarchivingScratchShare()
    {
        SimpleDataSetInformationDTO ds1 = dataSet(1, 11 * FileUtils.ONE_GB, AVAILABLE);
        Share share = new Share(shareFolder, 0, freeSpaceProvider);

        try
        {
            SegmentedStoreUtils.freeSpace(share, service, asDatasetDescriptions(ds1), dataSetDirectoryProvider,
                    shareIdManager, log);
            fail("EnvironmentFailureException expected.");
        } catch (EnvironmentFailureException ex)
        {
            assertEquals("Share '1' isn't an unarchving scratch share. Such a share has the property "
                    + "unarchiving-scratch-share of the file share.properties set to 'true'.", ex.getMessage());
        }

        assertEquals("", log.toString());
    }

    @Test
    public void testFreeSpaceNothingToDo()
    {
        SimpleDataSetInformationDTO ds1 = dataSet(1, 11 * FileUtils.ONE_GB, AVAILABLE);
        Share share = new Share(shareFolder, 0, freeSpaceProvider);
        share.setUnarchivingScratchShare(true);
        share.setUnarchivingScratchShareMaximumSize(Long.MAX_VALUE);
        RecordingMatcher<HostAwareFile> recordingFileMatcher = prepareFreeSpace(12 * FileUtils.ONE_GB);

        SegmentedStoreUtils.freeSpace(share, service, asDatasetDescriptions(ds1), dataSetDirectoryProvider,
                shareIdManager, log);

        assertEquals(shareFolder.getPath(), recordingFileMatcher.recordedObject().getPath());
        assertEquals("INFO: Free space on unarchiving scratch share '1': 12.00 GB, "
                + "requested space for unarchiving 1 data sets: 11.00 GB\n", log.toString());
    }

    @Test
    public void testFreeSpaceRemovingOneDataSet()
    {
        SimpleDataSetInformationDTO ds1 = dataSet(1, 10 * FileUtils.ONE_GB, AVAILABLE);
        SimpleDataSetInformationDTO ds2 = dataSet(2, 10 * FileUtils.ONE_GB, AVAILABLE);
        SimpleDataSetInformationDTO ds3 = dataSet(3, 12 * FileUtils.ONE_GB, AVAILABLE);
        SimpleDataSetInformationDTO ds4 = dataSet(4, 11 * FileUtils.ONE_GB, AVAILABLE);
        SimpleDataSetInformationDTO ds5 = dataSet(5, 14 * FileUtils.ONE_GB, AVAILABLE);
        Share share = new Share(shareFolder, 0, freeSpaceProvider);
        share.setUnarchivingScratchShare(true);
        share.setUnarchivingScratchShareMaximumSize(Long.MAX_VALUE);
        share.addDataSet(ds5);
        share.addDataSet(ds3);
        share.addDataSet(ds1);
        RecordingMatcher<HostAwareFile> recordingFileMatcher = prepareFreeSpace(12 * FileUtils.ONE_GB, 22 * FileUtils.ONE_GB);
        prepareSetArchingStatus(ds1);
        File file = prepareDeleteFromShare(ds1);
        assertEquals(true, file.exists());

        SegmentedStoreUtils.freeSpace(share, service, asDatasetDescriptions(ds2, ds4), dataSetDirectoryProvider,
                shareIdManager, log);

        assertEquals(false, file.exists());
        assertEquals(shareFolder.getPath(), recordingFileMatcher.getRecordedObjects().get(0).getPath());
        assertEquals(shareFolder.getPath(), recordingFileMatcher.getRecordedObjects().get(1).getPath());
        assertEquals(2, recordingFileMatcher.getRecordedObjects().size());
        assertEquals("INFO: Remove the following data sets from share '1' and set their archiving status "
                + "back to ARCHIVED: [ds-1]\n"
                + "INFO: Await for data set ds-1 to be unlocked.\n"
                + "INFO: Start deleting data set ds-1 at " + shareFolder + "/abc/ds-1\n"
                + "INFO: Data set ds-1 at " + shareFolder + "/abc/ds-1 has been successfully deleted.\n"
                + "INFO: The following data sets have been successfully removed from share '1' "
                + "and their archiving status has been successfully set back to ARCHIVED: [ds-1]\n"
                + "INFO: Free space on unarchiving scratch share '1': 22.00 GB, requested space for "
                + "unarchiving 2 data sets: 21.00 GB\n", log.toString());
    }

    @Test
    public void testFreeSpaceForThreeDataSetsOneAlreadyInShare()
    {
        SimpleDataSetInformationDTO ds0 = dataSet(0, 19 * FileUtils.ONE_GB, ARCHIVED);
        SimpleDataSetInformationDTO ds1 = dataSet(1, 10 * FileUtils.ONE_GB, AVAILABLE);
        SimpleDataSetInformationDTO ds2 = dataSet(2, 10 * FileUtils.ONE_GB, AVAILABLE);
        SimpleDataSetInformationDTO ds35 = dataSet(35, 12 * FileUtils.ONE_GB, AVAILABLE, false);
        SimpleDataSetInformationDTO ds3 = dataSet(3, 12 * FileUtils.ONE_GB, AVAILABLE);
        SimpleDataSetInformationDTO ds4 = dataSet(4, 11 * FileUtils.ONE_GB, AVAILABLE);
        SimpleDataSetInformationDTO ds5 = dataSet(5, 14 * FileUtils.ONE_GB, AVAILABLE);
        Share share = new Share(shareFolder, 0, freeSpaceProvider);
        share.setUnarchivingScratchShare(true);
        share.setUnarchivingScratchShareMaximumSize(50 * FileUtils.ONE_GB);
        share.addDataSet(ds0);
        share.addDataSet(ds5);
        share.addDataSet(ds35);
        share.addDataSet(ds3);
        share.addDataSet(ds1);
        List<DatasetDescription> dataSetsToUnarchive = asDatasetDescriptions(ds1, ds2, ds4);
        // Data sets in share: ds1(10G), ds3(12G), ds5(14G) total 36G
        // Data sets to unarchive: ds1(10G), ds2(10G), ds4(11G)
        RecordingMatcher<HostAwareFile> recordingFileMatcher = prepareFreeSpace(28 * FileUtils.ONE_GB, 40 * FileUtils.ONE_GB);
        prepareSetArchingStatus(ds3);
        File file = prepareDeleteFromShare(ds3);
        assertEquals(true, file.exists());

        SegmentedStoreUtils.freeSpace(share, service, dataSetsToUnarchive, dataSetDirectoryProvider,
                shareIdManager, log);

        assertEquals(false, file.exists());
        assertEquals(shareFolder.getPath(), recordingFileMatcher.getRecordedObjects().get(0).getPath());
        assertEquals(shareFolder.getPath(), recordingFileMatcher.getRecordedObjects().get(1).getPath());
        assertEquals(2, recordingFileMatcher.getRecordedObjects().size());
        assertEquals("INFO: Remove the following data sets from share '1' and set their archiving status "
                + "back to ARCHIVED: [ds-3]\n"
                + "INFO: Await for data set ds-3 to be unlocked.\n"
                + "INFO: Start deleting data set ds-3 at " + shareFolder + "/abc/ds-3\n"
                + "INFO: Data set ds-3 at " + shareFolder + "/abc/ds-3 has been successfully deleted.\n"
                + "INFO: The following data sets have been successfully removed from share '1' "
                + "and their archiving status has been successfully set back to ARCHIVED: [ds-3]\n"
                + "INFO: Free space on unarchiving scratch share '1': 25.00 GB, requested space for "
                + "unarchiving 2 data sets: 21.00 GB\n", log.toString());
    }

    @Test
    public void testFreeSpaceRemovingDataSetsButStillNotEnoughFreeSpace()
    {
        SimpleDataSetInformationDTO ds1 = dataSet(1, 10 * FileUtils.ONE_GB, AVAILABLE);
        SimpleDataSetInformationDTO ds2 = dataSet(2, 10 * FileUtils.ONE_GB, AVAILABLE);
        SimpleDataSetInformationDTO ds3 = dataSet(3, 12 * FileUtils.ONE_GB, AVAILABLE);
        SimpleDataSetInformationDTO ds4 = dataSet(4, 11 * FileUtils.ONE_GB, AVAILABLE);
        SimpleDataSetInformationDTO ds5 = dataSet(5, 14 * FileUtils.ONE_GB, AVAILABLE);
        Share share = new Share(shareFolder, 0, freeSpaceProvider);
        share.setUnarchivingScratchShare(true);
        share.setUnarchivingScratchShareMaximumSize(Long.MAX_VALUE);
        share.addDataSet(ds3);
        share.addDataSet(ds2);
        share.addDataSet(ds1);
        RecordingMatcher<HostAwareFile> recordingFileMatcher = prepareFreeSpace(2 * FileUtils.ONE_GB);

        try
        {
            SegmentedStoreUtils.freeSpace(share, service, asDatasetDescriptions(ds4, ds5, ds1), dataSetDirectoryProvider,
                    shareIdManager, log);
            fail("EnvironmentFailureException expected");
        } catch (EnvironmentFailureException ex)
        {
            assertEquals("Even after removing all removable data sets from share '1' there would be "
                    + "still only 24.00 GB free space which is not enough as 25.00 GB is requested.", ex.getMessage());
        }

        assertEquals(shareFolder.getPath(), recordingFileMatcher.recordedObject().getPath());
        assertEquals("", log.toString());
    }

    @Test
    public void testGetDataSetsPerShare()
    {
        final File ds1File = new File(store, "1/uuid/01/02/03/ds-1");
        ds1File.mkdirs();
        FileUtilities.writeToFile(new File(ds1File, "read.me"), "nice work!");
        FileUtilities.writeToFile(new File(store, "1/" + ShareFactory.SPEED_FILE), "  143  \n");
        final SimpleDataSetInformationDTO ds1 = dataSet(ds1File, DATA_STORE_CODE, null);
        File ds2File = new File(store, "1/uuid/01/02/04/ds-2");
        ds2File.mkdirs();
        FileUtilities.writeToFile(new File(ds2File, "hello.txt"), "hello world");
        final SimpleDataSetInformationDTO ds2 = dataSet(ds2File, "blabla", null);
        File ds3File = new File(store, "2/uuid/01/05/04/ds-3");
        ds3File.mkdirs();
        File speedFile2 = new File(store, "2/" + ShareFactory.SPEED_FILE);
        FileUtilities.writeToFile(speedFile2, "not a number");
        FileUtilities.writeToFile(new File(ds3File, "hi.txt"), "hi everybody");
        final SimpleDataSetInformationDTO ds3 = dataSet(ds3File, DATA_STORE_CODE, 123456789L);
        File ds4File = new File(store, "1/uuid/0a/02/03/ds-4");
        ds4File.mkdirs();
        FileUtilities.writeToFile(new File(ds4File, "hello.data"), "hello data");
        final SimpleDataSetInformationDTO ds4 = dataSet(ds4File, DATA_STORE_CODE, 42L);
        final SimpleDataSetInformationDTO ds5 = new SimpleDataSetInformationDTO();
        ds5.setDataSetCode("ds5");
        ds5.setDataSetShareId("2");
        ds5.setDataSetLocation("blabla");
        ds5.setDataStoreCode(DATA_STORE_CODE);
        final RecordingMatcher<HostAwareFile> fileMatcher = new RecordingMatcher<HostAwareFile>();
        context.checking(new Expectations()
            {
                {
                    one(service).listPhysicalDataSets();
                    will(returnValue(Arrays.asList(ds1, ds2, ds3, ds4, ds5)));

                    one(service).updateShareIdAndSize("ds-1", "1", 10L);

                    try
                    {
                        one(freeSpaceProvider).freeSpaceKb(with(fileMatcher));
                        will(returnValue(12345L));
                    } catch (IOException ex)
                    {
                        ex.printStackTrace();
                    }
                }
            });

        List<Share> shares =
                SegmentedStoreUtils.getSharesWithDataSets(store, DATA_STORE_CODE, FilterOptions.AVAILABLE_FOR_SHUFFLING,
                        freeSpaceProvider, service, log, timeProvider);
        Share share1 = shares.get(0);
        long freeSpace = share1.calculateFreeSpace();

        assertEquals(new File(store, "1"), fileMatcher.recordedObject().getLocalFile());
        assertEquals(12345L * 1024, freeSpace);
        assertEquals(new File(store, "1").toString(), share1.getShare().toString());
        assertEquals("1", share1.getShareId());
        assertEquals(100, share1.getSpeed());
        assertSame(ds4, share1.getDataSetsOrderedBySize().get(0));
        assertEquals(42L, share1.getDataSetsOrderedBySize().get(0).getDataSetSize().longValue());
        assertSame(ds1, share1.getDataSetsOrderedBySize().get(1));
        assertEquals(10L, share1.getDataSetsOrderedBySize().get(1).getDataSetSize().longValue());
        assertEquals(2, share1.getDataSetsOrderedBySize().size());
        assertEquals(52L, share1.getTotalSizeOfDataSets());
        assertEquals(new File(store, "2").toString(), shares.get(1).getShare().toString());
        assertEquals("2", shares.get(1).getShareId());
        assertSame(ds3, shares.get(1).getDataSetsOrderedBySize().get(0));
        assertEquals(123456789L, shares.get(1).getDataSetsOrderedBySize().get(0).getDataSetSize()
                .longValue());
        assertEquals(1, shares.get(1).getDataSetsOrderedBySize().size());
        assertEquals(Math.abs(Constants.DEFAULT_SPEED_HINT), shares.get(1).getSpeed());
        assertEquals(123456789L, shares.get(1).getTotalSizeOfDataSets());
        assertEquals(2, shares.size());
        assertEquals("WARN: Speed file " + speedFile2 + " doesn't contain a number: not a number\n"
                + "INFO: Calculating size of " + ds1File + "\n" + "INFO: " + ds1File
                + " contains 10 bytes (calculated in 0 msec)\n"
                + "WARN: Data set ds5 no longer exists in share 2.\n", log.toString());
    }

    @Test
    public void testMoveDataSetToAnothetShareAndDelete() throws IOException
    {
        File share1 = new File(workingDirectory, "store/1");
        File share1uuid01 = new File(share1, "uuid/01");
        File ds2 = new File(share1uuid01, "0b/0c/ds-2/original");
        ds2.mkdirs();
        final File readmeFile = new File(ds2, "read.me");
        FileUtilities.writeToFile(readmeFile, "do nothing");
        final File dataSetDirInStore = new File(share1uuid01, "02/03/ds-1");
        File original = new File(dataSetDirInStore, "original");
        original.mkdirs();
        final File helloFile = new File(original, "hello.txt");
        FileUtilities.writeToFile(helloFile, "hello world");
        final File share2 = new File(workingDirectory, "store/2");
        share2.mkdirs();
        File share2uuid01 = new File(share2, "uuid/01");
        File file = new File(share2uuid01, "22/33/orig");
        file.mkdirs();
        final File hiFile = new File(file, "hi.txt");
        FileUtilities.writeToFile(hiFile, "hi");
        context.checking(new Expectations()
            {
                {
                    one(service).tryGetDataSet("ds-1");
                    will(returnValue(new PhysicalDataSet()));

                    one(service).updateShareIdAndSize("ds-1", "2", 11L);
                    one(shareIdManager).lock("ds-1");
                    one(shareIdManager).setShareId("ds-1", "2");
                    one(shareIdManager).releaseLock("ds-1");
                    exactly(2).of(shareIdManager).await("ds-1");

                    one(datasetLocation).getDataSetCode();
                    will(returnValue("ds-1"));

                    one(dataSetDirectoryProvider).getDataSetDirectory(datasetLocation);
                    will(returnValue(new File(
                            "targets/unit-test-wd/ch.systemsx.cisd.openbis.dss.generic.shared.utils.SegmentedStoreUtilsTest/store/2/uuid/01/02/03/ds-1")));

                    one(checksumProvider).getChecksum("ds-1", "original/hello.txt");
                    will(returnValue(FileUtils.checksumCRC32(helloFile)));
                }
            });
        assertEquals(true, dataSetDirInStore.exists());
        assertFileNames(share2uuid01, "22");
        final MessageChannel moveChannel = new MessageChannel(10000);
        final MessageChannel deletionChannel = new MessageChannel(10000);

        new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    SegmentedStoreUtils.moveDataSetToAnotherShare(dataSetDirInStore, share2,
                            service, new ProxyShareIdManager(shareIdManager)
                                {
                                    @Override
                                    public void lock(String dataSetCode)
                                    {
                                        super.lock(dataSetCode);
                                        moveChannel.send(LOCK);
                                        deletionChannel.assertNextMessage(START_DELETION);
                                    }
                                }, checksumProvider, log);
                    moveChannel.send(MOVED);
                }
            }).start();
        moveChannel.assertNextMessage(LOCK);
        SegmentedStoreUtils.deleteDataSet(datasetLocation, dataSetDirectoryProvider,
                new ProxyShareIdManager(shareIdManager)
                    {
                        @Override
                        public void await(String dataSetCode)
                        {
                            super.await(dataSetCode);
                            deletionChannel.send(START_DELETION);
                            moveChannel.assertNextMessage(MOVED);
                        }
                    }, log);

        log.assertNextLogMessage("Await for data set ds-1 to be unlocked.");
        log.assertNextLogMessage("Start moving directory 'targets/unit-test-wd/ch.systemsx.cisd."
                + "openbis.dss.generic.shared.utils.SegmentedStoreUtilsTest/store/1/uuid/01/02/03/ds-1' "
                + "to new share 'targets/unit-test-wd/ch.systemsx.cisd.openbis.dss.generic.shared.utils."
                + "SegmentedStoreUtilsTest/store/2/uuid/01/02/03/ds-1'");
        log.assertNextLogMessageContains("Finished moving directory 'targets/unit-test-wd/ch.systemsx.cisd."
                + "openbis.dss.generic.shared.utils.SegmentedStoreUtilsTest/store/1/uuid/01/02/03/ds-1'"
                + " to new share 'targets/unit-test-wd/ch.systemsx.cisd.openbis.dss.generic.shared.utils."
                + "SegmentedStoreUtilsTest/store/2/uuid/01/02/03/ds-1'");
        log.assertNextLogMessage("Verifying structure, size and optional checksum of data set content in share 2.");
        log.assertNextLogMessage("Await for data set ds-1 to be unlocked.");
        log.assertNextLogMessage("Start deleting data set ds-1 at " + share1
                + "/uuid/01/02/03/ds-1");
        log.assertNextLogMessage("Data set ds-1 at " + share1
                + "/uuid/01/02/03/ds-1 has been successfully deleted.");
        log.assertNextLogMessage("Start deleting data set ds-1 at " + share2
                + "/uuid/01/02/03/ds-1");
        log.assertNextLogMessage("Data set ds-1 at " + share2
                + "/uuid/01/02/03/ds-1 has been successfully deleted.");
        assertEquals(false, dataSetDirInStore.exists());
        assertEquals(false, new File(
                "targets/unit-test-wd/ch.systemsx.cisd.openbis.dss.generic.shared.utils."
                        + "SegmentedStoreUtilsTest/store/2/uuid/01/02/03/ds-1").exists());
        assertFileNames(share2uuid01, "02", "22");
        moveChannel.assertEmpty();
        deletionChannel.assertEmpty();
        log.assertNoMoreLogMessages();
    }

    @Test(groups = "slow")
    public void testMoveDataSetToAnotherShare()
    {
        File share1 = new File(workingDirectory, "store/1");
        File share1uuid01 = new File(share1, "uuid/01");
        File ds2 = new File(share1uuid01, "0b/0c/ds-2/original");
        ds2.mkdirs();
        FileUtilities.writeToFile(new File(ds2, "read.me"), "do nothing");
        File dataSetDirInStore = new File(share1uuid01, "02/03/ds-1");
        File original = new File(dataSetDirInStore, "original");
        original.mkdirs();
        FileUtilities.writeToFile(new File(original, "hello.txt"), "hello world");
        File share2 = new File(workingDirectory, "store/2");
        share2.mkdirs();
        File share2uuid01 = new File(share2, "uuid/01");
        File file = new File(share2uuid01, "22/33/orig");
        file.mkdirs();
        FileUtilities.writeToFile(new File(file, "hi.txt"), "hi");
        context.checking(new Expectations()
            {
                {
                    one(service).tryGetDataSet("ds-1");
                    will(returnValue(new PhysicalDataSet()));

                    one(service).updateShareIdAndSize("ds-1", "2", 11L);
                    one(shareIdManager).lock("ds-1");
                    one(shareIdManager).setShareId("ds-1", "2");
                    one(shareIdManager).releaseLock("ds-1");
                    one(shareIdManager).await("ds-1");
                }
            });
        assertEquals(true, dataSetDirInStore.exists());
        assertFileNames(share2uuid01, "22");

        SegmentedStoreUtils.moveDataSetToAnotherShare(dataSetDirInStore, share2, service,
                shareIdManager, null, log);

        log.assertNextLogMessage("Start moving directory 'targets/unit-test-wd/ch.systemsx.cisd."
                + "openbis.dss.generic.shared.utils.SegmentedStoreUtilsTest/store/1/uuid/01/02/03/ds-1' "
                + "to new share 'targets/unit-test-wd/ch.systemsx.cisd.openbis.dss.generic.shared.utils."
                + "SegmentedStoreUtilsTest/store/2/uuid/01/02/03/ds-1'");
        log.assertNextLogMessageContains("Finished moving directory 'targets/unit-test-wd/ch.systemsx.cisd."
                + "openbis.dss.generic.shared.utils.SegmentedStoreUtilsTest/store/1/uuid/01/02/03/ds-1'"
                + " to new share 'targets/unit-test-wd/ch.systemsx.cisd.openbis.dss.generic.shared.utils."
                + "SegmentedStoreUtilsTest/store/2/uuid/01/02/03/ds-1'");
        log.assertNextLogMessage("Verifying structure, size and optional checksum of data set content in share 2.");
        log.assertNextLogMessage("Await for data set ds-1 to be unlocked.");
        log.assertNextLogMessage("Start deleting data set ds-1 at " + share1
                + "/uuid/01/02/03/ds-1");
        log.assertNextLogMessage("Data set ds-1 at " + share1
                + "/uuid/01/02/03/ds-1 has been successfully deleted.");
        assertEquals(false, dataSetDirInStore.exists());
        assertFileNames(share2uuid01, "02", "22");
        assertEquals("hello world\n",
                FileUtilities.loadToString(new File(share2uuid01, "02/03/ds-1/original/hello.txt")));
        log.assertNoMoreLogMessages();
    }

    @Test(groups = "slow", expectedExceptions = EnvironmentFailureException.class)
    public void testMoveDataSetToAnotherShareWithDifferentChecksums() throws IOException
    {
        File share1 = new File(workingDirectory, "store/1");
        File share1uuid01 = new File(share1, "uuid/01");
        File dataSetDirInStore = new File(share1uuid01, "02/03/ds-1");
        File original = new File(dataSetDirInStore, "original");
        original.mkdirs();
        final File helloFile = new File(original, "hello.txt");
        FileUtilities.writeToFile(helloFile, "hello world");
        File share2 = new File(workingDirectory, "store/2");

        context.checking(new Expectations()
            {
                {
                    one(service).tryGetDataSet("ds-1");
                    will(returnValue(new PhysicalDataSet()));

                    one(shareIdManager).lock("ds-1");
                    one(shareIdManager).releaseLock("ds-1");

                    one(checksumProvider).getChecksum("ds-1", "original/hello.txt");
                    will(returnValue(1L));
                }
            });

        SegmentedStoreUtils.moveDataSetToAnotherShare(dataSetDirInStore, share2, service,
                shareIdManager, checksumProvider, log);

        fail();
    }

    @Test
    public void testMoveDataSetToAnotherShareWhichIsAnUnarchivingScratchShare()
    {
        File share1 = new File(workingDirectory, "store/1");
        File share1uuid01 = new File(share1, "uuid/01");
        File dataSetDirInStore = new File(share1uuid01, "02/03/ds-1");
        File original = new File(dataSetDirInStore, "original");
        original.mkdirs();
        final File helloFile = new File(original, "hello.txt");
        FileUtilities.writeToFile(helloFile, "hello world");
        File share2 = new File(workingDirectory, "store/2");
        share2.mkdirs();
        FileUtilities.writeToFile(new File(share2, ShareFactory.SHARE_PROPS_FILE),
                ShareFactory.UNARCHIVING_SCRATCH_SHARE_PROP + "=true");
        context.checking(new Expectations()
            {
                {
                    one(service).tryGetDataSet("ds-1");
                    will(returnValue(new PhysicalDataSet()));

                    one(shareIdManager).lock("ds-1");
                    one(shareIdManager).releaseLock("ds-1");
                }
            });

        try
        {
            SegmentedStoreUtils.moveDataSetToAnotherShare(dataSetDirInStore, share2, service,
                    shareIdManager, checksumProvider, log);
            fail("EnvironmentFailureException expected");
        } catch (EnvironmentFailureException ex)
        {
            assertEquals("Share '2' is a scratch share for unarchiving purposes. "
                    + "No data sets can be moved from/to such a share.", ex.getMessage());
        }
    }

    @Test
    public void testMoveDataSetFromAnUnarchivingScratchShareToAnotherShare()
    {
        File share1 = new File(workingDirectory, "store/1");
        File share1uuid01 = new File(share1, "uuid/01");
        File dataSetDirInStore = new File(share1uuid01, "02/03/ds-1");
        File original = new File(dataSetDirInStore, "original");
        original.mkdirs();
        FileUtilities.writeToFile(new File(share1, ShareFactory.SHARE_PROPS_FILE),
                ShareFactory.UNARCHIVING_SCRATCH_SHARE_PROP + "=true");
        final File helloFile = new File(original, "hello.txt");
        FileUtilities.writeToFile(helloFile, "hello world");
        File share2 = new File(workingDirectory, "store/2");
        context.checking(new Expectations()
            {
                {
                    one(service).tryGetDataSet("ds-1");
                    will(returnValue(new PhysicalDataSet()));

                    one(shareIdManager).lock("ds-1");
                    one(shareIdManager).releaseLock("ds-1");
                }
            });

        try
        {
            SegmentedStoreUtils.moveDataSetToAnotherShare(dataSetDirInStore, share2, service,
                    shareIdManager, checksumProvider, log);
            fail("EnvironmentFailureException expected");
        } catch (EnvironmentFailureException ex)
        {
            assertEquals("Share '1' is a scratch share for unarchiving purposes. "
                    + "No data sets can be moved from/to such a share.", ex.getMessage());
        }
    }

    @Test
    public void testCleanupOld()
    {
        File ds1In1 = dataSetFile("1", false);
        File ds1In2 = dataSetFile("2", false);
        SimpleDataSetInformationDTO dataSet = dataSet(ds1In1, DATA_STORE_CODE, null);
        context.checking(new Expectations()
            {
                {
                    one(shareIdManager).getShareId("ds-1");
                    will(returnValue("2"));

                    one(shareIdManager).await("ds-1");
                }
            });

        SegmentedStoreUtils.cleanUp(dataSet, store, "2", shareIdManager, log);

        assertEquals(false, ds1In1.exists());
        assertEquals(true, ds1In2.exists());
        log.assertNextLogMessage("Await for data set ds-1 to be unlocked.");
        log.assertNextLogMessage("Start deleting data set ds-1 at " + ds1In1);
        log.assertNextLogMessage("Data set ds-1 at " + ds1In1 + " has been successfully deleted.");
        log.assertNoMoreLogMessages();
    }

    @Test
    public void testCleanupNew()
    {
        File ds1In1 = dataSetFile("1", false);
        File ds1In2 = dataSetFile("2", true);
        SimpleDataSetInformationDTO dataSet = dataSet(ds1In1, DATA_STORE_CODE, null);
        context.checking(new Expectations()
            {
                {
                    one(shareIdManager).getShareId("ds-1");
                    will(returnValue("1"));

                    one(shareIdManager).await("ds-1");
                }
            });

        SegmentedStoreUtils.cleanUp(dataSet, store, "2", shareIdManager, log);

        assertEquals(true, ds1In1.exists());
        log.assertNextLogMessage("Await for data set ds-1 to be unlocked.");
        log.assertNextLogMessage("Start deleting data set ds-1 at " + ds1In2);
        log.assertNextLogMessage("Deletion of data set ds-1 at " + ds1In2 + " failed.");
        log.assertNoMoreLogMessages();
    }

    @Test
    public void testFindIncomingShare()
    {
        File incomingFolder = new File(workingDirectory, "incoming");
        incomingFolder.mkdirs();
        File share1 = new File(store, "1");
        share1.mkdirs();
        FileUtilities.writeToFile(new File(share1, "share.properties"),
                ShareFactory.WITHDRAW_SHARE_PROP + " = true");

        String share = SegmentedStoreUtils.findIncomingShare(incomingFolder, store, null, log);

        assertEquals("1", share);
        assertEquals(
                "WARN: Incoming folder [targets/unit-test-wd/"
                        + SegmentedStoreUtilsTest.class.getName()
                        + "/incoming] can not be assigned to share 1 because its property "
                        + "withdraw-share is set to true.\n", log.toString());
    }

    @Test
    public void testFindIncomingShareToBeIgnoredInShuffling()
    {
        File incomingFolder = new File(workingDirectory, "incoming");
        incomingFolder.mkdirs();
        File share1 = new File(store, "1");
        share1.mkdirs();
        FileUtilities.writeToFile(new File(share1, "share.properties"),
                ShareFactory.IGNORED_FOR_SHUFFLING_PROP + " = true");

        String share = SegmentedStoreUtils.findIncomingShare(incomingFolder, store, null, log);

        assertEquals("1", share);
        assertEquals("", log.toString());
    }

    @Test
    public void testGetSharesFilterOutIgnoredForShuffling()
    {
        File share1 = new File(store, "1");
        share1.mkdirs();
        FileUtilities.writeToFile(new File(share1, "share.properties"),
                ShareFactory.IGNORED_FOR_SHUFFLING_PROP + " = true");
        File share2 = new File(store, "2");
        share2.mkdirs();

        List<Share> shares =
                SegmentedStoreUtils.getSharesWithDataSets(store, DATA_STORE_CODE, FilterOptions.AVAILABLE_FOR_SHUFFLING,
                        freeSpaceProvider, service, log, timeProvider);

        assertEquals("2", shares.get(0).getShareId());
        assertEquals(1, shares.size());
    }

    @Test
    public void testGetAllShares()
    {
        File share1 = new File(store, "1");
        share1.mkdirs();
        FileUtilities.writeToFile(new File(share1, "share.properties"),
                ShareFactory.IGNORED_FOR_SHUFFLING_PROP + " = true");
        File share2 = new File(store, "2");
        share2.mkdirs();

        List<Share> shares =
                SegmentedStoreUtils.getSharesWithDataSets(store, DATA_STORE_CODE, FilterOptions.ALL,
                        freeSpaceProvider, service, log, timeProvider);

        assertEquals("1", shares.get(0).getShareId());
        assertEquals("2", shares.get(1).getShareId());
        assertEquals(2, shares.size());
    }

    @Test
    public void testIncomingShareCannotbeAssignedIfWithdrawn()
    {
        File incomingFolder = new File(workingDirectory, "incoming");
        incomingFolder.mkdirs();
        File share1 = new File(store, "1");
        share1.mkdirs();

        Integer incomingShareId = 2;
        File share2 = new File(store, String.valueOf(incomingShareId));
        share2.mkdirs();
        FileUtilities.writeToFile(new File(share2, "share.properties"),
                ShareFactory.WITHDRAW_SHARE_PROP + " = true");

        try
        {
            SegmentedStoreUtils.findIncomingShare(incomingFolder, store, incomingShareId, log);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Incoming folder [targets/unit-test-wd/"
                    + SegmentedStoreUtilsTest.class.getName()
                    + "/incoming] can not be assigned to share " + String.valueOf(incomingShareId) + " because its property "
                    + "withdraw-share is set to true.", ex.getMessage());
        }
    }

    @Test
    public void testIncomingShareAssignment()
    {
        File incomingFolder = new File(workingDirectory, "incoming");
        incomingFolder.mkdirs();
        File share1 = new File(store, "1");
        share1.mkdirs();

        Integer incomingShareId = 2;
        File share2 = new File(store, String.valueOf(incomingShareId));
        share2.mkdirs();

        String assignedShare = SegmentedStoreUtils.findIncomingShare(incomingFolder, store, incomingShareId, log);

        assertEquals(String.valueOf(incomingShareId), assignedShare);
        assertEquals(
                "INFO: Incoming folder [targets/unit-test-wd/"
                        + SegmentedStoreUtilsTest.class.getName()
                        + "/incoming] is assigned to incoming share " + String.valueOf(incomingShareId) + ".\n", log.toString());
    }

    private File dataSetFile(String shareId, boolean empty)
    {
        File share = new File(store, shareId);
        File dataSetFile = new File(share, "uuid/01/0b/0c/ds-1");
        if (empty)
        {
            return dataSetFile;
        }
        dataSetFile.mkdirs();
        FileUtilities.writeToFile(new File(dataSetFile, "read.me"), "do nothing");
        return dataSetFile;
    }

    private void assertFileNames(File file, String... names)
    {
        File[] files = file.listFiles();
        Arrays.sort(files);
        List<String> actualNames = new ArrayList<String>();
        for (File child : files)
        {
            actualNames.add(child.getName());
        }
        assertEquals(Arrays.asList(names).toString(), actualNames.toString());
    }

    private File prepareDeleteFromShare(final SimpleDataSetInformationDTO dataSet)
    {
        final File file = new File(shareFolder, dataSet.getDataSetLocation());
        context.checking(new Expectations()
            {
                {
                    one(shareIdManager).await(dataSet.getDataSetCode());
                    one(dataSetDirectoryProvider).getDataSetDirectory(dataSet);
                    will(returnValue(file));
                }
            });
        return file;
    }

    private void prepareSetArchingStatus(final SimpleDataSetInformationDTO... dataSets)
    {
        context.checking(new Expectations()
            {
                {
                    List<String> dataSetCodes = new ArrayList<String>();
                    for (SimpleDataSetInformationDTO dataSet : dataSets)
                    {
                        dataSetCodes.add(dataSet.getDataSetCode());
                    }
                    one(service).updateDataSetStatuses(dataSetCodes, DataSetArchivingStatus.ARCHIVED, true);
                }
            });
    }

    private RecordingMatcher<HostAwareFile> prepareFreeSpace(final long... freeSpaceValues)
    {
        final RecordingMatcher<HostAwareFile> recorder = new RecordingMatcher<HostAwareFile>();
        final Sequence sequence = context.sequence("free space");
        context.checking(new Expectations()
            {
                {
                    try
                    {
                        for (long freeSpace : freeSpaceValues)
                        {
                            one(freeSpaceProvider).freeSpaceKb(with(recorder));
                            will(returnValue((freeSpace + SegmentedStoreUtils.MINIMUM_FREE_SCRATCH_SPACE) / 1024));
                            inSequence(sequence);
                        }
                    } catch (IOException ex)
                    {
                        ex.printStackTrace();
                    }
                }
            });
        return recorder;
    }

    private List<DatasetDescription> asDatasetDescriptions(SimpleDataSetInformationDTO... dataSets)
    {
        List<DatasetDescription> result = new ArrayList<DatasetDescription>();
        for (SimpleDataSetInformationDTO dataSet : dataSets)
        {
            DatasetDescription datasetDescription = new DatasetDescription();
            datasetDescription.setDataSetCode(dataSet.getDataSetCode());
            datasetDescription.setDataSetSize(dataSet.getDataSetSize());
            result.add(datasetDescription);
        }
        return result;
    }

    private SimpleDataSetInformationDTO dataSet(int id, long size, DataSetArchivingStatus status)
    {
        return dataSet(id, size, status, true);
    }

    private SimpleDataSetInformationDTO dataSet(int id, long size, DataSetArchivingStatus status, boolean presentInArchive)
    {
        File dsFile = new File(shareFolder, "abc/ds-" + id);
        dsFile.mkdirs();
        FileUtilities.writeToFile(new File(dsFile, "read.me"), id + " nice works!");
        SimpleDataSetInformationDTO dataSet = dataSet(dsFile, DATA_STORE_CODE, size);
        dataSet.setStatus(status);
        dataSet.setPresentInArchive(presentInArchive);
        return dataSet;
    }

    private SimpleDataSetInformationDTO dataSet(File dataSetFile, String dataStoreCode, Long size)
    {
        SimpleDataSetInformationDTO dataSet = new SimpleDataSetInformationDTO();
        dataSet.setDataSetCode(dataSetFile.getName());
        dataSet.setDataStoreCode(dataStoreCode);
        String path = FileUtilities.getRelativeFilePath(store, dataSetFile);
        int indexOfFirstSeparator = path.indexOf(File.separatorChar);
        dataSet.setDataSetShareId(path.substring(0, indexOfFirstSeparator));
        dataSet.setDataSetLocation(path.substring(indexOfFirstSeparator + 1));
        dataSet.setDataSetSize(size);
        dataSet.setAccessTimestamp(new Date(accessTimestamp += 10000));
        return dataSet;
    }

}
