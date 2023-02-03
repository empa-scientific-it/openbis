/*
 * Copyright 2019 ETH Zuerich, SIS
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
 * Wrapper of an {@link InputStream} which adds CRC32 checksum.
 * 
 * @author pkupczyk
 * @author Franz-Josef Elmer
 */
class PayloadInputStream extends AbstractBulkInputStream
{
    private InputStream payload;

    private int length;

    private CRC32 crc;

    private ByteBuffer crcBuffer;

    private ILogger logger;

    public PayloadInputStream(InputStream payLoad, ILogger logger) throws DownloadException
    {
        this.payload = payLoad;
        this.logger = logger;
        this.crc = new CRC32();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        // Format:
        // - payload (byte[] variable length)
        // - checksum (Long 8 bytes)
        int n = payload.read(b, off, len);
        if (n >= 0)
        {
            crc.update(b, off, n);
            length += n;
            return n;
        }
        if (crcBuffer == null)
        {
            crcBuffer = ByteBuffer.allocate(8).putLong(crc.getValue());
            crcBuffer.rewind();

            if (logger.isEnabled(LogLevel.DEBUG))
            {
                logger.log(getClass(), LogLevel.DEBUG, "Payload length (server): " + length);
                logger.log(getClass(), LogLevel.DEBUG, "Payload CRC (server): " + Long.toHexString(crc.getValue()));
            }
        }
        if (crcBuffer.hasRemaining())
        {
            n = Math.min(len, crcBuffer.remaining());
            crcBuffer.get(b, off, n);
            return n;
        }
        return -1;
    }

    @Override
    public void close() throws IOException
    {
        payload.close();
    }

}