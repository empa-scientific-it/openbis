/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.filesystem.tar;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FileUtils;

import ch.systemsx.cisd.base.unix.Unix;
import ch.systemsx.cisd.common.io.MonitoredIOStreamCopier;
import ch.systemsx.cisd.common.logging.ConsoleLogger;

/**
 * Simple interface to the Apache commons tar classes for extracting tar files to a directory.
 *
 * @author Bernd Rinn
 */
public class Untar implements Closeable
{

    private final static int DEFAULT_BUFFER_SIZE = 128 * 1024;

    private final TarArchiveInputStream in;

    private final boolean setOwner;

    private final MonitoredIOStreamCopier copier;

    public Untar(final File tarFile) throws FileNotFoundException
    {
        this(tarFile, DEFAULT_BUFFER_SIZE);
    }

    public Untar(final File tarFile, final int bufferSize) throws FileNotFoundException
    {
        this(tarFile, new MonitoredIOStreamCopier(bufferSize));
    }

    public Untar(final File tarFile, MonitoredIOStreamCopier copier) throws FileNotFoundException
    {
        this.copier = copier;
        this.in = new TarArchiveInputStream(new FileInputStream(tarFile));
        this.setOwner = (Unix.isOperational() && Unix.getEuid() == 0);
    }

    /**
     * Extracts the tar file.
     * 
     * @param rootDirectory The directory to extract the tar file into.
     */
    public void extract(final File rootDirectory) throws IOException
    {
        final List<TarArchiveEntry> dirEntries = new ArrayList<TarArchiveEntry>();

        TarArchiveEntry entry;
        while ((entry = in.getNextTarEntry()) != null)
        {
            final File entryFile = new File(rootDirectory, entry.getName());
            extractEntry(entry, entryFile, dirEntries);
        }
        // Set directory metadata in reverse order, i.e. descendants first
        final ListIterator<TarArchiveEntry> it = dirEntries
                .listIterator(dirEntries.size());
        while (it.hasPrevious())
        {
            entry = it.previous();
            setFileMetadata(new File(rootDirectory, entry.getName()), entry);
        }
    }

    /**
     * Extracts the tar file. Each top level directory is extracted to a different location specified by the {@code locations} argument. Only part of
     * the file is extracted. Top level directories not present in {@code locations} will be omitted.
     * 
     * @param locations maps top level directory names to directories where those should be extracted
     */
    public void extract(final Map<String, File> locations) throws IOException
    {
        final List<TarArchiveEntry> dirEntries = new ArrayList<TarArchiveEntry>();

        TarArchiveEntry entry;
        while ((entry = in.getNextTarEntry()) != null)
        {
            final File entryFile = getEntryInLocation(entry, locations);
            if (entryFile == null)
            {
                continue;
            }
            extractEntry(entry, entryFile, dirEntries);
        }
        // Set directory metadata in reverse order, i.e. descendants first
        final ListIterator<TarArchiveEntry> it = dirEntries
                .listIterator(dirEntries.size());
        while (it.hasPrevious())
        {
            entry = it.previous();
            File entryFile = getEntryInLocation(entry, locations);
            setFileMetadata(entryFile, entry);
        }
    }

    private File getEntryInLocation(TarArchiveEntry entry, final Map<String, File> locations)
    {
        String[] parts = entry.getName().split("/", 2);
        String head = parts[0];
        String tail = parts[1];

        File parent = locations.get(head);
        return parent == null ? null : new File(parent, tail);
    }

    private void extractEntry(TarArchiveEntry entry, final File entryFile, final List<TarArchiveEntry> dirEntries) throws FileNotFoundException,
            IOException
    {
        if (entry.isDirectory())
        {
            dirEntries.add(entry);
            createDirectory(entryFile);
        } else if (entry.isLink())
        {
            createHardLink(entryFile, entry);
        } else if (entry.isSymbolicLink())
        {
            createSymbolicLink(entryFile, entry);
        } else if (entry.isFile())
        {
            // We need to work around that isFile() is giving all
            // kinds of false positives on what is a file.
            if (entry.isBlockDevice() || entry.isCharacterDevice()
                    || entry.isFIFO() || entry.isGNULongLinkEntry()
                    || entry.isGNULongNameEntry() || entry.isGNUSparse()
                    || entry.isPaxHeader() || entry.isGlobalPaxHeader())
            {
                return;
            }
            createParentDirectories(entryFile);
            extractFileContent(entryFile, entry);
            setFileMetadata(entryFile, entry);
        }
    }

    private void extractFileContent(final File entryFile, TarArchiveEntry entry) throws FileNotFoundException, IOException
    {
        final OutputStream out = createOutputStream(entryFile, entry);
        try
        {
            copier.copy(in, out);
        } finally
        {
            out.close();
        }
    }

    protected void createParentDirectories(final File file)
    {
        if (file.getParentFile().exists() == false)
        {
            file.getParentFile().mkdirs();
        }
    }

    protected void createSymbolicLink(final File file, TarArchiveEntry entry)
    {
        if (Unix.isOperational())
        {
            Unix.createSymbolicLink(entry.getLinkName(),
                    file.getPath());
        }
    }

    protected void createHardLink(final File file, TarArchiveEntry entry)
    {
        if (Unix.isOperational())
        {
            Unix.createHardLink(entry.getLinkName(),
                    file.getPath());
        }
    }

    protected void createDirectory(final File directory)
    {
        directory.mkdirs();
    }

    protected void setFileMetadata(final File entryFile, TarArchiveEntry entry)
    {
        if (Unix.isOperational())
        {
            Unix.setAccessMode(entryFile.getPath(), (short) entry.getMode());
            if (setOwner)
            {
                Unix.setOwner(entryFile.getPath(), entry.getUserId(),
                        entry.getGroupId());
            } else if (Unix.getUid() == entry.getUserId()
                    && Unix.getGid() != entry.getGroupId())
            {
                Unix.setOwner(entryFile.getPath(), entry.getUserId(),
                        entry.getGroupId());
            }
        }
        entryFile.setLastModified(entry.getModTime().getTime());
    }

    protected OutputStream createOutputStream(final File file, TarArchiveEntry entry) throws IOException
    {
        return new FileOutputStream(file);
    }

    @Override
    public void close() throws IOException
    {
        in.close();
        copier.close();
    }

    public static void main(String[] args) throws FileNotFoundException, IOException
    {
        if (args.length != 2)
        {
            System.err.println("Untar <tarfile> <directory>");
            System.exit(1);
        }
        final File tarFile = new File(args[0]);
        final File directory = new File(args[1]);
        Untar untar = null;
        try
        {
            MonitoredIOStreamCopier copier = new MonitoredIOStreamCopier((int) FileUtils.ONE_MB);
            copier.setLogger(new ConsoleLogger());
            untar = new Untar(tarFile, copier);
            untar.extract(directory);
            untar.close();
        } finally
        {
            if (untar != null)
            {
                untar.close();
            }
        }
    }

}
