/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.authorizationgroup;

import java.util.Comparator;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.AuthorizationGroup;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.fetchoptions.AuthorizationGroupSortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortParameter;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.CodeComparator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.ComparatorFactory;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class AuthorizationGroupComparatorFactory extends ComparatorFactory
{

    @Override
    public boolean accepts(Class<?> sortOptionsClass)
    {
        return AuthorizationGroupSortOptions.class.isAssignableFrom(sortOptionsClass);
    }

    @Override
    public Comparator<AuthorizationGroup> getComparator(String field, Map<SortParameter, String> parameters, ISearchCriteria criteria)
    {
        if (AuthorizationGroupSortOptions.CODE.equals(field))
        {
            return new CodeComparator<AuthorizationGroup>();
        } else
        {
            return null;
        }
    }

    @Override
    public Comparator<AuthorizationGroup> getDefaultComparator()
    {
        return new CodeComparator<AuthorizationGroup>();
    }

}
