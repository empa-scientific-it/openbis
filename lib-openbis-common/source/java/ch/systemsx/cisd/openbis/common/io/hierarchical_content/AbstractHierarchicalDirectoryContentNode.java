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

import ch.systemsx.cisd.base.io.IRandomAccessFile;

/**
 * {@link AbstractHierarchicalDirectoryContentNode} extension for directory nodes.
 * 
 * @author Piotr Buczek
 */
public abstract class AbstractHierarchicalDirectoryContentNode extends
        AbstractHierarchicalContentNode
{
    @Override
    protected final long doGetFileLength()
    {
        throw new UnsupportedOperationException(OPERATION_NOT_SUPPORTED_FOR_A_DIRECTORY);
    }

    @Override
    protected int doGetChecksumCRC32()
    {
        throw new UnsupportedOperationException(OPERATION_NOT_SUPPORTED_FOR_A_DIRECTORY);
    }

    @Override
    protected final IRandomAccessFile doGetFileContent()
    {
        throw new UnsupportedOperationException(OPERATION_NOT_SUPPORTED_FOR_A_DIRECTORY);
    }

    @Override
    protected final InputStream doGetInputStream()
    {
        throw new UnsupportedOperationException(OPERATION_NOT_SUPPORTED_FOR_A_DIRECTORY);
    }

}
