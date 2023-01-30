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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.top;

import com.extjs.gxt.ui.client.widget.menu.Menu;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ComponentProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.ActionMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenuItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * The Browse menu of the top menu bar.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class BrowseMenu extends TopMenuItem
{
    public BrowseMenu(IMessageProvider messageProvider, ComponentProvider componentProvider)
    {
        super(messageProvider.getMessage(Dict.MENU_BROWSE));
        setId("browse_menu");

        Menu submenu = new Menu();
        submenu.add(new ActionMenu(TopMenu.ActionMenuKind.METAPROJECT_MENU_BROWSE, messageProvider,
                componentProvider.getMetaprojectBrowser()));
        submenu.add(new ActionMenu(TopMenu.ActionMenuKind.PROJECT_MENU_BROWSE, messageProvider,
                componentProvider.getProjectBrowser()));
        submenu.add(new ActionMenu(TopMenu.ActionMenuKind.EXPERIMENT_MENU_BROWSE, messageProvider,
                componentProvider.getExperimentBrowser()));
        submenu.add(new ActionMenu(TopMenu.ActionMenuKind.SAMPLE_MENU_BROWSE, messageProvider,
                componentProvider.getSampleBrowser()));
        submenu.add(new ActionMenu(TopMenu.ActionMenuKind.SAMPLE_MENU_SEARCH, messageProvider,
                componentProvider.getSampleSearch()));
        submenu.add(new ActionMenu(TopMenu.ActionMenuKind.DATA_SET_MENU_SEARCH, messageProvider,
                componentProvider.getDataSetSearch()));
        submenu.add(new ActionMenu(TopMenu.ActionMenuKind.MATERIAL_MENU_BROWSE, messageProvider,
                componentProvider.getMaterialBrowser()));

        setMenu(submenu);
    }
}
