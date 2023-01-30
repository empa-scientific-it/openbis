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

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ApplicationInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CustomImport;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.displaysettings.IDisplaySettingsUpdate;

/**
 * @author Christian Ribeaud
 */
public interface IClientServiceAsync
{

    /** @see IClientService#getApplicationInfo() */
    public void getApplicationInfo(AsyncCallback<ApplicationInfo> callback);

    /** @see IClientService#getCustomImports */
    public void getCustomImports(AsyncCallback<List<CustomImport>> callback);

    /** @see IClientService#tryToGetCurrentSessionContext(boolean, String) */
    public void tryToGetCurrentSessionContext(boolean anonymous, String sessionIdOrNull, AsyncCallback<SessionContext> callback);

    /** @see IClientService#tryToLogin(String, String) */
    public void tryToLogin(String userID, String password, AsyncCallback<SessionContext> callback);

    /** @see IClientService#tryToLoginAnonymously() */
    public void tryToLoginAnonymously(AsyncCallback<SessionContext> callback);

    /** @see IClientService#setBaseURL(String) */
    public void setBaseURL(String baseURL, AsyncCallback<SessionContext> callback);

    /** @see IClientService#saveDisplaySettings(DisplaySettings) */
    public void saveDisplaySettings(DisplaySettings displaySettings, AsyncCallback<Void> callback);

    /** @see IClientService#updateDisplaySettings(IDisplaySettingsUpdate) */
    public void updateDisplaySettings(IDisplaySettingsUpdate displaySettingsUpdate,
            AsyncCallback<Void> callback);

    /** @see IClientService#resetDisplaySettings() */
    public void resetDisplaySettings(AsyncCallback<DisplaySettings> resetUserSettingsCallback);

    /** @see IClientService#changeUserHomeSpace(TechId) */
    public void changeUserHomeSpace(TechId spaceIdOrNull, AsyncCallback<Void> callback);

    /** @see IClientService#logout(DisplaySettings, boolean) */
    public void logout(DisplaySettings displaySettings, boolean simpleViewMode,
            AsyncCallback<Void> callback);

    /** @see IClientService#deactivatePersons(List) */
    public void deactivatePersons(List<String> personsCodes, AsyncCallback<Void> callback);

    /** @see IClientService#countActiveUsers() */
    public void countActiveUsers(AsyncCallback<Integer> callback);
}
