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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort;

import java.util.Comparator;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.EntitySortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortParameter;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IModificationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPermIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistrationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;

/**
 * @author pkupczyk
 */
public class EntityComparatorFactory<OBJECT extends ICodeHolder & IPermIdHolder & IRegistrationDateHolder & IModificationDateHolder> extends
        ComparatorFactory
{

    @Override
    public boolean accepts(Class<?> sortOptionsClass)
    {
        return EntitySortOptions.class.isAssignableFrom(sortOptionsClass);
    }

    @Override
    public Comparator<OBJECT> getComparator(String field, Map<SortParameter, String> parameters, ISearchCriteria criteria)
    {
        if (EntitySortOptions.CODE.equals(field))
        {
            return new CodeComparator<OBJECT>();
        } else if (EntitySortOptions.PERM_ID.equals(field))
        {
            return new PermIdComparator<OBJECT>();
        } else if (EntitySortOptions.REGISTRATION_DATE.equals(field))
        {
            return new RegistrationDateComparator<OBJECT>();
        } else if (EntitySortOptions.MODIFICATION_DATE.equals(field))
        {
            return new ModificationDateComparator<OBJECT>();
        } else
        {
            return null;
        }
    }

    @Override
    public Comparator<OBJECT> getDefaultComparator()
    {
        return new CodeComparator<OBJECT>();
    }

}
