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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IDescriptionHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPermIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistrationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistratorHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.fetchoptions.PluginFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.PluginPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Franz-Josef Elmer
 */
@JsonObject("as.dto.plugin.Plugin")
public class Plugin implements Serializable, IDescriptionHolder, IPermIdHolder, IRegistrationDateHolder, IRegistratorHolder
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private PluginFetchOptions fetchOptions;

    @JsonProperty
    private String name;

    @JsonProperty
    private PluginPermId permId;

    @JsonProperty
    private String description;

    @JsonProperty
    private Person registrator;

    @JsonProperty
    private Date registrationDate;

    @JsonProperty
    private PluginKind pluginKind;

    @JsonProperty
    private Set<EntityKind> entityKinds;

    @JsonProperty
    private PluginType pluginType;

    @JsonProperty
    private String script;

    @JsonProperty
    private boolean available;

    @JsonIgnore
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @JsonIgnore
    @Override
    public PluginPermId getPermId()
    {
        return permId;
    }

    public void setPermId(PluginPermId permId)
    {
        this.permId = permId;
    }

    @JsonIgnore
    @Override
    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    @JsonIgnore
    public PluginFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    public void setFetchOptions(PluginFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    @JsonIgnore
    @Override
    public Person getRegistrator()
    {
        if (getFetchOptions() != null && getFetchOptions().hasRegistrator())
        {
            return registrator;
        } else
        {
            throw new NotFetchedException("Registrator has not been fetched.");
        }
    }

    public void setRegistrator(Person registrator)
    {
        this.registrator = registrator;
    }

    @JsonIgnore
    @Override
    public Date getRegistrationDate()
    {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    @JsonIgnore
    public PluginKind getPluginKind()
    {
        return pluginKind;
    }

    public void setPluginKind(PluginKind pluginType)
    {
        this.pluginKind = pluginType;
    }

    @JsonIgnore
    public Set<EntityKind> getEntityKinds()
    {
        return entityKinds;
    }

    public void setEntityKinds(Set<EntityKind> entityKinds)
    {
        this.entityKinds = entityKinds;
    }

    @JsonIgnore
    public PluginType getPluginType()
    {
        return pluginType;
    }

    public void setPluginType(PluginType pluginType)
    {
        this.pluginType = pluginType;
    }

    @JsonIgnore
    public String getScript()
    {
        if (getFetchOptions() != null && getFetchOptions().hasScript())
        {
            return script;
        } else
        {
            throw new NotFetchedException("Script has not been fetched.");
        }
    }

    public void setScript(String script)
    {
        this.script = script;
    }

    @JsonIgnore
    public boolean isAvailable()
    {
        return available;
    }

    public void setAvailable(boolean available)
    {
        this.available = available;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("permId", permId).toString();
    }

}
