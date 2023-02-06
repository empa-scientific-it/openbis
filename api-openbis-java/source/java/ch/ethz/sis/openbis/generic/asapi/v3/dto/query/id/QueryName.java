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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id;

import java.io.Serializable;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.query.id.QueryName")
public class QueryName implements IQueryId, Serializable
{

    private static final long serialVersionUID = 1L;

    private String name;

    /**
     * @param name Query name, e.g. "test-query".
     */
    public QueryName(String name)
    {
        setName(name);
    }

    //
    // JSON-RPC
    //

    public String getName()
    {
        return name;
    }

    @SuppressWarnings("unused")
    private QueryName()
    {
        super();
    }

    private void setName(String name)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("Name cannot be null");
        }
        this.name = name;
    }

    @Override
    public String toString()
    {
        return getName();
    }

    @Override
    public int hashCode()
    {
        return ((getName() == null) ? 0 : getName().hashCode());
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
        QueryName other = (QueryName) obj;
        return getName() == null ? getName() == other.getName() : getName().equals(other.getName());
    }

}
