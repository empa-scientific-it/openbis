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

import java.nio.ByteBuffer;
import java.util.zip.CRC32;

import org.testng.annotations.DataProvider;

/**
 * @author Franz-Josef Elmer
 */
public class StreamTestUtils
{
    @DataProvider(name = "bufferSizes")
    public static Object[][] bufferSizes()
    {
        Object[][] objects = new Object[10][];
        for (int i = 0; i < objects.length; i++)
        {
            objects[i] = new Object[] { i + 1 };
        }
        return objects;
    }

    static long assertChunk(ByteBuffer byteBuffer, Chunk chunk)
    {
        int startPosition = byteBuffer.position();
        byteBuffer.mark();
        assertEquals(byteBuffer.getInt(), chunk.getSequenceNumber());
        String itemId = chunk.getDownloadItemId().getId();
        assertEquals(byteBuffer.getShort(), itemId.length());
        assertEquals(byteBuffer.get(), chunk.isDirectory() ? 1 : 0);
        String filePath = chunk.getFilePath();
        assertEquals(byteBuffer.getShort(), filePath.length());
        assertEquals(byteBuffer.getLong(), chunk.getFileOffset());
        assertEquals(byteBuffer.getInt(), chunk.getPayloadLength());
        assertEquals(new String(getBytesFrom(byteBuffer, itemId.length())), itemId);
        assertEquals(new String(getBytesFrom(byteBuffer, filePath.length())), filePath);
        int position = byteBuffer.position();
        byteBuffer.reset();
        CRC32 crc32 = new CRC32();
        crc32.update(getBytesFrom(byteBuffer, position - startPosition));
        assertEquals(byteBuffer.getLong(), crc32.getValue());
        TestInputStream payload = (TestInputStream) chunk.getPayload();
        assertEquals(new String(getBytesFrom(byteBuffer, payload.getContent().length())), payload.getContent());
        assertEquals(byteBuffer.getLong(), payload.getChecksum());
        return crc32.getValue();
    }

    private static byte[] getBytesFrom(ByteBuffer byteBuffer, int numberOfBytes)
    {
        byte[] bytes = new byte[numberOfBytes];
        byteBuffer.get(bytes);
        return bytes;
    }

}
