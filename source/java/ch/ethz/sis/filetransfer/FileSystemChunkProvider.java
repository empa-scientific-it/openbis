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
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An abstract chunk provider that creates chunks from a file system. The provider can handle files as well as directories. If an item is a directory
 * then the provider returns chunks for the directory itself as well as all its contents (recursively).
 * 
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

    /**
     * An abstract method to be implemented in an actual store. The method must return a location of an item in a file system. WARNING: The actual
     * implementation of the provider should make sure not to introduce an uncontrolled access to the whole file system, e.g. by simply taking a file
     * path from the item id without any further validation.
     */
    protected abstract Path getFilePath(IDownloadItemId itemId);

    @Override
    public Map<IDownloadItemId, List<Chunk>> getChunks(List<IDownloadItemId> itemIds) throws DownloadException
    {
        Map<IDownloadItemId, List<Chunk>> result = new HashMap<IDownloadItemId, List<Chunk>>();
        AtomicInteger sequenceNumber = new AtomicInteger(0);

        for (IDownloadItemId itemId : itemIds)
        {
            Path itemFilePath = getFilePath(itemId);
            List<Chunk> chunks = getChunks(sequenceNumber, itemId, itemFilePath.getParent(), itemFilePath);
            result.put(itemId, chunks);
        }

        return result;
    }

    private List<Chunk> getChunks(AtomicInteger sequenceNumber, IDownloadItemId itemId, Path rootFilePath, Path filePath) throws DownloadException
    {
        List<Chunk> chunks = new LinkedList<Chunk>();

        try
        {
            if (filePath.toFile().isDirectory())
            {
                chunks.add(new DirectoryChunk(sequenceNumber.getAndIncrement(), itemId, rootFilePath.relativize(filePath).toString()));

                for (File file : filePath.toFile().listFiles())
                {
                    chunks.addAll(getChunks(sequenceNumber, itemId, rootFilePath, file.toPath()));
                }
            } else
            {
                long fileSize = Files.size(filePath);
                long fileOffset = 0;

                do
                {
                    int payloadLength = (int) (Math.min(fileOffset + chunkSize, fileSize) - fileOffset);

                    chunks.add(new FileChunk(sequenceNumber.getAndIncrement(), itemId, rootFilePath.relativize(filePath).toString(), fileOffset,
                            filePath, payloadLength));

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

        private Path payloadPath;

        public FileChunk(int sequenceNumber, IDownloadItemId downloadItemId, String filePath, long fileOffset, Path payloadPath, int payloadLength)
        {
            super(sequenceNumber, downloadItemId, false, filePath, fileOffset, payloadLength);
            this.payloadPath = payloadPath;
        }

        @Override
        public InputStream getPayload() throws DownloadException
        {
            final ByteBuffer buffer;

            try (FileChannel fileChannel = FileChannel.open(payloadPath, StandardOpenOption.READ))
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
