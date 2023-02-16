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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Franz-Josef Elmer
 */
public class DownloadInputStreamTest
{
    private static final String LOG_TEMPLATE = "DownloadInputStream: [DEBUG] Starting to read chunk {0}\n{1}";
    
    static String getExpectedLogMessage(int sequenceNumber, long headerChecksum, TestInputStream testInputStream)
    {
        return new MessageFormat(LOG_TEMPLATE).format(new Object[] {sequenceNumber, 
                ChunkInputStreamTest.getExpectedLogMessage(headerChecksum, testInputStream)});
    }

    @Test(dataProvider = "bufferSizesAndNumberOfChunks")
    public void testBulkCase(int bufferSize, Integer numberOfChunks) throws IOException
    {
        // Given
        ILogger logger = new RecordingLogger();
        List<Chunk> chunks = createChunks();
        IChunkQueue chunkQueue = new IChunkQueue()
            {
                int index;

                @Override
                public Chunk poll()
                {
                    return index < chunks.size() ? chunks.get(index++) : null;
                }
            };
        IDownloadItemIdSerializer itemIdSerializer = new TestDownloadItemIdSerializer();
        DefaultChunkSerializer chunkSerializer = new DefaultChunkSerializer(logger, itemIdSerializer);
        DownloadInputStream inputStream = new DownloadInputStream(logger, chunkQueue, chunkSerializer, numberOfChunks);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        // When
        long numberOfBytes = IOUtils.copy(inputStream, output, bufferSize);
        inputStream.close();

        // Then
        byte[] rawBytes = output.toByteArray();
        ByteBuffer byteBuffer = ByteBuffer.allocate(rawBytes.length);
        byteBuffer.put(rawBytes);
        byteBuffer.flip();
        StringBuilder builder = new StringBuilder();
        for (int i = 0, n = numberOfChunks == null ? chunks.size() : numberOfChunks; i < n; i++)
        {
            Chunk chunk = chunks.get(i);
            long headerChecksum = StreamTestUtils.assertChunk(byteBuffer, chunk);
            assertEquals(((TestInputStream) chunk.getPayload()).isClosed(), true);
            TestInputStream testInputStream = (TestInputStream) chunk.getPayload();
            builder.append(getExpectedLogMessage(chunk.getSequenceNumber(), headerChecksum, testInputStream));
            
        }
        assertEquals(logger.toString(), builder.toString());
        assertEquals(numberOfBytes, rawBytes.length);
    }
    
    @DataProvider(name = "bufferSizesAndNumberOfChunks")
    public static Object[][] bufferSizesAndNumberOfChunks()
    {
        Object[][] bufferSizes = StreamTestUtils.bufferSizes();
        Object[][] result = new Object[2 * bufferSizes.length][];
        for (int i = 0; i < bufferSizes.length; i++)
        {
            Object bufferSize = bufferSizes[i][0];
            result[2 * i] = new Object[] {bufferSize, null};
            result[2 * i + 1] = new Object[] {bufferSize, 2};
        }
        return result;
    }

    private List<Chunk> createChunks()
    {
        List<Chunk> chunks = new ArrayList<>();
        for (int i = 0; i < 3; i++)
        {
            String content = "hello " + i;
            TestInputStream testInputStream = new TestInputStream(content);
            TestDownloadItemId downloadItemId = new TestDownloadItemId(new File("abc-" + i).toPath());
            Chunk chunk = new Chunk(42 + i, downloadItemId, false, "path-" + i, 13 + i, content.length())
                {
                    @Override
                    public InputStream getPayload() throws DownloadException
                    {
                        return testInputStream;
                    }
                };
            chunks.add(chunk);
        }
        return chunks;
    }

}
