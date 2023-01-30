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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

/**
 * The DTO for authorization role assignments.
 * 
 * @author Izabela Adamczyk
 */
public final class RoleAssignment extends Code<RoleAssignment>
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private RoleWithHierarchy role;

    private Person person;

    private AuthorizationGroup authorizationGroup;

    public AuthorizationGroup getAuthorizationGroup()
    {
        return authorizationGroup;
    }

    public void setAuthorizationGroup(AuthorizationGroup authorizationGroup)
    {
        this.authorizationGroup = authorizationGroup;
    }

    private Space space;

    private Project project;

    private DatabaseInstance instance;

    public RoleAssignment()
    {
    }

    public final RoleWithHierarchy getRoleSetCode()
    {
        return role;
    }

    public final void setRoleSetCode(RoleWithHierarchy roleSetCode)
    {
        this.role = roleSetCode;
        setCode(roleSetCode.toString());
    }

    public Space getSpace()
    {
        return space;
    }

    public void setSpace(final Space space)
    {
        this.space = space;
    }

    public Project getProject()
    {
        return project;
    }

    public void setProject(final Project project)
    {
        this.project = project;
    }

    public Person getPerson()
    {
        return person;
    }

    public void setPerson(final Person person)
    {
        this.person = person;
    }

    public DatabaseInstance getInstance()
    {
        return instance;
    }

    public void setInstance(final DatabaseInstance instance)
    {
        this.instance = instance;
    }

}
