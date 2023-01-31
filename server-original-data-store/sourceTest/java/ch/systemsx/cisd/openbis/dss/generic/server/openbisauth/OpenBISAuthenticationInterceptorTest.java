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

package ch.systemsx.cisd.openbis.dss.generic.server.openbisauth;

import static org.testng.AssertJUnit.assertEquals;

import java.io.File;
import java.util.Arrays;

import org.aopalliance.intercept.MethodInvocation;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.openbis.dss.generic.server.SessionTokenManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSourceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PluginUtilTest;
import ch.systemsx.cisd.openbis.generic.shared.IServiceForDataStoreServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServerInfo;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSourceDefinition;
import ch.systemsx.cisd.openbis.generic.shared.dto.OpenBISSessionHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * Test for {@link OpenBISAuthenticationInterceptor}.
 * 
 * @author Kaloyan Enimanev
 */
public class OpenBISAuthenticationInterceptorTest
{
    private static final int PORT = 4711;

    private static final String DOWNLOAD_URL = "download-url";

    private static final String DATA_STORE_CODE = "DATA-STORE-CODE1";

    private static final String LIMS_USER = "testuser";

    private static final String LIMS_PASSWORD = "testpassword";

    private static final String VALiD_SESSION_TOKEN = "valid-session";

    private Mockery context;

    private IServiceForDataStoreServer limsService;

    private OpenBISAuthenticationInterceptor interceptor;

    private MethodInvocation methodInvocation;

    private OpenBISSessionHolder sessionHolder;

    private IDataSourceProvider dataSourceProvider;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        limsService = context.mock(IServiceForDataStoreServer.class);
        methodInvocation = context.mock(MethodInvocation.class);
        dataSourceProvider = context.mock(IDataSourceProvider.class);

        sessionHolder = new OpenBISSessionHolder();
        sessionHolder.setDataStoreCode(DATA_STORE_CODE);

        interceptor =
                new OpenBISAuthenticationInterceptor(new SessionTokenManager(), limsService,
                        PluginUtilTest.createPluginTaskProviders(new File(".")),
                        dataSourceProvider, sessionHolder);

        interceptor.setUsername(LIMS_USER);
        interceptor.setPassword(LIMS_PASSWORD);
        interceptor.setDownloadUrl(DOWNLOAD_URL);
        interceptor.setPort(PORT);
    }

    @AfterMethod
    public void tearDown()
    {
        context.assertIsSatisfied();
    }

    @Test
    public final void testSessionTokenExpired() throws Throwable
    {
        sessionHolder.setSessionToken("expiredSessionToken");
        context.checking(new Expectations()
            {
                {
                    one(methodInvocation).proceed();
                    will(throwException(new InvalidSessionException("error")));
                    setUpAuthenticationExpectations(this);
                    one(methodInvocation).proceed();

                    one(dataSourceProvider).getAllDataSourceDefinitions();
                    will(returnValue(Arrays.asList(DataSourceDefinition.fromString("code=a"))));
                }
            });

        interceptor.invoke(methodInvocation);

        // the interceptor will reauthenticate and set the new session token
        assertEquals(VALiD_SESSION_TOKEN, sessionHolder.getSessionToken());
    }

    @Test
    public final void testInitializeSessionToken() throws Throwable
    {
        sessionHolder.setSessionToken(null);
        context.checking(new Expectations()
            {
                {
                    setUpAuthenticationExpectations(this);
                    one(methodInvocation).proceed();

                    one(dataSourceProvider).getAllDataSourceDefinitions();
                    will(returnValue(Arrays.asList(DataSourceDefinition.fromString("code=a"))));
                }
            });

        interceptor.invoke(methodInvocation);

        // the interceptor will reauthenticate and set the new session token
        assertEquals(VALiD_SESSION_TOKEN, sessionHolder.getSessionToken());
    }

    @Test
    public final void testNoReauthenticationNeeded() throws Throwable
    {
        sessionHolder.setSessionToken(VALiD_SESSION_TOKEN);
        context.checking(new Expectations()
            {
                {
                    one(methodInvocation).proceed();
                }
            });

        interceptor.invoke(methodInvocation);

        // the interceptor will reauthenticate and set the new session token
        assertEquals(VALiD_SESSION_TOKEN, sessionHolder.getSessionToken());
    }

    private SessionContextDTO createSession()
    {
        SessionContextDTO session = new SessionContextDTO();
        session.setSessionToken(VALiD_SESSION_TOKEN);
        return session;
    }

    private Matcher<DataStoreServerInfo> createDataStoreServerInfoMatcher()
    {
        return new BaseMatcher<DataStoreServerInfo>()
            {

                @Override
                public boolean matches(Object o)
                {
                    if (o instanceof DataStoreServerInfo)
                    {
                        DataStoreServerInfo info = (DataStoreServerInfo) o;
                        return DATA_STORE_CODE.equals(info.getDataStoreCode())
                                && DOWNLOAD_URL.equals(info.getDownloadUrl())
                                && PORT == info.getPort()
                                && info.getServicesDescriptions()
                                        .getProcessingServiceDescriptions().size() == 0
                                && info.getServicesDescriptions().getReportingServiceDescriptions()
                                        .size() == 0
                                && info.getDataSourceDefinitions().toString().equals("[code=a\t]");
                    }
                    return false;
                }

                @Override
                public void describeTo(Description description)
                {
                }

            };
    }

    private void setUpAuthenticationExpectations(final Expectations exp)
    {
        exp.one(limsService).tryAuthenticate(LIMS_USER, LIMS_PASSWORD);
        exp.will(Expectations.returnValue(createSession()));

        exp.one(limsService).registerDataStoreServer(
                exp.with(Expectations.equal(VALiD_SESSION_TOKEN)),
                exp.with(createDataStoreServerInfoMatcher()));
    }

}
