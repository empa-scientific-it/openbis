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

import static ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractArchiverProcessingPlugin.SHARE_FINDER_KEY;

import java.io.File;
import java.util.Arrays;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.BooleanStatus;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.common.time.TimingParameters;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.shared.ProcessingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.DatasetDescriptionBuilder;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = { AbstractArchiverProcessingPlugin.class, RsyncArchiver.class })
public class RsyncArchiverTest extends AbstractArchiverTestCase
{
    private static final String LOCATION = "location";

    private RsyncArchiver archiver;

    private File share2;

    @BeforeMethod
    public void setUpRsyncArchiver()
    {
        File ds1 = new File(share1, LOCATION);
        ds1.mkdir();
        FileUtilities.writeToFile(new File(ds1, "ds1"), "hello world");
        share2 = new File(store, "2");
        share2.mkdir();
        archiver = new RsyncArchiver(properties, store, fileOperationsManagerFactory);
        archiver.statusUpdater = statusUpdater;
    }

    @Test
    public void testSuccessfulArchivingIfDataSetPresentInArchive()
    {
        final DatasetDescription ds1 =
                new DatasetDescriptionBuilder("ds1").location(LOCATION).getDatasetDescription();
        context.checking(new Expectations()
            {
                {
                    one(shareIdManager).getShareId("ds1");
                    will(returnValue("1"));

                    one(service).updateShareIdAndSize("ds1", "1", 11L);

                    one(dataSetDirectoryProvider).getDataSetDirectory(ds1);
                    File file = new File(share1, LOCATION);
                    will(returnValue(file));

                    one(fileOperationsManager).isSynchronizedWithDestination(file, ds1);
                    will(returnValue(BooleanStatus.createTrue()));

                    one(deleter).scheduleDeletionOfDataSets(Arrays.asList(ds1),
                            TimingParameters.DEFAULT_MAXIMUM_RETRY_COUNT,
                            TimingParameters.DEFAULT_INTERVAL_TO_WAIT_AFTER_FAILURE_SECONDS);

                    one(statusUpdater).update(Arrays.asList("ds1"),
                            DataSetArchivingStatus.ARCHIVED, true);
                }
            });

        ProcessingStatus status = archiver.archive(Arrays.asList(ds1), archiverTaskContext, true);

        AssertionUtil.assertContainsLines("INFO  OPERATION.AbstractDatastorePlugin - "
                + "Archiving of the following datasets has been requested: [Dataset 'ds1']",
                logRecorder.getLogContent());
        assertEquals("[]", status.getErrorStatuses().toString());
    }

    @Test
    public void testSuccessfulArchiving()
    {
        final DatasetDescription ds1 =
                new DatasetDescriptionBuilder("ds1").location(LOCATION).size(42L)
                        .getDatasetDescription();
        final File retrievedDataSet = new File(store, RsyncArchiver.STAGING_FOLDER + "/ds1");
        retrievedDataSet.mkdirs();
        context.checking(new Expectations()
            {
                {
                    exactly(2).of(dataSetDirectoryProvider).getDataSetDirectory(ds1);
                    File file = new File(share1, LOCATION);
                    will(returnValue(file));

                    one(fileOperationsManager).isSynchronizedWithDestination(file, ds1);
                    will(returnValue(BooleanStatus.createFalse()));

                    one(fileOperationsManager).copyToDestination(file, ds1);
                    will(returnValue(Status.OK));

                    one(fileOperationsManager).isHosted();
                    will(returnValue(true));

                    one(statusUpdater).update(Arrays.asList("ds1"),
                            DataSetArchivingStatus.AVAILABLE, true);

                    one(contentProvider).asContentWithoutModifyingAccessTimestamp("ds1");
                    will(returnValue(new MockContent(":0:0", "f.txt:9:8DA988AF")));

                    FileUtilities.writeToFile(new File(retrievedDataSet, "f.txt"), "abcdefghi");
                    one(fileOperationsManager).retrieveFromDestination(retrievedDataSet, ds1);
                    will(returnValue(Status.OK));
                }
            });

        ProcessingStatus status = archiver.archive(Arrays.asList(ds1), archiverTaskContext, false);

        AssertionUtil.assertContainsLines("INFO  OPERATION.AbstractDatastorePlugin - "
                + "Archiving of the following datasets has been requested: [Dataset 'ds1']",
                logRecorder.getLogContent());
        assertEquals("[]", status.getErrorStatuses().toString());
        assertEquals(false, retrievedDataSet.exists());
    }

    @Test
    public void testSuccessfulUnarchivingWithRealUnarchivingPreparation()
    {
        properties.setProperty(SHARE_FINDER_KEY + ".class", ShareFinder.class.getName());
        properties.setProperty(SHARE_FINDER_KEY + ".p1", "property 1");
        archiverTaskContext.setUnarchivingPreparation(unarchivingPreparation);
        final DatasetDescription ds1 =
                new DatasetDescriptionBuilder("ds1").experiment("exp1")
                        .location("loc1").project("p1").sample("s1").space("space").size(11l)
                        .type("my-type").getDatasetDescription();
        context.checking(new Expectations()
            {
                {
                    one(configProvider).getDataStoreCode();
                    will(returnValue(DATA_STORE_CODE));

                    one(shareIdManager).getShareId("ds1");
                    will(returnValue("2"));

                    one(dataSetDirectoryProvider).getDataSetDirectory(ds1);
                    File file = new File(store, LOCATION);
                    will(returnValue(file));

                    one(service).updateShareIdAndSize("ds1", "1", 11L);
                    one(shareIdManager).setShareId("ds1", "1");

                    one(fileOperationsManager).retrieveFromDestination(file, ds1);
                    will(returnValue(Status.OK));

                    one(statusUpdater).update(Arrays.asList("ds1"),
                            DataSetArchivingStatus.AVAILABLE, true);
                }
            });

        ProcessingStatus status = archiver.unarchive(Arrays.asList(ds1), archiverTaskContext);

        AssertionUtil.assertContainsLines("INFO  OPERATION.AbstractDatastorePlugin - "
                + "Unarchiving of the following datasets has been requested: [Dataset 'ds1']", logRecorder.getLogContent());
        assertEquals("[]", status.getErrorStatuses().toString());
        assertEquals(ShareFinder.class.getName(), ShareFinder.properties.getProperty("class"));
        assertEquals("property 1", ShareFinder.properties.getProperty("p1"));
        assertEquals("ds1", ShareFinder.recordedDataSet.getDataSetCode());
        assertEquals("loc1", ShareFinder.recordedDataSet.getDataSetLocation());
        assertEquals(null, ShareFinder.recordedDataSet.getDataSetShareId());
        assertEquals("my-type", ShareFinder.recordedDataSet.getDataSetType());
        assertEquals("exp1", ShareFinder.recordedDataSet.getExperimentCode());
        assertEquals("space", ShareFinder.recordedDataSet.getSpaceCode());
        assertEquals("p1", ShareFinder.recordedDataSet.getProjectCode());
        assertEquals("s1", ShareFinder.recordedDataSet.getSampleCode());
        assertEquals(Long.valueOf(11L), ShareFinder.recordedDataSet.getDataSetSize());
        assertEquals(share1, ShareFinder.recordedShares.get(0).getShare());
        assertEquals(share2, ShareFinder.recordedShares.get(1).getShare());
        assertEquals(2, ShareFinder.recordedShares.size());
    }

    @Test
    public void testFailingUnarchivingWhenNoShareHasBeenFound()
    {
        properties.setProperty(SHARE_FINDER_KEY + ".class", ShareFinder.class.getName());
        properties.setProperty(SHARE_FINDER_KEY + ".alwaysReturnNull", "true");
        archiverTaskContext.setUnarchivingPreparation(unarchivingPreparation);
        final DatasetDescription ds1 =
                new DatasetDescriptionBuilder("ds1").experiment("exp1")
                        .location("loc1").project("p1").sample("s1").space("space")
                        .type("my-type").getDatasetDescription();
        context.checking(new Expectations()
            {
                {
                    one(configProvider).getDataStoreCode();
                    will(returnValue(DATA_STORE_CODE));

                    one(statusUpdater).update(Arrays.asList("ds1"),
                            DataSetArchivingStatus.ARCHIVED, true);
                }
            });

        ProcessingStatus status = archiver.unarchive(Arrays.asList(ds1), archiverTaskContext);

        assertEquals(1, status.getErrorStatuses().size());
        Status errorStatus = status.getErrorStatuses().get(0);
        assertEquals("Unarchiving failed: Unarchiving of data set 'ds1' has failed, because no "
                + "appropriate destination share was found. Most probably there is not enough "
                + "free space in the data store.", errorStatus.tryGetErrorMessage());
    }

    @Test
    public void testUnarchivingWithDefaultShareFinder()
    {
        final DatasetDescription ds1 = new DatasetDescriptionBuilder("ds1").getDatasetDescription();
        final DatasetDescription ds2 = new DatasetDescriptionBuilder("ds2").getDatasetDescription();
        context.checking(new Expectations()
            {
                {
                    one(configProvider).getDataStoreCode();
                    will(returnValue(DATA_STORE_CODE));

                    one(statusUpdater).update(Arrays.asList("ds1", "ds2"),
                            DataSetArchivingStatus.ARCHIVED, true);
                }
            });

        ProcessingStatus status = archiver.unarchive(Arrays.asList(ds1, ds2), archiverTaskContext);
        assertEquals("[ERROR: \"Unarchiving failed: null\"]", status.getErrorStatuses().toString());
    }

    @Test
    public void testDeleteFromArchivePermanently()
    {
        properties.setProperty(RsyncArchiver.ONLY_MARK_AS_DELETED_KEY, "false");
        final DatasetLocation datasetLocation = new DatasetLocation();
        datasetLocation.setDataSetLocation("my-location");
        context.checking(new Expectations()
            {
                {
                    one(fileOperationsManager).deleteFromDestination(datasetLocation);
                    will(returnValue(Status.OK));
                }
            });

        archiver = new RsyncArchiver(properties, store, fileOperationsManagerFactory);
        archiver.deleteFromArchive(Arrays.asList(datasetLocation));
    }

    @Test
    public void testDeleteFromArchiveOnlyMarkAsDeleted()
    {
        final DatasetLocation datasetLocation = new DatasetLocation();
        datasetLocation.setDataSetLocation("my-location");
        context.checking(new Expectations()
            {
                {
                    one(fileOperationsManager).markAsDeleted(datasetLocation);
                    will(returnValue(Status.OK));
                }
            });

        archiver = new RsyncArchiver(properties, store, fileOperationsManagerFactory);
        archiver.deleteFromArchive(Arrays.asList(datasetLocation));
    }

    @Test
    public void testCheckHierarchySizeAndChecksumsHappyCase()
    {
        IHierarchicalContentNode root1 =
                new MockContent(":0:0", "a/:0:0", "a/f1.txt:5:-3", "a/f2.txt:15:13", "r.txt:7:17")
                        .getRootNode();
        IHierarchicalContentNode root2 =
                new MockContent(":0:0", "a/:0:0", "a/f2.txt:15:13", "a/f1.txt:5:-3", "r.txt:7:17")
                        .getRootNode();
        assertEquals(
                "OK",
                RsyncArchiver.checkHierarchySizeAndChecksums(root1, "", root2,
                        RsyncArchiver.ChecksumVerificationCondition.YES).toString());
    }

    @Test
    public void testCheckHierarchySizeAndChecksumsHappyCaseWithContainers()
    {
        IHierarchicalContentNode root1 =
                new MockContent(":0:0", "f2.txt:15:13", "f1.txt:5:-3")
                        .getRootNode();
        IHierarchicalContentNode root2 =
                new MockContent(":0:0", "a/:0:0", "a/f1.txt:5:-3", "a/f2.txt:15:13", "r.txt:7:17")
                        .getRootNode().getChildNodes().get(0);
        assertEquals(
                "OK",
                RsyncArchiver.checkHierarchySizeAndChecksums(root1, "a", root2,
                        RsyncArchiver.ChecksumVerificationCondition.YES).toString());
    }

    @Test
    public void testCheckHierarchySizeAndChecksumsWrongPaths()
    {
        IHierarchicalContentNode root1 =
                new MockContent(":0:0", "a/:0:0", "a/f1.txt:5:-3").getRootNode();
        IHierarchicalContentNode root2 =
                new MockContent(":0:0", "a/:0:0", "a/f3.txt:15:13").getRootNode();
        assertEquals(
                "ERROR: \"Different paths: Path in the store is 'a/f1.txt' "
                        + "and in the archive 'a/f3.txt'.\"",
                RsyncArchiver.checkHierarchySizeAndChecksums(root1, "", root2,
                        RsyncArchiver.ChecksumVerificationCondition.YES).toString());
    }

    @Test
    public void testCheckHierarchySizeAndChecksumsFileInsteadOfDirectory()
    {
        IHierarchicalContentNode root1 = new MockContent(":0:0", "a/:0:0").getRootNode();
        IHierarchicalContentNode root2 = new MockContent(":0:0", "a:1:2").getRootNode();
        assertEquals(
                "ERROR: \"The path 'a' should be in store and archive either "
                        + "both directories or files but not mixed: In the store it is a directory "
                        + "but in the archive it is a file.\"",
                RsyncArchiver.checkHierarchySizeAndChecksums(root1, "", root2,
                        RsyncArchiver.ChecksumVerificationCondition.YES).toString());
    }

    @Test
    public void testCheckHierarchySizeAndChecksumsWrongNumberOfChildren()
    {
        IHierarchicalContentNode root1 =
                new MockContent(":0:0", "a/:0:0", "a/f1.txt:5:-3", "a/f2.txt:15:13").getRootNode();
        IHierarchicalContentNode root2 =
                new MockContent(":0:0", "a/:0:0", "a/f2.txt:15:13").getRootNode();
        assertEquals(
                "ERROR: \"The directory 'a' has in the store 2 files but 1 in the archive.\"",
                RsyncArchiver.checkHierarchySizeAndChecksums(root1, "", root2,
                        RsyncArchiver.ChecksumVerificationCondition.YES).toString());
    }

    @Test
    public void testCheckHierarchySizeAndChecksumsWrongSize()
    {
        IHierarchicalContentNode root1 = new MockContent(":0:0", "r.txt:7:17").getRootNode();
        IHierarchicalContentNode root2 = new MockContent(":0:0", "r.txt:9:17").getRootNode();
        assertEquals(
                "ERROR: \"The file 'r.txt' has in the store 7 bytes but 9 in the archive.\"",
                RsyncArchiver.checkHierarchySizeAndChecksums(root1, "", root2,
                        RsyncArchiver.ChecksumVerificationCondition.YES).toString());
    }

    @Test
    public void testCheckHierarchySizeAndChecksumsWrongChecksum()
    {
        IHierarchicalContentNode root1 = new MockContent(":0:0", "r.txt:7:17").getRootNode();
        IHierarchicalContentNode root2 = new MockContent(":0:0", "r.txt:7:18").getRootNode();
        assertEquals(
                "ERROR: \"The file 'r.txt' has in the store the checksum 00000017 "
                        + "but 00000018 in the archive.\"",
                RsyncArchiver.checkHierarchySizeAndChecksums(root1, "", root2,
                        RsyncArchiver.ChecksumVerificationCondition.YES).toString());
    }

    public void testCheckHierarchySizeAndChecksumsWrongChecksumAreNotChecked()
    {
        IHierarchicalContentNode root1 = new MockContent(":0:0", "r.txt:7:17").getRootNode();
        IHierarchicalContentNode root2 = new MockContent(":0:0", "r.txt:7:18").getRootNode();
        assertEquals(
                "OK",
                RsyncArchiver.checkHierarchySizeAndChecksums(root1, "", root2,
                        RsyncArchiver.ChecksumVerificationCondition.NO).toString());
    }
}
