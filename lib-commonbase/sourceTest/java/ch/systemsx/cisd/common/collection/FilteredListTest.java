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

package ch.systemsx.cisd.common.collection;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.collection.FilteredList;
import ch.systemsx.cisd.common.collection.IValidator;
import ch.systemsx.cisd.common.collection.ValidatorUtils;

/**
 * Tests for {@link FilteredList}.
 * 
 * @author Christian Ribeaud
 */
public final class FilteredListTest
{

    @Test
    public final void testDecorate()
    {
        boolean exceptionThrown = false;
        try
        {
            FilteredList.decorate(new ArrayList<String>(), null);
        } catch (AssertionError e)
        {
            exceptionThrown = true;
        }
        assertTrue("Neither list nor validator can be null", exceptionThrown);
        exceptionThrown = false;
        try
        {
            FilteredList.decorate((List<Object>) null, ValidatorUtils.getNotNullValidator());
        } catch (AssertionError e)
        {
            exceptionThrown = true;
        }
        assertTrue("Neither list nor validator can be null", exceptionThrown);
    }

    private final static List<String> createList()
    {
        final IValidator<String> validator = ValidatorUtils.getNotNullValidator();
        List<String> list = new ArrayList<String>();
        list.add(null);
        list.add(null);
        list.add("0");
        list.add("1");
        list.add(null);
        list.add(null);
        list.add("2");
        list.add(null);
        assertEquals(8, list.size());
        List<String> decoratedList = FilteredList.decorate(list, validator);
        assertEquals(3, list.size());
        return decoratedList;
    }

    @Test
    public final void testSet()
    {
        List<String> list = createList();
        try
        {
            list.set(5, "5");
            fail("IndexOutOfBoundsException not thrown.");
        } catch (IndexOutOfBoundsException ex)
        {
            String errorMessage = null;
            if(System.getProperty("java.version").startsWith("1.8.")) {
                errorMessage = "Index: 5, Size: 3";
            } else {
                errorMessage = "Index 5 out of bounds for length 3";
            }
            assertEquals(errorMessage, ex.getMessage());
        }
        String old = list.set(2, "new2");
        assertEquals("2", old);
        assertEquals("new2", list.get(2));
    }

    @Test
    public final void testAddAll()
    {
        // addAll(List)
        List<String> list = createList();
        List<String> newList = new ArrayList<String>();
        newList.add("one");
        newList.add(null);
        newList.add("two");
        newList.add(null);
        newList.add(null);
        newList.add("thre");
        newList.add(null);
        assertEquals(7, newList.size());
        list.addAll(newList);
        assertEquals(6, list.size());
        assertEquals("1", list.get(1));
        assertEquals("one", list.get(3));
        // addAll(int, List)
        list = createList();
        list.addAll(1, newList);
        assertEquals(6, list.size());
        assertEquals("one", list.get(1));
        assertEquals("1", list.get(4));
    }

    @Test
    public final void testAdd()
    {
        // add(Object)
        List<String> list = createList();
        list.add(null);
        assertEquals(3, list.size());
        list.add("new");
        assertEquals(4, list.size());
        assertEquals("new", list.get(3));
        // add(int, Object)
        try
        {
            list.add(5, "renew");
        } catch (IndexOutOfBoundsException ex)
        {
            assertEquals("Index: 5, Size: 4", ex.getMessage());
        }
        list.add(4, "renew");
        assertEquals(5, list.size());
        assertEquals("renew", list.get(4));
    }

    @Test
    public final void testSubList()
    {
        List<String> list = createList();
        List<String> subList = list.subList(0, 2);
        assertEquals(2, subList.size());
        assertEquals(list.get(0), subList.get(0));
        assertEquals(list.get(1), subList.get(1));
    }
}