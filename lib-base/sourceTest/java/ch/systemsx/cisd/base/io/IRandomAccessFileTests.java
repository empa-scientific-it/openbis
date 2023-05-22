/*
 * Copyright 2007 - 2018 ETH Zuerich, CISD and SIS.
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

package ch.systemsx.cisd.base.io;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.convert.NativeData;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;

/**
 * Test cases for {@link IRandomAccessFile} implementations.
 * 
 * @author Bernd Rinn
 */
public abstract class IRandomAccessFileTests extends AbstractFileSystemTestCase
{

    abstract protected IRandomAccessFile createRandomAccessFile(String name);

    abstract protected IRandomAccessFile createRandomAccessFile(String name, byte[] content);

    @Test
    public void testSkip()
    {
        final IRandomAccessFile raf = createRandomAccessFile("testSkip");
        final byte[] b = new byte[4096];
        for (int i = 0; i < b.length; ++i)
        {
            b[i] = (byte) i;
        }
        raf.write(b);
        raf.seek(0);
        assertEquals(509, raf.skip(509));
        assertEquals(509, raf.getFilePointer());
        assertEquals(-3, raf.readByte());
        assertEquals(4096 - 509 - 1, raf.skip(4096));
        assertEquals(4096, raf.getFilePointer());
        raf.close();
    }

    @Test
    public void testLongByteOrder()
    {
        final IRandomAccessFile raf = createRandomAccessFile("testLongByteOrder");
        raf.writeLong(1);
        final byte[] buf = new byte[8];
        raf.seek(0);
        raf.read(buf);
        // Default is big endian
        assertEquals(0, buf[0]);
        assertEquals(1, buf[7]);

        raf.seek(0);
        raf.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        raf.writeLong(1);
        raf.seek(0);
        raf.read(buf);
        assertEquals(1, buf[0]);
        assertEquals(0, buf[7]);
        raf.close();
    }

    @Test
    public void testMark()
    {
        final IRandomAccessFile raf = createRandomAccessFile("testMark");
        final byte[] buf = new byte[128];
        raf.write(buf);
        raf.seek(0);
        assertTrue(raf.markSupported());
        raf.mark(0);
        raf.read();
        assertEquals(1, raf.getFilePointer());
        raf.reset();
        assertEquals(0, raf.getFilePointer());
        raf.read();
        raf.read();
        raf.read();
        raf.mark(0);
        raf.read();
        raf.read();
        raf.read();
        raf.reset();
        assertEquals(3, raf.getFilePointer());
        raf.close();
    }

    @Test
    public void testWriteReadByte()
    {
        final IRandomAccessFile raf = createRandomAccessFile("testWriteReadByte");
        raf.write(254);
        raf.seek(0);
        assertEquals(-2, raf.readByte());
        raf.seek(0);
        assertEquals(254, raf.read());
        raf.seek(0);
        assertEquals(254, raf.readUnsignedByte());
        raf.close();
    }

    @Test
    public void testWriteReadShort()
    {
        final IRandomAccessFile raf = createRandomAccessFile("testWriteReadShort");
        raf.writeShort(65534);
        raf.seek(0);
        assertEquals(-2, raf.readShort());
        raf.seek(0);
        assertEquals(65534, raf.readUnsignedShort());
        raf.close();
    }

    @Test
    public void testAvailable()
    {
        final IRandomAccessFile raf = createRandomAccessFile("testAvailable");
        assertEquals(0, raf.available());
        raf.writeDouble(5.5);
        raf.seek(0);
        assertEquals(8, raf.available());
        raf.close();
    }

    @Test
    public void testWriteReadStringBytes()
    {
        final String s = "teststring";
        final IRandomAccessFile raf = createRandomAccessFile("testWriteReadStringBytes");
        raf.writeBytes(s);
        raf.seek(0);
        assertEquals(s.length(), raf.available());
        final byte[] buf = new byte[raf.available()];
        raf.read(buf);
        assertEquals(s, new String(buf));
        raf.close();
    }

    @Test
    public void testWriteReadStringChars()
    {
        final String s = "teststring";
        final IRandomAccessFile raf = createRandomAccessFile("testWriteReadStringChars");
        raf.writeChars(s);
        raf.seek(0);
        assertEquals(2 * s.length(), raf.available());
        final byte[] buf = new byte[raf.available()];
        raf.read(buf);
        assertEquals(
                s,
                new String(NativeData.byteToChar(buf,
                        ch.systemsx.cisd.base.convert.NativeData.ByteOrder.BIG_ENDIAN)));
        raf.close();
    }

    @Test
    public void testReadLine() throws IOException
    {
        final byte[] bytes = "hello world".getBytes();
        final IRandomAccessFile raf = createRandomAccessFile("testWriteReadStringChars", bytes);
        final AdapterIInputStreamToInputStream is = new AdapterIInputStreamToInputStream(raf);

        assertEquals("[hello world]", IOUtils.readLines(is, Charset.defaultCharset()).toString());
        raf.close();
    }

    @Test
    public void testToByteArray() throws IOException
    {
        final byte[] bytes = "hello world".getBytes();
        final IRandomAccessFile raf = createRandomAccessFile("testWriteReadStringChars", bytes);
        final AdapterIInputStreamToInputStream is = new AdapterIInputStreamToInputStream(raf);

        assertEquals(bytes, IOUtils.toByteArray(is));
        raf.close();
    }

}
