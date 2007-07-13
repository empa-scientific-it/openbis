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

package ch.systemsx.cisd.common.collections;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;

/**
 * Tests for {@link FilteredList}.
 * 
 * @author Christian Ribeaud
 */
// TODO 2007-07-11, Franz-Josef Elmer: More tests needed. All methods of List have to be tested.
public final class FilteredListTest
{

    @Test
    public final void testDecorate()
    {
        try
        {
            FilteredList.decorate(new ArrayList<String>(), null);
            fail("Neither list nor validator can be null");
        } catch (AssertionError e)
        {
            // Nothing to do here.
        }
        try
        {
            FilteredList.decorate(null, ValidatorUtils.getNotNullValidator());
            fail("Neither list nor validator can be null");
        } catch (AssertionError e)
        {
            // Nothing to do here.
        }
    }

    @Test
    public final void testWithEmptyList()
    {
        final Validator<String> validator = ValidatorUtils.getNotNullValidator();
        List<String> list = FilteredList.decorate(new ArrayList<String>(), validator);
        list.add(null);
        list.add(null);
        list.add("0");
        list.add(null);
        assertEquals(1, list.size());
        try
        {
            list.set(1, "1");
            fail("IndexOutOfBoundsException should be thrown.");
        } catch (IndexOutOfBoundsException e)
        {
            // Nothing to do here.
        }
        String old0 = list.set(0, "newO");
        assertEquals(1, list.size());
        assertEquals("0", old0);
    }

    @Test
    public final void testWithNonEmptyList()
    {
        final Validator<String> validator = ValidatorUtils.getNotNullValidator();
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
        list = FilteredList.decorate(list, validator);
        assertEquals(8, list.size());
        int count = 0;
        for (final String string : list)
        {
            assertEquals(count++ + "", string);
        }
        assertEquals(3, count);
    }
}