/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared.content;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Franz-Josef Elmer
 */
public class DelayedPersistenceManagerTest extends AssertJUnit
{
    private static final int DELAY = 500;

    private Mockery context;

    private IPersistenceManager persistenceManager;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        persistenceManager = context.mock(IPersistenceManager.class);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testRequestPersistence() throws Exception
    {
        context.checking(new Expectations()
            {
                {
                    exactly(3).of(persistenceManager).requestPersistence();
                }
            });

        DelayedPersistenceManager manager = new DelayedPersistenceManager(persistenceManager, DELAY);

        manager.requestPersistence();
        manager.requestPersistence();
        manager.requestPersistence();
        Thread.sleep(2 * DELAY);
        manager.requestPersistence();
        manager.requestPersistence();
        Thread.sleep(2 * DELAY);
        manager.requestPersistence();
        Thread.sleep(2 * DELAY);

        context.assertIsSatisfied();
    }

}
