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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ComponentProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.ActionMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;

/**
 * Property Types top submenu.
 * 
 * @author Piotr Buczek
 */
public class PropertyTypesMenu extends MenuItem
{

    public PropertyTypesMenu(IMessageProvider messageProvider, ComponentProvider componentProvider)
    {
        super(messageProvider.getMessage(Dict.MENU_PROPERTY_TYPES));
        setId(TopMenu.ActionMenuKind.ADMINISTRATION_MENU_MANAGE_PROPERTY_TYPES.toString());
        Menu menu = new Menu();
        menu.add(new ActionMenu(TopMenu.ActionMenuKind.PROPERTY_TYPES_MENU_NEW_PROPERTY_TYPES, messageProvider, componentProvider
                .getPropertyTypeRegistration()));
        menu.add(new ActionMenu(TopMenu.ActionMenuKind.PROPERTY_TYPES_MENU_BROWSE_PROPERTY_TYPES, messageProvider, componentProvider
                .getPropertyTypeBrowser()));
        menu.add(new ActionMenu(TopMenu.ActionMenuKind.PROPERTY_TYPES_MENU_BROWSE_ASSIGNMENTS, messageProvider, componentProvider
                .getPropertyTypeAssignmentBrowser(null, false)));
        menu.add(new ActionMenu(TopMenu.ActionMenuKind.PROPERTY_TYPES_MENU_ASSIGN_TO_DATA_SET_TYPE, messageProvider, componentProvider
                .getPropertyTypeDataSetTypeAssignmentForm()));
        menu.add(new ActionMenu(TopMenu.ActionMenuKind.PROPERTY_TYPES_MENU_ASSIGN_TO_SAMPLE_TYPE, messageProvider, componentProvider
                .getPropertyTypeSampleTypeAssignmentForm()));
        menu.add(new ActionMenu(TopMenu.ActionMenuKind.PROPERTY_TYPES_MENU_ASSIGN_TO_EXPERIMENT_TYPE, messageProvider, componentProvider
                .getPropertyTypeExperimentTypeAssignmentForm()));
        menu.add(new ActionMenu(TopMenu.ActionMenuKind.PROPERTY_TYPES_MENU_ASSIGN_TO_MATERIAL_TYPE, messageProvider, componentProvider
                .getPropertyTypeMaterialTypeAssignmentForm()));
        setSubMenu(menu);
    }
}
