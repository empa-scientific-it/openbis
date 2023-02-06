/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.DataSetFile;

/**
 * @author pkupczyk
 */
public class DataSetFileDownloadReader implements Serializable
{

    private static final long serialVersionUID = 1L;

    private InputStream in;

    private DataSetFileDownload lastDownload;

    public DataSetFileDownloadReader(InputStream in)
    {
        this.in = in;
    }

    public DataSetFileDownload read()
    {
        // [header size][header][content size][content]

        try
        {
            closeLastDownload();

            final long headerSize = readSize();
            final DataSetFile header = (DataSetFile) deserializeObject(headerSize);
            final long contentSize = readSize();
            final InputStream content = new InputStream()
                {

                    long bytesToRead = contentSize;

                    @Override
                    public int read() throws IOException
                    {
                        assertValidInputStream();

                        if (bytesToRead > 0)
                        {
                            bytesToRead--;
                            return in.read();
                        } else
                        {
                            return -1;
                        }
                    }

                    @Override
                    public int read(byte[] b, int off, int len) throws IOException
                    {
                        assertValidInputStream();

                        int numberOfBytesRead = -1;
                        if (bytesToRead > 0)
                        {
                            numberOfBytesRead = in.read(b, off, (int) Math.min(bytesToRead, (long) len));
                            if (numberOfBytesRead > 0)
                            {
                                bytesToRead -= numberOfBytesRead;
                            }
                        }
                        return numberOfBytesRead;
                    }

                    private void assertValidInputStream()
                    {
                        if (lastDownload.getInputStream() != this)
                        {
                            throw new IllegalStateException("Input stream no longer valid");
                        }
                    }
                };

            lastDownload = new DataSetFileDownload(header, content);
            return lastDownload;

        } catch (EOFException e)
        {
            close();
            return null;
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void close()
    {
        try
        {
            in.close();
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void closeLastDownload() throws IOException
    {
        if (lastDownload != null)
        {
            InputStream stream = lastDownload.getInputStream();
            while (true)
            {
                if (stream.read() == -1)
                {
                    return;
                }
            }
        }
    }

    private long readSize() throws IOException
    {
        DataInputStream data = new DataInputStream(in);
        return data.readLong();
    }

    private Object deserializeObject(long objectSize) throws IOException, ClassNotFoundException
    {
        if (objectSize == 0)
        {
            return null;
        }

        byte[] bytes = new byte[(int) objectSize];
        int numberOfBytesRead = read(in, bytes);
        if (numberOfBytesRead < objectSize)
        {
            throw new IllegalStateException("Only " + numberOfBytesRead + " bytes read instead of " + objectSize + " read.");
        }
        ByteArrayInputStream b = new ByteArrayInputStream(bytes);
        ObjectInputStream o = new ObjectInputStream(b);
        return o.readObject();
    }

    private static int read(final InputStream input, final byte[] buffer) throws IOException
    {
        int remaining = buffer.length;
        while (remaining > 0)
        {
            final int location = buffer.length - remaining;
            final int count = input.read(buffer, location, remaining);
            if (-1 == count)
            {
                break;
            }
            remaining -= count;
        }
        return buffer.length - remaining;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).toString();
    }

}
