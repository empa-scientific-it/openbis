/*
 * Copyright 2018 ETH Zuerich, CISD
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

package ch.ethz.sis.filetransfer;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * @author pkupczyk
 */
class DownloadInputStreamReader
{

    private ILogger logger;

    private PushbackInputStream downloadStream;

    private IChunkDeserializer chunkDeserializer;

    public DownloadInputStreamReader(ILogger logger, InputStream downloadStream, IChunkDeserializer chunkDeserializer)
    {
        this.logger = logger;
        this.downloadStream = new PushbackInputStream(downloadStream, 1);
        this.chunkDeserializer = chunkDeserializer;
    }

    public Chunk read() throws IOException
    {
        int b = downloadStream.read();

        if (b == -1)
        {
            return null;
        } else
        {
            downloadStream.unread(b);
        }

        Chunk chunk = chunkDeserializer.deserialize(downloadStream);

        if (chunk != null)
        {
            if (logger.isEnabled(LogLevel.INFO))
            {
                logger.log(getClass(), LogLevel.INFO, "Read chunk " + chunk.getSequenceNumber());
            }
        }

        return chunk;
    }

    public void close() throws IOException
    {
        downloadStream.close();
    }

}
