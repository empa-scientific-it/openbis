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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.menu;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.SearchWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ComponentProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.administration.AdministrationMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.modules.ModulesMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.top.BrowseMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.top.ImportMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.top.NewMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.user.LoggedUserMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;

/**
 * Implements functionality of the top menu.
 * 
 * @author Franz-Josef Elmer
 * @author Izabela Adamczyk
 */
public class TopMenu extends LayoutContainer
{
    public static final String ID = GenericConstants.ID_PREFIX + "top-menu";

    /** {@link ActionMenu} kind enum with names matching dictionary keys */
    public static enum ActionMenuKind implements IActionMenuItem
    {
        ADMINISTRATION_MENU_MANAGE_GROUPS, ADMINISTRATION_MENU_MANAGE_TYPES,
        ADMINISTRATION_MENU_MANAGE_PROPERTY_TYPES, ADMINISTRATION_MENU_MANAGE_AUTHORIZATION,

        AUTHORIZATION_MENU_USERS, AUTHORIZATION_MENU_ROLES,
        AUTHORIZATION_MENU_AUTHORIZATION_GROUPS,

        LOGGING_CONSOLE,

        DATA_SET_MENU_SEARCH, DATA_SET_MENU_TYPES, DATA_SET_MENU_FILE_FORMATS,
        DATA_SET_MENU_UPLOAD, DATA_SET_MENU_MASS_UPDATE,

        EXPERIMENT_MENU_BROWSE, EXPERIMENT_MENU_NEW, EXPERIMENT_MENU_IMPORT,
        EXPERIMENT_MENU_MASS_UPDATE, EXPERIMENT_MENU_TYPES,

        MATERIAL_MENU_BROWSE, MATERIAL_MENU_IMPORT, MATERIAL_MENU_MASS_UPDATE, MATERIAL_MENU_TYPES,

        SAMPLE_MENU_SEARCH, SAMPLE_MENU_BROWSE, SAMPLE_MENU_NEW, SAMPLE_MENU_IMPORT,
        SAMPLE_MENU_MASS_UPDATE, SAMPLE_MENU_TYPES,

        SCRIPT_MENU_BROWSE, SCRIPT_MENU_NEW,

        PROJECT_MENU_BROWSE, PROJECT_MENU_NEW,

        METAPROJECT_MENU_BROWSE,

        PROPERTY_TYPES_MENU_BROWSE_PROPERTY_TYPES, PROPERTY_TYPES_MENU_BROWSE_ASSIGNMENTS,
        PROPERTY_TYPES_MENU_NEW_PROPERTY_TYPES, PROPERTY_TYPES_MENU_ASSIGN_TO_EXPERIMENT_TYPE,
        PROPERTY_TYPES_MENU_ASSIGN_TO_MATERIAL_TYPE, PROPERTY_TYPES_MENU_ASSIGN_TO_DATA_SET_TYPE,
        PROPERTY_TYPES_MENU_ASSIGN_TO_SAMPLE_TYPE,

        USER_MENU_CHANGE_SETTINGS, USER_MENU_LOGOUT, USER_MENU_LOGIN, USER_MENU_ABOUT_BOX,

        VOCABULARY_MENU_BROWSE, VOCABULARY_MENU_NEW, GENERAL_IMPORT_MENU, CUSTOM_IMPORT_MENU,
        ACTIVE_USERS_COUNT;

        @Override
        public String getMenuId()
        {
            return ID + "_" + this.name();
        }

        @Override
        public String getMenuText(IMessageProvider messageProvider)
        {
            return messageProvider.getMessage(this.name());
        }
    }

    private final ToolBar toolBar;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final ComponentProvider componentProvider;

    public TopMenu(final IViewContext<ICommonClientServiceAsync> viewContext,
            ComponentProvider componentProvider)
    {
        this.viewContext = viewContext;
        this.componentProvider = componentProvider;
        setId(ID);
        setLayout(new FlowLayout());
        setBorders(true);
        toolBar = new ToolBar();
        add(toolBar);
    }

    final void refresh()
    {
        toolBar.removeAll();

        toolBar.add(new BrowseMenu(viewContext, componentProvider));
        toolBar.add(new NewMenu(viewContext, componentProvider));
        toolBar.add(new ImportMenu(viewContext, componentProvider));
        toolBar.add(new ModulesMenu(viewContext, viewContext.getClientPluginFactoryProvider(),
                componentProvider));
        toolBar.add(new FillToolItem());
        toolBar.add(new SearchWidget(viewContext));
        toolBar.add(new SeparatorToolItem());
        toolBar.add(new InfoButton(viewContext));
        if (viewContext.getModel().getApplicationInfo().getWebClientConfiguration()
                .getEnableTrash())
        {
            Button trash = new TrashButton(viewContext, componentProvider);
            trash.setId("trash-button");
            toolBar.add(trash);
        }
        toolBar.add(new AdministrationMenu(viewContext, componentProvider));
        toolBar.add(new LoggedUserMenu(viewContext, componentProvider));
    }

    @Override
    protected void onRender(final Element parent, final int pos)
    {
        super.onRender(parent, pos);
        refresh();
    }

}
