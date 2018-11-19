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
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author pkupczyk
 */
public abstract class FileSystemChunkProvider implements IChunkProvider
{

    private ILogger logger;

    private long chunkSize;

    public FileSystemChunkProvider(ILogger logger, long chunkSize)
    {
        this.logger = logger;
        this.chunkSize = chunkSize;
    }

    public abstract Path getItemPath(IDownloadItemId itemId);

    @Override
    public Map<IDownloadItemId, List<Chunk>> getChunks(List<IDownloadItemId> itemIds) throws DownloadException
    {
        Map<IDownloadItemId, List<Chunk>> result = new HashMap<IDownloadItemId, List<Chunk>>();
        AtomicInteger sequenceNumber = new AtomicInteger(0);

        for (IDownloadItemId itemId : itemIds)
        {
            List<Chunk> chunks = getChunks(sequenceNumber, itemId);
            result.put(itemId, chunks);
        }

        return result;
    }

    private List<Chunk> getChunks(AtomicInteger sequenceNumber, IDownloadItemId itemId) throws DownloadException
    {
        try
        {
            final Path filePath = getItemPath(itemId);
            final long fileSize = Files.size(filePath);
            long fileOffset = 0;

            List<Chunk> chunks = new LinkedList<Chunk>();

            do
            {
                final long theFileOffset = fileOffset;

                int payloadLength = (int) (Math.min(theFileOffset + chunkSize, fileSize) - theFileOffset);

                chunks.add(new Chunk(sequenceNumber.getAndIncrement(), itemId, filePath.toString(), fileOffset, payloadLength)
                    {
                        @Override
                        public InputStream getPayload() throws DownloadException
                        {
                            final ByteBuffer buffer;

                            try (FileChannel fileChannel = FileChannel.open(filePath, StandardOpenOption.READ))
                            {
                                buffer = ByteBuffer.allocate((int) chunkSize);
                                fileChannel.position(theFileOffset);
                                fileChannel.read(buffer);
                                buffer.flip();
                            } catch (IOException e)
                            {
                                throw new DownloadException("Couldn't get payload", e, true);
                            }

                            return new InputStream()
                                {
                                    @Override
                                    public int read() throws IOException
                                    {
                                        if (buffer.hasRemaining())
                                        {
                                            return 0xff & buffer.get();
                                        } else
                                        {
                                            return -1;
                                        }
                                    }

                                    @Override
                                    public void close() throws IOException
                                    {
                                        if (logger.isEnabled(LogLevel.DEBUG))
                                        {
                                            logger.log(FileSystemChunkProvider.class, LogLevel.DEBUG,
                                                    "Closing input stream for chunk " + getSequenceNumber());
                                        }
                                    }
                                };
                        }
                    });

                fileOffset += chunkSize;
            } while (fileOffset < fileSize);

            return chunks;
        } catch (IOException e)
        {
            throw new DownloadException("Couldn't get chunk", e, true);
        }
    }

}
