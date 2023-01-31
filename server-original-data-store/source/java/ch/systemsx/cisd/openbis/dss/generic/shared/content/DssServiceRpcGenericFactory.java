/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared.content;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;

/**
 * Implementation of {@link IDssServiceRpcGenericFactory} using on {@link HttpInvokerUtils} and caching services in a map.
 * 
 * @author Franz-Josef Elmer
 */
public class DssServiceRpcGenericFactory implements IDssServiceRpcGenericFactory
{
    private final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DssServiceRpcGenericFactory.class);

    private final Map<String, IDssServiceRpcGeneric> services =
            new HashMap<String, IDssServiceRpcGeneric>();

    @Override
    public IDssServiceRpcGeneric getService(String baseURL)
    {
        IDssServiceRpcGeneric service = services.get(baseURL);
        if (service == null)
        {
            String serviceURL = baseURL + "/datastore_server/rmi-dss-api-v1";
            service =
                    HttpInvokerUtils.createServiceStub(IDssServiceRpcGeneric.class, serviceURL,
                            300000);
            services.put(baseURL, service);
            operationLog.info("DSS remote service created. URL: " + serviceURL);
        }
        return service;
    }

}
