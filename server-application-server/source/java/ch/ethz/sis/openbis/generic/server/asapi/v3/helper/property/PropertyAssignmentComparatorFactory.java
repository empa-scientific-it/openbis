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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.property;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortParameter;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentSortOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.AbstractComparator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.AbstractStringComparator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.ComparatorFactory;

/**
 * @author Franz-Josef Elmer
 */
public class PropertyAssignmentComparatorFactory extends ComparatorFactory
{
    private static final Map<String, Comparator<?>> COMPARATORS_BY_FIELD = new HashMap<>();

    static
    {
        COMPARATORS_BY_FIELD.put(PropertyAssignmentSortOptions.ORDINAL, new AbstractComparator<PropertyAssignment, Integer>()
            {
                @Override
                protected Integer getValue(PropertyAssignment propertyAssignment)
                {
                    return propertyAssignment.getOrdinal();
                }
            });
        COMPARATORS_BY_FIELD.put(PropertyAssignmentSortOptions.CODE, new AbstractPropertyAssignmentComparator()
            {
                @Override
                protected String getValue(PropertyType propertyType)
                {
                    return propertyType.getCode();
                }
            });
        COMPARATORS_BY_FIELD.put(PropertyAssignmentSortOptions.LABEL, new AbstractPropertyAssignmentComparator()
            {
                @Override
                protected String getValue(PropertyType propertyType)
                {
                    return propertyType.getLabel();
                }
            });
    }

    @Override
    public boolean accepts(Class<?> sortOptionsClass)
    {
        return PropertyAssignmentSortOptions.class.isAssignableFrom(sortOptionsClass);
    }

    @Override
    public Comparator<?> getComparator(String field, Map<SortParameter, String> parameters, ISearchCriteria criteria)
    {
        return COMPARATORS_BY_FIELD.get(field);
    }

    @Override
    public Comparator<?> getDefaultComparator()
    {
        return getComparator(PropertyAssignmentSortOptions.ORDINAL, null, null);
    }

    private abstract static class AbstractPropertyAssignmentComparator extends AbstractStringComparator<PropertyAssignment>
    {
        @Override
        protected String getValue(PropertyAssignment assignment)
        {
            PropertyType propertyType = assignment.getPropertyType();
            String v = propertyType == null ? null : getValue(propertyType);
            return v == null ? "" : v;
        }

        protected abstract String getValue(PropertyType propertyType);

    }
}
