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

import java.io.EOFException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ch.systemsx.cisd.base.convert.NativeData;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;

/**
 * An implementation of {@link IRandomAccessFile} based on a {@link ByteBuffer}.
 * <p>
 * Does <i>not</i> implement {@link IRandomAccessFile#readLine()}.
 * 
 * @author Bernd Rinn
 */
public class ByteBufferRandomAccessFile implements IRandomAccessFile
{

    private final ByteBuffer buf;

    private void addToLength(int newItemLen)
    {
        final int rem = buf.remaining();
        if (newItemLen > rem)
        {
            buf.limit(buf.limit() + (newItemLen - rem));
        }
    }

    /**
     * Creates a {@link IRandomAccessFile} wrapper for the given <var>buf</var>.
     * 
     * @param buf The buffer to wrap.
     * @param initialLength The initially set length (corresponds to the {@link ByteBuffer#limit()}
     *            ).
     */
    public ByteBufferRandomAccessFile(ByteBuffer buf, int initialLength)
    {
        this(buf);
        setLength(initialLength);
    }

    /**
     * Creates a {@link IRandomAccessFile} wrapper for the given <var>buf</var>. Does not change the
     * {@link ByteBuffer#limit()} of <var>buf</var>.
     * 
     * @param buf The buffer to wrap.
     */
    public ByteBufferRandomAccessFile(ByteBuffer buf)
    {
        this.buf = buf;
    }

    /**
     * Creates a {@link IRandomAccessFile} wrapper for the given <var>array</var>.
     * 
     * @param array The byte array to wrap.
     * @param initialLength The initially set length.
     */
    public ByteBufferRandomAccessFile(byte[] array, int initialLength)
    {
        this(array);
        setLength(initialLength);
    }

    /**
     * Creates a {@link IRandomAccessFile} wrapper for the given <var>array</var>. The initial
     * {@link ByteBuffer#limit()} will be <code>array.length</code>.
     * 
     * @param array The byte array to wrap.
     */
    public ByteBufferRandomAccessFile(byte[] array)
    {
        this(ByteBuffer.wrap(array));
    }

    /**
     * Creates a {@link IRandomAccessFile} wrapper for a {@link ByteBuffer} with
     * <var>capacity</var>. The initial {@link ByteBuffer#limit()} will be <code>0</code>.
     * 
     * @param capacity The maximal size of the {@link ByteBuffer}.
     */
    public ByteBufferRandomAccessFile(int capacity)
    {
        this(ByteBuffer.allocate(capacity));
        setLength(0);
    }

    @Override
    public ByteOrder getByteOrder()
    {
        return buf.order();
    }

    @Override
    public void setByteOrder(ByteOrder byteOrder)
    {
        buf.order(byteOrder);
    }

    @Override
    public void readFully(byte[] b) throws IOExceptionUnchecked
    {
        readFully(b, 0, b.length);
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOExceptionUnchecked
    {
        if (available0() == -1)
        {
            throw new IOExceptionUnchecked(new EOFException());
        } else
        {
            buf.get(b, off, len);
        }
    }

    @Override
    public int skipBytes(int n) throws IOExceptionUnchecked
    {
        if (n <= 0)
        {
            return 0;
        }
        final int pos = buf.position();
        final int len = buf.limit();
        final int newpos = Math.min(len, pos + n);
        buf.position(newpos);
        return (newpos - pos);
    }

    @Override
    public void close() throws IOExceptionUnchecked
    {
        // NOOP
    }

    @Override
    public int read() throws IOExceptionUnchecked
    {
        return buf.get() & 0xff;
    }

    @Override
    public int read(byte[] b) throws IOExceptionUnchecked
    {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOExceptionUnchecked
    {
        final int bytesRead = Math.min(available0(), len);
        if (bytesRead < 0)
        {
            return bytesRead;
        }
        buf.get(b, off, bytesRead);
        return bytesRead;
    }

    @Override
    public long skip(long n) throws IOExceptionUnchecked
    {
        if (n > Integer.MAX_VALUE)
        {
            throw new IndexOutOfBoundsException();
        }
        return skipBytes((int) n);
    }
    
    private int available0() throws IOExceptionUnchecked
    {
        return (buf.remaining() == 0) ? -1 : buf.remaining();
    }

    @Override
    public int available() throws IOExceptionUnchecked
    {
        return buf.remaining();
    }

    @Override
    public void mark(int readlimit)
    {
        buf.mark();
    }

    @Override
    public void reset() throws IOExceptionUnchecked
    {
        buf.reset();
    }

    @Override
    public boolean markSupported()
    {
        return true;
    }

    @Override
    public void flush() throws IOExceptionUnchecked
    {
        // NOOP
    }

    @Override
    public void synchronize() throws IOExceptionUnchecked
    {
        // NOOP
    }

    @Override
    public long getFilePointer() throws IOExceptionUnchecked
    {
        return buf.position();
    }

    @Override
    public void seek(long pos) throws IOExceptionUnchecked
    {
        buf.position((int) pos);
    }

    @Override
    public long length() throws IOExceptionUnchecked
    {
        return buf.limit();
    }

    @Override
    public void setLength(long newLength) throws IOExceptionUnchecked
    {
        buf.limit((int) newLength);
    }

    @Override
    public boolean readBoolean() throws IOExceptionUnchecked
    {
        return buf.get() != 0;
    }

    @Override
    public byte readByte() throws IOExceptionUnchecked
    {
        return buf.get();
    }

    @Override
    public int readUnsignedByte() throws IOExceptionUnchecked
    {
        return buf.get() & 0xff;
    }

    @Override
    public short readShort() throws IOExceptionUnchecked
    {
        return buf.getShort();
    }

    @Override
    public int readUnsignedShort() throws IOExceptionUnchecked
    {
        return buf.getShort() & 0xffff;
    }

    @Override
    public char readChar() throws IOExceptionUnchecked
    {
        return buf.getChar();
    }

    @Override
    public int readInt() throws IOExceptionUnchecked
    {
        return buf.getInt();
    }

    @Override
    public long readLong() throws IOExceptionUnchecked
    {
        return buf.getLong();
    }

    @Override
    public float readFloat() throws IOExceptionUnchecked
    {
        return buf.getFloat();
    }

    @Override
    public double readDouble() throws IOExceptionUnchecked
    {
        return buf.getDouble();
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public String readLine() throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String readUTF()
    {
        try
        {
            final byte[] strBuf = new byte[readUnsignedShort()];
            buf.get(strBuf);
            return new String(strBuf, "UTF-8");
        } catch (UnsupportedEncodingException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public void write(int b) throws IOExceptionUnchecked
    {
        addToLength(1);
        buf.put((byte) b);
    }

    @Override
    public void write(byte[] b) throws IOExceptionUnchecked
    {
        addToLength(b.length);
        buf.put(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOExceptionUnchecked
    {
        addToLength(len);
        buf.put(b, off, len);
    }

    @Override
    public void writeBoolean(boolean v) throws IOExceptionUnchecked
    {
        addToLength(1);
        buf.put((byte) (v ? 1 : 0));
    }

    @Override
    public void writeByte(int v) throws IOExceptionUnchecked
    {
        write((byte) v);
    }

    @Override
    public void writeShort(int v) throws IOExceptionUnchecked
    {
        addToLength(NativeData.SHORT_SIZE);
        buf.putShort((short) v);
    }

    @Override
    public void writeChar(int v) throws IOExceptionUnchecked
    {
        addToLength(NativeData.CHAR_SIZE);
        buf.putChar((char) v);
    }

    @Override
    public void writeInt(int v) throws IOExceptionUnchecked
    {
        addToLength(NativeData.INT_SIZE);
        buf.putInt(v);
    }

    @Override
    public void writeLong(long v) throws IOExceptionUnchecked
    {
        addToLength(NativeData.LONG_SIZE);
        buf.putLong(v);
    }

    @Override
    public void writeFloat(float v) throws IOExceptionUnchecked
    {
        addToLength(NativeData.FLOAT_SIZE);
        buf.putFloat(v);
    }

    @Override
    public void writeDouble(double v) throws IOExceptionUnchecked
    {
        addToLength(NativeData.DOUBLE_SIZE);
        buf.putDouble(v);
    }

    @Override
    public void writeBytes(String s) throws IOExceptionUnchecked
    {
        final int len = s.length();
        addToLength(len);
        for (int i = 0; i < len; i++)
        {
            buf.put((byte) s.charAt(i));
        }
    }

    @Override
    public void writeChars(String s) throws IOExceptionUnchecked
    {
        final int len = s.length();
        addToLength(NativeData.CHAR_SIZE * len);
        for (int i = 0; i < len; i++)
        {
            final int v = s.charAt(i);
            buf.put((byte) ((v >>> 8) & 0xFF));
            buf.put((byte) ((v >>> 0) & 0xFF));
        }
    }

    @Override
    public void writeUTF(String str) throws UnsupportedOperationException
    {
        try
        {
            final byte[] strBuf = str.getBytes("UTF-8");
            addToLength(NativeData.SHORT_SIZE + strBuf.length);
            writeShort(strBuf.length);
            write(strBuf);
        } catch (UnsupportedEncodingException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

}
