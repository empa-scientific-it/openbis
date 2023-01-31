/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.tar.Untar;
import ch.systemsx.cisd.common.io.MonitoredIOStreamCopier;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.hdf5.h5ar.ArchiveEntry;
import ch.systemsx.cisd.openbis.common.hdf5.HDF5Container;
import ch.systemsx.cisd.openbis.common.hdf5.IHDF5ContainerReader;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.H5FolderChecker;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.H5FolderFlags;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.HierarchicalContentUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.ISingleDataSetPathInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetPathInfo;

/**
 * An implementations of {@link ISingleDataSetPathInfoProvider} which retrieves and calculates path info from a tar file.
 *
 * @author Franz-Josef Elmer
 */
class TarBasedPathInfoProvider implements ISingleDataSetPathInfoProvider
{
    private final File packageFile;

    private final int bufferSize;

    private final ISimpleLogger ioSpeedLogger;

    private Map<String, DataSetPathInfo> pathInfos;

    private Map<String, List<DataSetPathInfo>> pathInfoChildren;

    private List<H5FolderFlags> h5FolderFlags;

    TarBasedPathInfoProvider(File packageFile, List<H5FolderFlags> h5FolderFlags, int bufferSize, ISimpleLogger ioSpeedLogger)
    {
        this.packageFile = packageFile;
        this.h5FolderFlags = h5FolderFlags;
        this.bufferSize = bufferSize;
        this.ioSpeedLogger = ioSpeedLogger;
    }

    @Override
    public DataSetPathInfo getRootPathInfo()
    {
        return tryGetPathInfoByRelativePath(Collections.min(getPathInfoChildren().keySet()));
    }

    @Override
    public DataSetPathInfo tryGetPathInfoByRelativePath(String relativePath)
    {
        return getPathInfos().get(relativePath);
    }

    @Override
    public List<DataSetPathInfo> listChildrenPathInfos(DataSetPathInfo parent)
    {
        return getPathInfoChildren().get(parent.getRelativePath());
    }

    @Override
    public List<DataSetPathInfo> listMatchingPathInfos(String relativePathPattern)
    {
        List<DataSetPathInfo> result = new ArrayList<DataSetPathInfo>();
        Pattern pattern = Pattern.compile(relativePathPattern);
        for (DataSetPathInfo pathInfo : getPathInfos().values())
        {
            if (pattern.matcher(pathInfo.getRelativePath()).matches())
            {
                result.add(pathInfo);
            }
        }
        return result;
    }

    @Override
    public List<DataSetPathInfo> listMatchingPathInfos(String startingPath, String fileNamePattern)
    {
        List<DataSetPathInfo> result = new ArrayList<DataSetPathInfo>();
        Pattern pattern = Pattern.compile(fileNamePattern);
        for (DataSetPathInfo pathInfo : getPathInfos().values())
        {
            if (pathInfo.getRelativePath().startsWith(startingPath)
                    && pattern.matcher(pathInfo.getFileName()).matches())
            {
                result.add(pathInfo);
            }
        }
        return result;
    }

    private Map<String, List<DataSetPathInfo>> getPathInfoChildren()
    {
        getPathInfos(); // lazy creation of pathInfoChildren
        return pathInfoChildren;
    }

    private Map<String, DataSetPathInfo> getPathInfos()
    {
        if (pathInfos == null)
        {
            MonitoredIOStreamCopier copier = new MonitoredIOStreamCopier(bufferSize);
            copier.setLogger(ioSpeedLogger);
            UntarMetaData untar = null;
            try
            {
                pathInfos = new TreeMap<String, DataSetPathInfo>();
                untar = new UntarMetaData(packageFile, new H5FolderChecker(h5FolderFlags), copier, pathInfos);
                untar.extract((File) null);
                createPathInfoLinks();
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            } finally
            {
                if (untar != null)
                {
                    try
                    {
                        untar.close();
                    } catch (IOException e)
                    {
                        throw CheckedExceptionTunnel.wrapIfNecessary(e);
                    }
                }
            }
        }
        return pathInfos;
    }

    private void createPathInfoLinks()
    {
        pathInfoChildren = new HashMap<String, List<DataSetPathInfo>>();
        for (DataSetPathInfo pathInfo : pathInfos.values())
        {
            String relativePath = pathInfo.getRelativePath();
            String parentRelativePath = FileUtilities.getParentRelativePath(relativePath);
            if (StringUtils.isNotBlank(parentRelativePath))
            {
                DataSetPathInfo parentPathInfo = pathInfos.get(parentRelativePath);
                pathInfo.setParent(parentPathInfo);
                List<DataSetPathInfo> children = pathInfoChildren.get(parentRelativePath);
                if (children == null)
                {
                    children = new ArrayList<DataSetPathInfo>();
                    pathInfoChildren.put(parentRelativePath, children);
                }
                children.add(pathInfo);
            }
        }
    }

    private static final void addDirectory(String path, Map<String, DataSetPathInfo> pathInfos)
    {
        if (StringUtils.isNotBlank(path) && pathInfos.containsKey(path) == false)
        {
            DataSetPathInfo pathInfo = new DataSetPathInfo();
            pathInfo.setDirectory(true);
            pathInfo.setFileName(FileUtilities.getFileNameFromRelativePath(path));
            pathInfo.setRelativePath(path);
            pathInfos.put(path, pathInfo);
            addDirectory(FileUtilities.getParentRelativePath(path), pathInfos);
        }

    }

    private static final class UntarMetaData extends Untar
    {
        private Map<String, DataSetPathInfo> pathInfos;
        private H5FolderChecker folderChecker;

        public UntarMetaData(File tarFile, H5FolderChecker folderChecker,
                MonitoredIOStreamCopier copier, Map<String, DataSetPathInfo> pathInfos) throws FileNotFoundException
        {
            super(tarFile, copier);
            this.folderChecker = folderChecker;
            this.pathInfos = pathInfos;
        }

        @Override
        protected OutputStream createOutputStream(File entryFile, TarArchiveEntry entry)
                throws IOException
        {
            return new PathInfoOutputStream(entry, folderChecker, pathInfos);
        }

        @Override
        protected void createDirectory(File directory)
        {
            addDirectory(directory.getPath(), pathInfos);
        }

        @Override
        protected void createParentDirectories(File file)
        {
        }

        @Override
        protected void createSymbolicLink(File file, TarArchiveEntry entry)
        {
        }

        @Override
        protected void createHardLink(File file, TarArchiveEntry entry)
        {
        }

        @Override
        protected void setFileMetadata(File entryFile, TarArchiveEntry entry)
        {
        }
    }

    private static final class PathInfoOutputStream extends FilterOutputStream
    {
        private final TarArchiveEntry entry;

        private final Map<String, DataSetPathInfo> pathInfos;

        private final Checksum checksum;

        private long fileSize;


        private OutputStream fileOutputStream;

        private File tmpFile;

        public PathInfoOutputStream(TarArchiveEntry entry, H5FolderChecker folderChecker, 
                Map<String, DataSetPathInfo> pathInfos)
        {
            super(new NullOutputStream());
            this.entry = entry;
            this.pathInfos = pathInfos;
            checksum = new CRC32();
            String name = entry.getName();
            if (folderChecker.handleHdf5AsFolder(name))
            {
                try
                {
                    tmpFile = Files.createTempFile("openbis", "untarh5").toFile();
                    System.out.println("temp file: " + tmpFile.getAbsolutePath());
                    tmpFile.deleteOnExit();
                    fileOutputStream = new FileOutputStream(tmpFile);
                } catch (IOException ex)
                {
                    throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                }
            } else
            {
                fileOutputStream = new NullOutputStream();
            }
        }
        
        @Override
        protected void finalize() throws Throwable
        {
            fileOutputStream.close();
            if (tmpFile != null)
            {
                tmpFile.delete();
            }
        }

        @Override
        public void write(int b) throws IOException
        {
            out.write(b);
            fileOutputStream.write(b);
            checksum.update(b);
            fileSize++;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException
        {
            out.write(b, off, len);
            fileOutputStream.write(b, off, len);
            checksum.update(b, off, len);
            fileSize += len;
        }

        @Override
        public void close() throws IOException
        {
            super.close();
            fileOutputStream.close();
            String name = entry.getName();
            if (tmpFile == null)
            {
                addAsPathInfo(name);
            } else
            {
                IHDF5ContainerReader reader = new HDF5Container(tmpFile).createSimpleReader();
                try
                {
                    if (HierarchicalContentUtils.isFileAbstractionOk(reader, "/"))
                    {
                        List<ArchiveEntry> entries = reader.getGroupMembers("/");
                        for (ArchiveEntry archiveEntry : entries)
                        {
                            handleEntry(name, reader, archiveEntry);
                        }
                    } else
                    {
                        addAsPathInfo(name);
                    }
                } finally
                {
                    reader.close();
                }
            }
        }

        private void addAsPathInfo(String name)
        {
            DataSetPathInfo pathInfo = new DataSetPathInfo();
            pathInfo.setChecksumCRC32((int) checksum.getValue());
            pathInfo.setDirectory(false);
            pathInfo.setFileName(FileUtilities.getFileNameFromRelativePath(name));
            pathInfo.setLastModified(entry.getLastModifiedDate());
            pathInfo.setRelativePath(name);
            pathInfo.setSizeInBytes(fileSize);
            pathInfos.put(name, pathInfo);
            addDirectory(FileUtilities.getParentRelativePath(name), pathInfos);
        }
        
        private void handleEntry(String name, IHDF5ContainerReader reader, ArchiveEntry archiveEntry)
        {
            String path =  name + archiveEntry.getPath();
            DataSetPathInfo pathInfo = new DataSetPathInfo();
            pathInfo.setChecksumCRC32(archiveEntry.getCrc32());
            pathInfo.setDirectory(archiveEntry.isDirectory());
            pathInfo.setLastModified(new Date(archiveEntry.getLastModified()));
            pathInfo.setRelativePath(path);
            pathInfo.setFileName(archiveEntry.getName());
            pathInfo.setSizeInBytes(archiveEntry.getSize());
            pathInfos.put(path, pathInfo);
            addDirectory(FileUtilities.getParentRelativePath(path), pathInfos);
            if (archiveEntry.isDirectory())
            {
                List<ArchiveEntry> groupMembers = reader.getGroupMembers(archiveEntry.getPath());
                for (ArchiveEntry childEntry : groupMembers)
                {
                    handleEntry(name, reader, childEntry);
                }
            }
        }

    }
}
