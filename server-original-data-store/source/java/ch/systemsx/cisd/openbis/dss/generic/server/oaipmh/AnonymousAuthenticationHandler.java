/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.oaipmh;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * @author pkupczyk
 */
public class AnonymousAuthenticationHandler implements IAuthenticationHandler
{

    private static final String PROPERTY_USER = "user";

    private static final String PROPERTY_PASSWORD = "password";

    private String user;

    private String password;

    @Override
    public void init(Properties properties)
    {
        user = (String) properties.get(PROPERTY_USER);

        if (user == null || user.isEmpty())
        {
            throw new IllegalArgumentException("'" + PROPERTY_USER + "' cannot be null or empty.");
        }

        password = (String) properties.get(PROPERTY_PASSWORD);
    }

    @Override
    public SessionContextDTO handle(HttpServletRequest request, HttpServletResponse response)
    {
        SessionContextDTO session = ServiceProvider.getOpenBISService().tryAuthenticate(user, password);

        if (session != null)
        {
            return session;
        } else
        {
            throw new IllegalArgumentException("Incorrect user or password specified in " + getClass().getName()
                    + " configuration. Please check the plugin.properties.");
        }
    }

}
