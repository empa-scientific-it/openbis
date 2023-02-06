/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * A map containing persistent settings for an openBIS web app.
 * 
 * @author Bernd Rinn
 */
@JsonObject("WebAppSettings")
public class WebAppSettings implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String webAppId;

    private Map<String, String> settings;

    public WebAppSettings(String webAppId, Map<String, String> settings)
    {
        this.webAppId = webAppId;
        this.settings = settings;
    }

    public WebAppSettings(String webAppId, WebAppSettings settings)
    {
        this.webAppId = webAppId;
        this.settings = settings.settings;
    }

    public String getWebAppId()
    {
        return webAppId;
    }

    public Map<String, String> getSettings()
    {
        return settings;
    }

    @Override
    public String toString()
    {
        final ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        builder.append(getWebAppId());
        builder.append(settings);
        return builder.toString();
    }

    //
    // JSON-RPC
    //
    @SuppressWarnings("unused")
    private WebAppSettings()
    {
        this.settings = new HashMap<String, String>();
    }

    @SuppressWarnings("unused")
    private void setWebAppId(String webAppId)
    {
        this.webAppId = webAppId;
    }

    @SuppressWarnings("unused")
    private void setSettings(Map<String, String> settings)
    {
        this.settings = settings;
    }

}
