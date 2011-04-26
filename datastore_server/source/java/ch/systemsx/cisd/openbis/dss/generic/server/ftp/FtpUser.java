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

package ch.systemsx.cisd.openbis.dss.generic.server.ftp;

import java.util.Collections;
import java.util.List;

import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.AuthorizationRequest;
import org.apache.ftpserver.ftplet.User;

/**
 * An implementation of the Apache {@link User} interface, additionally holding an authenticated
 * openBIS session token.
 * 
 * @author Kaloyan Enimanev
 */
public class FtpUser implements User
{
    private static final int SECONDS_PER_HOUR = 60 * 60;

    private final String name;

    private final String sessionToken;

    public FtpUser(String userName, String sessionToken)
    {
        this.name = userName;
        this.sessionToken = sessionToken;
    }

    public boolean getEnabled()
    {
        return true;
    }

    public String getHomeDirectory()
    {
        return "/";
    }

    public int getMaxIdleTime()
    {
        return SECONDS_PER_HOUR;
    }

    public String getName()
    {
        return name;
    }

    public String getPassword()
    {
        throw new UnsupportedOperationException();
    }

    public String getSessionToken()
    {
        return sessionToken;
    }

    public AuthorizationRequest authorize(AuthorizationRequest request)
    {
        // authorization is implemented by not showing the users datasets they cannot see.
        return request;
    }

    public List<Authority> getAuthorities()
    {
        return Collections.emptyList();
    }

    public List<Authority> getAuthorities(Class<? extends Authority> arg0)
    {
        return Collections.emptyList();
    }

}
