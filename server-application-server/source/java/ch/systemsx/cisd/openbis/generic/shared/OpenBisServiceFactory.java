/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.shared;

import ch.systemsx.cisd.openbis.common.api.client.IServicePinger;

/**
 * A factory for creating proxies to the openBIS application server.
 * <p>
 * The OpenBisServiceFactory will create a proxy by trying several possible locations for the service.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class OpenBisServiceFactory extends AbstractOpenBisServiceFactory<IServiceForDataStoreServer>
{

    /**
     * Constructor for the OpenBisServiceFactory. The service factory works best when the serverUrl is simply the protocol://machine:port of the
     * openBIS application server. It will automatically append likely locations of the openBIS service to the url.
     * <p>
     * Examples:
     * <ul>
     * <li>OpenBisServiceFactory("http://localhost:8888/")</li>
     * <li>OpenBisServiceFactory("https://openbis.ethz.ch:8443/")</li>
     * </ul>
     * 
     * @param serverUrl The Url where the openBIS server is assumed to be.
     */
    public OpenBisServiceFactory(String serverUrl)
    {
        super(serverUrl, ResourceNames.ETL_SERVICE_URL, IServiceForDataStoreServer.class);
    }

    @Override
    protected IServicePinger<IServiceForDataStoreServer> createServicePinger()
    {
        return new IServicePinger<IServiceForDataStoreServer>()
            {
                @Override
                public void ping(IServiceForDataStoreServer service)
                {
                    service.getVersion();
                }
            };
    }

}
