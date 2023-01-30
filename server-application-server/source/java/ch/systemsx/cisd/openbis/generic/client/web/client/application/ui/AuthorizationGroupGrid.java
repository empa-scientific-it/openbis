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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageDomain;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.PersonRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.DescriptionField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IBrowserGridActionInvoker;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractDataListPermanentDeletionConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.AuthorizationGroupGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroupUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * Grid displaying authorization groups.
 * 
 * @author Izabela Adamczyk
 */
public class AuthorizationGroupGrid extends TypedTableGrid<AuthorizationGroup>
{
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX
            + "authorization-group-browser";

    public static final String GRID_ID = BROWSER_ID + TypedTableGrid.GRID_POSTFIX;

    public static final String ADD_BUTTON_ID = BROWSER_ID + "_add-button";

    public static final String DELETE_BUTTON_ID = BROWSER_ID + "_delete-button";

    public static final String USERS_BUTTON_ID = BROWSER_ID + "_users-button";

    private final IDelegatedAction postRegistrationCallback;

    public static IDisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final AuthorizationGroupGrid grid = new AuthorizationGroupGrid(viewContext);
        grid.extendBottomToolbar();
        return grid.asDisposableWithoutToolbar();
    }

    private AuthorizationGroupGrid(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, BROWSER_ID, true,
                DisplayTypeIDGenerator.AUTHORIZATION_GROUP_BROWSER_GRID);
        postRegistrationCallback = createRefreshGridAction();
    }

    private void extendBottomToolbar()
    {
        addEntityOperationsLabel();

        Button showDetailsButton =
                createSelectedItemButton(
                        viewContext.getMessage(Dict.BUTTON_SHOW_USERS),
                        new ISelectedEntityInvoker<BaseEntityModel<TableModelRowWithObject<AuthorizationGroup>>>()
                            {
                                @Override
                                public void invoke(
                                        BaseEntityModel<TableModelRowWithObject<AuthorizationGroup>> selectedItem,
                                        boolean keyPressed)
                                {
                                    showEntityViewer(selectedItem.getBaseObject(), false,
                                            keyPressed);
                                }
                            });
        showDetailsButton.setId(USERS_BUTTON_ID);
        addButton(showDetailsButton);

        final Button addAuthorizationGroupButton =
                new Button(viewContext.getMessage(Dict.BUTTON_ADD, "Group"),
                        new SelectionListener<ButtonEvent>()
                            {
                                @Override
                                public void componentSelected(ButtonEvent ce)
                                {
                                    AddAuthorizationGroupDialog dialog =
                                            new AddAuthorizationGroupDialog(viewContext,
                                                    createRefreshGridAction());
                                    dialog.show();
                                }
                            });
        addAuthorizationGroupButton.setId(ADD_BUTTON_ID);
        addButton(addAuthorizationGroupButton);

        addButton(createSelectedItemButton(
                viewContext.getMessage(Dict.BUTTON_EDIT),
                new ISelectedEntityInvoker<BaseEntityModel<TableModelRowWithObject<AuthorizationGroup>>>()
                    {

                        @Override
                        public void invoke(
                                BaseEntityModel<TableModelRowWithObject<AuthorizationGroup>> selectedItem,
                                boolean keyPressed)
                        {
                            final AuthorizationGroup authGroup =
                                    selectedItem.getBaseObject().getObjectOrNull();
                            createEditDialog(authGroup).show();
                        }
                    }));

        Button deleteButton =
                createSelectedItemsButton(viewContext.getMessage(Dict.BUTTON_DELETE),
                        new AbstractCreateDialogListener()
                            {
                                @Override
                                protected Dialog createDialog(
                                        List<TableModelRowWithObject<AuthorizationGroup>> selected,
                                        IBrowserGridActionInvoker invoker)
                                {
                                    ArrayList<AuthorizationGroup> groups =
                                            new ArrayList<AuthorizationGroup>();
                                    for (TableModelRowWithObject<AuthorizationGroup> row : selected)
                                    {
                                        groups.add(row.getObjectOrNull());
                                    }
                                    return new GroupListDeletionConfirmationDialog(viewContext,
                                            groups, createRefreshCallback(invoker));
                                }
                            });
        deleteButton.setId(DELETE_BUTTON_ID);
        addButton(deleteButton);

        allowMultipleSelection();

        addEntityOperationsSeparator();
    }

    @Override
    protected ColumnDefsAndConfigs<TableModelRowWithObject<AuthorizationGroup>> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<TableModelRowWithObject<AuthorizationGroup>> schema =
                super.createColumnsDefinition();
        schema.setGridCellRendererFor(AuthorizationGroupGridColumnIDs.DESCRIPTION,
                createMultilineStringCellRenderer());
        schema.setGridCellRendererFor(AuthorizationGroupGridColumnIDs.CODE,
                createInternalLinkCellRenderer());
        schema.setGridCellRendererFor(AuthorizationGroupGridColumnIDs.REGISTRATOR,
                PersonRenderer.REGISTRATOR_RENDERER);
        return schema;
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<AuthorizationGroup>> resultSetConfig,
            AbstractAsyncCallback<TypedTableResultSet<AuthorizationGroup>> callback)
    {
        viewContext.getService().listAuthorizationGroups(resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<TableModelRowWithObject<AuthorizationGroup>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportAuthorizationGroups(exportCriteria, callback);
    }

    @Override
    protected String translateColumnIdToDictionaryKey(String columnID)
    {
        return columnID.toLowerCase();
    }

    @Override
    protected List<String> getColumnIdsOfFilters()
    {
        return Arrays.asList(AuthorizationGroupGridColumnIDs.CODE);
    }

    @Override
    protected void showEntityViewer(final TableModelRowWithObject<AuthorizationGroup> group,
            boolean editMode, boolean inBackground)
    {
        final AbstractTabItemFactory tabFactory = new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent component =
                            PersonGrid.createForAuthorizationGroup(viewContext,
                                    group.getObjectOrNull());
                    return DefaultTabItem.create(getTabTitle(), component, viewContext);
                }

                @Override
                public String getId()
                {
                    return PersonGrid.createBrowserId(group.getObjectOrNull());
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.AUTHORIZATION_GROUPS,
                            HelpPageAction.VIEW);
                }

                @Override
                public String getTabTitle()
                {
                    return viewContext.getMessage(Dict.AUTHORIZATION_GROUP_USERS, group
                            .getObjectOrNull().getCode());
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }
            };
        tabFactory.setInBackground(inBackground);
        DispatcherHelper.dispatchNaviEvent(tabFactory);
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
        { DatabaseModificationKind.createOrDelete(ObjectKind.AUTHORIZATION_GROUP),
                DatabaseModificationKind.edit(ObjectKind.AUTHORIZATION_GROUP) };
    }

    private static final class GroupListDeletionConfirmationDialog extends
            AbstractDataListPermanentDeletionConfirmationDialog<AuthorizationGroup>
    {

        public GroupListDeletionConfirmationDialog(
                IViewContext<ICommonClientServiceAsync> viewContext, List<AuthorizationGroup> data,
                AbstractAsyncCallback<Void> callback)
        {
            super(viewContext, data, callback);
        }

        @SuppressWarnings("unchecked")
        private IViewContext<ICommonClientServiceAsync> getViewContext()
        {
            return (IViewContext<ICommonClientServiceAsync>) viewContext;
        }

        @Override
        protected void executeDeletion(AsyncCallback<Void> deletionCallback)
        {
            getViewContext().getCommonService().deleteAuthorizationGroups(TechId.createList(data),
                    reason.getValue(), deletionCallback);
        }

        @Override
        protected String getEntityName()
        {
            return messageProvider.getMessage(Dict.AUTHORIZATION_GROUP);
        }

    }

    private Window createEditDialog(final AuthorizationGroup authGroup)
    {
        final String title =
                viewContext.getMessage(Dict.EDIT_TITLE, "Authorization Group", authGroup.getCode());

        return new AbstractRegistrationDialog(viewContext, title, postRegistrationCallback)
            {
                private final DescriptionField descriptionField;

                {
                    descriptionField = createDescriptionField(viewContext, false);
                    FieldUtil.setValueWithUnescaping(descriptionField, authGroup.getDescription());
                    addField(descriptionField);
                }

                @Override
                protected void register(AsyncCallback<Void> registrationCallback)
                {
                    AuthorizationGroupUpdates updates = new AuthorizationGroupUpdates();
                    updates.setDescription(descriptionField.getValue());
                    updates.setId(TechId.create(authGroup));
                    updates.setVersion(authGroup.getModificationDate());
                    viewContext.getService()
                            .updateAuthorizationGroup(updates, registrationCallback);
                }
            };
    }
}
