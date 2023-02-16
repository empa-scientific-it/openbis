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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.MessageFormat;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

/**
 * @author Franz-Josef Elmer
 */
public class PayloadInputStreamTest
{
    private static final String LOG_TEMPLATE = "PayloadInputStream: [DEBUG] Payload length (server): {0}\n"
            + "PayloadInputStream: [DEBUG] Payload CRC (server): {1}\n";

    static String getExpectedLogMessage(TestInputStream testInputStream)
    {
        return new MessageFormat(LOG_TEMPLATE).format(new Object[] { testInputStream.getContent().length(),
                Long.toHexString(testInputStream.getChecksum()) });
    }

    @Test(dataProviderClass = StreamTestUtils.class, dataProvider = "bufferSizes")
    public void testBulkCase(int bufferSize) throws IOException
    {
        // Given
        ILogger logger = new RecordingLogger();
        String content = "hello";
        TestInputStream testInputStream = new TestInputStream(content);
        PayloadInputStream inputStream = new PayloadInputStream(testInputStream, logger);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        // When
        long numberOfBytes = IOUtils.copy(inputStream, output, bufferSize);
        inputStream.close();

        // Then
        assertEquals(13, numberOfBytes);
        byte[] bytes = output.toByteArray();
        assertEquals(new String(bytes, 0, content.length()), content);
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes, content.length(), Long.BYTES);
        buffer.flip();
        long checksum = testInputStream.getChecksum();
        assertEquals(buffer.getLong(), checksum);
        assertEquals(logger.toString(), getExpectedLogMessage(testInputStream));
        assertEquals(testInputStream.isClosed(), true);
    }

    @Test
    public void testByteByByteCase() throws IOException
    {
        // Given
        ILogger logger = new RecordingLogger();
        String content = "hello";
        TestInputStream testInputStream = new TestInputStream(content);
        PayloadInputStream inputStream = new PayloadInputStream(testInputStream, logger);

        // When
        ByteBuffer contentBuffer = getByteByByte(inputStream, content.length());
        ByteBuffer checksumBuffer = getByteByByte(inputStream, Long.BYTES);

        // Then
        assertEquals(inputStream.read(), -1);
        byte[] bytes = new byte[content.length()];
        contentBuffer.get(bytes);
        assertEquals(new String(bytes), content);
        long checksum = testInputStream.getChecksum();
        assertEquals(checksumBuffer.getLong(), checksum);
        assertEquals(logger.toString(), getExpectedLogMessage(testInputStream));
    }

    private ByteBuffer getByteByByte(InputStream inputStream, int numberOgBytes) throws IOException
    {
        ByteBuffer buffer = ByteBuffer.allocate(numberOgBytes);
        for (int i = 0; i < numberOgBytes; i++)
        {
            int b = inputStream.read();
            assertTrue(b >= 0, "EOF at index " + i);
            buffer.put((byte) b);
        }
        buffer.flip();
        return buffer;
    }
}
