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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Level;
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

    public DataSetRegistrationCleanUpTaskTest()
    {
        super(false);
    }

    @BeforeMethod
    public void setUp() throws IOException
    {
        super.setUp();
        LogInitializer.init();
        logRecorder = new BufferedAppender(Level.INFO);
        storeRoot = new File(workingDirectory, "store");
        timeProvider = new MockTimeProvider(0, 34 * DateUtils.MILLIS_PER_DAY + 13 * DateUtils.MILLIS_PER_HOUR
                + 21 * DateUtils.MILLIS_PER_MINUTE + 1597);
        thread1Properties = new Properties();
        File incoming1 = new File(storeRoot, "1/in");
        incoming1.mkdirs();
        thread1Properties.setProperty(ThreadParameters.INCOMING_DIR, incoming1.getAbsolutePath());
        thread2Properties = new Properties();
        File incoming2 = new File(storeRoot, "2/in");
        incoming2.mkdirs();
        thread2Properties.setProperty(ThreadParameters.INCOMING_DIR, incoming2.getAbsolutePath());
        threadParameters = new ThreadParameters[] {
                new ThreadParameters(thread1Properties, "t1"),
                new ThreadParameters(thread2Properties, "t2") };
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
        DataSetRegistrationCleanUpTask task = new DataSetRegistrationCleanUpTask(threadParameters,
                storeRoot, timeProvider);
        Properties properties = new Properties();
        properties.setProperty(DataSetRegistrationCleanUpTask.MINIMUM_AGE_IN_DAYS, "70");
        task.setUp("cleanup", properties);

        // When
        task.execute();

        // Then
        assertEquals("pre-staging directory " + storeRoot.getAbsolutePath() + "/1/pre-staging has 1 files\n"
                + "1970-01-01_01-00-00-000_test_test (last modified: 1970-01-01 01:00:50)\n"
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
        DataSetRegistrationCleanUpTask task = new DataSetRegistrationCleanUpTask(threadParameters,
                storeRoot, timeProvider);
        Properties properties = new Properties();
        properties.setProperty(DataSetRegistrationCleanUpTask.MINIMUM_AGE_IN_DAYS, "70");
        task.setUp("cleanup", properties);

        // When
        task.execute();

        // Then
        assertEquals("staging directory " + storeRoot.getAbsolutePath() + "/1/staging has 1 files\n"
                + "19700101010000000-0 (last modified: 1970-01-01 01:00:00)\n"
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
        DataSetRegistrationCleanUpTask task = new DataSetRegistrationCleanUpTask(threadParameters,
                storeRoot, timeProvider);
        Properties properties = new Properties();
        properties.setProperty(DataSetRegistrationCleanUpTask.MINIMUM_AGE_IN_DAYS, "70");
        task.setUp("cleanup", properties);

        // When
        task.execute();

        // Then
        assertEquals("pre-commit directory " + storeRoot.getAbsolutePath() + "/1/pre-commit has 1 files\n"
                + "19700101010000000-0 (last modified: 1970-01-01 01:00:00)\n"
                + "Stale folder deleted: " + storeRoot.getAbsolutePath()
                + "/1/pre-commit/19700101010000000-0", logRecorder.getLogContent());
        assertEquals("[19700204142101597-1]", getStuffFromDir(share1, defaultPreCommitDir).toString());
        assertEquals("[19700311034203194-0]", getStuffFromDir(share2, defaultPreCommitDir).toString());
    }

}
