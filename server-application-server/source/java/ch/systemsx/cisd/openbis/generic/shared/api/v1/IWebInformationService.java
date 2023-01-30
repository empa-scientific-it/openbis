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

package ch.systemsx.cisd.openbis.generic.shared.api.v1;

import ch.systemsx.cisd.common.api.IRpcService;

/**
 * Service for retrieving web information.
 * 
 * @author Piotr Kupczyk
 */
public interface IWebInformationService extends IRpcService
{
    /**
     * Name of this service for which it is registered at the RPC name server.
     */
    public static final String SERVICE_NAME = "web-information";

    /**
     * Application part of the URL to access this service remotely.
     */
    public static final String JSON_SERVICE_URL = "/rmi-" + SERVICE_NAME + "-v1.json";

    /**
     * Returns the server side session token for the current HTTP session.
     */
    public String getSessionToken();

}
