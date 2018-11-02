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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;

/**
 * @author pkupczyk
 */
public class DefaultChunkDeserializer implements IChunkDeserializer
{

    private ILogger logger;

    private IDownloadItemIdDeserializer itemIdDeserializer;

    private ByteBuffer sequenceNumberBuffer = ByteBuffer.allocate(4);

    private ByteBuffer downloadItemIdLengthBuffer = ByteBuffer.allocate(2);

    private ByteBuffer filePathLengthBuffer = ByteBuffer.allocate(2);

    private ByteBuffer fileOffsetBuffer = ByteBuffer.allocate(8);

    private ByteBuffer payloadLengthBuffer = ByteBuffer.allocate(4);

    private ByteBuffer downloadItemIdBuffer;

    private ByteBuffer filePathBuffer;

    private ByteBuffer payloadBuffer;

    private ByteBuffer sentHeaderChecksumBuffer = ByteBuffer.allocate(8);

    private ByteBuffer sentPayloadChecksumBuffer = ByteBuffer.allocate(8);

    private CRC32 calculatedHeaderChecksum = new CRC32();

    private CRC32 calculatedPayloadChecksum = new CRC32();

    public DefaultChunkDeserializer(ILogger logger, IDownloadItemIdDeserializer itemIdDeserializer)
    {
        this.logger = logger;
        this.itemIdDeserializer = itemIdDeserializer;
    }

    @Override
    public Chunk deserialize(InputStream stream) throws IOException
    {
        calculatedHeaderChecksum.reset();
        calculatedPayloadChecksum.reset();

        putToBuffer(stream, sequenceNumberBuffer, "sequenceNumber", sequenceNumberBuffer.capacity(), calculatedHeaderChecksum);
        int sequenceNumber = sequenceNumberBuffer.getInt();

        putToBuffer(stream, downloadItemIdLengthBuffer, "downloadItemIdLength", downloadItemIdLengthBuffer.capacity(), calculatedHeaderChecksum);
        short downloadItemIdLength = downloadItemIdLengthBuffer.getShort();

        putToBuffer(stream, filePathLengthBuffer, "filePathLength", filePathLengthBuffer.capacity(), calculatedHeaderChecksum);
        short filePathLength = filePathLengthBuffer.getShort();

        putToBuffer(stream, fileOffsetBuffer, "fileOffset", fileOffsetBuffer.capacity(), calculatedHeaderChecksum);
        long fileOffset = fileOffsetBuffer.getLong();

        putToBuffer(stream, payloadLengthBuffer, "payloadLength", payloadLengthBuffer.capacity(), calculatedHeaderChecksum);
        int payloadLength = payloadLengthBuffer.getInt();

        downloadItemIdBuffer = reuseOrExtendBuffer(downloadItemIdBuffer, downloadItemIdLength);
        putToBuffer(stream, downloadItemIdBuffer, "downloadItemId", downloadItemIdLength, calculatedHeaderChecksum);
        IDownloadItemId downloadItemId = itemIdDeserializer.deserialize(downloadItemIdBuffer.array());

        filePathBuffer = reuseOrExtendBuffer(filePathBuffer, filePathLength);
        putToBuffer(stream, filePathBuffer, "filePath", filePathLength, calculatedHeaderChecksum);
        String filePath = new String(filePathBuffer.array());

        putToBuffer(stream, sentHeaderChecksumBuffer, "headerChecksum", sentHeaderChecksumBuffer.capacity(), null);
        long sentHeaderChecksum = sentHeaderChecksumBuffer.getLong();

        if (logger.isEnabled(LogLevel.DEBUG))
        {
            logger.log(getClass(), LogLevel.DEBUG, "Header CRC (client): " + Long.toHexString(calculatedHeaderChecksum.getValue()));
        }

        if (calculatedHeaderChecksum.getValue() != sentHeaderChecksum)
        {
            throw new IOException(
                    "Error in header data detected. Calculated checksum: " + calculatedHeaderChecksum.getValue() + ". Sent checksum: "
                            + sentHeaderChecksum);
        }

        payloadBuffer = reuseOrExtendBuffer(payloadBuffer, payloadLength);
        putToBuffer(stream, payloadBuffer, "payload", payloadLength, calculatedPayloadChecksum);
        byte[] payload = payloadBuffer.array();

        putToBuffer(stream, sentPayloadChecksumBuffer, "payloadChecksum", sentPayloadChecksumBuffer.capacity(), null);
        long sentPayloadChecksum = sentPayloadChecksumBuffer.getLong();

        if (logger.isEnabled(LogLevel.DEBUG))
        {
            logger.log(getClass(), LogLevel.DEBUG, "Payload: '" + new String(payload) + "', length: " + payloadLength);
            logger.log(getClass(), LogLevel.DEBUG, "Payload CRC (client): " + Long.toHexString(calculatedPayloadChecksum.getValue()));
        }

        if (calculatedPayloadChecksum.getValue() != sentPayloadChecksum)
        {
            throw new IOException(
                    "Error in payload data detected. Calculated checksum: " + calculatedPayloadChecksum.getValue() + ". Sent checksum: "
                            + sentPayloadChecksum);
        }

        return new Chunk(sequenceNumber, downloadItemId, filePath, fileOffset, payloadLength)
            {
                @Override
                public InputStream getPayload() throws IOException
                {
                    return new ByteArrayInputStream(payload, 0, payloadLength);
                }
            };
    }

    private ByteBuffer reuseOrExtendBuffer(ByteBuffer buffer, int length)
    {
        if (buffer != null && buffer.capacity() >= length)
        {
            return buffer;
        } else
        {
            return ByteBuffer.allocate(length);
        }
    }

    private void putToBuffer(InputStream stream, ByteBuffer buffer, String name, int length, CRC32 checksum) throws IOException
    {
        buffer.clear();

        int i = 0;

        while (i < length)
        {
            int b = stream.read();
            buffer.put((byte) b);
            if (checksum != null)
            {
                checksum.update(b);
            }
            i++;
        }

        if (i > 0 && buffer.get(i - 1) == -1)
        {
            throw new IOException("Unexpected finish of '" + name + "' field");
        }

        buffer.rewind();
    }

}
