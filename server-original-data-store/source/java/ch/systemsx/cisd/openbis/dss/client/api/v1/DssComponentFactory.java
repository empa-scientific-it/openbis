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

package ch.systemsx.cisd.openbis.dss.client.api.v1;

import ch.systemsx.cisd.openbis.common.api.client.ServiceFinder;
import ch.systemsx.cisd.openbis.dss.client.api.v1.impl.DssComponent;

/**
 * A class that creates DssComponents.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DssComponentFactory
{
    private static final int DEFAULT_TIMEOUT = ServiceFinder.SERVER_TIMEOUT_IN_MINUTES * 60 * 1000;

    /**
     * Public factory method for creating an IDssComponent with a username and password.
     * 
     * @param user The user name
     * @param password The user's password
     * @param openBISUrl The URL to openBIS
     * @param timeoutInMillis network timeout when connecting to remote services
     */
    public static IDssComponent tryCreate(String user, String password, String openBISUrl,
            long timeoutInMillis)
    {
        return DssComponent.tryCreate(user, password, openBISUrl, timeoutInMillis);
    }

    /**
     * Public factory method for creating an IDssComponent for a user that has already been authenticated.
     * 
     * @param sessionToken The session token provided by authentication
     * @param openBISUrl The URL to openBIS
     * @param timeoutInMillis network timeout when connecting to remote services
     */
    public static IDssComponent tryCreate(String sessionToken, String openBISUrl,
            long timeoutInMillis)
    {
        return DssComponent.tryCreate(sessionToken, openBISUrl, timeoutInMillis);
    }

    /** See {@link #tryCreate(String, String, String, long)}. The timeout is fixed to 15 min. */
    public static IDssComponent tryCreate(String user, String password, String openBISUrl)
    {
        return DssComponent.tryCreate(user, password, openBISUrl, DEFAULT_TIMEOUT);
    }

    /** See {@link #tryCreate(String, String, long)}. The timeout is fixed to 15 min. */
    public static IDssComponent tryCreate(String sessionToken, String openBISUrl)
    {
        return DssComponent.tryCreate(sessionToken, openBISUrl, DEFAULT_TIMEOUT);
    }
}
