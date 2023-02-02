/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.googlecode.jsonrpc4j.spring.JsonServiceExporter;
import com.marathon.util.spring.StreamSupportingHttpInvokerServiceExporter;

/**
 * @author pkupczyk
 */
public class DssScreeningApplicationContext
{
    private static final String DSS_RPC_SERVICE = "data-store-rpc-service-screening";

    private static final String DSS_RPC_SERVICE_JSON = "data-store-rpc-service-screening-json";

    private static ApplicationContext instance = null;

    // applicationContex is lazily initialized
    private static boolean buildingApplicationContext;

    public static StreamSupportingHttpInvokerServiceExporter getDssRpcService()
    {
        return (StreamSupportingHttpInvokerServiceExporter) getInstance().getBean(DSS_RPC_SERVICE);
    }

    public static JsonServiceExporter getDssRpcServiceJson()
    {
        return (JsonServiceExporter) getInstance().getBean(DSS_RPC_SERVICE_JSON);
    }

    public static ApplicationContext getInstance()
    {
        if (instance == null)
        {
            synchronized (DssScreeningApplicationContext.class)
            {
                if (instance == null)
                {
                    if (buildingApplicationContext)
                    {
                        throw new IllegalStateException("Building application context. "
                                + "Application context hasn't been built completely. "
                                + "Beans should access other beans lazily.");
                    }
                    buildingApplicationContext = true;
                    instance = new ClassPathXmlApplicationContext(new String[]
                    { "screening-dssApplicationContext.xml" }, true)
                        {
                            {
                                setDisplayName("Application Context from { screening-dssApplicationContext.xml }");
                            }
                        };
                    buildingApplicationContext = false;
                }
            }
        }
        return instance;
    }

}
