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

package ch.systemsx.cisd.openbis.generic.shared;

import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomColumn;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.displaysettings.IDisplaySettingsUpdate;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * An basic server.
 * 
 * @author Christian Ribeaud
 */
public interface IServer extends ISessionProvider
{
    /**
     * Every time some public class in this package or subpackage is changed, we should increment the appropriate variable.
     */
    public static final int VERSION = ServiceVersionHolder.VERSION;

    /**
     * Returns the version of this interface.
     */
    public int getVersion();

    public Map<String, String> getServerInformation(String sessionToken);

    /**
     * @return 'true' if archiver is configured for at least one data store, 'false' otherwise.
     */
    @Transactional(readOnly = true)
    public boolean isArchivingConfigured(final String sessionToken);

    /**
     * @return 'true' if project samples are enabled.
     */
    @Transactional(readOnly = true)
    public boolean isProjectSamplesEnabled(final String sessionToken);

    /**
     * @return 'true' if project level authorization is enabled.
     */
    @Transactional(readOnly = true)
    public boolean isProjectLevelAuthorizationEnabled(final String sessionToken);
    
    /**
     * @return 'true' if user represented by the session token is configured to use the project level authorization.
     */
    @Transactional(readOnly = true)
    public boolean isProjectLevelAuthorizationUser(final String sessionToken);

    /**
     * Tries to authenticate the specified user with given password.
     * 
     * @return <code>null</code> if authentication failed.
     */
    @Transactional
    public SessionContextDTO tryAuthenticate(final String user, final String password);

    /**
     * Tries to authenticate the specified user with given password to act as another user. Only instance admin users are allowed to do it.
     * 
     * @return <code>null</code> if authentication failed.
     */
    @Transactional
    public SessionContextDTO tryAuthenticateAs(final String user, final String password, final String asUser);

    @Transactional
    public SessionContextDTO tryAuthenticateAnonymously();
    
    @Transactional
    public SessionContextDTO tryToAuthenticate(String sessionToken);

    /** @return session for the specified token or null if session has expired */
    public SessionContextDTO tryGetSession(String sessionToken);

    /**
     * Checks that the session is valid.
     * 
     * @throws InvalidSessionException If the session is not valid.
     */
    public void checkSession(final String sessionToken) throws InvalidSessionException;

    /**
     * Sets the base URL (including "index.html") that the web server is reachable at for this client.
     */
    @Transactional
    public void setBaseIndexURL(String sessionToken, String baseIndexURL);

    @Transactional
    public String getBaseIndexURL(String sessionToken);

    @Transactional
    public DisplaySettings getDefaultDisplaySettings(String sessionToken);

    @Transactional
    public void saveDisplaySettings(String sessionToken, DisplaySettings displaySettings,
            int maxEntityVisits);

    @Transactional
    public void updateDisplaySettings(String sessionToken,
            IDisplaySettingsUpdate displaySettingsUpdate);

    /**
     * Lists grid custom columns for a given grid id.
     */
    @Transactional
    public List<GridCustomColumn> listGridCustomColumns(String sessionToken, String gridDisplayId);

    /** Changes logged user home space to the one with given technical id. */
    @Transactional
    public void changeUserHomeSpace(String sessionToken, TechId spaceIdOrNull);

    /**
     * Logout the session with the specified session token.
     */
    public void logout(final String sessionToken) throws UserFailureException;

    /**
     * Expires the session with the specified session token.
     */
    public void expireSession(final String sessionToken) throws UserFailureException;

    /**
     * Deactivates specified persons.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.PERSON, ObjectKind.AUTHORIZATION_GROUP, ObjectKind.ROLE_ASSIGNMENT })
    public void deactivatePersons(String sessionToken, List<String> personsCodes);

    /**
     * @return number of active users
     */
    @Transactional(readOnly = true)
    public int countActivePersons(String sessionToken);

    /**
     * Sets the user that owns this session. All methods called after this method are called with the privileges of the user specified by
     * <var>userCode</code>.
     * <p>
     * This method may only be called by an administrator and only from an explicitly allowed IP address or else it will throw an
     * {@link AuthorizationFailureException}.
     */
    @Transactional(readOnly = true)
    public void setSessionUser(String sessionToken, String userID);

}
