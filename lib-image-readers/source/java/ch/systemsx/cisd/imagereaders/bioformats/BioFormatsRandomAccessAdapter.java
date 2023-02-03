/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.imagereaders.bioformats;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import loci.common.IRandomAccess;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.io.IRandomAccessFile;

/**
 * An adapter of the {@link IRandomAccess} object to the BioFormats {@Link IRandomAccess} object.
 * 
 * @author Bernd Rinn
 */
final class BioFormatsRandomAccessAdapter implements IRandomAccess
{
    private final IRandomAccessFile randomAccessFile;

    public BioFormatsRandomAccessAdapter(IRandomAccessFile randomAccessFile)
    {
        this.randomAccessFile = randomAccessFile;
    }

    @Override
    public void close() throws IOException
    {
        try
        {
            randomAccessFile.close();
        } catch (IOExceptionUnchecked ex)
        {
            throw ex.getCause();
        }
    }

    @Override
    public boolean exists() throws IOException
    {
        return true;
    }

    @Override
    public long skipBytes(long arg0) throws IOException
    {
        return randomAccessFile.skipBytes((int) arg0);
    }

    @Override
    public int read(byte[] b) throws IOException
    {
        try
        {
        return randomAccessFile.read(b);
        } catch (IOExceptionUnchecked ex)
        {
            throw ex.getCause();
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        try
        {
        return randomAccessFile.read(b, off, len);
        } catch (IOExceptionUnchecked ex)
        {
            throw ex.getCause();
        }
    }

    @Override
    public long getFilePointer() throws IOException
    {
        try
        {
        return randomAccessFile.getFilePointer();
        } catch (IOExceptionUnchecked ex)
        {
            throw ex.getCause();
        }
    }

    @Override
    public void seek(long pos) throws IOException
    {
        try
        {
        randomAccessFile.seek(pos);
        } catch (IOExceptionUnchecked ex)
        {
            throw ex.getCause();
        }
    }

    @Override
    public long length() throws IOException
    {
        try
        {
        return randomAccessFile.length();
        } catch (IOExceptionUnchecked ex)
        {
            throw ex.getCause();
        }
    }

    @Override
    public void readFully(byte[] b) throws IOException
    {
        try
        {
        randomAccessFile.readFully(b);
        } catch (IOExceptionUnchecked ex)
        {
            throw ex.getCause();
        }
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException
    {
        try
        {
        randomAccessFile.readFully(b, off, len);
        } catch (IOExceptionUnchecked ex)
        {
            throw ex.getCause();
        }
    }

    @Override
    public int skipBytes(int n) throws IOException
    {
        try
        {
        return randomAccessFile.skipBytes(n);
        } catch (IOExceptionUnchecked ex)
        {
            throw ex.getCause();
        }
    }

    @Override
    public boolean readBoolean() throws IOException
    {
        try
        {
        return randomAccessFile.readBoolean();
        } catch (IOExceptionUnchecked ex)
        {
            throw ex.getCause();
        }
    }

    @Override
    public byte readByte() throws IOException
    {
        try
        {
        return randomAccessFile.readByte();
        } catch (IOExceptionUnchecked ex)
        {
            throw ex.getCause();
        }
    }

    @Override
    public int readUnsignedByte() throws IOException
    {
        try
        {
        return randomAccessFile.readUnsignedByte();
        } catch (IOExceptionUnchecked ex)
        {
            throw ex.getCause();
        }
    }

    @Override
    public short readShort() throws IOException
    {
        try
        {
        return randomAccessFile.readShort();
        } catch (IOExceptionUnchecked ex)
        {
            throw ex.getCause();
        }
    }

    @Override
    public int readUnsignedShort() throws IOException
    {
        try
        {
        return randomAccessFile.readUnsignedShort();
        } catch (IOExceptionUnchecked ex)
        {
            throw ex.getCause();
        }
    }

    @Override
    public char readChar() throws IOException
    {
        try
        {
        return randomAccessFile.readChar();
        } catch (IOExceptionUnchecked ex)
        {
            throw ex.getCause();
        }
    }

    @Override
    public int readInt() throws IOException
    {
        try
        {
        return randomAccessFile.readInt();
        } catch (IOExceptionUnchecked ex)
        {
            throw ex.getCause();
        }
    }

    @Override
    public long readLong() throws IOException
    {
        try
        {
        return randomAccessFile.readLong();
        } catch (IOExceptionUnchecked ex)
        {
            throw ex.getCause();
        }
    }

    @Override
    public float readFloat() throws IOException
    {
        try
        {
        return randomAccessFile.readFloat();
        } catch (IOExceptionUnchecked ex)
        {
            throw ex.getCause();
        }
    }

    @Override
    public double readDouble() throws IOException
    {
        try
        {
        return randomAccessFile.readDouble();
        } catch (IOExceptionUnchecked ex)
        {
            throw ex.getCause();
        }
    }

    @Override
    public String readLine() throws IOException
    {
        try
        {
        return randomAccessFile.readLine();
        } catch (IOExceptionUnchecked ex)
        {
            throw ex.getCause();
        }
    }

    @Override
    public String readUTF() throws IOException
    {
        try
        {
        return randomAccessFile.readUTF();
        } catch (IOExceptionUnchecked ex)
        {
            throw ex.getCause();
        }
    }

    @Override
    public void write(int b) throws IOException
    {
        try
        {
        randomAccessFile.write(b);
        } catch (IOExceptionUnchecked ex)
        {
            throw ex.getCause();
        }
    }

    @Override
    public void write(byte[] b) throws IOException
    {
        try
        {
        randomAccessFile.write(b);
        } catch (IOExceptionUnchecked ex)
        {
            throw ex.getCause();
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        try
        {
        randomAccessFile.write(b, off, len);
        } catch (IOExceptionUnchecked ex)
        {
            throw ex.getCause();
        }
    }

    @Override
    public void writeBoolean(boolean v) throws IOException
    {
        try
        {
        randomAccessFile.writeBoolean(v);
        } catch (IOExceptionUnchecked ex)
        {
            throw ex.getCause();
        }
    }

    @Override
    public void writeByte(int v) throws IOException
    {
        try
        {
        randomAccessFile.writeByte(v);
        } catch (IOExceptionUnchecked ex)
        {
            throw ex.getCause();
        }
    }

    @Override
    public void writeShort(int v) throws IOException
    {
        try
        {
        randomAccessFile.writeShort(v);
        } catch (IOExceptionUnchecked ex)
        {
            throw ex.getCause();
        }
    }

    @Override
    public void writeChar(int v) throws IOException
    {
        try
        {
        randomAccessFile.writeChar(v);
        } catch (IOExceptionUnchecked ex)
        {
            throw ex.getCause();
        }
    }

    @Override
    public void writeInt(int v) throws IOException
    {
        try
        {
        randomAccessFile.writeInt(v);
        } catch (IOExceptionUnchecked ex)
        {
            throw ex.getCause();
        }
    }

    @Override
    public void writeLong(long v) throws IOException
    {
        try
        {
        randomAccessFile.writeLong(v);
        } catch (IOExceptionUnchecked ex)
        {
            throw ex.getCause();
        }
    }

    @Override
    public void writeFloat(float v) throws IOException
    {
        try
        {
        randomAccessFile.writeFloat(v);
        } catch (IOExceptionUnchecked ex)
        {
            throw ex.getCause();
        }
    }

    @Override
    public void writeDouble(double v) throws IOException
    {
        try
        {
        randomAccessFile.writeDouble(v);
        } catch (IOExceptionUnchecked ex)
        {
            throw ex.getCause();
        }
    }

    @Override
    public void writeBytes(String s) throws IOException
    {
        try
        {
        randomAccessFile.writeBytes(s);
        } catch (IOExceptionUnchecked ex)
        {
            throw ex.getCause();
        }
    }

    @Override
    public void writeChars(String s) throws IOException
    {
        try
        {
            randomAccessFile.writeChars(s);
        } catch (IOExceptionUnchecked ex)
        {
            throw ex.getCause();
        }
    }

    @Override
    public void writeUTF(String str) throws IOException
    {
        try
        {
        randomAccessFile.writeUTF(str);
        } catch (IOExceptionUnchecked ex)
        {
            throw ex.getCause();
        }
    }

    @Override
    public ByteOrder getOrder()
    {
        return randomAccessFile.getByteOrder();
    }

    @Override
    public void setOrder(ByteOrder order)
    {
        randomAccessFile.setByteOrder(order);
    }

    @Override
    public void write(ByteBuffer buf) throws IOException
    {
        write(buf, 0, buf.capacity());
    }

    @Override
    public void write(ByteBuffer buffer, int offset, int len) throws IOException
    {
        if (buffer.hasArray())
        {
            write(buffer.array(), offset, len);
        } else
        {
            final int pos = buffer.position();
            buffer.position(offset);
            final byte[] tbuf = new byte[4096];
            int remaining = len;
            while (remaining > 0)
            {
                final int bytesToRead = Math.min(remaining, tbuf.length);
                buffer.get(tbuf, 0, bytesToRead);
                remaining -= bytesToRead;
                write(tbuf, 0, bytesToRead);
            }
            buffer.position(pos);
        }
    }

    @Override
    public int read(ByteBuffer buffer) throws IOException
    {
        return read(buffer, 0, buffer.capacity());
    }

    @Override
    public int read(ByteBuffer buffer, int offset, int len) throws IOException
    {
        if (buffer.hasArray())
        {
            return read(buffer.array(), offset, len);
        } else
        {
            final int pos = buffer.position();
            buffer.position(offset);
            final byte[] tbuf = new byte[4096];
            int remaining = len;
            int bytesTotallyRead = 0;
            while (remaining > 0)
            {
                final int bytesRead = read(tbuf, 0, remaining);
                remaining -= bytesRead;
                bytesTotallyRead += bytesRead;
                buffer.put(tbuf, 0, bytesRead);
            }
            buffer.position(pos);
            return bytesTotallyRead;
        }
    }

}
