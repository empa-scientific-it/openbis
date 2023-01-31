/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.client.api.v3;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.systemsx.cisd.common.api.retry.RetryCaller;
import ch.systemsx.cisd.common.api.retry.RetryProxyFactory;
import ch.systemsx.cisd.openbis.common.api.client.ServiceFinder;

/**
 * @author Franz-Josef Elmer
 */
public class OpenbisServiceFacadeV3 implements IOpenbisServiceFacadeV3
{
    public static IOpenbisServiceFacadeV3 tryCreate(final String username, final String password,
            final String openbisUrl, final long timeoutInMillis)
    {
        RetryCaller<IOpenbisServiceFacadeV3, RuntimeException> caller =
                new RetryCaller<IOpenbisServiceFacadeV3, RuntimeException>()
                    {
                        @Override
                        protected IOpenbisServiceFacadeV3 call()
                        {
                            IApplicationServerApi service = createService(openbisUrl, timeoutInMillis);
                            String sessionToken = service.login(username, password);
                            OpenbisServiceFacadeV3 facade = new OpenbisServiceFacadeV3(sessionToken, service);
                            return RetryProxyFactory.createProxy(facade);
                        }
                    };
        return caller.callWithRetry();
    }

    public static IOpenbisServiceFacadeV3 tryCreate(final String sessionToken,
            final String openbisUrl, final long timeoutInMillis)
    {
        RetryCaller<IOpenbisServiceFacadeV3, RuntimeException> caller =
                new RetryCaller<IOpenbisServiceFacadeV3, RuntimeException>()
        {
            @Override
            protected IOpenbisServiceFacadeV3 call()
            {
                IApplicationServerApi service = createService(openbisUrl, timeoutInMillis);
                OpenbisServiceFacadeV3 facade = new OpenbisServiceFacadeV3(sessionToken, service);
                return RetryProxyFactory.createProxy(facade);
            }
        };
        return caller.callWithRetry();
    }
    
    private static IApplicationServerApi createService(String openbisUrl, long timeoutInMillis)
    {
        ServiceFinder serviceFinder = new ServiceFinder("openbis", IApplicationServerApi.SERVICE_URL);
        return serviceFinder.createService(IApplicationServerApi.class, openbisUrl, timeoutInMillis);
    }
    
    private String sessionToken;
    private IApplicationServerApi service;
    
    private OpenbisServiceFacadeV3(String sessionToken, IApplicationServerApi service)
    {
        this.sessionToken = sessionToken;
        this.service = service;
    }

    @Override
    public String getSessionToken()
    {
        return sessionToken;
    }

    @Override
    public SearchResult<Sample> searchSamples(SampleSearchCriteria searchCriteria, SampleFetchOptions fetchOptions)
    {
        return service.searchSamples(sessionToken, searchCriteria, fetchOptions);
    }
}
