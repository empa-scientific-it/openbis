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

package ch.ethz.sis.openbis.generic.server.api.v3.helper.sort;

import java.util.Comparator;

import ch.ethz.sis.openbis.generic.as.api.v3.dto.common.fetchoptions.EntityWithPropertiesSortOptions;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.common.fetchoptions.SortOptions;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.common.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.common.interfaces.IModificationDateHolder;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.common.interfaces.IPropertiesHolder;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.common.interfaces.IRegistrationDateHolder;

/**
 * @author pkupczyk
 */
public class EntityWithPropertiesComparatorFactory<OBJECT extends ICodeHolder & IRegistrationDateHolder & IModificationDateHolder & IPropertiesHolder>
        extends EntityComparatorFactory<OBJECT>
{

    @Override
    public boolean accepts(SortOptions<?> sortOptions)
    {
        return sortOptions instanceof EntityWithPropertiesSortOptions;
    }

    @Override
    public Comparator<OBJECT> getComparator(String field)
    {
        if (field.startsWith(EntityWithPropertiesSortOptions.PROPERTY))
        {
            return new PropertyComparator<OBJECT>(field.substring(EntityWithPropertiesSortOptions.PROPERTY.length()));
        } else
        {
            return super.getComparator(field);
        }
    }

}
