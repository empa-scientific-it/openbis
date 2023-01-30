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

package ch.systemsx.cisd.openbis.systemtest.base.matcher;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;

public class InSpaceMatcher extends TypeSafeMatcher<Object>
{
    private Space expectedSpace;

    public InSpaceMatcher(Space space)
    {
        this.expectedSpace = space;
    }

    public InSpaceMatcher()
    {
        this(null);
    }

    @Override
    public void describeTo(Description description)
    {
        if (expectedSpace == null)
        {
            description.appendText("An entity without space");
        } else
        {
            description.appendText("An entity in space " + expectedSpace);
        }
    }

    @Override
    public boolean matchesSafely(Object actual)
    {
        Space actualSpace;
        if (actual instanceof Project)
        {
            actualSpace = ((Project) actual).getSpace();
        } else
        {
            actualSpace = ((Sample) actual).getSpace();
        }

        if (this.expectedSpace == null && actualSpace == null)
        {
            return true;
        }

        if (expectedSpace == null)
        {
            return false;
        }

        return expectedSpace.equals(actualSpace);

    }

}