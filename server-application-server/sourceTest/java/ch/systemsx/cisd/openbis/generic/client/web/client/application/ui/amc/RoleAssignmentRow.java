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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.RoleAssignmentGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.RowWithProperties;

/**
 * Allows to define role assignment table row expectations.
 * 
 * @author Piotr Buczek
 */
public class RoleAssignmentRow extends RowWithProperties
{

    public static final RoleAssignmentRow personRoleRow(final String groupCode,
            final String userId, final String roleCode)
    {
        return new RoleAssignmentRow(groupCode, userId, roleCode, null);
    }

    public static final RoleAssignmentRow authorizationGroupRoleRow(final String groupCode,
            final String authGroupId, final String roleCode)
    {
        return new RoleAssignmentRow(groupCode, null, roleCode, authGroupId);
    }

    private RoleAssignmentRow(final String groupCode, final String userId, final String roleCode,
            String authGroupId)
    {
        super();
        assert userId == null && authGroupId != null || userId != null && authGroupId == null;
        withCell(RoleAssignmentGridColumnIDs.SPACE, groupCode);
        if (userId != null)
            withCell(RoleAssignmentGridColumnIDs.PERSON, userId);
        if (authGroupId != null)
            withCell(RoleAssignmentGridColumnIDs.AUTHORIZATION_GROUP, authGroupId);
        withCell(RoleAssignmentGridColumnIDs.ROLE, roleCode);
    }
}
