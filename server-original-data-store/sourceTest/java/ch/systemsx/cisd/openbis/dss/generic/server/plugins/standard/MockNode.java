/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

final class MockNode implements IHierarchicalContentNode
{
    private final List<IHierarchicalContentNode> children =
            new ArrayList<IHierarchicalContentNode>();

    String name;

    String relativePath;

    private IHierarchicalContentNode parent;

    boolean directory;

    long size;

    int crc32Checksum;

    String checksum;

    String content;

    void addNode(MockNode node)
    {
        node.parent = this;
        children.add(node);
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getRelativePath()
    {
        return relativePath;
    }

    @Override
    public String getParentRelativePath()
    {
        return parent == null ? null : parent.getRelativePath();
    }

    @Override
    public boolean exists()
    {
        return true;
    }

    @Override
    public boolean isDirectory()
    {
        return directory;
    }

    @Override
    public long getLastModified()
    {
        return 0;
    }

    @Override
    public List<IHierarchicalContentNode> getChildNodes() throws UnsupportedOperationException
    {
        return children;
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
    public long getFileLength() throws UnsupportedOperationException
    {
        return size;
    }

    @Override
    public String getChecksum() throws UnsupportedOperationException
    {
        return checksum;
    }

    @Override
    public int getChecksumCRC32() throws UnsupportedOperationException
    {
        return crc32Checksum;
    }

    @Override
    public boolean isChecksumCRC32Precalculated()
    {
        return true;
    }

    @Override
    public IRandomAccessFile getFileContent() throws UnsupportedOperationException,
            IOExceptionUnchecked
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getInputStream() throws UnsupportedOperationException, IOExceptionUnchecked
    {
        if (content != null)
        {
            return new ByteArrayInputStream(content.getBytes());
        }
        throw new UnsupportedOperationException();
    }

}