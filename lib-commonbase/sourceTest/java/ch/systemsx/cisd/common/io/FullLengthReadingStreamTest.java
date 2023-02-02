/*
 * Copyright 2013 ETH Zuerich, CISD
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

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * @author pkupczyk
 */
public class FullLengthReadingStreamTest extends AssertJUnit
{

    @Test
    public void testHalfReadingStream() throws IOException
    {
        HalfReadingStream stream = new HalfReadingStream(new byte[]
        { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 });

        byte[] bytes = new byte[10];
        int count = stream.read(bytes, 3, 11);

        assertEquals(5, count);
        assertEquals(new byte[]
        { 0, 0, 0, 0, 1, 2, 3, 4, 0, 0 }, bytes);

        bytes = new byte[10];
        count = stream.read(bytes, 8, 4);

        assertEquals(2, count);
        assertEquals(new byte[]
        { 0, 0, 0, 0, 0, 0, 0, 0, 5, 6 }, bytes);

        bytes = new byte[10];
        count = stream.read(bytes, 0, 10);

        assertEquals(3, count);
        assertEquals(new byte[]
        { 7, 8, 9, 0, 0, 0, 0, 0, 0, 0 }, bytes);

        bytes = new byte[10];
        count = stream.read(bytes, 0, 10);

        assertEquals(-1, count);
        assertEquals(new byte[]
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, bytes);
        stream.close();
    }

    @Test
    public void testFullReadingStream() throws IOException
    {
        HalfReadingStream stream = new HalfReadingStream(new byte[]
        { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 });
        FullLengthReadingStream fullStream = new FullLengthReadingStream(stream);

        byte[] bytes = new byte[6];
        int count = fullStream.read(bytes);

        assertEquals(6, count);
        assertEquals(new byte[]
        { 0, 1, 2, 3, 4, 5 }, bytes);

        bytes = new byte[6];
        count = fullStream.read(bytes);

        assertEquals(4, count);
        assertEquals(new byte[]
        { 6, 7, 8, 9, 0, 0 }, bytes);

        bytes = new byte[6];
        count = fullStream.read(bytes);

        assertEquals(-1, count);
        assertEquals(new byte[]
        { 0, 0, 0, 0, 0, 0 }, bytes);
        fullStream.close();
    }

    private class HalfReadingStream extends ByteArrayInputStream
    {

        public HalfReadingStream(byte[] bytes)
        {
            super(bytes);
        }

        @Override
        public int read(byte[] b, int off, int len)
        {
            return super.read(b, off, Math.max(1, len / 2));
        }

    }

}
