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

/**
 * A {@link AbstractGWTTestCase} extension to test <i>AMC</i>.
 * 
 * @author Izabela Adamczyk
 */
public class SampleBrowserTest extends AbstractGWTTestCase
{

    public final void testListSamples()
    {
        // TODO 2008-10-22, Izabela Adamczyk: finish this test once we have system test framework
        // allowing more than one callback
        remoteConsole.prepare(new Login("test", "a"));
        // remoteConsole.prepare(new ListSamples("MASTER_PLATE", "3V", true, true));
        // remoteConsole.prepare(new CheckListSamples(new ArrayList<Sample>())).finish(10000);
        remoteConsole.finish(10000);
        client.onModuleLoad();
    }
}
