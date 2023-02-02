/*
 * Copyright 2011 ETH Zuerich, CISD
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
import java.io.OutputStream;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.collections4.map.ReferenceMap;

import ch.systemsx.cisd.hdf5.HDF5DataClass;
import ch.systemsx.cisd.hdf5.HDF5DataSetInformation;
import ch.systemsx.cisd.hdf5.HDF5DataTypeInformation;
import ch.systemsx.cisd.hdf5.HDF5DataTypeInformation.DataTypeInfoOptions;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import ch.systemsx.cisd.hdf5.h5ar.ArchiveEntry;
import ch.systemsx.cisd.hdf5.h5ar.HDF5ArchiverFactory;
import ch.systemsx.cisd.hdf5.h5ar.IHDF5ArchiveReader;
import ch.systemsx.cisd.hdf5.h5ar.ListParameters;

/**
 * An implementation of {@link IHDF5ContainerReader}.
 * 
 * @author Bernd Rinn
 */
final class HDF5ContainerReader implements IHDF5ContainerReader
{
    private static final long CACHE_CLEANER_INTERVAL_MILLIS = 60000L;

    /**
     * A container for reader which stores last access time and current reference count.
     */
    private static class Reader
    {
        private final static long RETENTION_TIME_MILLIS = 5 * 60 * 1000L; // 5 minutes

        private final IHDF5Reader hdf5Reader;

        private final IHDF5ArchiveReader archiveReader;

        private final File archiveFile;

        private long lastAccessed;

        private int referenceCount;

        Reader(File archiveFile)
        {
            this.archiveFile = archiveFile;
            this.hdf5Reader = HDF5Factory.openForReading(archiveFile);
            this.archiveReader = HDF5ArchiverFactory.openForReading(hdf5Reader);
            this.lastAccessed = System.currentTimeMillis();
            this.referenceCount = 1;
        }

        IHDF5ArchiveReader getArchiveReader()
        {
            this.lastAccessed = System.currentTimeMillis();
            return archiveReader;
        }

        IHDF5Reader getHDF5Reader()
        {
            this.lastAccessed = System.currentTimeMillis();
            return hdf5Reader;
        }

        File getArchiveFile()
        {
            return archiveFile;
        }

        void incCount()
        {
            ++referenceCount;
        }

        void decCount()
        {
            --referenceCount;
        }

        boolean isUnreferenced()
        {
            return referenceCount == 0;
        }

        boolean isExpired()
        {
            return (referenceCount == 0 && (System.currentTimeMillis() - lastAccessed) > RETENTION_TIME_MILLIS);
        }
    }

    static
    {
        final Timer t = new Timer("HDF5ContainerReader - Cache Cleaner", true);
        t.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    synchronized (fileToReaderMap)
                    {
                        final Iterator<Reader> it = fileToReaderMap.values().iterator();
                        while (it.hasNext())
                        {
                            final Reader container = it.next();
                            if (container.isExpired())
                            {
                                container.getArchiveReader().close();
                                it.remove();
                            }
                        }
                    }
                }
            }, CACHE_CLEANER_INTERVAL_MILLIS, CACHE_CLEANER_INTERVAL_MILLIS);
    }

    private static final Map<File, Reader> fileToReaderMap =
            new ReferenceMap<File, HDF5ContainerReader.Reader>();

    private static boolean noCaching = false;

    /**
     * Disable caching for unit testing where the same file name is reused but the file content changes between tests.
     */
    static void disableCaching()
    {
        noCaching = true;
        synchronized (fileToReaderMap)
        {
            for (Reader r : fileToReaderMap.values())
            {
                r.getArchiveReader().close();
            }
            fileToReaderMap.clear();
        }
    }

    private static Reader openReader(File hdf5Container)
    {
        Reader entryOrNull;
        synchronized (fileToReaderMap)
        {
            entryOrNull = fileToReaderMap.get(hdf5Container);
            if (entryOrNull == null)
            {
                entryOrNull =
                        new Reader(hdf5Container);
                fileToReaderMap.put(hdf5Container, entryOrNull);
            } else
            {
                entryOrNull.incCount();
            }
        }
        return entryOrNull;
    }

    private final Reader reader;

    HDF5ContainerReader(final File hdf5Container)
    {
        this.reader = openReader(hdf5Container);
    }

    @Override
    public void close()
    {
        reader.decCount();
        if (noCaching && reader.isUnreferenced())
        {
            synchronized (fileToReaderMap)
            {
                reader.getArchiveReader().close();
                reader.getHDF5Reader().close();
                fileToReaderMap.remove(reader.getArchiveFile());
            }
        }
    }

    @Override
    public boolean exists(String objectPath)
    {
        return reader.getArchiveReader().exists(objectPath);
    }

    @Override
    public ArchiveEntry tryGetEntry(String path)
    {
        return reader.getArchiveReader().tryGetResolvedEntry(path, true);
    }

    @Override
    public List<ArchiveEntry> getGroupMembers(String groupPath)
    {
        return reader.getArchiveReader().list(groupPath, ListParameters.build().nonRecursive()
                .resolveSymbolicLinks().get());
    }

    @Override
    public void readFromHDF5Container(String objectPath, OutputStream ostream)
    {
        reader.getArchiveReader().extractFile(objectPath, ostream);
    }

    @Override
    public boolean isFileAbstraction(ArchiveEntry entry)
    {
        final HDF5DataSetInformation info = reader.getHDF5Reader().object().getDataSetInformation(entry.getPath(), DataTypeInfoOptions.MINIMAL);
        final HDF5DataTypeInformation tInfo = info.getTypeInformation();
        return (EnumSet.of(HDF5DataClass.INTEGER, HDF5DataClass.OPAQUE).contains(tInfo.getDataClass())
                && tInfo.getElementSize() == 1
                && info.getRank() == 1);
    }
}
