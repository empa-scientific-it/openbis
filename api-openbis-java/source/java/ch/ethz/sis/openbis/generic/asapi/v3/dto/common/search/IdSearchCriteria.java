/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.common.search.IdSearchCriteria")
public class IdSearchCriteria<T extends IObjectId> extends AbstractSearchCriteria
{

    private static final long serialVersionUID = 1L;

    private T id;

    @SuppressWarnings("hiding")
    public void thatEquals(T id)
    {
        this.id = id;
    }

    public T getId()
    {
        return id;
    }

    @Override
    public int hashCode()
    {
        return ((id == null) ? 0 : id.hashCode());
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        IdSearchCriteria<?> other = (IdSearchCriteria<?>) obj;
        return id == null ? id == other.id : id.equals(other.id);
    }

    @Override
    public String toString()
    {
        return "with id '" + id + "'";
    }

}
