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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.managed_property;

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AsyncCallbackWithProgressBar;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableModelReference;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IManagedPropertyGridInformationProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;

/**
 * @author Piotr Buczek
 */
public class ManagedPropertyGridGeneratedCallback extends
        AbstractAsyncCallback<TableModelReference>
{
    public interface IOnGridComponentGeneratedAction
    {
        void execute(final IDisposableComponent gridComponent);
    }

    @SuppressWarnings("hiding")
    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final IOnGridComponentGeneratedAction onGridGeneratedAction;

    private final IManagedPropertyGridInformationProvider gridInfo;

    private final IDelegatedAction onRefreshAction;

    private final IManagedProperty managedProperty;

    private final IEntityInformationHolder entity;

    public static AsyncCallback<TableModelReference> create(
            IViewContext<ICommonClientServiceAsync> viewContext, IEntityInformationHolder entity,
            IManagedProperty managedProperty, IManagedPropertyGridInformationProvider gridInfo,
            IOnGridComponentGeneratedAction onGridGeneratedAction, IDelegatedAction onRefreshAction)
    {
        return AsyncCallbackWithProgressBar.decorate(new ManagedPropertyGridGeneratedCallback(
                viewContext, entity, managedProperty, gridInfo, onGridGeneratedAction,
                onRefreshAction), "Generating the table...");
    }

    private ManagedPropertyGridGeneratedCallback(
            IViewContext<ICommonClientServiceAsync> viewContext, IEntityInformationHolder entity,
            IManagedProperty managedProperty, IManagedPropertyGridInformationProvider gridInfo,
            IOnGridComponentGeneratedAction onGridGeneratedAction, IDelegatedAction onRefreshAction)
    {
        super(viewContext);
        this.viewContext = viewContext;
        this.entity = entity;
        this.managedProperty = managedProperty;
        this.gridInfo = gridInfo;
        this.onGridGeneratedAction = onGridGeneratedAction;
        this.onRefreshAction = onRefreshAction;
    }

    @Override
    protected void process(final TableModelReference tableModelReference)
    {
        final IDisposableComponent reportComponent =
                ManagedPropertyGrid.create(viewContext, tableModelReference, entity,
                        managedProperty, gridInfo, onRefreshAction);
        onGridGeneratedAction.execute(reportComponent);
        if (StringUtils.isBlank(tableModelReference.tryGetMessage()) == false)
        {
            MessageBox.info(null, tableModelReference.tryGetMessage(), null);
        }
    }

}
