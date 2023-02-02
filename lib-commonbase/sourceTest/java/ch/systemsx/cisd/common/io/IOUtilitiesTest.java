/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Random;
import java.util.zip.CRC32;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * @author Franz-Josef Elmer
 */
public class IOUtilitiesTest extends AssertJUnit
{
    private static final class CloseDetectingByteArrayInputStream extends ByteArrayInputStream
    {
        private boolean closeInvoked;

        public CloseDetectingByteArrayInputStream(byte[] bytes)
        {
            super(bytes);
        }

        @Override
        public void close() throws IOException
        {
            closeInvoked = true;
            super.close();
        }
    }

    @Test
    public void test()
    {
        Random random = new Random(37);
        byte[] exampleData = new byte[1000];
        for (int i = 0; i < exampleData.length; i++)
        {
            exampleData[i] = (byte) random.nextInt();
        }
        CRC32 crc32 = new CRC32();
        crc32.update(exampleData);
        long expectedChecksum = crc32.getValue();
        CloseDetectingByteArrayInputStream inputStream =
                new CloseDetectingByteArrayInputStream(exampleData);

        long checksum = IOUtilities.getChecksumCRC32(inputStream);

        assertEquals(expectedChecksum, checksum);
        assertEquals(true, inputStream.closeInvoked);
    }

}
