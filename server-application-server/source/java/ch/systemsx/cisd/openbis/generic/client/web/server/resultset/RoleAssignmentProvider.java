/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.RoleAssignmentGridColumnIDs.AUTHORIZATION_GROUP;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.RoleAssignmentGridColumnIDs.PERSON;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.RoleAssignmentGridColumnIDs.PROJECT;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.RoleAssignmentGridColumnIDs.ROLE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.RoleAssignmentGridColumnIDs.SPACE;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;

/**
 * Provider of table model for {@link RoleAssignment} instances
 * 
 * @author Franz-Josef Elmer
 */
public class RoleAssignmentProvider extends AbstractCommonTableModelProvider<RoleAssignment>
{

    private IAuthorizationConfig authorizationConfig;

    public RoleAssignmentProvider(ICommonServer commonServer, IAuthorizationConfig authorizationConfig, String sessionToken)
    {
        super(commonServer, sessionToken);
        this.authorizationConfig = authorizationConfig;
    }

    @Override
    protected TypedTableModel<RoleAssignment> createTableModel()
    {
        List<RoleAssignment> roles = commonServer.listRoleAssignments(sessionToken);
        TypedTableModelBuilder<RoleAssignment> builder = new TypedTableModelBuilder<RoleAssignment>();
        builder.addColumn(PERSON);
        builder.addColumn(AUTHORIZATION_GROUP);
        builder.addColumn(SPACE);

        if (authorizationConfig.isProjectLevelEnabled())
        {
            builder.addColumn(PROJECT);
        }

        builder.addColumn(ROLE);
        for (RoleAssignment roleAssignment : roles)
        {
            if (roleAssignment.getProject() != null && false == authorizationConfig.isProjectLevelEnabled())
            {
                continue;
            }

            builder.addRow(roleAssignment);
            Person person = roleAssignment.getPerson();
            AuthorizationGroup group = roleAssignment.getAuthorizationGroup();
            Space space = roleAssignment.getSpace();
            Project project = roleAssignment.getProject();
            builder.column(PERSON).addString(person == null ? "" : person.getUserId());
            builder.column(AUTHORIZATION_GROUP).addString(group == null ? "" : group.getCode());
            builder.column(SPACE).addString(space == null ? "" : space.getCode());

            if (authorizationConfig.isProjectLevelEnabled())
            {
                builder.column(PROJECT).addString(project == null ? "" : project.getIdentifier());
            }

            builder.column(ROLE).addString(roleAssignment.getCode());
        }

        return builder.getModel();
    }

}
