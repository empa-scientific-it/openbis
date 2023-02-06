/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.person.update;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IObjectUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.IPersonId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.webapp.update.WebAppSettingsUpdateValue;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Franz-Josef Elmer
 */
@JsonObject("as.dto.person.update.PersonUpdate")
public class PersonUpdate implements IUpdate, IObjectUpdate<IPersonId>
{

    private static final long serialVersionUID = 1L;

    @JsonProperty
    private IPersonId userId;

    @JsonProperty
    private FieldUpdateValue<ISpaceId> spaceId = new FieldUpdateValue<ISpaceId>();

    @JsonProperty
    private FieldUpdateValue<Boolean> active = new FieldUpdateValue<Boolean>();

    @JsonProperty
    private Map<String, WebAppSettingsUpdateValue> webAppSettings;

    public IPersonId getUserId()
    {
        return userId;
    }

    public void setUserId(IPersonId personId)
    {
        this.userId = personId;
    }

    @Override
    @JsonIgnore
    public IPersonId getObjectId()
    {
        return getUserId();
    }

    @JsonIgnore
    public void setSpaceId(ISpaceId spaceId)
    {
        this.spaceId.setValue(spaceId);
    }

    @JsonIgnore
    public FieldUpdateValue<ISpaceId> getSpaceId()
    {
        return spaceId;
    }

    @JsonIgnore
    public WebAppSettingsUpdateValue getWebAppSettings(String webAppId)
    {
        if (webAppSettings == null)
        {
            webAppSettings = new HashMap<String, WebAppSettingsUpdateValue>();
        }

        WebAppSettingsUpdateValue updateValue = webAppSettings.get(webAppId);

        if (updateValue == null)
        {
            updateValue = new WebAppSettingsUpdateValue();
            webAppSettings.put(webAppId, updateValue);
        }

        return updateValue;
    }

    public Map<String, WebAppSettingsUpdateValue> getWebAppSettings()
    {
        return webAppSettings;
    }

    @JsonIgnore
    public FieldUpdateValue<Boolean> isActive()
    {
        return active;
    }

    @JsonIgnore
    public void activate()
    {
        active.setValue(true);
    }

    @JsonIgnore
    public void deactivate()
    {
        active.setValue(false);
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("userId", userId).toString();
    }

}
