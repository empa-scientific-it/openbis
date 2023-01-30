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

package ch.systemsx.cisd.openbis.plugin.demo.client.web.client.application.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.DisposableTabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IModule;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;
import ch.systemsx.cisd.openbis.plugin.demo.client.web.client.IDemoClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.demo.client.web.client.application.Dict;

/**
 * Simple demo module.
 * 
 * @author Izabela Adamczyk
 */
public class DemoModule implements IModule
{

    private final IViewContext<IDemoClientServiceAsync> viewContext;

    public DemoModule(final IViewContext<IDemoClientServiceAsync> viewContext)
    {
        this.viewContext = viewContext;
    }

    private IViewContext<IDemoClientServiceAsync> getViewContext()
    {
        return viewContext;
    }

    @Override
    public List<? extends MenuItem> getMenuItems()
    {
        return Collections.singletonList(new TopMenuItemDemoModuleMenu(getViewContext()));
        // Uncomment to see customized top menu .
        // return new CustomizedWidgetDemoModuleMenu(getViewContext());
    }

    @Override
    public String getName()
    {
        return viewContext.getMessage(Dict.MODULE_MENU_TITLE);
    }

    @Override
    public void initialize(AsyncCallback<Void> callback)
    {
        callback.onSuccess(null);
    }

    @Override
    public Collection<? extends DisposableTabContent> getSections(
            IEntityInformationHolderWithIdentifier entity)
    {
        return new ArrayList<DisposableTabContent>();
    }
}
