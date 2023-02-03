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

package ch.systemsx.cisd.imagereaders;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.io.ByteBufferRandomAccessFile;
import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.base.io.RandomAccessFileImpl;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * Abstract class that facilitates the implementations of {@link IImageReader}.
 * 
 * @author Kaloyan Enimanev
 */
public abstract class AbstractImageReader implements IImageReader
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            AbstractImageReader.class);

    private final String libraryName;

    private final String readerName;

    public AbstractImageReader(String libraryName, String readerName)
    {
        this.libraryName = libraryName;
        this.readerName = readerName;
    }

    @Override
    public String getLibraryName()
    {
        return libraryName;
    }

    @Override
    public String getName()
    {
        return readerName;
    }

    @Override
    public final List<ImageID> getImageIDs(File file) throws IOExceptionUnchecked
    {
        IRandomAccessFile raf = createRandomAccessFile(file);
        try
        {
            return getImageIDs(raf);
        } finally
        {
            raf.close();
        }
    }

    @Override
    public final List<ImageID> getImageIDs(byte[] bytes)
    {
        return getImageIDs(new ByteBufferRandomAccessFile(bytes));
    }

    @Override
    public List<ImageID> getImageIDs(IRandomAccessFile handle) throws IOExceptionUnchecked
    {
        return Arrays.asList(ImageID.NULL);
    }

    @Override
    public BufferedImage readImage(File file, ImageID imageID, IReadParams params) throws IOExceptionUnchecked
    {
        IRandomAccessFile raf = createRandomAccessFile(file);
        try
        {
            return readImage(raf, imageID, params);
        } finally
        {
            raf.close();
        }
    }

    @Override
    public BufferedImage readImage(byte[] bytes, ImageID imageID, IReadParams params)
    {
        IRandomAccessFile raf = new ByteBufferRandomAccessFile(bytes);
        return readImage(raf, imageID, params);
    }

    @Override
    public boolean isMetaDataAware()
    {
        return false;
    }

    @Override
    public Map<String, Object> readMetaData(File file, ImageID imageID, IReadParams params)
            throws IOExceptionUnchecked
    {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Object> readMetaData(byte[] bytes, ImageID imageID, IReadParams params)
    {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Object> readMetaData(IRandomAccessFile handle, ImageID imageID, IReadParams params)
    {
        return Collections.emptyMap();
    }

    @Override
    public void close()
    {
    }
    
    protected IRandomAccessFile createRandomAccessFile(File file)
    {
        return new RandomAccessFileWithLogging(file, operationLog);
    }

    private static final class RandomAccessFileWithLogging implements IRandomAccessFile
    {
        private static int counter;
        
        private final IRandomAccessFile file;
        private final Logger logger;
        private final int id;

        public RandomAccessFileWithLogging(File file, Logger logger)
        {
            this.file = new RandomAccessFileImpl(file, "r");
            this.logger = logger;
            id = counter++;
            logger.info("Create random access file object " + id + " for file " + file.getAbsolutePath());
        }

        @Override
        public void close() throws IOExceptionUnchecked
        {
            // TODO: remove the next line of code after the reason for bug SSDM-4492 has been found
            logger.info("Close random access file object " + id, new RuntimeException("Stack trace"));
            file.close();
            logger.info("Successfully closed random access file object " + id);
        }

        @Override
        public int read() throws IOExceptionUnchecked
        {
            return file.read();
        }

        @Override
        public int read(byte[] b) throws IOExceptionUnchecked
        {
            return file.read(b);
        }

        @Override
        public ByteOrder getByteOrder()
        {
            return file.getByteOrder();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOExceptionUnchecked
        {
            return file.read(b, off, len);
        }

        @Override
        public void setByteOrder(ByteOrder byteOrder)
        {
            file.setByteOrder(byteOrder);
        }

        @Override
        public long skip(long n) throws IOExceptionUnchecked
        {
            return file.skip(n);
        }

        @Override
        public void flush() throws IOExceptionUnchecked
        {
            file.flush();
        }

        @Override
        public long getFilePointer() throws IOExceptionUnchecked
        {
            return file.getFilePointer();
        }

        @Override
        public int available() throws IOExceptionUnchecked
        {
            return file.available();
        }

        @Override
        public void synchronize() throws IOExceptionUnchecked
        {
            file.synchronize();
        }

        @Override
        public void mark(int readlimit)
        {
            file.mark(readlimit);
        }

        @Override
        public void reset() throws IOExceptionUnchecked
        {
            file.reset();
        }

        @Override
        public void seek(long pos) throws IOExceptionUnchecked
        {
            file.seek(pos);
        }

        @Override
        public boolean markSupported()
        {
            return file.markSupported();
        }

        @Override
        public long length() throws IOExceptionUnchecked
        {
            return file.length();
        }

        @Override
        public void setLength(long newLength) throws IOExceptionUnchecked
        {
            file.setLength(newLength);
        }

        @Override
        public void readFully(byte[] b) throws IOExceptionUnchecked
        {
            file.readFully(b);
        }

        @Override
        public void readFully(byte[] b, int off, int len) throws IOExceptionUnchecked
        {
            file.readFully(b, off, len);
        }

        @Override
        public int skipBytes(int n) throws IOExceptionUnchecked
        {
            return file.skipBytes(n);
        }

        @Override
        public boolean readBoolean() throws IOExceptionUnchecked
        {
            return file.readBoolean();
        }

        @Override
        public byte readByte() throws IOExceptionUnchecked
        {
            return file.readByte();
        }

        @Override
        public int readUnsignedByte() throws IOExceptionUnchecked
        {
            return file.readUnsignedByte();
        }

        @Override
        public short readShort() throws IOExceptionUnchecked
        {
            return file.readShort();
        }

        @Override
        public int readUnsignedShort() throws IOExceptionUnchecked
        {
            return file.readUnsignedShort();
        }

        @Override
        public char readChar() throws IOExceptionUnchecked
        {
            return file.readChar();
        }

        @Override
        public int readInt() throws IOExceptionUnchecked
        {
            return file.readInt();
        }

        @Override
        public long readLong() throws IOExceptionUnchecked
        {
            return file.readLong();
        }

        @Override
        public float readFloat() throws IOExceptionUnchecked
        {
            return file.readFloat();
        }

        @Override
        public double readDouble() throws IOExceptionUnchecked
        {
            return file.readDouble();
        }

        @Override
        public String readLine() throws IOExceptionUnchecked
        {
            return file.readLine();
        }

        @Override
        public String readUTF() throws IOExceptionUnchecked
        {
            return file.readUTF();
        }

        @Override
        public void write(int b) throws IOExceptionUnchecked
        {
            file.write(b);
        }

        @Override
        public void write(byte[] b) throws IOExceptionUnchecked
        {
            file.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOExceptionUnchecked
        {
            file.write(b, off, len);
        }

        @Override
        public void writeBoolean(boolean v) throws IOExceptionUnchecked
        {
            file.writeBoolean(v);
        }

        @Override
        public void writeByte(int v) throws IOExceptionUnchecked
        {
            file.writeByte(v);
        }

        @Override
        public void writeShort(int v) throws IOExceptionUnchecked
        {
            file.writeShort(v);
        }

        @Override
        public void writeChar(int v) throws IOExceptionUnchecked
        {
            file.writeChar(v);
        }

        @Override
        public void writeInt(int v) throws IOExceptionUnchecked
        {
            file.writeInt(v);
        }

        @Override
        public void writeLong(long v) throws IOExceptionUnchecked
        {
            file.writeLong(v);
        }

        @Override
        public void writeFloat(float v) throws IOExceptionUnchecked
        {
            file.writeFloat(v);
        }

        @Override
        public void writeDouble(double v) throws IOExceptionUnchecked
        {
            file.writeDouble(v);
        }

        @Override
        public void writeBytes(String s) throws IOExceptionUnchecked
        {
            file.writeBytes(s);
        }

        @Override
        public void writeChars(String s) throws IOExceptionUnchecked
        {
            file.writeChars(s);
        }

        @Override
        public void writeUTF(String str) throws IOExceptionUnchecked
        {
            file.writeUTF(str);
        }
    }
}
