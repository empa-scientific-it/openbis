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

package ch.systemsx.cisd.openbis.generic.server.task;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.stacked.StackedAuthenticationService;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IRoleAssignmentDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * {@link IMaintenanceTask} to revoke access to users not present on the authentication service anymore.
 * 
 * @author Juan Fuentes
 */
public class RevokeUserAccessMaintenanceTask implements IMaintenanceTask
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, RevokeUserAccessMaintenanceTask.class);

    private static final String AUTH_SERVICE_BEAN = "authentication-service";

    private static final IAuthenticationService authService;

    static
    {
        IAuthenticationService authServiceAux = (IAuthenticationService) CommonServiceProvider.tryToGetBean(AUTH_SERVICE_BEAN);

        if (authServiceAux instanceof StackedAuthenticationService
                && ((StackedAuthenticationService) authServiceAux).allServicesSupportListingByUserId())
        {
            authService = authServiceAux;
        } else if (authServiceAux.supportsListingByUserId())
        {
            authService = authServiceAux;
        }
        else
        {
            authService = null;
        }
    }

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        operationLog.info("Task " + pluginName + " initialized.");
    }

    @Override
    public void execute()
    {
        operationLog.info("execution started");
        // 0. Initial Check
        if (authService == null)
        {
            operationLog.info("This plugin doesn't work with authentication services that don't support listing by user idt.");
            return;
        }
        // 1. Grab all users, user roles and user authorization groups
        IPersonDAO personDAO = CommonServiceProvider.getDAOFactory().getPersonDAO();
        IRoleAssignmentDAO rolesDAO = CommonServiceProvider.getDAOFactory().getRoleAssignmentDAO();

        // Used to manage the authorization groups since the IPersonDAO throw a session exception when accessing this information.
        ICommonServerForInternalUse server = CommonServiceProvider.getCommonServer();
        SessionContextDTO contextOrNull = server.tryToAuthenticateAsSystem();

        List<PersonPE> people = personDAO.listActivePersons();

        // 2. Users to Revoke
        List<PersonPE> peopleToRevoke = new ArrayList<PersonPE>();

        // 3. Check if the users exists on LDAP currently
        personCheck: for (PersonPE person : people)
        {
            if (false == person.isSystemUser() && person.isActive() && false == isUserValid(person.getUserId()))
            {
                List<RoleAssignmentPE> roles = rolesDAO.listRoleAssignmentsByPerson(person);
                for (RoleAssignmentPE role : roles)
                {
                    if (role.getRole().name().equals("ETL_SERVER"))
                    {
                        continue personCheck;
                    }
                }
                peopleToRevoke.add(person);
            }
        }

        // 4. If is not found on the authentication service, revoke access
        for (PersonPE person : peopleToRevoke)
        {
            String userIdToRevoke = person.getUserId();
            operationLog.info("person " + userIdToRevoke + " is going to be revoked.");

            // Delete person roles
            for (RoleAssignmentPE role : rolesDAO.listRoleAssignmentsByPerson(person))
            {
                rolesDAO.delete(role);
            }

            // Delete person from groups
            List<AuthorizationGroup> groups = server.listAuthorizationGroups(contextOrNull.getSessionToken());

            for (AuthorizationGroup group : groups)
            {
                List<Person> peopleInGroup = server.listPersonInAuthorizationGroup(contextOrNull.getSessionToken(), new TechId(group.getId()));
                for (Person personInGroup : peopleInGroup)
                {
                    if (personInGroup.getUserId().equals(userIdToRevoke))
                    {
                        List<String> toRemoveFromGroup = new ArrayList<String>();
                        toRemoveFromGroup.add(person.getUserId());
                        server.removePersonsFromAuthorizationGroup(contextOrNull.getSessionToken(), new TechId(group.getId()), toRemoveFromGroup);
                    }
                }
            }

            // Change userId and disable
            person.setUserId(person.getUserId() + "-" + getTimeStamp());
            person.setActive(false);
            person.setDisplaySettings(null);
            personDAO.updatePerson(person);

            operationLog.info("person " + userIdToRevoke + " has been revoked.");
        }

        operationLog.info("task executed");
    }

    /*
     * We can only delete the users if, the Principals are listable and they are not available.
     */
    private boolean isUserValid(String userId)
    {
        return authService.supportsListingByUserId() && false == authService.listPrincipalsByUserId(userId).isEmpty();
    }

    private String getTimeStamp()
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
        return dateFormat.format(new Date());
    }

}
