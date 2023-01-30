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

package ch.systemsx.cisd.openbis.plugin.demo.server;

import org.testng.annotations.BeforeMethod;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.plugin.demo.shared.IDemoServer;

/**
 * Test cases for corresponding {@link DemoServer} class.
 * 
 * @author Christian Ribeaud
 */
@Friend(toClasses = DemoServer.class)
public final class DemoServerTest extends AbstractServerTestCase
{
    private IDemoBusinessObjectFactory demoBusinessObjectFactory;

    private ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin;

    private IDataSetTypeSlaveServerPlugin dataSetTypeSlaveServerPlugin;

    @SuppressWarnings("unused")
    private final IDemoServer createServer()
    {
        return new DemoServer(sessionManager, daoFactory, propertiesBatchManager,
                demoBusinessObjectFactory, sampleTypeSlaveServerPlugin,
                dataSetTypeSlaveServerPlugin);
    }

    //
    // AbstractServerTestCase
    //

    @Override
    @BeforeMethod
    public final void setUp()
    {
        super.setUp();
        sampleTypeSlaveServerPlugin = context.mock(ISampleTypeSlaveServerPlugin.class);
        demoBusinessObjectFactory = context.mock(IDemoBusinessObjectFactory.class);
        dataSetTypeSlaveServerPlugin = context.mock(IDataSetTypeSlaveServerPlugin.class);
    }
}
