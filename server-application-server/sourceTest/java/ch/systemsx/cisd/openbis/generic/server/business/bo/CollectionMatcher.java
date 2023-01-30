/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.Collection;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * @author Izabela Adamczyk
 */
public final class CollectionMatcher<C extends Collection<?>> extends BaseMatcher<C>
{
    private final C expected;

    public CollectionMatcher(final C expected)
    {
        this.expected = expected;
    }

    @Override
    public boolean matches(Object item)
    {
        if (expected == item)
        {
            return true;
        }
        if (expected == null && item != null || expected != null && item == null)
        {
            return false;
        }
        @SuppressWarnings("unchecked")
        C set = (C) item;
        if (set.size() != expected.size())
        {
            return false;
        }
        for (Object s : expected)
        {
            if (set.contains(s) == false)
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public void describeTo(Description description)
    {
        description.appendValue(expected);
    }
}