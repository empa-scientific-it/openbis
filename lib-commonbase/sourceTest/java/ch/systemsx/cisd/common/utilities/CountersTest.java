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

package ch.systemsx.cisd.common.utilities;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * @author Franz-Josef Elmer
 */
public class CountersTest extends AssertJUnit
{
    @Test
    public void test()
    {
        Counters<String> counters = new Counters<String>();

        assertEquals(0, counters.getNumberOfDifferentObjectsCounted());
        assertEquals(0, counters.getCountOf("a"));
        assertEquals(1, counters.count("a"));
        assertEquals(1, counters.getNumberOfDifferentObjectsCounted());
        assertEquals(1, counters.getCountOf("a"));
        assertEquals(1, counters.count("b"));
        assertEquals(1, counters.getCountOf("a"));
        assertEquals(1, counters.getCountOf("b"));
        assertEquals(2, counters.count("a"));
        assertEquals(3, counters.count("a"));
        assertEquals(3, counters.getCountOf("a"));
        assertEquals(1, counters.getCountOf("b"));
        assertEquals(2, counters.getNumberOfDifferentObjectsCounted());
    }
}
