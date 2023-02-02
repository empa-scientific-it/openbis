/*
 * Copyright 2008 ETH Zuerich, CISD
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
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.test.RetryTen;
import ch.systemsx.cisd.common.test.TestReportCleaner;

/**
 * Test cases for the {@link QueueingPathRemoverService}.
 * 
 * @author Bernd Rinn
 */
@Listeners(TestReportCleaner.class)
public class QueueingPathRemoverServiceTest
{

    private static final long WAIT_MILLIS = 300L;

    private static final long DELAY_MILLIS = 1L;

    private static final File wdRootDirectory =
            new File("targets" + File.separator + "unit-test-wd");

    private static final File workingDirectory = new File(wdRootDirectory, "ShredderTest");

    private static final File queueFile = new File(workingDirectory, "qfile.dat");

    private static final FilenameFilter SHREDDER_FILTER = new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String name)
            {
                return name.startsWith(QueueingPathRemoverService.SHREDDER_PREFIX);
            }
        };

    @BeforeClass
    public void setUp()
    {
        LogInitializer.init();
        queueFile.deleteOnExit();
        workingDirectory.mkdirs();
        QueueingPathRemoverService.start(wdRootDirectory, queueFile);
    }

    @BeforeMethod
    public void cleanUp() throws IOException
    {
        File[] files = workingDirectory.listFiles(SHREDDER_FILTER);
        if (files != null)
        {
            for (File f : files)
            {
                for (int i = 0; f.exists() && i < 3; i++)
                {
                    FileUtilities.deleteRecursively(f);
                }
                assertFalse(f.exists());
            }
        }
    }

    @Test(groups = "slow")
    public void testShredderFile() throws IOException
    {
        final File f = new File(workingDirectory, "someFile");
        f.createNewFile();
        f.deleteOnExit();
        assertTrue(f.exists());
        QueueingPathRemoverService.removeRecursively(f);
        assertFalse(f.exists());
        ConcurrencyUtilities.sleep(WAIT_MILLIS);
        assertEquals(0, workingDirectory.list(SHREDDER_FILTER).length);
    }

    @Test(groups = "slow", retryAnalyzer = RetryTen.class)
    public void testShredderDirectory() throws IOException
    {
        final File f = new File(workingDirectory, "someDir");
        f.mkdir();
        for (int i = 0; i < 100; ++i)
        {
            (new File(f, "d" + i)).mkdir();
        }
        for (int i = 0; i < 100; ++i)
        {
            (new File(f, "f" + i)).createNewFile();
        }
        assertTrue(f.exists());
        QueueingPathRemoverService.removeRecursively(f);
        assertFalse(f.exists());
        ConcurrencyUtilities.sleep(WAIT_MILLIS);
        assertEquals(0, workingDirectory.list(SHREDDER_FILTER).length);
    }

    @Test(groups = "slow")
    public void testManyFiles() throws IOException
    {
        final List<File> list = new ArrayList<File>();
        for (int i = 0; i < 100; ++i)
        {
            final File f = new File(workingDirectory, "oneOfMany" + i);
            f.createNewFile();
            list.add(f);
        }
        for (File f : list)
        {
            QueueingPathRemoverService.removeRecursively(f);
            assertFalse(f.exists());
        }
        ConcurrencyUtilities.sleep(WAIT_MILLIS);
        assertEquals(0, workingDirectory.list(SHREDDER_FILTER).length);
    }

    @Test(groups = "slow")
    public void testManyFilesSlowlyComingIn() throws IOException
    {
        final List<File> list = new ArrayList<File>();
        for (int i = 0; i < 100; ++i)
        {
            final File f = new File(workingDirectory, "oneOfMany" + i);
            f.createNewFile();
            list.add(f);
        }
        for (File f : list)
        {
            QueueingPathRemoverService.removeRecursively(f);
            ConcurrencyUtilities.sleep(DELAY_MILLIS);
            assertFalse(f.exists());
        }
        ConcurrencyUtilities.sleep(WAIT_MILLIS);
        assertEquals(0, workingDirectory.list(SHREDDER_FILTER).length);
    }

}
