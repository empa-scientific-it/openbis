/*
 * Copyright ETH 2019 - 2023 Zürich, Scientific IT Services
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * A chunk of a file.
 * 
 * @author pkupczyk
 * @author Franz-Josef Elmer
 *
 */
public class FileChunk extends Chunk
{

    private Path payloadPath;
    private ILogger logger;

    public FileChunk(int sequenceNumber, IDownloadItemId downloadItemId, String filePath, long fileOffset, 
            int payloadLength, Path payloadPath, ILogger logger)
    {
        super(sequenceNumber, downloadItemId, false, filePath, fileOffset, payloadLength);
        this.payloadPath = payloadPath;
        this.logger = logger;
    }

    @Override
    public InputStream getPayload() throws DownloadException
    {
        final ByteBuffer buffer;

        try (FileChannel fileChannel = FileChannel.open(payloadPath, StandardOpenOption.READ))
        {
            int payloadLength = getPayloadLength();
            buffer = ByteBuffer.allocate(payloadLength);
            fileChannel.position(getFileOffset());
            fileChannel.read(buffer);
            buffer.flip();
            byte[] bytes = new byte[payloadLength];
            buffer.get(bytes);
            return new ByteArrayInputStream(bytes)
                {
                    @Override
                    public void close() throws IOException
                    {
                        if (logger.isEnabled(LogLevel.DEBUG))
                        {
                            logger.log(FileChunk.class, LogLevel.DEBUG,
                                    "Closing input stream for chunk " + getSequenceNumber());
                        }
                    }
                };
        } catch (IOException e)
        {
            throw new DownloadException("Couldn't get payload: " + e.getMessage(), e, true);
        }
    }

}