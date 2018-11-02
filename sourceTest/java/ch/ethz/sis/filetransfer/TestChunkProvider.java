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
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import ch.ethz.sis.filetransfer.Chunk;
import ch.ethz.sis.filetransfer.IChunkProvider;
import ch.ethz.sis.filetransfer.IDownloadItemId;
import ch.ethz.sis.filetransfer.ILogger;
import ch.ethz.sis.filetransfer.LogLevel;

/**
 * @author pkupczyk
 */
public class TestChunkProvider implements IChunkProvider
{

    private static final long CHUNK_SIZE = 5;

    private ILogger logger;

    public TestChunkProvider(ILogger logger)
    {
        this.logger = logger;
    }

    @Override
    public Map<IDownloadItemId, List<Chunk>> getChunks(List<IDownloadItemId> itemIds)
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

    private List<Chunk> getChunks(AtomicInteger sequenceNumber, IDownloadItemId itemId)
    {
        try
        {
            final TestDownloadItemId filePathId = (TestDownloadItemId) itemId;
            final Path filePath = Paths.get(filePathId.getFilePath());
            final long fileSize = Files.size(filePath);
            long fileOffset = 0;

            List<Chunk> chunks = new LinkedList<Chunk>();

            do
            {
                final long theFileOffset = fileOffset;

                int payloadLength = (int) (Math.min(theFileOffset + CHUNK_SIZE, fileSize) - theFileOffset);

                chunks.add(new Chunk(sequenceNumber.getAndIncrement(), itemId, filePathId.getFilePath(), fileOffset, payloadLength)
                    {
                        @Override
                        public InputStream getPayload() throws IOException
                        {
                            FileChannel fileChannel = FileChannel.open(filePath, StandardOpenOption.READ);
                            fileChannel.position(theFileOffset);
                            InputStream fileStream = Channels.newInputStream(fileChannel);

                            return new InputStream()
                                {
                                    int counter = 0;

                                    @Override
                                    public int read() throws IOException
                                    {
                                        if (counter < CHUNK_SIZE)
                                        {
                                            counter++;
                                            return fileStream.read();
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
                                            logger.log(TestChunkProvider.class, LogLevel.DEBUG,
                                                    "Closing input stream for chunk " + getSequenceNumber());
                                        }
                                        fileStream.close();
                                    }
                                };
                        }
                    });

                fileOffset += CHUNK_SIZE;
            } while (fileOffset < fileSize);

            return chunks;
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

}
