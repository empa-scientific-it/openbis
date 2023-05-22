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

import static ch.systemsx.cisd.base.convert.NativeData.changeByteOrder;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;

/**
 * The file implementation of {@link IRandomAccessFile}.
 * 
 * @author Bernd Rinn
 */
public class RandomAccessFileImpl implements IRandomAccessFile
{

    private final RandomAccessFile randomAccessFile;

    private ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;

    private boolean changeByteOrder = ByteOrder.LITTLE_ENDIAN.equals(byteOrder);

    private long markedPosition = -1;

    public RandomAccessFileImpl(RandomAccessFile randomAccessFile)
    {
        this.randomAccessFile = randomAccessFile;
    }

    public RandomAccessFileImpl(String name, String mode) throws IOExceptionUnchecked
    {
        try
        {
            this.randomAccessFile = new RandomAccessFile(name, mode);
        } catch (FileNotFoundException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public RandomAccessFileImpl(File file, String mode) throws IOExceptionUnchecked
    {
        try
        {
            this.randomAccessFile = new RandomAccessFile(file, mode);
        } catch (FileNotFoundException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public final FileDescriptor getFD() throws IOException
    {
        return randomAccessFile.getFD();
    }

    public final FileChannel getChannel()
    {
        return randomAccessFile.getChannel();
    }

    @Override
    public ByteOrder getByteOrder()
    {
        return byteOrder;
    }

    @Override
    public void setByteOrder(ByteOrder byteOrder)
    {
        this.byteOrder = byteOrder;
        this.changeByteOrder = ByteOrder.LITTLE_ENDIAN.equals(byteOrder);
    }

    @Override
    public int read() throws IOExceptionUnchecked
    {
        try
        {
            return randomAccessFile.read();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOExceptionUnchecked
    {
        try
        {
            return randomAccessFile.read(b, off, len);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public int read(byte[] b) throws IOExceptionUnchecked
    {
        try
        {
            return randomAccessFile.read(b);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public final void readFully(byte[] b) throws IOExceptionUnchecked
    {
        try
        {
            randomAccessFile.readFully(b);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public final void readFully(byte[] b, int off, int len) throws IOExceptionUnchecked
    {
        try
        {
            randomAccessFile.readFully(b, off, len);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public int skipBytes(int n) throws IOExceptionUnchecked
    {
        try
        {
            return randomAccessFile.skipBytes(n);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public void write(int b) throws IOExceptionUnchecked
    {
        try
        {
            randomAccessFile.write(b);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public void write(byte[] b) throws IOExceptionUnchecked
    {
        try
        {
            randomAccessFile.write(b);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOExceptionUnchecked
    {
        try
        {
            randomAccessFile.write(b, off, len);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public long getFilePointer() throws IOExceptionUnchecked
    {
        try
        {
            return randomAccessFile.getFilePointer();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public void seek(long pos) throws IOExceptionUnchecked
    {
        try
        {
            randomAccessFile.seek(pos);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public long length() throws IOExceptionUnchecked
    {
        try
        {
            return randomAccessFile.length();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public void setLength(long newLength) throws IOExceptionUnchecked
    {
        try
        {
            randomAccessFile.setLength(newLength);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public void close() throws IOExceptionUnchecked
    {
        try
        {
            randomAccessFile.close();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public final boolean readBoolean() throws IOExceptionUnchecked
    {
        try
        {
            return randomAccessFile.readBoolean();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public final byte readByte() throws IOExceptionUnchecked
    {
        try
        {
            return randomAccessFile.readByte();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public final int readUnsignedByte() throws IOExceptionUnchecked
    {
        try
        {
            return randomAccessFile.readUnsignedByte();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public final short readShort() throws IOExceptionUnchecked
    {
        try
        {
            final short s = randomAccessFile.readShort();
            return changeByteOrder ? changeByteOrder(s) : s;
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public final int readUnsignedShort() throws IOExceptionUnchecked
    {
        return readShort() & 0xffff;
    }

    @Override
    public final char readChar() throws IOExceptionUnchecked
    {
        try
        {
            final char c = randomAccessFile.readChar(); 
            return changeByteOrder ? changeByteOrder(c) : c;
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public final int readInt() throws IOExceptionUnchecked
    {
        try
        {
            final int i = randomAccessFile.readInt();
            return changeByteOrder ? changeByteOrder(i) : i;
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public final long readLong() throws IOExceptionUnchecked
    {
        try
        {
            final long l = randomAccessFile.readLong();
            return changeByteOrder ? changeByteOrder(l) : l;
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public final float readFloat() throws IOExceptionUnchecked
    {
        try
        {
            final float f = randomAccessFile.readFloat();
            return changeByteOrder ? changeByteOrder(f) : f;
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public final double readDouble() throws IOExceptionUnchecked
    {
        try
        {
            final double d = randomAccessFile.readDouble();
            return changeByteOrder ? changeByteOrder(d) : d;
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public final String readLine() throws IOExceptionUnchecked
    {
        try
        {
            return randomAccessFile.readLine();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public final String readUTF() throws IOExceptionUnchecked
    {
        try
        {
            return randomAccessFile.readUTF();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public final void writeBoolean(boolean v) throws IOExceptionUnchecked
    {
        try
        {
            randomAccessFile.writeBoolean(v);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public final void writeByte(int v) throws IOExceptionUnchecked
    {
        try
        {
            randomAccessFile.writeByte(v);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public final void writeShort(int v) throws IOExceptionUnchecked
    {
        try
        {
            randomAccessFile.writeShort(changeByteOrder ? changeByteOrder(v) : v);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public final void writeChar(int v) throws IOExceptionUnchecked
    {
        try
        {
            randomAccessFile.writeChar(changeByteOrder ? changeByteOrder(v) : v);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public final void writeInt(int v) throws IOExceptionUnchecked
    {
        try
        {
            randomAccessFile.writeInt(changeByteOrder ? changeByteOrder(v) : v);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public final void writeLong(long v) throws IOExceptionUnchecked
    {
        try
        {
            randomAccessFile.writeLong(changeByteOrder ? changeByteOrder(v) : v);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public final void writeFloat(float v) throws IOExceptionUnchecked
    {
        try
        {
            randomAccessFile.writeFloat(changeByteOrder ? changeByteOrder(v) : v);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public final void writeDouble(double v) throws IOExceptionUnchecked
    {
        try
        {
            randomAccessFile.writeDouble(changeByteOrder ? changeByteOrder(v) : v);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public final void writeBytes(String s) throws IOExceptionUnchecked
    {
        try
        {
            randomAccessFile.writeBytes(s);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public final void writeChars(String s) throws IOExceptionUnchecked
    {
        try
        {
            randomAccessFile.writeChars(s);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public final void writeUTF(String str) throws IOExceptionUnchecked
    {
        try
        {
            randomAccessFile.writeUTF(str);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public long skip(long n) throws IOExceptionUnchecked
    {
        if (n <= 0)
        {
            return 0;
        }
        try
        {
            final long pos = randomAccessFile.getFilePointer();
            final long len = randomAccessFile.length();
            final long newpos = Math.min(len, pos + n);
            randomAccessFile.seek(newpos);
            return (int) (newpos - pos);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public int available() throws IOExceptionUnchecked
    {
        try
        {
            return (int) Math.min(randomAccessFile.length() - randomAccessFile.getFilePointer(), Integer.MAX_VALUE);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public void mark(int readlimit)
    {
        try
        {
            markedPosition = randomAccessFile.getFilePointer();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public void reset() throws IOExceptionUnchecked
    {
        if (markedPosition == -1)
        {
            throw new IOExceptionUnchecked(new IOException("mark() not called"));
        }
        try
        {
            randomAccessFile.seek(markedPosition);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
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
        try
        {
            randomAccessFile.getFD().sync();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    //
    // Object
    //

    @Override
    public int hashCode()
    {
        return randomAccessFile.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        return randomAccessFile.equals(obj);
    }

    @Override
    public String toString()
    {
        return randomAccessFile.toString();
    }

}
