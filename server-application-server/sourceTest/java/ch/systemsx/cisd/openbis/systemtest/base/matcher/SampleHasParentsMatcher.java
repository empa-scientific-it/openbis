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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

public class SampleHasParentsMatcher extends TypeSafeMatcher<Sample>
{

    private Set<Sample> expectedParents;

    public SampleHasParentsMatcher(Sample first, Sample... rest)
    {
        this.expectedParents = new HashSet<Sample>();
        expectedParents.add(first);
        expectedParents.addAll(Arrays.asList(rest));
    }

    @Override
    public void describeTo(Description description)
    {
        description.appendText("A sample with parents " + expectedParents);
    }

    @Override
    public boolean matchesSafely(Sample actual)
    {
        if (actual.getParents().size() != expectedParents.size())
        {
            return false;
        }

        for (Sample parent : actual.getParents())
        {
            if (!expectedParents.contains(parent))
            {
                return false;
            }
        }
        return true;
    }
}