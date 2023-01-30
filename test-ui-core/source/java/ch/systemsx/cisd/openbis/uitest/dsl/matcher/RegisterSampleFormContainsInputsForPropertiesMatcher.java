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

package ch.systemsx.cisd.openbis.uitest.dsl.matcher;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import ch.systemsx.cisd.openbis.uitest.page.RegisterSample;
import ch.systemsx.cisd.openbis.uitest.type.PropertyType;

/**
 * @author anttil
 */
public class RegisterSampleFormContainsInputsForPropertiesMatcher extends
        TypeSafeMatcher<RegisterSample>
{

    private Collection<PropertyType> properties;

    public RegisterSampleFormContainsInputsForPropertiesMatcher(PropertyType... fields)
    {
        this.properties = new HashSet<PropertyType>(Arrays.asList(fields));
    }

    @Override
    public void describeTo(Description description)
    {
        description
                .appendText("Properties " + this.properties
                        + " asked for when registering a sample");
    }

    @Override
    public boolean matchesSafely(RegisterSample page)
    {
        for (PropertyType type : properties)
        {
            if (!page.getProperties().contains(type.getLabel()))
            {
                return false;
            }
        }
        return true;
    }

}
