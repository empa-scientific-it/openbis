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

package ch.systemsx.cisd.common.shared.basic.utils;

import java.util.List;

import static org.testng.AssertJUnit.*;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;

/**
 * Tests for {@link StringUtils}.
 *
 * @author Bernd Rinn
 */
public class StringUtilsTest
{
    @Test
    public void testTokenize()
    {
        List<String> result = StringUtils.tokenize("a b c");
        assertEquals(3, result.size());
        assertEquals("a", result.get(0));
        assertEquals("b", result.get(1));
        assertEquals("c", result.get(2));

        result = StringUtils.tokenize("  a  b   c ");
        assertEquals(3, result.size());
        assertEquals("a", result.get(0));
        assertEquals("b", result.get(1));
        assertEquals("c", result.get(2));

        result = StringUtils.tokenize("  eins  zwei   drei vier  ");
        assertEquals(4, result.size());
        assertEquals("eins", result.get(0));
        assertEquals("zwei", result.get(1));
        assertEquals("drei", result.get(2));
        assertEquals("vier", result.get(3));

        result = StringUtils.tokenize("  'eins  zwei'   'drei vier'  ");
        assertEquals(2, result.size());
        assertEquals("eins  zwei", result.get(0));
        assertEquals("drei vier", result.get(1));

        result = StringUtils.tokenize("  \"eins  zwei\"   \"drei vier\"  ");
        assertEquals(2, result.size());
        assertEquals("eins  zwei", result.get(0));
        assertEquals("drei vier", result.get(1));

        result = StringUtils.tokenize("  \"eins  zwei\"   'drei \"vier'  '");
        assertEquals(2, result.size());
        assertEquals("eins  zwei", result.get(0));
        assertEquals("drei \"vier", result.get(1));

        result = StringUtils.tokenize("  \"eins  zwei\"   'drei \"vier'  ' ");
        assertEquals(3, result.size());
        assertEquals("eins  zwei", result.get(0));
        assertEquals("drei \"vier", result.get(1));
        assertEquals(" ", result.get(2));

        result = StringUtils.tokenize("");
        assertEquals(0, result.size());

        result = StringUtils.tokenize("    ");
        assertEquals(0, result.size());

        result = StringUtils.tokenize("    '");
        assertEquals(0, result.size());

        result = StringUtils.tokenize("'    ");
        assertEquals(1, result.size());
        assertEquals("    ", result.get(0));

    }

    @Test
    public void testTokenizeWithEscapedQuotes()
    {
        List<String> result = StringUtils.tokenize("a \\'berta\\' c");
        assertEquals(3, result.size());
        assertEquals("a", result.get(0));
        assertEquals("\\'berta\\'", result.get(1));
        assertEquals("c", result.get(2));

        result = StringUtils.tokenize("a 'berta\\' somemore' c");
        assertEquals(3, result.size());
        assertEquals("a", result.get(0));
        assertEquals("berta\\' somemore", result.get(1));
        assertEquals("c", result.get(2));
    }
}
