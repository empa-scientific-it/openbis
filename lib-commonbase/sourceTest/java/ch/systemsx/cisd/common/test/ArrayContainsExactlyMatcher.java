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

package ch.systemsx.cisd.common.test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * @author pkupczyk
 */
public class ArrayContainsExactlyMatcher<T> extends TypeSafeMatcher<T[]>
{

    private T[] expectedArray;

    public ArrayContainsExactlyMatcher(T... expected)
    {
        this.expectedArray = expected;
    }

    @Override
    public void describeTo(Description description)
    {
        description.appendText("A collection containing exactly items " + expectedArray.toString());
    }

    @Override
    public boolean matchesSafely(T[] actualArray)
    {
        if (actualArray.length != expectedArray.length)
        {
            return false;
        }

        Collection<T> actualCollection = new HashSet<T>(Arrays.asList(actualArray));
        Collection<T> expectedCollection = new HashSet<T>(Arrays.asList(actualArray));

        return actualCollection.equals(expectedCollection);
    }

    public static <T> ArrayContainsExactlyMatcher<T> containsExactly(T... items)
    {
        return new ArrayContainsExactlyMatcher<T>(items);
    }

}
