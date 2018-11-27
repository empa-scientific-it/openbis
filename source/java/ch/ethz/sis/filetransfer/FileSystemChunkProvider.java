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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
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

    protected abstract Path getFilePath(IDownloadItemId itemId);

    @Override
    public Map<IDownloadItemId, List<Chunk>> getChunks(List<IDownloadItemId> itemIds) throws DownloadException
    {
        Map<IDownloadItemId, List<Chunk>> result = new HashMap<IDownloadItemId, List<Chunk>>();
        AtomicInteger sequenceNumber = new AtomicInteger(0);

        for (IDownloadItemId itemId : itemIds)
        {
            List<Chunk> chunks = getChunks(sequenceNumber, itemId, getFilePath(itemId));
            result.put(itemId, chunks);
        }

        return result;
    }

    private List<Chunk> getChunks(AtomicInteger sequenceNumber, IDownloadItemId itemId, Path filePath) throws DownloadException
    {
        List<Chunk> chunks = new LinkedList<Chunk>();

        try
        {
            if (filePath.toFile().isDirectory())
            {
                chunks.add(new DirectoryChunk(sequenceNumber.getAndIncrement(), itemId, filePath.toString()));

                for (File file : filePath.toFile().listFiles())
                {
                    chunks.addAll(getChunks(sequenceNumber, itemId, file.toPath()));
                }
            } else
            {
                long fileSize = Files.size(filePath);
                long fileOffset = 0;

                do
                {
                    int payloadLength = (int) (Math.min(fileOffset + chunkSize, fileSize) - fileOffset);

                    chunks.add(new FileChunk(sequenceNumber.getAndIncrement(), itemId, filePath.toString(), fileOffset, payloadLength));

                    fileOffset += chunkSize;
                } while (fileOffset < fileSize);
            }
        } catch (IOException e)
        {
            throw new DownloadException("Couldn't get chunk for file path: " + filePath, e, true);
        }

        return chunks;
    }

    private class DirectoryChunk extends Chunk
    {

        public DirectoryChunk(int sequenceNumber, IDownloadItemId downloadItemId, String filePath)
        {
            super(sequenceNumber, downloadItemId, true, filePath, 0, 0);
        }

        @Override
        public InputStream getPayload() throws DownloadException
        {
            return new InputStream()
                {
                    @Override
                    public int read() throws IOException
                    {
                        return -1;
                    }
                };
        }

    }

    private class FileChunk extends Chunk
    {

        public FileChunk(int sequenceNumber, IDownloadItemId downloadItemId, String filePath, long fileOffset, int payloadLength)
        {
            super(sequenceNumber, downloadItemId, false, filePath, fileOffset, payloadLength);
        }

        @Override
        public InputStream getPayload() throws DownloadException
        {
            final ByteBuffer buffer;

            try (FileChannel fileChannel = FileChannel.open(Paths.get(getFilePath()), StandardOpenOption.READ))
            {
                buffer = ByteBuffer.allocate(getPayloadLength());
                fileChannel.position(getFileOffset());
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

    }

}
