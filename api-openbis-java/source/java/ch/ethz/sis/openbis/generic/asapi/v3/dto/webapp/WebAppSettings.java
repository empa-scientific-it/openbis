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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.webapp;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.webapp.fetchoptions.WebAppSettingsFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * A map containing persistent settings for an openBIS web app.
 * 
 * @author Franz-Josef Elmer
 */
@JsonObject("as.dto.webapp.WebAppSettings")
public class WebAppSettings implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private WebAppSettingsFetchOptions fetchOptions;

    @JsonProperty
    private String webAppId;

    @JsonProperty
    private Map<String, WebAppSetting> settings;

    @JsonIgnore
    public WebAppSettingsFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    public void setFetchOptions(WebAppSettingsFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    @JsonIgnore
    public String getWebAppId()
    {
        return webAppId;
    }

    public void setWebAppId(String webAppId)
    {
        this.webAppId = webAppId;
    }

    @JsonIgnore
    public WebAppSetting getSetting(String setting)
    {
        if (getFetchOptions() != null && (getFetchOptions().hasAllSettings() || getFetchOptions().hasSetting(setting)))
        {
            return settings != null ? settings.get(setting) : null;
        } else
        {
            throw new NotFetchedException("Setting '" + setting + "' has not been fetched.");
        }
    }

    @JsonIgnore
    public Map<String, WebAppSetting> getSettings()
    {
        if (getFetchOptions() != null && (getFetchOptions().hasAllSettings()
                || (getFetchOptions().getSettings() != null && false == getFetchOptions().getSettings().isEmpty())))
        {
            return settings;
        } else
        {
            throw new NotFetchedException("Settings have not been fetched.");
        }
    }

    public void setSettings(Map<String, WebAppSetting> settings)
    {
        this.settings = settings;
    }

    @Override
    public String toString()
    {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        builder.append(webAppId);
        builder.append(settings);
        return builder.toString();
    }

}
