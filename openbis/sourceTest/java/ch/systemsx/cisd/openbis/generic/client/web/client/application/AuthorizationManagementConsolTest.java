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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.CheckGroupCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.CreateGroupCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.LoginCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.RemoteConsole;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class AuthorizationManagementConsolTest extends AbstractGWTTestCase
{
    private RemoteConsole remoteConsole;
    
    @Override
    protected void gwtSetUp() throws Exception
    {
        remoteConsole = new RemoteConsole(this);
        System.out.println("TEST: " + getName());
    }
    
    public void testCreateGroup()
    {
        final Client client = new Client();
        client.onModuleLoad();
        remoteConsole.prepare(new LoginCommand("a", "a"));
        remoteConsole.prepare(new CreateGroupCommand("test-group"));
        remoteConsole.prepare(new CheckGroupCommand("test-group")).finish(10000);
        
    }

}
