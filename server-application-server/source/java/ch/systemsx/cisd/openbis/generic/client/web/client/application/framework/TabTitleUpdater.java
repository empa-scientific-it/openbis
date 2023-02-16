/*
 * Copyright ETH 2009 - 2023 Zürich, Scientific IT Services
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
 * Allows to update the tab title. The update feature is not yet used anywhere!
 * 
 * @author Izabela Adamczyk
 */
public class TabTitleUpdater
{
    private TabItem tab;

    private String title;

    public TabTitleUpdater(String initialTitle)
    {
        this.title = initialTitle;
    }

    /**
     * Returns current title of the tab.
     */
    String getCurrentTitle()
    {
        return title;
    }

    /**
     * Updates the tab title if binded, stores the title value otherwise.
     */
    private void update(String newTitle)
    {
        this.title = newTitle;
        if (tab != null)
        {
            tab.setText(newTitle);
        }
    }

    /**
     * Binds the tab and updates the title with already stored value.
     */
    void bind(TabItem tabItem)
    {
        this.tab = tabItem;
        update(title);
    }
}
