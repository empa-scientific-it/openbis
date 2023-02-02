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

package ch.systemsx.cisd.common.parser;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.parser.filter.ExcludeEmptyAndCommentLineFilter;
import ch.systemsx.cisd.common.parser.filter.NonEmptyLineFilter;

/**
 * Test cases for corresponding {@link ParserUtilities} class.
 * 
 * @author Christian Ribeaud
 */
public final class ParserUtilitiesTest
{

    private static final File unitTestRootDirectory = new File("targets" + File.separator
            + "unit-test-wd");

    private static final File workingDirectory = new File(unitTestRootDirectory,
            "ParserUtilitiesTest");

    @BeforeClass
    public final void setUp()
    {
        LogInitializer.init();
        workingDirectory.mkdirs();
        assert workingDirectory.isDirectory();
    }

    @AfterClass
    public void tearDown() throws IOException
    {
        FileUtils.deleteDirectory(workingDirectory);
        workingDirectory.deleteOnExit();
    }

    @Test
    public final void testGetFirstAcceptedLineWithProblematicValues()
    {
        File file = new File(workingDirectory, "doesNotExist");
        boolean exceptionThrown = false;
        try
        {
            ParserUtilities.tryGetFirstAcceptedLine((File) null, null);
        } catch (AssertionError e)
        {
            exceptionThrown = true;
        }
        assertTrue("Given file can not be null.", exceptionThrown);

        exceptionThrown = false;
        assert file.exists() == false;
        try
        {
            ParserUtilities.tryGetFirstAcceptedLine(file, null);
        } catch (IOExceptionUnchecked e)
        {
            exceptionThrown = true;
        }
        assertTrue("Given file must exist.", exceptionThrown);
    }

    @Test
    public final void testGetFirstAcceptedLineWithNullILineFilter() throws IOException
    {
        String[] lines = new String[]
        { StringUtils.EMPTY, "non-empty line", StringUtils.EMPTY, "hello" };
        File file = new File(workingDirectory, "test.txt");
        FileUtils.writeLines(file, Arrays.asList(lines));
        ILine<String> line = ParserUtilities.tryGetFirstAcceptedLine(file, null);
        assertEquals(StringUtils.EMPTY, line.getText());
        assertEquals(0, line.getNumber());
        assert file.delete();
    }

    @Test
    public final void testGetAllAcceptedLinesWithNullILineFilter() throws IOException
    {
        String[] lines = new String[]
        { StringUtils.EMPTY, "non-empty line", StringUtils.EMPTY, "hello" };
        File file = new File(workingDirectory, "test.txt");
        FileUtils.writeLines(file, Arrays.asList(lines));
        ParserUtilities.LineSplitter splitter =
                new ParserUtilities.LineSplitter(file, NonEmptyLineFilter.INSTANCE);
        try
        {
            ILine<String> line = splitter.tryNextLine();
            assertEquals("non-empty line", line.getText());
            assertEquals(1, line.getNumber());
            line = splitter.tryNextLine();
            assertEquals("hello", line.getText());
            assertEquals(3, line.getNumber());
            assertNull(splitter.tryNextLine());
            assert file.delete();
        } finally
        {
            splitter.close();
        }
    }

    @Test
    public final void testGetFirstAcceptedLine() throws IOException
    {
        String[] lines = new String[]
        { StringUtils.EMPTY, "# comment line", StringUtils.EMPTY, "hello" };
        File file = new File(workingDirectory, "test.txt");
        FileUtils.writeLines(file, Arrays.asList(lines));
        ILine<String> line =
                ParserUtilities.tryGetFirstAcceptedLine(file,
                        ExcludeEmptyAndCommentLineFilter.INSTANCE);
        assertEquals("hello", line.getText());
        assertEquals(3, line.getNumber());
        assert file.delete();
    }
}
