/*
 * Copyright 2014 ETH Zuerich, CISD
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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.webapp.fetchoptions;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.EmptyFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchIgnore;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptionsToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchProperty;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.webapp.WebAppSettings;
import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.webapp.fetchoptions.WebAppSettingsFetchOptions")
public class WebAppSettingsFetchOptions extends FetchOptions<WebAppSettings> implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    @FetchProperty(handler = WebAppSettingsFetchOptionsSettingsHandler.class)
    private Collection<String> settings;

    @JsonProperty
    @FetchProperty(handler = WebAppSettingsFetchOptionsAllSettingsHandler.class)
    private boolean allSettings = false;

    @JsonProperty
    private WebAppSettingsSortOptions sort;

    public WebAppSettingsFetchOptions()
    {
    }

    @FetchIgnore
    public void withSetting(String setting)
    {
        if (settings == null)
        {
            settings = new HashSet<String>();
        }

        settings.add(setting);
    }

    @FetchIgnore
    public boolean hasSetting(String setting)
    {
        return settings != null && settings.contains(setting);
    }

    @FetchIgnore
    public Collection<String> withSettingsUsing(Collection<String> settings)
    {
        return this.settings = settings;
    }

    @FetchIgnore
    public Collection<String> getSettings()
    {
        return settings;
    }

    @FetchIgnore
    public void withAllSettings()
    {
        allSettings = true;
    }

    @FetchIgnore
    public boolean hasAllSettings()
    {
        return allSettings;
    }

    @FetchIgnore
    public boolean withAllSettingsUsing(boolean allSettings)
    {
        return this.allSettings = allSettings;
    }

    @Override
    @JsonIgnore
    public WebAppSettingsSortOptions sortBy()
    {
        if (sort == null)
        {
            sort = new WebAppSettingsSortOptions();
        }
        return sort;
    }

    @Override
    @JsonIgnore
    public WebAppSettingsSortOptions getSortBy()
    {
        return sort;
    }

    @Override
    protected FetchOptionsToStringBuilder getFetchOptionsStringBuilder()
    {
        FetchOptionsToStringBuilder f = new FetchOptionsToStringBuilder("WebAppSettings", this);

        if (settings != null && false == settings.isEmpty())
        {
            f.addFetchOption("Settings " + settings, new EmptyFetchOptions());
        }

        if (allSettings)
        {
            f.addFetchOption("AllSettings", new EmptyFetchOptions());
        }

        return f;
    }

}
