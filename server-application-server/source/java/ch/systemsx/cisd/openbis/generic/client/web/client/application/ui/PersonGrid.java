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
import com.extjs.gxt.ui.client.widget.button.Button;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.PersonRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.AddPersonDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.AddPersonToAuthorizationGroupDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IBrowserGridActionInvoker;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListPersonsCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PersonGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * Grid displaying persons.
 * 
 * @author Piotr Buczek
 */
public class PersonGrid extends TypedTableGrid<Person>
{
    // browser consists of the grid and the paging toolbar
    private static final String BROWSER_ID = GenericConstants.ID_PREFIX + "person-browser";

    private static final String GRID_SUFFIX = TypedTableGrid.GRID_POSTFIX;

    private static final String ADD_BUTTON_SUFFIX = "_add-button";

    private static final String REMOVE_BUTTON_SUFFIX = "_remove-button";

    private static final String DEACTIVATE_BUTTON_SUFFIX = "_deactivate-button";

    private final AuthorizationGroup authorizationGroupOrNull;

    public static IDisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final PersonGrid grid = new PersonGrid(viewContext, null);
        grid.extendBottomToolbar();
        return grid.asDisposableWithoutToolbar();
    }

    public static IDisposableComponent createForAuthorizationGroup(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            AuthorizationGroup authorizationGroup)
    {
        final PersonGrid grid = new PersonGrid(viewContext, authorizationGroup);
        grid.extendBottomToolbar();
        return grid.asDisposableWithoutToolbar();
    }

    private PersonGrid(IViewContext<ICommonClientServiceAsync> viewContext,
            AuthorizationGroup groupOrNull)
    {
        super(viewContext, createBrowserId(groupOrNull), true,
                DisplayTypeIDGenerator.PERSON_BROWSER_GRID);
        this.authorizationGroupOrNull = groupOrNull;
    }

    public static final String createBrowserId()
    {
        TechId nullTechId = null;
        return createBrowserId(nullTechId);
    }

    public static final String createBrowserId(AuthorizationGroup group)
    {
        return createBrowserId(TechId.create(group));
    }

    private static final String createBrowserId(TechId id)
    {
        return BROWSER_ID + (id != null ? ("_" + id) : "");
    }

    public static final String createRemoveButtonId(AuthorizationGroup group)
    {
        return createBrowserId(group) + REMOVE_BUTTON_SUFFIX;
    }

    public static final String createAddButtonId(AuthorizationGroup group)
    {
        return createBrowserId(group) + ADD_BUTTON_SUFFIX;
    }

    public static final String createDeactivateButtonId(AuthorizationGroup group)
    {
        return createBrowserId(group) + DEACTIVATE_BUTTON_SUFFIX;
    }

    public static final String createGridId(AuthorizationGroup group)
    {
        return createBrowserId(group) + GRID_SUFFIX;
    }

    private void extendBottomToolbar()
    {
        addEntityOperationsLabel();
        final Button addPersonButton =
                new Button(viewContext.getMessage(Dict.BUTTON_ADD, "Person"),
                        new SelectionListener<ButtonEvent>()
                            {
                                @Override
                                public void componentSelected(ButtonEvent ce)
                                {
                                    AbstractRegistrationDialog dialog =
                                            authorizationGroupOrNull == null ? createAddPersonDialog()
                                                    : createAddPersonToAuthoriationGroupDialog();
                                    dialog.show();
                                }

                            });
        addPersonButton.setId(createAddButtonId(authorizationGroupOrNull));
        addButton(addPersonButton);

        if (authorizationGroupOrNull != null)
        {
            Button deleteButton =
                    createSelectedItemsButton(viewContext.getMessage(Dict.BUTTON_DELETE),
                            new AbstractCreateDialogListener()
                                {
                                    @Override
                                    protected Dialog createDialog(
                                            List<TableModelRowWithObject<Person>> selected,
                                            IBrowserGridActionInvoker invoker)
                                    {
                                        ArrayList<Person> selectedPersons = new ArrayList<Person>();
                                        for (TableModelRowWithObject<Person> row : selected)
                                        {
                                            selectedPersons.add(row.getObjectOrNull());
                                        }
                                        return new PersonListDeletionConfirmationDialog(
                                                viewContext, selectedPersons,
                                                authorizationGroupOrNull,
                                                createRefreshCallback(invoker));
                                    }
                                });
            deleteButton.setId(createRemoveButtonId(authorizationGroupOrNull));
            addButton(deleteButton);
            allowMultipleSelection();
        } else
        {
            Button deactivateButton =
                    createSelectedItemsButton(viewContext.getMessage(Dict.BUTTON_DEACTIVATE),
                            new AbstractCreateDialogListener()
                                {
                                    @Override
                                    protected Dialog createDialog(
                                            List<TableModelRowWithObject<Person>> selected,
                                            IBrowserGridActionInvoker invoker)
                                    {
                                        ArrayList<Person> selectedPersons = new ArrayList<Person>();
                                        for (TableModelRowWithObject<Person> row : selected)
                                        {
                                            selectedPersons.add(row.getObjectOrNull());
                                        }
                                        return new PersonListDeactivationConfirmationDialog(
                                                viewContext, selectedPersons,
                                                createRefreshCallback(invoker));
                                    }
                                });
            deactivateButton.setId(createDeactivateButtonId(authorizationGroupOrNull));
            addButton(deactivateButton);
            allowMultipleSelection();
        }

        addEntityOperationsSeparator();
    }

    private AddPersonToAuthorizationGroupDialog createAddPersonToAuthoriationGroupDialog()
    {
        return new AddPersonToAuthorizationGroupDialog(viewContext, authorizationGroupOrNull,
                createRefreshGridAction());
    }

    private AddPersonDialog createAddPersonDialog()
    {
        return new AddPersonDialog(viewContext, createRefreshGridAction());
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<Person>> resultSetConfig,
            AbstractAsyncCallback<TypedTableResultSet<Person>> callback)
    {
        ListPersonsCriteria criteria = new ListPersonsCriteria(authorizationGroupOrNull);
        criteria.copyPagingConfig(resultSetConfig);
        viewContext.getService().listPersons(criteria, callback);
    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<TableModelRowWithObject<Person>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportPersons(exportCriteria, callback);
    }

    @Override
    protected List<String> getColumnIdsOfFilters()
    {
        return Arrays.asList(PersonGridColumnIDs.USER_ID);
    }

    @Override
    protected ColumnDefsAndConfigs<TableModelRowWithObject<Person>> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<TableModelRowWithObject<Person>> schema =
                super.createColumnsDefinition();
        schema.setGridCellRendererFor(PersonGridColumnIDs.REGISTRATOR,
                PersonRenderer.REGISTRATOR_RENDERER);
        return schema;
    }

    @Override
    protected String translateColumnIdToDictionaryKey(String columnID)
    {
        return columnID.toLowerCase();
    }

    @Override
    protected void showEntityViewer(final TableModelRowWithObject<Person> person, boolean editMode,
            boolean inBackground)
    {
        assert false : "not implemented";
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        List<DatabaseModificationKind> databaseModificationKinds =
                new ArrayList<DatabaseModificationKind>();
        databaseModificationKinds.add(DatabaseModificationKind.createOrDelete(ObjectKind.PERSON));
        databaseModificationKinds.add(DatabaseModificationKind.edit(ObjectKind.PERSON));
        if (authorizationGroupOrNull != null)
        {
            databaseModificationKinds.add(DatabaseModificationKind
                    .createOrDelete(ObjectKind.AUTHORIZATION_GROUP));
        }
        return databaseModificationKinds.toArray(new DatabaseModificationKind[0]);
    }
}
