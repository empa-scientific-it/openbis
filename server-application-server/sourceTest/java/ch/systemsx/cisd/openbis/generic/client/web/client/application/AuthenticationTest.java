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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.Login;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.LoginWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.Logout;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.FailureExpectation;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.WaitForAllActiveCallbacksFinish;

/**
 * A {@link AbstractGWTTestCase} extension to test login.
 * 
 * @author Franz-Josef Elmer
 */
public class AuthenticationTest extends AbstractGWTTestCase
{
    public void testLogin() throws Exception
    {
        remoteConsole.prepare(new Login("test", "a"));
        remoteConsole.prepare(new WaitForAllActiveCallbacksFinish());

        launchTest();
    }

    public void testLogout() throws Exception
    {
        remoteConsole.prepare(new Login("test", "a"));
        remoteConsole.prepare(new Logout());

        launchTest();
    }

    public void testFailedLoginBecauseOfEmptyPassword() throws Exception
    {
        remoteConsole.prepare(new Login("u", ""));
        remoteConsole.prepare(new FailureExpectation(LoginWidget.LoginCallback.class));

        launchTest();
    }

}
