/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.generic.shared;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;

/**
 * Decorator of {@link IHierarchicalContentNode} which knows data set behind.
 *
 * @author Franz-Josef Elmer
 */
public class DataSetAwareHierarchicalContentNode implements IHierarchicalContentNode
{
    private final IDatasetLocation dataSet;
    private final IHierarchicalContentNode node;

    public DataSetAwareHierarchicalContentNode(IDatasetLocation dataSet, IHierarchicalContentNode node)
    {
        this.dataSet = dataSet;
        this.node = node;
    }

    public IDatasetLocation getDataSet()
    {
        return dataSet;
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
        return DataSetAwareHierarchicalContent.decorate(dataSet, node.getChildNodes());
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
    public int getChecksumCRC32() throws UnsupportedOperationException
    {
        return node.getChecksumCRC32();
    }

    @Override
    public String getChecksum() throws UnsupportedOperationException
    {
        return node.getChecksum();
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
