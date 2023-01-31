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

package ch.systemsx.cisd.etlserver;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.time.TimingParameters;

/**
 * Test cases for corresponding {@link FileBasedFile} class.
 * 
 * @author Franz-Josef Elmer
 */
public class FileBasedFileTest
{
    private static final File WORKING_DIRECTORY =
            new File("targets/unit-test-wd/FileBasedFileTest");

    private static final File DESTINATION = new File(WORKING_DIRECTORY, "destination");

    @BeforeTest
    public void setUp()
    {
        LogInitializer.init();
        FileUtilities.deleteRecursively(WORKING_DIRECTORY);
        assertTrue(WORKING_DIRECTORY.mkdirs());
        assertTrue(DESTINATION.mkdirs());
    }

    @Test
    public void copyFileUsingHardLinks()
    {
        File file = new File(WORKING_DIRECTORY, "test.txt");
        FileUtilities.writeToFile(file, "hello world!");
        File destFile = new File(DESTINATION, "copy_of_test.txt");
        IFile destinationFile =
                new FileBasedFileFactory(true, TimingParameters.getNoTimeoutNoRetriesParameters())
                        .create(destFile.getPath());

        destinationFile.copyFrom(file);

        assertEquals("hello world!", FileUtilities.loadToString(destFile).trim());
    }

    @Test
    public void copyDirectoryUsingHardLinks()
    {
        File folder = new File(WORKING_DIRECTORY, "folder");
        assertTrue(folder.mkdir());
        File file1 = new File(folder, "file1.txt");
        FileUtilities.writeToFile(file1, "hello file1");
        File file2 = new File(folder, "file2.txt");
        FileUtilities.writeToFile(file2, "hello file2");
        File destFolder = new File(DESTINATION, "copy_folder");
        IFile destinationFolder =
                new FileBasedFileFactory(true, TimingParameters.createNoRetries(2000L))
                        .create(destFolder.getPath());

        destinationFolder.copyFrom(folder);

        assertEquals("hello file1", FileUtilities.loadToString(
                new File(DESTINATION, "copy_folder/file1.txt")).trim());
        assertEquals("hello file2", FileUtilities.loadToString(
                new File(DESTINATION, "copy_folder/file2.txt")).trim());
    }
}
