/*
@ * Copyright 2012 ETH Zuerich, CISD
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
import ch.systemsx.cisd.openbis.uitest.widget.Link;

public class UserMenu
{

    @Locate("openbis_top-menu_USER_MENU_LOGOUT")
    private Link logout;

    @Locate("openbis_top-menu_USER_MENU_CHANGE_SETTINGS")
    private Link settings;

    public void logout()
    {
        logout.click();
    }

    public void settings()
    {
        settings.click();
    }
}
