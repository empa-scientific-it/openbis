/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.menu;

import ch.systemsx.cisd.openbis.uitest.webdriver.Locate;
import ch.systemsx.cisd.openbis.uitest.widget.Button;
import ch.systemsx.cisd.openbis.uitest.widget.Text;

/**
 * @author anttil
 */
public class TopBar
{
    @Locate("admin_menu")
    private Button adminMenu;

    @Locate("user_menu")
    private Button userMenu;

    @Locate("browse_menu")
    private Button browseMenu;

    @Locate("new_menu")
    private Button newMenu;

    @Locate("import_menu")
    private Button importMenu;

    @Locate("openbis_top-menu_MODULES")
    private Button utilitiesMenu;

    @Locate("trash-button")
    private Button trash;

    @SuppressWarnings("unused")
    @Locate("openbis_search-widget_text-field-input")
    private Text searchTextBox;

    public void admin()
    {
        adminMenu.click();
    }

    public void user()
    {
        userMenu.click();
    }

    public void browse()
    {
        browseMenu.click();
    }

    public void trash()
    {
        trash.click();
    }

    public void newMenu()
    {
        newMenu.click();
    }

    public void importMenu()
    {
        importMenu.click();
    }

    public void utilitiesMenu()
    {
        utilitiesMenu.click();
    }

    public String getUserName()
    {
        return userMenu.getText();
    }
}
