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

package ch.systemsx.cisd.openbis.dss.generic.server.openbisauth;

/**
 * A class that holds information about the openBIS session. It has the information necessary to
 * connect to any of the openBIS APIs.
 * 
 * @author Kaloyan Enimanev
 */
public class OpenBISSessionHolder
{
    private String token;

    private String dataStoreCode;

    private String serverUrl;

    public String getServerUrl()
    {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl)
    {
        this.serverUrl = serverUrl;
    }

    public String getDataStoreCode()
    {
        return dataStoreCode;
    }

    public void setDataStoreCode(String dataStoreCode)
    {
        this.dataStoreCode = dataStoreCode.toUpperCase();
    }

    public String getToken()
    {
        return token;
    }

    public void setToken(String sessionToken)
    {
        this.token = sessionToken;
    }

}
