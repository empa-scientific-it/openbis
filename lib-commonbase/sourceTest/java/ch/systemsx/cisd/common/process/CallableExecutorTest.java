/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.process;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.util.concurrent.Callable;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test cases for the {@link CallableExecutor}.
 * 
 * @author Christian Ribeaud
 */
public final class CallableExecutorTest
{

    private Mockery context;

    private Callable<Boolean> callable;

    @SuppressWarnings("unchecked")
    @BeforeMethod
    public final void beforeMethod()
    {
        context = new Mockery();
        callable = context.mock(Callable.class);
    }

    @AfterMethod
    public final void afterMethod()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public final void testConstructor()
    {
        boolean fail = true;
        try
        {
            new CallableExecutor(-1, 1000L);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        fail = true;
        try
        {
            new CallableExecutor(0, -1L);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
    }

    @DataProvider(name = "maxRetryOnFailure")
    public final Object[][] getMaxRetryOnFailure()
    {
        return new Object[][]
        {
                { 0 },
                { 1 },
                { 3 } };
    }

    @Test(dataProvider = "maxRetryOnFailure")
    public final void testRunUnsuccessfulCallable(final int maxRetryOnFailure) throws Exception
    {
        context.checking(new Expectations()
            {
                {
                    if (maxRetryOnFailure == 0)
                    {
                        one(callable).call();
                    } else
                    {
                        exactly(maxRetryOnFailure).of(callable).call();
                    }
                    will(returnValue(null));
                }
            });
        assertEquals(null, new CallableExecutor(maxRetryOnFailure, 10L).executeCallable(callable));
        context.assertIsSatisfied();
    }

    @Test(dataProvider = "maxRetryOnFailure")
    public final void testRunSuccessfulCallable(final int maxRetryOnFailure) throws Exception
    {
        context.checking(new Expectations()
            {
                {
                    one(callable).call();
                    will(returnValue(false));
                }
            });
        assertEquals(Boolean.FALSE, new CallableExecutor(maxRetryOnFailure, 10L)
                .executeCallable(callable));
        context.assertIsSatisfied();
    }
}
