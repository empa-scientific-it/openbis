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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.annotation.DoNotEscape;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;

/**
 * Context of a web session. Contains session ID, {@link User}, and {@link DisplaySettings}.
 * 
 * @author Franz-Josef Elmer
 */
@DoNotEscape
public class SessionContext implements IsSerializable
{

    private User user;

    private String sessionID;

    private DisplaySettings displaySettings;

    private boolean anonymous;

    public final User getUser()
    {
        return user;
    }

    public final void setUser(User user)
    {
        this.user = user;
    }

    public final String getSessionID()
    {
        return sessionID;
    }

    public final void setSessionID(String sessionID)
    {
        this.sessionID = sessionID;
    }

    /**
     * Returns the display settings. Display settings shouldn't be manipulated directly. Instead the display settings manager should be used. It can
     * be accessed by {@link IViewContext#getDisplaySettingsManager()}.
     */
    public final DisplaySettings getDisplaySettings()
    {
        return displaySettings;
    }

    /**
     * Sets the display settings. This method should not be used on the client. In order to manipulate the display settings a display settings manager
     * as returned by {@link IViewContext#getDisplaySettingsManager()} should be used.
     */
    public final void setDisplaySettings(DisplaySettings displaySettings)
    {
        this.displaySettings = displaySettings;
    }

    public void setAnonymous(boolean anonymous)
    {
        this.anonymous = anonymous;
    }

    public boolean isAnonymous()
    {
        return this.anonymous;
    }

}
