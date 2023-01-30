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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import java.util.ArrayList;
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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.PersonRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.AddGroupDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.DescriptionField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IBrowserGridActionInvoker;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractDataListPermanentDeletionConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SpaceGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * Grid displaying spaces.
 * 
 * @author Piotr Buczek
 */
public class SpaceGrid extends TypedTableGrid<Space>
{
    // browser consists of the grid and the paging toolbar
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + "space-browser";

    public static final String GRID_ID = BROWSER_ID + TypedTableGrid.GRID_POSTFIX;

    public static final String ADD_BUTTON_ID = BROWSER_ID + "_add-button";

    public static final String DELETE_BUTTON_ID = BROWSER_ID + "_delete-button";

    public static final String EDIT_BUTTON_ID = BROWSER_ID + "-edit-button";

    private final IDelegatedAction postRegistrationCallback;

    public static IDisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final SpaceGrid grid = new SpaceGrid(viewContext);
        grid.extendBottomToolbar();
        return grid.asDisposableWithoutToolbar();
    }

    private SpaceGrid(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, BROWSER_ID, true, DisplayTypeIDGenerator.SPACES_BROWSER_GRID);
        postRegistrationCallback = createRefreshGridAction();
    }

    private void extendBottomToolbar()
    {
        addEntityOperationsLabel();

        final Button addGroupButton =
                new Button(viewContext.getMessage(Dict.BUTTON_ADD,
                        viewContext.getMessage(Dict.GROUP)), new SelectionListener<ButtonEvent>()
                    {
                        @Override
                        public void componentSelected(ButtonEvent ce)
                        {
                            AddGroupDialog dialog =
                                    new AddGroupDialog(viewContext, createRefreshGridAction());
                            dialog.show();
                        }
                    });
        addGroupButton.setId(ADD_BUTTON_ID);
        addButton(addGroupButton);

        Button editButton =
                createSelectedItemButton(
                        viewContext.getMessage(Dict.BUTTON_EDIT),
                        new ISelectedEntityInvoker<BaseEntityModel<TableModelRowWithObject<Space>>>()
                            {
                                @Override
                                public void invoke(
                                        BaseEntityModel<TableModelRowWithObject<Space>> selectedItem,
                                        boolean keyPressed)
                                {
                                    Space space = selectedItem.getBaseObject().getObjectOrNull();
                                    createEditDialog(space).show();
                                }
                            });
        editButton.setId(EDIT_BUTTON_ID);
        addButton(editButton);

        Button deleteButton =
                createSelectedItemsButton(viewContext.getMessage(Dict.BUTTON_DELETE),
                        new AbstractCreateDialogListener()
                            {
                                @Override
                                protected Dialog createDialog(
                                        List<TableModelRowWithObject<Space>> groups,
                                        IBrowserGridActionInvoker invoker)
                                {
                                    return new GroupListDeletionConfirmationDialog(viewContext,
                                            groups, createRefreshCallback(invoker));
                                }
                            });
        deleteButton.setId(DELETE_BUTTON_ID);
        addButton(deleteButton);
        allowMultipleSelection(); // we allow deletion of multiple projects

        addEntityOperationsSeparator();
    }

    private Window createEditDialog(final Space space)
    {
        final String code = space.getCode();
        final String description = space.getDescription();
        final String title =
                viewContext.getMessage(Dict.EDIT_TITLE, viewContext.getMessage(Dict.GROUP), code);

        return new AbstractRegistrationDialog(viewContext, title, postRegistrationCallback)
            {
                private final DescriptionField descriptionField;

                {
                    boolean mandatory = false;

                    descriptionField = createDescriptionField(viewContext, mandatory);
                    FieldUtil.setValueWithUnescaping(descriptionField, description);
                    addField(descriptionField);
                }

                @Override
                protected void register(AsyncCallback<Void> registrationCallback)
                {
                    space.setDescription(descriptionField.getValue());

                    viewContext.getService().updateSpace(space, registrationCallback);
                }
            };
    }

    @Override
    protected String translateColumnIdToDictionaryKey(String columnID)
    {
        return columnID.toLowerCase();
    }

    @Override
    protected ColumnDefsAndConfigs<TableModelRowWithObject<Space>> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<TableModelRowWithObject<Space>> definitions =
                super.createColumnsDefinition();
        definitions.setGridCellRendererFor(SpaceGridColumnIDs.REGISTRATOR,
                PersonRenderer.REGISTRATOR_RENDERER);
        return definitions;
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<Space>> resultSetConfig,
            AbstractAsyncCallback<TypedTableResultSet<Space>> callback)
    {
        viewContext.getService().listSpaces(resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<TableModelRowWithObject<Space>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportSpaces(exportCriteria, callback);
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
        { DatabaseModificationKind.createOrDelete(ObjectKind.SPACE),
                DatabaseModificationKind.edit(ObjectKind.SPACE) };
    }

    private static final class GroupListDeletionConfirmationDialog extends
            AbstractDataListPermanentDeletionConfirmationDialog<TableModelRowWithObject<Space>>
    {

        @SuppressWarnings("hiding")
        private final IViewContext<ICommonClientServiceAsync> viewContext;

        public GroupListDeletionConfirmationDialog(
                IViewContext<ICommonClientServiceAsync> viewContext,
                List<TableModelRowWithObject<Space>> data, AbstractAsyncCallback<Void> callback)
        {
            super(viewContext, data, callback);
            this.viewContext = viewContext;
            this.setId("deletion-confirmation-dialog");
        }

        @Override
        protected void executeDeletion(AsyncCallback<Void> deletionCallback)
        {
            List<TableModelRowWithObject<Space>> d = data;
            List<TechId> list = new ArrayList<TechId>();
            for (TableModelRowWithObject<Space> tableModelRowWithObject : d)
            {
                list.add(new TechId(tableModelRowWithObject.getObjectOrNull().getId()));
            }
            viewContext.getCommonService().deleteSpaces(list, reason.getValue(), deletionCallback);
        }

        @Override
        protected String getEntityName()
        {
            return messageProvider.getMessage(Dict.GROUP);
        }

    }
}
