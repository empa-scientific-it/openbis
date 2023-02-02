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

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.FileUtils;

import ch.systemsx.cisd.base.unix.FileLinkType;
import ch.systemsx.cisd.base.unix.Unix;
import ch.systemsx.cisd.base.unix.Unix.Stat;
import ch.systemsx.cisd.common.io.MonitoredIOStreamCopier;
import ch.systemsx.cisd.common.logging.ConsoleLogger;
import ch.systemsx.cisd.common.shared.basic.string.StringUtils;

/**
 * Simple interface to the Apache commons tar classes for archiving directories, files and memory objects into tar files.
 * 
 * @author Bernd Rinn
 */
public class Tar implements Closeable
{

    private final static int DEFAULT_BUFFER_SIZE = 128 * 1024;

    private final static Map<Integer, String> userMap = new HashMap<Integer, String>();

    private final static Map<Integer, String> groupMap = new HashMap<Integer, String>();

    private final TarArchiveOutputStream out;

    private final MonitoredIOStreamCopier copier;

    public Tar(final File tarFile) throws FileNotFoundException
    {
        this(tarFile, DEFAULT_BUFFER_SIZE);
    }

    public Tar(final File tarFile, final int bufferSize) throws FileNotFoundException
    {
        this(tarFile, new MonitoredIOStreamCopier(bufferSize));
    }

    public Tar(final File tarFile, MonitoredIOStreamCopier copier) throws FileNotFoundException
    {
        this.out = new TarArchiveOutputStream(new FileOutputStream(tarFile));
        out.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        out.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
        out.setAddPaxHeadersForNonAsciiNames(true);
        this.copier = copier;
    }

    /**
     * Add a file or directory to the tar file.
     * 
     * @param file The file or directory to add to the tar file.
     * @param stripFromName The number of characters to strip from the start of each name when creating the archive entry names from the file paths.
     */
    public void add(final File file, final int stripFromName)
            throws IOException
    {
        if (file.isDirectory())
        {
            addDirectory(stripFromName, file);
        } else
        {
            addFile(stripFromName, file);
        }
    }

    public void add(String name) throws IOException
    {
        final TarArchiveEntry entry = new TarArchiveEntry(name);
        if (Unix.isOperational())
        {
            entry.setUserId(Unix.getUid());
            entry.setGroupId(Unix.getGid());
        }
        add(entry, (InputStream) null);
    }

    /**
     * Add a memory object to the tar file.
     * 
     * @param name The name of the archive entry.
     * @param data The data to write to the archive.
     */
    public void add(String name, byte[] data) throws IOException
    {
        final TarArchiveEntry entry = new TarArchiveEntry(name);
        if (Unix.isOperational())
        {
            entry.setUserId(Unix.getUid());
            entry.setGroupId(Unix.getGid());
        }
        add(entry, data);
    }

    /**
     * Add a memory object to the tar file.
     * 
     * @param entry The metadata of the archive entry.
     * @param data The data to write to the archive.
     */
    public void add(TarArchiveEntry entry, byte[] data) throws IOException
    {
        entry.setSize(data.length);
        add(entry, new ByteArrayInputStream(data));
    }

    /**
     * Add a stream content to the tar file.
     * 
     * @param entry The metadata of the archive entry.
     * @param input The stream to write to the archive.
     */
    public void add(TarArchiveEntry entry, InputStream input) throws IOException
    {
        if (StringUtils.isBlank(entry.getUserName()))
        {
            String username = tryGetUserName(entry.getUserId());
            if (username == null)
            {
                username = System.getProperty("user.name", "");
            }
            entry.setUserName(username);
        }
        if (StringUtils.isBlank(entry.getGroupName()))
        {
            String groupname = tryGetGroupName(entry.getGroupId());
            if (groupname == null)
            {
                groupname = "";
            }
            entry.setGroupName(groupname);
        }
        out.putArchiveEntry(entry);
        try
        {
            if (input != null)
            {
                copier.copy(input, out);
            }
        } finally
        {
            out.closeArchiveEntry();
        }
    }

    @Override
    public void close() throws IOException
    {
        copier.close();
        out.close();
    }

    private void addDirectory(int rootDirectoryLength, File directory) throws IOException
    {
        final String relativeDirName = directory.getPath().substring(
                rootDirectoryLength);
        if (relativeDirName.isEmpty() == false)
        {
            out.putArchiveEntry(createArchiveEntry(directory,
                    rootDirectoryLength, true));
            out.closeArchiveEntry();
        }
        final File[] files = directory.listFiles();
        for (File f : files)
        {
            if (f.isDirectory())
            {
                addDirectory(rootDirectoryLength, f);
            } else
            {
                addFile(rootDirectoryLength, f);
            }
        }
    }

    private void addFile(int rootDirectoryLength, File f) throws IOException, FileNotFoundException
    {
        out.putArchiveEntry(createArchiveEntry(f, rootDirectoryLength, false));
        final InputStream in = new FileInputStream(f);
        try
        {
            copier.copy(in, out);
        } finally
        {
            out.closeArchiveEntry();
            in.close();
        }
    }

    private String tryGetGroupName(int gid)
    {
        final String cached = groupMap.get(gid);
        if (cached == null)
        {
            final String name = Unix.tryGetGroupNameForGid(gid);
            if (name != null)
            {
                groupMap.put(gid, name);
            }
            return name;
        } else
        {
            return cached;
        }
    }

    private String tryGetUserName(int uid)
    {
        final String cached = userMap.get(uid);
        if (cached == null)
        {
            final String name = Unix.tryGetUserNameForUid(uid);
            if (name != null)
            {
                userMap.put(uid, name);
            }
            return name;
        } else
        {
            return cached;
        }
    }

    private TarArchiveEntry createArchiveEntry(final File file,
            final int rootDirectoryLength, final boolean addSlash)
    {
        final Stat stat;
        if (Unix.isOperational())
        {
            stat = Unix.tryGetLinkInfo(file.getPath());
        } else
        {
            stat = null;
        }
        String relativeName = file.getPath().substring(
                rootDirectoryLength + 1);
        if (addSlash)
        {
            relativeName += "/";
        }
        if (stat == null)
        {
            return new TarArchiveEntry(file, relativeName);
        } else
        {
            final TarArchiveEntry e = createArchiveEntry(relativeName, stat);
            return e;
        }
    }

    private TarArchiveEntry createArchiveEntry(final String name, final Stat stat)
    {
        final byte linkType =
                stat.getLinkType() == FileLinkType.SYMLINK ? TarArchiveEntry.LF_SYMLINK
                        : stat.getLinkType() == FileLinkType.DIRECTORY ? TarArchiveEntry.LF_DIR
                                : TarArchiveEntry.LF_NORMAL;
        final TarArchiveEntry e = new TarArchiveEntry(name, linkType, false);
        e.setModTime(stat.getLastModified() * TarArchiveEntry.MILLIS_PER_SECOND);
        e.setMode(stat.getPermissions());
        e.setUserId(stat.getUid());
        final String username = tryGetUserName(stat.getUid());
        if (username != null)
        {
            e.setUserName(username);
        }
        final String groupname = tryGetGroupName(stat.getGid());
        if (groupname != null)
        {
            e.setGroupName(groupname);
        }
        e.setGroupId(stat.getGid());
        if (linkType == TarArchiveEntry.LF_SYMLINK)
        {
            e.setLinkName(stat.tryGetSymbolicLink());
            e.setSize(0);
        } else if (linkType == TarArchiveEntry.LF_DIR)
        {
            e.setSize(0);
        } else
        {
            e.setSize(stat.getSize());
        }
        return e;
    }

    public static void main(String[] args) throws IOException
    {
        if (args.length < 2)
        {
            System.err.println("Tar [-p] <tarfile> <directory>");
            System.exit(1);
        }
        int tarFileIndex = 0;
        int dirIndex = 1;
        boolean parallel = args[0].equals("-p");
        if (parallel)
        {
            tarFileIndex++;
            dirIndex++;
        }
        final File tarFile = new File(args[tarFileIndex]);
        final File directory = new File(args[dirIndex]);
        Tar tar = null;
        try
        {
            Long maxQueueSize = parallel ? 5 * FileUtils.ONE_MB : null;
            MonitoredIOStreamCopier copier = new MonitoredIOStreamCopier((int) FileUtils.ONE_MB, maxQueueSize);
            copier.setLogger(new ConsoleLogger());
            tar = new Tar(tarFile, copier);
            tar.add(directory, directory.getPath().length());
        } finally
        {
            if (tar != null)
            {
                tar.close();
            }
        }
    }
}