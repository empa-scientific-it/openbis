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

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

/**
 * @author Franz-Josef Elmer
 */
public class FilteredHierarchicalContent implements IHierarchicalContent
{
    private final IHierarchicalContent content;

    private final IHierarchicalContentNodeFilter filter;

    public FilteredHierarchicalContent(IHierarchicalContent content, IHierarchicalContentNodeFilter filter)
    {
        this.content = content;
        this.filter = filter;
    }

    @Override
    public IHierarchicalContentNode getRootNode()
    {
        return wrap(content.getRootNode());
    }

    @Override
    public IHierarchicalContentNode getNode(String relativePath) throws IllegalArgumentException
    {
        return wrap(content.getNode(relativePath));
    }

    @Override
    public IHierarchicalContentNode tryGetNode(String relativePath)
    {
        return wrap(content.tryGetNode(relativePath));
    }

    @Override
    public List<IHierarchicalContentNode> listMatchingNodes(String relativePathPattern)
    {
        return wrap(content.listMatchingNodes(relativePathPattern));
    }

    @Override
    public List<IHierarchicalContentNode> listMatchingNodes(String startingPath, String fileNamePattern)
    {
        return wrap(content.listMatchingNodes(startingPath, fileNamePattern));
    }

    @Override
    public void close()
    {
        content.close();
    }

    private List<IHierarchicalContentNode> wrap(List<IHierarchicalContentNode> nodes)
    {
        List<IHierarchicalContentNode> wrappedNodes = new ArrayList<IHierarchicalContentNode>(nodes.size());
        for (IHierarchicalContentNode node : nodes)
        {
            if (filter.accept(node))
            {
                wrappedNodes.add(wrap(node));
            }
        }
        return wrappedNodes;
    }

    private IHierarchicalContentNode wrap(IHierarchicalContentNode node)
    {
        return node == null ? null : new FilteredHierarchicalContentNode(node);
    }

    private final class FilteredHierarchicalContentNode implements IHierarchicalContentNode
    {
        private final IHierarchicalContentNode node;

        FilteredHierarchicalContentNode(IHierarchicalContentNode node)
        {
            this.node = node;
        }

        @Override
        public String getName()
        {
            return node.getName();
        }

        @Override
        public String getRelativePath()
        {
            return node.getRelativePath();
        }

        @Override
        public String getParentRelativePath()
        {
            return node.getParentRelativePath();
        }

        @Override
        public boolean exists()
        {
            return node.exists();
        }

        @Override
        public boolean isDirectory()
        {
            return node.isDirectory();
        }

        @Override
        public long getLastModified()
        {
            return node.getLastModified();
        }

        @Override
        public List<IHierarchicalContentNode> getChildNodes() throws UnsupportedOperationException
        {
            return wrap(node.getChildNodes());
        }

        @Override
        public File getFile() throws UnsupportedOperationException
        {
            return node.getFile();
        }

        @Override
        public File tryGetFile()
        {
            return node.tryGetFile();
        }

        @Override
        public long getFileLength() throws UnsupportedOperationException
        {
            return node.getFileLength();
        }

        @Override
        public String getChecksum() throws UnsupportedOperationException
        {
            return node.getChecksum();
        }

        @Override
        public int getChecksumCRC32() throws UnsupportedOperationException
        {
            return node.getChecksumCRC32();
        }

        @Override
        public boolean isChecksumCRC32Precalculated()
        {
            return node.isChecksumCRC32Precalculated();
        }

        @Override
        public IRandomAccessFile getFileContent() throws UnsupportedOperationException, IOExceptionUnchecked
        {
            return node.getFileContent();
        }

        @Override
        public InputStream getInputStream() throws UnsupportedOperationException, IOExceptionUnchecked
        {
            return node.getInputStream();
        }
    }

}
