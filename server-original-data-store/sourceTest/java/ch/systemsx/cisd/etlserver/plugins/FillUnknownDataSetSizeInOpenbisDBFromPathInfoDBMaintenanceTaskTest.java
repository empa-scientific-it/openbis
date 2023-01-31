/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.plugins;

import static ch.systemsx.cisd.common.test.ArrayContainsExactlyMatcher.containsExactly;
import static ch.systemsx.cisd.etlserver.plugins.FillUnknownDataSetSizeInOpenbisDBFromPathInfoDBMaintenanceTask.CHUNK_SIZE_DEFAULT;
import static ch.systemsx.cisd.etlserver.plugins.FillUnknownDataSetSizeInOpenbisDBFromPathInfoDBMaintenanceTask.CHUNK_SIZE_PROPERTY;
import static ch.systemsx.cisd.etlserver.plugins.FillUnknownDataSetSizeInOpenbisDBFromPathInfoDBMaintenanceTask.DELETE_LAST_SEEN_DATA_SET_FILE_INTERVAL_PROPERTY;
import static ch.systemsx.cisd.etlserver.plugins.FillUnknownDataSetSizeInOpenbisDBFromPathInfoDBMaintenanceTask.LAST_SEEN_DATA_SET_FILE_PROPERTY;
import static ch.systemsx.cisd.etlserver.plugins.FillUnknownDataSetSizeInOpenbisDBFromPathInfoDBMaintenanceTask.TIME_LIMIT_PROPERTY;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.common.utilities.TestResources;
import ch.systemsx.cisd.etlserver.path.IPathsInfoDAO;
import ch.systemsx.cisd.etlserver.path.PathEntryDTO;
import ch.systemsx.cisd.etlserver.plugins.FillUnknownDataSetSizeInOpenbisDBFromPathInfoDBMaintenanceTask.LastSeenDataSetFileContent;
import ch.systemsx.cisd.openbis.dss.generic.shared.IConfigProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * @author pkupczyk
 */
public class FillUnknownDataSetSizeInOpenbisDBFromPathInfoDBMaintenanceTaskTest
{

    private Mockery context;

    private IEncapsulatedOpenBISService service;

    private IPathsInfoDAO dao;

    private ITimeProvider timeProvider;

    private IConfigProvider configProvider;

    private SimpleDataSetInformationDTO dataSet1;

    private SimpleDataSetInformationDTO dataSet2;

    private SimpleDataSetInformationDTO dataSet3;

    private SimpleDataSetInformationDTO dataSet4;

    private PathEntryDTO entry1;

    private PathEntryDTO entry2;

    private PathEntryDTO entry4;

    private BufferedAppender logRecorder;

    @BeforeMethod
    public void beforeMethod()
    {
        LogInitializer.init();
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.DEBUG);

        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        dao = context.mock(IPathsInfoDAO.class);
        timeProvider = context.mock(ITimeProvider.class);
        configProvider = context.mock(IConfigProvider.class);

        dataSet1 = new SimpleDataSetInformationDTO();
        dataSet1.setDataSetCode("DS_1");
        dataSet2 = new SimpleDataSetInformationDTO();
        dataSet2.setDataSetCode("DS_2");
        dataSet3 = new SimpleDataSetInformationDTO();
        dataSet3.setDataSetCode("DS_3");
        dataSet4 = new SimpleDataSetInformationDTO();
        dataSet4.setDataSetCode("DS_4");

        entry1 = new PathEntryDTO();
        entry1.setDataSetCode("DS_1");
        entry1.setSizeInBytes(123L);

        entry2 = new PathEntryDTO();
        entry2.setDataSetCode("DS_2");

        entry4 = new PathEntryDTO();
        entry4.setDataSetCode("DS_4");
        entry4.setSizeInBytes(234L);

        File storeRoot = getStoreRoot();

        if (storeRoot.exists())
        {
            FileUtilities.deleteRecursively(storeRoot);
        }

        getStoreRoot().mkdirs();

        context.checking(new Expectations()
            {
                {
                    allowing(configProvider).getStoreRoot();
                    will(returnValue(getStoreRoot()));
                }
            });
    }

    @AfterMethod
    public void afterMethod(Method method)
    {
        System.out.println("======= Log content for " + method.getName() + "():");
        System.out.println(logRecorder.getLogContent());
        System.out.println("=======");
        logRecorder.reset();

        context.assertIsSatisfied();
    }

    @Test
    public void testExecuteWhereListOfDataSetsWithUnknownSizeInOpenbisDBIsEmpty()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(timeProvider).getTimeInMilliseconds();
                    will(returnValue(0L));

                    one(service).listPhysicalDataSetsWithUnknownSize(CHUNK_SIZE_DEFAULT, null);
                    will(returnValue(Collections.emptyList()));
                }
            });

        execute(null, null, null, null);

        assertLogThatAllDataSetsHaveBeenFixed();
    }

    @Test
    public void testExecuteWhereListOfDataSetsWithUnknownSizeInOpenbisDBIsNull()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(timeProvider).getTimeInMilliseconds();
                    will(returnValue(0L));

                    one(service).listPhysicalDataSetsWithUnknownSize(CHUNK_SIZE_DEFAULT, null);
                    will(returnValue(null));
                }
            });

        execute(null, null, null, null);

        assertLogThatAllDataSetsHaveBeenFixed();
    }

    @Test
    public void testExecuteWhereListOfDataSetSizesFoundInPathinfoDBIsEmpty()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(timeProvider).getTimeInMilliseconds();
                    will(returnValue(0L));

                    one(service).listPhysicalDataSetsWithUnknownSize(CHUNK_SIZE_DEFAULT, null);
                    will(returnValue(Arrays.asList(dataSet1)));

                    one(dao).listDataSetsSize(new String[] { dataSet1.getDataSetCode() });
                    will(returnValue(Collections.emptyList()));
                    
                    one(dao).commit();

                    one(service).listPhysicalDataSetsWithUnknownSize(CHUNK_SIZE_DEFAULT, dataSet1.getDataSetCode());
                    will(returnValue(Collections.emptyList()));
                }
            });

        execute(null, null, null, null);

        assertLogThatSomeDataSetsHaveNotBeenFixedYet();
    }

    @Test
    public void testExecuteWhereListOfDataSetSizesFoundInPathinfoDBIsNull()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(timeProvider).getTimeInMilliseconds();
                    will(returnValue(0L));

                    one(service).listPhysicalDataSetsWithUnknownSize(CHUNK_SIZE_DEFAULT, null);
                    will(returnValue(Arrays.asList(dataSet1)));

                    one(dao).listDataSetsSize(new String[] { dataSet1.getDataSetCode() });
                    will(returnValue(null));
                    
                    one(dao).commit();

                    one(service).listPhysicalDataSetsWithUnknownSize(CHUNK_SIZE_DEFAULT, dataSet1.getDataSetCode());
                    will(returnValue(Collections.emptyList()));
                }
            });

        execute(null, null, null, null);

        assertLogThatSomeDataSetsHaveNotBeenFixedYet();
    }

    @Test
    public void testExecuteWithOneChunk()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(timeProvider).getTimeInMilliseconds();
                    will(returnValue(0L));

                    one(service).listPhysicalDataSetsWithUnknownSize(CHUNK_SIZE_DEFAULT, null);
                    will(returnValue(Arrays.asList(dataSet1, dataSet2, dataSet3)));

                    one(dao).listDataSetsSize(
                            with(containsExactly(dataSet1.getDataSetCode(), dataSet2.getDataSetCode(), dataSet3
                                    .getDataSetCode())));
                    will(returnValue(Arrays.asList(entry1, entry2)));
                    
                    one(dao).commit();

                    Map<String, Long> sizeMap = new HashedMap<String, Long>();
                    sizeMap.put(dataSet1.getDataSetCode(), entry1.getSizeInBytes());

                    one(service).updatePhysicalDataSetsSize(sizeMap);

                    one(service).listPhysicalDataSetsWithUnknownSize(CHUNK_SIZE_DEFAULT, dataSet3.getDataSetCode());
                    will(returnValue(Collections.emptyList()));
                }
            });

        execute(null, null, null, null);

        assertLogThatSomeDataSetsHaveNotBeenFixedYet();
    }

    @Test
    public void testExecuteWithMultipleChunks()
    {
        final int chunkSize = 2;

        context.checking(new Expectations()
            {
                {
                    allowing(timeProvider).getTimeInMilliseconds();
                    will(returnValue(0L));

                    one(service).listPhysicalDataSetsWithUnknownSize(chunkSize, null);
                    will(returnValue(Arrays.asList(dataSet1, dataSet2)));

                    one(dao).listDataSetsSize(
                            with(containsExactly(dataSet1.getDataSetCode(), dataSet2.getDataSetCode())));
                    will(returnValue(Arrays.asList(entry1, entry2)));
                    
                    one(dao).commit();

                    Map<String, Long> sizeMap = new HashedMap<String, Long>();
                    sizeMap.put(dataSet1.getDataSetCode(), entry1.getSizeInBytes());

                    one(service).updatePhysicalDataSetsSize(sizeMap);

                    one(service).listPhysicalDataSetsWithUnknownSize(chunkSize, dataSet2.getDataSetCode());
                    will(returnValue(Arrays.asList(dataSet3)));

                    one(dao).listDataSetsSize(new String[] { dataSet3.getDataSetCode() });
                    will(returnValue(Collections.emptyList()));
                    
                    one(dao).commit();

                    one(service).listPhysicalDataSetsWithUnknownSize(chunkSize, dataSet3.getDataSetCode());
                    will(returnValue(Collections.emptyList()));
                }
            });

        execute(null, chunkSize, null, null);

        assertLogThatSomeDataSetsHaveNotBeenFixedYet();
    }

    @Test
    public void testExecuteWithSomeChunkFailing()
    {
        final File lastSeenFile = new File(getStoreRoot(), "lastSeenWithSomeChunkFailing");
        final int chunkSize = 1;

        context.checking(new Expectations()
            {
                {
                    allowing(timeProvider).getTimeInMilliseconds();
                    will(returnValue(0L));

                    one(service).listPhysicalDataSetsWithUnknownSize(chunkSize, null);
                    will(returnValue(Arrays.asList(dataSet1)));

                    one(dao).listDataSetsSize(new String[] { dataSet1.getDataSetCode() });
                    will(returnValue(Arrays.asList(entry1)));
                    
                    one(dao).commit();

                    Map<String, Long> sizeMap = new HashedMap<String, Long>();
                    sizeMap.put(dataSet1.getDataSetCode(), entry1.getSizeInBytes());

                    one(service).updatePhysicalDataSetsSize(sizeMap);

                    one(service).listPhysicalDataSetsWithUnknownSize(chunkSize, dataSet1.getDataSetCode());
                    will(returnValue(Arrays.asList(dataSet4)));

                    one(dao).listDataSetsSize(new String[] { dataSet4.getDataSetCode() });
                    will(throwException(new RuntimeException("Test exception")));
                }
            });

        LastSeenDataSetFileContent lastSeenContent = LastSeenDataSetFileContent.readFromFile(lastSeenFile);
        Assert.assertNull(lastSeenContent);

        try
        {
            execute(null, chunkSize, lastSeenFile, null);
            Assert.fail();
        } catch (Exception e)
        {
            Assert.assertEquals(e.getClass(), RuntimeException.class);
            Assert.assertEquals(e.getMessage(), "Test exception");
        }

        lastSeenContent = LastSeenDataSetFileContent.readFromFile(lastSeenFile);
        Assert.assertEquals(lastSeenContent.getLastSeenDataSetCode(), dataSet1.getDataSetCode());
    }

    @Test
    public void testExecuteWithTimeLimit()
    {
        final long timeLimit = 10L;
        final int chunkSize = 1;

        context.checking(new Expectations()
            {
                {
                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(0L));

                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(0L));

                    one(service).listPhysicalDataSetsWithUnknownSize(chunkSize, null);
                    will(returnValue(Arrays.asList(dataSet1)));

                    one(dao).listDataSetsSize(new String[] { dataSet1.getDataSetCode() });
                    will(returnValue(Arrays.asList(entry1)));
                    
                    one(dao).commit();

                    Map<String, Long> sizeMap = new HashedMap<String, Long>();
                    sizeMap.put(dataSet1.getDataSetCode(), entry1.getSizeInBytes());

                    one(service).updatePhysicalDataSetsSize(sizeMap);

                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(8L));

                    one(service).listPhysicalDataSetsWithUnknownSize(chunkSize, dataSet1.getDataSetCode());
                    will(returnValue(Arrays.asList(dataSet2)));

                    one(dao).listDataSetsSize(new String[] { dataSet2.getDataSetCode() });
                    will(returnValue(Collections.emptyList()));
                    
                    one(dao).commit();

                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(12L));
                }
            });

        execute(timeLimit, chunkSize, null, null);

        assertLogThatSomeDataSetsHaveNotBeenFixedYet();
    }

    @Test
    public void testExecuteWhenAllDataSetsGetFixedButTimeLimitIsReached()
    {
        final long timeLimit = 10L;
        final int chunkSize = 1;

        context.checking(new Expectations()
            {
                {
                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(0L));

                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(0L));

                    one(service).listPhysicalDataSetsWithUnknownSize(chunkSize, null);
                    will(returnValue(Arrays.asList(dataSet1)));

                    one(dao).listDataSetsSize(new String[] { dataSet1.getDataSetCode() });
                    will(returnValue(Arrays.asList(entry1)));
                    
                    one(dao).commit();

                    Map<String, Long> sizeMap = new HashedMap<String, Long>();
                    sizeMap.put(dataSet1.getDataSetCode(), entry1.getSizeInBytes());

                    one(service).updatePhysicalDataSetsSize(sizeMap);

                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(12L));
                }
            });

        execute(timeLimit, chunkSize, null, null);

        assertLogThatSomeDataSetsHaveNotBeenFixedYet();
    }

    @Test
    public void testExecuteWithUpToDateLastSeenDataSetFile()
    {
        final long lastSeenCreationTime = System.currentTimeMillis();
        final long deleteLastSeenFileInterval = 100L;
        final File lastSeenFile = new File(getStoreRoot(), "upToDateLastSeenFile");

        LastSeenDataSetFileContent lastSeenContent = new LastSeenDataSetFileContent();
        lastSeenContent.setFileCreationTime(lastSeenCreationTime);
        lastSeenContent.setLastSeenDataSetCode(dataSet1.getDataSetCode());
        lastSeenContent.writeToFile(lastSeenFile);

        context.checking(new Expectations()
            {
                {
                    allowing(timeProvider).getTimeInMilliseconds();
                    will(returnValue(lastSeenCreationTime + deleteLastSeenFileInterval / 2));

                    one(service).listPhysicalDataSetsWithUnknownSize(CHUNK_SIZE_DEFAULT, dataSet1.getDataSetCode());
                    will(returnValue(Arrays.asList(dataSet2, dataSet3)));

                    one(dao).listDataSetsSize(with(containsExactly(dataSet2.getDataSetCode(), dataSet3.getDataSetCode())));
                    will(returnValue(Arrays.asList(entry2)));
                    
                    one(dao).commit();

                    one(service).listPhysicalDataSetsWithUnknownSize(CHUNK_SIZE_DEFAULT, dataSet3.getDataSetCode());
                    will(returnValue(Collections.emptyList()));
                }
            });

        execute(null, null, lastSeenFile, deleteLastSeenFileInterval);

        lastSeenContent = LastSeenDataSetFileContent.readFromFile(lastSeenFile);
        Assert.assertEquals(lastSeenContent.getFileCreationTime(), Long.valueOf(lastSeenCreationTime));
        Assert.assertEquals(lastSeenContent.getLastSeenDataSetCode(), dataSet3.getDataSetCode());

        assertLogThatSomeDataSetsHaveNotBeenFixedYet();
    }

    @Test
    public void testExecuteWithOutOfDateLastSeenDataSetFile()
    {
        final long lastSeenCreationTime = System.currentTimeMillis();
        final long deleteLastSeenFileInterval = 100L;
        final File lastSeenFile = new File(getStoreRoot(), "outOfDateLastSeenFile");

        LastSeenDataSetFileContent lastSeenContent = new LastSeenDataSetFileContent();
        lastSeenContent.setFileCreationTime(lastSeenCreationTime);
        lastSeenContent.setLastSeenDataSetCode(dataSet1.getDataSetCode());
        lastSeenContent.writeToFile(lastSeenFile);

        context.checking(new Expectations()
            {
                {
                    allowing(timeProvider).getTimeInMilliseconds();
                    will(returnValue(lastSeenCreationTime + 2 * deleteLastSeenFileInterval));

                    one(service).listPhysicalDataSetsWithUnknownSize(CHUNK_SIZE_DEFAULT, null);
                    will(returnValue(Arrays.asList(dataSet1, dataSet2, dataSet3)));

                    one(dao).listDataSetsSize(with(containsExactly(dataSet1.getDataSetCode(), dataSet2.getDataSetCode(), dataSet3.getDataSetCode())));
                    will(returnValue(Arrays.asList(entry1, entry2)));
                    
                    one(dao).commit();

                    Map<String, Long> sizeMap = new HashedMap<String, Long>();
                    sizeMap.put(dataSet1.getDataSetCode(), entry1.getSizeInBytes());

                    one(service).updatePhysicalDataSetsSize(sizeMap);

                    one(service).listPhysicalDataSetsWithUnknownSize(CHUNK_SIZE_DEFAULT, dataSet3.getDataSetCode());
                    will(returnValue(Collections.emptyList()));
                }
            });

        execute(null, null, lastSeenFile, deleteLastSeenFileInterval);

        lastSeenContent = LastSeenDataSetFileContent.readFromFile(lastSeenFile);
        Assert.assertEquals(lastSeenContent.getFileCreationTime(), Long.valueOf(lastSeenCreationTime + 2 * deleteLastSeenFileInterval));
        Assert.assertEquals(lastSeenContent.getLastSeenDataSetCode(), dataSet3.getDataSetCode());

        assertLogThatSomeDataSetsHaveNotBeenFixedYet();
    }

    @Test(expectedExceptions = { IllegalArgumentException.class }, expectedExceptionsMessageRegExp = "Could not read the last seen data set file")
    public void testExecuteWithIncorrectLastSeenDataSetFile()
    {
        TestResources resources = new TestResources(getClass());

        final File lastSeenFile = resources.getResourceFile("incorrectLastSeenFile");

        context.checking(new Expectations()
            {
                {
                    allowing(timeProvider).getTimeInMilliseconds();
                    will(returnValue(0L));
                }
            });

        execute(null, null, lastSeenFile, null);
    }

    @Test
    public void testExecuteWhenAllDataSetsGetFixedInOneChunk()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(timeProvider).getTimeInMilliseconds();
                    will(returnValue(0L));

                    one(service).listPhysicalDataSetsWithUnknownSize(CHUNK_SIZE_DEFAULT, null);
                    will(returnValue(Arrays.asList(dataSet1)));

                    one(dao).listDataSetsSize(new String[] { dataSet1.getDataSetCode() });
                    will(returnValue(Arrays.asList(entry1)));
                    
                    one(dao).commit();

                    Map<String, Long> sizeMap = new HashedMap<String, Long>();
                    sizeMap.put(dataSet1.getDataSetCode(), entry1.getSizeInBytes());

                    one(service).updatePhysicalDataSetsSize(sizeMap);

                    one(service).listPhysicalDataSetsWithUnknownSize(CHUNK_SIZE_DEFAULT, dataSet1.getDataSetCode());
                    will(returnValue(Collections.emptyList()));
                }
            });

        execute(null, null, null, null);

        assertLogThatAllDataSetsHaveBeenFixed();
    }

    @Test
    public void testExecuteWhenAllDataSetsGetFixedInMultipleChunks()
    {
        final int chunkSize = 1;

        context.checking(new Expectations()
            {
                {
                    allowing(timeProvider).getTimeInMilliseconds();
                    will(returnValue(0L));

                    one(service).listPhysicalDataSetsWithUnknownSize(chunkSize, null);
                    will(returnValue(Arrays.asList(dataSet1)));

                    one(dao).listDataSetsSize(new String[] { dataSet1.getDataSetCode() });
                    will(returnValue(Arrays.asList(entry1)));
                    
                    one(dao).commit();

                    Map<String, Long> sizeMap = new HashedMap<String, Long>();
                    sizeMap.put(dataSet1.getDataSetCode(), entry1.getSizeInBytes());

                    one(service).updatePhysicalDataSetsSize(sizeMap);

                    one(service).listPhysicalDataSetsWithUnknownSize(chunkSize, dataSet1.getDataSetCode());
                    will(returnValue(Arrays.asList(dataSet4)));

                    one(dao).listDataSetsSize(new String[] { dataSet4.getDataSetCode() });
                    will(returnValue(Arrays.asList(entry4)));
                    
                    one(dao).commit();

                    sizeMap = new HashedMap<String, Long>();
                    sizeMap.put(dataSet4.getDataSetCode(), entry4.getSizeInBytes());

                    one(service).updatePhysicalDataSetsSize(sizeMap);

                    one(service).listPhysicalDataSetsWithUnknownSize(chunkSize, dataSet4.getDataSetCode());
                    will(returnValue(Collections.emptyList()));
                }
            });

        execute(null, chunkSize, null, null);

        assertLogThatAllDataSetsHaveBeenFixed();
    }

    @Test
    public void testExecuteWhenAllDataSetsGetFixedStartingFromTheLastSeen()
    {
        final long lastSeenCreationTime = System.currentTimeMillis();
        final File lastSeenFile = new File(getStoreRoot(), "correctLastSeenFile");

        LastSeenDataSetFileContent lastSeenContent = new LastSeenDataSetFileContent();
        lastSeenContent.setFileCreationTime(lastSeenCreationTime);
        lastSeenContent.setLastSeenDataSetCode(dataSet1.getDataSetCode());
        lastSeenContent.writeToFile(lastSeenFile);

        context.checking(new Expectations()
            {
                {
                    allowing(timeProvider).getTimeInMilliseconds();
                    will(returnValue(0L));

                    one(service).listPhysicalDataSetsWithUnknownSize(CHUNK_SIZE_DEFAULT, dataSet1.getDataSetCode());
                    will(returnValue(Arrays.asList(dataSet4)));

                    one(dao).listDataSetsSize(new String[] { dataSet4.getDataSetCode() });
                    will(returnValue(Arrays.asList(entry4)));
                    
                    one(dao).commit();

                    Map<String, Long> sizeMap = new HashedMap<String, Long>();
                    sizeMap.put(dataSet4.getDataSetCode(), entry4.getSizeInBytes());

                    one(service).updatePhysicalDataSetsSize(sizeMap);

                    one(service).listPhysicalDataSetsWithUnknownSize(CHUNK_SIZE_DEFAULT, dataSet4.getDataSetCode());
                    will(returnValue(Collections.emptyList()));
                }
            });

        execute(null, null, lastSeenFile, null);

        assertLogThatSomeDataSetsHaveNotBeenFixedYet();
    }

    private void execute(Long timeLimit, Integer chunkSize, File lastSeenFile, Long deleteLastSeenFileInterval)
    {
        FillUnknownDataSetSizeInOpenbisDBFromPathInfoDBMaintenanceTask task =
                new FillUnknownDataSetSizeInOpenbisDBFromPathInfoDBMaintenanceTask(service, dao, timeProvider, configProvider);

        Properties properties = new Properties();

        if (timeLimit != null)
        {
            properties.setProperty(TIME_LIMIT_PROPERTY, timeLimit.toString() + " ms");
        }
        if (chunkSize != null)
        {
            properties.setProperty(CHUNK_SIZE_PROPERTY, chunkSize.toString());
        }
        if (lastSeenFile != null)
        {
            properties.setProperty(LAST_SEEN_DATA_SET_FILE_PROPERTY, lastSeenFile.getAbsolutePath());
        }
        if (deleteLastSeenFileInterval != null)
        {
            properties.setProperty(DELETE_LAST_SEEN_DATA_SET_FILE_INTERVAL_PROPERTY, deleteLastSeenFileInterval.toString() + " ms");
        }

        task.setUp("fill-unknown-sizes", properties);
        task.execute();
    }

    private File getStoreRoot()
    {
        return new File(System.getProperty("java.io.tmpdir") + File.separator + getClass().getName() + "Store");
    }

    private void assertLogThatAllDataSetsHaveBeenFixed()
    {
        AssertionUtil.assertContainsLines("INFO  OPERATION.FillUnknownDataSetSizeInOpenbisDBFromPathInfoDBMaintenanceTask - "
                + "All data sets with unknown size in openbis database have been fixed. The maintenance task can be now disabled.",
                logRecorder.getLogContent());
    }

    private void assertLogThatSomeDataSetsHaveNotBeenFixedYet()
    {
        AssertionUtil.assertContainsLines("INFO  OPERATION.FillUnknownDataSetSizeInOpenbisDBFromPathInfoDBMaintenanceTask - "
                + "Some data sets with unknown size in openbis database have not been fixed yet. Do not disable the maintenance task yet.",
                logRecorder.getLogContent());
    }

}
