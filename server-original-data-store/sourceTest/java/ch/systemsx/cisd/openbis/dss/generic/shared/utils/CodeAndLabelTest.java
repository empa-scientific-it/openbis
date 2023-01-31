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

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.CodeNormalizer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;

/**
 * @author Franz-Josef Elmer
 */
public class CodeAndLabelTest extends AssertJUnit
{
    @Test
    public void testNormalize()
    {
        assertNormalized("", "");
        assertNormalized("ABC_123", "Abc=123");
    }

    private void assertNormalized(String expectedNormalizedCode, String code)
    {
        assertEquals(expectedNormalizedCode, CodeNormalizer.normalize(code));
        assertEquals(expectedNormalizedCode,
                CodeNormalizer.normalize(CodeNormalizer.normalize(code)));
    }

    @Test
    public void testConstructor()
    {
        assertCodeAndTitle("ABC_1_2-3_4", "abc", CodeAndLabelUtil.create("<abc?1=2-3+4> abc"));
        assertCodeAndTitle("ABC_123_", "abc<123>", CodeAndLabelUtil.create("abc<123>"));
        assertCodeAndTitle("ABC", "ABC", CodeAndLabelUtil.create("<abc> "));
    }

    private void assertCodeAndTitle(String expectedCode, String expectedTitle,
            CodeAndLabel codeAndTitle)
    {
        assertEquals(expectedCode, codeAndTitle.getCode());
        assertEquals(expectedTitle, codeAndTitle.getLabel());
    }
}
