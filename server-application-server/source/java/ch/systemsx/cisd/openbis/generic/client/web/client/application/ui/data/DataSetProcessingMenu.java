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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import java.util.List;

import com.extjs.gxt.ui.client.widget.menu.Menu;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.ActionMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.IActionMenuItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.AbstractExternalDataGrid.SelectedAndDisplayedItems;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedActionWithResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.TextToolItem;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;

/**
 * 'Actions' menu for Data Sets.
 * 
 * @author Piotr Buczek
 */
public class DataSetProcessingMenu extends TextToolItem
{

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final IDelegatedActionWithResult<SelectedAndDisplayedItems> selectedDataSetsGetter;

    public DataSetProcessingMenu(IViewContext<ICommonClientServiceAsync> viewContext,
            IDelegatedActionWithResult<SelectedAndDisplayedItems> selectedDataSetsGetter,
            List<DatastoreServiceDescription> processingServices)
    {
        super(viewContext.getMessage(Dict.MENU_PROCESSING));
        this.viewContext = viewContext;
        this.selectedDataSetsGetter = selectedDataSetsGetter;

        Menu submenu = new Menu();
        for (DatastoreServiceDescription service : processingServices)
        {
            addMenuItem(submenu, service);
        }
        setMenu(submenu);
    }

    private final void addMenuItem(Menu submenu, final DatastoreServiceDescription service)
    {
        final IDelegatedAction menuItemAction =
                DataSetComputeUtils.createComputeAction(viewContext, selectedDataSetsGetter,
                        service, null);
        final IActionMenuItem menuItemKind = new IActionMenuItem()
            {
                @Override
                public String getMenuText(IMessageProvider messageProvider)
                {
                    return service.getLabel();
                }

                @Override
                public String getMenuId()
                {
                    return service.getKey();
                }
            };
        submenu.add(new ActionMenu(menuItemKind, viewContext, menuItemAction));
    }

}
