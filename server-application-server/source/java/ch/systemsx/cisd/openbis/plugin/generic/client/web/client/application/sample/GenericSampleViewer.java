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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AttachmentVersionsSection;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.DisposableTabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.TabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractDatabaseModificationObserverWithCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.CompositeDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.CompositeDatabaseModificationObserverWithMainObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IComponentWithRefresh;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractViewerWithVerticalSplit;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PropertyValueRenderers;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.deletion.RevertDeletionConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.EntityHistoryGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.PropertyGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleListDeletionConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.ExternalHyperlink;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.SectionsPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.EntityDeletionConfirmationUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleDisplayCriteria2;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleChildrenInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.WebAppContext;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.PropertiesPanelUtils;

/**
 * The <i>generic</i> sample viewer.
 * 
 * @author Christian Ribeaud
 */
abstract public class GenericSampleViewer extends AbstractViewerWithVerticalSplit<Sample> implements
        IDatabaseModificationObserver, IComponentWithRefresh
{
    private static final String GENERIC_SAMPLE_VIEWER = "generic-sample-viewer";

    private static final String PREFIX = GENERIC_SAMPLE_VIEWER + "_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    public static final String PROPERTIES_ID_PREFIX = GenericConstants.ID_PREFIX
            + "generic-sample-properties-viewer_";

    protected final TechId sampleId;

    private DisposableTabContent attachmentsSection;

    private DisposableTabContent containerSamplesSection;

    private DisposableTabContent derivedSamplesSection;

    private DisposableTabContent parentSamplesSection;

    private DisposableTabContent dataSetSection;

    private PropertyGrid propertyGrid;

    private SectionsPanel rightPanel;

    private StringBuffer additionalMessage;

    public static DatabaseModificationAwareComponent create(
            final IViewContext<IGenericClientServiceAsync> creationViewContext,
            final IIdAndCodeHolder identifiable)
    {
        GenericSampleViewer viewer = new GenericSampleViewer(creationViewContext, identifiable)
            {
                @Override
                protected void loadSampleGenerationInfo(TechId sampleTechId,
                        AsyncCallback<SampleParentWithDerived> callback)
                {
                    TechId techId = TechId.create(identifiable);
                    creationViewContext.getService().getSampleGenerationInfo(techId, callback);
                }

            };
        viewer.reloadAllData();
        return new DatabaseModificationAwareComponent(viewer, viewer);
    }

    abstract protected void loadSampleGenerationInfo(final TechId sampleTechId,
            AsyncCallback<SampleParentWithDerived> asyncCallback);

    protected GenericSampleViewer(final IViewContext<?> viewContext,
            final IIdAndCodeHolder identifiable)
    {
        super(viewContext, createId(identifiable));
        setLayout(new BorderLayout());
        this.sampleId = TechId.create(identifiable);
        extendToolBar();
    }

    @Override
    protected void fillBreadcrumbWidgets(List<Widget> widgets)
    {
        if (originalData.getSpace() != null)
        {
            Widget spaceBreadcrumb = createSpaceLink(originalData.getSpace());
            widgets.add(spaceBreadcrumb);
        }
        if (originalData.getProject() != null && originalData.getExperiment() == null)
        {
            widgets.add(createProjectLink(originalData.getProject()));
        }
        if (originalData.getExperiment() != null)
        {
            Widget projectBreadcrumb = createProjectLink(originalData.getExperiment().getProject());
            widgets.add(projectBreadcrumb);
            Widget experimentBreadcrumb = createEntityLink(originalData.getExperiment());
            widgets.add(experimentBreadcrumb);
        }

        super.fillBreadcrumbWidgets(widgets);
    }

    private void extendToolBar()
    {
        if (getViewContext().isSimpleOrEmbeddedMode())
        {
            return;
        }
        addToolBarButton(createDeleteButton(new IDelegatedAction()
            {
                @Override
                @SuppressWarnings({ "unchecked", "rawtypes" })
                public void execute()
                {
                    final AsyncCallback<Void> callback =
                            isTrashEnabled() ? createDeletionCallback()
                                    : createPermanentDeletionCallback();
                    additionalMessage = new StringBuffer();

                    // we need info for just 1 sample/
                    List<TechId> sampleIds = new ArrayList<TechId>(Arrays.asList(TechId.create(getOriginalData())));
                    viewContext.getCommonService().getSampleChildrenInfo(sampleIds, true,
                            new AbstractAsyncCallback<List<SampleChildrenInfo>>(viewContext)
                                {
                                    @Override
                                    protected void process(List<SampleChildrenInfo> info)
                                    {
                                        SampleChildrenInfo sampleInfo = info.get(0);

                                        additionalMessage.append(EntityDeletionConfirmationUtils.getMessageForSingleSample(viewContext, sampleInfo));

                                        new SampleListDeletionConfirmationDialog(getViewContext()
                                                .getCommonViewContext(), getOriginalDataAsSingleton(), callback,
                                                getOriginalData(), additionalMessage.toString()).show();
                                    }
                                });
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
    }

    @Override
    protected void reloadAllData()
    {
        reloadSampleGenerationData(new SampleGenerationInfoCallback(getViewContext(), this));
    }

    public static final String createId(final IIdAndCodeHolder identifiable)
    {
        return createId(TechId.create(identifiable));
    }

    public static final String createId(final TechId sampleId)
    {
        return ID_PREFIX + sampleId;
    }

    private final SectionsPanel createRightPanel(SampleParentWithDerived sampleGeneration)
    {
        final Sample generator = sampleGeneration.getParent();

        final IViewContext<?> context = getViewContext();
        final SectionsPanel container = new SectionsPanel(context.getCommonViewContext(), getId());
        container.setDisplayID(DisplayTypeIDGenerator.GENERIC_SAMPLE_VIEWER, displayIdSuffix);
        List<TabContent> additionalPanels = createAdditionalSectionPanels();
        for (TabContent panel : additionalPanels)
        {
            container.addSection(panel);
        }
        // Contained samples
        containerSamplesSection = new ContainerSamplesSection(context, generator);
        container.addSection(containerSamplesSection);
        // Derived samples
        derivedSamplesSection = new DerivedSamplesSection(context, generator);
        container.addSection(derivedSamplesSection);
        // Parent samples
        parentSamplesSection = new ParentSamplesSection(context, generator);
        container.addSection(parentSamplesSection);
        // Data Sets
        dataSetSection = new SampleDataSetsSection(context, sampleId, generator.getSampleType());
        container.addSection(dataSetSection);
        // Properties History
        container.addSection(EntityHistoryGrid.createPropertiesHistorySection(viewContext,
                EntityKind.SAMPLE, sampleId));

        // Attachments
        attachmentsSection = createAttachmentsSection(generator);
        container.addSection(attachmentsSection);

        container.layout();

        // managed properties
        attachManagedPropertiesSections(container, generator);

        moduleSectionManager.initialize(container, generator);

        attachWebAppsSections(container, generator, WebAppContext.SAMPLE_DETAILS_VIEW);

        return container;
    }

    /**
     * To be subclassed. Creates additional panels of the viewer in the right side section besides components, datasets and attachments
     */
    protected List<TabContent> createAdditionalSectionPanels()
    {
        return new ArrayList<TabContent>();
    }

    private AttachmentVersionsSection createAttachmentsSection(final Sample sample)
    {
        return new AttachmentVersionsSection(getViewContext().getCommonViewContext(), sample);
    }

    private final static Map<String, Object> createProperties(final IViewContext<?> viewContext,
            final SampleParentWithDerived sampleGeneration)
    {
        final Map<String, Object> properties = new LinkedHashMap<String, Object>();
        final Sample sample = sampleGeneration.getParent();
        final SampleType sampleType = sample.getSampleType();
        final Sample[] generated = sampleGeneration.getDerived();
        properties.put(viewContext.getMessage(Dict.SAMPLE_PROPERTIES_PANEL_SAMPLE_IDENTIFIER),
                sample.getIdentifier());
        properties.put(viewContext.getMessage(Dict.PERM_ID),
                new ExternalHyperlink(sample.getPermId(), sample.getPermlink()));
        properties.put(viewContext.getMessage(Dict.SAMPLE_TYPE), sampleType);
        properties.put(viewContext.getMessage(Dict.REGISTRATOR), sample.getRegistrator());
        properties
                .put(viewContext.getMessage(Dict.REGISTRATION_DATE), sample.getRegistrationDate());
        final Deletion deletion = sample.getDeletion();
        if (deletion != null)
        {
            properties.put(viewContext.getMessage(Dict.DELETION), deletion);
        }

        Experiment experiment = sample.getExperiment();
        Project project = sample.getProject();
        if (experiment != null)
        {
            properties.put(viewContext.getMessage(Dict.PROJECT), experiment.getProject());
            properties.put(viewContext.getMessage(Dict.EXPERIMENT), experiment);
        } else if (project != null)
        {
            properties.put(viewContext.getMessage(Dict.PROJECT), project);
        }

        // If there is only one Derived Sample it can be shown as a property,
        // otherwise show number of samples (users should use Derived Samples section).
        if (generated.length == 1)
        {
            properties.put(viewContext.getMessage(Dict.DERIVED_SAMPLE), generated);
        } else if (generated.length > 1)
        {
            properties.put(viewContext.getMessage(Dict.DERIVED_SAMPLES), generated.length);
        }
        final Set<Sample> parents = sample.getParents();
        final int parentsSize = parents.size();
        if (parentsSize == 1)
        {
            properties.put(viewContext.getMessage(Dict.PARENT), parents.iterator().next());
        } else if (parentsSize > 1)
        {
            properties.put(viewContext.getMessage(Dict.PARENTS), parentsSize);
        }
        Sample partOf = sample.getContainer();
        if (partOf != null)
        {
            properties.put(viewContext.getMessage(Dict.PART_OF), partOf);
        }

        PropertiesPanelUtils.addMetaprojects(viewContext, properties, sample.getMetaprojects());
        PropertiesPanelUtils.addEntityProperties(viewContext, properties, sample.getProperties());

        return properties;
    }

    private final Component createLeftPanel(final SampleParentWithDerived sampleGeneration)
    {
        final ContentPanel panel = new ContentPanel();
        panel.setScrollMode(Scroll.AUTOY);
        panel.setHeading(getViewContext().getMessage(Dict.SAMPLE_PROPERTIES_HEADING));
        viewContext.log("create property section");
        propertyGrid = createPropertyGrid(getViewContext(), sampleId);
        propertyGrid.getElement().setId(PROPERTIES_ID_PREFIX + sampleId);
        updateProperties(sampleGeneration);
        panel.add(propertyGrid);

        return panel;
    }

    public static PropertyGrid createPropertyGrid(final IViewContext<?> viewContext,
            final TechId sampleId)
    {
        final IMessageProvider messageProvider = viewContext;
        final PropertyGrid propertyGrid = new PropertyGrid(viewContext, 0);
        propertyGrid.registerPropertyValueRenderer(SampleType.class,
                PropertyValueRenderers.createSampleTypePropertyValueRenderer(messageProvider));
        return propertyGrid;
    }

    public final void updateProperties(final SampleParentWithDerived sampleGeneration)
    {
        propertyGrid.resizeRows(0);
        final Map<String, Object> properties = createProperties(viewContext, sampleGeneration);
        propertyGrid.setProperties(properties);
        Sample sample = sampleGeneration.getParent();
        SampleType sampleType = sample.getSampleType();
        if (sampleType.isShowParentMetadata())
        {
            Set<Sample> parents = sample.getParents();
            if (parents.isEmpty() == false)
            {
                ListSampleCriteria listCriteria =
                        ListSampleCriteria.createForChild(new TechId(sample.getId()));
                viewContext.getCommonService().listSamples2(
                        new ListSampleDisplayCriteria2(listCriteria),
                        new AbstractAsyncCallback<TypedTableResultSet<Sample>>(viewContext)
                            {
                                @Override
                                protected void process(TypedTableResultSet<Sample> result)
                                {
                                    GridRowModels<TableModelRowWithObject<Sample>> list =
                                            result.getResultSet().getList();
                                    ParentsPropertiesSectionBuilder builder =
                                            new ParentsPropertiesSectionBuilder();
                                    for (GridRowModel<TableModelRowWithObject<Sample>> row : list)
                                    {
                                        Sample parent = row.getOriginalObject().getObjectOrNull();
                                        builder.addParent(parent);
                                    }
                                    for (Entry<String, List<IEntityProperty>> entry : builder
                                            .getSections().entrySet())
                                    {
                                        String title = entry.getKey();
                                        List<IEntityProperty> parentProperties = entry.getValue();
                                        Map<String, Object> props =
                                                new LinkedHashMap<String, Object>();
                                        PropertiesPanelUtils.addEntityProperties(viewContext,
                                                props, parentProperties);
                                        propertyGrid.addAdditionalProperties(title, props);
                                    }
                                }
                            });
            }
        }
    }

    /**
     * Load the {@link SampleParentWithDerived} information.
     */
    protected void reloadSampleGenerationData(
            AbstractAsyncCallback<SampleParentWithDerived> callback)
    {
        loadSampleGenerationInfo(sampleId, callback);
    }

    //
    // Helper classes
    //

    private final class SampleGenerationInfoCallback extends
            AbstractAsyncCallback<SampleParentWithDerived>
    {
        private final GenericSampleViewer genericSampleViewer;

        private SampleGenerationInfoCallback(final IViewContext<?> viewContext,
                final GenericSampleViewer genericSampleViewer)
        {
            super(viewContext);
            this.genericSampleViewer = genericSampleViewer;
        }

        //
        // AbstractAsyncCallback
        //

        /**
         * Sets the {@link SampleParentWithDerived} for this <var>generic</var> sample viewer.
         * <p>
         * This method triggers the whole <i>GUI</i> construction.
         * </p>
         */
        @Override
        protected final void process(final SampleParentWithDerived result)
        {
            genericSampleViewer.updateOriginalData(result.getParent());
            genericSampleViewer.removeAll();
            // Left panel
            final Component leftPanel = genericSampleViewer.createLeftPanel(result);
            genericSampleViewer.add(leftPanel, genericSampleViewer.createLeftBorderLayoutData());
            genericSampleViewer.configureLeftPanel(leftPanel);
            // Right panel
            rightPanel = genericSampleViewer.createRightPanel(result);
            genericSampleViewer.add(rightPanel, createRightBorderLayoutData());

            genericSampleViewer.layout();
        }

    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return createDatabaseModificationObserver().getRelevantModifications();
    }

    @Override
    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        createDatabaseModificationObserver().update(observedModifications);
    }

    private IDatabaseModificationObserver createDatabaseModificationObserver()
    {
        CompositeDatabaseModificationObserver observer =
                new CompositeDatabaseModificationObserverWithMainObserver(
                        new PropertyGridDatabaseModificationObserver());
        if (dataSetSection != null)
        {
            observer.addObserver(dataSetSection.tryGetDatabaseModificationObserver());
        }
        if (attachmentsSection != null)
        {
            observer.addObserver(attachmentsSection.tryGetDatabaseModificationObserver());
        }
        if (containerSamplesSection != null)
        {
            observer.addObserver(containerSamplesSection.tryGetDatabaseModificationObserver());
        }
        if (derivedSamplesSection != null)
        {
            observer.addObserver(derivedSamplesSection.tryGetDatabaseModificationObserver());
        }
        if (parentSamplesSection != null)
        {
            observer.addObserver(parentSamplesSection.tryGetDatabaseModificationObserver());
        }
        return observer;
    }

    private class PropertyGridDatabaseModificationObserver extends
            AbstractDatabaseModificationObserverWithCallback
    {

        @Override
        public DatabaseModificationKind[] getRelevantModifications()
        {
            return new DatabaseModificationKind[] {
                    DatabaseModificationKind.createOrDelete(ObjectKind.SAMPLE),
                    DatabaseModificationKind.createOrDelete(ObjectKind.SAMPLE_TYPE),
                    DatabaseModificationKind.edit(ObjectKind.SAMPLE),
                    DatabaseModificationKind.edit(ObjectKind.SAMPLE_TYPE),
                    DatabaseModificationKind.createOrDelete(ObjectKind.EXPERIMENT),
                    DatabaseModificationKind.edit(ObjectKind.EXPERIMENT),
                    DatabaseModificationKind
                            .createOrDelete(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                    DatabaseModificationKind.edit(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                    DatabaseModificationKind.createOrDelete(ObjectKind.VOCABULARY_TERM),
                    DatabaseModificationKind.edit(ObjectKind.VOCABULARY_TERM),
                    DatabaseModificationKind.createOrDelete(ObjectKind.METAPROJECT),
                    DatabaseModificationKind.edit(ObjectKind.METAPROJECT) };
        }

        @Override
        public void update(Set<DatabaseModificationKind> observedModifications)
        {
            reloadSampleGenerationData(new ReloadPropertyGridCallback(getViewContext(),
                    GenericSampleViewer.this));
        }

        private final class ReloadPropertyGridCallback extends
                AbstractAsyncCallback<SampleParentWithDerived>
        {
            private final GenericSampleViewer genericSampleViewer;

            private ReloadPropertyGridCallback(final IViewContext<?> viewContext,
                    final GenericSampleViewer genericSampleViewer)
            {
                super(viewContext);
                this.genericSampleViewer = genericSampleViewer;
            }

            //
            // AbstractAsyncCallback
            //

            /** This method triggers reloading of the {@link PropertyGrid} data. */
            @Override
            protected final void process(final SampleParentWithDerived result)
            {
                genericSampleViewer.updateOriginalData(result.getParent());
                genericSampleViewer.updateProperties(result);
                executeSuccessfulUpdateCallback();
            }

            @Override
            public void finishOnFailure(Throwable caught)
            {
                genericSampleViewer.setupRemovedEntityView();
            }
        }
    }

    @Override
    protected String getDeleteButtonLabel()
    {
        return viewContext.getMessage(Dict.BUTTON_DELETE_SAMPLE);
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
