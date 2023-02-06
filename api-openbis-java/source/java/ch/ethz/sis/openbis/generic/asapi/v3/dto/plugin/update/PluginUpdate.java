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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.update;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IObjectUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.IPluginId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Franz-Josef Elmer
 */
@JsonObject("as.dto.plugin.update.PluginUpdate")
public class PluginUpdate implements IUpdate, IObjectUpdate<IPluginId>
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private IPluginId pluginId;

    @JsonProperty
    private FieldUpdateValue<String> description = new FieldUpdateValue<String>();

    @JsonProperty
    private FieldUpdateValue<String> script = new FieldUpdateValue<String>();

    @JsonProperty
    private FieldUpdateValue<Boolean> available = new FieldUpdateValue<Boolean>();

    @Override
    @JsonIgnore
    public IPluginId getObjectId()
    {
        return getPluginId();
    }

    @JsonIgnore
    public IPluginId getPluginId()
    {
        return pluginId;
    }

    public void setPluginId(IPluginId pluginId)
    {
        this.pluginId = pluginId;
    }

    @JsonIgnore
    public FieldUpdateValue<String> getDescription()
    {
        return description;
    }

    @JsonIgnore
    public void setDescription(String description)
    {
        this.description.setValue(description);
    }

    @JsonIgnore
    public FieldUpdateValue<String> getScript()
    {
        return script;
    }

    @JsonIgnore
    public void setScript(String script)
    {
        this.script.setValue(script);
    }

    @JsonIgnore
    public FieldUpdateValue<Boolean> getAvailable()
    {
        return available;
    }

    @JsonIgnore
    public void setAvailable(Boolean available)
    {
        this.available.setValue(available);
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("pluginId", pluginId).toString();
    }

}
