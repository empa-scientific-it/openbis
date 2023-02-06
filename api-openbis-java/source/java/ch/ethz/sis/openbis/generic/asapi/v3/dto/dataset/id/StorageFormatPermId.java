/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Storage format perm id.
 * 
 * @author pkupczyk
 */
@JsonObject("as.dto.dataset.id.StorageFormatPermId")
public class StorageFormatPermId extends ObjectPermId implements IStorageFormatId
{

    private static final long serialVersionUID = 1L;

    /**
     * @param permId Storage format perm id, e.g. "PROPRIETARY".
     */
    public StorageFormatPermId(String permId)
    {
        super(permId != null ? permId.toUpperCase() : null);
    }

    //
    // JSON-RPC
    //

    @SuppressWarnings("unused")
    private StorageFormatPermId()
    {
        super();
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
        if (false == (obj instanceof StorageFormatPermId))
        {
            return false;
        }
        StorageFormatPermId other = (StorageFormatPermId) obj;
        if (getPermId() == null)
        {
            if (other.getPermId() != null)
            {
                return false;
            }
        } else if (!getPermId().equals(other.getPermId()))
        {
            return false;
        }
        return true;
    }

}
