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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.dataset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.TabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IComponentWithRefresh;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.ActionMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.IActionMenuItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractViewerWithVerticalSplit;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.AbstractExternalDataGrid.SelectedAndDisplayedItems;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetListDeletionConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetUploadConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.deletion.RevertDeletionConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.EntityHistoryGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.SectionsPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedActionWithResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedDatasetCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.WebAppContext;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

/**
 * The <i>generic</i> dataset viewer.
 * 
 * @author Piotr Buczek
 */
abstract public class GenericDataSetViewer extends AbstractViewerWithVerticalSplit<AbstractExternalData>
        implements IDatabaseModificationObserver, IComponentWithRefresh
{
    public static final String PREFIX = "generic-dataset-viewer_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    private final ProcessButtonHolder processButtonHolder;

    protected final TechId datasetId;

    private boolean toolbarInitialized;

    private SectionsPanel rightPanel;

    public static DatabaseModificationAwareComponent create(
            final IViewContext<IGenericClientServiceAsync> localViewContext,
            final IIdAndCodeHolder identifiable)
    {
        GenericDataSetViewer viewer = new GenericDataSetViewer(localViewContext, identifiable)
            {
                @Override
                protected void loadDatasetInfo(TechId datasetTechId,
                        AsyncCallback<AbstractExternalData> asyncCallback)
                {
                    localViewContext.getService().getDataSetInfo(datasetTechId, asyncCallback);
                }
            };
        viewer.reloadAllData();
        return new DatabaseModificationAwareComponent(viewer, viewer);
    }

    protected GenericDataSetViewer(final IViewContext<?> viewContext,
            final IIdAndCodeHolder identifiable)
    {
        super(viewContext, createId(identifiable));
        setLayout(new BorderLayout());
        this.datasetId = TechId.create(identifiable);
        this.processButtonHolder = new ProcessButtonHolder();
    }

    abstract protected void loadDatasetInfo(TechId datasetTechId,
            AsyncCallback<AbstractExternalData> asyncCallback);

    /**
     * To be subclassed. Creates additional panels of the viewer in the right side section besides components, datasets and attachments
     */
    protected List<TabContent> createAdditionalSectionPanels(AbstractExternalData dataset)
    {
        return new ArrayList<TabContent>();
    }

    @Override
    protected void fillBreadcrumbWidgets(List<Widget> widgets)
    {
        Widget spaceBreadcrumb = createSpaceLink(originalData.getSpace());
        widgets.add(spaceBreadcrumb);

        Experiment experiment = originalData.getExperiment();
        if (experiment != null)
        {
            Widget projectBreadcrumb = createProjectLink(experiment.getProject());
            Widget experimentBreadcrumb = createEntityLink(experiment);
            widgets.add(projectBreadcrumb);
            widgets.add(experimentBreadcrumb);
        }

        Sample sample = originalData.getSample();
        if (sample != null)
        {
            Project project = sample.getProject();
            if (project != null && experiment == null)
            {
                widgets.add(createProjectLink(project));
            }
            Widget sampleBreadcrumb = createEntityLink(sample);
            widgets.add(sampleBreadcrumb);
        }

        super.fillBreadcrumbWidgets(widgets);
    }

    private void extendToolBar(final AbstractExternalData result)
    {
        if (toolbarInitialized)
        {
            return;
        } else
        {
            toolbarInitialized = true;
        }

        if (result.isLinkData() == false)
        {
            Button exportButton = new Button(viewContext.getMessage(Dict.BUTTON_UPLOAD_DATASETS));
            exportButton.addListener(Events.Select, new Listener<BaseEvent>()
                {
                    @Override
                    public void handleEvent(BaseEvent be)
                    {
                        TableModelRowWithObject<AbstractExternalData> row =
                                new TableModelRowWithObject<AbstractExternalData>(originalData, Arrays
                                        .<ISerializableComparable> asList());
                        @SuppressWarnings("unchecked")
                        final List<TableModelRowWithObject<AbstractExternalData>> dataSets =
                                Arrays.<TableModelRowWithObject<AbstractExternalData>> asList(row);
                        IDelegatedActionWithResult<SelectedAndDisplayedItems> action =
                                new IDelegatedActionWithResult<SelectedAndDisplayedItems>()
                                    {
                                        @Override
                                        public SelectedAndDisplayedItems execute()
                                        {
                                            return new SelectedAndDisplayedItems(dataSets, null, 1);
                                        }
                                    };
                        new DataSetUploadConfirmationDialog(dataSets, action, 1, viewContext)
                                .show();
                    }
                });
            addToolBarButton(exportButton);
        }
        if (getViewContext().isSimpleOrEmbeddedMode())
        {
            return;
        }
        addToolBarButton(createDeleteButton(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    final AsyncCallback<Void> callback =
                            isTrashEnabled() ? createDeletionCallback()
                                    : createPermanentDeletionCallback();
                    TableModelRowWithObject<AbstractExternalData> row =
                            new TableModelRowWithObject<AbstractExternalData>(getOriginalData(),
                                    Arrays.<ISerializableComparable> asList());
                    new DataSetListDeletionConfirmationDialog(getViewContext()
                            .getCommonViewContext(), callback, row).show();
                }

            }));
        addToolBarButton(createRevertDeletionButton(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    new RevertDeletionConfirmationDialog(getViewContext().getCommonViewContext(),
                            getOriginalData(), createRevertDeletionCallback()).show();
                }
            }));

        addToolBarButton(processButtonHolder.getButton());
    }

    public static final String createId(final IIdAndCodeHolder identifiable)
    {
        return createId(TechId.create(identifiable));
    }

    public static final String createId(final TechId datasetId)
    {
        return ID_PREFIX + datasetId;
    }

    /**
     * Load the dataset information.
     */
    @Override
    protected void reloadAllData()
    {
        loadDatasetInfo(datasetId, new DataSetInfoCallback(getViewContext(), this));
    }

    private final Component createLeftPanel(final AbstractExternalData dataset)
    {
        final ContentPanel panel = createDataSetPropertiesPanel(dataset);
        panel.setScrollMode(Scroll.AUTOY);

        return panel;
    }

    private ContentPanel createDataSetPropertiesPanel(final AbstractExternalData dataset)
    {
        return new DataSetPropertiesPanel(dataset, getViewContext());
    }

    private final SectionsPanel createRightPanel(final AbstractExternalData dataset)
    {
        final IViewContext<?> context = getViewContext();
        final SectionsPanel container =
                new SectionsPanel(context.getCommonViewContext(), ID_PREFIX + dataset.getId());
        container.setDisplayID(DisplayTypeIDGenerator.GENERIC_DATASET_VIEWER, displayIdSuffix);

        List<TabContent> additionalPanels = createAdditionalSectionPanels(dataset);
        for (TabContent panel : additionalPanels)
        {
            container.addSection(panel);
        }

        container.addSection(new DataViewSection(context, dataset));

        if (dataset.isContainer())
        {
            final TabContent containedSection = new DataSetContainedSection(context, dataset);
            container.addSection(containedSection);
        }
        final TabContent containerSection = new DataSetContainerSection(context, dataset);
        container.addSection(containerSection);

        // parents
        final TabContent parentsSection = new DataSetParentsSection(context, dataset);
        container.addSection(parentsSection);

        // children
        final TabContent childrenSection = new DataSetChildrenSection(context, dataset);
        container.addSection(childrenSection);

        // properties history
        container.addSection(EntityHistoryGrid.createPropertiesHistorySection(viewContext,
                EntityKind.DATA_SET, new TechId(dataset.getId())));

        // managed properties
        attachManagedPropertiesSections(container, dataset);

        moduleSectionManager.initialize(container, dataset);

        attachWebAppsSections(container, dataset, WebAppContext.DATA_SET_DETAILS_VIEW);

        return container;
    }

    private final class DataSetInfoCallback extends AbstractAsyncCallback<AbstractExternalData>
    {
        private final GenericDataSetViewer genericDataSetViewer;

        private DataSetInfoCallback(final IViewContext<?> viewContext,
                final GenericDataSetViewer genericSampleViewer)
        {
            super(viewContext);
            this.genericDataSetViewer = genericSampleViewer;
        }

        //
        // AbstractAsyncCallback
        //

        /**
         * Sets the {@link AbstractExternalData} for this <var>generic</var> dataset viewer.
         * <p>
         * This method triggers the whole <i>GUI</i> construction.
         * </p>
         */
        @Override
        protected final void process(final AbstractExternalData result)
        {
            genericDataSetViewer.extendToolBar(result);
            genericDataSetViewer.updateOriginalData(result);
            PhysicalDataSet dataSet = result.tryGetAsDataSet();
            if (dataSet != null && dataSet.getStatus().isAvailable() == false)
            {
                genericDataSetViewer.setupUnavailableDataSetView(dataSet);
            }
            genericDataSetViewer.removeAll();
            // Left panel
            final Component leftPanel = genericDataSetViewer.createLeftPanel(result);
            genericDataSetViewer.add(leftPanel, genericDataSetViewer.createLeftBorderLayoutData());
            genericDataSetViewer.configureLeftPanel(leftPanel);
            // Right panel
            rightPanel = genericDataSetViewer.createRightPanel(result);
            genericDataSetViewer.add(rightPanel, createRightBorderLayoutData());

            genericDataSetViewer.layout();
        }

        @Override
        public void finishOnFailure(Throwable caught)
        {
            genericDataSetViewer.setupRemovedEntityView();
        }
    }

    /** Updates data displayed in the browser when shown dataset is not available. */
    public void setupUnavailableDataSetView(final PhysicalDataSet result)
    {
        setToolBarButtonsEnabled(false);
        updateTitle(getOriginalDataDescription() + " (not available)");
        String msg =
                getViewContext().getMessage(Dict.DATASET_NOT_AVAILABLE_MSG, result.getCode(),
                        result.getStatus().getDescription().toLowerCase());
        MessageBox.info("Data not available", msg, null);
    }

    @Override
    protected void updateOriginalData(final AbstractExternalData result)
    {
        super.updateOriginalData(result);
        processButtonHolder.setupData(result);
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[] { DatabaseModificationKind.edit(ObjectKind.DATA_SET),
                DatabaseModificationKind.createOrDelete(ObjectKind.DATA_SET),
                DatabaseModificationKind.createOrDelete(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                DatabaseModificationKind.edit(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                DatabaseModificationKind.createOrDelete(ObjectKind.VOCABULARY_TERM),
                DatabaseModificationKind.edit(ObjectKind.VOCABULARY_TERM),
                DatabaseModificationKind.createOrDelete(ObjectKind.EXPERIMENT),
                DatabaseModificationKind.edit(ObjectKind.EXPERIMENT),
                DatabaseModificationKind.createOrDelete(ObjectKind.SAMPLE),
                DatabaseModificationKind.edit(ObjectKind.SAMPLE),
                DatabaseModificationKind.createOrDelete(ObjectKind.METAPROJECT),
                DatabaseModificationKind.edit(ObjectKind.METAPROJECT) };
    }

    @Override
    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        reloadAllData(); // reloads everything
    }

    /**
     * Holder of a {@link Button} that has a menu with items that schedule dataset plugin processing. The button is hidden at the beginning. When data
     * set is successfully loaded by the viewer and there is a nonempty list of plugins assigned to its data type data then the menu is filled and
     * button is shown.
     */
    private class ProcessButtonHolder
    {
        private final Button button;

        public ProcessButtonHolder()
        {
            this.button = createProcessButton();
        }

        private Button createProcessButton()
        {
            final Button result = new Button(getViewContext().getMessage(Dict.BUTTON_PROCESS));
            // need to set menu here, otherwise menu button will not be displayed
            result.setMenu(new Menu());
            result.hide();
            return result;
        }

        public Button getButton()
        {
            return this.button;
        }

        /** @param data external data that will be processed */
        public void setupData(final AbstractExternalData data)
        {
            getViewContext().getCommonService().listDataStoreServices(
                    DataStoreServiceKind.PROCESSING,
                    new ProcessingServicesCallback(getViewContext(), getOriginalData(), button));
        }
    }

    private static final class ProcessingServicesCallback extends
            AbstractAsyncCallback<List<DatastoreServiceDescription>>
    {

        private final AbstractExternalData dataset;

        private final Button processButton;

        public ProcessingServicesCallback(IViewContext<?> viewContext, AbstractExternalData dataset,
                Button processButton)
        {
            super(viewContext);
            this.dataset = dataset;
            this.processButton = processButton;
        }

        @Override
        protected void process(List<DatastoreServiceDescription> result)
        {
            List<DatastoreServiceDescription> matchingServices = filterNotMatching(result);
            if (matchingServices.size() > 0)
            {
                processButton.setMenu(createPerformProcessingMenu(matchingServices));
                processButton.show();
            }
        }

        private List<DatastoreServiceDescription> filterNotMatching(
                List<DatastoreServiceDescription> services)
        {
            List<DatastoreServiceDescription> matchingServices =
                    new ArrayList<DatastoreServiceDescription>();
            for (DatastoreServiceDescription service : services)
            {
                if (DatastoreServiceDescription.isMatching(service, dataset))
                {
                    matchingServices.add(service);
                }
            }
            return matchingServices;
        }

        private Menu createPerformProcessingMenu(List<DatastoreServiceDescription> services)
        {
            Menu result = new Menu();
            final DisplayedOrSelectedDatasetCriteria criteria =
                    DisplayedOrSelectedDatasetCriteria.createSelectedItems(Arrays.asList(dataset
                            .getCode()));
            for (DatastoreServiceDescription service : services)
            {
                result.add(new ActionMenu(createActionMenuItem(service), viewContext,
                        createProcessDatasetAction(service, criteria)));
            }
            return result;
        }

        private IActionMenuItem createActionMenuItem(final DatastoreServiceDescription service)
        {
            return new IActionMenuItem()
                {
                    @Override
                    public String getMenuId()
                    {
                        return service.getKey();
                    }

                    @Override
                    public String getMenuText(IMessageProvider messageProvider)
                    {
                        return service.getLabel();
                    }
                };
        }

        private IDelegatedAction createProcessDatasetAction(
                final DatastoreServiceDescription service,
                final DisplayedOrSelectedDatasetCriteria criteria)
        {
            return new IDelegatedAction()
                {
                    @Override
                    public void execute()
                    {
                        viewContext.getCommonService().processDatasets(service, criteria,
                                new ProcessingDisplayCallback(viewContext, service));
                    }
                };
        }

    }

    private static final class ProcessingDisplayCallback extends AbstractAsyncCallback<Void>
    {
        private final DatastoreServiceDescription service;

        private ProcessingDisplayCallback(IViewContext<?> viewContext,
                DatastoreServiceDescription service)
        {
            super(viewContext);
            this.service = service;
        }

        @Override
        public final void process(final Void result)
        {
            final String title = viewContext.getMessage(Dict.PROCESSING_INFO_TITLE);
            final String msg = viewContext.getMessage(Dict.PROCESSING_INFO_MSG, service.getLabel());
            MessageBox.info(title, msg, null);
        }
    }

    @Override
    protected String getDeleteButtonLabel()
    {
        return viewContext.getMessage(Dict.BUTTON_DELETE_DATA_SET);
    }

    @Override
    public void refresh()
    {
        if (rightPanel != null)
        {
            rightPanel.tryApplyDisplaySettings();
        }
    }
}
