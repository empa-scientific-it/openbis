/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.LinkExtractor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICellListenerAndLinkGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ClientPluginFactory;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningDisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.AnalysisProcedureChooser.IAnalysisProcedureSelectionListener;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.ChannelWidgetWithListener.ISimpleChanneledViewerFactory;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.IntensityRange;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.AnalysisProcedureCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteriaHolder;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.MaterialSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.grids.WellSearchGridColumnIds;

/**
 * @author Franz-Josef Elmer
 */
public class WellSearchGrid extends TypedTableGrid<WellContent> implements
        IAnalysisProcedureSelectionListener
{
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX
            + "PlateMaterialReviewer2Grid";

    private static final String CHANNEL_CHOOSER_LABEL = "Channel:";

    private static final int IMAGE_SIZE_PX = 80;

    // by experiment perm id
    public static void openTab(
            final IViewContext<IScreeningClientServiceAsync> screeningViewContext,
            final String experimentPermId, final MaterialSearchCriteria materialSearchCriteria,
            final AnalysisProcedureCriteria analysisProcedureCriteria,
            final boolean showCombinedResults, final String nothingFoundRedirectionUrlOrNull)
    {
        screeningViewContext.getCommonService().getEntityInformationHolder(EntityKind.EXPERIMENT,
                experimentPermId,
                new AbstractAsyncCallback<IEntityInformationHolderWithPermId>(screeningViewContext)
                    {
                        @Override
                        protected void process(
                                IEntityInformationHolderWithPermId experimentIdentifier)
                        {
                            TechId experimentId = new TechId(experimentIdentifier.getId());
                            WellSearchGrid.openTab(screeningViewContext, experimentId,
                                    materialSearchCriteria, analysisProcedureCriteria,
                                    showCombinedResults, nothingFoundRedirectionUrlOrNull);
                        }
                    });
    }

    // by experiment tech id
    private static void openTab(
            final IViewContext<IScreeningClientServiceAsync> screeningViewContext,
            TechId experimentId, final MaterialSearchCriteria materialSearchCriteria,
            final AnalysisProcedureCriteria analysisProcedureCriteria,
            final boolean showCombinedResults, final String nothingFoundRedirectionUrlOrNull)
    {
        screeningViewContext.getCommonService().getExperimentInfo(experimentId,
                new AbstractAsyncCallback<Experiment>(screeningViewContext)
                    {
                        @Override
                        protected void process(Experiment experiment)
                        {
                            ExperimentSearchCriteria experimentCriteria =
                                    ExperimentSearchCriteria.createExperiment(experiment);
                            WellSearchGrid.openTab(screeningViewContext, experimentCriteria,
                                    materialSearchCriteria, analysisProcedureCriteria,
                                    showCombinedResults, nothingFoundRedirectionUrlOrNull);
                        }
                    });
    }

    /** @param nothingFoundRedirectionUrlOrNull used only when showCombinedResults is false. */
    public static void openTab(final IViewContext<IScreeningClientServiceAsync> viewContext,
            final ExperimentSearchCriteria experimentCriteria,
            final MaterialSearchCriteria materialCriteria,
            final AnalysisProcedureCriteria analysisProcedureCriteria,
            final boolean showCombinedResults, String nothingFoundRedirectionUrlOrNull)
    {
        WellSearchCriteria searchCriteria =
                new WellSearchCriteria(experimentCriteria, materialCriteria,
                        analysisProcedureCriteria);
        if (showCombinedResults)
        {
            openWellSearchTab(viewContext, searchCriteria);
        } else
        {
            MaterialDisambiguationGrid.openTab(viewContext, searchCriteria,
                    nothingFoundRedirectionUrlOrNull);
        }
    }

    private static void openWellSearchTab(
            final IViewContext<IScreeningClientServiceAsync> viewContext,
            final WellSearchCriteria searchCriteria)
    {
        DispatcherHelper.dispatchNaviEvent(new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent grid =
                            WellSearchGrid.create(viewContext,
                                    searchCriteria.getExperimentCriteria(),
                                    searchCriteria.getMaterialSearchCriteria(),
                                    searchCriteria.getAnalysisProcedureCriteria());
                    return DefaultTabItem.create(getTabTitle(), grid, viewContext);
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return HelpPageIdentifier.createSpecific("Well Reviewing Panel");
                }

                @Override
                public String getId()
                {
                    final String reportDate =
                            DateTimeFormat.getFormat(PredefinedFormat.TIME_MEDIUM).format(
                                    new Date());
                    return GenericConstants.ID_PREFIX + "-PlateMaterialReviewer-" + reportDate;
                }

                @Override
                public String getTabTitle()
                {
                    return viewContext.getMessage(Dict.PLATE_MATERIAL_REVIEWER_TITLE);
                }

                @Override
                public String tryGetLink()
                {
                    return ScreeningLinkExtractor.tryCreateWellsSearchLink(searchCriteria, true);
                }
            });
    }

    public static IDisposableComponent create(
            IViewContext<IScreeningClientServiceAsync> viewContext,
            ExperimentSearchCriteria experimentCriteriaOrNull, TechId materialId,
            AnalysisProcedureCriteria analysisProcedureCriteria,
            boolean restrictGlobalScopeLinkToProject)
    {
        return create(viewContext, experimentCriteriaOrNull,
                MaterialSearchCriteria.createIdCriteria(materialId), analysisProcedureCriteria,
                restrictGlobalScopeLinkToProject);
    }

    @Deprecated
    // FIXME get rid of this method if possible
    private static IDisposableComponent create(
            IViewContext<IScreeningClientServiceAsync> viewContext,
            ExperimentSearchCriteria experimentCriteriaOrNull,
            MaterialSearchCriteria materialCriteria,
            AnalysisProcedureCriteria analysisProcedureCriteria)
    {
        return create(viewContext, experimentCriteriaOrNull, materialCriteria,
                analysisProcedureCriteria, false);
    }

    private static IDisposableComponent create(
            IViewContext<IScreeningClientServiceAsync> viewContext,
            ExperimentSearchCriteria experimentCriteriaOrNull,
            MaterialSearchCriteria materialCriteria,
            AnalysisProcedureCriteria analysisProcedureCriteria,
            boolean restrictGlobalScopeLinkToProject)
    {
        WellSearchGrid reviewer =
                new WellSearchGrid(viewContext, experimentCriteriaOrNull, materialCriteria,
                        analysisProcedureCriteria, restrictGlobalScopeLinkToProject);
        final ToolBar toolbar = reviewer.createToolbar();
        return reviewer.asDisposableWithToolbar(new IDisposableComponent()
            {
                @Override
                public void update(Set<DatabaseModificationKind> observedModifications)
                {
                }

                @Override
                public DatabaseModificationKind[] getRelevantModifications()
                {
                    return null;
                }

                @Override
                public Component getComponent()
                {
                    return toolbar;
                }

                @Override
                public void dispose()
                {
                }
            });
    }

    private final IViewContext<IScreeningClientServiceAsync> screeningViewContext;

    private final MaterialSearchCriteria materialCriteria;

    private final ExperimentSearchCriteriaHolder experimentCriteriaHolder;

    private final boolean restrictGlobalScopeLinkToProject;

    private ChannelChooserPanel channelChooser;

    // field value can change
    private AnalysisProcedureCriteria analysisProcedureCriteria;

    private WellSearchGrid(IViewContext<IScreeningClientServiceAsync> viewContext,
            ExperimentSearchCriteria experimentCriteriaOrNull,
            MaterialSearchCriteria materialCriteria,
            AnalysisProcedureCriteria analysisProcedureCriteria,
            boolean restrictGlobalScopeLinkToProject)
    {
        super(viewContext.getCommonViewContext(), BROWSER_ID, false,
                DisplayTypeIDGenerator.PLATE_MATERIAL_REVIEWER);
        this.screeningViewContext = viewContext;

        this.restrictGlobalScopeLinkToProject = restrictGlobalScopeLinkToProject;
        this.experimentCriteriaHolder =
                new ExperimentSearchCriteriaHolder(experimentCriteriaOrNull);
        this.materialCriteria = materialCriteria;
        this.analysisProcedureCriteria = analysisProcedureCriteria;

        final IDefaultChannelState defaultChannelState =
                createDefaultChannelState(viewContext, experimentCriteriaOrNull);
        channelChooser = new ChannelChooserPanel(screeningViewContext, defaultChannelState);
        linkExperiment();
        linkPlate();
        linkWell();
        linkImageDataSet();
        linkImageAnalysisDataSet();
    }

    private IViewContext<IScreeningClientServiceAsync> getViewContext()
    {
        return screeningViewContext;
    }

    private static IDefaultChannelState createDefaultChannelState(
            final IViewContext<IScreeningClientServiceAsync> viewContext,
            ExperimentSearchCriteria experimentCriteriaOrNull)
    {
        // If there is a single experiment set in criteria reuse default channel settings,
        // otherwise use global settings.
        ScreeningDisplayTypeIDGenerator displayTypeIdGenerator =
                ScreeningDisplayTypeIDGenerator.EXPERIMENT_CHANNEL;
        String displayTypeId;
        if (experimentCriteriaOrNull != null && experimentCriteriaOrNull.tryGetExperiment() != null)
        {
            String experimentPermId =
                    experimentCriteriaOrNull.tryGetExperiment().getExperimentPermId();
            displayTypeId = displayTypeIdGenerator.createID(experimentPermId);
        } else
        {
            displayTypeId = displayTypeIdGenerator.createID(null);
        }
        return new DefaultChannelState(viewContext, displayTypeId);
    }

    @Override
    protected ICellListenerAndLinkGenerator<WellContent> tryGetCellListenerAndLinkGenerator(
            String columnId)
    {
        final String wellMaterialPropertyTypeCode =
                WellSearchGridColumnIds.tryExtractWellMaterialPropertyCode(columnId);
        if (wellMaterialPropertyTypeCode != null)
        {
            ICellListenerAndLinkGenerator<WellContent> listenerLinkGenerator =
                    createMaterialLinkGenerator(wellMaterialPropertyTypeCode);
            registerLinkClickListenerFor(columnId, listenerLinkGenerator);
            return listenerLinkGenerator;
        } else
        {
            return super.tryGetCellListenerAndLinkGenerator(columnId);
        }
    }

    private ICellListenerAndLinkGenerator<WellContent> createMaterialLinkGenerator(
            final String wellMaterialPropertyTypeCode)
    {
        return new ICellListenerAndLinkGenerator<WellContent>()
            {
                @Override
                public String tryGetLink(WellContent wellContent, ISerializableComparable value)
                {
                    Material material = tryGetMaterial(wellContent, wellMaterialPropertyTypeCode);
                    if (material == null)
                    {
                        return null;
                    }
                    return tryCreateMaterialDetailsLink(wellContent, material);
                }

                @Override
                public void handle(TableModelRowWithObject<WellContent> row,
                        boolean specialKeyPressed)
                {
                    WellContent wellContent = row.getObjectOrNull();
                    if (wellContent == null)
                    {
                        return;
                    }
                    Material material = tryGetMaterial(wellContent, wellMaterialPropertyTypeCode);
                    if (material == null)
                    {
                        return;
                    }
                    openImagingMaterialViewer(wellContent, material);
                }

            };
    }

    private static Material tryGetMaterial(WellContent entity, String wellMaterialPropertyTypeCode)
    {
        for (IEntityProperty wellMaterialProperty : entity.getMaterialTypeProperties())
        {
            if (wellMaterialProperty.getPropertyType().getCode()
                    .equalsIgnoreCase(wellMaterialPropertyTypeCode))
            {
                return wellMaterialProperty.getMaterial();
            }
        }
        return null;
    }

    private static String tryCreateMaterialDetailsLink(WellContent wellContent, Material material)
    {
        return ScreeningLinkExtractor.createMaterialDetailsLink(material,
                getExperimentCriteria(wellContent));
    }

    private static ExperimentSearchCriteria getExperimentCriteria(WellContent wellContent)
    {
        IEntityInformationHolderWithIdentifier experiment = wellContent.getExperiment();
        return getExperimentCriteria(experiment);
    }

    private static ExperimentSearchCriteria getExperimentCriteria(
            IEntityInformationHolderWithIdentifier experiment)
    {
        return ExperimentSearchCriteria.createExperiment(experiment);
    }

    private void openImagingMaterialViewer(WellContent wellContent, Material material)
    {
        ClientPluginFactory.openImagingMaterialViewer(material, getExperimentCriteria(wellContent),
                analysisProcedureCriteria, false, getViewContext());
    }

    private void linkExperiment()
    {
        registerListenerAndLinkGenerator(WellSearchGridColumnIds.EXPERIMENT,
                new ICellListenerAndLinkGenerator<WellContent>()
                    {
                        @Override
                        public String tryGetLink(WellContent entity, ISerializableComparable value)
                        {
                            return LinkExtractor.tryExtract(entity.getExperiment());
                        }

                        @Override
                        public void handle(TableModelRowWithObject<WellContent> wellContent,
                                boolean specialKeyPressed)
                        {
                            showEntityViewer(wellContent.getObjectOrNull().getExperiment(),
                                    specialKeyPressed);
                        }
                    });
    }

    private void linkPlate()
    {
        registerListenerAndLinkGenerator(WellSearchGridColumnIds.PLATE,
                new ICellListenerAndLinkGenerator<WellContent>()
                    {
                        @Override
                        public String tryGetLink(WellContent entity, ISerializableComparable value)
                        {
                            return LinkExtractor.tryExtract(entity.getPlate());
                        }

                        @Override
                        public void handle(TableModelRowWithObject<WellContent> wellContent,
                                boolean specialKeyPressed)
                        {
                            showEntityViewer(wellContent.getObjectOrNull().getPlate(),
                                    specialKeyPressed);
                        }
                    });
    }

    private void linkWell()
    {
        registerListenerAndLinkGenerator(WellSearchGridColumnIds.WELL,
                new ICellListenerAndLinkGenerator<WellContent>()
                    {
                        @Override
                        public String tryGetLink(WellContent entity, ISerializableComparable value)
                        {
                            return LinkExtractor.tryExtract(entity.getWell());
                        }

                        @Override
                        public void handle(TableModelRowWithObject<WellContent> wellContent,
                                boolean specialKeyPressed)
                        {
                            showEntityViewer(wellContent.getObjectOrNull().getWell(),
                                    specialKeyPressed);
                        }
                    });
    }

    private void linkImageDataSet()
    {
        registerListenerAndLinkGenerator(WellSearchGridColumnIds.IMAGE_DATA_SET,
                new ICellListenerAndLinkGenerator<WellContent>()
                    {
                        @Override
                        public String tryGetLink(WellContent entity, ISerializableComparable value)
                        {
                            DatasetImagesReference imageDataset = entity.tryGetImageDataset();
                            if (imageDataset != null)
                            {
                                return LinkExtractor.tryExtract(imageDataset.getDatasetReference());
                            } else
                            {
                                return null;
                            }
                        }

                        @Override
                        public void handle(TableModelRowWithObject<WellContent> wellContent,
                                boolean specialKeyPressed)
                        {
                            DatasetImagesReference dataset =
                                    wellContent.getObjectOrNull().tryGetImageDataset();
                            if (dataset != null)
                            {
                                showEntityViewer(dataset.getDatasetReference(), specialKeyPressed);
                            }
                        }
                    });
    }

    private void linkImageAnalysisDataSet()
    {
        registerListenerAndLinkGenerator(WellSearchGridColumnIds.IMAGE_ANALYSIS_DATA_SET,
                new ICellListenerAndLinkGenerator<WellContent>()
                    {
                        @Override
                        public String tryGetLink(WellContent entity, ISerializableComparable value)
                        {
                            DatasetReference dataset = entity.tryGetFeatureVectorDataset();
                            if (dataset != null)
                            {
                                return LinkExtractor.tryExtract(dataset);
                            } else
                            {
                                return null;
                            }
                        }

                        @Override
                        public void handle(TableModelRowWithObject<WellContent> wellContent,
                                boolean specialKeyPressed)
                        {
                            DatasetReference dataset =
                                    wellContent.getObjectOrNull().tryGetFeatureVectorDataset();
                            if (dataset != null)
                            {
                                showEntityViewer(dataset, specialKeyPressed);
                            }
                        }
                    });
    }

    private void showEntityViewer(IEntityInformationHolderWithPermId entityOrNull,
            boolean specialKeyPressed)
    {
        if (entityOrNull != null)
        {
            new OpenEntityDetailsTabAction(entityOrNull, getViewContext(), specialKeyPressed)
                    .execute();
        }
    }

    private ToolBar createToolbar()
    {
        ToolBar toolbar = new ToolBar();
        toolbar.setEnableOverflow(false);
        AnalysisProcedureChooser analysisProcedureChooser = createAnalysisProcedureChooser();
        toolbar.add(createExperimentChooser(analysisProcedureChooser));
        toolbar.add(analysisProcedureChooser);
        toolbar.add(new Label(CHANNEL_CHOOSER_LABEL));
        toolbar.add(channelChooser);
        return toolbar;
    }

    private Component createExperimentChooser(AnalysisProcedureChooser analysisProcedureChooser)
    {
        IDelegatedAction experimentSelectionChangedAction =
                createExperimentSelectionChangedAction(analysisProcedureChooser);
        return new SingleOrAllExperimentsChooser(getViewContext(), experimentCriteriaHolder,
                restrictGlobalScopeLinkToProject, experimentSelectionChangedAction);
    }

    protected final IDelegatedAction createExperimentSelectionChangedAction(
            final AnalysisProcedureChooser analysisProcedureChooser)
    {
        return new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    analysisProcedureChooser.updateAnalysisProcedures();
                }
            };
    }

    private AnalysisProcedureChooser createAnalysisProcedureChooser()
    {
        AnalysisProcedureChooser analysisProcedureChooser =
                AnalysisProcedureChooser.createVertical(getViewContext(), experimentCriteriaHolder,
                        AnalysisProcedureCriteria.createAllProcedures(), this, true);
        return analysisProcedureChooser;
    }

    @Override
    protected ColumnDefsAndConfigs<TableModelRowWithObject<WellContent>> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<TableModelRowWithObject<WellContent>> columnDefs =
                super.createColumnsDefinition();
        setImageRenderer(columnDefs);
        return columnDefs;
    }

    @Override
    protected String translateColumnIdToDictionaryKey(String columnID)
    {
        return columnID;
    }

    private void setImageRenderer(ColumnDefsAndConfigs<TableModelRowWithObject<WellContent>> schema)
    {
        GridCellRenderer<BaseEntityModel<?>> render = new GridCellRenderer<BaseEntityModel<?>>()
            {

                @Override
                public Object render(BaseEntityModel<?> model, String property, ColumnData config,
                        int rowIndex, int colIndex, ListStore<BaseEntityModel<?>> store,
                        Grid<BaseEntityModel<?>> grid)
                {
                    final WellContent entity = getTableModel(model).getObjectOrNull();
                    if (entity == null)
                    {
                        return null;
                    }
                    DatasetImagesReference images = entity.tryGetImageDataset();
                    if (images == null)
                    {
                        return null;
                    }
                    final ISimpleChanneledViewerFactory viewerFactory =
                            new ISimpleChanneledViewerFactory()
                                {
                                    @Override
                                    public Widget create(List<String> channels,
                                            String imageTransformationCodeOrNull,
                                            Map<String, IntensityRange> rangesOrNull)
                                    {
                                        return WellContentDialog.createImageViewerForChannel(
                                                getViewContext(), entity, IMAGE_SIZE_PX, channels,
                                                imageTransformationCodeOrNull, rangesOrNull);
                                    }
                                };
                    ChannelWidgetWithListener widgetWithListener =
                            new ChannelWidgetWithListener(viewerFactory);
                    widgetWithListener.selectionChanged(channelChooser.getSelectedValues(),
                            channelChooser.tryGetSelectedTransformationCode(false),
                            channelChooser.tryGetSelectedIntensityRanges());

                    channelChooser.addSelectionChangedListener(widgetWithListener);

                    return widgetWithListener.asWidget();
                }

                @SuppressWarnings("unchecked")
                private TableModelRowWithObject<WellContent> getTableModel(BaseEntityModel<?> model)
                {
                    return ((TableModelRowWithObject<WellContent>) model.getBaseObject());
                }
            };
        schema.setGridCellRendererFor(WellSearchGridColumnIds.WELL_IMAGES, render);
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<WellContent>> resultSetConfig,
            final AbstractAsyncCallback<TypedTableResultSet<WellContent>> callback)
    {
        ExperimentSearchCriteria experimentCriteriaOrNull =
                experimentCriteriaHolder.tryGetCriteria();
        assert experimentCriteriaOrNull != null : "experiment not specified";

        WellSearchCriteria searchCriteria =
                new WellSearchCriteria(experimentCriteriaOrNull, materialCriteria,
                        analysisProcedureCriteria);
        getViewContext().getService().listPlateWells(resultSetConfig, searchCriteria,
                new AsyncCallback<TypedTableResultSet<WellContent>>()
                    {
                        @Override
                        public void onFailure(Throwable caught)
                        {
                            callback.onFailure(caught);
                        }

                        @Override
                        public void onSuccess(TypedTableResultSet<WellContent> result)
                        {
                            GridRowModels<TableModelRowWithObject<WellContent>> rows =
                                    result.getResultSet().getList();
                            for (GridRowModel<TableModelRowWithObject<WellContent>> row : rows)
                            {
                                WellContent wellContent = row.getOriginalObject().getObjectOrNull();
                                if (wellContent != null)
                                {

                                    DatasetImagesReference dataSet =
                                            wellContent.tryGetImageDataset();
                                    if (dataSet != null)
                                    {
                                        ImageDatasetParameters imageParameters =
                                                dataSet.getImageParameters();
                                        channelChooser.addChannelsForParameters(imageParameters);
                                    }
                                }
                            }
                            channelChooser.updateChannelSelection(null);
                            callback.onSuccess(result);
                        }
                    });
    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<TableModelRowWithObject<WellContent>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        getViewContext().getService().prepareExportPlateWells(exportCriteria, callback);
    }

    @Override
    protected List<String> getColumnIdsOfFilters()
    {
        return Arrays.asList(WellSearchGridColumnIds.PLATE, WellSearchGridColumnIds.WELL);
    }

    @Override
    public void analysisProcedureSelected(AnalysisProcedureCriteria selectedProcedureCriteria)
    {
        if (experimentCriteriaHolder.tryGetCriteria() != null)
        {
            this.analysisProcedureCriteria = selectedProcedureCriteria;
            refresh(true);
        }
    }

}
