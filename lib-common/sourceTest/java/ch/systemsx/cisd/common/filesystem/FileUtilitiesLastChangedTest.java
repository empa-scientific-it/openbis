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
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.IOException;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UnknownLastChangedException;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.test.RetryTen;
import ch.systemsx.cisd.common.test.TestReportCleaner;

/**
 * Test cases for the {@link FileUtilities#lastChanged(File)} method.
 * 
 * @author Bernd Rinn
 */
@Listeners(TestReportCleaner.class)
public class FileUtilitiesLastChangedTest
{

    private static final File workingDirectory =
            new File("targets" + File.separator + "unit-test-wd");

    @DataProvider(name = "testLastChanged")
    public Object[][] getDirectories()
    {
        return new Object[][]
        {
                { ".fsLastChangedCheckerTestDirectory",
                        ".fsLastChangedCheckerTestDirectory" },
                { ".fsLastChangedCheckerTestDirectory",
                        ".fsLastChangedCheckerTestDirectory" + File.separator + "1" },
                {
                        ".fsLastChangedCheckerTestDirectory",
                        ".fsLastChangedCheckerTestDirectory" + File.separator + "1"
                                + File.separator + 2 },
                {
                        ".fsLastChangedCheckerTestDirectory",
                        ".fsLastChangedCheckerTestDirectory" + File.separator + "1"
                                + File.separator + 2 + File.separator + 3 } };
    }

    private void restALittleBit()
    {
        try
        {
            Thread.sleep(1500);
        } catch (InterruptedException e)
        {
            throw new CheckedExceptionTunnel(e);
        }
    }

    @BeforeSuite
    public void init()
    {
        LogInitializer.init();
        workingDirectory.mkdirs();
        assert workingDirectory.isDirectory();
    }

    @Test(groups =
    { "slow" }, dataProvider = "testLastChanged")
    public void testLastChanged(String dirToCheck, String dirToCreate)
            throws UnknownLastChangedException
    {
        final File testDir = new File(workingDirectory, dirToCheck);
        testDir.deleteOnExit();
        if (testDir.exists() && testDir.isDirectory() == false)
        {
            throw EnvironmentFailureException.fromTemplate(
                    "Directory '%s' exists and is not a directory.", testDir.getPath());
        }
        final File createDir = new File(workingDirectory, dirToCreate);
        if (createDir.exists() && FileUtilities.deleteRecursively(createDir) == false)
        {
            throw EnvironmentFailureException.fromTemplate(
                    "Directory '%s' exists and cannot be deleted.", createDir.getPath());
        }
        createDir.deleteOnExit();
        final long now = System.currentTimeMillis();
        if (createDir.mkdir() == false)
        {
            throw new EnvironmentFailureException("Can't make directory " + createDir.getPath()
                    + ".");
        }
        final long lastChanged = FileUtilities.lastChanged(testDir);
        final long diff = Math.abs(lastChanged - now);
        assert diff <= 2500 : "expected difference less than 2.5s, but was " + diff / 1000.0 + "s";

        // We need to wait for more than 1s because that's the granularity of the changed time
        // attribute saved with
        // files.
        restALittleBit();
    }

    @Test
    public void testLastChangedSpecialFeatures() throws IOException
    {
        final File dirA = new File(workingDirectory, "a");
        dirA.mkdir();
        dirA.deleteOnExit();
        final File dirB = new File(dirA, "b");
        dirB.mkdir();
        dirB.deleteOnExit();
        final File fileC = new File(dirB, "c");
        fileC.createNewFile();
        fileC.deleteOnExit();
        fileC.setLastModified(3000L);
        dirB.setLastModified(2000L);
        dirA.setLastModified(1000L);
        assertEquals(3000L, FileUtilities.lastChanged(dirA, false, 0L));
        assertEquals(2000L, FileUtilities.lastChanged(dirA, true, 0L));
        assertEquals(1000L, FileUtilities.lastChanged(dirA, false, 999L));
        assertEquals(1000L, FileUtilities.lastChanged(dirA, true, 999L));
    }

    @Test(retryAnalyzer = RetryTen.class)
    public void testLastChangedRelative() throws IOException
    {
        final File dirA = new File(workingDirectory, "a-relative");
        final long now = System.currentTimeMillis();
        dirA.mkdir();
        dirA.deleteOnExit();
        final File fileB = new File(dirA, "b");
        fileB.createNewFile();
        fileB.deleteOnExit();
        final long fakedModTime1 = now - 10000L;
        final long fakedModTime2 = now - 30000L;
        fileB.setLastModified(fakedModTime1);
        dirA.setLastModified(fakedModTime2);
        final long diff1 =
                Math.abs(fakedModTime2 - FileUtilities.lastChangedRelative(dirA, false, 31000L));
        assertTrue("Difference to big: " + diff1 + " ms", diff1 < 1000L);
        final long diff2 =
                Math.abs(fakedModTime1 - FileUtilities.lastChangedRelative(dirA, false, 11000L));
        assertTrue("Difference to big: " + diff2 + " ms", diff2 < 1000L);
    }

}
