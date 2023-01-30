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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.framework;

import com.extjs.gxt.ui.client.widget.TabItem;

/**
 * An interface which contains everything to create a {@link TabItem}.
 * 
 * @author Christian Ribeaud
 */
public interface ITabItem extends IClosableItem
{
    /**
     * Returns the title updater.
     */
    public TabTitleUpdater getTabTitleUpdater();

    /**
     * Performs operations relevant to the tab when it is activated, namely when we opened this tab for the first time or we switched from another tab
     * to this one.
     * <p>
     * Usually refreshes the tab if it was detected that relevant database objects have changed.
     * </p>
     */
    public void onActivate(String linkOrNull);

    /**
     * Performs operations relevant to the tab when it is refreshed, namely when we opened this tab for the first time, we switched from another tab
     * to this one or this tab has been requested to be refreshed.
     */
    public void onRefresh(String linkOrNull);

    /**
     * Returns true if the confirmation dialog should be displayed before closing the tab.
     */
    public boolean isCloseConfirmationNeeded();
}
