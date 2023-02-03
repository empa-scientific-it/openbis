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

package ch.systemsx.cisd.imagereaders;

import java.io.File;
import java.util.Map;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.io.ByteBufferRandomAccessFile;
import ch.systemsx.cisd.base.io.IRandomAccessFile;

/**
 * Abstract class that facilitates the implementations of metadata aware {@link IImageReader}-s.
 * 
 * @author Kaloyan Enimanev
 */
public abstract class AbstractMetaDataAwareImageReader extends AbstractImageReader
{
    public AbstractMetaDataAwareImageReader(String libraryName, String readerName)
    {
        super(libraryName, readerName);
    }

    @Override
    public Map<String, Object> readMetaData(File file, ImageID imageID, IReadParams params)
            throws IOExceptionUnchecked
    {
        IRandomAccessFile raf = createRandomAccessFile(file);
        try
        {
            return readMetaData(raf, imageID, params);
        } finally
        {
            raf.close();
        }
    }

    @Override
    public Map<String, Object> readMetaData(byte[] bytes, ImageID imageID, IReadParams params)
    {
        IRandomAccessFile raf = new ByteBufferRandomAccessFile(bytes);
        return readMetaData(raf, imageID, params);
    }

    @Override
    public boolean isMetaDataAware()
    {
        return true;
    }

}
