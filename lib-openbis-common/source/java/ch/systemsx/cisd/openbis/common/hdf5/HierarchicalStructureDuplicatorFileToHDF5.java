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

import java.io.File;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;

/**
 * Takes the hierarchical structure of a file and applies it to an HDF5 container.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class HierarchicalStructureDuplicatorFileToHDF5
{
    private final File file;

    private final String rootPath;

    private final IHDF5ContainerWriter writer;

    /**
     * Utility class that adapts to the IHdf5WriterClient interface
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static class DuplicatorWriterClient implements HDF5Container.IHDF5WriterClient
    {
        private final File file;

        private final String rootPath;

        public DuplicatorWriterClient(File file)
        {
            this(file, "/");
        }

        public DuplicatorWriterClient(File file, String rootPath)
        {
            this.file = file;
            this.rootPath = rootPath;
        }

        @Override
        public void runWithSimpleWriter(IHDF5ContainerWriter writer)
        {
            HierarchicalStructureDuplicatorFileToHDF5.makeDuplicate(file, rootPath, writer);
        }

    }

    /**
     * Makes a duplicate; does not close the writer when finished it is expected that users of this method close the writer.
     * 
     * @param file The file to duplicate.
     * @param rootPath The root path in the container. Should end with "/"
     * @param writer The writer on the container that should house the duplicate.
     * @throws IllegalArgumentException Thrown if one of the files to duplicate is a symbolic link or the file does not exist
     * @throws CheckedExceptionTunnel Thrown if an underlying error occurs
     * @throws IOExceptionUnchecked Thrown if an underlying error occurs
     */
    public static void makeDuplicate(File file, String rootPath, IHDF5ContainerWriter writer)
    {
        new HierarchicalStructureDuplicatorFileToHDF5(file, rootPath, writer).makeDuplicate();
    }

    private HierarchicalStructureDuplicatorFileToHDF5(File file, String rootPath,
            IHDF5ContainerWriter writer)
    {
        this.file = file;
        this.rootPath = rootPath;
        this.writer = writer;
    }

    private void makeDuplicate()
    {
        if (false == file.exists())
        {
            throw new IllegalArgumentException("File does not exist " + file);
        }
        writer.archiveToHDF5Container(rootPath, file);
    }

}
