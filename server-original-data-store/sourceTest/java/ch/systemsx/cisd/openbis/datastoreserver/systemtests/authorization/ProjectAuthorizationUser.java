/*
 * Copyright 2017 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization;

import static ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.CommonAuthorizationSystemTest.AUTH_PROJECT_1;
import static ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.CommonAuthorizationSystemTest.AUTH_SPACE_1;
import static ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.CommonAuthorizationSystemTest.AUTH_SPACE_2;
import static ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.CommonAuthorizationSystemTest.PERSON_WITH_PA_OFF;
import static ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.CommonAuthorizationSystemTest.PERSON_WITH_PA_ON;

import java.util.Collections;

import ch.systemsx.cisd.openbis.generic.server.authorization.project.TestAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
public class ProjectAuthorizationUser
{

    private PersonPE person;

    public ProjectAuthorizationUser(PersonPE person)
    {
        this.person = person;
    }

    public String getUserId()
    {
        return person.getUserId();
    }

    public boolean hasPAEnabled()
    {
        return getUserId().endsWith("_pa_on");
    }

    public boolean isInstanceUser()
    {
        return hasInstanceRole();
    }

    public boolean isSpaceUser()
    {
        return hasSpaceRole();
    }

    public boolean isSpaceUser(String spaceCode)
    {
        return hasSpaceRole(spaceCode);
    }

    public boolean isSpace1User()
    {
        return isSpaceUser(AUTH_SPACE_1);
    }

    public boolean isSpace2User()
    {
        return isSpaceUser(AUTH_SPACE_2);
    }

    public boolean isDisabledProjectUser()
    {
        return isProjectUser() && false == hasPAEnabled();
    }

    public boolean isProjectUser()
    {
        return hasProjectRole();
    }

    public boolean isProjectUser(String spaceCode, String projectCode)
    {
        return hasProjectRole(spaceCode, projectCode);
    }

    public boolean isProject11User()
    {
        return isProjectUser(AUTH_SPACE_1, AUTH_PROJECT_1);
    }

    public boolean isProject21User()
    {
        return isProjectUser(AUTH_SPACE_2, AUTH_PROJECT_1);
    }

    public boolean isETLServerUser()
    {
        return hasETLServerRole();
    }

    public IAuthSessionProvider getSessionProvider()
    {
        SimpleSession session = new SimpleSession();
        session.setPerson(person);
        return new TestAuthSessionProvider(session);
    }

    public static ProjectAuthorizationUser[] providerUsers()
    {
        PersonPE instanceAdmin = new PersonPE();
        instanceAdmin.setUserId(PERSON_WITH_PA_OFF);
        instanceAdmin.setRoleAssignments(Collections.singleton(createInstanceRole(RoleCode.ADMIN)));

        PersonPE space1AdminPAOff = new PersonPE();
        space1AdminPAOff.setUserId(PERSON_WITH_PA_OFF);
        space1AdminPAOff.setRoleAssignments(Collections.singleton(createSpaceRole(RoleCode.ADMIN, AUTH_SPACE_1)));

        PersonPE space1AdminPAOn = new PersonPE();
        space1AdminPAOn.setUserId(PERSON_WITH_PA_ON);
        space1AdminPAOn.setRoleAssignments(Collections.singleton(createSpaceRole(RoleCode.ADMIN, AUTH_SPACE_1)));

        PersonPE project11AdminPAOff = new PersonPE();
        project11AdminPAOff.setUserId(PERSON_WITH_PA_OFF);
        project11AdminPAOff.setRoleAssignments(Collections.singleton(createProjectRole(RoleCode.ADMIN, AUTH_SPACE_1, AUTH_PROJECT_1)));

        PersonPE project11AdminPAOn = new PersonPE();
        project11AdminPAOn.setUserId(PERSON_WITH_PA_ON);
        project11AdminPAOn.setRoleAssignments(Collections.singleton(createProjectRole(RoleCode.ADMIN, AUTH_SPACE_1, AUTH_PROJECT_1)));

        PersonPE space2AdminPAOff = new PersonPE();
        space2AdminPAOff.setUserId(PERSON_WITH_PA_OFF);
        space2AdminPAOff.setRoleAssignments(Collections.singleton(createSpaceRole(RoleCode.ADMIN, AUTH_SPACE_2)));

        PersonPE space2AdminPAOn = new PersonPE();
        space2AdminPAOn.setUserId(PERSON_WITH_PA_ON);
        space2AdminPAOn.setRoleAssignments(Collections.singleton(createSpaceRole(RoleCode.ADMIN, AUTH_SPACE_2)));

        PersonPE project21AdminPAOff = new PersonPE();
        project21AdminPAOff.setUserId(PERSON_WITH_PA_OFF);
        project21AdminPAOff.setRoleAssignments(Collections.singleton(createProjectRole(RoleCode.ADMIN, AUTH_SPACE_2, AUTH_PROJECT_1)));

        PersonPE project21AdminPAOn = new PersonPE();
        project21AdminPAOn.setUserId(PERSON_WITH_PA_ON);
        project21AdminPAOn.setRoleAssignments(Collections.singleton(createProjectRole(RoleCode.ADMIN, AUTH_SPACE_2, AUTH_PROJECT_1)));

        return new ProjectAuthorizationUser[] {
                new ProjectAuthorizationUser(instanceAdmin),
                new ProjectAuthorizationUser(space1AdminPAOff),
                new ProjectAuthorizationUser(space1AdminPAOn),
                new ProjectAuthorizationUser(project11AdminPAOff),
                new ProjectAuthorizationUser(project11AdminPAOn),
                new ProjectAuthorizationUser(space2AdminPAOff),
                new ProjectAuthorizationUser(space2AdminPAOn),
                new ProjectAuthorizationUser(project21AdminPAOff),
                new ProjectAuthorizationUser(project21AdminPAOn),
        };
    }

    private static RoleAssignmentPE createInstanceRole(RoleCode roleCode)
    {
        RoleAssignmentPE ra = new RoleAssignmentPE();
        ra.setRole(roleCode);
        return ra;
    }

    private static RoleAssignmentPE createSpaceRole(RoleCode roleCode, String spaceCode)
    {
        SpacePE space = new SpacePE();
        space.setCode(spaceCode);

        RoleAssignmentPE ra = new RoleAssignmentPE();
        ra.setRole(roleCode);
        ra.setSpace(space);
        return ra;
    }

    private static RoleAssignmentPE createProjectRole(RoleCode roleCode, String spaceCode, String projectCode)
    {
        SpacePE space = new SpacePE();
        space.setCode(spaceCode);

        ProjectPE project = new ProjectPE();
        project.setCode(projectCode);
        project.setSpace(space);

        RoleAssignmentPE ra = new RoleAssignmentPE();
        ra.setRole(roleCode);
        ra.setProject(project);
        return ra;
    }

    private boolean hasInstanceRole()
    {
        for (RoleAssignmentPE role : person.getAllPersonRoles())
        {
            if (role.getRoleWithHierarchy().isInstanceLevel())
            {
                return true;
            }
        }
        return false;
    }

    private boolean hasETLServerRole()
    {
        for (RoleAssignmentPE role : person.getAllPersonRoles())
        {
            if (RoleCode.ETL_SERVER.equals(role.getRole()))
            {
                return true;
            }
        }
        return false;
    }

    private boolean hasSpaceRole()
    {
        for (RoleAssignmentPE role : person.getAllPersonRoles())
        {
            if (role.getSpace() != null)
            {
                return true;
            }
        }
        return false;
    }

    private boolean hasSpaceRole(String spaceCode)
    {
        for (RoleAssignmentPE role : person.getAllPersonRoles())
        {
            if (role.getSpace() != null && role.getSpace().getCode().equals(spaceCode))
            {
                return true;
            }
        }
        return false;
    }

    private boolean hasProjectRole()
    {
        for (RoleAssignmentPE role : person.getAllPersonRoles())
        {
            if (role.getProject() != null)
            {
                return true;
            }
        }
        return false;
    }

    private boolean hasProjectRole(String spaceCode, String projectCode)
    {
        for (RoleAssignmentPE role : person.getAllPersonRoles())
        {
            if (role.getProject() != null && role.getProject().getSpace().getCode().equals(spaceCode)
                    && role.getProject().getCode().equals(projectCode))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString()
    {
        return "userId: " + getUserId()
                + ", isInstanceUser: " + isInstanceUser()
                + ", isSpace1User: " + isSpace1User()
                + ", isSpace2User: " + isSpace2User()
                + ", isProject11User: " + isProject11User()
                + ", isProject21User: " + isProject21User()
                + ", isETLServer: " + isETLServerUser()
                + ", hasPAEnabled: " + hasPAEnabled();
    }

}
