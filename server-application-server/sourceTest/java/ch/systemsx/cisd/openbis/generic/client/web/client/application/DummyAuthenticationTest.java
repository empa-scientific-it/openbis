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
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.WaitForAllActiveCallbacksFinish;

/**
 * A shorter version of {@link AuthenticationTest} that just logins to the application. It is supposed to be run at the beginning of each suite
 * (except the first one that should start with full {@link AuthenticationTest}) to do all the GWT initialization and make the next test work faster.
 * 
 * @author Piotr Buczek
 */
public class DummyAuthenticationTest extends AbstractGWTTestCase
{
    public void testLogin() throws Exception
    {
        remoteConsole.prepare(new Login("test", "a"));
        remoteConsole.prepare(new WaitForAllActiveCallbacksFinish());

        launchTest(2 * DEFAULT_TIMEOUT);
    }

}
