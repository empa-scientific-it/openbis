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
                if (logger.isEnabled(LogLevel.INFO))
                {
                    logger.log(getClass(), LogLevel.INFO, "Starting to read chunk " + chunk.getSequenceNumber());
                }

                try
                {
                    chunkStream = chunkSerializer.serialize(chunk);
                } catch (DownloadException e)
                {
                    throw new IOException("Couldn't serialize a chunk", e);
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
                logger.log(getClass(), LogLevel.WARN, "Couldn't close a chunk stream", e);
            }
        }
        chunkStream = null;
        return read(b, off, len);
    }

    @Override
    public int read() throws IOException
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
                if (logger.isEnabled(LogLevel.INFO))
                {
                    logger.log(getClass(), LogLevel.INFO, "Starting to read chunk " + chunk.getSequenceNumber());
                }

                try
                {
                    chunkStream = chunkSerializer.serialize(chunk);
                } catch (DownloadException e)
                {
                    throw new IOException("Couldn't serialize a chunk", e);
                }
            }
        }

        int value = chunkStream.read();

        if (value == -1)
        {
            try
            {
                chunkStream.close();
            } catch (Exception e)
            {
                if (logger.isEnabled(LogLevel.WARN))
                {
                    logger.log(getClass(), LogLevel.WARN, "Couldn't close a chunk stream", e);
                }
            }
            chunkStream = null;
            return read();
        } else
        {
            return value;
        }
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
