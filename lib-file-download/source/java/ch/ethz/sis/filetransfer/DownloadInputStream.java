/*
 * Copyright ETH 2018 - 2023 Zürich, Scientific IT Services
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

/**
 * @author pkupczyk
 */
class DownloadInputStream extends AbstractBulkInputStream
{

    private ILogger logger;

    private IChunkQueue chunkQueue;

    private IChunkSerializer chunkSerializer;

    private Integer numberOfChunksOrNull;

    private InputStream chunkStream;

    private int sequenceNumber;

    public DownloadInputStream(ILogger logger, IChunkQueue chunkQueue, IChunkSerializer chunkSerializer, Integer numberOfChunksOrNull)
    {
        this.logger = logger;
        this.chunkQueue = chunkQueue;
        this.chunkSerializer = chunkSerializer;
        this.numberOfChunksOrNull = numberOfChunksOrNull;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        if (chunkStream == null)
        {
            if (numberOfChunksOrNull != null)
            {
                if (numberOfChunksOrNull > 0)
                {
                    numberOfChunksOrNull = numberOfChunksOrNull - 1;
                } else
                {
                    return -1;
                }
            }

            Chunk chunk = chunkQueue.poll();

            if (chunk == null)
            {
                return -1;
            } else
            {
                sequenceNumber = chunk.getSequenceNumber();
                if (logger.isEnabled(LogLevel.DEBUG))
                {
                    logger.log(getClass(), LogLevel.DEBUG, "Starting to read chunk " + sequenceNumber);
                }

                try
                {
                    chunkStream = chunkSerializer.serialize(chunk);
                } catch (DownloadException e)
                {
                    throw new IOException("Couldn't serialize a chunk " + sequenceNumber, e);
                }
            }
        }
        int n = chunkStream.read(b, off, len);
        if (n >= 0)
        {
            return n;
        }
        try
        {
            chunkStream.close();
        } catch (Exception e)
        {
            if (logger.isEnabled(LogLevel.WARN))
            {
                logger.log(getClass(), LogLevel.WARN, "Couldn't close the stream for chunk " + sequenceNumber, e);
            }
        }
        chunkStream = null;
        return read(b, off, len);
    }

    @Override
    public void close() throws IOException
    {
        if (chunkStream != null)
        {
            chunkStream.close();
        }
    }

}
