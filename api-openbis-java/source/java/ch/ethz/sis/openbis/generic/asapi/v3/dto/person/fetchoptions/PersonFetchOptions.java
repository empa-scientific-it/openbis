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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.EmptyFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchIgnore;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptionsToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchProperty;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.fetchoptions.RoleAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.webapp.fetchoptions.WebAppSettingsFetchOptions;
import ch.systemsx.cisd.base.annotation.JsonObject;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.person.fetchoptions.PersonFetchOptions")
public class PersonFetchOptions extends FetchOptions<Person> implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private SpaceFetchOptions space;

    @JsonProperty
    private PersonFetchOptions registrator;

    @JsonProperty
    private RoleAssignmentFetchOptions roleAssignments;

    @JsonProperty
    @FetchProperty(handler = PersonFetchOptionsWebAppSettingsHandler.class)
    private Map<String, WebAppSettingsFetchOptions> webAppSettings;

    @JsonProperty
    @FetchProperty(handler = PersonFetchOptionsAllWebAppSettingsHandler.class)
    private boolean allWebAppSettings;

    @JsonProperty
    private PersonSortOptions sort;

    // Method automatically generated with DtoGenerator
    public SpaceFetchOptions withSpace()
    {
        if (space == null)
        {
            space = new SpaceFetchOptions();
        }
        return space;
    }

    // Method automatically generated with DtoGenerator
    public SpaceFetchOptions withSpaceUsing(SpaceFetchOptions fetchOptions)
    {
        return space = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasSpace()
    {
        return space != null;
    }

    // Method automatically generated with DtoGenerator
    public PersonFetchOptions withRegistrator()
    {
        if (registrator == null)
        {
            registrator = new PersonFetchOptions();
        }
        return registrator;
    }

    // Method automatically generated with DtoGenerator
    public PersonFetchOptions withRegistratorUsing(PersonFetchOptions fetchOptions)
    {
        return registrator = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasRegistrator()
    {
        return registrator != null;
    }

    public RoleAssignmentFetchOptions withRoleAssignments()
    {
        if (roleAssignments == null)
        {
            roleAssignments = new RoleAssignmentFetchOptions();
        }
        return roleAssignments;
    }

    public RoleAssignmentFetchOptions withRoleAssignmentsUsing(RoleAssignmentFetchOptions fetchOptions)
    {
        return roleAssignments = fetchOptions;
    }

    public boolean hasRoleAssignments()
    {
        return roleAssignments != null;
    }

    @FetchIgnore
    public WebAppSettingsFetchOptions withWebAppSettings(String webAppId)
    {
        if (webAppSettings == null)
        {
            webAppSettings = new HashMap<String, WebAppSettingsFetchOptions>();
        }

        WebAppSettingsFetchOptions webAppFo = webAppSettings.get(webAppId);

        if (webAppFo == null)
        {
            webAppFo = new WebAppSettingsFetchOptions();
            webAppSettings.put(webAppId, webAppFo);
        }

        return webAppFo;
    }

    @FetchIgnore
    public boolean hasWebAppSettings(String webAppId)
    {
        return webAppSettings != null && webAppSettings.get(webAppId) != null;
    }

    @FetchIgnore
    public Map<String, WebAppSettingsFetchOptions> withWebAppSettingsUsing(Map<String, WebAppSettingsFetchOptions> webAppSettings)
    {
        return this.webAppSettings = webAppSettings;
    }

    @FetchIgnore
    public Map<String, WebAppSettingsFetchOptions> getWebAppSettings()
    {
        return webAppSettings;
    }

    @FetchIgnore
    public void withAllWebAppSettings()
    {
        allWebAppSettings = true;
    }

    @FetchIgnore
    public boolean hasAllWebAppSettings()
    {
        return allWebAppSettings;
    }

    @FetchIgnore
    public boolean withAllWebAppSettingsUsing(boolean allWebAppSettings)
    {
        return this.allWebAppSettings = allWebAppSettings;
    }

    // Method automatically generated with DtoGenerator
    @Override
    @JsonIgnore
    public PersonSortOptions sortBy()
    {
        if (sort == null)
        {
            sort = new PersonSortOptions();
        }
        return sort;
    }

    // Method automatically generated with DtoGenerator
    @Override
    @JsonIgnore
    public PersonSortOptions getSortBy()
    {
        return sort;
    }

    @Override
    protected FetchOptionsToStringBuilder getFetchOptionsStringBuilder()
    {
        FetchOptionsToStringBuilder f = new FetchOptionsToStringBuilder("Person", this);
        f.addFetchOption("Space", space);
        f.addFetchOption("Registrator", registrator);
        f.addFetchOption("RoleAssignments", roleAssignments);

        if (webAppSettings != null && false == webAppSettings.isEmpty())
        {
            for (Map.Entry<String, WebAppSettingsFetchOptions> entry : webAppSettings.entrySet())
            {
                f.addFetchOption("WebAppSettings [" + entry.getKey() + "]", entry.getValue());
            }
        }

        if (allWebAppSettings)
        {
            f.addFetchOption("AllWebAppSettings", new EmptyFetchOptions());
        }

        return f;
    }

}
