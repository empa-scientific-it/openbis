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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.fetchoptions;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.fetchoptions.AuthorizationGroupFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptionsToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Franz-Josef Elmer
 */
@JsonObject("as.dto.roleassignment.fetchoptions.RoleAssignmentFetchOptions")
public class RoleAssignmentFetchOptions extends FetchOptions<RoleAssignment> implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private PersonFetchOptions registrator;

    @JsonProperty
    private PersonFetchOptions user;

    @JsonProperty
    private AuthorizationGroupFetchOptions authorizationGroup;

    @JsonProperty
    private SpaceFetchOptions space;

    @JsonProperty
    private ProjectFetchOptions project;

    @JsonProperty
    private RoleAssignmentSortOptions sort;

    public PersonFetchOptions withUser()
    {
        if (user == null)
        {
            user = new PersonFetchOptions();
        }
        return user;
    }

    public PersonFetchOptions withUserUsing(PersonFetchOptions fetchOptions)
    {
        return user = fetchOptions;
    }

    public boolean hasUser()
    {
        return user != null;
    }

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

    public AuthorizationGroupFetchOptions withAuthorizationGroup()
    {
        if (authorizationGroup == null)
        {
            authorizationGroup = new AuthorizationGroupFetchOptions();
        }
        return authorizationGroup;
    }

    public AuthorizationGroupFetchOptions withAuthorizationGroupUsing(AuthorizationGroupFetchOptions fetchOptions)
    {
        return authorizationGroup = fetchOptions;
    }

    public boolean hasAuthorizationGroup()
    {
        return authorizationGroup != null;
    }

    public SpaceFetchOptions withSpace()
    {
        if (space == null)
        {
            space = new SpaceFetchOptions();
        }
        return space;
    }

    public SpaceFetchOptions withSpaceUsing(SpaceFetchOptions fetchOptions)
    {
        return space = fetchOptions;
    }

    public boolean hasSpace()
    {
        return space != null;
    }

    public ProjectFetchOptions withProject()
    {
        if (project == null)
        {
            project = new ProjectFetchOptions();
        }
        return project;
    }

    public ProjectFetchOptions withProjectUsing(ProjectFetchOptions fetchOptions)
    {
        return project = fetchOptions;
    }

    public boolean hasProject()
    {
        return project != null;
    }

    @Override
    public RoleAssignmentSortOptions sortBy()
    {
        if (sort == null)
        {
            sort = new RoleAssignmentSortOptions();
        }
        return sort;
    }

    @Override
    public RoleAssignmentSortOptions getSortBy()
    {
        return sort;
    }

    @Override
    protected FetchOptionsToStringBuilder getFetchOptionsStringBuilder()
    {
        FetchOptionsToStringBuilder f = new FetchOptionsToStringBuilder("RoleAssignment", this);
        f.addFetchOption("Space", space);
        f.addFetchOption("Project", project);
        f.addFetchOption("User", user);
        f.addFetchOption("AuthorizationGroup", authorizationGroup);
        f.addFetchOption("Registrator", registrator);
        return f;
    }

}
