/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.time.DateFormatUtils;

import ch.systemsx.cisd.authentication.BasicSession;
import ch.systemsx.cisd.authentication.Principal;

/**
 * A small object that encapsulates information related to a session.
 * 
 * @author Christian Ribeaud
 */
public final class Session extends BasicSession implements IAuthSession
{
    private static final String BASE_URL_FALLBACK = "http://localhost/openbis/index.html";

    final private static long serialVersionUID = 1L;

    public interface ISessionCleaner
    {
        /**
         * Method called at the end of a session to perform some sort of cleanup.
         */
        public void cleanup();
    }

    /**
     * The {@link PersonPE} represented by this <code>Session</code> or <code>null</code> if it is not defined.
     */
    private PersonPE personOrNull;

    /**
     * A person that created this <code>Session</code> or <code>null</code> if it is not defined. For normal sessions <code>getPerson()</code> and
     * <code>getCreatorPerson()</code> return the same person. They can be different in case of on behalf sessions.
     */
    private PersonPE creatorPersonOrNull;

    /**
     * The base URL that the web server is reachable at.
     */
    private String baseIndexURL;

    private final Set<ISessionCleaner> cleanupListeners = new LinkedHashSet<ISessionCleaner>();

    @Deprecated
    public Session()
    {
        super();
    }

    public Session(final String user, final String sessionToken, final Principal principal,
            final String remoteHost, final long sessionStart)
    {
        this(user, sessionToken, principal, remoteHost, sessionStart, 0);
    }

    public Session(String userName, String sessionToken, Principal principal, String remoteHost,
            long sessionStart, int expirationTime)
    {
        super(sessionToken, userName, principal, remoteHost, sessionStart, expirationTime);
    }

    public Session(String userName, String sessionToken, Principal principal, String remoteHost,
            long sessionStart, int expirationTime, boolean isPersonalAccessTokenSession, String personalAccessTokenSessionName)
    {
        super(sessionToken, userName, principal, remoteHost, sessionStart, expirationTime, isPersonalAccessTokenSession,
                personalAccessTokenSessionName);
    }

    @Override
    public void cleanup()
    {
        super.cleanup();
        for (ISessionCleaner cleaner : cleanupListeners)
        {
            cleaner.cleanup();
        }
    }

    public final void setPerson(final PersonPE person)
    {
        this.personOrNull = person;
    }

    public final void setCreatorPerson(PersonPE creatorPerson)
    {
        this.creatorPersonOrNull = creatorPerson;
    }

    /**
     * Returns the {@link PersonPE} associated to this session or <code>null</code>.
     */
    @Override
    public final PersonPE tryGetPerson()
    {
        return personOrNull;
    }

    @Override
    public final PersonPE tryGetCreatorPerson()
    {
        return creatorPersonOrNull;
    }

    /** Returns the home group or <code>null</code>. */
    public final SpacePE tryGetHomeGroup()
    {
        if (personOrNull == null)
        {
            return null;
        }
        return personOrNull.getHomeSpace();
    }

    /** Returns home group code or <code>null</code>. */
    @Override
    public final String tryGetHomeGroupCode()
    {
        final SpacePE homeGroup = tryGetHomeGroup();
        if (homeGroup == null)
        {
            return null;
        }
        return homeGroup.getCode();
    }

    /**
     * Returns the base URL that the web server is reachable at (including "index.html")
     */
    public final String getBaseIndexURL()
    {
        return (baseIndexURL != null) ? baseIndexURL : BASE_URL_FALLBACK;
    }

    /**
     * Sets the base URL that the web server is reachable at.
     */
    public final void setBaseIndexURL(String baseIndexURL)
    {
        this.baseIndexURL = baseIndexURL;
    }

    @Deprecated
    public PersonPE getPerson()
    {
        return personOrNull;
    }

    @Override
    public String getUserName()
    {
        return personOrNull == null ? super.getUserName() : personOrNull.getUserId();
    }

    /**
     * Adds a new listener for session cleanup.
     */
    public void addCleanupListener(ISessionCleaner sessionCleaner)
    {
        cleanupListeners.add(sessionCleaner);
    }

    /**
     * Removes a registered listener for session cleanup.
     */
    public void removeCleanupListener(ISessionCleaner sessionCleaner)
    {
        cleanupListeners.remove(sessionCleaner);
    }

    //
    // Object
    //

    @Override
    public final String toString()
    {
        return "Session{user=" + getUserName() + ",space=" + tryGetHomeGroupCode()
                + ",sessionstart=" + DateFormatUtils.format(getSessionStart(), DATE_FORMAT_PATTERN)
                + "}";
    }

    public String getUserEmail()
    {
        return personOrNull == null ? getPrincipal().getEmail() : personOrNull.getEmail();
    }

    public boolean isOnBehalfSession()
    {
        String personUserId = tryGetPerson() != null ? tryGetPerson().getUserId() : null;
        String creatorUserId = tryGetCreatorPerson() != null ? tryGetCreatorPerson().getUserId() : null;
        boolean areEqual = personUserId == null ? creatorUserId == null : personUserId.equals(creatorUserId);
        return areEqual == false;
    }
}
