/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.util.JsonPropertyUtil;

/**
 * Description of a query. Contains everything needed on client side to show to the user what queries are available, to specify parameter bindings,
 * and to identify a query uniquely.
 * 
 * @author Franz-Josef Elmer
 */
@SuppressWarnings("unused")
@JsonObject("QueryDescription")
public class QueryDescription implements Serializable
{
    private static final long serialVersionUID = 1L;

    private long id;

    private String name;

    private String description;

    private List<String> parameters;

    /**
     * Returns the ID of the query. Will be used to identify the query to be executed.
     */
    @JsonIgnore
    public long getId()
    {
        return id;
    }

    @JsonIgnore
    public void setId(long id)
    {
        this.id = id;
    }

    /**
     * Returns the name of the query.
     */
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Returns an optional description or empty string if undefined.
     */
    public String getDescription()
    {
        return description == null ? "" : description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * Returns the list of parameters to which values should be bound when executing the query.
     * 
     * @return an empty list if there are no parameters.
     */
    public List<String> getParameters()
    {
        return parameters;
    }

    public void setParameters(List<String> parameters)
    {
        this.parameters = parameters;
    }

    /**
     * Returns <code>true</code> if and only if the specified object is of type {@link QueryDescription} and has the same ID as this.
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj instanceof QueryDescription == false)
        {
            return false;
        }
        QueryDescription queryDescription = (QueryDescription) obj;
        return queryDescription.id == id;
    }

    /**
     * Returns the ID.
     */
    @Override
    public int hashCode()
    {
        return (int) id;
    }

    /**
     * Returns the name.
     */
    @Override
    public String toString()
    {
        return name;
    }

    //
    // JSON-RPC
    //

    @JsonProperty("id")
    private String getIdAsString()
    {
        return JsonPropertyUtil.toStringOrNull(id);
    }

    private void setIdAsString(String id)
    {
        this.id = JsonPropertyUtil.toLongOrNull(id);
    }

}
