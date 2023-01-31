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

package ch.systemsx.cisd.etlserver.path;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import ch.systemsx.cisd.etlserver.postregistration.IPostRegistrationTaskExecutor;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.utilities.MockTimeProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = PathInfoDatabaseFeedingTask.class)
public class PathInfoDatabaseFeedingTaskTest extends AbstractFileSystemTestCase
{
    private static final File STORE_ROOT = new File("../datastore_server/resource/test-data/"
            + PathInfoDatabaseFeedingTaskTest.class.getSimpleName());

    private static final String DATA_SET_CODE = "ds1";

    private Mockery context;

    private IEncapsulatedOpenBISService service;

    private IDataSetDirectoryProvider directoryProvider;

    private IShareIdManager shareIdManager;

    private IPathsInfoDAO dao;

    private PathInfoDatabaseFeedingTask task;

    private File dataSetFolder;

    private MockPathsInfoDAO mockPathsInfoDAO;

    @BeforeMethod
    public void beforeMethod()
    {
        LogInitializer.init();
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        shareIdManager = context.mock(IShareIdManager.class);
        context.checking(new Expectations()
            {
                {
                    allowing(shareIdManager).lock(with(any(String.class)));
                    allowing(shareIdManager).getShareId(with(any(String.class)));
                    will(returnValue("1"));
                    allowing(shareIdManager).releaseLocks();
                }
            });
        directoryProvider = new DataSetDirectoryProvider(STORE_ROOT, shareIdManager);
        dao = context.mock(IPathsInfoDAO.class);
        context.checking(new Expectations()
            {
                {
                    allowing(dao).tryGetDataSetId(with(any(String.class)));
                }
            });
        mockPathsInfoDAO = new MockPathsInfoDAO();
        task = createTask(mockPathsInfoDAO, 0, 0, 0);
        dataSetFolder = new File(workingDirectory, "ds1");
        dataSetFolder.mkdirs();
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
    public void testChecksumTypeSHA1()
    {
        final SimpleDataSetInformationDTO ds1 = new SimpleDataSetInformationDTO();
        ds1.setDataSetCode("ds1");
        ds1.setDataSetLocation("2");
        ds1.setRegistrationTimestamp(new Date(78000));
        ds1.setStorageConfirmed(true);
        context.checking(new Expectations()
            {
                {
                    one(service).listOldestPhysicalDataSets(12);
                    will(returnValue(Arrays.asList(ds1)));

                }
            });

        MockPathsInfoDAO pathsInfoDAO = new MockPathsInfoDAO();
        createTask(pathsInfoDAO, "SHA1", 12, 3, 0).execute();

        assertEquals("createDataSet(code=ds1, location=2)\n"
                + "createDataSetFile(0, parent=null, 2 (, 3, d))\n"
                + "createDataSetFiles:\n"
                + "  0, parent=1, hi.txt (hi.txt, 3, f, checksumCRC32=ed6f7a7a, checksum=SHA1:55ca6286e3e4f4fba5d0448333fa99fc5a404a73)\n"
                + "commit()\n"
                + "deleteLastFeedingEvent()\n"
                + "createLastFeedingEvent(Thu Jan 01 01:01:18 CET 1970)\n"
                + "commit()\n", pathsInfoDAO.getLog());
    }

    @Test
    public void testH5AndH5arFolderDisabled()
    {
        final SimpleDataSetInformationDTO ds1 = new SimpleDataSetInformationDTO();
        ds1.setDataSetCode("ds1");
        ds1.setDataSetLocation("1");
        ds1.setRegistrationTimestamp(new Date(78000));
        ds1.setStorageConfirmed(true);
        context.checking(new Expectations()
            {
                {
                    one(service).listOldestPhysicalDataSets(12);
                    will(returnValue(Arrays.asList(ds1)));

                }
            });

        MockPathsInfoDAO pathsInfoDAO = new MockPathsInfoDAO();
        createTask(pathsInfoDAO, 12, 3, 0).execute();

        assertEquals("createDataSet(code=ds1, location=1)\n"
                + "createDataSetFile(0, parent=null, 1 (, 1025784, d))\n"
                + "createDataSetFile(0, parent=1, test-data (test-data, 1025774, d))\n"
                + "createDataSetFiles:\n"
                + "  0, parent=1, info.txt (info.txt, 10, f, checksumCRC32=176bdc9d)\n"
                + "  0, parent=2, farray.h5 (test-data/farray.h5, 8640, f, checksumCRC32=47dedeef)\n"
                + "  0, parent=2, thumbnails.h5ar (test-data/thumbnails.h5ar, 508567, f, checksumCRC32=9fb9b84a)\n"
                + "  0, parent=2, thumbnails2.h5 (test-data/thumbnails2.h5, 508567, f, checksumCRC32=9fb9b84a)\n"
                + "commit()\n"
                + "deleteLastFeedingEvent()\n"
                + "createLastFeedingEvent(Thu Jan 01 01:01:18 CET 1970)\n"
                + "commit()\n", pathsInfoDAO.getLog());
    }

    @Test
    public void testH5FolderEnabledWithSHA1()
    {
        final SimpleDataSetInformationDTO ds1 = new SimpleDataSetInformationDTO();
        ds1.setDataSetCode("ds1");
        ds1.setDataSetLocation("1");
        ds1.setRegistrationTimestamp(new Date(78000));
        ds1.setStorageConfirmed(true);
        ds1.setH5Folders(true);
        context.checking(new Expectations()
            {
                {
                    one(service).listOldestPhysicalDataSets(12);
                    will(returnValue(Arrays.asList(ds1)));

                }
            });

        MockPathsInfoDAO pathsInfoDAO = new MockPathsInfoDAO();
        createTask(pathsInfoDAO, "SHA1", 12, 3, 0).execute();

        assertEquals("createDataSet(code=ds1, location=1)\n"
                + "createDataSetFile(0, parent=null, 1 (, 1007978, d))\n"
                + "createDataSetFile(0, parent=1, test-data (test-data, 1007968, d))\n"
                + "createDataSetFile(0, parent=2, thumbnails2.h5 (test-data/thumbnails2.h5, 490761, d, checksumCRC32=9fb9b84a, checksum=SHA1:cfb5c11ae566a094c3d950ac0fd89057e3eecf56))\n"
                + "createDataSetFiles:\n"
                + "  0, parent=1, info.txt (info.txt, 10, f, checksumCRC32=176bdc9d, checksum=SHA1:a5105d3fcba551031e7abdb25f9bbdb2ad3a9ffa)\n"
                + "  0, parent=2, farray.h5 (test-data/farray.h5, 8640, f, checksumCRC32=47dedeef, checksum=SHA1:8f463b0c828b993efd441f602a0907d1bccb0234)\n"
                + "  0, parent=2, thumbnails.h5ar (test-data/thumbnails.h5ar, 508567, f, checksumCRC32=9fb9b84a, checksum=SHA1:cfb5c11ae566a094c3d950ac0fd89057e3eecf56)\n"
                + "  0, parent=3, wA1_d1-1_cCy3.png (test-data/thumbnails2.h5/wA1_d1-1_cCy3.png, 24242, f, checksumCRC32=3361fd20, checksum=SHA1:fc6eb645dc3a2934442358b4198042b1e2c8a3d4)\n"
                + "  0, parent=3, wA1_d1-1_cDAPI.png (test-data/thumbnails2.h5/wA1_d1-1_cDAPI.png, 29353, f, checksumCRC32=609f3183, checksum=SHA1:0707a40ffc2620ef0dc132fce642068e0fa8db9f)\n"
                + "  0, parent=3, wA1_d1-1_cGFP.png (test-data/thumbnails2.h5/wA1_d1-1_cGFP.png, 27211, f, checksumCRC32=b68f97cf, checksum=SHA1:1b63ea85ec0b020605dfdb9e52c66b81da628598)\n"
                + "  0, parent=3, wA1_d1-2_cCy3.png (test-data/thumbnails2.h5/wA1_d1-2_cCy3.png, 28279, f, checksumCRC32=e2c7c34f, checksum=SHA1:b9618a2404e2f80e72334c26dabed1b54d57797e)\n"
                + "  0, parent=3, wA1_d1-2_cDAPI.png (test-data/thumbnails2.h5/wA1_d1-2_cDAPI.png, 22246, f, checksumCRC32=1bf73b61, checksum=SHA1:dcd254b6a549e4a435a15e6d5e6b132882bb74f9)\n"
                + "  0, parent=3, wA1_d1-2_cGFP.png (test-data/thumbnails2.h5/wA1_d1-2_cGFP.png, 22227, f, checksumCRC32=58e14da9, checksum=SHA1:68ac59f992acbd413952063ea2a43ec3362f78df)\n"
                + "  0, parent=3, wA1_d2-1_cCy3.png (test-data/thumbnails2.h5/wA1_d2-1_cCy3.png, 31570, f, checksumCRC32=b312b087, checksum=SHA1:f3d3f624cb4e931e712f1a429dbb8e58782429e3)\n"
                + "  0, parent=3, wA1_d2-1_cDAPI.png (test-data/thumbnails2.h5/wA1_d2-1_cDAPI.png, 28267, f, checksumCRC32=e7082b23, checksum=SHA1:8e2bbc9ca305cdb8c5e22deb57d4aed95d17ede7)\n"
                + "  0, parent=3, wA1_d2-1_cGFP.png (test-data/thumbnails2.h5/wA1_d2-1_cGFP.png, 26972, f, checksumCRC32=fb7f320e, checksum=SHA1:f07f5c3886bc13bfc4a57376143aa48ce5896289)\n"
                + "  0, parent=3, wA1_d2-2_cCy3.png (test-data/thumbnails2.h5/wA1_d2-2_cCy3.png, 34420, f, checksumCRC32=d367dd9d, checksum=SHA1:9882f8963d344337ccab9e0dd961c723def7ab6f)\n"
                + "  0, parent=3, wA1_d2-2_cDAPI.png (test-data/thumbnails2.h5/wA1_d2-2_cDAPI.png, 28070, f, checksumCRC32=15e1f3b0, checksum=SHA1:05c8a28ddcb89738649440783696ea99343e08a4)\n"
                + "  0, parent=3, wA1_d2-2_cGFP.png (test-data/thumbnails2.h5/wA1_d2-2_cGFP.png, 27185, f, checksumCRC32=34bcde32, checksum=SHA1:91653d6e94a15414951c1b4d5de6753d97832b14)\n"
                + "  0, parent=3, wA1_d3-1_cCy3.png (test-data/thumbnails2.h5/wA1_d3-1_cCy3.png, 28916, f, checksumCRC32=a97cff4e, checksum=SHA1:b704d9d19ea811715185622db88496513ca0215f)\n"
                + "  0, parent=3, wA1_d3-1_cDAPI.png (test-data/thumbnails2.h5/wA1_d3-1_cDAPI.png, 30079, f, checksumCRC32=6f0abf6f, checksum=SHA1:8200d0ade2f8062031ce9805a37da5623995932b)\n"
                + "  0, parent=3, wA1_d3-1_cGFP.png (test-data/thumbnails2.h5/wA1_d3-1_cGFP.png, 28072, f, checksumCRC32=5ba6ae39, checksum=SHA1:7892abc5001e61e2e2133be11b7fd79a611c558d)\n"
                + "  0, parent=3, wA1_d3-2_cCy3.png (test-data/thumbnails2.h5/wA1_d3-2_cCy3.png, 26367, f, checksumCRC32=f8d4cfc7, checksum=SHA1:448f695916bd8275fd35082b6bd84997b420d190)\n"
                + "  0, parent=3, wA1_d3-2_cDAPI.png (test-data/thumbnails2.h5/wA1_d3-2_cDAPI.png, 25086, f, checksumCRC32=aeb12b1a, checksum=SHA1:07faef2f0d836b848b60d1459f73cac0ec08a555)\n"
                + "  0, parent=3, wA1_d3-2_cGFP.png (test-data/thumbnails2.h5/wA1_d3-2_cGFP.png, 22199, f, checksumCRC32=ced4332a, checksum=SHA1:e16f03d67e86d952cfec4a9a39065e7ec78beb99)\n"
                + "commit()\n"
                + "deleteLastFeedingEvent()\n"
                + "createLastFeedingEvent(Thu Jan 01 01:01:18 CET 1970)\n"
                + "commit()\n", pathsInfoDAO.getLog());
    }

    @Test
    public void testH5arFolderEnabled()
    {
        final SimpleDataSetInformationDTO ds1 = new SimpleDataSetInformationDTO();
        ds1.setDataSetCode("ds1");
        ds1.setDataSetLocation("1");
        ds1.setRegistrationTimestamp(new Date(78000));
        ds1.setStorageConfirmed(true);
        ds1.setH5arFolders(true);
        context.checking(new Expectations()
            {
                {
                    one(service).listOldestPhysicalDataSets(12);
                    will(returnValue(Arrays.asList(ds1)));

                }
            });

        MockPathsInfoDAO pathsInfoDAO = new MockPathsInfoDAO();
        createTask(pathsInfoDAO, 12, 3, 0).execute();

        assertEquals("createDataSet(code=ds1, location=1)\n"
                + "createDataSetFile(0, parent=null, 1 (, 1007978, d))\n"
                + "createDataSetFile(0, parent=1, test-data (test-data, 1007968, d))\n"
                + "createDataSetFile(0, parent=2, thumbnails.h5ar (test-data/thumbnails.h5ar, 490761, d, checksumCRC32=9fb9b84a))\n"
                + "createDataSetFiles:\n"
                + "  0, parent=1, info.txt (info.txt, 10, f, checksumCRC32=176bdc9d)\n"
                + "  0, parent=2, farray.h5 (test-data/farray.h5, 8640, f, checksumCRC32=47dedeef)\n"
                + "  0, parent=3, wA1_d1-1_cCy3.png (test-data/thumbnails.h5ar/wA1_d1-1_cCy3.png, 24242, f, checksumCRC32=3361fd20)\n"
                + "  0, parent=3, wA1_d1-1_cDAPI.png (test-data/thumbnails.h5ar/wA1_d1-1_cDAPI.png, 29353, f, checksumCRC32=609f3183)\n"
                + "  0, parent=3, wA1_d1-1_cGFP.png (test-data/thumbnails.h5ar/wA1_d1-1_cGFP.png, 27211, f, checksumCRC32=b68f97cf)\n"
                + "  0, parent=3, wA1_d1-2_cCy3.png (test-data/thumbnails.h5ar/wA1_d1-2_cCy3.png, 28279, f, checksumCRC32=e2c7c34f)\n"
                + "  0, parent=3, wA1_d1-2_cDAPI.png (test-data/thumbnails.h5ar/wA1_d1-2_cDAPI.png, 22246, f, checksumCRC32=1bf73b61)\n"
                + "  0, parent=3, wA1_d1-2_cGFP.png (test-data/thumbnails.h5ar/wA1_d1-2_cGFP.png, 22227, f, checksumCRC32=58e14da9)\n"
                + "  0, parent=3, wA1_d2-1_cCy3.png (test-data/thumbnails.h5ar/wA1_d2-1_cCy3.png, 31570, f, checksumCRC32=b312b087)\n"
                + "  0, parent=3, wA1_d2-1_cDAPI.png (test-data/thumbnails.h5ar/wA1_d2-1_cDAPI.png, 28267, f, checksumCRC32=e7082b23)\n"
                + "  0, parent=3, wA1_d2-1_cGFP.png (test-data/thumbnails.h5ar/wA1_d2-1_cGFP.png, 26972, f, checksumCRC32=fb7f320e)\n"
                + "  0, parent=3, wA1_d2-2_cCy3.png (test-data/thumbnails.h5ar/wA1_d2-2_cCy3.png, 34420, f, checksumCRC32=d367dd9d)\n"
                + "  0, parent=3, wA1_d2-2_cDAPI.png (test-data/thumbnails.h5ar/wA1_d2-2_cDAPI.png, 28070, f, checksumCRC32=15e1f3b0)\n"
                + "  0, parent=3, wA1_d2-2_cGFP.png (test-data/thumbnails.h5ar/wA1_d2-2_cGFP.png, 27185, f, checksumCRC32=34bcde32)\n"
                + "  0, parent=3, wA1_d3-1_cCy3.png (test-data/thumbnails.h5ar/wA1_d3-1_cCy3.png, 28916, f, checksumCRC32=a97cff4e)\n"
                + "  0, parent=3, wA1_d3-1_cDAPI.png (test-data/thumbnails.h5ar/wA1_d3-1_cDAPI.png, 30079, f, checksumCRC32=6f0abf6f)\n"
                + "  0, parent=3, wA1_d3-1_cGFP.png (test-data/thumbnails.h5ar/wA1_d3-1_cGFP.png, 28072, f, checksumCRC32=5ba6ae39)\n"
                + "  0, parent=3, wA1_d3-2_cCy3.png (test-data/thumbnails.h5ar/wA1_d3-2_cCy3.png, 26367, f, checksumCRC32=f8d4cfc7)\n"
                + "  0, parent=3, wA1_d3-2_cDAPI.png (test-data/thumbnails.h5ar/wA1_d3-2_cDAPI.png, 25086, f, checksumCRC32=aeb12b1a)\n"
                + "  0, parent=3, wA1_d3-2_cGFP.png (test-data/thumbnails.h5ar/wA1_d3-2_cGFP.png, 22199, f, checksumCRC32=ced4332a)\n"
                + "  0, parent=2, thumbnails2.h5 (test-data/thumbnails2.h5, 508567, f, checksumCRC32=9fb9b84a)\n"
                + "commit()\n"
                + "deleteLastFeedingEvent()\n"
                + "createLastFeedingEvent(Thu Jan 01 01:01:18 CET 1970)\n"
                + "commit()\n", pathsInfoDAO.getLog());
    }

    @Test
    public void testAsMaintenanceTask()
    {
        final SimpleDataSetInformationDTO ds1 = new SimpleDataSetInformationDTO();
        ds1.setDataSetCode("ds1");
        ds1.setDataSetLocation("2");
        ds1.setRegistrationTimestamp(new Date(78000));
        ds1.setStorageConfirmed(true);
        context.checking(new Expectations()
            {
                {
                    one(service).listOldestPhysicalDataSets(12);
                    will(returnValue(Arrays.asList(ds1)));

                }
            });

        MockPathsInfoDAO pathsInfoDAO = new MockPathsInfoDAO();
        createTask(pathsInfoDAO, 12, 3, 0).execute();

        assertEquals("createDataSet(code=ds1, location=2)\n"
                + "createDataSetFile(0, parent=null, 2 (, 3, d))\n"
                + "createDataSetFiles:\n"
                + "  0, parent=1, hi.txt (hi.txt, 3, f, checksumCRC32=ed6f7a7a)\n"
                + "commit()\n"
                + "deleteLastFeedingEvent()\n"
                + "createLastFeedingEvent(Thu Jan 01 01:01:18 CET 1970)\n"
                + "commit()\n", pathsInfoDAO.getLog());
    }

    @Test
    public void testNonConfirmed()
    {
        final SimpleDataSetInformationDTO ds1NonConfirmed = new SimpleDataSetInformationDTO();
        ds1NonConfirmed.setDataSetCode("ds1");
        ds1NonConfirmed.setDataSetLocation("2");
        ds1NonConfirmed.setRegistrationTimestamp(new Date(78000));
        ds1NonConfirmed.setStorageConfirmed(false);

        final SimpleDataSetInformationDTO ds1Confirmed = new SimpleDataSetInformationDTO();
        ds1Confirmed.setDataSetCode("ds1");
        ds1Confirmed.setDataSetLocation("2");
        ds1Confirmed.setRegistrationTimestamp(new Date(79000));
        ds1Confirmed.setStorageConfirmed(true);

        context.checking(new Expectations()
            {
                {
                    one(service).listOldestPhysicalDataSets(12);
                    will(returnValue(Arrays.asList(ds1NonConfirmed)));

                    one(service).listOldestPhysicalDataSets(12);
                    will(returnValue(Arrays.asList(ds1Confirmed)));

                }
            });

        MockPathsInfoDAO pathsInfoDAO = new MockPathsInfoDAO();
        createTask(pathsInfoDAO, 12, 3, 0).execute();

        assertEquals("createDataSet(code=ds1, location=2)\n"
                + "createDataSetFile(0, parent=null, 2 (, 3, d))\n"
                + "createDataSetFiles:\n"
                + "  0, parent=1, hi.txt (hi.txt, 3, f, checksumCRC32=ed6f7a7a)\n"
                + "commit()\n"
                + "deleteLastFeedingEvent()\n"
                + "createLastFeedingEvent(Thu Jan 01 01:01:19 CET 1970)\n"
                + "commit()\n", pathsInfoDAO.getLog());
    }

    @Test
    public void testAsMaintenanceTaskWithFiniteNumberOfChunks()
    {
        final SimpleDataSetInformationDTO ds1 = dataSet(1000);
        final SimpleDataSetInformationDTO ds2 = dataSet(2000);
        final SimpleDataSetInformationDTO ds3 = dataSet(3000);
        final SimpleDataSetInformationDTO ds4 = dataSet(4000);
        final SimpleDataSetInformationDTO ds5 = dataSet(5000);
        final SimpleDataSetInformationDTO ds6 = dataSet(6000);
        final Sequence chunkReadingSequence = context.sequence("chunkReadingSequence");
        context.checking(new Expectations()
            {
                {
                    one(service).listOldestPhysicalDataSets(2);
                    will(returnValue(Arrays.asList(ds1, ds2)));
                    inSequence(chunkReadingSequence);

                    one(service).listOldestPhysicalDataSets(new Date(2000), 2);
                    will(returnValue(Arrays.asList(ds3, ds4)));
                    inSequence(chunkReadingSequence);

                    one(service).listOldestPhysicalDataSets(new Date(4000), 2);
                    will(returnValue(Arrays.asList(ds5, ds6)));
                    inSequence(chunkReadingSequence);
                }
            });

        MockPathsInfoDAO pathsInfoDAO = new MockPathsInfoDAO();
        createTask(pathsInfoDAO, 2, 3, 0).execute();

        assertEquals("createDataSet(code=DS-1000, location=3)\n"
                + "createDataSetFile(0, parent=null, 3 (, 16, d))\n"
                + "createDataSetFiles:\n"
                + "  0, parent=1, readme.txt (readme.txt, 16, f, checksumCRC32=379d0103)\n"
                + "commit()\n"
                + "createDataSet(code=DS-2000, location=2)\n"
                + "createDataSetFile(2, parent=null, 2 (, 3, d))\n"
                + "createDataSetFiles:\n"
                + "  2, parent=3, hi.txt (hi.txt, 3, f, checksumCRC32=ed6f7a7a)\n"
                + "commit()\n"
                + "deleteLastFeedingEvent()\n"
                + "createLastFeedingEvent(Thu Jan 01 01:00:02 CET 1970)\n"
                + "commit()\n"
                + "createDataSet(code=DS-3000, location=3)\n"
                + "createDataSetFile(4, parent=null, 3 (, 16, d))\n"
                + "createDataSetFiles:\n"
                + "  4, parent=5, readme.txt (readme.txt, 16, f, checksumCRC32=379d0103)\n"
                + "commit()\n"
                + "createDataSet(code=DS-4000, location=2)\n"
                + "createDataSetFile(6, parent=null, 2 (, 3, d))\n"
                + "createDataSetFiles:\n"
                + "  6, parent=7, hi.txt (hi.txt, 3, f, checksumCRC32=ed6f7a7a)\n"
                + "commit()\n"
                + "deleteLastFeedingEvent()\n"
                + "createLastFeedingEvent(Thu Jan 01 01:00:04 CET 1970)\n"
                + "commit()\n"
                + "createDataSet(code=DS-5000, location=3)\n"
                + "createDataSetFile(8, parent=null, 3 (, 16, d))\n"
                + "createDataSetFiles:\n"
                + "  8, parent=9, readme.txt (readme.txt, 16, f, checksumCRC32=379d0103)\n"
                + "commit()\n"
                + "createDataSet(code=DS-6000, location=2)\n"
                + "createDataSetFile(10, parent=null, 2 (, 3, d))\n"
                + "createDataSetFiles:\n"
                + "  10, parent=11, hi.txt (hi.txt, 3, f, checksumCRC32=ed6f7a7a)\n"
                + "commit()\n"
                + "deleteLastFeedingEvent()\n"
                + "createLastFeedingEvent(Thu Jan 01 01:00:06 CET 1970)\n"
                + "commit()\n", pathsInfoDAO.getLog());
    }

    @Test
    public void testAsMaintenanceTaskWithFiniteTimeLimit()
    {
        final SimpleDataSetInformationDTO ds1 = dataSet(1000);
        final SimpleDataSetInformationDTO ds2 = dataSet(2000);
        final SimpleDataSetInformationDTO ds3 = dataSet(3000);
        final SimpleDataSetInformationDTO ds4 = dataSet(4000);
        final Sequence chunkReadingSequence = context.sequence("chunkReadingSequence");
        context.checking(new Expectations()
            {
                {
                    one(service).listOldestPhysicalDataSets(2);
                    will(returnValue(Arrays.asList(ds1, ds2)));
                    inSequence(chunkReadingSequence);

                    one(service).listOldestPhysicalDataSets(new Date(2000), 2);
                    will(returnValue(Arrays.asList(ds3, ds4)));
                    inSequence(chunkReadingSequence);

                }
            });

        MockPathsInfoDAO pathsInfoDAO = new MockPathsInfoDAO();
        createTask(pathsInfoDAO, 2, 0, 1500).execute();

        assertEquals("createDataSet(code=DS-1000, location=3)\n"
                + "createDataSetFile(0, parent=null, 3 (, 16, d))\n"
                + "createDataSetFiles:\n"
                + "  0, parent=1, readme.txt (readme.txt, 16, f, checksumCRC32=379d0103)\n"
                + "commit()\n"
                + "createDataSet(code=DS-2000, location=2)\n"
                + "createDataSetFile(2, parent=null, 2 (, 3, d))\n"
                + "createDataSetFiles:\n"
                + "  2, parent=3, hi.txt (hi.txt, 3, f, checksumCRC32=ed6f7a7a)\n"
                + "commit()\n"
                + "deleteLastFeedingEvent()\n"
                + "createLastFeedingEvent(Thu Jan 01 01:00:02 CET 1970)\n"
                + "commit()\n"
                + "createDataSet(code=DS-3000, location=3)\n"
                + "createDataSetFile(4, parent=null, 3 (, 16, d))\n"
                + "createDataSetFiles:\n"
                + "  4, parent=5, readme.txt (readme.txt, 16, f, checksumCRC32=379d0103)\n"
                + "commit()\n"
                + "createDataSet(code=DS-4000, location=2)\n"
                + "createDataSetFile(6, parent=null, 2 (, 3, d))\n"
                + "createDataSetFiles:\n"
                + "  6, parent=7, hi.txt (hi.txt, 3, f, checksumCRC32=ed6f7a7a)\n"
                + "commit()\n"
                + "deleteLastFeedingEvent()\n"
                + "createLastFeedingEvent(Thu Jan 01 01:00:04 CET 1970)\n"
                + "commit()\n", pathsInfoDAO.getLog());
    }

    @Test
    public void testAsMaintenanceTaskUnlimited()
    {
        final SimpleDataSetInformationDTO ds1 = dataSet("ds1", 1000, "3");
        final SimpleDataSetInformationDTO ds2 = dataSet("ds2", 2000, "2");
        final SimpleDataSetInformationDTO ds3 = dataSet("ds3", 3000, "3");
        final Sequence chunkReadingSequence = context.sequence("chunkReadingSequence");
        context.checking(new Expectations()
            {
                {
                    one(service).listOldestPhysicalDataSets(2);
                    will(returnValue(Arrays.asList(ds1, ds2)));
                    inSequence(chunkReadingSequence);

                    one(service).listOldestPhysicalDataSets(new Date(2000), 2);
                    will(returnValue(Arrays.asList(ds3)));
                    inSequence(chunkReadingSequence);
                }
            });

        MockPathsInfoDAO pathsInfoDAO = new MockPathsInfoDAO();
        createTask(pathsInfoDAO, 2, 0, 0).execute();

        assertEquals("createDataSet(code=ds1, location=3)\n"
                + "createDataSetFile(0, parent=null, 3 (, 16, d))\n"
                + "createDataSetFiles:\n"
                + "  0, parent=1, readme.txt (readme.txt, 16, f, checksumCRC32=379d0103)\n"
                + "commit()\n"
                + "createDataSet(code=ds2, location=2)\n"
                + "createDataSetFile(2, parent=null, 2 (, 3, d))\n"
                + "createDataSetFiles:\n"
                + "  2, parent=3, hi.txt (hi.txt, 3, f, checksumCRC32=ed6f7a7a)\n"
                + "commit()\n"
                + "deleteLastFeedingEvent()\n"
                + "createLastFeedingEvent(Thu Jan 01 01:00:02 CET 1970)\n"
                + "commit()\n"
                + "createDataSet(code=ds3, location=3)\n"
                + "createDataSetFile(4, parent=null, 3 (, 16, d))\n"
                + "createDataSetFiles:\n"
                + "  4, parent=5, readme.txt (readme.txt, 16, f, checksumCRC32=379d0103)\n"
                + "commit()\n"
                + "deleteLastFeedingEvent()\n"
                + "createLastFeedingEvent(Thu Jan 01 01:00:03 CET 1970)\n"
                + "commit()\n", pathsInfoDAO.getLog());
    }

    @Test
    public void testToSmallChunkSizeBecauseAllRegistrationTimeStampAreTheSame()
    {
        final SimpleDataSetInformationDTO ds1 = dataSet("ds1", 1000);
        final SimpleDataSetInformationDTO ds2 = dataSet("ds2", 1000);
        final SimpleDataSetInformationDTO ds3 = dataSet("ds3", 1000);

        final Sequence chunkReadingSequence = context.sequence("chunkReadingSequence");
        context.checking(new Expectations()
            {
                {
                    one(service).listOldestPhysicalDataSets(2);
                    will(returnValue(Arrays.asList(ds1, ds2)));
                    inSequence(chunkReadingSequence);

                    one(service).listOldestPhysicalDataSets(4);
                    will(returnValue(Arrays.asList(ds1, ds2, ds3)));
                    inSequence(chunkReadingSequence);

                    one(service).listOldestPhysicalDataSets(new Date(1000), 2);
                    will(returnValue(Arrays.asList(ds1, ds2)));
                    inSequence(chunkReadingSequence);

                    one(service).listOldestPhysicalDataSets(new Date(1000), 4);
                    will(returnValue(Arrays.asList(ds1, ds2, ds3)));
                    inSequence(chunkReadingSequence);

                }
            });

        MockPathsInfoDAO pathsInfoDAO = new MockPathsInfoDAO();
        createTask(pathsInfoDAO, 2, 0, 0).execute();

        assertEquals("createDataSet(code=ds1, location=3)\n"
                + "createDataSetFile(0, parent=null, 3 (, 16, d))\n"
                + "createDataSetFiles:\n"
                + "  0, parent=1, readme.txt (readme.txt, 16, f, checksumCRC32=379d0103)\n"
                + "commit()\n"
                + "createDataSet(code=ds2, location=3)\n"
                + "createDataSetFile(2, parent=null, 3 (, 16, d))\n"
                + "createDataSetFiles:\n"
                + "  2, parent=3, readme.txt (readme.txt, 16, f, checksumCRC32=379d0103)\n"
                + "commit()\n"
                + "createDataSet(code=ds3, location=3)\n"
                + "createDataSetFile(4, parent=null, 3 (, 16, d))\n"
                + "createDataSetFiles:\n"
                + "  4, parent=5, readme.txt (readme.txt, 16, f, checksumCRC32=379d0103)\n"
                + "commit()\n"
                + "deleteLastFeedingEvent()\n"
                + "createLastFeedingEvent(Thu Jan 01 01:00:01 CET 1970)\n"
                + "commit()\n", pathsInfoDAO.getLog());
    }

    @Test
    public void testToSmallChunkSizeBecauseOfSameRegistrationTimeStamp()
    {
        final SimpleDataSetInformationDTO ds1 = dataSet("ds1", 1000);
        final SimpleDataSetInformationDTO ds2 = dataSet("ds2", 1000);
        final SimpleDataSetInformationDTO ds3 = dataSet("ds3", 1000);
        final SimpleDataSetInformationDTO ds4 = dataSet("ds4", 2000);

        final Sequence chunkReadingSequence = context.sequence("chunkReadingSequence");
        context.checking(new Expectations()
            {
                {
                    one(service).listOldestPhysicalDataSets(2);
                    will(returnValue(Arrays.asList(ds1, ds2)));
                    inSequence(chunkReadingSequence);

                    one(service).listOldestPhysicalDataSets(4);
                    will(returnValue(Arrays.asList(ds1, ds2, ds3, ds4)));
                    inSequence(chunkReadingSequence);

                    one(service).listOldestPhysicalDataSets(new Date(2000), 2);
                    will(returnValue(Arrays.asList()));
                    inSequence(chunkReadingSequence);

                }
            });

        MockPathsInfoDAO pathsInfoDAO = new MockPathsInfoDAO();
        createTask(pathsInfoDAO, 2, 0, 0).execute();

        assertEquals("createDataSet(code=ds1, location=3)\n"
                + "createDataSetFile(0, parent=null, 3 (, 16, d))\n"
                + "createDataSetFiles:\n"
                + "  0, parent=1, readme.txt (readme.txt, 16, f, checksumCRC32=379d0103)\n"
                + "commit()\n"
                + "createDataSet(code=ds2, location=3)\n"
                + "createDataSetFile(2, parent=null, 3 (, 16, d))\n"
                + "createDataSetFiles:\n"
                + "  2, parent=3, readme.txt (readme.txt, 16, f, checksumCRC32=379d0103)\n"
                + "commit()\n"
                + "createDataSet(code=ds3, location=3)\n"
                + "createDataSetFile(4, parent=null, 3 (, 16, d))\n"
                + "createDataSetFiles:\n"
                + "  4, parent=5, readme.txt (readme.txt, 16, f, checksumCRC32=379d0103)\n"
                + "commit()\n"
                + "createDataSet(code=ds4, location=2)\n"
                + "createDataSetFile(6, parent=null, 2 (, 3, d))\n"
                + "createDataSetFiles:\n"
                + "  6, parent=7, hi.txt (hi.txt, 3, f, checksumCRC32=ed6f7a7a)\n"
                + "commit()\n"
                + "deleteLastFeedingEvent()\n"
                + "createLastFeedingEvent(Thu Jan 01 01:00:02 CET 1970)\n"
                + "commit()\n", pathsInfoDAO.getLog());
    }

    @Test
    public void testPostRegistrationHappyCase()
    {
        final PhysicalDataSet dataSet =
                new DataSetBuilder().code(DATA_SET_CODE).location("2").getDataSet();
        context.checking(new Expectations()
            {
                {
                    one(service).tryGetDataSet(DATA_SET_CODE);
                    will(returnValue(dataSet));

                    one(service).updatePhysicalDataSetsSize(Collections.singletonMap(DATA_SET_CODE, 3L));
                }
            });

        task.createExecutor(DATA_SET_CODE, false).execute();

        assertEquals("createDataSet(code=ds1, location=2)\n"
                + "createDataSetFile(0, parent=null, 2 (, 3, d))\n"
                + "createDataSetFiles:\n"
                + "  0, parent=1, hi.txt (hi.txt, 3, f, checksumCRC32=ed6f7a7a)\n"
                + "commit()\n", mockPathsInfoDAO.getLog());
    }

    @Test
    public void testPostRegistrationFailingCase()
    {
        final PhysicalDataSet dataSet =
                new DataSetBuilder().code(DATA_SET_CODE).location("2").getDataSet();
        context.checking(new Expectations()
            {
                {
                    one(service).tryGetDataSet(DATA_SET_CODE);
                    will(returnValue(dataSet));
                }
            });

        IPostRegistrationTaskExecutor executor = task.createExecutor(DATA_SET_CODE, false);
        mockPathsInfoDAO.addException(1L, new RuntimeException("Oops!"));

        try
        {
            executor.execute();
        } catch (Throwable ex)
        {
            assertEquals("Oops!", ex.getMessage());
        }

        assertEquals("createDataSet(code=ds1, location=2)\n"
                + "createDataSetFile(0, parent=null, 2 (, 3, d))\n"
                + "createDataSetFiles:\n"
                + "ERROR:java.lang.RuntimeException: Oops!\n"
                + "rollback()\n", mockPathsInfoDAO.getLog());
    }

    @Test
    public void testNonExistingDataSetFolder()
    {
        context.checking(new Expectations()
            {
                {
                    one(service).tryGetDataSet(DATA_SET_CODE);
                    PhysicalDataSet dataSet =
                            new DataSetBuilder().code(DATA_SET_CODE).location("abc").getDataSet();
                    will(returnValue(dataSet));
                }
            });

        task.createExecutor(DATA_SET_CODE, false).execute();

        assertEquals("", mockPathsInfoDAO.getLog());
    }

    @Test
    public void testAlreadyExistingDataSetInDatabase()
    {
        context.checking(new Expectations()
            {
                {
                    one(service).tryGetDataSet(DATA_SET_CODE);
                    PhysicalDataSet dataSet =
                            new DataSetBuilder().code(DATA_SET_CODE).location("abc").getDataSet();
                    will(returnValue(dataSet));

                }
            });

        mockPathsInfoDAO.setDataSetId(42);
        task.createExecutor(DATA_SET_CODE, false).execute();

        assertEquals("", mockPathsInfoDAO.getLog());
    }

    private SimpleDataSetInformationDTO dataSet(long timeStamp)
    {
        return dataSet("DS-" + timeStamp, timeStamp);
    }

    private SimpleDataSetInformationDTO dataSet(String dataSetCode, long timeStamp)
    {
        return dataSet(dataSetCode, timeStamp, (timeStamp / 1000) % 2 == 0 ? "2" : "3");
    }

    private SimpleDataSetInformationDTO dataSet(String dataSetCode, long timeStamp, String location)
    {
        SimpleDataSetInformationDTO dataSet = new SimpleDataSetInformationDTO();
        dataSet.setDataSetCode(dataSetCode);
        dataSet.setRegistrationTimestamp(new Date(timeStamp));
        dataSet.setDataSetLocation(location);
        dataSet.setStorageConfirmed(true);
        return dataSet;
    }

    private PathInfoDatabaseFeedingTask createTask(IPathsInfoDAO pathsInfoDAO,
            int chunkSize, int maxNumberOfChunks, long timeLimite)
    {
        return createTask(pathsInfoDAO, null, chunkSize, maxNumberOfChunks, timeLimite);
    }

    private PathInfoDatabaseFeedingTask createTask(IPathsInfoDAO pathsInfoDAO, String checksumType,
            int chunkSize, int maxNumberOfChunks, long timeLimite)
    {
        return new PathInfoDatabaseFeedingTask(service, directoryProvider, pathsInfoDAO,
                new MockTimeProvider(0, 1000), true, checksumType, chunkSize, maxNumberOfChunks, timeLimite);
    }

}
