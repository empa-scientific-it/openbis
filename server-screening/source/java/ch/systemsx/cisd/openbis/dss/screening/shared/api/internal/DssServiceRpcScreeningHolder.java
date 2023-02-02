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

package ch.systemsx.cisd.openbis.dss.screening.shared.api.internal;

import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.v1.IDssServiceRpcScreening;

/**
 * An object that holds a {@link IDssServiceRpcScreening} and its version.
 * 
 * @author Bernd Rinn
 */
public class DssServiceRpcScreeningHolder
{

    private final IDssServiceRpcScreening service;

    private final String serverUrl;

    private Integer majorVersion;

    private Integer minorVersion;

    public DssServiceRpcScreeningHolder(String serverUrl, IDssServiceRpcScreening service)
    {
        this.serverUrl = serverUrl;
        this.service = service;
    }

    public DssServiceRpcScreeningHolder(String serverUrl, int majorVersion, long timeout)
    {
        this(serverUrl, createService(serverUrl, majorVersion, timeout));
    }

    private static IDssServiceRpcScreening createService(String serverUrl, int majorVersion,
            long timeout)
    {
        return HttpInvokerUtils.createStreamSupportingServiceStub(IDssServiceRpcScreening.class,
                serverUrl + "/rmi-datastore-server-screening-api-v" + majorVersion + "/", timeout);
    }

    public IDssServiceRpcScreening getService()
    {
        return service;
    }

    public String getServerUrl()
    {
        return serverUrl;
    }

    public int getMajorVersion()
    {
        if (majorVersion == null)
        {
            majorVersion = service.getMajorVersion();
        }
        return majorVersion;
    }

    public int getMinorVersion()
    {
        if (minorVersion == null)
        {
            minorVersion = service.getMinorVersion();
        }
        return minorVersion;
    }

}