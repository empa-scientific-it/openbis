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

package ch.ethz.sis.filedownload;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;

/**
 * @author pkupczyk
 */
public class DefaultChunkSerializer implements IChunkSerializer
{

    private ILogger logger;

    private IDownloadItemIdSerializer itemIdSerializer;

    public DefaultChunkSerializer(ILogger logger, IDownloadItemIdSerializer itemIdSerializer)
    {
        this.logger = logger;
        this.itemIdSerializer = itemIdSerializer;
    }

    @Override
    public InputStream serialize(Chunk chunk) throws IOException
    {
        return new ChunkInputStream(chunk);
    }

    private class ChunkInputStream extends InputStream
    {

        private Chunk chunk;

        private HeaderInputStream header;

        private PayloadInputStream payload;

        public ChunkInputStream(Chunk chunk) throws IOException
        {
            this.chunk = chunk;
            this.header = new HeaderInputStream();
            this.payload = new PayloadInputStream();
        }

        @Override
        public int read() throws IOException
        {
            int value = header.read();

            if (value != -1)
            {
                return value;
            } else
            {
                return payload.read();
            }
        }

        @Override
        public void close() throws IOException
        {
            payload.close();
        }

        private class HeaderInputStream extends InputStream
        {

            private ByteBuffer fieldsBuffer;

            private ByteBuffer crcBuffer;

            public HeaderInputStream()
            {
                // Format:
                // - sequenceNumber (Integer 4 bytes)
                // - downloadItemIdLength (Short 2 bytes)
                // - filePathLength (Short 2 bytes)
                // - fileOffset (Long 8 bytes)
                // - payloadLength (Integer 4 bytes)
                // - downloadItemId (byte[] variable length)
                // - filePath (byte[] variable length)
                // - checksum (Long 8 bytes)

                byte[] downloadItemIdBytes = itemIdSerializer.serialize(chunk.getDownloadItemId());
                byte[] filePathBytes = chunk.getFilePath().getBytes();

                if (downloadItemIdBytes.length > Short.MAX_VALUE)
                {
                    throw new RuntimeException("Download item id too long too serialize");
                }

                if (filePathBytes.length > Short.MAX_VALUE)
                {
                    throw new RuntimeException("File path too long to serialize");
                }

                fieldsBuffer = ByteBuffer.allocate(20 + downloadItemIdBytes.length + filePathBytes.length)
                        .putInt(chunk.getSequenceNumber())
                        .putShort((short) downloadItemIdBytes.length)
                        .putShort((short) filePathBytes.length)
                        .putLong(chunk.getFileOffset())
                        .putInt(chunk.getPayloadLength())
                        .put(downloadItemIdBytes)
                        .put(filePathBytes);
                fieldsBuffer.rewind();

                CRC32 crc = new CRC32();
                crc.update(fieldsBuffer.array());

                crcBuffer = ByteBuffer.allocate(8).putLong(crc.getValue());
                crcBuffer.rewind();

                if (logger.isEnabled(LogLevel.DEBUG))
                {
                    logger.log(getClass(), LogLevel.DEBUG, "Header CRC (server): " + Long.toHexString(crc.getValue()));
                }
            }

            @Override
            public int read() throws IOException
            {
                if (fieldsBuffer.hasRemaining())
                {
                    // Do a binary 'AND' to convert a byte to an int, e.g. from 11111111 (i.e. from 255)
                    // make 0...11111111 instead of 1...11111111. The latter (i.e. -1) would be treated
                    // as an end of stream.
                    return 0xff & fieldsBuffer.get();
                } else if (crcBuffer.hasRemaining())
                {
                    return 0xff & crcBuffer.get();
                } else
                {
                    return -1;
                }
            }

        }

        private class PayloadInputStream extends InputStream
        {

            private InputStream payload;

            private CRC32 crc;

            private ByteBuffer crcBuffer;

            public PayloadInputStream() throws IOException
            {
                this.payload = chunk.getPayload();
                this.crc = new CRC32();
            }

            @Override
            public int read() throws IOException
            {
                // Format:
                // - payload (byte[] variable length)
                // - checksum (Long 8 bytes)

                int b = payload.read();

                if (b != -1)
                {
                    crc.update(b);
                    return b;
                } else
                {
                    if (crcBuffer == null)
                    {
                        crcBuffer = ByteBuffer.allocate(8).putLong(crc.getValue());
                        crcBuffer.rewind();

                        if (logger.isEnabled(LogLevel.DEBUG))
                        {
                            logger.log(getClass(), LogLevel.DEBUG, "Payload CRC (server): " + Long.toHexString(crc.getValue()));
                        }
                    }
                    if (crcBuffer.hasRemaining())
                    {
                        return 0xff & crcBuffer.get();
                    } else
                    {
                        return -1;
                    }
                }
            }

            @Override
            public void close() throws IOException
            {
                payload.close();
            }

        }
    }

}
