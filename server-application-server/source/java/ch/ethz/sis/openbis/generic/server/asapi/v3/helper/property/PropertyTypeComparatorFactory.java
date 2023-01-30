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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.property;

import java.util.Comparator;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortParameter;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeSortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularySortOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.CodeComparator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.ComparatorFactory;

/**
 * @author Franz-Josef Elmer
 *
 */
public class PropertyTypeComparatorFactory extends ComparatorFactory
{

    @Override
    public boolean accepts(Class<?> sortOptionsClass)
    {
        return PropertyTypeSortOptions.class.isAssignableFrom(sortOptionsClass);
    }

    @Override
    public Comparator<PropertyType> getComparator(String field, Map<SortParameter, String> parameters, ISearchCriteria criteria)
    {
        if (VocabularySortOptions.CODE.equals(field))
        {
            return new CodeComparator<PropertyType>();
        } else
        {
            return null;
        }
    }

    @Override
    public Comparator<PropertyType> getDefaultComparator()
    {
        return new CodeComparator<PropertyType>();
    }

}
