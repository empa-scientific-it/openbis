/*
 * Copyright 2014 ETH Zuerich, CISD
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
import ch.systemsx.cisd.openbis.common.api.client.ServiceFinder;

/**
 * @author Jakub Straszewski
 */
public abstract class AbstractOpenBisServiceFactory<T>
{
    private final String initialServerUrl;

    private final String urlServiceSuffix;

    private final Class<T> clazz;

    /**
     * Constructor for the OpenBisServiceFactory. The service factory works best when the serverUrl is simply the protocol://machine:port of the
     * openBIS application server. It will automatically append likely locations of the openBIS service to the url.
     * <p>
     * Examples:
     * <ul>
     * <li>OpenBisServiceFactory("http://localhost:8888/", stubFactory)</li>
     * <li>OpenBisServiceFactory("https://openbis.ethz.ch:8443/", stubFactory)</li>
     * </ul>
     * 
     * @param serverUrl The Url where the openBIS server is assumed to be.
     * @param urlServiceSuffix The suffix appended to the url to refer to the service.
     */
    public AbstractOpenBisServiceFactory(String serverUrl, String urlServiceSuffix, Class<T> clazz)
    {
        this.initialServerUrl = serverUrl;
        this.urlServiceSuffix = urlServiceSuffix;
        this.clazz = clazz;
    }

    /**
     * Create a IETLLIMSService by trying several possible locations for the service until one that works is found. If the service cannot be found, a
     * proxy to the constructor-provided serverUrl will be returned.
     */
    public T createService()
    {
        return createServiceFinder().createService(clazz, initialServerUrl,
                createServicePinger());
    }

    /**
     * Create a IETLLIMSService by trying several possible locations for the service until one that works is found. If the service cannot be found, a
     * proxy to the constructor-provided serverUrl will be returned.
     */
    public T createService(long timeoutInMillis)
    {
        return createServiceFinder().createService(clazz, initialServerUrl,
                createServicePinger(), timeoutInMillis);
    }

    private ServiceFinder createServiceFinder()
    {
        return new ServiceFinder("openbis", urlServiceSuffix);
    }

    protected abstract IServicePinger<T> createServicePinger();
}
