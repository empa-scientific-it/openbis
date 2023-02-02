/*
 * Copyright 2018 ETH Zuerich, SIS
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

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * @author Franz-Josef Elmer
 */
public class ToStringMatcher<T> extends BaseMatcher<T>
{
    private String expectedToStringString;

    public ToStringMatcher(T expectedItem)
    {
        this(String.valueOf(expectedItem));
    }

    public ToStringMatcher(String expectedToStringString)
    {
        this.expectedToStringString = expectedToStringString;
    }

    @Override
    public boolean matches(Object item)
    {
        return String.valueOf(item).equals(expectedToStringString);
    }

    @Override
    public void describeTo(Description description)
    {
        description.appendText(expectedToStringString);
    }

}
