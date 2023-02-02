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
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for corresponding {@link DefaultLineTokenizer} class.
 * 
 * @author Christian Ribeaud
 */
public final class DefaultLineTokenizerTest
{
    private DefaultLineTokenizer tokenizer;

    @BeforeClass
    public final void initTokenizer()
    {
        tokenizer = new DefaultLineTokenizer();
        tokenizer.init();
    }

    @Test
    public final void testTokenize()
    {
        // Default separator is '\t'
        assertNotNull(tokenizer);
        String line = "This\tis\ta\tline";
        String[] tokens = tokenizer.tokenize(line);
        assertTrue(tokens.length == 4);
        assertEquals(tokens[0], "This");
        assertEquals(tokens[1], "is");
        assertEquals(tokens[2], "a");
        assertEquals(tokens[3], "line");
        // Trim is whitespace
        line = " This\t is \t a \tline ";
        tokens = tokenizer.tokenize(line);
        assertTrue(tokens.length == 4);
        assertEquals(tokens[0], "This");
        assertEquals(tokens[1], "is");
        assertEquals(tokens[2], "a");
        assertEquals(tokens[3], "line");
        // Separators are " \t"
        tokenizer.setProperty(DefaultLineTokenizer.PropertyKey.SEPARATOR_CHARS, " \t");
        line = "This is \ta\tline";
        tokens = tokenizer.tokenize(line);
        assertTrue(tokens.length == 5);
        assertEquals(tokens[0], "This");
        assertEquals(tokens[1], "is");
        assertEquals(tokens[2], "");
        assertEquals(tokens[3], "a");
        assertEquals(tokens[4], "line");
        // Trying quote characters
        tokenizer.setProperty(DefaultLineTokenizer.PropertyKey.QUOTE_CHARS, "'");
        line = "'This rule'\t'is ''certainly not'''\ta\tline";
        tokens = tokenizer.tokenize(line);
        assertTrue(tokens.length == 4);
        assertEquals(tokens[0], "This rule");
        assertEquals(tokens[1], "is 'certainly not'");
        assertEquals(tokens[2], "a");
        assertEquals(tokens[3], "line");
        // Trying to set <code>null</code>
        tokenizer.setProperty(null, null);
        // Resetting to default values
        tokenizer.setProperty(DefaultLineTokenizer.PropertyKey.QUOTE_CHARS, null);
        tokenizer.setProperty(DefaultLineTokenizer.PropertyKey.SEPARATOR_CHARS, null);
        line = " This\t is \t a \tline ";
        tokens = tokenizer.tokenize(line);
        assertTrue(tokens.length == 4);
        assertEquals(tokens[0], "This");
        assertEquals(tokens[1], "is");
        assertEquals(tokens[2], "a");
        assertEquals(tokens[3], "line");
    }
}