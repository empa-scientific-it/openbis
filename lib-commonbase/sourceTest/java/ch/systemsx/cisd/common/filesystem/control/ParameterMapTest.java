/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.filesystem.control;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author anttil
 */
public class ParameterMapTest
{
    IEventFeed eventProvider;

    Mockery context;

    ParameterMap map;

    @BeforeMethod
    public void fixture()
    {
        context = new Mockery();
        eventProvider = context.mock(IEventFeed.class);
        map = new ParameterMap(eventProvider);
    }

    @Test
    public void registeredParametersReturnDefaultValue() throws Exception
    {
        context.checking(new Expectations()
            {
                {
                    allowing(eventProvider).getNewEvents(with(eventFilterAcceptingValueUpdatesOf("parameter")));
                    will(returnValue(new ArrayList<String>()));
                }
            });

        map.addParameter("parameter", "value");
        assertThat(map.getParameterValue("parameter").getValue(), is("value"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void parametersCannotBeRegisteredWithInvalidValue() throws Exception
    {
        map.addParameter("parameter", "100", passNothingFilter());
    }

    @Test
    public void parametersCanBeUpdated() throws Exception
    {
        context.checking(new Expectations()
            {
                {
                    allowing(eventProvider).getNewEvents(with(eventFilterAcceptingValueUpdatesOf("parameter")));
                    will(returnValue(Arrays.asList("parameter-updated value")));
                }
            });
        map.addParameter("parameter", "default value");
        assertThat(map.getParameterValue("parameter").getValue(), is("updated value"));
    }

    @Test
    public void emptyValueIsAccepted() throws Exception
    {
        context.checking(new Expectations()
            {
                {
                    allowing(eventProvider).getNewEvents(with(eventFilterAcceptingValueUpdatesOf("parameter")));
                    will(returnValue(Arrays.asList("parameter-")));
                }
            });
        map.addParameter("parameter", "default value");
        assertThat(map.getParameterValue("parameter").getValue(), is(""));
    }

    @Test
    public void illegalParameterValuesAreNotUpdated() throws Exception
    {
        context.checking(new Expectations()
            {
                {
                    allowing(eventProvider).getNewEvents(with(eventFilterAcceptingValueUpdatesOf("parameter")));
                    will(returnValue(Arrays.asList("parameter-updated value")));
                }
            });
        map.addParameter("parameter", "default value", acceptValuesStartingWith("d"));
        assertThat(map.getParameterValue("parameter").getValue(), is("default value"));
    }

    private IValueFilter passNothingFilter()
    {
        return new IValueFilter()
            {
                @Override
                public boolean isValid(String value)
                {
                    return false;
                }
            };
    }

    private IValueFilter acceptValuesStartingWith(final String string)
    {
        return new IValueFilter()
            {

                @Override
                public boolean isValid(String value)
                {
                    return value.startsWith(string);
                }

            };
    }

    static TypeSafeMatcher<IEventFilter> eventFilterAcceptingValueUpdatesOf(final String value)
    {
        return new TypeSafeMatcher<IEventFilter>()
            {

                @Override
                public void describeTo(Description description)
                {
                    description.appendText("IEventFilter accepting '" + value + "-*'");
                }

                @Override
                public boolean matchesSafely(IEventFilter filter)
                {
                    return filter.accepts(value + "-" + UUID.randomUUID().toString());
                }
            };
    }
}
