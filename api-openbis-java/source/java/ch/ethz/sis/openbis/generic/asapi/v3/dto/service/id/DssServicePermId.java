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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.IDataStoreId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Franz-Josef Elmer
 */
@JsonObject("as.dto.service.id.DssServicePermId")
public class DssServicePermId extends ObjectPermId implements IDssServiceId
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private IDataStoreId dataStoreId;
    
    public DssServicePermId(String permId)
    {
        this(permId, null);
    }

    public DssServicePermId(String permId, IDataStoreId dataStoreId)
    {
        super(permId);
        this.dataStoreId = dataStoreId;
    }

    @Override
    public String toString()
    {
        return dataStoreId == null ? super.toString() : dataStoreId + ":" + super.toString();
    }

    @Override
    public int hashCode()
    {
        return (dataStoreId == null ? 0 : dataStoreId.hashCode()) * 37 + super.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj == null || super.equals(obj) == false || getClass() != obj.getClass())
        {
            return false;
        }
        DssServicePermId that = (DssServicePermId) obj;
        return dataStoreId == null ? that.dataStoreId == null : dataStoreId.equals(that.dataStoreId);
    }

    //
    // JSON-RPC
    //

    @SuppressWarnings("unused")
    private DssServicePermId()
    {
    }

    @JsonIgnore
    public IDataStoreId getDataStoreId()
    {
        return dataStoreId;
    }

}
