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

package ch.systemsx.cisd.openbis.common.io;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

/**
 * Content based on a {@link IHierarchicalContentNode}.
 * 
 * @author Franz-Josef Elmer
 */
public class HierarchicalContentNodeBasedHierarchicalContentNode implements
        IHierarchicalContentNode
{
    private IHierarchicalContentNode content;

    private static final IHierarchicalContentNode DUMMY = new IHierarchicalContentNode()
        {
            @Override
            public boolean isDirectory()
            {
                return false;
            }

            @Override
            public String getRelativePath()
            {
                return null;
            }

            @Override
            public String getParentRelativePath()
            {
                return null;
            }

            @Override
            public String getName()
            {
                return null;
            }

            @Override
            public long getLastModified()
            {
                return 0;
            }

            @Override
            public InputStream getInputStream() throws UnsupportedOperationException,
                    IOExceptionUnchecked
            {
                return null;
            }

            @Override
            public long getFileLength() throws UnsupportedOperationException
            {
                return 0;
            }

            @Override
            public String getChecksum() throws UnsupportedOperationException
            {
                return "";
            }

            @Override
            public int getChecksumCRC32() throws UnsupportedOperationException
            {
                return 0;
            }

            @Override
            public IRandomAccessFile getFileContent() throws UnsupportedOperationException,
                    IOExceptionUnchecked
            {
                return null;
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
            public List<IHierarchicalContentNode> getChildNodes()
                    throws UnsupportedOperationException
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean exists()
            {
                return false;
            }

            @Override
            public boolean isChecksumCRC32Precalculated()
            {
                return false;
            }
        };

    /**
     * Creates an instance for specified content provider.
     */
    public HierarchicalContentNodeBasedHierarchicalContentNode(IHierarchicalContentNode content)
    {
        this.content = content;
    }

    @Override
    public boolean exists()
    {
        return getContent().exists();
    }

    @Override
    public InputStream getInputStream()
    {
        return getContent().getInputStream();
    }

    private IHierarchicalContentNode getContent()
    {
        if (content == null)
        {
            content = DUMMY;
        }
        return content;
    }

    @Override
    public String getName()
    {
        return getContent().getName();
    }

    @Override
    public String getRelativePath()
    {
        return getContent().getRelativePath();
    }

    @Override
    public String getParentRelativePath()
    {
        return getContent().getParentRelativePath();
    }

    @Override
    public boolean isDirectory()
    {
        return getContent().isDirectory();
    }

    @Override
    public long getLastModified()
    {
        return getContent().getLastModified();
    }

    @Override
    public List<IHierarchicalContentNode> getChildNodes() throws UnsupportedOperationException
    {
        return getContent().getChildNodes();
    }

    @Override
    public File getFile() throws UnsupportedOperationException
    {
        return getContent().getFile();
    }

    @Override
    public File tryGetFile()
    {
        return getContent().tryGetFile();
    }

    @Override
    public long getFileLength() throws UnsupportedOperationException
    {
        return getContent().getFileLength();
    }

    @Override
    public String getChecksum() throws UnsupportedOperationException
    {
        return getContent().getChecksum();
    }

    @Override
    public int getChecksumCRC32() throws UnsupportedOperationException
    {
        return getContent().getChecksumCRC32();
    }

    @Override
    public boolean isChecksumCRC32Precalculated()
    {
        return getContent().isChecksumCRC32Precalculated();
    }

    @Override
    public IRandomAccessFile getFileContent() throws UnsupportedOperationException,
            IOExceptionUnchecked
    {
        return getContent().getFileContent();
    }

}
