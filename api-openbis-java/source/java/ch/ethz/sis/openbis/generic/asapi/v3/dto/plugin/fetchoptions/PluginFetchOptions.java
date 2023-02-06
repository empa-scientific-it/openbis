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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.fetchoptions;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.EmptyFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptionsToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Franz-Josef Elmer
 *
 */
@JsonObject("as.dto.plugin.fetchoptions.PluginFetchOptions")
public class PluginFetchOptions extends FetchOptions<Plugin> implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private PersonFetchOptions registrator;

    @JsonProperty
    private PluginSortOptions sort;
    
    @JsonProperty
    private EmptyFetchOptions script;

    public PersonFetchOptions withRegistrator()
    {
        if (registrator == null)
        {
            registrator = new PersonFetchOptions();
        }
        return registrator;
    }

    public PersonFetchOptions withRegistratorUsing(PersonFetchOptions fetchOptions)
    {
        return registrator = fetchOptions;
    }

    public boolean hasRegistrator()
    {
        return registrator != null;
    }
    @JsonIgnore
    public EmptyFetchOptions withScript()
    {
        if (script == null)
        {
            script = new EmptyFetchOptions();
        }
        return script;
    }
    
    public EmptyFetchOptions withScriptUsing(EmptyFetchOptions fetchOptions)
    {
        return script = fetchOptions;
    }
    
    @JsonIgnore
    public boolean hasScript()
    {
        return script != null;
    }
    
    @Override
    public PluginSortOptions sortBy()
    {
        if (sort == null)
        {
            sort = new PluginSortOptions();
        }
        return sort;
    }
    
    @Override
    public PluginSortOptions getSortBy()
    {
        return sort;
    }

    @Override
    protected FetchOptionsToStringBuilder getFetchOptionsStringBuilder()
    {
        FetchOptionsToStringBuilder f = new FetchOptionsToStringBuilder("Plugin", this);
        f.addFetchOption("Registrator", registrator);
        f.addFetchOption("Script", script);
        return f;
    }

}
