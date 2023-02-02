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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.string.StringUtilities;

/**
 * Test cases for the {@link CollectionUtils} class.
 * 
 * @author Christian Ribeaud
 */
public final class CollectionUtilsTest
{

    @Test
    public final void testAbbreviateWithEllipsesAndNoLeft()
    {
        final String[] s = new String[]
        { "1", "2", "3", "4", "5" };
        assertEquals("[1, 2, 3, ...]", CollectionUtils.abbreviate(s, 3, false));
        assertEquals("[1, ...]", CollectionUtils.abbreviate(s, 1, false));
        assertEquals("[]", CollectionUtils.abbreviate(s, 0, false));
    }

    @Test
    public final void testAbbreviateWithLeft()
    {
        final String[] s = new String[]
        { "1", "2", "3", "4", "5" };
        assertEquals("[]", CollectionUtils.abbreviate(s, 0));
        assertEquals("[1, ... (4 left)]", CollectionUtils.abbreviate(s, 1));
        assertEquals("[1, 2, ... (3 left)]", CollectionUtils.abbreviate(s, 2));
        assertEquals(CollectionUtils.abbreviate(s, 10, false), CollectionUtils.abbreviate(s, 10));
    }

    @Test
    public final void testAbbreviateWithArray()
    {
        final String[] s = StringUtilities.getStrings(5);
        final CollectionStyle collectionStyle = CollectionStyle.DEFAULT;
        String string =
                collectionStyle.getCollectionStart()
                        + StringUtils.join(s, collectionStyle.getCollectionSeparator())
                        + collectionStyle.getCollectionEnd();
        assertEquals(string, CollectionUtils.abbreviate(s, -1, false));
        assertEquals(string, CollectionUtils.abbreviate(s, -10, false));
        assertEquals(string, CollectionUtils.abbreviate(s, 5, false));
        assertEquals(string, CollectionUtils.abbreviate(s, 10, false));
    }

    @Test
    public final void testAbbreviateWithError()
    {
        final Object[] objects = new Object[0];

        boolean exceptionThrown = false;
        try
        {
            CollectionUtils.abbreviate((Collection<?>) null, 0, false);
        } catch (AssertionError e)
        {
            exceptionThrown = true;
        }
        assertTrue("Given list can not be null.", exceptionThrown);

        exceptionThrown = false;
        try
        {
            CollectionUtils.abbreviate(objects, 0, false, ToStringDefaultConverter.getInstance(),
                    null);
        } catch (AssertionError e)
        {
            exceptionThrown = true;
        }
        assertTrue("Given CollectionStyle can not be null.", exceptionThrown);

        exceptionThrown = false;
        try
        {
            CollectionUtils.abbreviate(objects, 0, null);
        } catch (AssertionError e)
        {
            exceptionThrown = true;
        }
        assertTrue("Given CollectionStyle can not be null.", exceptionThrown);

        exceptionThrown = false;
        try
        {
            CollectionUtils.abbreviate((Collection<?>) null, 0, false, null);
        } catch (AssertionError e)
        {
            exceptionThrown = true;
        }
        assertTrue("IToStringConverter can not be null.", exceptionThrown);
    }

    @Test
    public void testSort()
    {
        IKeyExtractor<Integer, String> lengthExtractor = new IKeyExtractor<Integer, String>()
            {

                @Override
                public Integer getKey(String e)
                {
                    return e.length();
                }
            };

        List<String> strings = Arrays.asList("bbbb", "z", "ccc", "aa");
        List<String> sortedByLength = Arrays.asList("z", "aa", "ccc", "bbbb");

        CollectionUtils.sort(strings, lengthExtractor);
        assertEquals(sortedByLength, strings);
    }
}
