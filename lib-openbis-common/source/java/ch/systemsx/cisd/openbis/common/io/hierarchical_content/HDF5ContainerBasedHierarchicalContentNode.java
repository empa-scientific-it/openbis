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

package ch.systemsx.cisd.openbis.common.io.hierarchical_content;

import java.io.Closeable;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.base.io.AdapterIInputStreamToInputStream;
import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.common.io.IOUtilities;
import ch.systemsx.cisd.hdf5.h5ar.ArchiveEntry;
import ch.systemsx.cisd.hdf5.io.HDF5DataSetRandomAccessFile;
import ch.systemsx.cisd.hdf5.io.HDF5IOAdapterFactory;
import ch.systemsx.cisd.openbis.common.hdf5.HDF5Container;
import ch.systemsx.cisd.openbis.common.hdf5.IHDF5ContainerReader;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

/**
 * {@link IHierarchicalContent} implementation for HDF5 container.
 * 
 * @author Piotr Buczek
 */
public class HDF5ContainerBasedHierarchicalContentNode extends
        DefaultFileBasedHierarchicalContentNode
{
    private final HDF5Container hdf5Container;

    public HDF5ContainerBasedHierarchicalContentNode(IHierarchicalContent root,
            File hdf5ContainerFile)
    {
        super(root, hdf5ContainerFile);
        this.hdf5Container = new HDF5Container(hdf5ContainerFile);
    }

    protected IHDF5ContainerReader createReader()
    {
        return hdf5Container.createSimpleReader();
    }

    //

    @Override
    public boolean isDirectory()
    {
        return true; // always a directory
    }

    @Override
    protected boolean isPhysicalFile()
    {
        return true;
    }

    @Override
    public List<IHierarchicalContentNode> doGetChildNodes()
    {
        IHDF5ContainerReader reader = createReader();
        try
        {
            final List<ArchiveEntry> entries = reader.getGroupMembers("/");
            final List<IHierarchicalContentNode> result = new ArrayList<IHierarchicalContentNode>();
            for (ArchiveEntry entry : entries)
            {
                result.add(getChildNode(entry));
            }
            return result;
        } finally
        {
            reader.close();
        }
    }

    /** @return is file abstraction valid for whole hdf archive */
    public boolean isFileAbstractionOk()
    {
        IHDF5ContainerReader reader = createReader();
        try
        {
            return HierarchicalContentUtils.isFileAbstractionOk(reader, "/");
        } finally
        {
            reader.close();
        }
    }

    /** @return child node with given path relative to this container */
    private IHierarchicalContentNode getChildNode(ArchiveEntry entry)
    {
        if (entry.isDirectory())
        {
            return new HDF5GroupNode(this, entry);
        } else
        {
            return new HDF5FileNode(this, entry);
        }
    }

    /**
     * @return child node with given path relative to this container, or null, if the path does not exist.
     */
    public IHierarchicalContentNode tryGetChildNode(String childPath)
    {
        final IHDF5ContainerReader reader = createReader();
        try
        {
            final ArchiveEntry childEntry = reader.tryGetEntry(childPath);
            if (childEntry == null)
            {
                return null;
            }
            if (childEntry.isDirectory())
            {
                return new HDF5GroupNode(this, childEntry);
            } else
            {
                return new HDF5FileNode(this, childEntry);
            }
        } finally
        {
            reader.close();
        }
    }

    //
    // Object
    //

    @Override
    public String toString()
    {
        return "HDF5ContainerBasedHierarchicalContentNode [root=" + root + ", container="
                + hdf5Container.getHDF5File() + "]";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + hdf5Container.getHDF5File().hashCode();
        result = prime * result + root.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (!(obj instanceof HDF5ContainerBasedHierarchicalContentNode))
        {
            return false;
        }
        HDF5ContainerBasedHierarchicalContentNode other =
                (HDF5ContainerBasedHierarchicalContentNode) obj;
        if (!hdf5Container.getHDF5File().equals(other.hdf5Container.getHDF5File()))
        {
            return false;
        }
        if (!root.equals(other.root))
        {
            return false;
        }
        return true;
    }

    class HDF5GroupNode extends AbstractHierarchicalDirectoryContentNode
    {
        private final ArchiveEntry entry;

        private final HDF5ContainerBasedHierarchicalContentNode containerNode;

        public HDF5GroupNode(HDF5ContainerBasedHierarchicalContentNode containerNode,
                ArchiveEntry entry)
        {
            this.containerNode = containerNode;
            this.entry = entry;
        }

        @Override
        public String getName()
        {
            return entry.getName();
        }

        @Override
        public String doGetRelativePath()
        {
            return containerNode.getRelativePath() + entry.getPath();
        }

        @Override
        public boolean exists()
        {
            return true;
        }

        @Override
        public boolean isDirectory()
        {
            return true;
        }

        @Override
        public long getLastModified()
        {
            return entry.getLastModified() < 0 ? file.lastModified()
                    : entry.getLastModified() * 1000;
        }

        @Override
        public File getFile() throws UnsupportedOperationException
        {
            throw new UnsupportedOperationException("This is not a normal directory node.");
        }

        @Override
        public boolean isChecksumCRC32Precalculated()
        {
            return false;
        }

        @Override
        public File tryGetFile()
        {
            return null;
        }

        @Override
        protected List<IHierarchicalContentNode> doGetChildNodes()
        {
            IHDF5ContainerReader reader = createReader();
            try
            {
                final List<IHierarchicalContentNode> result =
                        new ArrayList<IHierarchicalContentNode>();
                final List<ArchiveEntry> children = reader.getGroupMembers(entry.getPath());
                for (ArchiveEntry childEntry : children)
                {
                    if (childEntry.isDirectory())
                    {
                        result.add(new HDF5GroupNode(containerNode, childEntry));
                    } else
                    {
                        result.add(new HDF5FileNode(containerNode, childEntry));
                    }
                }
                return result;
            } finally
            {
                reader.close();
            }
        }

        @Override
        public String toString()
        {
            return "HDF5GroupNode [containerPath=" + containerNode.getRelativePath()
                    + ", relativePath=" + entry.getPath() + ", groupName=" + entry.getName() + "]";
        }

    }

    class HDF5FileNode extends AbstractHierarchicalFileContentNode
    {
        private final ArchiveEntry entry;

        private HDF5DataSetBasedContent contentOrNull;

        private final HDF5ContainerBasedHierarchicalContentNode containerNode;

        private Integer checksum;

        public HDF5FileNode(HDF5ContainerBasedHierarchicalContentNode containerNode,
                ArchiveEntry entry)
        {
            this.containerNode = containerNode;
            this.entry = entry;
        }

        @Override
        public String getName()
        {
            return entry.getName();
        }

        @Override
        public String doGetRelativePath()
        {
            return containerNode.getRelativePath() + entry.getPath();
        }

        @Override
        public boolean exists()
        {
            return entry.isRegularFile();
        }

        @Override
        public boolean isDirectory()
        {
            return false;
        }

        @Override
        public long getLastModified()
        {
            return entry.getLastModified() < 0 ? file.lastModified()
                    : entry.getLastModified() * 1000;
        }

        @Override
        public File getFile() throws UnsupportedOperationException
        {
            throw new UnsupportedOperationException("This is not a normal file node.");
        }

        @Override
        public File tryGetFile()
        {
            return null;
        }

        @Override
        protected long doGetFileLength()
        {
            return entry.getSize();
        }

        @Override
        protected int doGetChecksumCRC32()
        {
            if (checksum != null)
            {
                return checksum;
            }
            if (isChecksumCRC32Precalculated())
            {
                checksum = entry.getCrc32();
            } else
            {
                checksum = IOUtilities.getChecksumCRC32(doGetInputStream());
            }
            return checksum;
        }

        @Override
        public boolean isChecksumCRC32Precalculated()
        {
            return entry.hasChecksum();
        }

        @Override
        protected IRandomAccessFile doGetFileContent()
        {
            return getContent().getReadOnlyRandomAccessFile();
        }

        @Override
        protected InputStream doGetInputStream()
        {
            return getContent().getInputStream();
        }

        private HDF5DataSetBasedContent getContent()
        {
            if (contentOrNull == null)
            {
                contentOrNull = extractFileContent(file, entry.getPath());
            }
            return contentOrNull;
        }

        @Override
        public String toString()
        {
            return "HDF5FileNode [containerPath=" + containerNode.getRelativePath()
                    + ", relativePath=" + entry.getPath() + ", fileName=" + entry.getName() + "]";
        }

    }

    private static HDF5DataSetBasedContent extractFileContent(File hdf5File, String path)
    {
        return new HDF5DataSetBasedContent(hdf5File, path);
    }

    public static class HDF5DataSetBasedContent implements IFileContentProvider, Closeable
    {
        private final File hdf5File;

        private final String path;

        private final List<HDF5DataSetRandomAccessFile> randomAccessFiles;

        public HDF5DataSetBasedContent(File hdf5File, String path)
        {
            this.hdf5File = hdf5File;
            this.path = path;
            this.randomAccessFiles = new ArrayList<HDF5DataSetRandomAccessFile>();
        }

        @Override
        public IRandomAccessFile getReadOnlyRandomAccessFile()
        {
            final HDF5DataSetRandomAccessFile randomAccessFile =
                    HDF5IOAdapterFactory.asRandomAccessFileReadOnly(hdf5File, path);
            randomAccessFiles.add(randomAccessFile);
            return randomAccessFile;
        }

        @Override
        public InputStream getInputStream()
        {
            return new AdapterIInputStreamToInputStream(getReadOnlyRandomAccessFile());
        }

        @Override
        public void close()
        {
            for (HDF5DataSetRandomAccessFile raFile : randomAccessFiles)
            {
                raFile.close();
            }
        }
    }
}
