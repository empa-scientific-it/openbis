/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic;

import java.util.Arrays;
import java.util.Collection;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.translator.GridCustomExpressionTranslator.GridCustomFilterTranslator;

/**
 * Test cases for {@link GridCustomFilterTranslator}.
 * 
 * @author Izabela Adamczyk
 */
public class ExpressionUtilTest extends AssertJUnit
{
    @Test
    public void testExtractNoParameters() throws Exception
    {
        String expression = "";
        assertEquals("[]", extractParameters(expression).toString());
    }

    @Test
    public void testExtractOneParameter() throws Exception
    {
        String expression = "hello ${abc}";
        assertEquals("[abc]", extractParameters(expression).toString());
    }

    @Test
    public void testExtractOneDuplicatedParameter() throws Exception
    {
        String expression = "${abc} and ${abc}";
        assertEquals("[abc, abc]", extractParameters(expression).toString());
    }

    @Test
    public void testExtractManyParametersWithPreservedOrder() throws Exception
    {
        String expression = "${abc} ${def} ${abc} ${ghi}.";
        assertEquals("[abc, def, abc, ghi]", extractParameters(expression).toString());
    }

    @Test
    public void testCreateDistinctParametersList()
    {
        assertEquals("[a, b, c]", createDistinctList("a", "b", "a", "c", "b").toString());
    }

    private Collection<String> createDistinctList(String... strings)
    {
        return ExpressionUtil.createDistinctParametersList(Arrays.asList(strings));
    }

    private Collection<String> extractParameters(String expression)
    {
        return ExpressionUtil.extractParameters(expression);
    }

}
