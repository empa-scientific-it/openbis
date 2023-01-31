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

package ch.systemsx.cisd.openbis.dss.generic.shared;

import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.systemsx.cisd.openbis.common.api.client.IServicePinger;
import ch.systemsx.cisd.openbis.generic.shared.AbstractOpenBisServiceFactory;

/**
 * A factory for creating proxies to the V3 datastore server.
 * <p>
 * The DataStoreServerV3Factory will create a proxy by trying several possible locations for the service.
 * 
 * @author anttil
 */
public class DataStoreServerV3Factory extends AbstractOpenBisServiceFactory<IDataStoreServerApi>
{

    /**
     * Constructor for the DataStoreServerV3Factory.
     * <p>
     * Example: DataStoreServerV3Factory("http://localhost:8889/datastore_server")
     * 
     * @param serverUrl The Url where the datastore server is.
     */
    public DataStoreServerV3Factory(String serverUrl)
    {
        super(serverUrl, IDataStoreServerApi.SERVICE_URL, IDataStoreServerApi.class);
    }

    @Override
    protected IServicePinger<IDataStoreServerApi> createServicePinger()
    {
        return new IServicePinger<IDataStoreServerApi>()
            {
                @Override
                public void ping(IDataStoreServerApi service)
                {
                    service.getMajorVersion();
                }
            };
    }

}
