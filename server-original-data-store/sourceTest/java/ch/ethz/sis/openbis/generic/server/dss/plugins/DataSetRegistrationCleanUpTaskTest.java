/*
 * Copyright 2020 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.common.utilities.MockTimeProvider;
import ch.systemsx.cisd.etlserver.DssUniqueFilenameGenerator;
import ch.systemsx.cisd.etlserver.ThreadParameters;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.IngestionService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;

/**
 * @author Franz-Josef Elmer
 */
public class DataSetRegistrationCleanUpTaskTest extends AbstractFileSystemTestCase
{
    private static final String DATA_SET_TIMESTAMP_TEMPLATE = "yyyyMMddHHmmssSSS";

    private ITimeProvider timeProvider;

    private File storeRoot;

    private BufferedAppender logRecorder;

    private ThreadParameters[] threadParameters;

    private Properties thread1Properties;

    private Properties thread2Properties;

    private Mockery context;

    private IEncapsulatedOpenBISService service;

    private DataSetRegistrationCleanUpTask task;

    private DatabaseInstance databaseInstance;

    public DataSetRegistrationCleanUpTaskTest()
    {
        super(false);
    }

    @BeforeMethod
    public void setUp() throws IOException
    {
        super.setUp();
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        LogInitializer.init();
        logRecorder = new BufferedAppender(Level.INFO);
        databaseInstance = new DatabaseInstance();
        databaseInstance.setUuid(UUID.randomUUID().toString().toUpperCase());
        storeRoot = new File(workingDirectory, "store");
        timeProvider = new MockTimeProvider(0, 34 * DateUtils.MILLIS_PER_DAY + 13 * DateUtils.MILLIS_PER_HOUR
                + 21 * DateUtils.MILLIS_PER_MINUTE + 1597);
        thread1Properties = new Properties();
        File incoming1 = new File(storeRoot, "1/in");
        incoming1.mkdirs();
        new File(incoming1.getParent(), databaseInstance.getUuid()).mkdirs();
        thread1Properties.setProperty(ThreadParameters.INCOMING_DIR, incoming1.getAbsolutePath());
        thread2Properties = new Properties();
        File incoming2 = new File(storeRoot, "2/in");
        incoming2.mkdirs();
        new File(incoming2.getParent(), databaseInstance.getUuid()).mkdirs();
        new File(storeRoot, "9").mkdirs();
        thread2Properties.setProperty(ThreadParameters.INCOMING_DIR, incoming2.getAbsolutePath());
        threadParameters = new ThreadParameters[] {
                new ThreadParameters(thread1Properties, "t1"),
                new ThreadParameters(thread2Properties, "t2") };
        task = new DataSetRegistrationCleanUpTask(threadParameters, storeRoot, timeProvider, service);
        prepareGetHomeDatabaseInstance();
    }

    private File createShareWithPreStagingStuff(ITimeProvider timeProvider, int shareId, int numberOfFiles)
    {
        File share = new File(storeRoot, Integer.toString(shareId));
        File preStagingDir = new File(share, TopLevelDataSetRegistratorGlobalState.DEFAULT_PRE_STAGING_DIR);
        preStagingDir.mkdirs();
        DssUniqueFilenameGenerator filenameGenerator = new DssUniqueFilenameGenerator(timeProvider, "test", "test", null);
        for (int i = 0; i < numberOfFiles; i++)
        {
            String filename = filenameGenerator.generateFilename();
            File file = new File(preStagingDir, filename);
            FileUtilities.writeToFile(file, filename);
            file.setLastModified(50000);
        }
        return share;
    }

    @Test
    public void testPreStaging()
    {
        // Given
        File share1 = createShareWithPreStagingStuff(timeProvider, 1, 2);
        File share2 = createShareWithPreStagingStuff(timeProvider, 2, 1);
        Properties properties = new Properties();
        properties.setProperty(DataSetRegistrationCleanUpTask.MINIMUM_AGE_IN_DAYS, "70");
        task.setUp("cleanup", properties);

        // When
        task.execute();

        // Then
        assertEquals("pre-staging directory " + storeRoot.getAbsolutePath()
                + "/1/pre-staging has 1 files which are older than 70 days:\n"
                + "1970-01-01_01-00-00-000_test_test (last modified: 1970-01-01 01:00:50)\n"
                + "Deleting directory '" + storeRoot + "/1/pre-staging/1970-01-01_01-00-00-000_test_test'\n"
                + "Stale folder deleted: " + storeRoot.getAbsolutePath()
                + "/1/pre-staging/1970-01-01_01-00-00-000_test_test", logRecorder.getLogContent());
        assertEquals("[1970-02-04_14-21-01-597_test_test]",
                getStuffFromDir(share1, TopLevelDataSetRegistratorGlobalState.DEFAULT_PRE_STAGING_DIR).toString());
        assertEquals("[1970-03-11_03-42-03-194_test_test]",
                getStuffFromDir(share2, TopLevelDataSetRegistratorGlobalState.DEFAULT_PRE_STAGING_DIR).toString());
    }

    private List<String> getStuffFromDir(File share, String dir)
    {
        List<String> result = new ArrayList<>(Arrays.asList(
                new File(share, dir).list()));
        Collections.sort(result);
        return result;
    }

    @Test
    public void testStaging()
    {
        // Given
        String defaultStagingDir = TopLevelDataSetRegistratorGlobalState.DEFAULT_STAGING_DIR;
        File share1 = createShareWithStuffInDir(defaultStagingDir, timeProvider, 1, 2);
        File share2 = createShareWithStuffInDir(defaultStagingDir, timeProvider, 2, 1);
        Properties properties = new Properties();
        properties.setProperty(DataSetRegistrationCleanUpTask.MINIMUM_AGE_IN_DAYS, "70");
        task.setUp("cleanup", properties);

        // When
        task.execute();

        // Then
        assertEquals("staging directory " + storeRoot.getAbsolutePath()
                + "/1/staging has 1 files which are older than 70 days:\n"
                + "19700101010000000-0 (last modified: 1970-01-01 01:00:00)\n"
                + "Deleting directory '" + storeRoot + "/1/staging/19700101010000000-0'\n"
                + "Stale folder deleted: " + storeRoot.getAbsolutePath()
                + "/1/staging/19700101010000000-0", logRecorder.getLogContent());
        assertEquals("[19700204142101597-1]", getStuffFromDir(share1, defaultStagingDir).toString());
        assertEquals("[19700311034203194-0]", getStuffFromDir(share2, defaultStagingDir).toString());
    }

    private File createShareWithStuffInDir(String dir, ITimeProvider timeProvider,
            int shareId, int numberOfFiles)
    {
        File share = new File(storeRoot, Integer.toString(shareId));
        File stagingDir = new File(share, dir);
        stagingDir.mkdirs();
        for (int i = 0; i < numberOfFiles; i++)
        {
            long time = timeProvider.getTimeInMilliseconds();
            String filename = DateFormatUtils.format(time, DATA_SET_TIMESTAMP_TEMPLATE) + "-" + i;
            File file = new File(stagingDir, filename);
            FileUtilities.writeToFile(file, filename);
            file.setLastModified(time);
        }
        return share;
    }

    @Test
    public void testPreCommit()
    {
        // Given
        String defaultPreCommitDir = TopLevelDataSetRegistratorGlobalState.DEFAULT_PRE_COMMIT_DIR;
        File share1 = createShareWithStuffInDir(defaultPreCommitDir, timeProvider, 1, 2);
        File share2 = createShareWithStuffInDir(defaultPreCommitDir, timeProvider, 2, 1);
        Properties properties = new Properties();
        properties.setProperty(DataSetRegistrationCleanUpTask.MINIMUM_AGE_IN_DAYS, "70");
        task.setUp("cleanup", properties);

        // When
        task.execute();

        // Then
        assertEquals("pre-commit directory " + storeRoot.getAbsolutePath()
                + "/1/pre-commit has 1 files which are older than 70 days:\n"
                + "19700101010000000-0 (last modified: 1970-01-01 01:00:00)\n"
                + "Deleting directory '" + storeRoot + "/1/pre-commit/19700101010000000-0'\n"
                + "Stale folder deleted: " + storeRoot.getAbsolutePath()
                + "/1/pre-commit/19700101010000000-0", logRecorder.getLogContent());
        assertEquals("[19700204142101597-1]", getStuffFromDir(share1, defaultPreCommitDir).toString());
        assertEquals("[19700311034203194-0]", getStuffFromDir(share2, defaultPreCommitDir).toString());
    }

    @Test
    public void testElnTmp()
    {
        // Given
        String elnTmpDir = IngestionService.AGGREGATION_SERVICE_SCRATCH_DIR_NAME + "/"
                + IngestionService.INCOMING_DIR + "/"
                + DataSetRegistrationCleanUpTask.ELN_TEMP_DIR_FOR_REGISTRATION;
        File share1 = createShareWithStuffInDir(elnTmpDir, timeProvider, 1, 2);
        File share2 = createShareWithStuffInDir(elnTmpDir, timeProvider, 2, 1);
        Properties properties = new Properties();
        properties.setProperty(DataSetRegistrationCleanUpTask.MINIMUM_AGE_IN_DAYS, "70");
        task.setUp("cleanup", properties);

        // When
        task.execute();

        // Then
        assertEquals("ELN temporary registration directory " + storeRoot.getAbsolutePath()
                + "/1/" + elnTmpDir + " has 1 files which are older than 70 days:\n"
                + "19700101010000000-0 (last modified: 1970-01-01 01:00:00)\n"
                + "Deleting directory '" + storeRoot + "/1/" + elnTmpDir + "/19700101010000000-0'\n"
                + "Stale folder deleted: " + storeRoot.getAbsolutePath()
                + "/1/" + elnTmpDir + "/19700101010000000-0", logRecorder.getLogContent());
        assertEquals("[19700204142101597-1]", getStuffFromDir(share1, elnTmpDir).toString());
        assertEquals("[19700311034203194-0]", getStuffFromDir(share2, elnTmpDir).toString());
    }

    @Test
    public void testCleanUpStore() throws IOException
    {
        // Given
        File nas = new File(workingDirectory, "nas");
        nas.mkdirs();
        File share = new File(storeRoot, "3");
        Files.createSymbolicLink(share.toPath(), nas.toPath().toAbsolutePath());
        File instance = new File(share, databaseInstance.getUuid());
        long time = timeProvider.getTimeInMilliseconds();
        File dir1 = createFolder(instance, "12", time);
        File dir2 = createFolder(instance, "13", time + 20 * DateUtils.MILLIS_PER_DAY);
        File dir3 = createFolder(instance, "ca/42", time);
        File dir4 = createFolder(instance, "ab/cd/ef", time);
        File dir5 = createFolder(instance, "ab/cd/78/ds-11", time);
        File dir6 = createFolder(instance, "ab/cd/78/ds-12", time + 20 * DateUtils.MILLIS_PER_DAY);
        File dir7 = createFolder(instance, "ab/cd/78/ds-13", time);
        createFile(instance, "ab/66/ab/ds-1/original/hello.txt", "hello");
        createFile(instance, "ab/66/ab/ds-2/original/hi.txt", "hi");
        createFolder(instance, "ab/66/ab/ds-1/original", time);
        prepareListDataSetsByCode(Arrays.asList("ds-11", "ds-13"), createDataSets("ds-13"));
        task.setUp("cleanup", new Properties());

        // When
        task.execute();

        // Then
        assertEquals("2 empty data set folders found in share 3 which are older than 30 days: ["
                + dir5 + ", " + dir7 + "]\n"
                + "Deleting directory '" + dir5 + "'\n"
                + "Stale data set folder deleted: " + dir5.getAbsolutePath()
                + " (last modified: 1970-01-01T00:00:00Z)\n"
                + "3 empty sharding folders found in share 3 which are older than 30 days: ["
                + dir1 + ", " + dir4 + ", " + dir3 + "]\n"
                + "Deleting directory '" + dir1 + "'\n"
                + "Empty sharding folder deleted: " + dir1.getAbsolutePath()
                + " (last modified: 1970-01-01T00:00:00Z)\n"
                + "Deleting directory '" + dir4 + "'\n"
                + "Empty sharding folder deleted: " + dir4.getAbsolutePath()
                + " (last modified: 1970-01-01T00:00:00Z)\n"
                + "Deleting directory '" + dir3 + "'\n"
                + "Empty sharding folder deleted: " + dir3.getAbsolutePath()
                + " (last modified: 1970-01-01T00:00:00Z)",
                logRecorder.getLogContent());
        assertEquals(false, dir1.exists());
        assertEquals(true, dir2.exists());
        assertEquals(false, dir3.exists());
        assertEquals(false, dir4.exists());
        assertEquals(false, dir5.exists());
        assertEquals(true, dir6.exists());
        assertEquals(true, dir7.exists());
    }

    private File createFolder(File instance, String relPath, long lastModified)
    {
        File folder = new File(instance, relPath);
        folder.mkdirs();
        folder.setLastModified(lastModified);
        return folder;
    }

    private void createFile(File nas, String relativePath, String content)
    {
        File file = new File(nas, relativePath);
        file.getParentFile().mkdirs();
        FileUtilities.writeToFile(file, content);
    }

    private List<AbstractExternalData> createDataSets(String... dataSetCodes)
    {
        List<AbstractExternalData> dataSets = new ArrayList<>();
        for (String dataSetCode : dataSetCodes)
        {
            AbstractExternalData dataSet = new PhysicalDataSet();
            dataSet.setCode(dataSetCode);
            dataSets.add(dataSet);
        }
        return dataSets;
    }

    private void prepareGetHomeDatabaseInstance()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(service).getHomeDatabaseInstance();
                    will(returnValue(databaseInstance));
                }
            });
    }

    private void prepareListDataSetsByCode(List<String> dataSetCodes, List<AbstractExternalData> dataSets)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(service).listDataSetsByCode(dataSetCodes);
                    will(returnValue(dataSets));
                }
            });
    }
}
