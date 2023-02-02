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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps;

import static ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.PlateStyleSetter.WELL_SPACING_PX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.extjs.gxt.ui.client.widget.tips.ToolTip;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.IRealNumberRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.RealNumberRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.LabeledItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.SimpleModelComboBox;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.ToolTipAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningViewContext;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.PlateStyleSetter;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.WellContentDialog;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto.WellData;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.dto.Color;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.model.PlateLayouterModel;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.utils.GuiUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.PlateUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureList;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetEnrichedReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImages;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMetadata;

/**
 * Utilities to create plate visualization. Visualizes wells metadata and at most one image dataset.
 * 
 * @author Tomasz Pylak
 */
public class PlateLayouter
{
    /**
     * Visualization of one image dataset. Note that image overlays will not be shown.
     * 
     * @return widget with plate visualization - all the wells and possibility to browse images.
     */
    public static Widget createVisualization(PlateImages plateImages,
            ScreeningViewContext viewContext)
    {
        PlateLayouter plateLayouter =
                new PlateLayouter(viewContext, plateImages.getPlateMetadata());
        ImageDatasetEnrichedReference imageDataset =
                new ImageDatasetEnrichedReference(plateImages.getImagesDataset());
        plateLayouter.changeDisplayedImageDataset(imageDataset);
        return plateLayouter.getView();
    }

    // -------

    private static final String HEATMAP_KIND_CHOOSER_LABEL_MSG = "Choose heatmap kind:";

    private static final String FEATURE_LISTS_LABEL_MSG = "Choose features list:";

    private static final String METADATA_HEATMAP_KIND_MSG = "Metadata";

    private static final String FEATURE_HEATMAP_KIND_PREFIX_MSG = "Feature ";

    private static final int HEATMAP_KIND_COMBOBOX_CHOOSER_WIDTH_PX = 200;

    // ------- internal fixed state

    private final PlateLayouterModel model;

    private final HeatmapPresenter presenter;

    private final Widget view;

    private final SimpleModelComboBox<CodeAndLabel> heatmapKindChooser;

    private final Component[][] renderedWells;

    private SimpleModelComboBox<FeatureList> featureListsSelector;

    private final HashMap<String, FeatureVectorDataset> featureVectorDatasetCache;

    public PlateLayouter(ScreeningViewContext viewContext, PlateMetadata plateMetadata)
    {
        this.model =
                new PlateLayouterModel(plateMetadata,
                        viewContext.getTechnologySpecificDisplaySettingsManager());
        this.renderedWells = renderWells(model, viewContext, this);
        LayoutContainer legendContainer = new LayoutContainer();
        IRealNumberRenderer realNumberRenderer = createRealNumberRenderer(viewContext);
        this.presenter =
                new HeatmapPresenter(viewContext, model, realNumberRenderer, createViewManipulator(
                        viewContext, legendContainer));
        this.heatmapKindChooser = createHeatmapKindComboBox(presenter, viewContext);
        this.featureListsSelector =
                new SimpleModelComboBox<FeatureList>(viewContext, createFeaturesListsModel(null),
                        HEATMAP_KIND_COMBOBOX_CHOOSER_WIDTH_PX);
        this.featureListsSelector
                .addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<LabeledItem<FeatureList>>>()
                    {
                        @Override
                        public void selectionChanged(
                                SelectionChangedEvent<SimpleComboValue<LabeledItem<FeatureList>>> se)
                        {
                            FeatureList featureList = SimpleModelComboBox.getChosenItem(se);
                            if (featureList == null)
                            {
                                updateHeatmapKindComboBox(heatmapKindChooser,
                                        model.getAllFeatureNames());
                            } else
                            {
                                updateHeatmapKindComboBox(heatmapKindChooser,
                                        labelsOfFeatureList(featureList));
                            }
                        }
                    });

        this.featureVectorDatasetCache = new HashMap<String, FeatureVectorDataset>();

        this.view =
                renderView(renderedWells, heatmapKindChooser, featureListsSelector, legendContainer);

    }

    private IRealNumberRenderer createRealNumberRenderer(ScreeningViewContext viewContext)
    {
        return new RealNumberRenderer(viewContext.getDisplaySettingsManager()
                .getRealNumberFormatingParameters());
    }

    private HeatmapPresenter.IHeatmapViewManipulator createViewManipulator(
            final ScreeningViewContext viewContext, final LayoutContainer legendContainer)
    {
        return new HeatmapPresenter.IHeatmapViewManipulator()
            {
                // needed because well components are reused between datasets
                private Map<Component, ToolTipAction> toolTipActions =
                        new HashMap<Component, ToolTipAction>();

                @Override
                public void updateWellStyle(int rowIx, int colIx, Color backgroundColor)
                {
                    Component wellComponent = getWellComponent(rowIx, colIx);
                    PlateStyleSetter
                            .setBackgroudColor(wellComponent, backgroundColor.getHexColor());
                }

                @Override
                public void addEmptyTooltip(int rowIx, int colIx)
                {
                    Component wellComponent = getWellComponent(rowIx, colIx);
                    final String anchor =
                            (colIx < (renderedWells[0].length / 2)) ? "left" : "right";
                    final ToolTipConfig config = new ToolTipConfig("Loading...");
                    config.setMouseOffset(new int[]
                    { 0, 0 });
                    config.setAnchor(anchor);
                    config.setDismissDelay(10000); // 10s to hide
                    GWTUtils.setToolTip(wellComponent, config);
                }

                @Override
                public void updateTooltip(int rowIx, int colIx, String tooltipOrNull)
                {
                    hideAllToolTipsExcept(rowIx, colIx);
                    Component wellComponent = getWellComponent(rowIx, colIx);
                    if (tooltipOrNull != null)
                    {
                        String preparedText = GWTUtils.translateToHtmlLineBreaks(tooltipOrNull);
                        wellComponent.setToolTip(preparedText);
                    } else
                    {
                        wellComponent.hideToolTip();
                    }
                }

                @Override
                public void scheduleUpdateTooltip(final int rowIx, final int colIx,
                        IDelegatedAction refreshTooltipAction)
                {
                    Component wellComponent = getWellComponent(rowIx, colIx);
                    ToolTipAction toolTipAction = toolTipActions.get(wellComponent);
                    if (toolTipAction == null)
                    {
                        toolTipAction = new ToolTipAction(wellComponent);
                        toolTipAction.setAction(refreshTooltipAction);
                        toolTipActions.put(wellComponent, toolTipAction);
                    } else
                    {
                        toolTipAction.setAction(refreshTooltipAction);
                    }
                }

                @Override
                public void updateLegend(Widget legend)
                {
                    legendContainer.removeAll();
                    legendContainer.add(legend);
                    legendContainer.setAutoHeight(true);
                    legendContainer.layout();
                }

                private Component getWellComponent(int rowIx, int colIx)
                {
                    Component wellComponent = renderedWells[rowIx][colIx];
                    return wellComponent;
                }

                private void hideAllToolTipsExcept(int rowIx, int colIx)
                {
                    for (int row = 0; row < renderedWells.length; ++row)
                    {
                        for (int col = 0; col < renderedWells[row].length; ++col)
                        {
                            if (row == rowIx && col == colIx)
                            {
                                // Do not do anything
                            } else
                            {
                                renderedWells[row][col].hideToolTip();
                            }
                        }
                    }
                }
            };
    }

    /** @return widget for the plate layout */
    public Widget getView()
    {
        return view;
    }

    /** changes the image dataset from which images on the well detail view are displayed */
    public void changeDisplayedImageDataset(ImageDatasetEnrichedReference newImageDatasetOrNull)
    {
        this.model.setImageDataset(newImageDatasetOrNull);
    }

    /**
     * changes the feature vector dataset presented on the plate layout. If necessary fetches the dataset from the screening server
     */
    public void changeDisplayedFeatureVectorDataset(ScreeningViewContext context,
            DatasetReference reference)
    {
        String dataSetCode = reference.getCode();

        if (featureVectorDatasetCache.containsKey(dataSetCode))
        {
            changeDisplayedFeatureVectorDataset(featureVectorDatasetCache.get(dataSetCode));
        } else
        {
            context.getService().getFeatureVectorDataset(reference, null,
                    createLoadFeatureCallback(context, dataSetCode));
        }
    }

    private AsyncCallback<FeatureVectorDataset> createLoadFeatureCallback(
            ScreeningViewContext context, final String code)
    {
        return new AbstractAsyncCallback<FeatureVectorDataset>(context)
            {
                @Override
                protected void process(FeatureVectorDataset featureVector)
                {
                    featureVectorDatasetCache.put(code, featureVector);
                    changeDisplayedFeatureVectorDataset(featureVector);
                }
            };
    }

    /** changes the feature vector dataset presented on the plate layout */
    public void changeDisplayedFeatureVectorDataset(FeatureVectorDataset dataset)
    {
        this.model.setFeatureVectorDataset(dataset);
        updateFeaturesListsComboBox(featureListsSelector, model.getFeatureLists());

        updateHeatmapKindComboBox(heatmapKindChooser, labelsOfFeatureList(featureListsSelector
                .getValue().getValue().getItem()));
    }

    private List<CodeAndLabel> labelsOfFeatureList(FeatureList featureList)
    {
        Map<String, CodeAndLabel> map = new HashMap<String, CodeAndLabel>();
        for (CodeAndLabel codeAndLabel : model.getAllFeatureNames())
        {
            map.put(codeAndLabel.getLabel(), codeAndLabel);
        }
        List<CodeAndLabel> result = new ArrayList<CodeAndLabel>();
        for (String feature : featureList.getFeatures())
        {
            if (map.containsKey(feature))
            {
                result.add(map.get(feature));
            }
        }
        return result;
    }

    /**
     * Renders widget which visualizes the plate. The image dataset which is used to display images can be changed afterwards with
     * {@link #changeDisplayedImageDataset} method without re-rendering.
     */
    private static Widget renderView(Component[][] renderedWells,
            SimpleModelComboBox<CodeAndLabel> heatmapKindChooser,
            SimpleModelComboBox<FeatureList> featureListsSelector, LayoutContainer legendContainer)
    {
        LayoutContainer container = new LayoutContainer();
        container.setScrollMode(Scroll.AUTO);
        container.setLayout(new RowLayout());
        container.add(new Text(
                "Hold the mouse cursor over a well or click on it to get the details."),
                LayoutUtils.createRowLayoutHorizontalMargin());
        container.add(GuiUtils.renderInRow(new Text(HEATMAP_KIND_CHOOSER_LABEL_MSG),
                heatmapKindChooser, new Text(FEATURE_LISTS_LABEL_MSG), featureListsSelector));

        LayoutContainer plateContainer = new LayoutContainer();
        plateContainer.setLayout(new ColumnLayout());

        int legendWidth = 200;
        int plateWidth = getPlateMatrixPixelWidth(renderedWells);
        int totalWidth = plateWidth + legendWidth;
        plateContainer.setAutoHeight(true);
        plateContainer.setWidth(totalWidth);

        plateContainer.add(renderPlateLayout(renderedWells));

        // space between the well's matrix and the legend
        Widget separator = createBox();
        separator
                .setPixelSize(PlateStyleSetter.WELL_BOX_SIZE_PX, PlateStyleSetter.WELL_BOX_SIZE_PX);
        plateContainer.add(separator);
        plateContainer.add(legendContainer);

        container.add(plateContainer);
        container.setAutoHeight(true);
        container.setWidth(totalWidth);
        return container;
    }

    private static int getPlateMatrixPixelHeight(Component[][] renderedWells)
    {
        return getPlateMatrixPixelHeight(renderedWells.length + 1);
    }

    private static int getPlateMatrixPixelHeight(int numRows)
    {
        return WELL_SPACING_PX * (numRows + 1) + PlateStyleSetter.WELL_BOX_SIZE_PX * numRows;
    }

    private static int getPlateMatrixPixelWidth(Component[][] renderedWells)
    {
        int boxes = getColumnsNum(renderedWells) + 1;
        return Math.max(680, WELL_SPACING_PX * (boxes + 1) + PlateStyleSetter.WELL_BOX_SIZE_PX
                * boxes);
    }

    private static LayoutContainer renderPlateLayout(Component[][] renderedWells)
    {
        LayoutContainer plateMatrix = new LayoutContainer();
        int columnsNum = getColumnsNum(renderedWells) + 1;
        TableLayout layout = new TableLayout(columnsNum);
        layout.setCellSpacing(WELL_SPACING_PX);
        plateMatrix.setLayout(layout);

        plateMatrix.setAutoWidth(true);
        // NOTE: not sure if this is necessary
        int height = getPlateMatrixPixelHeight(renderedWells);
        plateMatrix.setHeight(height);

        addPlateWidgets(plateMatrix, renderedWells);
        return plateMatrix;
    }

    private static Component[][] renderWells(PlateLayouterModel model,
            ScreeningViewContext viewContext, PlateLayouter layouter)
    {
        WellData[][] wellMatrix = model.getWellMatrix();
        int rowsNum = wellMatrix.length;
        int colsNum = getColumnsNum(wellMatrix);

        Component[][] wells = new Component[rowsNum][colsNum];

        for (int row = 0; row < rowsNum; row++)
        {
            for (int col = 0; col < colsNum; col++)
            {
                WellData wellData = wellMatrix[row][col];
                wells[row][col] = createWellWidget(wellData, model, viewContext, layouter);
            }
        }
        return wells;
    }

    // renders wells and axis row/column descriptions
    private static void addPlateWidgets(LayoutContainer plateTable, Component[][] wellMatrix)
    {
        int rowsNum = wellMatrix.length;
        int colsNum = getColumnsNum(wellMatrix);
        for (int row = 0; row <= rowsNum; row++)
        {
            for (int col = 0; col <= colsNum; col++)
            {
                if (row != 0 && col != 0)
                {
                    plateTable.add(wellMatrix[row - 1][col - 1]);
                } else
                {
                    Widget labelWidget = tryCreateLabelWidget(row, col);
                    assert labelWidget != null : "Label widget is null";
                    plateTable.add(labelWidget);
                }
            }
        }
    }

    private static Component createWellBox(WellData wellData)
    {
        Component widget = createBox();
        return PlateStyleSetter.setWellStyle(widget);
    }

    // ----------- static methods

    private static <T> int getColumnsNum(T[][] matrix)
    {
        int rowsNum = matrix.length;
        return (rowsNum == 0 ? 0 : matrix[0].length);
    }

    // creates column or row label. Returns null if the coordinates do not point to the first column
    // or row.
    private static Component tryCreateLabelWidget(int row, int col)
    {
        String text = null;
        if (row == 0)
        {
            if (col == 0)
            {
                text = "";
            } else
            {
                text = "" + col;
            }
        } else if (col == 0)
        {
            text = PlateUtils.translateRowNumberIntoLetterCode(row);
        }
        if (text != null)
        {
            Component widget = new Text(text);
            return PlateStyleSetter.setWellLabelStyle(widget);
        } else
        {
            return null;
        }
    }

    private static Component createWellWidget(final WellData wellData,
            final PlateLayouterModel model, final ScreeningViewContext screeningViewContext,
            final PlateLayouter layouter)
    {
        Component widget = createWellBox(wellData);

        widget.addListener(Events.OnMouseDown, new Listener<BaseEvent>()
            {

                @Override
                public void handleEvent(BaseEvent ce)
                {
                    layouter.hideAllTooltops();
                    IScreeningClientServiceAsync service = screeningViewContext.getService();
                    ImageDatasetEnrichedReference dataset = model.tryGetImageDataset();
                    if (dataset == null)
                    {
                        WellContentDialog.showContentDialog(wellData, model.getPlateSample(), null,
                                screeningViewContext);
                    } else
                    {
                        // Reload meta data because they might be out dated especially when
                        // image transformer factory has changed. For the image URL the
                        // signature of the factory is needed to distinguish them. This is important
                        // because Web browser caches images.
                        DatasetImagesReference imageDataset = dataset.getImageDataset();
                        service.getImageDatasetReference(imageDataset.getDatasetCode(),
                                imageDataset.getDatastoreCode(),
                                new AbstractAsyncCallback<ImageDatasetEnrichedReference>(
                                        screeningViewContext)
                                    {
                                        @Override
                                        protected void process(
                                                ImageDatasetEnrichedReference refreshedDataset)
                                        {
                                            model.setImageDataset(refreshedDataset);
                                            WellContentDialog.showContentDialog(wellData,
                                                    layouter.model.getPlateSample(),
                                                    refreshedDataset, screeningViewContext);
                                        }
                                    });
                    }
                }
            });
        widget.sinkEvents(Events.OnMouseDown.getEventCode());
        return widget;
    }

    private void hideAllTooltops()
    {
        for (int row = 0; row < renderedWells.length; ++row)
        {
            for (int col = 0; col < renderedWells[row].length; ++col)
            {
                renderedWells[row][col].hideToolTip();
            }
        }
    }

    private static Component createBox()
    {
        return new Text("")
            {
                @Override
                // WORKAROUND to have an auto-width feature in ToolTips. See:
                // http://www.sencha.com/forum/archive/index.php/t-124754.html?s=656b4337e47b9419755bf0a6bc7409e9
                public void setToolTip(ToolTipConfig config)
                {
                    if (config != null)
                    {
                        if (toolTip == null)
                        {
                            toolTip = new ToolTip(this, config)
                                {
                                    @Override
                                    public void update(ToolTipConfig tooTipConfig)
                                    {
                                        super.update(tooTipConfig);
                                        if (isRendered() && isAttached())
                                        {
                                            hide();
                                            doAutoWidth();
                                            show();
                                        }
                                    }
                                };
                        }
                    }
                    super.setToolTip(config);
                }
            };
    }

    // ---------

    private static SimpleModelComboBox<CodeAndLabel> createHeatmapKindComboBox(
            final HeatmapPresenter presenter, IMessageProvider messageProvider)
    {
        List<LabeledItem<CodeAndLabel>> items = createHeatmapKindModel(null);
        final SimpleModelComboBox<CodeAndLabel> chooser =
                new SimpleModelComboBox<CodeAndLabel>(messageProvider, items,
                        HEATMAP_KIND_COMBOBOX_CHOOSER_WIDTH_PX);
        chooser.addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<LabeledItem<CodeAndLabel>>>()
            {
                @Override
                public void selectionChanged(
                        SelectionChangedEvent<SimpleComboValue<LabeledItem<CodeAndLabel>>> se)
                {
                    CodeAndLabel featureName = SimpleModelComboBox.getChosenItem(se);
                    if (featureName == null)
                    {
                        presenter.setWellMetadataMode();
                    } else
                    {
                        presenter.setFeatureValueMode(featureName);
                    }
                }
            });
        return chooser;
    }

    private static void updateHeatmapKindComboBox(SimpleModelComboBox<CodeAndLabel> chooser,
            List<CodeAndLabel> featureNames)
    {
        List<LabeledItem<CodeAndLabel>> items = createHeatmapKindModel(featureNames);
        chooser.removeAll();
        chooser.add(items);
        GWTUtils.autoselect(chooser, false);
    }

    private static List<LabeledItem<CodeAndLabel>> createHeatmapKindModel(
            List<CodeAndLabel> featureNamesOrNull)
    {
        List<LabeledItem<CodeAndLabel>> items = new ArrayList<LabeledItem<CodeAndLabel>>();
        items.add(new LabeledItem<CodeAndLabel>(null, METADATA_HEATMAP_KIND_MSG));
        if (featureNamesOrNull != null)
        {
            for (CodeAndLabel featureName : featureNamesOrNull)
            {
                String label = FEATURE_HEATMAP_KIND_PREFIX_MSG + featureName.getLabel();
                items.add(new LabeledItem<CodeAndLabel>(featureName, label));
            }
        }
        return items;
    }

    private static void updateFeaturesListsComboBox(SimpleModelComboBox<FeatureList> chooser,
            List<FeatureList> featureLists)
    {
        List<LabeledItem<FeatureList>> items = createFeaturesListsModel(featureLists);
        chooser.removeAll();
        chooser.add(items);
        GWTUtils.autoselect(chooser, false);
    }

    private static List<LabeledItem<FeatureList>> createFeaturesListsModel(
            List<FeatureList> featureListsOrNull)
    {
        List<LabeledItem<FeatureList>> items = new ArrayList<LabeledItem<FeatureList>>();
        items.add(new LabeledItem<FeatureList>(null, "All"));
        if (featureListsOrNull != null)
        {
            for (FeatureList featureList : featureListsOrNull)
            {
                String label = featureList.getName();
                items.add(new LabeledItem<FeatureList>(featureList, label));
            }
        }
        return items;
    }
}
