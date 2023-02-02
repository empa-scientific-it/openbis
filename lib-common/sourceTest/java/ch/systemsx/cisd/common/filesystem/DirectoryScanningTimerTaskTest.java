/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.filesystem;

import static ch.systemsx.cisd.common.filesystem.FileUtilities.ACCEPT_ALL_FILTER;
import static ch.systemsx.cisd.common.filesystem.FileUtilities.ACCEPT_ALL_BUT_HIDDEN_FILTER;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.io.CollectionIO;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.test.LogMonitoringAppender;

/**
 * Test cases for the {@link DirectoryScanningTimerTask}.
 * 
 * @author Bernd Rinn
 */
@Test(timeOut = 2000)
public class DirectoryScanningTimerTaskTest
{

    private static final File unitTestRootDirectory =
            new File("targets" + File.separator + "unit-test-wd");

    private static final File workingDirectory =
            new File(unitTestRootDirectory, "DirectoryScanningTimerTaskTest");

    private final static FileFilter ALWAYS_FALSE_FILE_FILTER = new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                return false;
            }
        };

    private final static String EXCEPTION_THROWING_FILE_FILTER_MESSAGE =
            "Exception throwing file filter does its job.";

    private final static FileFilter EXCEPTION_THROWING_FILE_FILTER = new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                throw new RuntimeException(EXCEPTION_THROWING_FILE_FILTER_MESSAGE);
            }
        };

    private final MockPathHandler mockPathHandler = new MockPathHandler();

    /**
     * A mock implementation that stores the handled paths.
     */
    private static class MockPathHandler implements IPathHandler
    {

        private final List<File> handledPaths = new ArrayList<File>();

        public void clear()
        {
            handledPaths.clear();
        }

        //
        // IPathHandler
        //

        @Override
        public void handle(final File path)
        {
            handledPaths.add(path);
            path.delete();
        }

        @Override
        public boolean isStopped()
        {
            return false;
        }
    }

    @BeforeClass
    public void init()
    {
        LogInitializer.init();
        unitTestRootDirectory.mkdirs();
        assert unitTestRootDirectory.isDirectory();
    }

    @BeforeMethod
    public void setUp()
    {
        FileUtilities.deleteRecursively(workingDirectory);
        workingDirectory.mkdirs();
        workingDirectory.deleteOnExit();
        mockPathHandler.clear();
    }

    private final static File getFaultyPathFile()
    {
        return new File(workingDirectory, FileConstants.FAULTY_PATH_FILENAME);
    }

    private final static void createNewFile(final File someFile) throws IOException
    {
        someFile.createNewFile();
        assert someFile.exists() && someFile.isFile();
        someFile.deleteOnExit();
    }

    @Test
    public void testFaultyPathsDeletion()
    {
        final File faultyPaths = getFaultyPathFile();
        CollectionIO.writeIterable(faultyPaths, Collections.singleton("some_path"));
        new DirectoryScanningTimerTask(workingDirectory, ACCEPT_ALL_FILTER, mockPathHandler);
        assertEquals(1, CollectionIO.readSet(faultyPaths).size());
    }

    @Test
    public void testProcessOK() throws IOException
    {
        final File someFile = new File(workingDirectory, "some_file");
        createNewFile(someFile);
        final File activityLogDir = new File(workingDirectory, ".activity_log");
        final String threadName = "abc_test";
        final DirectoryScanningTimerTask scanner =
                new DirectoryScanningTimerTask(workingDirectory, ACCEPT_ALL_BUT_HIDDEN_FILTER,
                        mockPathHandler, threadName, activityLogDir);
        assertEquals(0, mockPathHandler.handledPaths.size());
        scanner.run();
        assertEquals(1, mockPathHandler.handledPaths.size());
        assertEquals(someFile, mockPathHandler.handledPaths.get(0));
        final File activityLogFile = new File(activityLogDir, threadName);
        assertTrue(activityLogFile.exists());
        final long milliDiff = System.currentTimeMillis() - activityLogFile.lastModified();
        assertTrue(milliDiff + "ms", milliDiff < 1000);
    }

    @Test
    public void testFileFilterUsed() throws IOException
    {
        final File someFile = new File(workingDirectory, "some_file");
        createNewFile(someFile);
        final DirectoryScanningTimerTask scanner =
                new DirectoryScanningTimerTask(workingDirectory, ALWAYS_FALSE_FILE_FILTER,
                        mockPathHandler);
        assertEquals(0, mockPathHandler.handledPaths.size());
        scanner.run();
        assertEquals(0, mockPathHandler.handledPaths.size());
    }

    @Test
    public void testManipulateFaultyPaths() throws IOException
    {
        final File faultyPaths = getFaultyPathFile();
        final File someFile = new File(workingDirectory, "some_file");
        createNewFile(someFile);
        final DirectoryScanningTimerTask scanner =
                new DirectoryScanningTimerTask(workingDirectory, ACCEPT_ALL_FILTER, mockPathHandler);
        final String fileLocation = someFile.getPath();
        CollectionIO.writeIterable(faultyPaths, Collections.singleton(fileLocation));
        scanner.run();
        assertEquals(0, mockPathHandler.handledPaths.size());
    }

    @Test
    public void testFaultyPaths() throws IOException
    {
        final File faultyPaths = getFaultyPathFile();
        final File someFile = new File(workingDirectory, "some_file");
        final MockPathHandler myPathHandler = new MockPathHandler()
            {
                boolean firstTime = true;

                @Override
                public void handle(File path)
                {
                    if (firstTime)
                    {
                        firstTime = false;
                        return;
                    }
                    super.handle(path);
                }
            };
        createNewFile(someFile);
        final DirectoryScanningTimerTask scanner =
                new DirectoryScanningTimerTask(workingDirectory, ACCEPT_ALL_FILTER, myPathHandler);

        // See whether faulty_paths settings works.
        scanner.run();
        assertEquals(0, mockPathHandler.handledPaths.size());
        final List<String> faulty = CollectionIO.readList(faultyPaths);
        assertEquals(1, faulty.size());
        assertEquals(someFile.getPath(), faulty.get(0));
        // See whether fault_paths resetting works.
        assertTrue(faultyPaths.delete());
        myPathHandler.clear(); // Isn't necessary, just for expressing intention.
        // See whether faulty_paths settings works.
        scanner.run();
        assertEquals(1, myPathHandler.handledPaths.size());
        assertEquals(someFile, myPathHandler.handledPaths.get(0));
    }

    @Test
    public void testStopped() throws IOException
    {
        final DirectoryScanningTimerTask scanner =
                new DirectoryScanningTimerTask(workingDirectory, ACCEPT_ALL_FILTER,
                        new IPathHandler()
                            {
                                @Override
                                public void handle(File path)
                                {
                                    throw new AssertionError("Shouldn't have been called");
                                }

                                @Override
                                public boolean isStopped()
                                {
                                    return true;
                                }
                            });
        final File someFile = new File(workingDirectory, "some_file");
        someFile.createNewFile();
        assertTrue(someFile.exists());
        someFile.deleteOnExit();
        scanner.run();
    }

    @Test
    public void testStopInFirstFile() throws IOException
    {
        final File faultyPaths = new File(workingDirectory, FileConstants.FAULTY_PATH_FILENAME);
        faultyPaths.createNewFile();
        assertTrue(faultyPaths.exists());
        final AtomicInteger counter = new AtomicInteger(0);
        final DirectoryScanningTimerTask scanner =
                new DirectoryScanningTimerTask(workingDirectory, ACCEPT_ALL_FILTER,
                        new IPathHandler()
                            {
                                boolean stop = false;

                                @Override
                                public void handle(File path)
                                {
                                    counter.incrementAndGet();
                                    stop = true;
                                }

                                @Override
                                public boolean isStopped()
                                {
                                    return stop;
                                }
                            });
        final File someFile = new File(workingDirectory, "some_file");
        someFile.createNewFile();
        assertTrue(someFile.exists());
        final File someOtherFile = new File(workingDirectory, "some_other_file");
        someOtherFile.deleteOnExit();
        scanner.run();
        assertEquals(1, counter.get());
        assertEquals(0L, faultyPaths.length());
    }

    @Test
    public void testPathOrder() throws IOException
    {
        final File dir = new File(workingDirectory, "testPathOrder");
        final File f1 = new File(dir, "1");
        final File f2 = new File(dir, "2");
        final File f3 = new File(dir, "3");
        final File f4 = new File(dir, "4");
        final long now = System.currentTimeMillis();
        dir.mkdir();
        dir.deleteOnExit();
        createNewFile(f1);
        createNewFile(f2);
        createNewFile(f3);
        createNewFile(f4);
        // Order should be: 2, 4, 3, 1
        f2.setLastModified(now - 10000);
        f4.setLastModified(now - 5000);
        f3.setLastModified(now - 1000);
        f1.setLastModified(now);
        final DirectoryScanningTimerTask scanner =
                new DirectoryScanningTimerTask(dir, ACCEPT_ALL_FILTER, mockPathHandler);
        scanner.run();
        assertEquals(f2, mockPathHandler.handledPaths.get(0));
        assertEquals(f4, mockPathHandler.handledPaths.get(1));
        assertEquals(f3, mockPathHandler.handledPaths.get(2));
        assertEquals(f1, mockPathHandler.handledPaths.get(3));
        assertEquals(4, mockPathHandler.handledPaths.size());
    }

    @Test
    public void testMissingDirectory()
    {
        final File dir = new File(workingDirectory, "testMissingDirectory");
        dir.mkdir();
        final LogMonitoringAppender appender =
                LogMonitoringAppender.addAppender(LogCategory.NOTIFY,
                        "Failed to get listing of directory");
        try
        {
            // The directory needs to exist when the scanner is created, otherwise the self-test
            // will fail.
            final DirectoryScanningTimerTask scanner =
                    new DirectoryScanningTimerTask(dir, ACCEPT_ALL_FILTER, mockPathHandler);
            dir.delete();
            assert dir.exists() == false;
            scanner.run();
            appender.verifyLogHasHappened();
        } finally
        {
            LogMonitoringAppender.removeAppender(appender);
        }
    }

    @Test
    public void testDirectoryIsFile() throws IOException
    {
        final File dir = new File(workingDirectory, "testMissingDirectory");
        dir.mkdir();
        final LogMonitoringAppender appender =
                LogMonitoringAppender.addAppender(LogCategory.NOTIFY,
                        "Failed to get listing of directory");
        try
        {
            // The directory needs to exist when the scanner is created, otherwise the self-test
            // will fail.
            final DirectoryScanningTimerTask scanner =
                    new DirectoryScanningTimerTask(dir, ACCEPT_ALL_FILTER, mockPathHandler);
            dir.delete();
            createNewFile(dir);
            scanner.run();
            appender.verifyLogHasHappened();
            dir.delete();
        } finally
        {
            LogMonitoringAppender.removeAppender(appender);
        }
    }

    @Test
    public void testFailingFileFilter() throws IOException
    {
        final File dir = new File(workingDirectory, "testMissingDirectory");
        dir.mkdir();
        dir.deleteOnExit();
        final File file = new File(dir, "some.file");
        createNewFile(file);
        final LogMonitoringAppender appender1 =
                LogMonitoringAppender.addAppender(LogCategory.NOTIFY,
                        "Failed to filter store items for processing:");
        final LogMonitoringAppender appender2 =
                LogMonitoringAppender.addAppender(LogCategory.NOTIFY,
                        EXCEPTION_THROWING_FILE_FILTER_MESSAGE);
        try
        {
            // The directory needs to exist when the scanner is created, otherwise the self-test
            // will fail.
            final DirectoryScanningTimerTask scanner =
                    new DirectoryScanningTimerTask(dir, EXCEPTION_THROWING_FILE_FILTER,
                            mockPathHandler);
            scanner.run();
            appender1.verifyLogHasHappened();
            appender2.verifyLogHasHappened();
            file.delete();
            dir.delete();
        } finally
        {
            LogMonitoringAppender.removeAppender(appender1);
            LogMonitoringAppender.removeAppender(appender2);
        }
    }

    @Test
    public void testSuppressLogging() throws IOException
    {
        final File dir = new File(workingDirectory, "testSuppressLogging");
        dir.mkdir();
        final LogMonitoringAppender appenderNotifyError =
                LogMonitoringAppender.addAppender(LogCategory.NOTIFY,
                        "Failed to get listing of directory");
        final LogMonitoringAppender appenderOperationError =
                LogMonitoringAppender.addAppender(LogCategory.OPERATION,
                        "Failed to get listing of directory");
        final LogMonitoringAppender appenderOK =
                LogMonitoringAppender.addAppender(LogCategory.NOTIFY, "' is available again");
        try
        {
            final int numberOfErrorsToIgnore = 2;
            // The directory needs to exist when the scanner is created, otherwise the self-test
            // will fail.
            final DirectoryScanningTimerTask scanner =
                    new DirectoryScanningTimerTask(dir, ACCEPT_ALL_FILTER, mockPathHandler,
                            numberOfErrorsToIgnore);
            dir.delete();
            assert dir.exists() == false;
            // First error -> ignored
            scanner.run();
            appenderOperationError.verifyLogHasHappened();
            appenderNotifyError.verifyLogHasNotHappened();
            // Second error -> ignored
            appenderOperationError.reset();
            scanner.run();
            appenderOperationError.verifyLogHasHappened();
            appenderNotifyError.verifyLogHasNotHappened();
            // Third error -> recorded
            appenderOperationError.reset();
            scanner.run();
            appenderOperationError.verifyLogHasNotHappened();
            appenderNotifyError.verifyLogHasHappened();
            dir.mkdir();
            assert dir.exists();
            // Now it is OK again and that should be logged as well
            scanner.run();
            appenderOK.verifyLogHasHappened();
        } finally
        {
            LogMonitoringAppender.removeAppender(appenderNotifyError);
            LogMonitoringAppender.removeAppender(appenderOperationError);
            LogMonitoringAppender.removeAppender(appenderOK);
        }
    }

    @Test
    public void testDoNotLogDirectoryAvailableWhenNoErrorWasLogged() throws IOException
    {
        final File dir =
                new File(workingDirectory, "testDoNotLogDirectoryAvailableWhenNoErrorWasLogged");
        dir.mkdir();
        final LogMonitoringAppender appender =
                LogMonitoringAppender.addAppender(LogCategory.NOTIFY, "' is available again.");
        try
        {
            final int numberOfErrorsToIgnore = 2;
            // The directory needs to exist when the scanner is created, otherwise the self-test
            // will fail.
            final DirectoryScanningTimerTask scanner =
                    new DirectoryScanningTimerTask(dir, ACCEPT_ALL_FILTER, mockPathHandler,
                            numberOfErrorsToIgnore);
            dir.delete();
            assert dir.exists() == false;
            // First error -> ignored
            scanner.run();
            appender.verifyLogHasNotHappened();
            // Second error -> ignored
            scanner.run();
            appender.verifyLogHasNotHappened();
            dir.mkdir();
            assert dir.exists();
            // Now it's OK, but nothing should be logged because the error wasn't logged either.
            scanner.run();
            appender.verifyLogHasNotHappened();
        } finally
        {
            LogMonitoringAppender.removeAppender(appender);
        }
    }
}
