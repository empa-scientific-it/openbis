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

import java.io.InputStream;
import java.util.List;

import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

/**
 * Abstract {@link IHierarchicalContent} implementation with checks before calling methods specific to directories or non-directories.
 * 
 * @author Piotr Buczek
 */
public abstract class AbstractHierarchicalContentNode implements IHierarchicalContentNode
{
    static final String OPERATION_NOT_SUPPORTED_FOR_A_DIRECTORY =
            "Operation not supported for a directory: ";

    static final String OPERATION_SUPPORTED_ONLY_FOR_A_DIRECTORY =
            "Operation supported only for a directory: ";

    /** Returns relative path of this node or <code>null</code> for root node. */
    abstract protected String doGetRelativePath();

    /** Returns list of child nodes of a node known to be a directory. */
    abstract protected List<IHierarchicalContentNode> doGetChildNodes();

    /** Returns size of a node known NOT to be a directory. */
    abstract protected long doGetFileLength();

    /** Returns checksum of a node known NOT to be a directory. */
    abstract protected int doGetChecksumCRC32();

    /** Returns {@link IRandomAccessFile} of a node known NOT to be a directory. */
    abstract protected IRandomAccessFile doGetFileContent();

    /** Returns {@link InputStream} of a node known NOT to be a directory. */
    abstract protected InputStream doGetInputStream();

    private String relativePath; // lazily initialized and cached

    @Override
    public final String getRelativePath()
    {
        if (relativePath == null)
        {
            relativePath = doGetRelativePath();
        }
        return relativePath;
    }

    private final void requireDirectory()
    {
        if (isDirectory() == false)
        {
            throw new UnsupportedOperationException(OPERATION_SUPPORTED_ONLY_FOR_A_DIRECTORY + getRelativePath());
        }
    }

    protected boolean isPhysicalFile()
    {
        return isDirectory() == false;
    }

    private final void requirePhysicalFile()
    {
        if (isDirectory() && isPhysicalFile() == false)
        {
            throw new UnsupportedOperationException(OPERATION_NOT_SUPPORTED_FOR_A_DIRECTORY + getRelativePath());
        }
    }

    private final void requireFile()
    {
        if (isDirectory() && isPhysicalFile() == false)
        {
            throw new UnsupportedOperationException(OPERATION_NOT_SUPPORTED_FOR_A_DIRECTORY + getRelativePath());
        }
    }
    
    @Override
    public final List<IHierarchicalContentNode> getChildNodes()
    {
        requireDirectory();
        return doGetChildNodes();
    }

    @Override
    public final long getFileLength() throws UnsupportedOperationException
    {
        return doGetFileLength();
    }

    @Override
    public final int getChecksumCRC32() throws UnsupportedOperationException
    {
        requirePhysicalFile();
        return doGetChecksumCRC32();
    }

    @Override
    public final String getChecksum() throws UnsupportedOperationException
    {
        requireFile();
        return doGetChecksum();
    }
    
    protected String doGetChecksum()
    {
        return null;
    }
    
    @Override
    public final IRandomAccessFile getFileContent()
    {
        requirePhysicalFile();
        return doGetFileContent();
    }

    @Override
    public final InputStream getInputStream()
    {
        requirePhysicalFile();
        return doGetInputStream();
    }

    @Override
    public final String getParentRelativePath()
    {
        return FileUtilities.getParentRelativePath(getRelativePath());
    }
}
