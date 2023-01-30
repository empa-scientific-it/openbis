/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;

public abstract class SimpleFieldMatcher<OBJECT> extends Matcher<OBJECT>
{

    @Override
    public List<OBJECT> getMatching(IOperationContext context, List<OBJECT> objects, ISearchCriteria criteria)
    {
        List<OBJECT> matches = new ArrayList<OBJECT>();

        for (OBJECT object : objects)
        {
            if (isMatching(context, object, criteria))
            {
                matches.add(object);
            }
        }

        return matches;
    }

    protected abstract boolean isMatching(IOperationContext context, OBJECT object, ISearchCriteria criteria);

}