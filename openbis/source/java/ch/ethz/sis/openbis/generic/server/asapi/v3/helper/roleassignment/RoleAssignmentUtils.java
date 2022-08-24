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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.roleassignment;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class RoleAssignmentUtils
{
    public static void checkForSelfreducingAdminAuthorization(RoleAssignmentPE roleAssignment, final PersonPE personPE)
    {
        if (roleAssignment.getPerson() != null && roleAssignment.getPerson().equals(personPE)
                && roleAssignment.getRole().equals(RoleCode.ADMIN))
        {
            boolean isInstanceAdmin = isInstanceAdmin(personPE);
            if (isInstanceAdmin == false)
            {
                String adminType = "space";
                ProjectPE project = roleAssignment.getProject();
                if (project != null)
                {
                    if (isSpaceAdmin(personPE, project.getSpace()))
                    {
                        return;
                    }
                    adminType = "project";
                }
                throw new UserFailureException(
                        "For safety reason you cannot give away your own " + adminType + " admin power. "
                                + "Ask " + (adminType.equals("project") ? "space or " : "")
                                + "instance admin to do that for you.");
            } else if (roleAssignment.getSpace() == null && roleAssignment.getProject() == null)
            {
                throw new UserFailureException(
                        "For safety reason you cannot give away your own omnipotence. "
                                + "Ask another instance admin to do that for you.");
            }
        }
    }

    public static boolean isETLServer(PersonPE personPE)
    {
        for (RoleAssignmentPE roleAssigment : personPE.getRoleAssignments())
        {
            if (roleAssigment.getRole().equals(RoleCode.ETL_SERVER))
            {
                return true;
            }
        }
        return false;
    }

    public static boolean isInstanceAdmin(PersonPE personPE)
    {
        for (RoleAssignmentPE roleAssigment : personPE.getRoleAssignments())
        {
            if (roleAssigment.getSpace() == null && roleAssigment.getProject() == null
                    && roleAssigment.getRole().equals(RoleCode.ADMIN))
            {
                return true;
            }
        }
        return false;
    }
    
    private static boolean isSpaceAdmin(PersonPE personPE, SpacePE spacePE)
    {
        for (RoleAssignmentPE roleAssigment : personPE.getRoleAssignments())
        {
            if (spacePE.equals(roleAssigment.getSpace()) && roleAssigment.getRole().equals(RoleCode.ADMIN))
            {
                return true;
            }
        }
        return false;
        
    }

}
