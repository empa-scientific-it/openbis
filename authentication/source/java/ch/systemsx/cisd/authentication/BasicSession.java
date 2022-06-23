/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.authentication;

import java.io.Serializable;

import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * Basic session object.
 *
 * @author Franz-Josef Elmer
 */
public class BasicSession implements Serializable
{
    protected static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private static final long serialVersionUID = 1L;

    private String sessionToken;

    private String userName;

    private Principal principal;

    private String remoteHost;

    private long sessionStart;

    private int sessionExpirationTime;

    private boolean isPersonalAccessTokenSession;

    private String personalAccessTokenSessionName;

    @Deprecated
    public BasicSession()
    {
    }

    /**
     * Creates an instance from the specified session token, user name, principal, remoteHost, and session start (in milliseconds since start of the
     * epoch).
     */
    public BasicSession(String sessionToken, String userName,
            Principal principal, String remoteHost, long sessionStart, int sessionExpirationTime)
    {
        assert sessionToken != null : "Given session token can not be null.";
        assert userName != null : "Given user name can not be null.";
        assert principal != null : "Given principal can not be null.";
        assert sessionStart > 0 : "Given session start must be larger than zero.";
        assert remoteHost != null : "Given remote host can not be null";
        assert sessionExpirationTime >= 0;

        this.sessionToken = sessionToken;
        this.userName = userName;
        this.principal = principal;
        this.remoteHost = remoteHost;
        this.sessionStart = sessionStart;
        this.sessionExpirationTime = sessionExpirationTime;
    }

    public BasicSession(String sessionToken, String userName,
            Principal principal, String remoteHost, long sessionStart, int sessionExpirationTime, boolean isPersonalAccessTokenSession,
            String personalAccessTokenSessionName)
    {
        this(sessionToken, userName, principal, remoteHost, sessionStart, sessionExpirationTime);
        this.isPersonalAccessTokenSession = isPersonalAccessTokenSession;
        this.personalAccessTokenSessionName = personalAccessTokenSessionName;
    }

    /**
     * Returns the unique session ID.
     */
    public final String getSessionToken()
    {
        return sessionToken;
    }

    /**
     * Returns the owner of the session.
     */
    public String getUserName()
    {
        return userName;
    }

    /**
     * Returns full information about the user.
     */
    public final Principal getPrincipal()
    {
        return principal;
    }

    /**
     * Returns 'true' if the session is anonymous.
     */
    public final boolean isAnonymous()
    {
        return principal.isAnonymous();
    }

    /**
     * Returns the remote host.
     */
    public final String getRemoteHost()
    {
        return remoteHost;
    }

    /**
     * Returns the time when the session has been started (in milliseconds since start of the epoch).
     */
    public final long getSessionStart()
    {
        return sessionStart;
    }

    /**
     * Returns the expiration time of this session in milliseconds.
     */
    public final int getSessionExpirationTime()
    {
        return sessionExpirationTime;
    }

    public boolean isPersonalAccessTokenSession()
    {
        return isPersonalAccessTokenSession;
    }

    public String getPersonalAccessTokenSessionName()
    {
        return personalAccessTokenSessionName;
    }

    /**
     * Called when the session is closed. Can perform additional cleanup tasks.
     */
    public void cleanup()
    {
    }

    @Deprecated
    public void setSessionToken(String sessionToken)
    {
        this.sessionToken = sessionToken;
    }

    @Deprecated
    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    @Deprecated
    public void setPrincipal(Principal principal)
    {
        this.principal = principal;
    }

    @Deprecated
    public void setRemoteHost(String remoteHost)
    {
        this.remoteHost = remoteHost;
    }

    @Deprecated
    public void setSessionStart(long sessionStart)
    {
        this.sessionStart = sessionStart;
    }

    @Deprecated
    public void setSessionExpirationTime(int sessionExpirationTime)
    {
        this.sessionExpirationTime = sessionExpirationTime;
    }

    @Override
    public String toString()
    {
        return "BasicSession{user=" + userName + ",remoteHost=" + remoteHost + ",sessionstart="
                + DateFormatUtils.format(sessionStart, DATE_FORMAT_PATTERN) + "}";
    }
}
