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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ComponentProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.ActionMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;

/**
 * Types menu for managing entity types.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class TypesMenu extends MenuItem
{

    public TypesMenu(final IViewContext<?> viewContext, IMessageProvider messageProvider, ComponentProvider componentProvider)
    {
        super(messageProvider.getMessage(Dict.MENU_TYPES));
        setId(TopMenu.ActionMenuKind.ADMINISTRATION_MENU_MANAGE_TYPES.toString());

        Menu submenu = new Menu();
        submenu.add(new ActionMenu(TopMenu.ActionMenuKind.EXPERIMENT_MENU_TYPES, messageProvider,
                componentProvider.getExperimentTypeBrowser()));
        submenu.add(new ActionMenu(TopMenu.ActionMenuKind.SAMPLE_MENU_TYPES, messageProvider,
                componentProvider.getSampleTypeBrowser()));
        submenu.add(new ActionMenu(TopMenu.ActionMenuKind.DATA_SET_MENU_TYPES, messageProvider,
                componentProvider.getDataSetTypeBrowser()));
        submenu.add(new ActionMenu(TopMenu.ActionMenuKind.MATERIAL_MENU_TYPES, messageProvider,
                componentProvider.getMaterialTypeBrowser()));
        submenu.add(new ActionMenu(TopMenu.ActionMenuKind.DATA_SET_MENU_FILE_FORMATS,
                messageProvider, componentProvider.getFileFormatTypeBrowser()));

        if (false == viewContext.getDisplaySettingsManager().isLegacyMedadataUIEnabled())
        {
            submenu.add(new ActionMenu(TopMenu.ActionMenuKind.PROPERTY_TYPES_MENU_BROWSE_PROPERTY_TYPES, messageProvider, componentProvider
                    .getPropertyTypeBrowser()));
        }

        setSubMenu(submenu);
    }
}
