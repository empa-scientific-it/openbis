/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.api.retry;

import java.net.SocketTimeoutException;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.RemoteConnectFailureException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.api.retry.config.StaticRetryConfiguration;

/**
 * @author pkupczyk
 */
public class RetryCallerTest
{

    private Mockery mockery;

    private Runnable runnable;

    private StaticRetryConfiguration configuration;

    private RetryCaller<Object, Throwable> caller;

    @BeforeMethod
    public void beforeMethod()
    {
        mockery = new Mockery();
        runnable = mockery.mock(Runnable.class);

        configuration = new StaticRetryConfiguration();
        configuration.setMaximumNumberOfRetries(5);
        configuration.setWaitingTimeBetweenRetries(100);
        configuration.setWaitingTimeBetweenRetriesIncreasingFactor(2);

        caller = new RetryCaller<Object, Throwable>(configuration)
            {
                @Override
                protected Object call() throws Throwable
                {
                    runnable.run();
                    return null;
                }
            };
    }

    @Test
    public void testCallWithoutErrorShouldBeExecutedOnce() throws Throwable
    {
        mockery.checking(new Expectations()
            {
                {
                    oneOf(runnable).run();
                }
            });

        caller.callWithRetry();
        mockery.assertIsSatisfied();
    }

    @Test
    public void testCallWithConnectErrorShouldBeRetried() throws Throwable
    {
        mockery.checking(new Expectations()
            {
                {
                    oneOf(runnable).run();
                    will(throwException(new RemoteConnectFailureException("", null)));
                    oneOf(runnable).run();
                }
            });

        caller.callWithRetry();
        mockery.assertIsSatisfied();
    }

    @Test
    public void testCallWithTimeoutErrorShouldBeRetried() throws Throwable
    {
        mockery.checking(new Expectations()
            {
                {
                    oneOf(runnable).run();
                    will(throwException(new RemoteAccessException("", new SocketTimeoutException())));
                    oneOf(runnable).run();
                }
            });

        caller.callWithRetry();
        mockery.assertIsSatisfied();
    }

    @Test
    void testCallWithManyCommunicationErrorsShouldBeRetriedWithIncreasingWaitingTime()
            throws Throwable
    {
        final long startTime = System.currentTimeMillis();

        mockery.checking(new Expectations()
            {
                {
                    for (int i = 0; i < configuration.getMaximumNumberOfRetries(); i++)
                    {
                        final int ifinal = i;
                        oneOf(runnable).run();
                        will(new CustomAction("Check waiting time and throw exception")
                            {
                                @Override
                                public Object invoke(Invocation invocation) throws Throwable
                                {
                                    Assert.assertTrue(System.currentTimeMillis() >= startTime
                                            + (ifinal * configuration
                                                    .getWaitingTimeBetweenRetries()));
                                    throw new RemoteConnectFailureException("", null);
                                }
                            });
                    }
                    oneOf(runnable).run();
                }
            });

        caller.callWithRetry();
        mockery.assertIsSatisfied();
    }

    @Test(expectedExceptions =
    { RemoteConnectFailureException.class })
    void testCallWithTooManyCommunicationErrorsShouldFinallyThrowException() throws Throwable
    {
        mockery.checking(new Expectations()
            {
                {
                    for (int i = 0; i < configuration.getMaximumNumberOfRetries() + 1; i++)
                    {
                        oneOf(runnable).run();
                        will(throwException(new RemoteConnectFailureException("", null)));
                    }
                }
            });

        caller.callWithRetry();
        mockery.assertIsSatisfied();
    }

    @Test(expectedExceptions =
    { RuntimeException.class })
    void testCallWithNonCommunicationErrorShouldNotBeRetried() throws Throwable
    {
        mockery.checking(new Expectations()
            {
                {
                    oneOf(runnable).run();
                    will(throwException(new RuntimeException("", null)));
                }
            });

        caller.callWithRetry();
        mockery.assertIsSatisfied();
    }

}
