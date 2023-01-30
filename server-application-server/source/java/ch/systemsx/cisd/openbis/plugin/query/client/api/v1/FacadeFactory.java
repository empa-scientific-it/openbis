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

package ch.systemsx.cisd.openbis.plugin.query.client.api.v1;

import ch.systemsx.cisd.openbis.common.api.client.ServiceFinder;
import ch.systemsx.cisd.common.api.retry.RetryCaller;
import ch.systemsx.cisd.common.api.retry.RetryProxyFactory;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.IQueryApiServer;

/**
 * Factory of {@link IQueryApiFacade}.
 * 
 * @author Franz-Josef Elmer
 */
public class FacadeFactory
{
    private static final ServiceFinder QUERY_SERVICE_FINDER = new ServiceFinder("openbis",
            IQueryApiServer.QUERY_PLUGIN_SERVER_URL);

    private static final ServiceFinder GENERAL_INFORMATION_SERVICE_FINDER = new ServiceFinder(
            "openbis", IGeneralInformationService.SERVICE_URL);

    /**
     * Creates a facade for specified server URL, user Id, and password.
     */
    public static IQueryApiFacade create(final String serverURL, final String userID,
            final String password)
    {
        RetryCaller<IQueryApiFacade, RuntimeException> caller =
                new RetryCaller<IQueryApiFacade, RuntimeException>()
                    {
                        @Override
                        protected IQueryApiFacade call()
                        {
                            IQueryApiServer service = createQueryService(serverURL);
                            String sessionToken =
                                    service.tryToAuthenticateAtQueryServer(userID, password);
                            if (sessionToken == null)
                            {
                                throw new IllegalArgumentException("User " + userID
                                        + " couldn't be authenticated");
                            }
                            // Login at one service is enough
                            IQueryApiFacade facade =
                                    new QueryApiFacade(service,
                                            createGeneralInfoService(serverURL), sessionToken);

                            return RetryProxyFactory.createProxy(facade);
                        }
                    };
        return caller.callWithRetry();
    }

    /**
     * Creates a facade for specified url and sessionToken.
     */
    public static IQueryApiFacade create(final String serverURL, final String sessionToken)
    {
        RetryCaller<IQueryApiFacade, RuntimeException> caller =
                new RetryCaller<IQueryApiFacade, RuntimeException>()
                    {
                        @Override
                        protected IQueryApiFacade call()
                        {
                            IQueryApiFacade facade =
                                    new QueryApiFacade(createQueryService(serverURL),
                                            createGeneralInfoService(serverURL), sessionToken);

                            return RetryProxyFactory.createProxy(facade);
                        }
                    };
        return caller.callWithRetry();
    }

    private static IQueryApiServer createQueryService(String serverURL)
    {
        return QUERY_SERVICE_FINDER.createService(IQueryApiServer.class, serverURL);
    }

    private static IGeneralInformationService createGeneralInfoService(String serverURL)
    {
        return GENERAL_INFORMATION_SERVICE_FINDER.createService(IGeneralInformationService.class,
                serverURL);
    }
}
