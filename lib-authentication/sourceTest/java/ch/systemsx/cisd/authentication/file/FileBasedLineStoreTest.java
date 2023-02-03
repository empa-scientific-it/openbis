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

package ch.systemsx.cisd.authentication.file;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * Test cases for the {@link FileBasedLineStore}.
 * 
 * @author Bernd Rinn
 */
public class FileBasedLineStoreTest
{

    private final static String fileName = "store";

    private final static String fileDescription = "Some sort of file";

    private static final File unitTestRootDirectory =
            new File("targets" + File.separator + "unit-test-wd");

    private static final File workingDirectory =
            new File(unitTestRootDirectory, "FileBasedLineStoreTest");

    private final static File file = new File(workingDirectory, fileName);

    private final static FileBasedLineStore store = new FileBasedLineStore(file, fileDescription);

    @AfterClass
    public void cleanUp()
    {
        FileUtilities.deleteRecursively(workingDirectory);
    }

    @Test
    public void testCheck()
    {
        file.delete();
        assertFalse(file.exists());
        store.check();
        assertTrue(file.exists());
    }

    @Test
    public void testGetId()
    {
        assertEquals(file.getPath(), store.getId());
    }

    @Test
    public void testReadLinesNonExisting()
    {
        file.delete();
        assertFalse(file.exists());
        assertEquals(0, store.readLines().size());
        assertFalse(file.exists());
    }

    @Test
    public void testRoundtrip() throws IOException
    {
        final File svFile = new File(file.getPath() + ".sv");
        svFile.delete();
        assertFalse(svFile.exists());
        file.delete();
        assertFalse(file.exists());

        assertFalse(store.hasChanged());
        final List<String> lines1 = Arrays.asList("1", "2", "3");
        store.writeLines(lines1);
        assertTrue(file.exists());
        assertFalse(store.hasChanged());
        file.setLastModified(System.currentTimeMillis() + 1000);
        assertTrue(store.hasChanged());
        assertEquals(StringUtils.join(lines1, '\n') + "\n", FileUtils.readFileToString(file));
        final List<String> linesRead1 = store.readLines();
        assertFalse(store.hasChanged());
        assertEquals(lines1, linesRead1);
        assertTrue(svFile.exists());
        assertEquals(0, svFile.length());

        final List<String> lines2 = Arrays.asList("4", "5", "6");
        store.writeLines(lines2);
        assertTrue(file.exists());
        assertTrue(svFile.exists());
        assertEquals(StringUtils.join(lines1, '\n') + "\n", FileUtils.readFileToString(svFile));
        assertEquals(StringUtils.join(lines2, '\n') + "\n", FileUtils.readFileToString(file));
        final List<String> linesRead2 = store.readLines();
        assertEquals(lines2, linesRead2);
    }

}
