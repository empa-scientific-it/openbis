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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.expressions.filter;

import java.util.Arrays;
import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractColumnSettingsDataModelProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IBrowserGridActionInvoker;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.expressions.common.AbstractGridCustomExpressionEditOrRegisterDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractDataConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.CustomGridColumnGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExpression;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomFilter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewColumnOrFilter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * Allows to display, update, delete and create new custom grid filters.
 * 
 * @author Tomasz Pylak
 */
public class GridCustomFilterGrid extends TypedTableGrid<GridCustomFilter>
{
    private static final String BROWSER_ID = GenericConstants.ID_PREFIX + "filter-browser";

    public static IDisposableComponent create(IViewContext<ICommonClientServiceAsync> viewContext,
            String gridDisplayId, AbstractColumnSettingsDataModelProvider columnDataModelProvider)
    {
        return new GridCustomFilterGrid(viewContext, gridDisplayId, columnDataModelProvider)
                .asDisposableWithoutToolbar();
    }

    static final String createAddButtonId(String gridDisplayId)
    {
        return createGridId(gridDisplayId) + "_ADD_BUTTON";
    }

    static final String createGridId(String gridDisplayId)
    {
        return createBrowserId(gridDisplayId) + "_grid";
    }

    private static final String createBrowserId(String gridDisplayId)
    {
        return BROWSER_ID + (gridDisplayId != null ? ("_" + gridDisplayId) : "");
    }

    protected final String gridDisplayId;

    private final AbstractColumnSettingsDataModelProvider columnDataModelProvider;

    private void extendBottomToolbar()
    {
        addEntityOperationsLabel();
        final Button addButton =
                new Button(viewContext.getMessage(Dict.BUTTON_ADD,
                        viewContext.getMessage(Dict.FILTER)), new SelectionListener<ButtonEvent>()
                    {
                        @Override
                        public void componentSelected(ButtonEvent ce)
                        {
                            createAddDialog().show();
                        }

                    });
        addButton.setId(createAddButtonId(gridDisplayId));
        addButton(addButton);
        final Button editButton =
                createSelectedItemButton(
                        viewContext.getMessage(Dict.BUTTON_EDIT),
                        new ISelectedEntityInvoker<BaseEntityModel<TableModelRowWithObject<GridCustomFilter>>>()
                            {
                                @Override
                                public void invoke(
                                        BaseEntityModel<TableModelRowWithObject<GridCustomFilter>> selectedItem,
                                        boolean keyPressed)
                                {
                                    final GridCustomFilter selected =
                                            selectedItem.getBaseObject().getObjectOrNull();
                                    createEditDialog(selected).show();
                                }

                            });
        addButton(editButton);
        Button deleteButton =
                createSelectedItemsButton(viewContext.getMessage(Dict.BUTTON_DELETE),
                        new AbstractCreateDialogListener()
                            {
                                @Override
                                protected Dialog createDialog(
                                        List<TableModelRowWithObject<GridCustomFilter>> selected,
                                        IBrowserGridActionInvoker invoker)
                                {
                                    return new DeletionConfirmationDialog(viewContext, selected,
                                            createRefreshCallback(invoker));
                                }
                            });
        addButton(deleteButton);
        allowMultipleSelection();
        addEntityOperationsSeparator();
    }

    private Window createAddDialog()
    {
        return new AddDialog(viewContext, createRefreshGridAction(), gridDisplayId,
                columnDataModelProvider);
    }

    private Window createEditDialog(AbstractExpression updatedItem)
    {
        return new EditDialog(viewContext, createRefreshGridAction(), gridDisplayId,
                columnDataModelProvider, updatedItem);
    }

    private static class AddDialog extends AbstractGridCustomExpressionEditOrRegisterDialog
    {
        private final IViewContext<ICommonClientServiceAsync> viewContext;

        public AddDialog(final IViewContext<ICommonClientServiceAsync> viewContext,
                final IDelegatedAction postRegistrationCallback, String gridId,
                AbstractColumnSettingsDataModelProvider columnDataModelProvider)
        {
            super(viewContext, viewContext.getMessage(Dict.ADD_NEW_FILTER),
                    postRegistrationCallback, gridId, columnDataModelProvider);
            this.viewContext = viewContext;
        }

        @Override
        protected void register(AsyncCallback<Void> registrationCallback)
        {
            NewColumnOrFilter newItem = getNewItemInfo();
            viewContext.getService().registerFilter(newItem, registrationCallback);
        }

        @Override
        protected HelpPageIdentifier createHelpPageIdentifier()
        {
            return new HelpPageIdentifier(HelpPageIdentifier.HelpPageDomain.CUSTOM_FILTER,
                    HelpPageIdentifier.HelpPageAction.REGISTER);
        }
    }

    private static class EditDialog extends AbstractGridCustomExpressionEditOrRegisterDialog
    {
        private final IViewContext<ICommonClientServiceAsync> viewContext;

        private final AbstractExpression itemToUpdate;

        public EditDialog(final IViewContext<ICommonClientServiceAsync> viewContext,
                final IDelegatedAction postRegistrationCallback, String gridId,
                AbstractColumnSettingsDataModelProvider columnDataModelProvider,
                AbstractExpression itemToUpdate)
        {
            super(viewContext, viewContext.getMessage(Dict.EDIT_TITLE,
                    viewContext.getMessage(Dict.FILTER), itemToUpdate.getName()),
                    postRegistrationCallback, gridId, columnDataModelProvider);
            this.viewContext = viewContext;
            this.itemToUpdate = itemToUpdate;
            initializeValues(itemToUpdate);
        }

        @Override
        protected void register(AsyncCallback<Void> registrationCallback)
        {
            update(itemToUpdate);
            viewContext.getService().updateFilter(itemToUpdate, registrationCallback);
        }

        @Override
        protected HelpPageIdentifier createHelpPageIdentifier()
        {
            return new HelpPageIdentifier(HelpPageIdentifier.HelpPageDomain.CUSTOM_FILTER,
                    HelpPageIdentifier.HelpPageAction.EDIT);
        }
    }

    private static class DeletionConfirmationDialog extends
            AbstractDataConfirmationDialog<List<TableModelRowWithObject<GridCustomFilter>>>
    {
        private static final int LABEL_WIDTH = 60;

        private static final int FIELD_WIDTH = 180;

        private final IViewContext<ICommonClientServiceAsync> viewContext;

        private final AbstractAsyncCallback<Void> callback;

        public DeletionConfirmationDialog(IViewContext<ICommonClientServiceAsync> viewContext,
                List<TableModelRowWithObject<GridCustomFilter>> data,
                AbstractAsyncCallback<Void> callback)
        {
            super(viewContext, data, viewContext.getMessage(Dict.DELETE_CONFIRMATION_TITLE));
            this.callback = callback;
            this.viewContext = viewContext;
        }

        @Override
        protected void extendForm()
        {
            formPanel.setLabelWidth(LABEL_WIDTH);
            formPanel.setFieldWidth(FIELD_WIDTH);
        }

        @Override
        protected String createMessage()
        {
            return "Do you really want to delete selected (" + data.size() + ") filter(s)?";
        }

        @Override
        protected void executeConfirmedAction()
        {
            viewContext.getCommonService().deleteFilters(TechId.createList(data), callback);
        }

    }

    private GridCustomFilterGrid(IViewContext<ICommonClientServiceAsync> viewContext,
            String gridDisplayId, AbstractColumnSettingsDataModelProvider columnDataModelProvider)
    {
        super(viewContext, createBrowserId(gridDisplayId), true,
                DisplayTypeIDGenerator.FILTER_BROWSER_GRID);
        this.gridDisplayId = gridDisplayId;
        this.columnDataModelProvider = columnDataModelProvider;
        extendBottomToolbar();
    }

    @Override
    protected String translateColumnIdToDictionaryKey(String columnID)
    {
        return columnID.toLowerCase();
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<GridCustomFilter>> resultSetConfig,
            AbstractAsyncCallback<TypedTableResultSet<GridCustomFilter>> callback)
    {
        viewContext.getService().listFilters(gridDisplayId, resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<TableModelRowWithObject<GridCustomFilter>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportFilters(exportCriteria, callback);
    }

    @Override
    protected List<String> getColumnIdsOfFilters()
    {
        return Arrays.asList(CustomGridColumnGridColumnIDs.NAME,
                CustomGridColumnGridColumnIDs.IS_PUBLIC);
    }

    @Override
    protected ColumnDefsAndConfigs<TableModelRowWithObject<GridCustomFilter>> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<TableModelRowWithObject<GridCustomFilter>> schema =
                super.createColumnsDefinition();
        schema.setGridCellRendererFor(CustomGridColumnGridColumnIDs.DESCRIPTION,
                createMultilineStringCellRenderer());
        schema.setGridCellRendererFor(CustomGridColumnGridColumnIDs.EXPRESSION,
                createMultilineStringCellRenderer());
        return schema;
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
        { DatabaseModificationKind.createOrDelete(ObjectKind.GRID_CUSTOM_FILTER),
                DatabaseModificationKind.edit(ObjectKind.GRID_CUSTOM_FILTER) };
    }
}
