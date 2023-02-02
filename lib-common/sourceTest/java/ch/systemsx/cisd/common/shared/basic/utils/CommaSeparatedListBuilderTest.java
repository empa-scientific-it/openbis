/*
 * Copyright 2012 ETH Zuerich, CISD
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

import java.util.Arrays;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.shared.basic.string.CommaSeparatedListBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class CommaSeparatedListBuilderTest extends AssertJUnit
{

    @Test
    public void testNoObjectsAppended()
    {
        CommaSeparatedListBuilder builder = new CommaSeparatedListBuilder();

        assertEquals("", builder.toString());
    }

    @Test
    public void testOneObjectAppended()
    {
        CommaSeparatedListBuilder builder = new CommaSeparatedListBuilder();
        builder.append("hello");

        assertEquals("hello", builder.toString());
    }

    @Test
    public void testTwoObjectsAppended()
    {
        CommaSeparatedListBuilder builder = new CommaSeparatedListBuilder();
        builder.append("hello");
        builder.append(null);

        assertEquals("hello, null", builder.toString());
    }

    @Test
    public void testToStringArray()
    {
        assertEquals("hello, 42", CommaSeparatedListBuilder.toString("hello", 42));
    }

    @Test
    public void testToStringList()
    {
        assertEquals("hello, 42", CommaSeparatedListBuilder.toString(Arrays.<Object> asList("hello", 42)));
    }

}
