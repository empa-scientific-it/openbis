/*
 * Copyright ETH 2018 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.microservices.download.server.services.store;

import java.util.HashMap;
import java.util.Map;

import ch.ethz.sis.microservices.download.server.logging.LogManager;
import ch.ethz.sis.microservices.download.server.logging.log4j.Log4J2LogFactory;
import ch.ethz.sis.microservices.download.server.startup.HttpClient;
import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

public class AbstractFileServiceTest
{
    static
    {
        // Configuring Logging
        LogManager.setLogFactory(new Log4J2LogFactory());
    }

    public static void test(String openbisURL, String serviceURL, String user, String pass, Long offset) throws Exception
    {
        // Service
        String externalDMSCode = "ADMIN-BS-MBPR28.D.ETHZ.CH-E96954A7";
        String contentCopyPath = "/Users/localadmin/obis_data/data1";
        String datasetPermId = "20180523115921026-50";
        String datasetPathToFile = "openBIS-installation-standard-technologies-SNAPSHOT-r1526484921.tar.gz";

        // Obtain session token from openBIS
        int timeout = 10000;
        IApplicationServerApi v3As = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, openbisURL, timeout);
        String sessionToken = v3As.login(user, pass);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("sessionToken", sessionToken);
        parameters.put("datasetPermId", datasetPermId);
        parameters.put("externalDMSCode", externalDMSCode);
        parameters.put("contentCopyPath", contentCopyPath);
        parameters.put("datasetPathToFile", datasetPathToFile);
        if(offset != null) {
        		parameters.put("offset", offset.toString());
        }
        

        long start = System.currentTimeMillis();
        byte[] response = HttpClient.doGet(serviceURL, parameters);
        long end = System.currentTimeMillis();
        System.out.println("Response Size: " + response.length);
        System.out.println("Time: " + (end - start));
    }
}
