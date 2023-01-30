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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.dataset;

import java.util.Comparator;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.EntitySortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortParameter;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetSortOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.CodeComparator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.EntityWithPropertiesComparatorFactory;

/**
 * @author pkupczyk
 */
public class DataSetComparatorFactory extends EntityWithPropertiesComparatorFactory<DataSet>
{

    @Override
    public boolean accepts(Class<?> sortOptionsClass)
    {
        return DataSetSortOptions.class.equals(sortOptionsClass);
    }

    @Override
    public Comparator<DataSet> getComparator(String field, Map<SortParameter, String> parameters, ISearchCriteria criteria)
    {
        if (EntitySortOptions.PERM_ID.equals(field))
        {
            return new CodeComparator<DataSet>();
        }
        return super.getComparator(field, parameters, criteria);
    }

}
