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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.query;

import java.util.Comparator;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortParameter;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.QueryDatabase;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.fetchoptions.QueryDatabaseSortOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.AbstractStringComparator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.ComparatorFactory;

/**
 * @author pkupczyk
 */
public class QueryDatabaseComparatorFactory extends ComparatorFactory
{
    private static final Comparator<QueryDatabase> NAME_COMPARATOR = new AbstractStringComparator<QueryDatabase>()
        {
            @Override
            protected String getValue(QueryDatabase database)
            {
                return database.getName();
            }
        };

    @Override
    public boolean accepts(Class<?> sortOptionsClass)
    {
        return QueryDatabaseSortOptions.class.isAssignableFrom(sortOptionsClass);
    }

    @Override
    public Comparator<QueryDatabase> getComparator(String field, Map<SortParameter, String> parameters, ISearchCriteria criteria)
    {
        if (QueryDatabaseSortOptions.NAME.equals(field))
        {
            return NAME_COMPARATOR;
        } else
        {
            return null;
        }
    }

    @Override
    public Comparator<QueryDatabase> getDefaultComparator()
    {
        return NAME_COMPARATOR;
    }

}
