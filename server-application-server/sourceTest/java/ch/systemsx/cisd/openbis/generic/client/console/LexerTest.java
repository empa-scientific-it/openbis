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

package ch.systemsx.cisd.openbis.generic.client.console;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * @author Franz-Josef Elmer
 */
public class LexerTest extends AssertJUnit
{
    @Test
    public void test()
    {
        check("[]", "");
        check("[]", " ");
        check("[]", " \t  ");
        check("[alpha]", "alpha");
        check("[alpha]", " alpha");
        check("[alpha]", "\talpha\t");
        check("[alpha]", " alpha\t  ");
        check("[a, b, hello world]", "a  b \"hello world\"");
        check("[a, b, hello \t world ]", " a\tb \"hello \t world \"  ");
        check("[a, b, hello 'world']", "a  b \"hello 'world'\"");
        check("[a=hello world]", "a=\"hello world\"");
    }

    private void check(String expectedTokens, String input)
    {
        assertEquals(expectedTokens, Lexer.extractTokens(input).toString());
    }
}
