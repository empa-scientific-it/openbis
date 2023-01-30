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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AttachmentVersionsSection;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.DisposableTabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.CompositeDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.CompositeDatabaseModificationObserverWithMainObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IComponentWithRefresh;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractViewerWithVerticalSplit;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.deletion.RevertDeletionConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ExperimentListDeletionConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.EntityHistoryGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.SectionsPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDirectlyConnectedController;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.IAttachmentHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentHolderKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.WebAppContext;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.AbstractEntityDataSetsSection;

/**
 * The <i>generic</i> experiment viewer.
 * 
 * @author Izabela Adamczyk
 */
public class GenericExperimentViewer extends AbstractViewerWithVerticalSplit<Experiment> implements
        IDatabaseModificationObserver, IComponentWithRefresh
{
    private static final String GENERIC_EXPERIMENT_VIEWER = "generic-experiment-viewer";

    private static final String PREFIX = GENERIC_EXPERIMENT_VIEWER + "_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    protected final IViewContext<IGenericClientServiceAsync> localViewContext;

    protected final IIdAndCodeHolder experimentId;

    protected final BasicEntityType experimentType;

    protected Experiment experiment;

    private ExperimentPropertiesPanel propertiesPanelOrNull;

    private List<DisposableTabContent> rightPanelSectionsOrNull;

    private SectionsPanel rightPanel;

    public static DatabaseModificationAwareComponent create(
            final IViewContext<IGenericClientServiceAsync> viewContext,
            final BasicEntityType experimentType, final IIdAndCodeHolder identifiable)
    {
        GenericExperimentViewer viewer =
                new GenericExperimentViewer(viewContext, experimentType, identifiable);
        return new DatabaseModificationAwareComponent(viewer, viewer);
    }

    protected GenericExperimentViewer(final IViewContext<IGenericClientServiceAsync> viewContext,
            final BasicEntityType experimentType, final IIdAndCodeHolder experimentId)
    {
        super(viewContext, createId(experimentId));

        int logID = viewContext.log("create " + viewContext.getMessage(Dict.EXPERIMENT).toLowerCase() + " viewer");

        this.experimentId = experimentId;
        this.experimentType = experimentType;
        this.localViewContext = viewContext;
        setLayout(new BorderLayout());
        extendToolBar();

        reloadAllData();

        viewContext.logStop(logID);
    }

    @Override
    protected void fillBreadcrumbWidgets(List<Widget> widgets)
    {
        Widget spaceBreadcrumb = createSpaceLink(originalData.getProject().getSpace());
        Widget projectBreadcrumb = createProjectLink(originalData.getProject());
        widgets.add(spaceBreadcrumb);
        widgets.add(projectBreadcrumb);

        super.fillBreadcrumbWidgets(widgets);
    }

    private void extendToolBar()
    {
        if (localViewContext.isSimpleOrEmbeddedMode())
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
                    new ExperimentListDeletionConfirmationDialog(
                            localViewContext.getCommonViewContext(), callback, getOriginalData())
                            .show();
                }
            }));
        addToolBarButton(createRevertDeletionButton(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    new RevertDeletionConfirmationDialog(localViewContext.getCommonViewContext(),
                            getOriginalData(), createRevertDeletionCallback()).show();
                }
            }));
    }

    @Override
    protected void reloadAllData()
    {
        reloadExperiment(new AbstractAsyncCallback<Experiment>(localViewContext)
            {

                @Override
                protected final void process(final Experiment result)
                {
                    experiment = result;
                    layoutExperimentDetailView();
                }
            });
    }

    protected void reloadExperiment(AbstractAsyncCallback<Experiment> callback)
    {
        localViewContext.getCommonService().getExperimentInfo(new TechId(experimentId), callback);
    }

    private void layoutExperimentDetailView()
    {
        int logId = localViewContext.log("layoutExperimentDetailView");
        updateOriginalData(experiment);
        removeAll();

        propertiesPanelOrNull = new ExperimentPropertiesPanel(experiment, localViewContext, this);
        Component lowerLeftComponentOrNull = tryCreateLowerLeftComponent();
        if (lowerLeftComponentOrNull != null)
        {
            propertiesPanelOrNull.addSouthComponent(lowerLeftComponentOrNull);
        }
        add(propertiesPanelOrNull, createLeftBorderLayoutData());
        configureLeftPanel(propertiesPanelOrNull);

        final Html loadingLabel = new Html(localViewContext.getMessage(Dict.LOAD_IN_PROGRESS));
        add(loadingLabel, createRightBorderLayoutData());
        layout();
        localViewContext.logStop(logId);

        GWTUtils.executeDelayed(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    remove(loadingLabel);
                    GenericExperimentViewer.this.rightPanelSectionsOrNull = createRightPanel();
                    rightPanel = layoutSections(rightPanelSectionsOrNull);
                    attachManagedPropertiesSections(rightPanel, experiment);
                    attachModuleSpecificSections(rightPanel, experiment);
                    attachWebAppsSections(rightPanel, experiment,
                            WebAppContext.EXPERIMENT_DETAILS_VIEW);
                    add(rightPanel, createRightBorderLayoutData());
                    layout();
                }
            });
    }

    /**
     * Returns the component to be shown below experiment properties.
     * 
     * @return <code>null</code> if nothing should be shown (default behavior).
     */
    protected Component tryCreateLowerLeftComponent()
    {
        return null;
    }

    public static final String createId(final IIdAndCodeHolder identifiable)
    {
        return createId(TechId.create(identifiable));
    }

    public static final String createId(final TechId experimentId)
    {
        return ID_PREFIX + experimentId;
    }

    @Override
    public void updateOriginalData(Experiment newData)
    {
        super.updateOriginalData(newData);
    }

    private AttachmentVersionsSection createAttachmentsSection()
    {
        final IAttachmentHolder attachmentHolder = asExperimentAttachmentHolder(experimentId);
        return new AttachmentVersionsSection(localViewContext.getCommonViewContext(),
                attachmentHolder);
    }

    private static IAttachmentHolder asExperimentAttachmentHolder(
            final IIdAndCodeHolder identifiable)
    {
        return new IAttachmentHolder()
            {
                @Override
                public AttachmentHolderKind getAttachmentHolderKind()
                {
                    return AttachmentHolderKind.EXPERIMENT;
                }

                @Override
                public Long getId()
                {
                    return identifiable.getId();
                }

                @Override
                public String getCode()
                {
                    return identifiable.getCode();
                }
            };
    }

    private List<DisposableTabContent> createRightPanel()
    {

        List<DisposableTabContent> allPanels = new ArrayList<DisposableTabContent>();

        allPanels.addAll(createAdditionalBrowserSectionPanels());

        final DisposableTabContent sampleSection = createExperimentSampleSection();
        allPanels.add(sampleSection);

        final DisposableTabContent dataSection = createExperimentDataSetSection();
        dataSection.setIds(DisplayTypeIDGenerator.DATA_SETS_SECTION);
        allPanels.add(dataSection);

        allPanels.add(EntityHistoryGrid.createPropertiesHistorySection(localViewContext,
                EntityKind.EXPERIMENT, new TechId(experimentId)));

        final AttachmentVersionsSection attachmentsSection = createAttachmentsSection();
        allPanels.add(attachmentsSection);

        return allPanels;
    }

    protected DisposableTabContent createExperimentSampleSection()
    {
        return new ExperimentSamplesSection(localViewContext,
                localViewContext.getMessage(Dict.EXPERIMENT_SAMPLES_SELCTION_TITLE),
                experimentType, experimentId);
    }

    private DisposableTabContent createExperimentDataSetSection()
    {
        return new AbstractEntityDataSetsSection(localViewContext, new TechId(experimentId),
                experimentType)
            {

                @Override
                protected IDisposableComponent createDataSetBrowser(BasicEntityType type,
                        TechId entityID, IDirectlyConnectedController directlyConnectedController)
                {
                    return ExperimentDataSetBrowser.create(viewContext, new TechId(experimentId),
                            experimentType, directlyConnectedController);
                }
            };
    }

    private SectionsPanel layoutSections(List<DisposableTabContent> allPanels)
    {
        final SectionsPanel container =
                new SectionsPanel(localViewContext.getCommonViewContext(), ID_PREFIX + experimentId);
        container.setDisplayID(DisplayTypeIDGenerator.GENERIC_EXPERIMENT_VIEWER, displayIdSuffix);
        for (DisposableTabContent panel : allPanels)
        {
            container.addSection(panel);
        }
        container.layout();
        return container;
    }

    protected List<DisposableTabContent> createAdditionalBrowserSectionPanels()
    {
        return Collections.emptyList();
    }

    // this observer should not be cached - some sections may become visible in the future
    private IDatabaseModificationObserver createModificationObserver()
    {
        if (propertiesPanelOrNull != null)
        {
            assert rightPanelSectionsOrNull != null : "right panel not layouted";
            CompositeDatabaseModificationObserverWithMainObserver modificationObserver =
                    new CompositeDatabaseModificationObserverWithMainObserver(
                            propertiesPanelOrNull.getDatabaseModificationObserver());
            for (DisposableTabContent panel : rightPanelSectionsOrNull)
            {
                modificationObserver.addObserver(panel.tryGetDatabaseModificationObserver());
            }
            return modificationObserver;
        } else
        {
            return new CompositeDatabaseModificationObserver();
        }

    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return createModificationObserver().getRelevantModifications();
    }

    @Override
    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        createModificationObserver().update(observedModifications);
    }

    @Override
    protected String getDeleteButtonLabel()
    {
        return localViewContext.getMessage(Dict.BUTTON_DELETE_EXPERIMENT);
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
