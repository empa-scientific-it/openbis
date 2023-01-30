/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sample;

import java.util.Comparator;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortParameter;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleSortOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.EntityWithPropertiesComparatorFactory;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.IdentifierComparator;

/**
 * @author pkupczyk
 */
public class SampleComparatorFactory extends EntityWithPropertiesComparatorFactory<Sample>
{

    @Override
    public boolean accepts(Class<?> sortOptionsClass)
    {
        return SampleSortOptions.class.equals(sortOptionsClass);
    }

    @Override
    public Comparator<Sample> getComparator(String field, Map<SortParameter, String> parameters, ISearchCriteria criteria)
    {
        if (SampleSortOptions.IDENTIFIER.equals(field))
        {
            return new IdentifierComparator<Sample>();
        }
        return super.getComparator(field, parameters, criteria);
    }

}
