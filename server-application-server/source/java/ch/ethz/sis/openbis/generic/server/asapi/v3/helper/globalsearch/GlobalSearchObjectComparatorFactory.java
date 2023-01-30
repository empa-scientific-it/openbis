/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.globalsearch;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortParameter;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.GlobalSearchObject;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.fetchoptions.GlobalSearchObjectSortOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.ComparatorFactory;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.CompositeComparator;

/**
 * @author pkupczyk
 */
public class GlobalSearchObjectComparatorFactory extends ComparatorFactory
{

    @Override
    public boolean accepts(Class<?> sortOptionsClass)
    {
        return GlobalSearchObjectSortOptions.class.isAssignableFrom(sortOptionsClass);
    }

    @Override
    public Comparator<GlobalSearchObject> getComparator(String field, Map<SortParameter, String> parameters, ISearchCriteria criteria)
    {
        if (GlobalSearchObjectSortOptions.SCORE.equals(field))
        {
            return new ScoreComparator();
        } else if (GlobalSearchObjectSortOptions.OBJECT_KIND.equals(field))
        {
            return new ObjectKindComparator();
        } else if (GlobalSearchObjectSortOptions.OBJECT_PERM_ID.equals(field))
        {
            return new ObjectPermIdComparator();
        } else if (GlobalSearchObjectSortOptions.OBJECT_IDENTIFIER.equals(field))
        {
            return new ObjectIdentifierComparator();
        } else
        {
            return null;
        }
    }

    @Override
    public Comparator<GlobalSearchObject> getDefaultComparator()
    {
        return new CompositeComparator<GlobalSearchObject>(Collections.reverseOrder(new ScoreComparator()), new ObjectKindComparator(),
                new ObjectIdentifierComparator());
    }
}
