/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.framework;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;

/**
 * Creates {@link ITabItem} which consist of a description and a content of the tab.<br>
 * Allows to create a tab in a moment when it is needed.
 * 
 * @author Tomasz Pylak
 */
public abstract class AbstractTabItemFactory
{
    private boolean inBackground = false;

    private boolean forceReopen = false;

    /** Creates and initializes a new tab. */
    public abstract ITabItem create();

    /**
     * Returns the unique identifier of this tab item. Note that the id should be unique among all tabs, not widgets.
     * <p>
     * The framework ensures that no two components with the same id will be displayed. Instead the one already created will get the focus.
     * </p>
     */
    public abstract String getId();

    /**
     * Returns the identifier of the help page for this tab item.
     */
    public abstract HelpPageIdentifier getHelpPageIdentifier();

    /**
     * Returns a short description of contents of the tab that can be used in tab or page title.
     */
    public abstract String getTabTitle();

    /**
     * Returns a 'permlink' for this tab item or null if we don't support such a link.
     */
    public abstract String tryGetLink();

    /**
     * True if the tab should become active.
     */
    public boolean isInBackground()
    {
        return inBackground;
    }

    // default: false
    public void setInBackground(boolean inBackground)
    {
        this.inBackground = inBackground;
    }

    /**
     * True if the tab should become active.
     */
    public boolean isForceReopen()
    {
        return forceReopen;
    }

    // default: false
    public void setForceReopen(boolean forceReopen)
    {
        this.forceReopen = forceReopen;
    }

}
