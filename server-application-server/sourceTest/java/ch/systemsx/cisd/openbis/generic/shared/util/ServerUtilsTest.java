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

package ch.systemsx.cisd.openbis.generic.shared.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * @author Franz-Josef Elmer
 */
public class ServerUtilsTest extends AssertJUnit
{
    @Test
    public void testExtractSetWithNullArgument()
    {
        assertEquals("[]", ServerUtils.extractSet(null).toString());
    }

    @Test
    public void testExtractSetWithUnassignedProperty()
    {
        assertEquals("[]", ServerUtils.extractSet("${blabla}").toString());
    }

    @Test
    public void testExtractSetWithOneElement()
    {
        assertEquals("[blabla]", ServerUtils.extractSet("   blabla  ").toString());
    }

    @Test
    public void testExtractSetWithThreeElementsOneDuplicated()
    {
        Set<String> set = ServerUtils.extractSet("   a,   b,  a  ");
        ArrayList<String> list = new ArrayList<String>(set);
        Collections.sort(list);
        assertEquals("[a, b]", list.toString());
    }
}
