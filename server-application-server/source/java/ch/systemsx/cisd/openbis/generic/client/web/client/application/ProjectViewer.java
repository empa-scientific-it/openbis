/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import java.util.Set;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractViewer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.BorderLayoutDataFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.project.ProjectGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.project.ProjectListDeletionConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;

/**
 * Presents details of the project.
 * 
 * @author Izabela Adamczyk
 * @author Piotr Buczek
 */
public final class ProjectViewer extends AbstractViewer<IEntityInformationHolder> implements
        IDatabaseModificationObserver
{
    private static final String PREFIX = "project-viewer_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    @SuppressWarnings("hiding")
    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final TechId projectId;

    // cannot use 'originalData' because Project does not implement IEntityInformationHolder
    private Project originalProject;

    public static DatabaseModificationAwareComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext, final TechId projectId)
    {
        ProjectViewer viewer = new ProjectViewer(viewContext, projectId);
        return new DatabaseModificationAwareComponent(viewer, viewer);
    }

    private ProjectViewer(final IViewContext<ICommonClientServiceAsync> viewContext,
            final TechId projectId)
    {
        super(viewContext, createId(projectId));
        this.projectId = projectId;
        this.viewContext = viewContext;
        setLayout(new BorderLayout());
        extendToolBar();
        reloadAllData();
    }

    private void extendToolBar()
    {
        if (viewContext.isSimpleOrEmbeddedMode())
        {
            return;
        }
        addToolBarButton(createDeleteButton(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    new ProjectListDeletionConfirmationDialog(viewContext, originalProject,
                            createPermanentDeletionCallback()).show();
                }
            }));
    }

    @Override
    protected void reloadAllData()
    {
        reloadData(new ProjectInfoCallback(viewContext, this));
    }

    public static String createId(final TechId projectId)
    {
        return ID_PREFIX + projectId;
    }

    /**
     * Load the project information.
     */
    protected void reloadData(AbstractAsyncCallback<Project> callback)
    {
        viewContext.getService().getProjectInfo(projectId, callback);
    }

    private TabContent createAttachmentsSection(final Project project)
    {
        return new AttachmentVersionsSection(viewContext.getCommonViewContext(), project);
    }

    private static final class ProjectInfoCallback extends AbstractAsyncCallback<Project>
    {
        private final ProjectViewer viewer;

        private ProjectInfoCallback(final IViewContext<ICommonClientServiceAsync> viewContext,
                final ProjectViewer viewer)
        {
            super(viewContext);
            this.viewer = viewer;
        }

        @Override
        protected final void process(final Project result)
        {
            viewer.recreateView(result);
        }

        @Override
        public void finishOnFailure(Throwable caught)
        {
            viewer.setupRemovedEntityView();
        }
    }

    private void recreateView(final Project project)
    {
        updateOriginalProject(project);
        removeAll();
        // Top panel
        final Component topPanel = createTopPanel(project);
        add(topPanel, BorderLayoutDataFactory.create(LayoutRegion.NORTH, 150));
        // Central panel
        final Component centerPanel = createCenterPanel(project);
        add(centerPanel, BorderLayoutDataFactory.create(LayoutRegion.CENTER));

        layout();
    }

    private void updateOriginalProject(Project result)
    {
        this.originalProject = result;
        updateBreadcrumbs();
        setToolBarButtonsEnabled(true);
    }

    @Override
    public void setupRemovedEntityView()
    {
        removeAll();
        updateTitle(getOriginalDataDescription() + " does not exist any more.");
        setToolBarButtonsEnabled(false);
    }

    public Component createCenterPanel(Project result)
    {
        final TabContent panel = createAttachmentsSection(result);
        // need to set content visibility here as this section is not added to SectionPanel
        panel.setContentVisible(true);
        return panel;
    }

    public Component createTopPanel(Project result)
    {
        final ContentPanel panel = new ProjectPropertiesPanel(result, viewContext);
        panel.setScrollMode(Scroll.AUTOY);
        return panel;
    }

    @Override
    protected String getOriginalDataDescription()
    {
        return viewContext.getMessage(Dict.PROJECT) + " " + originalProject.getIdentifier();
    }

    @Override
    protected void showEntityEditor(boolean inBackground)
    {
        assert originalProject != null;
        ProjectGrid.showEntityViewer(originalProject, true, viewContext, inBackground);
    }

    @Override
    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        reloadAllData();
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return DatabaseModificationKind.any(ObjectKind.PROJECT);
    }

    @Override
    protected String getDeleteButtonLabel()
    {
        return viewContext.getMessage(Dict.BUTTON_DELETE_PROJECT);
    }
}
