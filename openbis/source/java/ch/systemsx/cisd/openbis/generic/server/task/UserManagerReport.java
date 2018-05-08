/*
 * Copyright 2018 ETH Zuerich, SIS
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

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.AuthorizationGroupPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.systemsx.cisd.common.utilities.ITimeProvider;

/**
 * @author Franz-Josef Elmer
 */
public class UserManagerReport
{
    private StringBuilder errorReport = new StringBuilder();

    private StringBuilder auditLog = new StringBuilder();

    private ITimeProvider timeProvider;

    public UserManagerReport(ITimeProvider timeProvider)
    {
        this.timeProvider = timeProvider;

    }

    public String getErrorReport()
    {
        return errorReport.toString();
    }

    public String getAuditLog()
    {
        return auditLog.toString();
    }

    void addErrorMessage(String message)
    {
        errorReport.append(message).append('\n');
    }

    void addGroup(String groupCode)
    {
        log("ADD-AUTHORIZATION-GROUP", groupCode);
    }

    void deactivateUser(String userId)
    {
        log("DEACTIVATE-USER", userId);
    }

    void addUser(String userId)
    {
        log("ADD-USER", userId);
    }

    public void reuseUser(String userId)
    {
        log("REUSE-USER", userId);
    }
    
    void addSpace(ISpaceId spaceId)
    {
        log("ADD-SPACE", spaceId);
    }
    
    void addSpaces(List<SpaceCreation> spaceCreations)
    {
        log("ADD-SPACES", spaceCreations.stream().map(SpaceCreation::getCode).collect(Collectors.toList()).toString());
    }

    void assignRoleTo(AuthorizationGroupPermId groupId, Role role, ISpaceId spaceId)
    {
        log("ASSIGN-ROLE-TO-AUTHORIZATION-GROUP", "group: " + groupId + ", role: SPACE_" + role + " for " + spaceId);
    }
    
    public void unassignRoleFrom(AuthorizationGroupPermId groupId, Role role, ISpaceId spaceId)
    {
        log("UNASSIGN-ROLE-FORM-AUTHORIZATION-GROUP", "group: " + groupId + ", role: SPACE_" + role + " for " + spaceId);
    }

    void addUserToGroup(String groupCode, String userId)
    {
        log("ADD-USER-TO-AUTHORIZATION-GROUP", "group: " + groupCode + ", user: " + userId);
    }

    void removeUserFromGroup(String groupCode, String userId)
    {
        log("REMOVE-USER-FROM-AUTHORIZATION-GROUP", "group: " + groupCode + ", user: " + userId);
    }

    private void log(String action, Object details)
    {
        Date timeStamp = new Date(timeProvider.getTimeInMilliseconds());
        MessageFormat messageFormat = new MessageFormat("{0,date,yyyy-MM-dd HH:mm:ss} [{1}] {2}\n");
        auditLog.append(messageFormat.format(new Object[] { timeStamp, action, details }));
    }

}
