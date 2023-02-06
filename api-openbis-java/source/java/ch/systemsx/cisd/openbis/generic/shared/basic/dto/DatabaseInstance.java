/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.util.JsonPropertyUtil;

/**
 * @author Franz-Josef Elmer
 */
@SuppressWarnings("unused")
@JsonObject("DatabaseInstance")
public class DatabaseInstance extends Code<DatabaseInstance>
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private Long id;

    private String uuid;

    private String identifier;

    private boolean isHomeDatabase;

    public final String getUuid()
    {
        return uuid;
    }

    public final void setUuid(String uuid)
    {
        this.uuid = uuid;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    @JsonIgnore
    public Long getId()
    {
        return id;
    }

    @JsonIgnore
    public void setId(Long id)
    {
        this.id = id;
    }

    public boolean isHomeDatabase()
    {
        return isHomeDatabase;
    }

    public void setHomeDatabase(boolean isHomeDatabase)
    {
        this.isHomeDatabase = isHomeDatabase;
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
