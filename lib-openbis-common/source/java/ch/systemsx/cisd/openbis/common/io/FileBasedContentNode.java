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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.FileUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.base.io.RandomAccessFileImpl;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

/**
 * File content. Wraps an instance of {@link File}.
 * 
 * @author Franz-Josef Elmer
 */
public class FileBasedContentNode implements IHierarchicalContentNode
{
    private final File file;

    /**
     * Creates an instance based on the specified file.
     */
    public FileBasedContentNode(File file)
    {
        this.file = file;
    }

    /**
     * Returns the name of the wrapped file.
     */
    public String tryGetName()
    {
        return file.getName();
    }

    /**
     * Returns <code>true</code> if the wrapped file exists.
     */
    @Override
    public boolean exists()
    {
        return file.exists();
    }

    /**
     * Returns a new instance of {@link FileInputStream} for the wrapped file.
     */
    @Override
    public InputStream getInputStream()
    {
        try
        {
            return new BufferedInputStream(new FileInputStream(file));
        } catch (FileNotFoundException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public String getName()
    {
        return file.getName();
    }

    @Override
    public String getRelativePath()
    {
        return file.getPath();
    }

    @Override
    public String getParentRelativePath()
    {
        return file.getParent();
    }

    @Override
    public boolean isDirectory()
    {
        return file.isDirectory();
    }

    @Override
    public long getLastModified()
    {
        return file.lastModified();
    }

    @Override
    public List<IHierarchicalContentNode> getChildNodes() throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getFile() throws UnsupportedOperationException
    {
        return file;
    }

    @Override
    public File tryGetFile()
    {
        return file;
    }

    @Override
    public long getFileLength() throws UnsupportedOperationException
    {
        return file.length();
    }

    @Override
    public String getChecksum() throws UnsupportedOperationException
    {
        return null;
    }

    @Override
    public int getChecksumCRC32() throws UnsupportedOperationException
    {
        try
        {
            return (int) FileUtils.checksumCRC32(file);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public boolean isChecksumCRC32Precalculated()
    {
        return false;
    }

    @Override
    public IRandomAccessFile getFileContent() throws UnsupportedOperationException,
            IOExceptionUnchecked
    {
        return new RandomAccessFileImpl(file, "r");
    }
}
