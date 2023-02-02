/*
 * Copyright 2013 ETH Zuerich, CISD
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.common.hdf5.IHDF5ContainerReader;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

import de.schlichtherle.io.rof.SimpleReadOnlyFile;
import de.schlichtherle.util.zip.BasicZipFile;
import de.schlichtherle.util.zip.ZipEntry;

/**
 * {@link IHierarchicalContent} implementation for ZIP files.
 *
 * @author Franz-Josef Elmer
 */
public class ZipBasedHierarchicalContent extends AbstractHierarchicalContent
{
    static final String TEMP_FILE_PREFIX = "dss-unzipped-";

    static final File TEMP_FOLDER = new File(System.getProperty("java.io.tmpdir"));

    private static final String extractName(String relativePath)
    {
        int indexOfLastDelimiter = relativePath.lastIndexOf('/');
        if (indexOfLastDelimiter < 0)
        {
            return relativePath;
        }
        return relativePath.substring(indexOfLastDelimiter + 1);
    }

    private static final class ZipContainerNode extends AbstractHierarchicalDirectoryContentNode
    {
        private final String relativePathFromZipEntry;

        private final String name;

        private final List<IHierarchicalContentNode> children = new ArrayList<IHierarchicalContentNode>();

        ZipContainerNode(String relativePath)
        {
            this.relativePathFromZipEntry = relativePath;
            name = extractName(relativePath);
        }

        @Override
        public String getName()
        {
            return name;
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
            return 0;
        }

        @Override
        public File getFile() throws UnsupportedOperationException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public File tryGetFile()
        {
            return null;
        }

        @Override
        public boolean isChecksumCRC32Precalculated()
        {
            return true;
        }

        @Override
        protected String doGetRelativePath()
        {
            return relativePathFromZipEntry;
        }

        @Override
        protected List<IHierarchicalContentNode> doGetChildNodes()
        {
            return new ArrayList<IHierarchicalContentNode>(children);
        }
    }

    private static final class ZipContentNode extends AbstractHierarchicalFileContentNode
    {
        private final BasicZipFile zipFile;

        private final ZipEntry zipEntry;

        private final String relativePathFromZipEntry;

        private final String name;

        ZipContentNode(BasicZipFile zipFile, ZipEntry zipEntry)
        {
            this.zipFile = zipFile;
            this.zipEntry = zipEntry;
            relativePathFromZipEntry = zipEntry.getName();
            name = extractName(relativePathFromZipEntry);
        }

        @Override
        public String getName()
        {
            return name;
        }

        @Override
        public boolean exists()
        {
            return true;
        }

        @Override
        public boolean isDirectory()
        {
            return false;
        }

        @Override
        public long getLastModified()
        {
            return zipEntry.getTime();
        }

        @Override
        public File getFile() throws UnsupportedOperationException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public File tryGetFile()
        {
            return null;
        }

        @Override
        protected String doGetRelativePath()
        {
            return relativePathFromZipEntry;
        }

        @Override
        protected long doGetFileLength()
        {
            return zipEntry.getSize();
        }

        @Override
        public boolean isChecksumCRC32Precalculated()
        {
            return true;
        }

        @Override
        protected int doGetChecksumCRC32()
        {
            return (int) zipEntry.getCrc();
        }

        @Override
        protected IRandomAccessFile doGetFileContent()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        protected InputStream doGetInputStream()
        {
            try
            {
                return zipFile.getInputStream(zipEntry);
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
    }

    private static final class HDF5ContainerNode extends HDF5ContainerBasedHierarchicalContentNode
    {
        private final BasicZipFile zipFile;

        private final String relativePath;

        private final ZipEntry zipEntry;

        private boolean loaded;

        private HDF5ContainerNode(IHierarchicalContent root, File hdf5ContainerFile, BasicZipFile zipFile,
                String relativePath, ZipEntry zipEntry)
        {
            super(root, hdf5ContainerFile);
            this.zipFile = zipFile;
            this.relativePath = relativePath;
            this.zipEntry = zipEntry;
        }

        private synchronized void lazyLoad()
        {
            if (loaded == false)
            {
                copyZipEntryToFile(file);
                loaded = true;
            }
        }

        private void copyZipEntryToFile(File tempFile)
        {
            InputStream in = null;
            FileOutputStream out = null;
            BufferedOutputStream bufferedOut = null;
            try
            {
                in = zipFile.getInputStream(zipEntry);
                out = new FileOutputStream(tempFile);
                bufferedOut = new BufferedOutputStream(out);
                IOUtils.copy(in, bufferedOut);
            } catch (Exception ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            } finally
            {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(bufferedOut);
            }
        }

        @Override
        public String doGetRelativePath()
        {
            return relativePath;
        }

        @Override
        public String getName()
        {
            return extractName(relativePath);
        }

        @Override
        public InputStream doGetInputStream()
        {
            lazyLoad();
            return super.doGetInputStream();
        }

        @Override
        protected IHDF5ContainerReader createReader()
        {
            lazyLoad();
            return super.createReader();
        }

        @Override
        public long getLastModified()
        {
            return zipEntry.getTime();
        }

        @Override
        public long doGetFileLength()
        {
            return zipEntry.getSize();
        }

        @Override
        protected int doGetChecksumCRC32()
        {
            return (int) zipEntry.getCrc();
        }

        @Override
        public File getFile()
        {
            lazyLoad();
            return super.getFile();
        }

        @Override
        public boolean isChecksumCRC32Precalculated()
        {
            return true;
        }

        @Override
        public File tryGetFile()
        {
            lazyLoad();
            return super.tryGetFile();
        }

        @Override
        public IRandomAccessFile doGetFileContent()
        {
            lazyLoad();
            return super.doGetFileContent();
        }
    }

    private static final class NodeManager
    {
        private final Map<String, ZipContainerNode> containerNodes = new HashMap<String, ZipContainerNode>();

        private final Map<String, IHierarchicalContentNode> allNodes = new HashMap<String, IHierarchicalContentNode>();

        private final List<File> unzippedFiles = new ArrayList<File>();

        private ZipContainerNode rootNode;

        private final IHierarchicalContent hierarchicalContent;

        private boolean h5Folders;

        private boolean h5arFolders;

        public NodeManager(IHierarchicalContent hierarchicalContent, List<H5FolderFlags> h5FolderFlags)
        {
            this.hierarchicalContent = hierarchicalContent;
            if (h5FolderFlags != null && h5FolderFlags.isEmpty() == false)
            {
                h5Folders = h5FolderFlags.get(0).isH5Folders();
                h5arFolders = h5FolderFlags.get(0).isH5arFolders();
            }
        }

        void handle(final BasicZipFile zipFile, final ZipEntry zipEntry)
        {
            final String relativePath = zipEntry.getName();
            if (zipEntry.isDirectory())
            {
                String name = zipEntry.getName();
                if (name.endsWith("/"))
                {
                    name = name.substring(0, name.length() - 1);
                }
                IHierarchicalContentNode contentNode = new ZipContainerNode(name);
                linkNode(contentNode, relativePath);
            } else
            {
                if (HierarchicalContentUtils.handleHdf5AsFolder(relativePath, h5Folders, h5arFolders))
                {
                    try
                    {
                        File tempFile = File.createTempFile(TEMP_FILE_PREFIX, extractName(relativePath), TEMP_FOLDER);
                        unzippedFiles.add(tempFile);
                        IHierarchicalContentNode contentNode =
                                new HDF5ContainerNode(hierarchicalContent, tempFile, zipFile, relativePath, zipEntry);
                        linkNode(contentNode, relativePath);
                    } catch (Exception ex)
                    {
                        throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                    }
                } else
                {
                    IHierarchicalContentNode contentNode = new ZipContentNode(zipFile, zipEntry);
                    linkNode(contentNode, relativePath);
                }
            }
        }

        private void linkNode(IHierarchicalContentNode contentNode, String relativePath)
        {
            allNodes.put(relativePath, contentNode);
            String parentRelativePath = FileUtilities.getParentRelativePath(relativePath);
            if (parentRelativePath != null)
            {
                ZipContainerNode containerNode = containerNodes.get(parentRelativePath);
                if (containerNode == null)
                {
                    containerNode = new ZipContainerNode(parentRelativePath);
                    containerNodes.put(parentRelativePath, containerNode);
                    if (parentRelativePath.length() == 0)
                    {
                        rootNode = containerNode;
                    } else
                    {
                        linkNode(containerNode, parentRelativePath);
                    }
                }
                containerNode.children.add(contentNode);
            }
        }
    }

    private final BasicZipFile zipFile;

    private final IHierarchicalContentNode rootNode;

    private final Map<String, IHierarchicalContentNode> allNodes;

    private final List<File> unzippedFiles;

    public ZipBasedHierarchicalContent(File file, List<H5FolderFlags> h5FolderFlags)
    {
        try
        {
            NodeManager nodeManager = new NodeManager(this, h5FolderFlags);
            zipFile = new BasicZipFile(new SimpleReadOnlyFile(file), "UTF-8", true, false);
            @SuppressWarnings("unchecked")
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements())
            {
                ZipEntry entry = entries.nextElement();
                nodeManager.handle(zipFile, entry);
            }
            rootNode = nodeManager.rootNode;
            allNodes = nodeManager.allNodes;
            unzippedFiles = nodeManager.unzippedFiles;
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public IHierarchicalContentNode getRootNode()
    {
        return rootNode;
    }

    @Override
    public IHierarchicalContentNode getNode(String relativePath) throws IllegalArgumentException
    {
        IHierarchicalContentNode node = tryGetNode(relativePath);
        if (node == null)
        {
            throw new IllegalArgumentException("Resource '" + relativePath + "' does not exist.");
        }
        return node;
    }

    @Override
    public IHierarchicalContentNode tryGetNode(String relativePath)
    {
        return allNodes.get(relativePath);
    }

    @Override
    public void close()
    {
        try
        {
            zipFile.close();
            for (File unzippedFile : unzippedFiles)
            {
                FileUtilities.delete(unzippedFile);
            }
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

}
