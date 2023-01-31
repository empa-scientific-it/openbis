/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver;

import java.io.IOException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.common.utilities.MockTimeProvider;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class DssRegistrationLogDirectoryHelperTest extends AbstractFileSystemTestCase
{
    private DssRegistrationLogDirectoryHelper dssRegistrationLogDirHelper;

    private BufferedAppender logAppender;

    @BeforeMethod
    @Override
    public void setUp() throws IOException
    {
        super.setUp();
        logAppender = LogRecordingUtils.createRecorder();
        MockTimeProvider timeProvider = new MockTimeProvider();
        dssRegistrationLogDirHelper =
                new DssRegistrationLogDirectoryHelper(workingDirectory, timeProvider);
    }

    @Test
    public void testLogFilenameGeneration()
    {
        String logFilename =
                dssRegistrationLogDirHelper.generateLogFileName("filename", "threadname");
        assertEquals("1970-01-01_01-00-00-000_threadname_filename.log", logFilename);
    }

    @Test
    public void testFileCreationAndMove()
    {
        DssRegistrationLogger logFile = createLogFile();
        assertTrue(logFile.getFile().exists());
        assertEquals("in-process", logFile.getFile().getParentFile().getName());

        logFile.registerSuccess();
        assertTrue(logFile.getFile().exists());
        assertEquals("succeeded", logFile.getFile().getParentFile().getName());

        logFile.log("hello");
        logFile.registerFailure();
        assertTrue(logFile.getFile().exists());
        assertEquals("failed", logFile.getFile().getParentFile().getName());
        AssertionUtil.assertContainsLines("Data set registration failed. See log for details : "
                + logFile.getFile().getAbsolutePath(), logAppender.getLogContent());

        // Check that duplicating a registerFailure does not cause problems
        logFile.registerFailure();
        assertTrue(logFile.getFile().exists());
        assertEquals("failed", logFile.getFile().getParentFile().getName());
    }

    @Test
    public void testLogging()
    {
        DssRegistrationLogger logFile = createLogFile();
        logFile.log("1: The message");
        logFile.log("2: The message");
        logFile.registerSuccess();
        logFile.log("3: Succeeded");
        assertEquals("1970-01-01 01:00:01 1: The message\n"
                + "1970-01-01 01:00:02 2: The message\n" + "1970-01-01 01:00:03 3: Succeeded\n",
                FileUtilities.loadToString(logFile.getFile()));
    }

    @Test
    public void testLoggingWithTruncating()
    {
        DssRegistrationLogger logFile = createLogFile();
        logFile.logTruncatingIfNecessary("This is a very long string, in fact, "
                + "a string that is longer than the limit for the length of an allowed string");
        String contents = FileUtilities.loadToString(logFile.getFile());
        assertEquals("1970-01-01 01:00:01 This is a very long string, in fact, "
                + "a string that is longer than the limit for the length of an...\n", contents);
    }

    private DssRegistrationLogger createLogFile()
    {
        dssRegistrationLogDirHelper.initializeSubdirectories();
        return dssRegistrationLogDirHelper.createNewLogFile("filename", "threadname",
                FileOperations.getInstance());
    }

}
