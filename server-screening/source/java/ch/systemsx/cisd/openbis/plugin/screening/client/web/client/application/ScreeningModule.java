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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.TabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.ActionMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.ITabActionMenuItemDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TabActionMenuItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IModule;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.WellSearchComponent;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.locator.GlobalWellSearchLocatorResolver;

/**
 * Screening extensions: reviewing panel section for the experiment (batch search for gene locations)
 * 
 * @author Tomasz Pylak
 */
public class ScreeningModule implements IModule
{
    public static final String ID = GenericConstants.ID_PREFIX + "-screening-";

    private final ScreeningViewContext viewContext;

    public ScreeningModule(ScreeningViewContext viewContext)
    {
        this.viewContext = viewContext;
    }

    @Override
    public List<? extends MenuItem> getMenuItems()
    {
        ActionMenu globalWellSearch =
                TabActionMenuItemFactory
                        .createActionMenu(viewContext, ID, createGlobalWellSearch());
        return Arrays.asList(globalWellSearch);
    }

    private ITabActionMenuItemDefinition<IScreeningClientServiceAsync> createGlobalWellSearch()
    {
        return new ITabActionMenuItemDefinition<IScreeningClientServiceAsync>()
            {
                @Override
                public String getName()
                {
                    return Dict.WELLS_SEARCH_MENU_ITEM;
                }

                @Override
                public String getHelpPageTitle()
                {
                    return "Global Well Search";
                }

                @Override
                public DatabaseModificationAwareComponent createComponent(
                        IViewContext<IScreeningClientServiceAsync> context)
                {
                    TabContent wellSearchTab =
                            new WellSearchComponent(viewContext, null, true, true);
                    return DatabaseModificationAwareComponent.wrapUnaware(wellSearchTab);
                }

                @Override
                public String tryGetLink()
                {
                    return GlobalWellSearchLocatorResolver.createQueryBrowserLink();
                }

            };
    }

    @Override
    public String getName()
    {
        return viewContext.getMessage(Dict.SCREENING_MODULE_TITLE);
    }

    @Override
    public void initialize(AsyncCallback<Void> callback)
    {
        callback.onSuccess(null);
    }

    @Override
    public Collection<? extends TabContent> getSections(
            IEntityInformationHolderWithIdentifier entity)
    {
        return Collections.emptyList();
    }

}
