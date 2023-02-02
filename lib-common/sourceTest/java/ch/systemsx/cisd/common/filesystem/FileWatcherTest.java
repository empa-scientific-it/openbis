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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.io.File;
import java.io.IOException;
import java.util.Timer;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.FileWatcher;
import ch.systemsx.cisd.common.logging.BufferedAppender;

/**
 * Test cases for the {@link FileWatcher}.
 * <p>
 * Note that this test is suite aware. This test will probably fail if you call it alone.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public class FileWatcherTest
{
    private static final File unitTestRootDirectory =
            new File("targets" + File.separator + "unit-test-wd");

    private static final File workingDirectory = new File(unitTestRootDirectory, "FileWatcherTest");

    private static final File tmpFile1 = new File(workingDirectory, "tmpFile1");

    private static final File tmpFile2 = new File(workingDirectory, "tmpFile2");

    private BufferedAppender testAppender;

    private volatile boolean onChangeCalled;

    private final static void createNewFile(File file) throws IOException
    {
        FileUtils.touch(file);
        file.setLastModified(0);
        assert file.exists();
        file.deleteOnExit();
    }

    @BeforeMethod(alwaysRun = true)
    public final void setUp() throws IOException
    {
        workingDirectory.mkdirs();
        assert workingDirectory.isDirectory();
        createNewFile(tmpFile1);
        createNewFile(tmpFile2);
        workingDirectory.deleteOnExit();
        Logger.getRootLogger().setLevel(Level.TRACE);
        testAppender = new BufferedAppender(Level.TRACE);
    }

    @AfterMethod(alwaysRun = true)
    public final void tearDown()
    {
        testAppender.reset();
        FileUtilities.deleteRecursively(workingDirectory);
    }

    @Test
    public final void testWithNonExistingFile()
    {
        File file = new File(workingDirectory, "doesNotExist");
        assert file.exists() == false;
        new TestFileWatcher(file).run();
        assertEquals(String.format(FileWatcher.DOES_NOT_EXIST_FORMAT, file), testAppender
                .getLogContent());
    }

    @Test
    public final void testNonChangingFile()
    {
        new TestFileWatcher(tmpFile1).run();
        assertEquals(String.format(FileWatcher.HAS_NOT_CHANGED_FORMAT, tmpFile1), testAppender
                .getLogContent());
    }

    @Test
    public final void testFileHasChanged() throws IOException
    {
        onChangeCalled = false;
        FileWatcher fileWatcher = new TestFileWatcher(tmpFile1);
        FileUtils.touch(tmpFile1);
        fileWatcher.run();
        assertEquals(String.format(FileWatcher.HAS_CHANGED_FORMAT, tmpFile1), testAppender
                .getLogContent());
        assertEquals(true, onChangeCalled);
    }

    @Test(timeOut = 5000, groups = "slow")
    public final void testOnChangeCalled() throws IOException
    {
        onChangeCalled = false;
        FileWatcher fileWatcher = new TestFileWatcher(tmpFile2);
        FileUtils.touch(tmpFile2);
        Timer timer = new Timer(true);
        timer.schedule(fileWatcher, 0);
        while (onChangeCalled == false)
        {
            try
            {
                Thread.sleep(200);
            } catch (InterruptedException ex)
            {
                fail(ex.getMessage());
            }
        }
    }

    //
    // Helper classes
    //

    private final class TestFileWatcher extends FileWatcher
    {

        TestFileWatcher(File file)
        {
            super(file);
        }

        @Override
        protected void onChange()
        {
            onChangeCalled = true;
        }
    }
}
