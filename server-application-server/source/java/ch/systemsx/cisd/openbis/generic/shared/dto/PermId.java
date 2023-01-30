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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.IPermIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Perm id of an entity.
 * 
 * @author Izabela Adamczyk
 */
public class PermId implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String id;

    protected PermId()
    {
        // for serialization
    }

    public PermId(String id)
    {
        assert id != null : "id cannot be null";
        this.id = id;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public static PermId create(IPermIdHolder holder)
    {
        if (holder == null || holder.getPermId() == null)
        {
            return null;
        } else
        {
            return new PermId(holder.getPermId());
        }
    }

    public static List<String> asStrings(List<PermId> permIds)
    {
        List<String> results = new ArrayList<String>();
        for (PermId permId : permIds)
        {
            results.add(permId.getId());
        }
        return results;
    }

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof PermId == false)
        {
            return false;
        }
        return this.toString().equals(obj.toString());
    }

    @Override
    public final int hashCode()
    {
        return this.toString().hashCode();
    }

    @Override
    public String toString()
    {
        return id;
    }

}
