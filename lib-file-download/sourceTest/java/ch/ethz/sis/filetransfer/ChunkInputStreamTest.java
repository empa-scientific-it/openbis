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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.MessageFormat;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

/**
 * @author Franz-Josef Elmer
 */
public class ChunkInputStreamTest
{
    private static final String LOG_TEMPLATE = "HeaderInputStream: [DEBUG] Header CRC (server): {0}\n{1}";

    static String getExpectedLogMessage(long headerChecksum, TestInputStream testInputStream)
    {
        return new MessageFormat(LOG_TEMPLATE).format(new Object[] { Long.toHexString(headerChecksum),
                PayloadInputStreamTest.getExpectedLogMessage(testInputStream) });
    }

    @Test(dataProviderClass = StreamTestUtils.class, dataProvider = "bufferSizes")
    public void testBulkCase(int bufferSize) throws IOException
    {
        // Given
        ILogger logger = new RecordingLogger();
        String content = "hello";
        TestInputStream testInputStream = new TestInputStream(content);
        IDownloadItemIdSerializer itemIdSerializer = new TestDownloadItemIdSerializer();
        TestDownloadItemId downloadItemId = new TestDownloadItemId(new File("abc").toPath());
        Chunk chunk = new Chunk(42, downloadItemId, false, "blabla", 13, content.length())
            {
                @Override
                public InputStream getPayload() throws DownloadException
                {
                    return testInputStream;
                }
            };
        ChunkInputStream inputStream = new ChunkInputStream(logger, itemIdSerializer, chunk);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        // When
        long numberOfBytes = IOUtils.copy(inputStream, output, bufferSize);
        inputStream.close();

        // Then
        byte[] rawBytes = output.toByteArray();
        ByteBuffer byteBuffer = ByteBuffer.allocate(rawBytes.length);
        byteBuffer.put(rawBytes);
        byteBuffer.flip();
        long headerCheckSum = StreamTestUtils.assertChunk(byteBuffer, chunk);
        assertEquals(logger.toString(),
                getExpectedLogMessage(headerCheckSum, testInputStream));
        assertEquals(numberOfBytes, rawBytes.length);
        assertEquals(testInputStream.isClosed(), true);
    }

}
