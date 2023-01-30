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

import java.util.Arrays;
import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.AddRoleAssignmentDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.ConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.RoleAssignmentGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * Grid displaying persons.
 * 
 * @author Piotr Buczek
 */
public class RoleAssignmentGrid extends TypedTableGrid<RoleAssignment>
{
    // browser consists of the grid and the paging toolbar
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + "role-browser";

    public static final String GRID_ID = BROWSER_ID + TypedTableGrid.GRID_POSTFIX;

    public static final String ASSIGN_BUTTON_ID = BROWSER_ID + "_assign-button";

    public static IDisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final RoleAssignmentGrid grid = new RoleAssignmentGrid(viewContext);
        grid.extendBottomToolbar();
        return grid.asDisposableWithoutToolbar();
    }

    private RoleAssignmentGrid(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, BROWSER_ID, true, DisplayTypeIDGenerator.ROLE_ASSIGNMENT_BROWSER_GRID);
    }

    private void extendBottomToolbar()
    {
        addEntityOperationsLabel();

        final Button addGroupButton =
                new Button(viewContext.getMessage(Dict.BUTTON_ASSIGN_ROLE),
                        new SelectionListener<ButtonEvent>()
                            {
                                @Override
                                public void componentSelected(ButtonEvent ce)
                                {
                                    AddRoleAssignmentDialog dialog =
                                            new AddRoleAssignmentDialog(viewContext,
                                                    createRefreshGridAction());
                                    dialog.show();
                                }
                            });
        addGroupButton.setId(ASSIGN_BUTTON_ID);
        addButton(addGroupButton);

        // refactor
        final Button removeRoleButton =
                createSelectedItemButton(
                        viewContext.getMessage(Dict.BUTTON_RELEASE_ROLE_ASSIGNMENT),
                        new ISelectedEntityInvoker<BaseEntityModel<TableModelRowWithObject<RoleAssignment>>>()
                            {

                                @Override
                                public void invoke(
                                        BaseEntityModel<TableModelRowWithObject<RoleAssignment>> selectedItem,
                                        boolean keyPressed)
                                {
                                    RoleAssignment assignment =
                                            selectedItem.getBaseObject().getObjectOrNull();
                                    new RemoveRoleDialog(assignment).show();
                                }
                            });
        addButton(removeRoleButton);

        addEntityOperationsSeparator();
    }

    class RemoveRoleDialog extends ConfirmationDialog
    {

        private final RoleAssignment selectedRoleAssignment;

        public RemoveRoleDialog(final RoleAssignment selectedRoleAssignment)
        {
            super(viewContext.getMessage(Dict.CONFIRM_ROLE_REMOVAL_TITLE), viewContext
                    .getMessage(Dict.CONFIRM_ROLE_REMOVAL_MSG));
            this.selectedRoleAssignment = selectedRoleAssignment;
        }

        private void deleteRole()
        {
            final AbstractAsyncCallback<Void> roleListRefreshCallback =
                    new AbstractAsyncCallback<Void>(viewContext)
                        {
                            @Override
                            public void process(Void result)
                            {
                                asActionInvoker().refresh();
                            }
                        };

            Person person = selectedRoleAssignment.getPerson();
            Grantee grantee =
                    (person != null && StringUtils.isBlank(person.getUserId()) == false) ? Grantee
                            .createPerson(person.getUserId()) : Grantee
                                    .createAuthorizationGroup(selectedRoleAssignment
                                            .getAuthorizationGroup().getCode());
            if (selectedRoleAssignment.getRoleSetCode().isInstanceLevel())
            {
                viewContext.getService().deleteInstanceRole(
                        selectedRoleAssignment.getRoleSetCode(), grantee, roleListRefreshCallback);
            } else if (selectedRoleAssignment.getRoleSetCode().isSpaceLevel())
            {
                viewContext.getService().deleteSpaceRole(selectedRoleAssignment.getRoleSetCode(),
                        selectedRoleAssignment.getSpace().getCode(), grantee,
                        roleListRefreshCallback);
            } else if (selectedRoleAssignment.getRoleSetCode().isProjectLevel())
            {
                viewContext.getService().deleteProjectRole(selectedRoleAssignment.getRoleSetCode(),
                        selectedRoleAssignment.getProject().getIdentifier(), grantee,
                        roleListRefreshCallback);
            }
        }

        @Override
        protected void onYes()
        {
            deleteRole();
        }
    }

    @Override
    protected String translateColumnIdToDictionaryKey(String columnID)
    {
        return columnID.toLowerCase();
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<RoleAssignment>> resultSetConfig,
            AbstractAsyncCallback<TypedTableResultSet<RoleAssignment>> callback)
    {
        viewContext.getService().listRoleAssignments(resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<TableModelRowWithObject<RoleAssignment>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportRoleAssignments(exportCriteria, callback);
    }

    @Override
    protected List<String> getColumnIdsOfFilters()
    {
        return Arrays.asList(RoleAssignmentGridColumnIDs.AUTHORIZATION_GROUP,
                RoleAssignmentGridColumnIDs.DATABASE_INSTANCE, RoleAssignmentGridColumnIDs.PERSON,
                RoleAssignmentGridColumnIDs.ROLE, RoleAssignmentGridColumnIDs.SPACE, RoleAssignmentGridColumnIDs.PROJECT);
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[] { DatabaseModificationKind.createOrDelete(ObjectKind.ROLE_ASSIGNMENT),
                DatabaseModificationKind.edit(ObjectKind.ROLE_ASSIGNMENT) };
    }
}
