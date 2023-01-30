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

import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IBrowserGridActionInvoker;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.TableExportType;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetFetchConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableModelReference;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IManagedPropertyGridInformationProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ReportRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedUiAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedUiTableAction;

/**
 * @author Piotr Buczek
 */
public class ManagedPropertyGrid extends TypedTableGrid<ReportRowModel>
{
    // browser consists of the grid and the paging toolbar
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + "ManagedPropertyGrid";

    public static IDisposableComponent create(IViewContext<ICommonClientServiceAsync> viewContext,
            TableModelReference tableModelReference, IEntityInformationHolder entity,
            IManagedProperty managedProperty,
            IManagedPropertyGridInformationProvider gridInformation,
            IDelegatedAction onRefreshAction)
    {
        final ManagedPropertyGrid grid =
                new ManagedPropertyGrid(viewContext, tableModelReference, entity, managedProperty,
                        gridInformation, onRefreshAction);
        return grid.asDisposableWithoutToolbar();
    }

    public static String createId(String idSuffix)
    {
        return BROWSER_ID + "_" + idSuffix;
    }

    private final String resultSetKey;

    private final String gridKind;

    private final IDelegatedAction onRefreshAction;

    private final IManagedProperty managedProperty;

    private final IEntityInformationHolder entity;

    private ManagedPropertyGrid(IViewContext<ICommonClientServiceAsync> viewContext,
            TableModelReference tableModelReference, IEntityInformationHolder entity,
            IManagedProperty managedProperty,
            IManagedPropertyGridInformationProvider gridInformation,
            IDelegatedAction onRefreshAction)
    {
        super(viewContext, BROWSER_ID, true, DisplayTypeIDGenerator.DATA_SET_REPORTING_GRID);
        this.entity = entity;
        this.managedProperty = managedProperty;
        this.onRefreshAction = onRefreshAction;
        setId(BROWSER_ID);
        this.resultSetKey = tableModelReference.getResultSetKey();
        this.gridKind = gridInformation.getKey();
        updateDefaultRefreshButton();
        extendBottomToolbar();
    }

    // adds action buttons
    private void extendBottomToolbar()
    {
        if (viewContext.isSimpleOrEmbeddedMode())
        {
            return;
        }
        if (managedProperty.getUiDescription().getActions().size() > 0)
        {
            addEntityOperationsLabel();
            addEntityOperationButtons();
            addEntityOperationsSeparator();
        }
    }

    private void addEntityOperationButtons()
    {
        for (IManagedUiAction managedAction : managedProperty.getUiDescription().getActions())
        {
            if (managedAction instanceof IManagedUiTableAction)
            {
                final IManagedUiTableAction tableAction = (IManagedUiTableAction) managedAction;

                final String actionTitle = tableAction.getName();
                final Button actionButton =
                        new Button(actionTitle, new AbstractCreateDialogListener()
                            {

                                @Override
                                protected Dialog createDialog(
                                        List<TableModelRowWithObject<ReportRowModel>> data,
                                        IBrowserGridActionInvoker invoker)
                                {
                                    AsyncCallback<Void> callback = createRefreshCallback(invoker);
                                    return new ManagedPropertyGridActionDialog(viewContext,
                                            actionTitle, data, callback, entity, managedProperty,
                                            tableAction);
                                }
                            });
                addButton(actionButton);

                switch (tableAction.getSelectionType())
                {
                    case REQUIRED:
                        enableButtonOnSelectedItems(actionButton);
                        allowMultipleSelection();
                        break;
                    case REQUIRED_SINGLE:
                        enableButtonOnSelectedItem(actionButton);
                        break;
                    case NOT_REQUIRED:
                        break; // nothing to do
                }
            }
        }
    }

    @Override
    public String getGridDisplayTypeID()
    {
        return createGridDisplayTypeID(gridKind);
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<ReportRowModel>> resultSetConfig,
            AbstractAsyncCallback<TypedTableResultSet<ReportRowModel>> callback)
    {
        // In all cases the data should be taken from the cache, and we know the key already.
        // The custom columns should be recomputed.
        resultSetConfig.setCacheConfig(ResultSetFetchConfig
                .createFetchFromCacheAndRecompute(resultSetKey));
        viewContext.getService().listReport(resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<TableModelRowWithObject<ReportRowModel>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportReport(exportCriteria, callback);
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
        {
                // script changes can cause all sorts of changes to the grid
                DatabaseModificationKind.edit(ObjectKind.SCRIPT),
                // different script can be assigned
                DatabaseModificationKind.edit(ObjectKind.PROPERTY_TYPE_ASSIGNMENT)

        };
    }

    @Override
    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        super.update(observedModifications);
    }

    @Override
    protected IBrowserGridActionInvoker asActionInvoker()
    {
        final IBrowserGridActionInvoker delegate = super.asActionInvoker();
        return new IBrowserGridActionInvoker()
            {

                @Override
                public boolean supportsExportForUpdate()
                {
                    return false;
                }

                @Override
                public void toggleFilters(boolean show)
                {
                    delegate.toggleFilters(show);
                }

                @Override
                public void refresh()
                {
                    onRefreshAction.execute();
                }

                @Override
                public void export(TableExportType type)
                {
                    delegate.export(type);
                }

                @Override
                public void configure()
                {
                    delegate.configure();
                }
            };
    }

}
