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

package ch.systemsx.cisd.openbis.common.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.io.ConcatenatedFileOutputStreamWriter;
import ch.systemsx.cisd.openbis.common.hdf5.HDF5Container;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

/**
 * Tests for {@link ConcatenatedContentInputStream}
 * 
 * @author Tomasz Pylak
 */
public class ConcatenatedContentInputStreamTest extends AbstractFileSystemTestCase
{
    @BeforeTest
    public void disableHDF5ContainerCaching()
    {
        HDF5Container.disableCaching();
    }

    @Test
    public void testNoFiles() throws IOException
    {
        ConcatenatedContentInputStream stream =
                new ConcatenatedContentInputStream(false, new IHierarchicalContentNode[0]);
        AssertJUnit.assertEquals(-1, stream.read());
        stream.close();
    }

    @Test
    public void testOneFile() throws IOException
    {
        String content = createLongString("1");
        IHierarchicalContentNode file = createContent(content, "f1.txt");
        ConcatenatedContentInputStream stream = new ConcatenatedContentInputStream(false, file);
        List<String> streamContent = readStrings(stream);
        assertEquals(1, streamContent.size());
        assertEquals(content, streamContent.get(0));
    }

    @Test
    public void testManyFiles() throws IOException
    {
        String content1 = createLongString("1");
        IHierarchicalContentNode file1 = createContent(content1, "f1.txt");

        String content2 = ""; // empty content
        IHierarchicalContentNode file2 = createContent(content2, "f2.txt");

        String content3 = createLongString("3");
        IHierarchicalContentNode file3 = createContent(content3, "f3.txt");

        ConcatenatedContentInputStream stream =
                new ConcatenatedContentInputStream(false, file1, file2, file3);
        List<String> streamContent = readStrings(stream);
        assertEquals(3, streamContent.size());
        assertEquals(content1, streamContent.get(0));
        assertEquals(content2, streamContent.get(1));
        assertEquals(content3, streamContent.get(2));
    }

    @Test
    public void testExistingAndNonExistingFiles() throws IOException
    {
        String content1 = createLongString("1");
        IHierarchicalContentNode file1 = createContent(content1, "f1.txt");

        IHierarchicalContentNode unexistingFile =
                new FileBasedContentNode(new File(workingDirectory, "unexisting.txt"));

        String content3 = createLongString("3");
        IHierarchicalContentNode file3 = createContent(content3, "f3.txt");

        IHierarchicalContentNode fileNull = null;

        ConcatenatedContentInputStream stream =
                new ConcatenatedContentInputStream(true, fileNull, file1, fileNull, unexistingFile,
                        file3, fileNull);
        List<String> streamContent = readStrings(stream);
        assertEquals(6, streamContent.size());
        assertEquals("", streamContent.get(0));
        assertEquals(content1, streamContent.get(1));
        assertEquals("", streamContent.get(2));
        assertEquals("", streamContent.get(3));
        assertEquals(content3, streamContent.get(4));
        assertEquals("", streamContent.get(5));

    }

    @Test
    public void testNonExistingFile() throws Exception
    {
        File file = new File(workingDirectory, "f.txt");
        IHierarchicalContentNode content = new FileBasedContentNode(file);

        ConcatenatedContentInputStream stream = new ConcatenatedContentInputStream(false, content);
        try
        {
            readStrings(stream);
            fail("IOExceptionUnchecked expected");
        } catch (IOExceptionUnchecked ex)
        {
            assertEquals(file + " (No such file or directory)", ex.getCause().getMessage());
        }
    }

    @Test
    public void testNullFile() throws IOException
    {
        ConcatenatedContentInputStream stream =
                new ConcatenatedContentInputStream(false, new IHierarchicalContentNode[]
                { null });
        try
        {
            readStrings(stream);
            fail("NullPointerException expected");
        } catch (NullPointerException ex)
        {
        }
    }

    // --------- helpers

    private static List<String> readStrings(ConcatenatedContentInputStream stream)
            throws IOException
    {
        ConcatenatedFileOutputStreamWriter reader = new ConcatenatedFileOutputStreamWriter(stream);
        List<String> result = new ArrayList<String>();
        while (true)
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            long blockSize = reader.writeNextBlock(out);
            if (blockSize == -1)
            {
                break;
            }
            result.add(out.toString());
        }
        return result;
    }

    private IHierarchicalContentNode createContent(String content, String name)
    {
        return new ByteArrayBasedContentNode(content.getBytes(), name);
    }

    private static String createLongString(String text)
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 9000; i++)
        {
            sb.append(text);
        }
        sb.append("\n");
        return sb.toString();
    }
}
