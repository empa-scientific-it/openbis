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

package ch.systemsx.cisd.common.test;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jmock.api.ExpectationError;

/**
 * Utilities for making assertions in unit tests.
 * 
 * @author Tomasz Pylak
 */
public class AssertionUtil
{
    /** asserts that given text starts with expectedSubstring */
    public static void assertStarts(String expectedPrefix, String text)
    {
        String errorMsg =
                String.format("String \n'%s'\nwas expected to start with \n'%s'.", text,
                        expectedPrefix);
        assertTrue(errorMsg, text.startsWith(expectedPrefix));
    }

    /** asserts that given text ends with expectedSubstring */
    public static void assertEnds(String expectedSuffix, String text)
    {
        String errorMsg =
                String.format("String \n'%s'\nwas expected to end with \n'%s'.", text,
                        expectedSuffix);
        assertTrue(errorMsg, text.endsWith(expectedSuffix));
    }

    /** asserts that given text contains expectedSubstring */
    public static void assertContains(String expectedSubstring, String text)
    {
        String errorMsg =
                String.format("String \n'%s'\nwas expected to be a substring of \n'%s'.",
                        expectedSubstring, text);
        assertTrue(errorMsg, text.contains(expectedSubstring));
    }

    /** asserts that given text does not contain unExpectedSubstring */
    public static void assertContainsNot(String unExpectedSubstring, String text)
    {
        String errorMsg =
                String.format("String \n'%s'\nwas expected to be NOT a substring of \n'%s'.",
                        unExpectedSubstring, text);
        assertFalse(errorMsg, text.contains(unExpectedSubstring));
    }
    
    /** asserts that given text contains expectedSubstring */
    public static void assertSize(Collection<?> collection, int size)
    {
        String errorMsg =
                String.format("Collection \n'%s'\nwas expected to have %d elements, but has %d.",
                        collection.toString(), size, collection.size());
        assertTrue(errorMsg, collection.size() == size);
    }

    public static void assertMatches(String expectedRegexp, String actual)
    {
        String errorMsg =
                String.format("String:\n'%s'\nwas expected to match the following regexp:\n'%s'", actual, expectedRegexp);
        assertTrue(errorMsg, actual != null && actual.matches(expectedRegexp));
    }

    /** asserts that given text contains expectedSubstring. Comparision is case insensitive. */
    public static void assertContainsInsensitive(String expectedSubstring, String text)
    {
        assertContains(expectedSubstring.toUpperCase(), text.toUpperCase());
    }

    public static void assertContainsLines(String expected, String actual)
    {
        Collection<String> expectedLines = getLines(expected);
        Collection<String> actualLines = getLines(actual);

        actualLines.retainAll(expectedLines);

        assertTrue("Expected to contain lines:\n" + expected + "\nactual lines:\n" + actual, expectedLines.equals(actualLines));
    }

    public static void assertContainsLinesInAnyOrder(String expected, String actual)
    {
        List<String> expectedLines = getLines(expected);
        List<String> actualLines = getLines(actual);

        actualLines.retainAll(expectedLines);

        Collections.sort(expectedLines);
        Collections.sort(actualLines);

        assertTrue("Expected to contain lines:\n" + expected + "\nactual lines:\n" + actual, expectedLines.equals(actualLines));
    }

    /** asserts that two int arrays are equal **/
    public static void assertArraysEqual(int[] a1, int[] a2)
    {
        assertEquals(a1.length, a2.length);
        for (int i = 0; i < a1.length; i++)
        {
            assertEquals("Different elements at position, " + i, a1[i], a2[i]);
        }
    }

    /** asserts that two float arrays are equal **/
    public static void assertArraysEqual(float[] a1, float[] a2)
    {
        assertEquals(a1.length, a2.length);
        for (int i = 0; i < a1.length; i++)
        {
            assertEquals("Different elements at position, " + i, a1[i], a2[i]);
        }
    }

    /**
     * asserts that two enums have the same values. Usage example:
     * 
     * <pre>
     * List&lt;MyEnum1&gt; values1 = Arrays.asList(MyEnum1.values());
     * List&lt;MyEnum2&gt; values2 = Arrays.asList(MyEnum2.values());
     * AssertionUtil.assertEnumsEqual(values1, values2);
     * </pre>
     */
    public static void assertEnumsEqual(List<? extends Enum<?>> values1,
            List<? extends Enum<?>> values2)
    {
        Set<String> valuesSet1 = asSet(values1);
        Set<String> valuesSet2 = asSet(values2);
        assertEquals(valuesSet1, valuesSet2);
    }

    private static Set<String> asSet(List<? extends Enum<?>> enumValues)
    {
        Set<String> stringValues = new HashSet<String>();
        for (Enum<?> enumInst : enumValues)
        {
            stringValues.add(enumInst.name());
        }
        return stringValues;
    }

    private static List<String> getLines(String text)
    {
        if (text == null || text.isEmpty())
        {
            return new ArrayList<String>();
        } else
        {
            return new ArrayList<String>(Arrays.asList(text.split("\n")));
        }
    }

    /**
     * returns true if error was caused by unexpected invocation.
     */
    public static Throwable tryAsErrorCausedByUnexpectedInvocation(Throwable t)
    {
        if (t == null)
        {
            return null;
        }
        if (t instanceof ExpectationError)
        {
            return t;
        }
        return tryAsErrorCausedByUnexpectedInvocation(t.getCause());
    }

    /**
     * Assert that toString() of at least one of objects in given array contains given substring.
     */
    public static void assertCollectionContainsString(Collection<?> objects, String subString)
    {
        for (Object object : objects)
        {
            if (object.toString().contains(subString))
            {
                return;
            }
        }
        fail("expected that representation of collection: <" + objects + "> contains: <" + subString + ">");
    }

    /**
     * Assert given collection contains given item
     */
    public static <T> void assertCollectionContains(Collection<T> objects, T item)
    {
        if (false == objects.contains(item))
        {
            fail("expected that collection: <" + objects + "> contains: <" + item + ">");
        }
    }

    /**
     * Assert given collection contains given item
     */
    public static <T> void assertCollectionDoesntContain(Collection<T> objects, T... items)
    {
        for (final T item : items)
        {
            if (objects.contains(item))
            {
                fail("expected that collection: <" + objects + "> does not contain: <" + item + ">");
            }
        }
    }

    /**
     * Assert given collection contains given items
     */
    public static <T> void assertCollectionContainsAtLeast(Collection<T> objects, T... items)
    {
        Set<T> objectsSet = new HashSet<T>(objects);
        for (T item : items)
        {
            assertCollectionContains(objectsSet, item);
        }
    }
    
    /**
     * Assert given collection contains only given items
     */
    public static <T> void assertCollectionContainsOnly(Collection<T> objects, T... items)
    {
        assertEquals(new HashSet<T>(Arrays.asList(items)), new HashSet<T>(objects));
    }

    public static void assertCollectionSize(Collection<?> objects, int size)
    {
        if (objects.size() != size)
        {
            fail("Collection size expected: <" + size + "> but was: <" + objects.size() + "> " + objects.toString());
        }
    }

    public static void assertCollectionIsEmpty(Collection<?> objects)
    {
        if (objects.size() != 0)
        {
            fail("Collection should be empty but was: " + objects.toString() + "");
        }
    }
}
