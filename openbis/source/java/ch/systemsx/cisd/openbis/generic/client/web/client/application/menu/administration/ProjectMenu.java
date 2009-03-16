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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.administration;

import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ComponentProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.ActionMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * Project top submenu.
 * 
 * @author Piotr Buczek
 */
public class ProjectMenu extends MenuItem
{

    public ProjectMenu(IMessageProvider messageProvider, ComponentProvider componentProvider)
    {
        super("Project");
        setIconStyle("icon-menu-show");
        Menu menu = new Menu();
        menu.add(new ActionMenu(TopMenu.ActionMenuKind.PROJECT_MENU_BROWSE, "Browse",
                componentProvider.getProjectBrowser()));
        menu.add(new ActionMenu(TopMenu.ActionMenuKind.PROJECT_MENU_NEW, "New", componentProvider
                .getProjectRegistration()));
        setSubMenu(menu);
    }
}