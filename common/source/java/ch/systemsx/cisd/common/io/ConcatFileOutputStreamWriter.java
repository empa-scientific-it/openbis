/*
 * Copyright 2010 ETH Zuerich, CISD
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
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A helper class which allows to separate multiple blocks written to one stream in a format:
 * (<block-size><block-of-bytes>)* where block-size is the long number. Useful to parse the content
 * of ConcatFileInputStream. Allocates only small constant amount of memory.
 * 
 * @author Tomasz Pylak
 */
class ConcatFileOutputStreamWriter
{
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    private static final int BYTES_PER_LONG = 8;

    private final InputStream inputStream;

    // buffer to read the file size
    private final byte[] blockSizeBuffer = new byte[BYTES_PER_LONG];

    // Number of bytes which left to be read from the currently read block, 0 if end of the current
    // block is reached.
    private long bytesToReadFromCurrent;

    public ConcatFileOutputStreamWriter(InputStream inputStream)
    {
        this.inputStream = inputStream;
        this.bytesToReadFromCurrent = 0;
    }

    /**
     * Copies the next block into the specified output stream. Returns the number of bytes copied or
     * -1 if there are no more blocks to read.
     */
    public long writeNextBlock(OutputStream output) throws IOException
    {
        long blockSize = gotoNextBlock();
        if (blockSize == -1)
        {
            return -1; // no more blocks
        }
        return copyCurrentBlock(output);
    }

    private long copyCurrentBlock(OutputStream output) throws IOException
    {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int n = 0;
        while (-1 != (n = readCurrent(buffer, 0, buffer.length)))
        {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    /**
     * block size if there is a next block in the stream and it is non-empty. Returns 0 if the block
     * is empty, -1 if there are no more blocks to read.<br>
     * Can be called only at the beginning or if the end of the previous block has been reached.
     * 
     * @throws IOException when the stream is corrupted and the size of the block cannot be read
     */
    private long gotoNextBlock() throws IOException
    {
        if (bytesToReadFromCurrent > 0)
        {
            throw new IllegalStateException(
                    "Cannot proceed to the next file before the current one is not read till the end.");
        }
        bytesToReadFromCurrent = readBlockSize();
        return bytesToReadFromCurrent;
    }

    // next block size in bytes if it's not the end of the stream, -1 if the end of stream has been
    // reached
    private long readBlockSize() throws IOException
    {
        int bytesRead = inputStream.read(blockSizeBuffer, 0, BYTES_PER_LONG);
        if (bytesRead == -1)
        {
            return -1;
        } else if (bytesRead < BYTES_PER_LONG)
        {
            throw new IOException("Stream corrupted, cannot read the block size.");
        } else
        {
            long blockSize = bytesToLong(blockSizeBuffer);
            assert blockSize >= 0 : "block size cannot be negative";
            return blockSize;
        }
    }

    /**
     * Reads the bytes from the current block. If len is 0 returns 0, otherwise if the end of one
     * block has been reached returns -1. It does not mean that the end of the whole stream has been
     * reached, the stream may contain other blocks. The method {@link #gotoNextBlock()} should be
     * called to start reading the next block.<br>
     * See {@link InputStream#read(byte[], int, int)} for parameters details.
     */
    private int readCurrent(byte b[], int off, int len) throws IOException
    {
        if (len == 0)
        {
            return 0;
        }
        if (bytesToReadFromCurrent == 0)
        {
            return -1;
        }

        int wantedBytes = (int) Math.min(len, bytesToReadFromCurrent);
        int bytesRead = inputStream.read(b, off, wantedBytes);

        if (bytesRead != wantedBytes)
        {
            throw new IOException("Corrupted stream, there should be at least " + wantedBytes
                    + " bytes in the block, but only " + bytesRead + " were available.");
        } else
        {
            bytesToReadFromCurrent -= bytesRead;
        }
        return bytesRead;
    }

    private static long bytesToLong(byte[] bytes) throws IOException
    {
        ByteArrayInputStream bos = new ByteArrayInputStream(bytes);
        DataInputStream dos = new DataInputStream(bos);
        try
        {
            return dos.readLong();
        } catch (IOException e)
        {
            throw new IOException("Cannot create the long from bytes: " + bytes);
        } finally
        {
            try
            {
                dos.close();
            } catch (IOException ex)
            {
                // ignore
            }
        }
    }

}
