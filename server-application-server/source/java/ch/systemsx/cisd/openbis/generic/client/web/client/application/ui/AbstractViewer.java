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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.Layout;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.TableRowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.DisposableTabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ManagedPropertySection;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.TabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AppEvents;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IModule;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IModuleInitializationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.LinkExtractor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabClickListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityEditorTabClickListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenExperimentBrowserTabClickListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.webapp.WebAppComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.webapp.WebAppSortingAndCodeComparator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.webapp.WebAppUrl;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.SectionsPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.EntityTypeUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ApplicationInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.DeletionUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.ICodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithProperties;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityVisit;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.WebApp;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.WebAppContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;

/**
 * @author Franz-Josef Elmer
 */
public abstract class AbstractViewer<D extends IEntityInformationHolder> extends ContentPanel
        implements IModuleInitializationObserver
{

    public static final String ID_EDIT_SUFFIX = "_edit";

    public static final String ID_DELETE_SUFFIX = "_edit";

    private ToolBar toolBar;

    private final List<Button> toolBarButtons = new ArrayList<Button>();

    private LabelToolItem titleLabel;

    private BreadcrumbContainer breadcrumbContainer;

    protected final IViewContext<?> viewContext;

    protected D originalData;

    private final DeletionButtonsManager deletionButtonsManager = new DeletionButtonsManager();

    protected final ModulesSectionsManager moduleSectionManager = new ModulesSectionsManager();

    // A suffix used to designate widgets owned by this panel
    protected String displayIdSuffix;

    /** reload data for the viewer, update original data and recreate the view */
    protected abstract void reloadAllData();

    public AbstractViewer(final IViewContext<?> viewContext, String id)
    {
        this(viewContext, null, id, true); // title is set later with updateTitle method
    }

    public AbstractViewer(final IViewContext<?> viewContext, String title, String id,
            boolean withToolBar)
    {
        this.viewContext = viewContext;
        setId(id);
        setHeaderVisible(false);
        if (withToolBar)
        {
            toolBar = new ToolBar();
            setTopComponent(toolBar);
            titleLabel = new LabelToolItem(title);
            toolBar.add(breadcrumbContainer = new BreadcrumbContainer(viewContext));
            toolBar.add(new FillToolItem());
            if (viewContext.isSimpleOrEmbeddedMode() == false)
            {
                Button editButton = createEditButton();
                if (editButton != null)
                {
                    addToolBarButton(editButton);
                }
            }
        }
        viewContext.getClientPluginFactoryProvider().registerModuleInitializationObserver(this);
    }

    protected abstract String getDeleteButtonLabel();

    protected Button createEditButton()
    {
        Button result = new Button(viewContext.getMessage(Dict.BUTTON_EDIT));
        result.setId(getId() + ID_EDIT_SUFFIX);
        result.addListener(Events.Select, new Listener<ButtonEvent>()
            {
                @Override
                public void handleEvent(ButtonEvent be)
                {
                    showEntityEditor(be.isShiftKey());
                }
            });
        result.disable();
        return result;
    }

    void updateDeletionButtons()
    {

    }

    protected Button createDeleteButton(final IDelegatedAction deleteAction)
    {
        Button result = new Button(getDeleteButtonLabel());
        result.setId(getId() + ID_DELETE_SUFFIX);
        result.addListener(Events.Select, new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent be)
                {
                    deleteAction.execute();
                }
            });
        if (DeletionUtils.isDeleted(originalData))
        {
            result.setVisible(false);
        } else
        {
            result.disable();
        }
        deletionButtonsManager.setDeleteButton(result);
        return result;
    }

    protected Button createRevertDeletionButton(final IDelegatedAction revertAction)
    {
        Button result = new Button(viewContext.getMessage(Dict.BUTTON_REVERT_DELETION));
        result.addListener(Events.Select, new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent be)
                {
                    revertAction.execute();
                }
            });
        if (DeletionUtils.isDeleted(originalData))
        {
            result.disable();
        } else
        {
            result.setVisible(false);
        }
        deletionButtonsManager.setRevertButton(result);
        return result;
    }

    protected final void addToolBarButton(Button button)
    {
        toolBarButtons.add(button);
        toolBar.add(button);
    }

    protected final void updateTitle(String title)
    {
        titleLabel.setLabel(title);
    }

    protected final String getBaseIndexURL()
    {
        return GWTUtils.getBaseIndexURL();
    }

    protected D getOriginalData()
    {
        assert originalData != null : "data is not yet set";
        return originalData;
    }

    protected List<D> getOriginalDataAsSingleton()
    {
        return Collections.singletonList(getOriginalData());
    }

    protected void showEntityEditor(boolean inBackground)
    {
        assert originalData != null : "data is not yet set";
        showEntityEditor(originalData, inBackground);
    }

    private final void showEntityEditor(IEntityInformationHolder entity, boolean inBackground)
    {
        OpenEntityEditorTabClickListener.showEntityEditor(viewContext, entity, inBackground);
    }

    public static String getTitle(final IMessageProvider messageProvider,
            final String entityKindDictKey, final ICodeHolder codeProvider)
    {
        return messageProvider.getMessage(Dict.DETAILS_TITLE,
                messageProvider.getMessage(entityKindDictKey), codeProvider.getCode());
    }

    /** Updates data displayed in the browser (needed to open editor view). */
    protected void updateOriginalData(D newData)
    {
        if (newData instanceof IEntityInformationHolderWithIdentifier)
        {
            IEntityInformationHolderWithIdentifier entity =
                    (IEntityInformationHolderWithIdentifier) newData;
            EntityVisit entityVisit = new EntityVisit(entity);
            viewContext.getDisplaySettingsManager().rememberVisit(entityVisit);
        }
        this.originalData = newData;
        this.displayIdSuffix = newData.getEntityType().getCode();
        updateBreadcrumbs();
        setToolBarButtonsEnabled(true);
        deletionButtonsManager.updateButtonVisibitity();
    }

    /**
     * Fills the specified list of widgets with widgets to show in breadcrumbs. Subclasses should add to the list and invoke the method from
     * superclass to add the last breadcrumb widget which is generic and contains updateable title of the view.
     */
    protected void fillBreadcrumbWidgets(List<Widget> widgets)
    {
        widgets.add(titleLabel);
    }

    protected void updateBreadcrumbs()
    {
        updateTitle(getOriginalDataDescription());
        breadcrumbContainer.removeAll();
        List<Widget> widgets = new ArrayList<Widget>();
        fillBreadcrumbWidgets(widgets);
        for (Widget widget : widgets)
        {
            breadcrumbContainer.addBreadcrumb(widget);
        }
        breadcrumbContainer.layout();
        syncSize(); // fixes layout problems in simple view mode

        titleLabel.removeStyleName("xtb-text"); // WORKAROUND for consistent style of text
    }

    /** Updates data displayed in the browser when shown data has been removed from DB. */
    public void setupRemovedEntityView()
    {
        removeAll();
        updateTitle(getOriginalDataDescription() + " <b>does not exist any more</b>");
        setToolBarButtonsEnabled(false);
    }

    protected void setToolBarButtonsEnabled(boolean enabled)
    {
        for (Button button : toolBarButtons)
        {
            button.setEnabled(enabled);
        }
    }

    protected String getOriginalDataDescription()
    {
        return EntityTypeUtils.translatedEntityKindForUI(viewContext, originalData.getEntityKind()) 
                + " " + originalData.getCode() + " [" + originalData.getEntityType().getCode() + "]"
                + (isDeleted() ? " <b>(moved to trash)</b>" : "");
    }

    protected final boolean isDeleted()
    {
        return DeletionUtils.isDeleted(originalData);
    }

    protected final static BorderLayoutData createBorderLayoutData(LayoutRegion region)
    {
        return BorderLayoutDataFactory.create(region);
    }

    protected final boolean isTrashEnabled()
    {
        return viewContext.getModel().getApplicationInfo().getWebClientConfiguration()
                .getEnableTrash();
    }

    protected final AbstractAsyncCallback<Void> createPermanentDeletionCallback()
    {
        return new CloseViewerCallback(viewContext);
    }

    protected final AbstractAsyncCallback<Void> createDeletionCallback()
    {
        return new CloseViewerCallback(viewContext)
            {
                @Override
                protected void process(Void result)
                {
                    super.process(result);
                    GWTUtils.displayInfo(viewContext.getMessage(Dict.USE_TRASH_BROWSER));
                }
            };
    }

    protected final AbstractAsyncCallback<Void> createRevertDeletionCallback()
    {
        return new RefreshViewerCallback(viewContext);
    }

    private class CloseViewerCallback extends AbstractAsyncCallback<Void>
    {
        public CloseViewerCallback(IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(Void result)
        {
            fireEvent(AppEvents.CloseViewer);
        }
    }

    private class RefreshViewerCallback extends AbstractAsyncCallback<Void>
    {
        public RefreshViewerCallback(IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(Void result)
        {
            reloadAllData();
        }
    }

    @Override
    public void notify(List<IModule> modules)
    {
        moduleSectionManager.initialize(modules);
    }

    protected void attachManagedPropertiesSections(final SectionsPanel container,
            final IEntityInformationHolderWithProperties entity)
    {
        boolean sectionsAdded = false;
        for (final IEntityProperty property : entity.getProperties())
        {
            if (property.isManaged() && property instanceof IManagedProperty) // double check
            {
                IManagedProperty managedProperty = (IManagedProperty) property;
                if (managedProperty.isOwnTab())
                {
                    TabContent managedSection =
                            createManagedPropertySection(property.getPropertyType().getLabel(),
                                    entity, managedProperty);
                    container.addSection(managedSection);
                    sectionsAdded = true;
                }
            }
        }
        if (sectionsAdded == true)
        {
            container.layout();
        }
    }

    protected void attachModuleSpecificSections(final SectionsPanel container,
            final IEntityInformationHolderWithProperties entity)
    {
        moduleSectionManager.initialize(container, entity);
    }

    protected void attachWebAppsSections(final SectionsPanel container,
            final IEntityInformationHolderWithProperties entity, WebAppContext context)
    {
        List<WebApp> webApps = new ArrayList<WebApp>();

        // find web applications for the given context, entity kind and entity type
        for (final WebApp webApp : getApplicationInfo().getWebapps())
        {
            if (webApp.matchesContext(context)
                    && webApp.matchesEntity(entity.getEntityKind(), entity.getEntityType()))
            {
                webApps.add(webApp);
            }
        }

        // sort web applications using sorting and code properties
        Collections.sort(webApps, new WebAppSortingAndCodeComparator());

        // attach web applications to the container
        for (final WebApp webApp : webApps)
        {
            DisposableTabContent webAppTab =
                    new DisposableTabContent(webApp.getLabel(), viewContext, entity)
                        {
                            @Override
                            protected IDisposableComponent createDisposableContent()
                            {
                                return new IDisposableComponent()
                                    {

                                        @Override
                                        public void update(
                                                Set<DatabaseModificationKind> observedModifications)
                                        {
                                        }

                                        @Override
                                        public DatabaseModificationKind[] getRelevantModifications()
                                        {
                                            return new DatabaseModificationKind[0];
                                        }

                                        @Override
                                        public Component getComponent()
                                        {
                                            WebAppUrl url =
                                                    new WebAppUrl(Window.Location.getProtocol(),
                                                            Window.Location.getHost(),
                                                            Window.Location.getPath(),
                                                            webApp.getCode(), getSessionId());
                                            url.addEntityKind(entity.getEntityKind());
                                            url.addEntityType(entity.getEntityType());
                                            url.addEntityIdentifier(entity.getIdentifier());
                                            url.addEntityPermId(entity.getPermId());
                                            return new WebAppComponent(url);
                                        }

                                        @Override
                                        public void dispose()
                                        {
                                        }
                                    };
                            }
                        };
            webAppTab.setIds(new IDisplayTypeIDGenerator()
                {
                    @Override
                    public String createID(String suffix)
                    {
                        return createID() + suffix;
                    }

                    @Override
                    public String createID()
                    {
                        return DisplayTypeIDGenerator.WEBAPP_SECTION.createID() + "_" + webApp.getCode();
                    }
                });
            container.addSection(webAppTab);
        }
    }

    protected TabContent createManagedPropertySection(final String header,
            final IEntityInformationHolder entity, final IManagedProperty managedProperty)
    {
        IDelegatedAction refreshAction = new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    reloadAllData();
                }
            };
        return new ManagedPropertySection(header, viewContext, entity, managedProperty,
                refreshAction);
    }

    protected Widget createLabel(final String label)
    {
        Widget result = new Html(label);
        return result;
    }

    protected Widget createSpaceLink(final Space space)
    {
        final String href = LinkExtractor.createExperimentBrowserLink(space.getCode(), null, null);
        ClickHandler listener =
                new OpenExperimentBrowserTabClickListener(viewContext, space.getCode(), null, true);
        Widget link = LinkRenderer.getLinkWidget(space.getCode(), listener, href);
        link.setTitle(viewContext.getMessage(Dict.SPACE) + " " + space.getCode());
        return link;
    }

    protected Widget createProjectLink(final Project project)
    {
        final String href =
                LinkExtractor.createExperimentBrowserLink(null, project.getIdentifier(), null);
        ClickHandler listener =
                new OpenExperimentBrowserTabClickListener(viewContext, null,
                        project.getIdentifier(), true);
        Widget link = LinkRenderer.getLinkWidget(project.getCode(), listener, href);
        link.setTitle(viewContext.getMessage(Dict.PROJECT) + " " + project.getCode());
        return link;
    }

    protected Widget createEntityLink(final IEntityInformationHolderWithPermId entity)
    {
        String href = LinkExtractor.tryExtract(entity);
        ClickHandler listener = new OpenEntityDetailsTabClickListener(entity, viewContext);
        Widget link =
                LinkRenderer.getLinkWidget(entity.getCode(), listener, href,
                        DeletionUtils.isDeleted(entity));
        link.setTitle(EntityTypeUtils.translatedEntityKindForUI(viewContext, entity.getEntityKind()) + " " + entity.getCode());
        return link;
    }

    private static class BreadcrumbContainer extends LayoutContainer
    {
        private final String separator;

        public BreadcrumbContainer(IMessageProvider messageProvider)
        {
            this.separator = messageProvider.getMessage(Dict.BREADCRUMBS_SEPARATOR);
            setLayout(createLayout());
        }

        private static Layout createLayout()
        {
            final TableRowLayout tableRowLayout = new TableRowLayout();
            tableRowLayout.setBorder(0);
            tableRowLayout.setCellPadding(0);
            tableRowLayout.setCellSpacing(2);
            return tableRowLayout;
        }

        /**
         * Adds the <var>widget</var> to breadcrumbs. For every widget but the first one a separator will be added before the widget.
         */
        public void addBreadcrumb(Widget widget)
        {
            if (getItemCount() > 0)
            {
                add(new Html(separator));
            }
            add(widget);
        }

    }

    private class DeletionButtonsManager
    {
        private Button deleteButtonOrNull;

        private Button revertButtonOrNull;

        public void setDeleteButton(Button deleteButton)
        {
            this.deleteButtonOrNull = deleteButton;
        }

        public void setRevertButton(Button revertButton)
        {
            this.revertButtonOrNull = revertButton;
        }

        public void updateButtonVisibitity()
        {
            if (deleteButtonOrNull != null)
            {
                deleteButtonOrNull.setVisible(DeletionUtils.isDeleted(originalData) == false);
            }
            if (revertButtonOrNull != null)
            {
                revertButtonOrNull.setVisible(DeletionUtils.isDeleted(originalData));
            }
        }
    }

    protected IViewContext<?> getViewContext()
    {
        return viewContext;
    }

    protected ApplicationInfo getApplicationInfo()
    {
        return getViewContext().getModel().getApplicationInfo();
    }

    protected String getSessionId()
    {
        return getViewContext().getModel().getSessionContext().getSessionID();
    }

}
