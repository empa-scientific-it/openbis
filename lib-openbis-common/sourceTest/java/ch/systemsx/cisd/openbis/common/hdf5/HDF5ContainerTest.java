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

package ch.systemsx.cisd.openbis.common.hdf5;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.openbis.common.hdf5.HDF5Container.IHDF5ReaderClient;
import ch.systemsx.cisd.openbis.common.hdf5.HDF5Container.IHDF5WriterClient;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class HDF5ContainerTest extends AbstractFileSystemTestCase
{
    private final static int KB = 1024;

    @BeforeTest
    public void disableHDF5ContainerCaching()
    {
        HDF5Container.disableCaching();
    }

    @Override
    @BeforeMethod
    public void setUp() throws IOException
    {
        super.setUp();
    }

    @Test
    public void testReadWriteUncompressedLarge()
    {
        File hdf5File = new File(workingDirectory, "testLarge.h5");
        hdf5File.delete();
        HDF5Container hdf5Content = new HDF5Container(hdf5File);
        final byte[] byteArray = createByteArray(1500 * KB);

        hdf5Content.runWriterClient(false, new IHDF5WriterClient()
            {
                @Override
                public void runWithSimpleWriter(IHDF5ContainerWriter writer)
                {
                    writer.writeToHDF5Container("/test-bytes", new ByteArrayInputStream(byteArray),
                            byteArray.length);
                }
            });

        hdf5Content.runReaderClient(new IHDF5ReaderClient()
            {
                @Override
                public void runWithSimpleReader(IHDF5ContainerReader reader)
                {
                    final ByteArrayOutputStream ostream = new ByteArrayOutputStream();
                    reader.readFromHDF5Container("/test-bytes", ostream);
                    byte[] readData = ostream.toByteArray();
                    assertEquals(byteArray, readData);
                }
            });
    }

    @Test
    public void testReadWriteUncompressed()
    {
        File hdf5File = new File(workingDirectory, "test.h5");
        hdf5File.delete();
        HDF5Container hdf5Content = new HDF5Container(hdf5File);
        final byte[] byteArray = createByteArray();

        hdf5Content.runWriterClient(false, new IHDF5WriterClient()
            {
                @Override
                public void runWithSimpleWriter(IHDF5ContainerWriter writer)
                {
                    writer.writeToHDF5Container("/test-bytes", new ByteArrayInputStream(byteArray),
                            byteArray.length);
                }
            });

        hdf5Content.runReaderClient(new IHDF5ReaderClient()
            {
                @Override
                public void runWithSimpleReader(IHDF5ContainerReader reader)
                {
                    final ByteArrayOutputStream ostream = new ByteArrayOutputStream();
                    reader.readFromHDF5Container("/test-bytes", ostream);
                    byte[] readData = ostream.toByteArray();
                    assertEquals(byteArray, readData);
                }
            });
    }

    @Test
    public void testReadWriteCompressed()
    {
        File hdf5File = new File(workingDirectory, "test.h5");
        hdf5File.delete();
        HDF5Container hdf5Content = new HDF5Container(hdf5File);
        final byte[] byteArray = createByteArray();

        hdf5Content.runWriterClient(true, new IHDF5WriterClient()
            {
                @Override
                public void runWithSimpleWriter(IHDF5ContainerWriter writer)
                {
                    writer.writeToHDF5Container("/test-bytes", new ByteArrayInputStream(byteArray),
                            byteArray.length);
                }
            });

        hdf5Content.runReaderClient(new IHDF5ReaderClient()
            {
                @Override
                public void runWithSimpleReader(IHDF5ContainerReader reader)
                {
                    final ByteArrayOutputStream ostream = new ByteArrayOutputStream();
                    reader.readFromHDF5Container("/test-bytes", ostream);
                    byte[] readData = ostream.toByteArray();
                    assertEquals(byteArray, readData);
                }
            });
    }

    @Test
    public void testSizeComparison()
    {
        final byte[] byteArray = createByteArray();

        File hdf5FileUncompressed = new File(workingDirectory, "test-uncompressed.h5");
        hdf5FileUncompressed.delete();
        HDF5Container hdf5ContentUncompressed = new HDF5Container(hdf5FileUncompressed);
        hdf5ContentUncompressed.runWriterClient(false, new IHDF5WriterClient()
            {
                @Override
                public void runWithSimpleWriter(IHDF5ContainerWriter writer)
                {
                    writer.writeToHDF5Container("/test-bytes", new ByteArrayInputStream(byteArray),
                            byteArray.length);
                }
            });

        File hdf5FileCompressed = new File(workingDirectory, "test-compressed.h5");
        HDF5Container hdf5ContentCompressed = new HDF5Container(hdf5FileCompressed);
        hdf5ContentCompressed.runWriterClient(true, new IHDF5WriterClient()
            {
                @Override
                public void runWithSimpleWriter(IHDF5ContainerWriter writer)
                {
                    writer.writeToHDF5Container("/test-bytes", new ByteArrayInputStream(byteArray),
                            byteArray.length);
                }
            });

        long uncompressedLength = hdf5FileUncompressed.length();
        long compressedLength = hdf5FileCompressed.length();

        assertTrue("" + uncompressedLength + " <= " + compressedLength,
                uncompressedLength > compressedLength);
    }

    private byte[] createByteArray()
    {
        return createByteArray(16 * KB);
    }

    private byte[] createByteArray(int numberOfBytes)
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(numberOfBytes);
        for (int i = 0; i < numberOfBytes; ++i)
        {
            bos.write(1);
        }
        return bos.toByteArray();
    }

}
