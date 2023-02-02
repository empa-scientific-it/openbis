/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.common.hdf5;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.testng.AssertJUnit;

import ch.systemsx.cisd.openbis.common.hdf5.HDF5Container.IHDF5ReaderClient;

/**
 * Helper class that verifies that a file structure is matched by the HDF5 structure.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class FileToHDF5DuplicationVerifier extends AssertJUnit
{

    private final File sourceFolderOrFile;

    private final HDF5Container container;

    private final IHDF5ContainerReader reader;

    public static IHDF5ReaderClient createVerifierClient(File sourceFolderOrFile,
            HDF5Container container)
    {
        return new ReaderClient(sourceFolderOrFile, container);
    }

    private static class ReaderClient implements IHDF5ReaderClient
    {
        private final File sourceFolderOrFile;

        private final HDF5Container container;

        private ReaderClient(File sourceFolderOrFile, HDF5Container container)
        {
            this.sourceFolderOrFile = sourceFolderOrFile;
            this.container = container;
        }

        @Override
        public void runWithSimpleReader(IHDF5ContainerReader reader)
        {
            new FileToHDF5DuplicationVerifier(sourceFolderOrFile, container, reader)
                    .verifyDuplicate();
        }
    }

    public FileToHDF5DuplicationVerifier(File sourceFolderOrFile, HDF5Container container,
            IHDF5ContainerReader reader)
    {
        this.sourceFolderOrFile = sourceFolderOrFile;
        this.container = container;
        this.reader = reader;
    }

    public void verifyDuplicate()
    {
        assertTrue(container.getHDF5File().length() > 0);

        if (sourceFolderOrFile.isFile())
        {
            // Check that the HDF5 container is contains the same file as the provided file
            String name = sourceFolderOrFile.getName();
            String hdf5Path = "/" + name;
            verifyDataSet(sourceFolderOrFile, hdf5Path);
            return;
        }

        verifyGroup(sourceFolderOrFile, "/");
    }

    private void verifyGroup(File directory, String groupPath)
    {
        File[] files = directory.listFiles();
        for (File fileOrDirectory : files)
        {
            String childPath = groupPath + fileOrDirectory.getName();
            if (fileOrDirectory.isDirectory())
            {
                // recursively verify the directory
                verifyGroup(fileOrDirectory, childPath + "/");
            } else
            {
                // verify the data set
                verifyDataSet(fileOrDirectory, childPath);
            }

        }
    }

    private void verifyDataSet(File file, String hdf5Path)
    {
        assertTrue(hdf5Path + " does not exist", reader.exists(hdf5Path));
        try
        {
            byte[] fileContent = FileUtils.readFileToByteArray(file);
            final ByteArrayOutputStream ostream = new ByteArrayOutputStream();
            reader.readFromHDF5Container(hdf5Path, ostream);
            byte[] content = ostream.toByteArray();
            assertEquals(file.getAbsolutePath() + " does not equal " + hdf5Path, fileContent,
                    content);
        } catch (IOException ex)
        {
            // This should not happen
            fail("Could not read file content " + file);
        }
    }
}
