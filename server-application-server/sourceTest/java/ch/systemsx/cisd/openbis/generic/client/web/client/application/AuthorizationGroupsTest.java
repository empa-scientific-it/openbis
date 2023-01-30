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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu.ActionMenuKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.CheckAuthorizationGroupTable;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.CheckPersonTable;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.CreateAuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.FillAddPersonForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.OpenAddPersonDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.ShowAuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PersonGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SpaceGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;
import ch.systemsx.cisd.openbis.generic.shared.basic.Row;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroup;

/**
 * A {@link AbstractGWTTestCase} extension to test registration of authorization groups.
 * 
 * @author Izabela Adamczyk
 */
public class AuthorizationGroupsTest extends AbstractGWTTestCase
{

    // TODO 2009-08-12 IA: add tests for (1)deleting auth group, (2)removing person, (3)removing
    // auth group role, (4)authorization via auth group

    public final void testCreateAuthorizationGroup()
    {
        final String authGroupCode = TestConstants.ADMINS_GROUP;
        loginAndInvokeAction(ActionMenuKind.AUTHORIZATION_MENU_AUTHORIZATION_GROUPS);

        remoteConsole.prepare(new CreateAuthorizationGroup(authGroupCode));
        final CheckAuthorizationGroupTable table = new CheckAuthorizationGroupTable();
        table.expectedRow(new Row().withCell(SpaceGridColumnIDs.CODE, authGroupCode.toUpperCase()));
        remoteConsole.prepare(table);

        launchTest();
    }

    public final void testAddPerson()
    {
        final String authGroupCode = TestConstants.ADMINS_GROUP;
        AuthorizationGroup authGroup = createAuthGroup();
        final String userId = TestConstants.USER_ID_TEST;
        loginAndInvokeAction(ActionMenuKind.AUTHORIZATION_MENU_AUTHORIZATION_GROUPS);

        remoteConsole.prepare(new ShowAuthorizationGroup(authGroupCode));
        remoteConsole.prepare(new OpenAddPersonDialog(authGroup));
        remoteConsole.prepare(FillAddPersonForm.singleUser(userId, authGroup));
        final CheckPersonTable table = new CheckPersonTable(authGroup);
        table.expectedRow(new Row().withCell(PersonGridColumnIDs.USER_ID, userId));
        remoteConsole.prepare(table);

        launchTest();
    }

    public final void testAddMultiplePerson()
    {
        final String authGroupCode = TestConstants.ADMINS_GROUP;
        AuthorizationGroup authGroup = createAuthGroup();
        loginAndInvokeAction(ActionMenuKind.AUTHORIZATION_MENU_AUTHORIZATION_GROUPS);
        remoteConsole.prepare(new ShowAuthorizationGroup(authGroupCode));
        remoteConsole.prepare(new OpenAddPersonDialog(authGroup));

        List<String> codes = Arrays.asList("u", "user");
        final CheckPersonTable table = new CheckPersonTable(authGroup);
        for (String userId : codes)
        {
            table.expectedRow(new Row().withCell(PersonGridColumnIDs.USER_ID, userId));
        }
        remoteConsole.prepare(FillAddPersonForm.multipleUsers(codes, authGroup));
        remoteConsole.prepare(table);

        launchTest();
    }

    private AuthorizationGroup createAuthGroup()
    {
        AuthorizationGroup authGroup = new AuthorizationGroup();
        authGroup.setId(TestConstants.TECH_ID_ADMINS_GROUP);
        return authGroup;
    }

}
