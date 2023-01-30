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

import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.AuthorizationGroupGridColumnIDs.CODE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.AuthorizationGroupGridColumnIDs.DESCRIPTION;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.AuthorizationGroupGridColumnIDs.REGISTRATION_DATE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.AuthorizationGroupGridColumnIDs.REGISTRATOR;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.CommonGridColumnIDs.MODIFICATION_DATE;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;

/**
 * Provider of {@link AuthorizationGroup} instances.
 * 
 * @author Franz-Josef Elmer
 */
public class AuthorizationGroupProvider extends
        AbstractCommonTableModelProvider<AuthorizationGroup>
{
    public AuthorizationGroupProvider(ICommonServer commonServer, String sessionToken)
    {
        super(commonServer, sessionToken);
    }

    @Override
    protected TypedTableModel<AuthorizationGroup> createTableModel()
    {
        List<AuthorizationGroup> groups = commonServer.listAuthorizationGroups(sessionToken);
        TypedTableModelBuilder<AuthorizationGroup> builder =
                new TypedTableModelBuilder<AuthorizationGroup>();
        builder.addColumn(CODE);
        builder.addColumn(DESCRIPTION);
        builder.addColumn(REGISTRATOR);
        builder.addColumn(REGISTRATION_DATE).withDefaultWidth(300);
        builder.addColumn(MODIFICATION_DATE).withDefaultWidth(300).hideByDefault();
        for (AuthorizationGroup group : groups)
        {
            builder.addRow(group);
            builder.column(CODE).addString(group.getCode());
            builder.column(DESCRIPTION).addString(group.getDescription());
            builder.column(REGISTRATOR).addPerson(group.getRegistrator());
            builder.column(REGISTRATION_DATE).addDate(group.getRegistrationDate());
            builder.column(MODIFICATION_DATE).addDate(group.getModificationDate());
        }
        return builder.getModel();
    }

}
