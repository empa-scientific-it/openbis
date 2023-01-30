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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.project;

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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ProjectViewer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ComponentProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageDomain;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.PersonRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractViewer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.LinkExtractor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IBrowserGridActionInvoker;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.CommonGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ProjectGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * Grid displaying projects.
 * 
 * @author Tomasz Pylak
 */
public class ProjectGrid extends TypedTableGrid<Project>
{
    // browser consists of the grid and the paging toolbar
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + "project-browser";

    public static final String GRID_ID = BROWSER_ID + TypedTableGrid.GRID_POSTFIX;

    public static final String SHOW_DETAILS_BUTTON_ID = BROWSER_ID + "-show-details";

    public static final String EDIT_BUTTON_ID = BROWSER_ID + "-edit";

    public static final String DELETE_BUTTON_ID = BROWSER_ID + "-delete";

    public static IDisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final ProjectGrid grid = new ProjectGrid(viewContext);
        grid.extendBottomToolbar();
        return grid.asDisposableWithoutToolbar();
    }

    private ProjectGrid(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, BROWSER_ID, true, DisplayTypeIDGenerator.PROJECT_BROWSER_GRID);
    }

    private void extendBottomToolbar()
    {
        addEntityOperationsLabel();

        final Button addButton =
                new Button(viewContext.getMessage(Dict.BUTTON_ADD, "Project"),
                        new SelectionListener<ButtonEvent>()
                            {
                                @Override
                                public void componentSelected(ButtonEvent ce)
                                {
                                    DispatcherHelper.dispatchNaviEvent(new ComponentProvider(
                                            viewContext).getProjectRegistration());
                                }
                            });
        addButton(addButton);

        Button showDetailsButton =
                createSelectedItemButton(
                        viewContext.getMessage(Dict.BUTTON_SHOW_DETAILS),
                        new ISelectedEntityInvoker<BaseEntityModel<TableModelRowWithObject<Project>>>()
                            {
                                @Override
                                public void invoke(
                                        BaseEntityModel<TableModelRowWithObject<Project>> selectedItem,
                                        boolean keyPressed)
                                {
                                    showEntityViewer(selectedItem.getBaseObject(), false,
                                            keyPressed);
                                }
                            });
        showDetailsButton.setId(SHOW_DETAILS_BUTTON_ID);
        addButton(showDetailsButton);

        Button editButton =
                createSelectedItemButton(
                        viewContext.getMessage(Dict.BUTTON_EDIT),
                        new ISelectedEntityInvoker<BaseEntityModel<TableModelRowWithObject<Project>>>()
                            {
                                @Override
                                public void invoke(
                                        BaseEntityModel<TableModelRowWithObject<Project>> selectedItem,
                                        boolean keyPressed)
                                {
                                    showEntityViewer(selectedItem.getBaseObject(), true, keyPressed);
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
                                        List<TableModelRowWithObject<Project>> rows,
                                        IBrowserGridActionInvoker invoker)
                                {
                                    List<Project> projects = new ArrayList<Project>();
                                    for (TableModelRowWithObject<Project> row : rows)
                                    {
                                        projects.add(row.getObjectOrNull());
                                    }
                                    return new ProjectListDeletionConfirmationDialog(viewContext,
                                            projects, createRefreshCallback(invoker));
                                }
                            });
        deleteButton.setId(DELETE_BUTTON_ID);
        addButton(deleteButton);
        allowMultipleSelection(); // we allow deletion of multiple projects

        addEntityOperationsSeparator();
    }

    @Override
    protected String translateColumnIdToDictionaryKey(String columnID)
    {
        return columnID.toLowerCase();
    }

    @Override
    protected ColumnDefsAndConfigs<TableModelRowWithObject<Project>> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<TableModelRowWithObject<Project>> schema =
                super.createColumnsDefinition();
        schema.setGridCellRendererFor(ProjectGridColumnIDs.CODE, createInternalLinkCellRenderer());
        schema.setGridCellRendererFor(ProjectGridColumnIDs.DESCRIPTION,
                createMultilineStringCellRenderer());
        schema.setGridCellRendererFor(ProjectGridColumnIDs.REGISTRATOR,
                PersonRenderer.REGISTRATOR_RENDERER);
        schema.setGridCellRendererFor(CommonGridColumnIDs.MODIFIER,
                PersonRenderer.MODIFIER_RENDERER);
        return schema;
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<Project>> resultSetConfig,
            AbstractAsyncCallback<TypedTableResultSet<Project>> callback)
    {
        viewContext.getService().listProjects(resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<TableModelRowWithObject<Project>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportProjects(exportCriteria, callback);
    }

    @Override
    protected List<String> getColumnIdsOfFilters()
    {
        return Arrays.asList(ProjectGridColumnIDs.CODE, ProjectGridColumnIDs.SPACE);
    }

    @Override
    protected void showEntityViewer(final TableModelRowWithObject<Project> row, boolean editMode,
            boolean inBackground)
    {
        showEntityViewer(row.getObjectOrNull(), editMode, viewContext, inBackground);
    }

    public static void showEntityViewer(final Project project, boolean editMode,
            final IViewContext<ICommonClientServiceAsync> viewContext, boolean inBackground)
    {
        AbstractTabItemFactory tabFactory;
        final TechId projectId = TechId.create(project);
        if (editMode == false)
        {
            tabFactory = new AbstractTabItemFactory()
                {
                    @Override
                    public ITabItem create()
                    {
                        final DatabaseModificationAwareComponent viewer =
                                ProjectViewer.create(viewContext, projectId);
                        return DefaultTabItem.create(getTabTitle(), viewer, viewContext, false);
                    }

                    @Override
                    public String getId()
                    {
                        return ProjectViewer.createId(projectId);
                    }

                    @Override
                    public String getTabTitle()
                    {
                        return AbstractViewer.getTitle(viewContext, Dict.PROJECT, project);
                    }

                    @Override
                    public HelpPageIdentifier getHelpPageIdentifier()
                    {
                        return new HelpPageIdentifier(HelpPageDomain.PROJECT, HelpPageAction.VIEW);
                    }

                    @Override
                    public String tryGetLink()
                    {
                        return LinkExtractor.tryExtract(project);
                    }
                };
        } else
        {
            tabFactory = new AbstractTabItemFactory()
                {
                    @Override
                    public ITabItem create()
                    {
                        DatabaseModificationAwareComponent component =
                                ProjectEditForm.create(viewContext, projectId);
                        return DefaultTabItem.create(getTabTitle(), component, viewContext, true);
                    }

                    @Override
                    public String getId()
                    {
                        return AbstractProjectEditRegisterForm.createId(projectId);
                    }

                    @Override
                    public String getTabTitle()
                    {
                        return AbstractRegistrationForm.getEditTitle(viewContext, Dict.PROJECT,
                                project);
                    }

                    @Override
                    public HelpPageIdentifier getHelpPageIdentifier()
                    {
                        return new HelpPageIdentifier(HelpPageDomain.PROJECT, HelpPageAction.EDIT);
                    }

                    @Override
                    public String tryGetLink()
                    {
                        return null;
                    }
                };
        }
        tabFactory.setInBackground(inBackground);
        DispatcherHelper.dispatchNaviEvent(tabFactory);
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
        { DatabaseModificationKind.createOrDelete(ObjectKind.PROJECT),
                DatabaseModificationKind.edit(ObjectKind.PROJECT) };
    }
}
