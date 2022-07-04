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

import java.util.List;

import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.server.IRemoteHostProvider;

/**
 * Implementation of this interface takes care of creating, managing and removing sessions.
 * <p>
 * It is a good idea to think about session synchronization when implementing this interface: you should synchronize access to <code>Session</code>
 * objects.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public interface ISessionManager<T extends BasicSession> extends IRemoteHostProvider
{

    /**
     * Opens a new session with given <code>user</code> and given <code>password</code>.
     *
     * @return A session token that is used afterwards to get the <code>Session</code> object, or <code>null</code>, if the user could not be
     *         authenticated.
     */
    public String tryToOpenSession(final String user, final String password);

    /**
     * Opens a new session for specified user and principal provider.
     */
    public String tryToOpenSession(String userID, IPrincipalProvider principalProvider);

    /**
     * Returns <code>true</code> if the specified session is active (not expired).
     */
    public boolean isSessionActive(String sessionToken);

    /**
     * Closes session by removing given <code>sessionToken</code> from active sessions.
     */
    public void closeSession(final String sessionToken) throws InvalidSessionException;

    /**
     * Expires session by removing given <code>sessionToken</code> from active sessions.
     */
    public void expireSession(final String sessionToken) throws InvalidSessionException;

    /**
     * Returns <code>true</code> if the specified string is a well-formed session token.
     */
    public boolean isAWellFormedSessionToken(String sessionTokenOrNull);

    /**
     * For given <var>sessionToken</var> return the <code>Session</code> object.
     * <p>
     * You should already be authenticated before calling this method.
     * </p>
     */
    public T getSession(final String sessionToken) throws InvalidSessionException;

    /**
     * Returns all sessions.
     */
    public List<T> getSessions();

    /**
     * For given <var>sessionToken</var> return the <code>Session</code> object, or <code>null</code>, if no session exist or the session is not
     * valid. This method will never touch or expire a session.
     * <p>
     * You should already be authenticated before calling this method.
     * </p>
     */
    public T tryGetSession(final String sessionToken);
    
    public void addListener(ISessionActionListener listener);

}