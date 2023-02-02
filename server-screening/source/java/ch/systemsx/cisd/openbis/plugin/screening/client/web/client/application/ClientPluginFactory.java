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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageDomain;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactoryUsingWildcards;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IModule;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractViewer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.LinkExtractor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.ICodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.DelegatedClientPlugin;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.dataset.GenericDataSetViewer;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.GenericExperimentViewer;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.material.GenericMaterialViewer;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.GenericSampleViewer;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.ExperimentAnalysisSummaryViewer;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.ExperimentViewer;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.ImageDataSetViewer;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.ImageSampleViewer;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.ImagingMaterialViewer;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.MaterialFeaturesFromAllExperimentsViewer;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.MaterialReplicaSummaryViewer;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.MicroscopyDatasetViewer;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.PlateDatasetViewer;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.PlateSampleViewer;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.sample.LibrarySampleBatchRegistrationForm;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.AnalysisProcedureCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchByProjectCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.SingleExperimentSearchCriteria;

import com.google.gwt.user.client.ui.Widget;

/**
 * {@link IClientPluginFactory} implementation for <i>screening</i> plugin.
 * <p>
 * Currently, this implementation only runs for a sample of type SampleTypeCode#CELL_PLATE.
 * </p>
 * 
 * @author Tomasz Pylak
 */
public final class ClientPluginFactory extends AbstractClientPluginFactory<ScreeningViewContext>
        implements IClientPluginFactoryUsingWildcards
{

    public ClientPluginFactory(final IViewContext<ICommonClientServiceAsync> originalViewContext)
    {
        super(originalViewContext);
    }

    //
    // AbstractClientPluginFactory
    //

    @Override
    public boolean isEnabled()
    {
        return checkEnabledProperty("screening");
    }

    @Override
    protected final ScreeningViewContext createViewContext(
            final IViewContext<ICommonClientServiceAsync> originalViewContext)
    {
        return new ScreeningViewContext(originalViewContext);
    }

    //
    // IClientPluginFactoryUsingWildcards
    //

    @Override
    public List<String> getOrderedEntityTypeCodes(EntityKind entityKind)
    {
        ArrayList<String> types = new ArrayList<String>();
        if (entityKind == EntityKind.SAMPLE)
        {
            // -- plate layout
            types.add(ScreeningConstants.HCS_PLATE_SAMPLE_TYPE_PATTERN);
            // -- library registration
            types.add(ScreeningConstants.LIBRARY_PLUGIN_TYPE_CODE);
            // -- screening well
            types.add(ScreeningConstants.CONTROL_WELL_SAMPLE_TYPE_PATTERN);
            types.add(ScreeningConstants.NON_CONTROL_WELL_SAMPLE_TYPE_PATTERN);
            // -- microscopy sample
            types.add(ScreeningConstants.MICROSCOPY_IMAGE_SAMPLE_TYPE_PATTERN);
        } else if (entityKind == EntityKind.MATERIAL)
        {
            // Should we do this for materials? (* pattern)
            types.add(ScreeningConstants.GENE_PLUGIN_TYPE_CODE);
            types.add(ScreeningConstants.SIRNA_PLUGIN_TYPE_NAME);
            types.add("CONTROL");
            types.add("COMPOUND");

        } else if (entityKind == EntityKind.DATA_SET)
        {
            types.add(ScreeningConstants.ANY_HCS_IMAGE_DATASET_TYPE_PATTERN);
            types.add(ScreeningConstants.ANY_MICROSCOPY_IMAGE_DATASET_TYPE_PATTERN);
        } else if (entityKind == EntityKind.EXPERIMENT)
        {
            types.add(ScreeningConstants.HCS_EXPERIMENT_TYPE_PATTERN);
        }
        return types;
    }

    @Override
    public final Set<String> getEntityTypeCodes(final EntityKind entityKind)
    {
        Set<String> types = new HashSet<String>();
        types.addAll(getOrderedEntityTypeCodes(entityKind));
        return types;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BasicEntityType, I extends IIdAndCodeHolder> IClientPlugin<T, I> createClientPlugin(
            final EntityKind entityKind)
    {
        ScreeningViewContext viewContext = getViewContext();
        if (EntityKind.MATERIAL.equals(entityKind))
        {
            return (IClientPlugin<T, I>) new MaterialClientPlugin(viewContext);
        }
        if (EntityKind.SAMPLE.equals(entityKind))
        {
            return (IClientPlugin<T, I>) new SampleClientPlugin(viewContext);
        }
        if (EntityKind.DATA_SET.equals(entityKind))
        {
            return (IClientPlugin<T, I>) new ImageDataSetViewerPlugin(viewContext);
        } else if (EntityKind.EXPERIMENT.equals(entityKind))
        {
            return (IClientPlugin<T, I>) new ExperimentClientPlugin(viewContext);
        }

        throw new UnsupportedOperationException("IClientPlugin for entity kind '" + entityKind
                + "' not implemented yet.");
    }

    //
    // Helper classes
    //

    private final class MaterialClientPlugin extends DelegatedClientPlugin<MaterialType>
    {
        private MaterialClientPlugin(IViewContext<IScreeningClientServiceAsync> viewContext)
        {
            super(viewContext, EntityKind.MATERIAL);
        }

        @Override
        public final AbstractTabItemFactory createEntityViewer(
                final IEntityInformationHolderWithPermId entity)
        {
            return createImagingMaterialViewerTabFactory(entity, null,
                    AnalysisProcedureCriteria.createNoProcedures(), getViewContext());
        }
    }

    private final class ExperimentClientPlugin extends DelegatedClientPlugin<ExperimentType>
    {
        private ExperimentClientPlugin(IViewContext<IScreeningClientServiceAsync> viewContext)
        {
            super(viewContext, EntityKind.EXPERIMENT);
        }

        @Override
        public final AbstractTabItemFactory createEntityViewer(
                final IEntityInformationHolderWithPermId entity)
        {
            return new AbstractTabItemFactory()
                {
                    @Override
                    public ITabItem create()
                    {
                        final DatabaseModificationAwareComponent experimentViewer =
                                ExperimentViewer.createComponent(getViewContext(), entity.getEntityType(),
                                        entity);
                        return DefaultTabItem.create(getTabTitle(), experimentViewer,
                                getViewContext(), false);
                    }

                    @Override
                    public String getId()
                    {
                        return GenericExperimentViewer.createId(entity);
                    }

                    @Override
                    public HelpPageIdentifier getHelpPageIdentifier()
                    {
                        return new HelpPageIdentifier(HelpPageDomain.EXPERIMENT,
                                HelpPageAction.VIEW);
                    }

                    @Override
                    public String getTabTitle()
                    {
                        return getViewerTitle(ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict.EXPERIMENT, entity,
                                getViewContext());
                    }

                    @Override
                    public String tryGetLink()
                    {
                        return LinkExtractor.tryExtract(entity);
                    }
                };
        }
    }

    /**
     * Opens experiment detail viewer. In embedded mode only the content of the analysis summary tab is presented.
     */
    public static final void openImagingExperimentViewer(
            final IEntityInformationHolderWithPermId experiment,
            boolean restrictGlobalScopeLinkToProject,
            final IViewContext<IScreeningClientServiceAsync> viewContext)
    {
        if (viewContext.getModel().isEmbeddedMode())
        {
            ExperimentAnalysisSummaryViewer.openTab(viewContext, new TechId(experiment),
                    restrictGlobalScopeLinkToProject, null);
        } else
        {
            new OpenEntityDetailsTabAction(experiment, viewContext).execute();
        }
    }

    /**
     * Creates a link to experiment detail viewer. In embedded mode the link will lead to the tab which has only the content of the analysis summary
     * panel.
     */
    public static String createImagingExperimentViewerLink(
            IEntityInformationHolderWithPermId experiment,
            boolean restrictGlobalScopeLinkToProject,
            IViewContext<IScreeningClientServiceAsync> viewContext)
    {
        if (viewContext.getModel().isEmbeddedMode())
        {
            return ScreeningLinkExtractor.createExperimentAnalysisSummaryBrowserLink(
                    experiment.getPermId(), restrictGlobalScopeLinkToProject, null);
        } else
        {
            return LinkExtractor.tryExtract(experiment);
        }
    }

    /**
     * Opens material detail viewer. Shows wells in which the material is contained, with a selected experiment. In embedded mode only the content of
     * the replica summary tab is presented.
     * 
     * @param experimentCriteriaOrNull note that null does NOT mean searching in all experiments, but that single experiment should be specified by
     *            the user.
     * @param computeRanks
     */
    public static final void openImagingMaterialViewer(
            final IEntityInformationHolderWithPermId material,
            final ExperimentSearchCriteria experimentCriteriaOrNull,
            final AnalysisProcedureCriteria analysisProcedureCriteria, boolean computeRanks,
            final IViewContext<IScreeningClientServiceAsync> viewContext)
    {
        if (viewContext.getModel().isEmbeddedMode())
        {
            MaterialIdentifier materialIdentifier = asMaterialIdentifier(material);
            ExperimentSearchByProjectCriteria multipleExperimentsScope =
                    tryAsMultipleExperimentsCriteria(experimentCriteriaOrNull);
            if (multipleExperimentsScope != null)
            {
                MaterialFeaturesFromAllExperimentsViewer.openTab(viewContext, materialIdentifier,
                        multipleExperimentsScope, analysisProcedureCriteria, computeRanks);
            } else
            {
                assert experimentCriteriaOrNull != null;
                SingleExperimentSearchCriteria experiment =
                        experimentCriteriaOrNull.tryGetExperiment();
                assert experiment != null;

                MaterialReplicaSummaryViewer.openTab(viewContext, experiment.getExperimentPermId(),
                        experimentCriteriaOrNull.getRestrictGlobalSearchLinkToProject(),
                        materialIdentifier, analysisProcedureCriteria);

            }
        } else
        {
            openImagingMaterialGenericViewer(material, experimentCriteriaOrNull,
                    analysisProcedureCriteria, viewContext);
        }
    }

    private static ExperimentSearchByProjectCriteria tryAsMultipleExperimentsCriteria(
            ExperimentSearchCriteria experimentCriteriaOrNull)
    {
        if (experimentCriteriaOrNull == null)
        {
            return ExperimentSearchByProjectCriteria.createAllExperimentsForAllProjects();
        }
        if (experimentCriteriaOrNull.tryGetExperiment() != null)
        {
            return null;
        }
        BasicProjectIdentifier project = experimentCriteriaOrNull.tryGetProjectIdentifier();
        if (project != null)
        {
            return ExperimentSearchByProjectCriteria.createAllExperimentsForProject(project);
        }
        return ExperimentSearchByProjectCriteria.createAllExperimentsForAllProjects();
    }

    private static void openImagingMaterialGenericViewer(
            final IEntityInformationHolderWithPermId material,
            final ExperimentSearchCriteria experimentCriteriaOrNull,
            final AnalysisProcedureCriteria analysisProcedureCriteria,
            final IViewContext<IScreeningClientServiceAsync> viewContext)
    {
        AbstractTabItemFactory tab =
                createImagingMaterialViewerTabFactory(material, experimentCriteriaOrNull,
                        analysisProcedureCriteria, viewContext);
        DispatcherHelper.dispatchNaviEvent(tab);
    }

    private static MaterialIdentifier asMaterialIdentifier(
            IEntityInformationHolderWithPermId material)
    {
        return new MaterialIdentifier(material.getCode(), material.getEntityType().getCode());
    }

    private static final AbstractTabItemFactory createImagingMaterialViewerTabFactory(
            final IEntityInformationHolderWithPermId material,
            final ExperimentSearchCriteria experimentCriteriaOrNull,
            final AnalysisProcedureCriteria analysisProcedureCriteria,
            final IViewContext<IScreeningClientServiceAsync> viewContext)
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    TechId materialTechId = TechId.create(material);
                    final DatabaseModificationAwareComponent viewer =
                            ImagingMaterialViewer.create(viewContext, materialTechId,
                                    experimentCriteriaOrNull, analysisProcedureCriteria);
                    return createViewerTab(viewer, getTabTitle(), viewContext);
                }

                @Override
                public String getId()
                {
                    return GenericMaterialViewer.createId(TechId.create(material));
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return HelpPageIdentifier.createSpecific("Well Content Material Viewer");
                }

                @Override
                public String getTabTitle()
                {
                    return getViewerTitle(ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict.MATERIAL, material, viewContext);
                }

                @Override
                public String tryGetLink()
                {
                    return LinkExtractor.tryExtract(material);
                }
            };
    }

    private final class ImageDataSetViewerPlugin extends DelegatedClientPlugin<DataSetType>
    {
        private final ScreeningViewContext screeningViewContext;

        private ImageDataSetViewerPlugin(ScreeningViewContext viewContext)
        {
            super(viewContext, EntityKind.DATA_SET);
            this.screeningViewContext = viewContext;
        }

        private AbstractTabItemFactory createImageDataSetViewer(
                final IEntityInformationHolderWithPermId entity, final WellLocation wellLocation)
        {
            return new AbstractTabItemFactory()
                {
                    @Override
                    public ITabItem create()
                    {
                        final DatabaseModificationAwareComponent viewer =
                                ImageDataSetViewer.create(screeningViewContext, entity,
                                        wellLocation);
                        return createViewerTab(viewer, getTabTitle(), screeningViewContext);
                    }

                    @Override
                    public String getId()
                    {
                        final TechId dataSetId = TechId.create(entity);
                        return GenericDataSetViewer.createId(dataSetId);
                    }

                    @Override
                    public HelpPageIdentifier getHelpPageIdentifier()
                    {
                        return HelpPageIdentifier.createSpecific("Plate Well Viewer");
                    }

                    @Override
                    public String getTabTitle()
                    {
                        return getViewerTitle(Dict.WELL, entity, screeningViewContext);
                    }

                    @Override
                    public String tryGetLink()
                    {
                        return LinkExtractor.tryExtract(entity);
                    }
                };
        }

        @Override
        public final AbstractTabItemFactory createEntityViewer(
                final IEntityInformationHolderWithPermId entity)
        {
            String datasetTypeCode = entity.getEntityType().getCode();
            if (entity.getPermId().contains(":"))
            {
                String permId = entity.getPermId();
                return createImageDataSetViewer(entity,
                        WellLocation.parseLocationStr(permId.substring(permId.indexOf(':') + 1)));
            } else if (datasetTypeCode
                    .matches(ScreeningConstants.ANY_HCS_IMAGE_DATASET_TYPE_PATTERN))
            {
                return createHCSImageDatasetTabItemFactory(entity);
            } else if (datasetTypeCode
                    .matches(ScreeningConstants.ANY_MICROSCOPY_IMAGE_DATASET_TYPE_PATTERN))
            {
                return createMicroscopyImageDatasetTabItemFactory(entity);
            } else
            {
                throw new IllegalStateException("Unknown dataset type " + datasetTypeCode);
            }
        }

        private AbstractTabItemFactory createMicroscopyImageDatasetTabItemFactory(
                final IEntityInformationHolderWithPermId entity)
        {
            return new AbstractTabItemFactory()
                {
                    @Override
                    public ITabItem create()
                    {
                        final DatabaseModificationAwareComponent viewer =
                                MicroscopyDatasetViewer.create(screeningViewContext, entity);
                        return createViewerTab(viewer, getTabTitle(), screeningViewContext);
                    }

                    @Override
                    public String getId()
                    {
                        final TechId id = TechId.create(entity);
                        return GenericDataSetViewer.createId(id);
                    }

                    @Override
                    public HelpPageIdentifier getHelpPageIdentifier()
                    {
                        return HelpPageIdentifier.createSpecific("Microscopy Dataset Viewer");
                    }

                    @Override
                    public String getTabTitle()
                    {
                        return getViewerTitle(ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict.DATA_SET, entity,
                                screeningViewContext);
                    }

                    @Override
                    public String tryGetLink()
                    {
                        return LinkExtractor.tryExtract(entity);
                    }
                };
        }

        private AbstractTabItemFactory createHCSImageDatasetTabItemFactory(
                final IEntityInformationHolderWithPermId entity)
        {
            return new AbstractTabItemFactory()
                {
                    @Override
                    public ITabItem create()
                    {
                        final DatabaseModificationAwareComponent viewer =
                                PlateDatasetViewer.create(screeningViewContext, entity);
                        return createViewerTab(viewer, getTabTitle(), screeningViewContext);
                    }

                    @Override
                    public String getId()
                    {
                        final TechId sampleId = TechId.create(entity);
                        return GenericDataSetViewer.createId(sampleId);
                    }

                    @Override
                    public HelpPageIdentifier getHelpPageIdentifier()
                    {
                        return HelpPageIdentifier.createSpecific("Plate Dataset Viewer");
                    }

                    @Override
                    public String getTabTitle()
                    {
                        return getViewerTitle(ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict.DATA_SET, entity,
                                screeningViewContext);
                    }

                    @Override
                    public String tryGetLink()
                    {
                        return LinkExtractor.tryExtract(entity);
                    }
                };
        }
    }

    private final class SampleClientPlugin extends DelegatedClientPlugin<SampleType>
    {
        private final ScreeningViewContext screeningViewContext;

        private SampleClientPlugin(ScreeningViewContext viewContext)
        {
            super(viewContext, EntityKind.SAMPLE);
            this.screeningViewContext = viewContext;
        }

        @Override
        public final AbstractTabItemFactory createEntityViewer(
                final IEntityInformationHolderWithPermId entity)
        {
            String sampleTypeCode = entity.getEntityType().getCode();
            if (sampleTypeCode.matches(ScreeningConstants.HCS_PLATE_SAMPLE_TYPE_PATTERN))
            {
                if (entity.getPermId().contains(":"))
                {
                    String permId = entity.getPermId();
                    return createImageSampleViewer(entity, WellLocation.parseLocationStr(permId
                            .substring(permId.indexOf(':') + 1)), false);
                } else
                {
                    return createPlateViewer(entity);
                }
            } else if (sampleTypeCode.equals(ScreeningConstants.LIBRARY_PLUGIN_TYPE_CODE))
            {
                throw new UserFailureException("Cannot browse objects of the "
                        + ScreeningConstants.LIBRARY_PLUGIN_TYPE_CODE + " type.");
            } else if (sampleTypeCode
                    .matches(ScreeningConstants.MICROSCOPY_IMAGE_SAMPLE_TYPE_PATTERN))
            {
                return createImageSampleViewer(entity, null, false);
            } else
            // well sample
            {
                return createImageSampleViewer(entity, null, true);
            }
        }

        private AbstractTabItemFactory createImageSampleViewer(
                final IEntityInformationHolderWithPermId entity,
                final WellLocation wellLocationOrNull, final boolean isWellSample)
        {
            return new AbstractTabItemFactory()
                {
                    @Override
                    public ITabItem create()
                    {
                        final DatabaseModificationAwareComponent viewer =
                                ImageSampleViewer.create(screeningViewContext, entity,
                                        wellLocationOrNull, isWellSample);
                        return createViewerTab(viewer, getTabTitle(), screeningViewContext);
                    }

                    @Override
                    public String getId()
                    {
                        final TechId sampleId = TechId.create(entity);
                        return GenericSampleViewer.createId(sampleId)
                                + (wellLocationOrNull == null ? "" : ":"
                                        + wellLocationOrNull.toWellIdString());
                    }

                    @Override
                    public HelpPageIdentifier getHelpPageIdentifier()
                    {
                        return HelpPageIdentifier.createSpecific("Plate Well Viewer");
                    }

                    @Override
                    public String getTabTitle()
                    {
                        if (isWellSample || wellLocationOrNull != null)
                        {
                            ICodeHolder codeHolder = new ICodeHolder()
                                {
                                    @Override
                                    public String getCode()
                                    {
                                        if (wellLocationOrNull != null)
                                        {
                                            return entity.getCode() + ":"
                                                    + wellLocationOrNull.toWellIdString();
                                        } else
                                        {
                                            return entity.getCode();
                                        }
                                    }
                                };

                            return getViewerTitle(Dict.WELL, codeHolder, screeningViewContext);
                        } else
                        {
                            return getViewerTitle(ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict.SAMPLE, entity,
                                    screeningViewContext);
                        }
                    }

                    @Override
                    public String tryGetLink()
                    {
                        return LinkExtractor.tryExtract(entity);
                    }
                };
        }

        private final AbstractTabItemFactory createPlateViewer(
                final IEntityInformationHolderWithPermId entity)
        {
            return new AbstractTabItemFactory()
                {
                    @Override
                    public ITabItem create()
                    {
                        final DatabaseModificationAwareComponent viewer =
                                PlateSampleViewer.create(screeningViewContext, entity);
                        return createViewerTab(viewer, getTabTitle(), screeningViewContext);
                    }

                    @Override
                    public String getId()
                    {
                        final TechId sampleId = TechId.create(entity);
                        return GenericSampleViewer.createId(sampleId);
                    }

                    @Override
                    public HelpPageIdentifier getHelpPageIdentifier()
                    {
                        return HelpPageIdentifier.createSpecific("Plate Sample Viewer");
                    }

                    @Override
                    public String getTabTitle()
                    {
                        return getViewerTitle(Dict.PLATE, entity, screeningViewContext);
                    }

                    @Override
                    public String tryGetLink()
                    {
                        return LinkExtractor.tryExtract(entity);
                    }
                };
        }

        @Override
        public Widget createBatchRegistrationForEntityType(SampleType type)
        {
            if (type.getCode().equals(ScreeningConstants.LIBRARY_PLUGIN_TYPE_CODE))
            {
                return new LibrarySampleBatchRegistrationForm(getViewContext());
            } else
            {
                return super.createBatchRegistrationForEntityType(type);
            }
        }
    }

    private static ITabItem createViewerTab(DatabaseModificationAwareComponent viewer,
            String title, IViewContext<?> viewContext)
    {
        return DefaultTabItem.create(title, viewer, viewContext, false);
    }

    private static String getViewerTitle(String dictTitleKey, ICodeHolder codeProvider,
            IMessageProvider messageProvider)
    {
        return AbstractViewer.getTitle(messageProvider, dictTitleKey, codeProvider);
    }

    @Override
    protected IModule maybeCreateModule()
    {
        return new ScreeningModule(getViewContext());
    }
}
