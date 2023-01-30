/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.module;

import java.util.HashMap;

import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.ITabActionMenuItemDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TabActionMenuItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.IQueryClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.QueryModule;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.QueryParameterValue;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.locator.QueryLocatorResolver;

/**
 * Main menu of {@link QueryModule}.
 * 
 * @author Piotr Buczek
 */
public class QueryModuleDatabaseMenuItem extends MenuItem
{
    public static final String ID = GenericConstants.ID_PREFIX;

    public QueryModuleDatabaseMenuItem(IViewContext<IQueryClientServiceAsync> viewContext)
    {
        super(viewContext.getMessage(Dict.QUERY_DATABASE_MENU_TITLE));

        Menu submenu = new Menu();
        ActionMenuDefinition[] values = ActionMenuDefinition.values();
        for (ActionMenuDefinition definition : values)
        {
            submenu.add(TabActionMenuItemFactory.createActionMenu(viewContext, ID, definition));
        }
        setSubMenu(submenu);
    }

    public static enum ActionMenuDefinition implements
            ITabActionMenuItemDefinition<IQueryClientServiceAsync>
    {
        RUN_CANNED_QUERY("Run Predefined Query")
        {
            @Override
            public DatabaseModificationAwareComponent createComponent(
                    IViewContext<IQueryClientServiceAsync> viewContext)
            {
                return QueryViewer.create(viewContext, RunCannedQueryToolbar.createGeneric(
                        viewContext, null, new HashMap<String, QueryParameterValue>(0)));
            }

            @Override
            public String tryGetLink()
            {
                return QueryLocatorResolver.createQueryBrowserLink();
            }
        },
        RUN_CUSTOM_QUERY("Run Custom SQL Query")
        {
            @Override
            public DatabaseModificationAwareComponent createComponent(
                    IViewContext<IQueryClientServiceAsync> viewContext)
            {
                return QueryViewer.create(viewContext, new RunCustomQueryToolbar(viewContext));
            }
        },
        QUERY_BROWSER("Browsing and Edition of SQL Queries")
        {
            @Override
            public DatabaseModificationAwareComponent createComponent(
                    IViewContext<IQueryClientServiceAsync> viewContext)
            {
                return QueryBrowserGrid.create(viewContext);
            }
        };

        private final String helpPageTitle;

        private ActionMenuDefinition(String helpPageTitle)
        {
            this.helpPageTitle = helpPageTitle;
        }

        @Override
        public String getHelpPageTitle()
        {
            return helpPageTitle;
        }

        @Override
        public String getName()
        {
            return name();
        }

        @Override
        public String tryGetLink()
        {
            return null;
        }
    }

}
