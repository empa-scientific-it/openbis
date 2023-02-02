/*
 * Copyright 2007 ETH Zuerich, CISD
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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.collection.IFromStringConverter;
import ch.systemsx.cisd.common.collection.IToStringConverter;
import ch.systemsx.cisd.common.io.CollectionIO;

/**
 * Test cases for the {@link CollectionIO} class.
 * 
 * @author Bernd Rinn
 */
public final class CollectionIOTest extends AbstractFileSystemTestCase
{

    @Test
    public void writeCollection() throws IOException
    {
        final File file = new File(workingDirectory, "test.list");
        file.delete();
        file.deleteOnExit();
        final List<String> list = Arrays.asList("Hund", "Katze", "Maus");
        assert CollectionIO.writeIterable(file, list);
        final BufferedReader reader = new BufferedReader(new FileReader(file));
        try
        {
            assert list.get(0).equals(reader.readLine());
            assert list.get(1).equals(reader.readLine());
            assert list.get(2).equals(reader.readLine());
            assert reader.readLine() == null;
        } finally
        {
            reader.close();
        }
    }

    @Test
    public void writeCollectionWithConverter() throws IOException
    {
        final File file = new File(workingDirectory, "test.list");
        file.delete();
        file.deleteOnExit();
        final List<String> list = Arrays.asList("Hund", "Katze", "Maus");
        assert CollectionIO.writeIterable(file, list, new IToStringConverter<String>()
            {
                @Override
                public String toString(String value)
                {
                    return value.toUpperCase();
                }
            });
        final BufferedReader reader = new BufferedReader(new FileReader(file));
        try
        {
            assert list.get(0).toUpperCase().equals(reader.readLine());
            assert list.get(1).toUpperCase().equals(reader.readLine());
            assert list.get(2).toUpperCase().equals(reader.readLine());
            assert reader.readLine() == null;
        } finally
        {
            reader.close();
        }
    }

    @Test
    public void writeCollectionFailure() throws IOException
    {
        final File file = new File(workingDirectory, "readonly-test.list");
        file.delete();
        FileUtils.touch(file);
        assert file.length() == 0;
        file.setReadOnly();
        final List<String> list = Arrays.asList("Hund", "Katze", "Maus");
        assert CollectionIO.writeIterable(file, list) == false;
        assert file.length() == 0;
    }

    @Test
    public void writeCollectionToOutputStream()
    {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        assert CollectionIO.writeIterable(os, Arrays.asList("One", "Two", "Three"));
        final PrintStream ps = new PrintStream(os);
        ps.println("Still open");
        assert ps.checkError() == false;
        final String resultingString = new String(os.toByteArray());
        final String lineSeparator = System.getProperty("line.separator");
        assert resultingString.equals(String.format("One%1$sTwo%1$sThree%1$sStill open%1$s",
                lineSeparator));
        assert CollectionIO.writeIterable(ps, Arrays.asList("yellow"));
        assert ps.checkError() == false;
        ps.println("Still open");
        final String resultingString2 = new String(os.toByteArray());
        assert resultingString2.equals(String.format(
                "One%1$sTwo%1$sThree%1$sStill open%1$syellow%1$sStill open%1$s", lineSeparator));
    }

    @Test
    public void readCollection() throws IOException
    {
        final File file = new File(workingDirectory, "test.list");
        final PrintWriter writer = new PrintWriter(file);
        try
        {
            writer.println("gelb");
            writer.println("gruen");
            writer.println("rot");
        } finally
        {
            writer.close();
        }
        final List<String> list = new ArrayList<String>();
        assert CollectionIO.readCollection(file, list);
        assert list.size() == 3;
        assert "gelb".equals(list.get(0));
        assert "gruen".equals(list.get(1));
        assert "rot".equals(list.get(2));
    }

    @Test
    public void readCollectionToByteArray() throws IOException
    {
        final ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        final PrintWriter writer = new PrintWriter(ostream);
        try
        {
            writer.println("gelb");
            writer.println("gruen");
            writer.println("rot");
        } finally
        {
            writer.close();
        }
        final byte[] buffer = ostream.toByteArray();
        final List<String> list = new ArrayList<String>();
        assert CollectionIO.readCollection(new ByteArrayInputStream(buffer), list);
        assert list.size() == 3;
        assert "gelb".equals(list.get(0));
        assert "gruen".equals(list.get(1));
        assert "rot".equals(list.get(2));
    }

    @Test
    public void readCollectionWithConverter() throws IOException
    {
        final File file = new File(workingDirectory, "test.list");
        final PrintWriter writer = new PrintWriter(file);
        try
        {
            writer.println("5.4");
            writer.println("-2");
            writer.println("9.99999");
        } finally
        {
            writer.close();
        }
        final List<Float> list = new ArrayList<Float>();
        assert CollectionIO.readCollection(file, list, new IFromStringConverter<Float>()
            {
                @Override
                public Float fromString(String value)
                {
                    return Float.valueOf(value);
                }
            });
        assert list.size() == 3;
        assertEquals(5.4f, list.get(0), 1e-7f);
        assertEquals(-2.0f, list.get(1), 1e-7f);
        assertEquals(9.99999f, list.get(2), 1e-7f);
    }

    @Test
    public void readCollectionFailure() throws IOException
    {
        final File file = new File(workingDirectory, "test.list");
        file.delete();
        final List<String> list = new ArrayList<String>();
        assert CollectionIO.readCollection(file, list) == false;
        assert list.isEmpty();
    }

    @Test
    public void readList() throws IOException
    {
        final File file = new File(workingDirectory, "test.list");
        final PrintWriter writer = new PrintWriter(file);
        try
        {
            writer.println("gelb");
            writer.println("gruen");
            writer.println("rot");
        } finally
        {
            writer.close();
        }
        final List<String> list = CollectionIO.readList(file);
        assert list != null;
        assert list.size() == 3;
        assert "gelb".equals(list.get(0));
        assert "gruen".equals(list.get(1));
        assert "rot".equals(list.get(2));
    }

    @Test
    public void readSet() throws IOException
    {
        final File file = new File(workingDirectory, "test.list");
        final PrintWriter writer = new PrintWriter(file);
        try
        {
            writer.println("gelb");
            writer.println("gruen");
            writer.println("rot");
            writer.println("gelb");
        } finally
        {
            writer.close();
        }
        final Set<String> set = CollectionIO.readSet(file);
        assert set != null;
        assert set.size() == 3;
        assert set.contains("gelb");
        assert set.contains("gruen");
        assert set.contains("rot");
    }

}
