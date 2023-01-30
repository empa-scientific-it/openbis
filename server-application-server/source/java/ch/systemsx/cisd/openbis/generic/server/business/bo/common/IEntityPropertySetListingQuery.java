/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo.common;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.lemnik.eodsql.Select;

/**
 * Query methods for retrieving entity properties.
 * <em>Queries are defined in subinterfaces but {@link Select} annotation has to be used here as well on every query.</em>
 * 
 * @author Franz-Josef Elmer
 */
public interface IEntityPropertySetListingQuery
{

    /**
     * Returns all generic property values of all specified entities.
     * 
     * @param entityIDs The set of entity IDs to get the property values for.
     */
    public Iterable<GenericEntityPropertyRecord> getEntityPropertyGenericValues(LongSet entityIDs);

}
