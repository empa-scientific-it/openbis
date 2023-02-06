/*
 * Copyright 2010 ETH Zuerich, CISD
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.ICodeHolder;

/**
 * Bean which represents a space, all its projects, and all its relevant role assignments
 * 
 * @author Franz-Josef Elmer
 */
@SuppressWarnings("unused")
@JsonObject("SpaceWithProjectsAndRoleAssignments")
public class SpaceWithProjectsAndRoleAssignments implements ICodeHolder, Serializable
{
    private static final long serialVersionUID = 1L;

    private String code;

    private List<Project> projects = new ArrayList<Project>();

    @JsonProperty
    private Map<String, Set<Role>> rolesPerUser = new HashMap<String, Set<Role>>();

    /**
     * Creates a new instance for the specified code.
     * 
     * @throws IllegalArgumentException if the code is <code>null</code> or an empty string.
     */
    public SpaceWithProjectsAndRoleAssignments(String code)
    {
        if (code == null || code.length() == 0)
        {
            throw new IllegalArgumentException("Unspecified code.");
        }
        this.code = code;
    }

    /**
     * Returns the space code.
     */
    @Override
    public String getCode()
    {
        return code;
    }

    public void add(Project project)
    {
        projects.add(project);
    }

    /**
     * Returns all projects of this space.
     */
    public List<Project> getProjects()
    {
        return projects;
    }

    public void add(String user, Role role)
    {
        Set<Role> roles = rolesPerUser.get(user);
        if (roles == null)
        {
            roles = new HashSet<Role>();
            rolesPerUser.put(user, roles);
        }
        roles.add(role);
    }

    /**
     * Returns all access roles the specified user has on this space.
     */
    public Set<Role> getRoles(String userID)
    {
        Set<Role> set = rolesPerUser.get(userID);
        return set == null ? Collections.<Role> emptySet() : set;
    }

    /**
     * Returns the set of all users that have a role in this space.
     */
    @JsonIgnore
    public Set<String> getUsers()
    {
        return rolesPerUser.keySet();
    }

    //
    // JSON-RPC
    //

    private SpaceWithProjectsAndRoleAssignments()
    {
    }

    private Map<String, Set<Role>> getRolesPerUser()
    {
        return rolesPerUser;
    }

    private void setRolesPerUser(Map<String, Set<Role>> rolesPerUser)
    {
        this.rolesPerUser = rolesPerUser;
    }

    private void setCode(String code)
    {
        this.code = code;
    }

    private void setProjects(List<Project> projects)
    {
        this.projects = projects;
    }
}
