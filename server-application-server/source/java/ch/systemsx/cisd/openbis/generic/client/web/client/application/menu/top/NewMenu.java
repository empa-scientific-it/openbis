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
 * The New menu of the top menu bar.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class NewMenu extends TopMenuItem
{
    public NewMenu(IMessageProvider messageProvider, ComponentProvider componentProvider)
    {
        super(messageProvider.getMessage(Dict.MENU_NEW));
        setId("new_menu");

        Menu submenu = new Menu();
        submenu.add(new ActionMenu(TopMenu.ActionMenuKind.PROJECT_MENU_NEW, messageProvider,
                componentProvider.getProjectRegistration()));
        submenu.add(new ActionMenu(TopMenu.ActionMenuKind.EXPERIMENT_MENU_NEW, messageProvider,
                componentProvider.getExperimentRegistration()));
        submenu.add(new ActionMenu(TopMenu.ActionMenuKind.SAMPLE_MENU_NEW, messageProvider,
                componentProvider.getSampleRegistration()));
        setMenu(submenu);
    }
}
