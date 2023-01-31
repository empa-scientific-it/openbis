/*
 * Copyright 2014 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.File;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;

/**
 * @author Franz-Josef Elmer
 */
public class KeyBasedDataSetCommandExecutorProviderTest extends AbstractFileSystemTestCase
{
    private static final String COMMAND_QUEUE_DIR = "queue";

    private Mockery context;

    private IDataSetCommandExecutorFactory executorFactory;

    private File store;

    private IDataSetCommandExecutor executor1;

    private IDataSetCommandExecutor executor2;

    private IDataSetCommandExecutor defaultExecutor;

    @BeforeMethod
    public void setup()
    {
        context = new Mockery();
        executorFactory = context.mock(IDataSetCommandExecutorFactory.class);
        executor1 = context.mock(IDataSetCommandExecutor.class, "executor1");
        executor2 = context.mock(IDataSetCommandExecutor.class, "executor2");
        defaultExecutor = context.mock(IDataSetCommandExecutor.class, "defaultExecutor");
        store = new File(workingDirectory, "store");
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testInvalidMappingDefinitions()
    {
        assertInvalidMapping("Missing ':' in mapping definition: abc.*", "abc.*");
        assertInvalidMapping("Unspecified executor name in mapping definition:  : abc.* ", " : abc.* ");
        assertInvalidMapping("Invalid regex in mapping definition: q:abc?*", "q:abc?*");
    }

    private void assertInvalidMapping(String expectedErrorMessage, String mappingDefinition)
    {
        try
        {
            new KeyBasedDataSetCommandExecutorProvider(mappingDefinition, COMMAND_QUEUE_DIR, executorFactory);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals(expectedErrorMessage, ex.getMessage());
        }

    }

    @Test
    public void testMapping()
    {
        KeyBasedDataSetCommandExecutorProvider provider =
                new KeyBasedDataSetCommandExecutorProvider(" q1 : abc.*  ,  q2:my-task", COMMAND_QUEUE_DIR, executorFactory);
        prepareExecutorFactory("q1", "q2");
        provider.init(store);

        assertSame(executor1, provider.getExecutor(null, "abc1"));
        assertSame(executor2, provider.getExecutor(null, "my-task"));
        assertSame(defaultExecutor, provider.getExecutor(null, "another-task"));
        assertSame(defaultExecutor, provider.getDefaultExecutor());

        context.assertIsSatisfied();
    }

    @Test
    public void testMappingWithNoAdditionalQueues()
    {
        KeyBasedDataSetCommandExecutorProvider provider =
                new KeyBasedDataSetCommandExecutorProvider("${data-set-command-executor-mapping}",
                        COMMAND_QUEUE_DIR, executorFactory);
        prepareExecutorFactoryForDefaultExecutor();
        provider.init(store);

        assertSame(defaultExecutor, provider.getExecutor(null, "my-task"));
        assertSame(defaultExecutor, provider.getDefaultExecutor());

        context.assertIsSatisfied();
    }

    private void prepareExecutorFactory(final String queue1, final String queue2)
    {
        context.checking(new Expectations()
            {
                {
                    one(executorFactory).create(store, new File(COMMAND_QUEUE_DIR), queue1);
                    will(returnValue(executor1));
                    one(executor1).start();

                    one(executorFactory).create(store, new File(COMMAND_QUEUE_DIR), queue2);
                    will(returnValue(executor2));
                    one(executor2).start();
                }
            });
        prepareExecutorFactoryForDefaultExecutor();
    }

    private void prepareExecutorFactoryForDefaultExecutor()
    {
        context.checking(new Expectations()
            {
                {
                    one(executorFactory).create(store, new File(COMMAND_QUEUE_DIR), "");
                    will(returnValue(defaultExecutor));
                    one(defaultExecutor).start();

                }
            });
    }

}
