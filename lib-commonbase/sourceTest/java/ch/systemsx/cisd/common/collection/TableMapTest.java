/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.collection;

import java.util.Arrays;
import java.util.Iterator;

import static org.testng.AssertJUnit.*;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.collection.IKeyExtractor;
import ch.systemsx.cisd.common.collection.TableMap;
import ch.systemsx.cisd.common.collection.TableMap.UniqueKeyViolationStrategy;

/**
 * Test cases for the {@link TableMap}
 * 
 * @author Bernd Rinn
 */
public class TableMapTest
{

    final IKeyExtractor<Integer, String> integerExtractor = new IKeyExtractor<Integer, String>()
        {
            @Override
            public Integer getKey(String e)
            {
                final int i = e.indexOf(':');
                if (i >= 0)
                {
                    return Integer.parseInt(e.substring(i + 1));
                } else
                {
                    return Integer.parseInt(e);
                }
            }
        };

    @Test
    public void testIteration()
    {
        final TableMap<Integer, String> tableMap =
                new TableMap<Integer, String>(Arrays.asList("1", "7", "0"), integerExtractor);
        Iterator<String> it = tableMap.iterator();
        assertEquals("1", it.next());
        assertEquals("7", it.next());
        assertEquals("0", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testTryGet()
    {
        final TableMap<Integer, String> tableMap =
                new TableMap<Integer, String>(Arrays.asList("1", "7", "0"), integerExtractor);
        assertNull(tableMap.tryGet(10));
        assertEquals("0", tableMap.tryGet(0));
        assertEquals("1", tableMap.tryGet(1));
        assertEquals("7", tableMap.tryGet(7));
    }

    @Test(expectedExceptions = TableMap.UniqueKeyViolationException.class)
    public void testUniqueKeyViolationError()
    {
        final TableMap<Integer, String> tableMap =
                new TableMap<Integer, String>(Arrays.asList("1", "7", "0", "1"), integerExtractor);
        assertNull(tableMap.tryGet(10));
        assertEquals("0", tableMap.tryGet(0));
        assertEquals("1", tableMap.tryGet(1));
        assertEquals("7", tableMap.tryGet(7));
    }

    @Test
    public void testUniqueKeyViolationKeepFirst()
    {
        final TableMap<Integer, String> tableMap =
                new TableMap<Integer, String>(Arrays.asList("a:1", "7", "0", "b:1"),
                        integerExtractor, UniqueKeyViolationStrategy.KEEP_FIRST);
        assertNull(tableMap.tryGet(10));
        assertEquals("0", tableMap.tryGet(0));
        assertEquals("a:1", tableMap.tryGet(1));
        assertEquals("7", tableMap.tryGet(7));
    }

    @Test
    public void testUniqueKeyViolationKeepLast()
    {
        final TableMap<Integer, String> tableMap =
                new TableMap<Integer, String>(Arrays.asList("a:1", "7", "0", "b:1"),
                        integerExtractor, UniqueKeyViolationStrategy.KEEP_LAST);
        assertNull(tableMap.tryGet(10));
        assertEquals("0", tableMap.tryGet(0));
        assertEquals("b:1", tableMap.tryGet(1));
        assertEquals("7", tableMap.tryGet(7));
    }

}
