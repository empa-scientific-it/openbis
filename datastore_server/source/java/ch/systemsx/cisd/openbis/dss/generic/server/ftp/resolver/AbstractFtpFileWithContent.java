/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.ftp.resolver;

import java.io.IOException;
import java.nio.channels.FileChannel;

import ch.systemsx.cisd.base.io.IRandomAccessFile;

/**
 * @author Jakub Straszewski
 */
public abstract class AbstractFtpFileWithContent extends AbstractFtpFile
{
    private long size;

    public AbstractFtpFileWithContent(String absolutePath)
    {
        super(absolutePath);
    }

    @Override
    public long getSize()
    {
        return size;
    }

    public void setSize(long size)
    {
        this.size = size;
    }

    public FileChannel getFileChannel() throws IOException
    {
        throw new UnsupportedOperationException("File channels not supported.");
    }

    public abstract IRandomAccessFile getFileContent();
}
