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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.annotation.DoNotEscape;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CustomImport;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.WebApp;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.WebClientConfiguration;

/**
 * A bean that contains information about the application.
 * 
 * @author Franz-Josef Elmer
 */
@DoNotEscape
public final class ApplicationInfo implements IsSerializable
{
    private String version;

    private String cifexURL;

    private String cifexRecipient;

    private boolean archivingConfigured;

    private boolean projectSamplesEnabled;

    private boolean projectLevelAuthorizationEnabled;

    private boolean projectLevelAuthorizationUser;

    private WebClientConfiguration webClientConfiguration;

    private Set<String> enabledTechnologies;

    private int maxResults = 100000;

    private List<CustomImport> customImports;

    private List<WebApp> webapps;

    public int getMaxResults()
    {
        return maxResults;
    }

    public void setMaxResults(int maxResults)
    {
        if (maxResults > 0)
        {
            this.maxResults = maxResults;
        }
    }

    public String getCifexRecipient()
    {
        return cifexRecipient;
    }

    public void setCifexRecipient(String cifexRecipient)
    {
        this.cifexRecipient = cifexRecipient;
    }

    public final String getVersion()
    {
        return version;
    }

    public final void setVersion(final String version)
    {
        this.version = version;
    }

    public void setCifexURL(String cifexURL)
    {
        this.cifexURL = cifexURL;
    }

    public final String getCifexURL()
    {
        return cifexURL;
    }

    public boolean isArchivingConfigured()
    {
        return archivingConfigured;
    }

    public void setArchivingConfigured(boolean archivingConfigured)
    {
        this.archivingConfigured = archivingConfigured;
    }

    public void setWebClientConfiguration(WebClientConfiguration configuration)
    {
        this.webClientConfiguration = configuration;
    }

    public WebClientConfiguration getWebClientConfiguration()
    {
        return webClientConfiguration;
    }

    public boolean isTechnologyEnabled(String technology)
    {
        for (String pattern : enabledTechnologies)
        {
            if (RegExp.compile(pattern).exec(technology) != null)
            {
                return true;
            }
        }
        return false;
    }

    public void setEnabledTechnologies(Set<String> enabledTechnologies)
    {
        this.enabledTechnologies = enabledTechnologies;
    }

    public void setCustomImports(List<CustomImport> customImports)
    {
        if (customImports == null)
        {
            this.customImports = new ArrayList<CustomImport>();
        } else
        {
            this.customImports = customImports;
        }
    }

    public List<CustomImport> getCustomImports()
    {
        return customImports;
    }

    public void setWebapps(List<WebApp> webapps)
    {
        this.webapps = webapps;
    }

    public List<WebApp> getWebapps()
    {
        if (webapps == null)
        {
            return Collections.emptyList();
        } else
        {
            return webapps;
        }
    }

    public boolean isProjectSamplesEnabled()
    {
        return projectSamplesEnabled;
    }

    public void setProjectSamplesEnabled(boolean projectSamplesEnabled)
    {
        this.projectSamplesEnabled = projectSamplesEnabled;
    }

    public boolean isProjectLevelAuthorizationEnabled()
    {
        return projectLevelAuthorizationEnabled;
    }

    public void setProjectLevelAuthorizationEnabled(boolean projectLevelAuthorizationEnabled)
    {
        this.projectLevelAuthorizationEnabled = projectLevelAuthorizationEnabled;
    }

    public boolean isProjectLevelAuthorizationUser()
    {
        return projectLevelAuthorizationUser;
    }

    public void setProjectLevelAuthorizationUser(boolean projectLevelAuthorizationUser)
    {
        this.projectLevelAuthorizationUser = projectLevelAuthorizationUser;
    }

}
