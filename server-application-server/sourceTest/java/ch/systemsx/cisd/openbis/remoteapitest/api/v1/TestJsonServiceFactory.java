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

package ch.systemsx.cisd.openbis.remoteapitest.api.v1;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationChangingService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.json.GenericObjectMapper;
import ch.systemsx.cisd.openbis.generic.shared.util.TestInstanceHostUtils;

/**
 * @author Kaloyan Enimanev
 */
public class TestJsonServiceFactory
{
    private static final String OPENBIS_SERVER_URL = TestInstanceHostUtils.getOpenBISUrl()
            + "/openbis/";

    private static final String GENERAL_INFO_SERVICE_URL = OPENBIS_SERVER_URL
            + IGeneralInformationService.JSON_SERVICE_URL;

    private static final String GENERAL_INFO_CHANGING_SERVICE_URL = OPENBIS_SERVER_URL
            + IGeneralInformationChangingService.JSON_SERVICE_URL;

    static IGeneralInformationService createGeneralInfoService()
    {
        try
        {
            GenericObjectMapper mapper = new GenericObjectMapper();
            JsonRpcHttpClient client =
                    new JsonRpcHttpClient(mapper, new URL(GENERAL_INFO_SERVICE_URL),
                            new HashMap<String, String>());
            return ProxyUtil.createClientProxy(TestJsonServiceFactory.class.getClassLoader(),
                    IGeneralInformationService.class, client);
        } catch (MalformedURLException ex)
        {
            throw new RuntimeException("Failed to initialize json-rpc client: " + ex.getMessage(),
                    ex);
        }
    }

    static IGeneralInformationChangingService createGeneralInfoChangingService()
    {
        try
        {
            GenericObjectMapper mapper = new GenericObjectMapper();
            JsonRpcHttpClient client =
                    new JsonRpcHttpClient(mapper, new URL(GENERAL_INFO_CHANGING_SERVICE_URL),
                            new HashMap<String, String>());
            return ProxyUtil.createClientProxy(TestJsonServiceFactory.class.getClassLoader(),
                    IGeneralInformationChangingService.class, client);
        } catch (MalformedURLException ex)
        {
            throw new RuntimeException("Failed to initialize json-rpc client: " + ex.getMessage(),
                    ex);
        }
    }

}
