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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;

/**
 * Input stream with chunk information as header (plus checksum) and payload data from {@link PayloadInputStream}.
 * 
 * @author pkupczyk
 * @author Franz-Josef Elmer
 */
class ChunkInputStream extends AbstractBulkInputStream
{

    Chunk chunk;

    private HeaderInputStream header;

    private PayloadInputStream payload;

    private ILogger logger;

    private IDownloadItemIdSerializer itemIdSerializer;

    public ChunkInputStream(ILogger logger, IDownloadItemIdSerializer itemIdSerializer, Chunk chunk) throws DownloadException
    {
        this.chunk = chunk;
        this.logger = logger;
        this.itemIdSerializer = itemIdSerializer;
        this.header = new HeaderInputStream();
        this.payload = new PayloadInputStream(chunk.getPayload(), logger);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        int n = header.read(b, off, len);
        if (n >= 0)
        {
            return n;
        }
        return payload.read(b, off, len);
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

        public HeaderInputStream() throws DownloadException
        {
            // Format:
            // - sequenceNumber (Integer 4 bytes)
            // - downloadItemIdLength (Short 2 bytes)
            // - isDirectory (byte 1 byte)
            // - filePathLength (Short 2 bytes)
            // - fileOffset (Long 8 bytes)
            // - payloadLength (Integer 4 bytes)
            // - downloadItemId (byte[] variable length)
            // - filePath (byte[] variable length)
            // - checksum (Long 8 bytes)

            byte[] downloadItemIdBytes = itemIdSerializer.serialize(chunk.getDownloadItemId());
            byte[] filePathBytes = chunk.getFilePath().toString().getBytes();

            if (downloadItemIdBytes.length > Short.MAX_VALUE)
            {
                throw new RuntimeException("Download item id too long too serialize");
            }

            if (filePathBytes.length > Short.MAX_VALUE)
            {
                throw new RuntimeException("File path too long to serialize");
            }

            fieldsBuffer = ByteBuffer.allocate(21 + downloadItemIdBytes.length + filePathBytes.length)
                    .putInt(chunk.getSequenceNumber())
                    .putShort((short) downloadItemIdBytes.length)
                    .put((byte) (chunk.isDirectory() ? 1 : 0))
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
}