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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.VoidAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SimplifiedBaseModelData;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.LinkExtractor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WidgetUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.ICodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.WidgetTreeGridCellRenderer;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * {@link LayoutContainer} containing a {@link TreeGrid} with projects loaded from the server. Main items of the tree are project spaces and projects
 * are their children.
 * 
 * @author Piotr Buczek
 */
public final class ProjectSelectionTreeGridContainer extends LayoutContainer implements
        IDisposableComponent
{

    public static final String ID = GenericConstants.ID_PREFIX + "select-project";

    private final IViewContext<?> viewContext;

    private final String initialSpaceCodeOrNull;

    private final String initialProjectIdentifierOrNull;

    private Project selectedProjectOrNull;

    private Space selectedSpaceOrNull;

    private SelectionChangedListener<?> selectionChangedListener;

    private Widget selectedProjectLinkOrNull;

    private final TreeGrid<ModelData> tree;

    private final Map<Project, Widget> projectLinks = new HashMap<Project, Widget>();

    private String resultSetKey;

    private ContentPanel contentPanel;

    public ProjectSelectionTreeGridContainer(final IViewContext<?> viewContext,
            String initialSpaceCodeOrNull, String initialProjectIdentifierOrNull)
    {
        super(new FitLayout());
        this.viewContext = viewContext;
        this.initialSpaceCodeOrNull = initialSpaceCodeOrNull;
        this.initialProjectIdentifierOrNull = initialProjectIdentifierOrNull;

        ColumnConfig codeColumn = createCodeColumn();
        ColumnModel columnModel = new ColumnModel(Arrays.asList(codeColumn));
        tree = createTreeGrid(columnModel);

        contentPanel = new ContentPanel();
        contentPanel.setBodyBorder(false);
        contentPanel.setHeading(viewContext.getMessage(Dict.PROJECT_SELECTOR_TITLE));
        contentPanel.setLayout(new FitLayout());
        contentPanel.add(tree);
        add(contentPanel);

        refreshTree();
    }

    public ProjectSelectionTreeGridContainer(final IViewContext<?> viewContext)
    {
        this(viewContext, null, null);
    }

    /** @return tree grid with empty store and specified column model */
    private TreeGrid<ModelData> createTreeGrid(ColumnModel columnModel)
    {
        final TreeGrid<ModelData> treeGrid =
                new TreeGrid<ModelData>(new TreeStore<ModelData>(), columnModel);
        // WORKAROUND see comment in AbstractBrowserGrid constructor
        treeGrid.setLazyRowRender(0);
        treeGrid.setId(ID);
        treeGrid.setBorders(true);
        treeGrid.setAutoExpandColumn(ModelDataPropertyNames.CODE);
        treeGrid.getTreeView().setForceFit(true);
        treeGrid.getTreeView().setSortingEnabled(false);
        treeGrid.getTreeView().setBufferEnabled(false);
        GWTUtils.setToolTip(treeGrid, viewContext.getMessage(Dict.PROJECT_SELECTOR_TOOLTIP));
        // switchOffFolderIcons
        treeGrid.getStyle().setNodeCloseIcon(null);
        treeGrid.getStyle().setNodeOpenIcon(null);

        treeGrid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        treeGrid.getSelectionModel().addSelectionChangedListener(
                new SelectionChangedListener<ModelData>()
                    {
                        @Override
                        public void selectionChanged(SelectionChangedEvent<ModelData> se)
                        {
                            if (selectedProjectOrNull != null)
                            {
                                selectedProjectLinkOrNull.setVisible(false);
                                selectedProjectLinkOrNull = null;
                                selectedProjectOrNull = null;
                            }
                            if (selectedSpaceOrNull != null)
                            {
                                selectedSpaceOrNull = null;
                            }

                            ModelData selected = se.getSelectedItem();

                            if (selected != null)
                            {
                                if (tree.isLeaf(selected))
                                {
                                    // project
                                    selectedProjectOrNull =
                                            (Project) selected.get(ModelDataPropertyNames.OBJECT);
                                    getSelectionChangedListener().handleEvent(null);
                                    selectedProjectLinkOrNull =
                                            projectLinks.get(selectedProjectOrNull);
                                    if (selectedProjectLinkOrNull != null)
                                    {
                                        selectedProjectLinkOrNull.setVisible(true);
                                    }
                                } else
                                {
                                    // space
                                    selectedSpaceOrNull =
                                            (Space) selected.get(ModelDataPropertyNames.OBJECT);
                                    getSelectionChangedListener().handleEvent(null);
                                }
                            }
                        }
                    });
        return treeGrid;
    }

    private ColumnConfig createCodeColumn()
    {
        final ColumnConfig columnConfig =
                new ColumnConfig(ModelDataPropertyNames.CODE,
                        viewContext.getMessage(Dict.PROJECT_SELECTOR_CODE_COLUMN), 1);

        columnConfig.setRenderer(new WidgetTreeGridCellRenderer<ModelData>()
            {
                // Messages that are used repeatedly
                final String projectSelectorDetailsLinkLabel = viewContext
                        .getMessage(Dict.PROJECT_SELECTOR_DETAILS_LINK_LABEL);

                final String projectSelectorDetailsLinkTooltip = viewContext
                        .getMessage(Dict.PROJECT_SELECTOR_DETAILS_LINK_TOOLTIP);

                final String projectSelectorDescriptionNotAvailable = viewContext
                        .getMessage(Dict.PROJECT_SELECTOR_DESCRIPTION_NOT_AVAILABLE);

                final String spaceMessage = viewContext.getMessage(Dict.SPACE);

                @Override
                public Widget getWidget(ModelData model, String property, ColumnData config,
                        int rowIndex, int colIndex, ListStore<ModelData> store, Grid<ModelData> grid)
                {
                    if (model instanceof SpaceItemModel)
                    {
                        return createSpaceWidget((SpaceItemModel) model);
                    } else if (model instanceof ProjectItemModel)
                    {
                        return createProjectWidget((ProjectItemModel) model);
                    } else
                    {
                        throw new UnsupportedOperationException("unsupported data model");
                    }
                }

                private Widget createSpaceWidget(SpaceItemModel model)
                {
                    final Space space = (Space) model.get(ModelDataPropertyNames.OBJECT);
                    final Widget result = new InlineHTML(space.getCode());
                    result.setTitle(createTooltipText(spaceMessage, space.getCode(),
                            space.getDescription()));
                    return result;
                }

                private Widget createProjectWidget(ProjectItemModel model)
                {
                    final Project project = (Project) model.get(ModelDataPropertyNames.OBJECT);

                    final String href = LinkExtractor.tryExtract(project);
                    final ClickHandler listener = new ClickHandler()
                        {
                            @Override
                            public void onClick(ClickEvent event)
                            {
                                OpenEntityDetailsTabHelper.open(viewContext, project,
                                        WidgetUtils.ifSpecialKeyPressed(event.getNativeEvent()),
                                        href);
                            }
                        };
                    final Widget detailsLink =
                            LinkRenderer.getLinkWidget(projectSelectorDetailsLinkLabel, listener,
                                    href);
                    detailsLink.setTitle(projectSelectorDetailsLinkTooltip);
                    projectLinks.put(project, detailsLink);

                    final FlowPanel panel =
                            new FlowPanelWithLinkAppearingOnMouseOver(project, detailsLink);
                    panel.setTitle(createTooltipText("Project", project.getCode(),
                            project.getDescription()));
                    panel.add(new InlineHTML(project.getCode() + " "));
                    panel.add(detailsLink);
                    return panel;
                }

                private String createTooltipText(String entity, String code,
                        String descriptionOrNull)
                {
                    String description =
                            descriptionOrNull == null ? projectSelectorDescriptionNotAvailable
                                    : StringEscapeUtils.unescapeHtml(descriptionOrNull);
                    return entity + " code: " + code + "\nDescription: " + description;
                }

                class FlowPanelWithLinkAppearingOnMouseOver extends FlowPanel
                {

                    private final Project project;

                    private final Widget link;

                    public FlowPanelWithLinkAppearingOnMouseOver(Project project, Widget link)
                    {
                        super();
                        this.project = project;
                        this.link = link;
                        sinkEvents(Event.ONMOUSEOVER | Event.ONMOUSEOUT);
                        if (isProjectSelected() == false)
                        {
                            link.setVisible(false);
                        }
                    }

                    private boolean isProjectSelected()
                    {
                        return project.equals(selectedProjectOrNull);
                    }

                    @Override
                    @SuppressWarnings("deprecation")
                    public void onBrowserEvent(Event event)
                    {
                        switch (DOM.eventGetType(event))
                        {
                            case Event.ONMOUSEOVER:
                                // Only fire the mouse over event if it's coming from
                                // outside
                                // this widget.
                            case Event.ONMOUSEOUT:
                                // Only fire the mouse out event if it's leaving this
                                // widget.
                                Element related = event != null ? event.getRelatedTarget() : null;
                                if (related != null && getElement().isOrHasChild(related))
                                {
                                    return;
                                }
                                // this is the only part different from Widget
                                if (isProjectSelected() == false)
                                {
                                    link.setVisible(DOM.eventGetType(event) == Event.ONMOUSEOVER);
                                }
                                //
                                break;
                        }
                        DomEvent.fireNativeEvent(event, this, this.getElement());
                    }
                }

            });
        columnConfig.setMenuDisabled(true);
        columnConfig.setSortable(true);
        return columnConfig;
    }

    /**
     * Returns the {@link Project} currently selected.
     * 
     * @return <code>null</code> if no project is selected.
     */
    public final Project tryGetSelectedProject()
    {
        return selectedProjectOrNull;
    }

    /**
     * Returns the {@link Space} currently selected.
     * 
     * @return <code>null</code> if no space is selected.
     */
    public final Space tryGetSelectedSpace()
    {
        return selectedSpaceOrNull;
    }

    private SelectionChangedListener<?> getSelectionChangedListener()
    {
        return selectionChangedListener;
    }

    public void setSelectionChangedListener(SelectionChangedListener<?> listener)
    {
        selectionChangedListener = listener;
    }

    private void clearTree()
    {
        projectLinks.clear();
        tree.getTreeStore().removeAll();
    }

    /**
     * Rebuilds the tree from a list of projects.
     */
    private void rebuildTree(List<Project> projects)
    {
        clearTree();
        addToStore(projects);
        tree.expandAll();
    }

    /** adds items for given <var>projects</var> to the tree */
    private void addToStore(List<Project> projects)
    {
        Set<Space> spaces = getSortedSpaces(projects);
        HashMap<Space, ModelData> spaceToModelMap = new HashMap<Space, ModelData>();
        List<ModelData> spaceModels = new ArrayList<ModelData>(spaces.size());
        for (Space space : spaces)
        {
            SpaceItemModel spaceModel = new SpaceItemModel(space);
            spaceModels.add(spaceModel);
            spaceToModelMap.put(space, spaceModel);
        }
        tree.getTreeStore().add(spaceModels, true);
        for (Project project : projects)
        {
            ModelData spaceModel = spaceToModelMap.get(project.getSpace());
            tree.setLeaf(spaceModel, false);
            ProjectItemModel projectModel = new ProjectItemModel(project);
            tree.getTreeStore().add(spaceModel, projectModel, false);
            tree.setLeaf(projectModel, true);
        }
    }

    /** @return a sorted set of spaces of given <var>projects</var> */
    private Set<Space> getSortedSpaces(List<Project> projects)
    {
        Set<Space> spaces = new TreeSet<Space>();
        for (final Project project : projects)
        {
            spaces.add(project.getSpace());
        }
        return spaces;
    }

    /**
     * Refreshes the whole tree. If the previously selected project is no longer present in the db, nothing will be selected. Otherwise the previous
     * selection will be preserved.
     */
    public void refreshTree()
    {
        loadData();
    }

    private void loadData()
    {
        // DefaultResultSetConfig<String, TableModelRowWithObject<Project>> config =
        // DefaultResultSetConfig.createFetchAll();
        // viewContext.getCommonService().listProjectsForTree(config,
        // new ListProjectsCallback(viewContext));
        viewContext.getCommonService().listProjectsForTree(new ListProjectsCallback(viewContext));
    }

    private void selectByProjectIdentifierIfPossible(String projectIdentifier)
    {
        GWTUtils.setSelectedItem(tree, ModelDataPropertyNames.PROJECT_IDENTIFIER, projectIdentifier);
    }

    private void selectBySpaceCodeIfPossible(String spaceCode)
    {
        GWTUtils.setSelectedItem(tree, ModelDataPropertyNames.CODE, spaceCode);
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return DatabaseModificationKind.any(ObjectKind.PROJECT);
    }

    @Override
    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        refreshTree();
    }

    @Override
    public Component getComponent()
    {
        return this;
    }

    @Override
    public void dispose()
    {
        if (resultSetKey != null)
        {
            viewContext.getCommonService().removeResultSet(resultSetKey,
                    new VoidAsyncCallback<Void>(viewContext));
        }
    }

    //
    // Helper classes
    //
    private final class ListProjectsCallback extends AbstractAsyncCallback<List<Project>>
    {

        ListProjectsCallback(final IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(final List<Project> projects)
        {
            rebuildTree(projects);

            String projectIdentifierOrNull = tryGetProjectIdentifierToSelect();
            if (projectIdentifierOrNull != null)
            {
                selectByProjectIdentifierIfPossible(projectIdentifierOrNull);
            } else
            {
                String spaceCodeOrNull = tryGetSpaceCodeToSelect();
                if (spaceCodeOrNull != null)
                {
                    selectBySpaceCodeIfPossible(spaceCodeOrNull);
                }
            }
        }

        private String tryGetProjectIdentifierToSelect()
        {
            return selectedProjectOrNull != null ? selectedProjectOrNull.getIdentifier()
                    : initialProjectIdentifierOrNull;
        }

        private String tryGetSpaceCodeToSelect()
        {
            return selectedSpaceOrNull != null ? selectedSpaceOrNull.getCode()
                    : initialSpaceCodeOrNull;
        }

    }

    private static class BaseModelDataWithCode extends SimplifiedBaseModelData
    {
        private static final long serialVersionUID = 1L;

        public BaseModelDataWithCode(ICodeHolder codeProvider)
        {
            set(ModelDataPropertyNames.CODE, codeProvider.getCode());
            set(ModelDataPropertyNames.OBJECT, codeProvider);
        }

        @Override
        public String toString()
        {
            return get(ModelDataPropertyNames.CODE);
        }
    }

    public static final String PROJECT_WITH_SPACE_CODE = "projectWithSpaceCode";

    private static class ProjectItemModel extends BaseModelDataWithCode
    {

        private static final long serialVersionUID = 1L;

        public ProjectItemModel(Project project)
        {
            super(project);
            set(ModelDataPropertyNames.PROJECT_IDENTIFIER, project.getIdentifier());
            set(PROJECT_WITH_SPACE_CODE, getProjectWithSpaceCode(project));
        }

        private static String getProjectWithSpaceCode(Project project)
        {
            return project.getCode() + " (" + project.getSpace().getCode() + ")";
        }

        // equals is needed because of weird TreeGrid implementation in GXT
        // to be able to select of previously selected project after refresh of the tree
        // #selectByProjectIdentifierIfPossible()
        @Override
        public boolean equals(Object obj)
        {
            if (obj == this)
            {
                return true;
            }
            if (obj instanceof ProjectItemModel == false)
            {
                return false;
            }
            final ProjectItemModel that = (ProjectItemModel) obj;
            return this.getIdentifier().equals(that.getIdentifier());
        }

        private String getIdentifier()
        {
            return get(ModelDataPropertyNames.PROJECT_IDENTIFIER);
        }

        @Override
        public int hashCode()
        {
            return getIdentifier().hashCode();
        }

    }

    private static class SpaceItemModel extends BaseModelDataWithCode
    {
        private static final long serialVersionUID = 1L;

        public SpaceItemModel(Space space)
        {
            super(space);
            set(ModelDataPropertyNames.CODE, space.getCode());
        }

        // equals is needed because of weird TreeGrid implementation in GXT
        // to be able to select of previously selected project after refresh of the tree
        // #selectBySpaceIdentifierIfPossible()
        @Override
        public boolean equals(Object obj)
        {
            if (obj == this)
            {
                return true;
            }
            if (obj instanceof SpaceItemModel == false)
            {
                return false;
            }
            final SpaceItemModel that = (SpaceItemModel) obj;
            return this.getCode().equals(that.getCode());
        }

        private String getCode()
        {
            return get(ModelDataPropertyNames.CODE);
        }

        @Override
        public int hashCode()
        {
            return getCode().hashCode();
        }

    }

}