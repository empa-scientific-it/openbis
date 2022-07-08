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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.IServiceForDataStoreServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * An implementation of the Apache {@link UserManager} interface, adapting openBIS users to FTP users.
 * 
 * @author Kaloyan Enimanev
 */
public class FtpUserManager implements UserManager
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            FtpUserManager.class);

    private final IServiceForDataStoreServer service;

    private final Map<String, String> sessionTokensByUser = Collections.synchronizedMap(new HashMap<>());

    public FtpUserManager(IServiceForDataStoreServer service)
    {
        this.service = service;
    }

    @Override
    public User authenticate(Authentication authentication) throws AuthenticationFailedException
    {
        if (authentication instanceof UsernamePasswordAuthentication)
        {
            UsernamePasswordAuthentication upa = (UsernamePasswordAuthentication) authentication;
            String user = upa.getUsername();
            String password = upa.getPassword();
            String key = String.format("%s:%s", user, password);
            String sessionToken = getSessionToken(key, user, password);
            if (sessionToken != null)
            {
                sessionToken = ServiceProvider.getPersonalAccessTokenConverter().convert(sessionToken);
                SessionContextDTO session = service.tryGetSession(sessionToken);
                if (session == null)
                {
                    sessionToken = null;
                } else
                {
                    user = session.getUserPersonObject().getUserId();
                    operationLog.info("User '" + user + "' authenticated via session token");
                    sessionToken = session.getSessionToken();
                }
            }
            if (sessionToken == null)
            {
                SessionContextDTO session = service.tryAuthenticate(user, password);
                sessionToken = session == null ? null : session.getSessionToken();
                sessionTokensByUser.put(key, sessionToken);
            }
            if (sessionToken != null)
            {
                return new FtpUser(user, sessionToken);
            }
        } else
        {
            operationLog.warn("Unsupported authentication type :" + authentication.getClass());
        }

        throw new AuthenticationFailedException();
    }

    public void close(User user, boolean noViews)
    {
        String key = getSessionKey(user);
        if (key != null)
        {
            String sessionToken = sessionTokensByUser.remove(key);
            operationLog.info("Session token " + sessionToken + " removed.");
            if (noViews)
            {
                service.logout(sessionToken);
                operationLog.info("Log out session " + sessionToken
                        + " because last file system session view for this session has been closed.");
            }
        }
    }

    private String getSessionKey(User user)
    {
        if (user instanceof FtpUser)
        {
            FtpUser ftpUser = (FtpUser) user;
            String sessionToken = ftpUser.getSessionToken();
            for (Entry<String, String> entry : sessionTokensByUser.entrySet())
            {
                if (sessionToken.equals(entry.getValue()))
                {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    private String getSessionToken(String key, String user, String password)
    {
        if ("?".equals(user))
        {
            return password;
        }
        return sessionTokensByUser.get(key);
    }

    @Override
    public void delete(String arg0) throws FtpException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean doesExist(String arg0) throws FtpException
    {
        return false;
    }

    @Override
    public String getAdminName() throws FtpException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] getAllUserNames() throws FtpException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public User getUserByName(String userName) throws FtpException
    {
        return new FtpUser(userName, null);
    }

    @Override
    public boolean isAdmin(String arg0) throws FtpException
    {
        return false;
    }

    @Override
    public void save(User arg0) throws FtpException
    {
        throw new UnsupportedOperationException();
    }
}
