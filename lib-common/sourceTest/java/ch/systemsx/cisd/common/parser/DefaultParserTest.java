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
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.testng.annotations.Test;

/**
 * Test cases for corresponding {@link DefaultParser} class.
 * 
 * @author Christian Ribeaud
 */
public final class DefaultParserTest
{
    private final static List<String> text = Arrays.asList("", "# This is a comment",
            "firstName\tlastName\taddress\tcity",
            "Charles\tDarwin\tHumboldt Ave. 1865\t4242 Somewhere",
            "Albert\tEinstein\tNewton Road 1905\t4711 Princton");

    private final static List<String> textWithTab = Arrays.asList("", "# This is a comment",
            "firstName\tlastName\taddress\tcity",
            "Charles\tDarwin\tHumboldt Ave. 1865\t4242 Somewhere", "Albert\t\tNewton Road 1905\t");

    private final static List<String> textWithMissingLastCells = Arrays.asList("",
            "# This is a comment", "firstName\tlastName\taddress\tcity",
            "\tDarwin\tHumboldt Ave. 1865", "Albert\tEinstein");

    private final static List<String> textWithEmptyLines = Arrays.asList("", "# This is a comment",
            "firstName\tlastName\taddress\tcity", "\tDarwin\tHumboldt Ave. 1865", "  \t  \t  \t  ",
            "Albert\tEinstein");

    private final static int HEADER_LENGTH = 4;

    private final static IParser<String[], String> createParser()
    {
        final IParser<String[], String> parser = DefaultParser.createDefaultParser();
        parser.setObjectFactory(IParserObjectFactory.STRING_ARRAY_OBJECT_FACTORY);
        return parser;
    }

    @Test
    public final void testParseWithoutFactoryAndHeader()
    {
        final IParser<String[], String> parser = createParser();
        final List<String[]> result =
                parser.parse(createLineIterator(text), new HeaderLineFilter(), HEADER_LENGTH);
        assertEquals(3, result.size());
        assertEquals(result.get(0)[0], "firstName");
        assertEquals(result.get(1)[1], "Darwin");
        assertEquals(result.get(2)[2], "Newton Road 1905");
        assertEquals(result.get(1)[3], "4242 Somewhere");
    }

    @Test
    public final void testParseIterativelyWithoutFactoryAndHeader()
    {
        final IParser<String[], String> parser = createParser();
        final Iterator<String[]> result =
                parser.parseIteratively(createLineIterator(text), new HeaderLineFilter(),
                        HEADER_LENGTH);
        assertTrue(result.hasNext());
        assertTrue(Arrays.equals(new String[]
        { "firstName", "lastName", "address", "city" }, result.next()));
        assertTrue(result.hasNext());
        assertTrue(Arrays.equals(new String[]
        { "Charles", "Darwin", "Humboldt Ave. 1865", "4242 Somewhere" }, result.next()));
        assertTrue(result.hasNext());
        assertTrue(Arrays.equals(new String[]
        { "Albert", "Einstein", "Newton Road 1905", "4711 Princton" }, result.next()));
        assertFalse(result.hasNext());
    }

    @Test
    public final void testParseWithoutFactoryWithLineFilter()
    {
        final IParser<String[], String> parser = createParser();
        final List<String[]> result =
                parser.parse(createLineIterator(text), new HeaderLineFilter(3), HEADER_LENGTH);
        assertEquals(2, result.size());
        assertEquals(result.get(0)[0], "Charles");
        assertEquals(result.get(1)[1], "Einstein");
    }

    @Test
    public final void testParseFileWithTabs()
    {
        final IParser<String[], String> parser = createParser();
        final List<String[]> result =
                parser.parse(createLineIterator(textWithTab), new HeaderLineFilter(), HEADER_LENGTH);
        assertEquals(3, result.size());
        assertEquals("Albert", result.get(2)[0]);
        assertEquals("", result.get(2)[1]);
        assertEquals("Newton Road 1905", result.get(2)[2]);
        assertEquals("", result.get(2)[3]);
    }

    @Test
    public final void testParseFileWithMissingLastCells()
    {
        final IParser<String[], String> parser = createParser();
        final List<String[]> result =
                parser.parse(createLineIterator(textWithMissingLastCells), new HeaderLineFilter(),
                        HEADER_LENGTH);
        assertEquals(3, result.size());
        assertEquals("", result.get(1)[0]);
        assertEquals("Darwin", result.get(1)[1]);
        assertEquals("Humboldt Ave. 1865", result.get(1)[2]);
        assertEquals("", result.get(1)[3]);
        assertEquals("Albert", result.get(2)[0]);
        assertEquals("Einstein", result.get(2)[1]);
        assertEquals("", result.get(2)[2]);
        assertEquals("", result.get(2)[3]);
    }

    @Test
    public final void testParseEmptyLinesIgnored()
    {
        final IParser<String[], String> parser = createParser();
        final List<String[]> result =
                parser.parse(createLineIterator(textWithEmptyLines), new HeaderLineFilter(),
                        HEADER_LENGTH);
        assertEquals(3, result.size());
        assertEquals("Darwin", result.get(1)[1]);
        assertEquals("Albert", result.get(2)[0]);
    }

    @Test
    public final void testParseWithColumnSizeMismatching()
    {
        final IParser<String[], String> parser = createParser();
        try
        {
            parser.parse(createLineIterator(text), new HeaderLineFilter(3), HEADER_LENGTH + 1);
        } catch (final ColumnSizeMismatchException ex)
        {
            assertEquals(
                    "Line <4> has less columns (4) than the header (5):\n  "
                            + "Charles <TAB> Darwin <TAB> Humboldt Ave. 1865 <TAB> 4242 Somewhere <END_OF_LINE>",
                    ex.getMessage());
        }
    }

    @Test
    public final void testCreateObjectWithParserException()
    {
        final IParser<String[], String> parser =
                new DefaultParser<String[], String>(new DefaultLineTokenizer())
                    {
                        //
                        // DefaultReaderParser
                        //

                        @Override
                        protected final String[] createObject(final String[] tokens)
                                throws ParserException
                        {
                            throw new ParserException("");
                        }
                    };
        parser.setObjectFactory(IParserObjectFactory.STRING_ARRAY_OBJECT_FACTORY);
        try
        {
            parser.parse(createLineIterator(text), new HeaderLineFilter(2), HEADER_LENGTH);
            fail(String.format("'%s' exception expected.", ParsingException.class));
        } catch (final ParsingException ex)
        {
            assertEquals(
                    "Creating an object with following tokens '[firstName, lastName, address, city]' failed.",
                    ex.getMessage());
            assertEquals(3, ex.getLineNumber());
        }
    }

    private Iterator<ILine<String>> createLineIterator(final List<String> lines)
    {
        return new Iterator<ILine<String>>()
            {
                private final Iterator<String> iterator = lines.iterator();

                private int lineNumber;

                @Override
                public void remove()
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public ILine<String> next()
                {
                    return new Line(++lineNumber, iterator.next());
                }

                @Override
                public boolean hasNext()
                {
                    return iterator.hasNext();
                }
            };
    }

}