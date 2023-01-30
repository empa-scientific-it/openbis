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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.locator;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.Login;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.WaitForAllActiveCallbacksFinish;

/**
 * @author Chandrasekhar Ramakrishnan
 * @author Piotr Buczek
 */
public class ViewLocatorResolverRegistryTest extends AbstractGWTTestCase
{
    public void testResolvePermlinkLocator()
    {
        loginAndOpenView("entity=SAMPLE&permId=200811050919915-8");
        launchTest();
    }

    public void testResolveSearchLocator()
    {
        loginAndOpenView("action=SEARCH&entity=SAMPLE&code=CL1");
        launchTest();
    }

    private void loginAndOpenView(String urlParams)
    {
        remoteConsole.prepare(new Login("test", "a"));
        remoteConsole.prepare(new OpenViewCommand(client, urlParams));
        remoteConsole.prepare(new WaitForAllActiveCallbacksFinish());
    }
}
