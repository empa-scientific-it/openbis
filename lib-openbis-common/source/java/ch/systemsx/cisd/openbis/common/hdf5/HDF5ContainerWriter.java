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
import java.io.InputStream;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.hdf5.h5ar.ArchivingStrategy;
import ch.systemsx.cisd.hdf5.h5ar.HDF5ArchiverFactory;
import ch.systemsx.cisd.hdf5.h5ar.IHDF5Archiver;
import ch.systemsx.cisd.hdf5.h5ar.NewArchiveEntry;

/**
 * An implementation of {@link IHDF5ContainerWriter}.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class HDF5ContainerWriter implements IHDF5ContainerWriter
{
    private static final ArchivingStrategy COMPRESSED_STRATEGY = new ArchivingStrategy()
            .compressAll().seal();

    private final IHDF5Archiver archiver;

    private final boolean compress;

    private final ArchivingStrategy strategy;

    HDF5ContainerWriter(HDF5Container parent, File containerFile, boolean compress)
    {
        this.archiver = HDF5ArchiverFactory.open(containerFile);
        this.compress = compress;
        this.strategy = compress ? COMPRESSED_STRATEGY : ArchivingStrategy.DEFAULT;
    }

    @Override
    public void archiveToHDF5Container(String rootPath, File path) throws IOExceptionUnchecked
    {
        if (path.isFile())
        {
            archiver.archiveFromFilesystem(rootPath, path, strategy);
        } else
        {
            archiver.archiveFromFilesystemBelowDirectory(rootPath, path, strategy);
        }
    }

    @Override
    public void writeToHDF5Container(String objectPath, InputStream istream, long size)
            throws IOExceptionUnchecked
    {
        archiver.archiveFile(NewArchiveEntry.file(objectPath).compress(compress), istream);
    }

    /**
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleReader#close()
     */
    @Override
    public void close()
    {
        archiver.close();
    }

}
