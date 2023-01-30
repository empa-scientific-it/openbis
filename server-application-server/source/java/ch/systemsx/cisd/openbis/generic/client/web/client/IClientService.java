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

package ch.systemsx.cisd.openbis.generic.client.web.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ApplicationInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CustomImport;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.displaysettings.IDisplaySettingsUpdate;

/**
 * The basic <i>GWT</i> client service interface.
 * <p>
 * Is extended by each plug-in and the generic <code>openBIS</code>.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public interface IClientService extends RemoteService
{

    /**
     * Returns static information of the application needed by the client.
     */
    public ApplicationInfo getApplicationInfo();

    /**
     * Returns list of available custom imports
     */
    public List<CustomImport> getCustomImports();

    /**
     * Tries to return the current session context. If failed <code>null</code> is returned.
     * 
     * @param anonymous whether the session is expected to be anonymous or not
     * @param sessionIdOrNull id of the session or <code>null</code>.
     */
    public SessionContext tryToGetCurrentSessionContext(boolean anonymous, String sessionIdOrNull);

    /**
     * Tries to login with specified user ID and password. If failed <code>null</code> is returned.
     */
    public SessionContext tryToLogin(String userID, String password) throws UserFailureException;

    public SessionContext tryToLoginAnonymously() throws UserFailureException;

    /**
     * Sets the base URL that the client uses to connect the web server.
     */
    public void setBaseURL(String baseURL);

    /**
     * Saves display settings on the server.
     */
    public void saveDisplaySettings(DisplaySettings displaySettings);

    /**
     * Updates display settings on the server.
     */
    public void updateDisplaySettings(IDisplaySettingsUpdate displaySettingsUpdate);

    /**
     * Resets display settings of the logged user to the default settings.
     * <p>
     * NOTE: this changes user display settings only on the DB level. Old settings are still in the session and needs to be updated with the settings
     * returned by this function.
     * 
     * @return default display settings
     */
    public DisplaySettings resetDisplaySettings();

    /**
     * Changes home space of the logged-in user on the server.
     */
    public void changeUserHomeSpace(TechId groupIdOrNull);

    /**
     * Logs out the user, saving his/her {@link DisplaySettings} if necessary.
     */
    public void logout(DisplaySettings displaySettings, boolean simpleViewMode);

    /**
     * Deactivates persons with specified codes.
     */
    public void deactivatePersons(List<String> personsCodes) throws UserFailureException;

    /**
     * Returns the number of active users.
     */
    public int countActiveUsers();
}
