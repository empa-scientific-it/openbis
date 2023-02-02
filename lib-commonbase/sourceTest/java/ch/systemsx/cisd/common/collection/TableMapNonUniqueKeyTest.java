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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import static org.testng.AssertJUnit.*;

import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.collection.IMultiKeyExtractor;
import ch.systemsx.cisd.common.collection.TableMapNonUniqueKey;

/**
 * Test cases for the {@link TableMapNonUniqueKey}
 * 
 * @author Bernd Rinn
 */
public class TableMapNonUniqueKeyTest
{

    final IMultiKeyExtractor<Integer, String> integerExtractor = new IMultiKeyExtractor<Integer, String>()
        {
            @Override
            public Collection<Integer> getKey(String e)
            {
                final int i = e.indexOf(':');
                if (i >= 0)
                {
                    String intStr = e.substring(i + 1);
                    String[] intStrs = StringUtils.split(intStr, ',');
                    Collection<Integer> ints = new ArrayList<Integer>(intStrs.length);
                    for (String inStr : intStrs)
                    {
                        ints.add(Integer.parseInt(inStr));
                    }
                    return ints;
                } else
                {
                    return Collections.singleton(Integer.parseInt(e));
                }
            }
        };

    @Test
    public void testIterationUniqueKey()
    {
        final TableMapNonUniqueKey<Integer, String> tableMap =
                new TableMapNonUniqueKey<Integer, String>(Arrays.asList("1", "7", "0"),
                        integerExtractor);
        Iterator<String> it = tableMap.iterator();
        assertEquals("1", it.next());
        assertEquals("7", it.next());
        assertEquals("0", it.next());
        assertFalse(it.hasNext());
    }

    @Test(expectedExceptions = TableMapNonUniqueKey.UniqueValueViolationException.class)
    public void testIterationDuplicateValuesStrategyError()
    {
        new TableMapNonUniqueKey<Integer, String>(Arrays.asList("1", "7", "0", "0", "1"),
                integerExtractor);
    }

    @Test
    public void testIterationDuplicateValuesKeepLastStrategy()
    {
        final String null1 = new String(Integer.toString(0));
        final String null2 = new String(Integer.toString(0));
        final TableMapNonUniqueKey<Integer, String> tableMap =
                new TableMapNonUniqueKey<Integer, String>(Arrays
                        .asList("1", "7", null1, null2, "1"), integerExtractor,
                        TableMapNonUniqueKey.UniqueValueViolationStrategy.KEEP_LAST);
        Iterator<String> it = tableMap.iterator();
        assertEquals("1", it.next());
        assertEquals("7", it.next());
        final String null3 = it.next();
        System.out.println(System.identityHashCode(null1) + ":" + System.identityHashCode(null2)
                + ":" + System.identityHashCode(null3));
        assertEquals(System.identityHashCode(null2), System.identityHashCode(null3));
        assertFalse(it.hasNext());
    }

    @Test
    public void testIterationDuplicateValuesKeepFirstStrategy()
    {
        final String null1 = new String(Integer.toString(0));
        final String null2 = new String(Integer.toString(0));
        final TableMapNonUniqueKey<Integer, String> tableMap =
                new TableMapNonUniqueKey<Integer, String>(Arrays
                        .asList("1", "7", null1, null2, "1"), integerExtractor,
                        TableMapNonUniqueKey.UniqueValueViolationStrategy.KEEP_FIRST);
        Iterator<String> it = tableMap.iterator();
        assertEquals("1", it.next());
        assertEquals("7", it.next());
        final String null3 = it.next();
        System.out.println(System.identityHashCode(null1) + ":" + System.identityHashCode(null2)
                + ":" + System.identityHashCode(null3));
        assertEquals(System.identityHashCode(null1), System.identityHashCode(null3));
        assertFalse(it.hasNext());
    }

    @Test
    public void testIterationDuplicateKey()
    {
        final TableMapNonUniqueKey<Integer, String> tableMap =
                new TableMapNonUniqueKey<Integer, String>(Arrays
                        .asList("1", "7", "0", "x:0", "x:1"), integerExtractor);
        Iterator<String> it = tableMap.iterator();
        assertEquals("1", it.next());
        assertEquals("x:1", it.next());
        assertEquals("7", it.next());
        assertEquals("0", it.next());
        assertEquals("x:0", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testTryGet()
    {
        final TableMapNonUniqueKey<Integer, String> tableMap =
                new TableMapNonUniqueKey<Integer, String>(Arrays.asList("1", "7", "0"),
                        integerExtractor);
        assertNull(tableMap.tryGet(10));
        assertEquals(Collections.singleton("0"), tableMap.tryGet(0));
        assertEquals(Collections.singleton("1"), tableMap.tryGet(1));
        assertEquals(Collections.singleton("7"), tableMap.tryGet(7));
    }

    @Test
    public void testTryGetNonUnique()
    {
        final TableMapNonUniqueKey<Integer, String> tableMap =
                new TableMapNonUniqueKey<Integer, String>(Arrays.asList("a:42", "7", "b:42", "0",
                        "b:7", "c:42"), integerExtractor);
        assertNull(tableMap.tryGet(10));
        assertEquals(Collections.singleton("0"), tableMap.tryGet(0));
        assertEquals(new HashSet<String>(Arrays.asList("7", "b:7")), tableMap.tryGet(7));
        assertEquals(new HashSet<String>(Arrays.asList("a:42", "b:42", "c:42")), tableMap
                .tryGet(42));
    }

    @Test
    public void testTryGetNonUniqueMultiKey()
    {
        final TableMapNonUniqueKey<Integer, String> tableMap =
                new TableMapNonUniqueKey<Integer, String>(Arrays.asList("a:42,8", "7", "b:42", "0",
                        "b:7", "c:42"), integerExtractor);
        assertNull(tableMap.tryGet(10));
        assertEquals(Collections.singleton("0"), tableMap.tryGet(0));
        assertEquals(new HashSet<String>(Arrays.asList("7", "b:7")), tableMap.tryGet(7));
        assertEquals(new HashSet<String>(Arrays.asList("a:42,8", "b:42", "c:42")), tableMap
                .tryGet(42));
        assertEquals(new HashSet<String>(Arrays.asList("a:42,8")), tableMap
                .tryGet(8));
    }

}
