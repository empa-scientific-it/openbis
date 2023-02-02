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

package ch.systemsx.cisd.openbis.common.api.client;

import org.apache.commons.lang3.time.DateUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.api.IRpcService;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = ServiceFinder.class)
public class ServiceFinderTest extends AssertJUnit
{
    private static final long SERVER_TIMEOUT = ServiceFinder.SERVER_TIMEOUT_IN_MINUTES * DateUtils.MILLIS_PER_MINUTE;

    private static interface ITestService
    {
        public void ping();
    }

    private static interface ITestRpcService extends IRpcService
    {
    }

    private Mockery context;

    private ITestService service;

    private ITestRpcService rpcService;

    private IServiceStubFactory stubFactory;

    private ServiceFinder finder;

    private IServicePinger<ITestService> pinger = new IServicePinger<ITestService>()
        {

            @Override
            public void ping(ITestService testService)
            {
                testService.ping();

            }
        };

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        service = context.mock(ITestService.class);
        rpcService = context.mock(ITestRpcService.class);
        stubFactory = context.mock(IServiceStubFactory.class);
        finder = new ServiceFinder("name", "service", stubFactory);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testBasicUse()
    {
        context.checking(new Expectations()
            {
                {
                    one(stubFactory).createServiceStub(ITestService.class,
                            "http://localhost/name/name/service", SERVER_TIMEOUT);
                    will(returnValue(service));

                    one(service).ping();
                }
            });

        finder.createService(ITestService.class, "http://localhost", pinger);

        context.assertIsSatisfied();
    }

    @Test
    public void testBasicUseForRpcService()
    {
        context.checking(new Expectations()
            {
                {
                    one(stubFactory).createServiceStub(ITestRpcService.class,
                            "http://localhost/name/name/service", SERVER_TIMEOUT);
                    will(returnValue(rpcService));

                    one(rpcService).getMajorVersion();
                    will(returnValue(1));
                }
            });

        finder.createService(ITestRpcService.class, "http://localhost/");

        context.assertIsSatisfied();
    }

    @Test
    public void testAlternateLocation()
    {
        context.checking(new Expectations()
            {
                {
                    one(stubFactory).createServiceStub(ITestService.class,
                            "http://localhost/name/name/service", SERVER_TIMEOUT);
                    will(returnValue(service));

                    one(service).ping();
                    will(throwException(new RuntimeException()));

                    one(stubFactory)
                            .createServiceStub(ITestService.class, "http://localhost/name/service", SERVER_TIMEOUT);
                    will(returnValue(service));

                    one(service).ping();
                }
            });

        finder.createService(ITestService.class, "http://localhost", pinger);

        context.assertIsSatisfied();
    }

    @Test
    public void testLocationAlreadySpecified()
    {
        context.checking(new Expectations()
            {
                {
                    one(stubFactory).createServiceStub(ITestService.class,
                            "http://localhost/name/name/service", SERVER_TIMEOUT);
                    will(returnValue(service));

                    one(service).ping();
                }
            });

        finder.createService(ITestService.class, "http://localhost/name/name", pinger);

        context.assertIsSatisfied();
    }

}
