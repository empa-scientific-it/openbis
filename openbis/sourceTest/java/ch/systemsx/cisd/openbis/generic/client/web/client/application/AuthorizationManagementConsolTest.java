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

import static ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants.AUTHORIZATION_MANAGEMENT_CONSOLE_VIEW;
import static ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants.VIEW_KEY;

import com.google.gwt.user.client.Window;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.Login;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.CheckGroup;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.CheckPerson;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.CreateGroup;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.CreatePerson;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;

/**
 * A {@link AbstractGWTTestCase} extension to test <i>AMC</i>.
 * 
 * @author Franz-Josef Elmer
 */
public class AuthorizationManagementConsolTest extends AbstractGWTTestCase
{

    @Override
    protected void setUpTest() throws Exception
    {
        if (AUTHORIZATION_MANAGEMENT_CONSOLE_VIEW.equals(Window.Location.getParameter(VIEW_KEY)) == false)
        {
            final String href = Window.Location.getHref();
            Window.Location.assign(href + "?" + VIEW_KEY + "="
                    + AUTHORIZATION_MANAGEMENT_CONSOLE_VIEW);
        }
    }

    public final void testCreateGroup()
    {
        remoteConsole.prepare(new Login("test", "a"));
        final String groupCode = "test-group";
        remoteConsole.prepare(new CreateGroup(groupCode));
        remoteConsole.prepare(new CheckGroup(groupCode.toUpperCase())).finish(10000);

        client.onModuleLoad();
    }

    public final void testCreatePerson()
    {
        remoteConsole.prepare(new Login("test", "a"));
        // This userId must be one of the ones located on 'etc/passwd' (file based authentication).
        final String userId = "o";
        remoteConsole.prepare(new CreatePerson(userId));
        remoteConsole.prepare(new CheckPerson(userId)).finish(10000);

        client.onModuleLoad();
    }

    public final void testCreateRole()
    {
    }
}
