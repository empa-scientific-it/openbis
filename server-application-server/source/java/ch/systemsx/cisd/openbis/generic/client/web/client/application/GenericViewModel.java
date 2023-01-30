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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ApplicationInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.ViewMode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;

/**
 * The view model.
 * 
 * @author Franz-Josef Elmer
 */
public class GenericViewModel
{
    private ApplicationInfo applicationInfo;

    private SessionContext sessionContext;

    private ViewMode viewMode;

    private boolean anonymousAllowed;

    public final ApplicationInfo getApplicationInfo()
    {
        return applicationInfo;
    }

    public final void setApplicationInfo(final ApplicationInfo applicationInfo)
    {
        this.applicationInfo = applicationInfo;
    }

    public final SessionContext getSessionContext()
    {
        return sessionContext;
    }

    public final void setSessionContext(final SessionContext sessionContext)
    {
        this.sessionContext = sessionContext;
    }

    public ViewMode getViewMode()
    {
        return viewMode;
    }

    public void setViewMode(ViewMode viewMode)
    {
        this.viewMode = viewMode;
    }

    public boolean isAnonymousLogin()
    {
        return sessionContext.isAnonymous();
    }

    public boolean isEmbeddedMode()
    {
        return viewMode == ViewMode.EMBEDDED;
    }

    public boolean isGridMode()
    {
        return viewMode == ViewMode.GRID;
    }

    public boolean isDisplaySettingsSaving()
    {
        return isAnonymousLogin() == false
                && (ViewMode.NORMAL.equals(getViewMode()) || ViewMode.GRID.equals(getViewMode()));
    }

    public void setAnonymousAllowed(boolean anonymousAllowed)
    {
        this.anonymousAllowed = anonymousAllowed;
    }

    public boolean isAnonymousAllowed()
    {
        return anonymousAllowed;
    }

    public Person getLoggedInPerson()
    {
        return sessionContext.getUser().getUserPersonObject();
    }
}
