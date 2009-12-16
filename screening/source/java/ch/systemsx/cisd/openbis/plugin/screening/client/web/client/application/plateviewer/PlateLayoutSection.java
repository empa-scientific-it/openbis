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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.plateviewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.SingleSectionPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetReportGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedDatasetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.TileImage;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.TileImages;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellMetadata;

/**
 * A section of plate detail view which shows where the oligo and gene samples are located on the
 * plate and allow to check the content of the well quickly.
 * 
 * @author Tomasz Pylak
 */
public class PlateLayoutSection extends SingleSectionPanel
{
    private static final String CONTROL_MATERIAL_TYPE = "CONTROL";

    public static final String ID_SUFFIX = "PlateLayoutSection";

    public PlateLayoutSection(IViewContext<IScreeningClientServiceAsync> viewContext,
            TechId sampleId)
    {
        super("Plate Layout");
        add(new Text(viewContext.getMessage(Dict.LOAD_IN_PROGRESS)));
        viewContext.getService().getPlateContent(sampleId, createDisplayPlateCallback(viewContext));
        setDisplayID(DisplayTypeIDGenerator.SAMPLE_SECTION, ID_SUFFIX);
    }

    private AsyncCallback<PlateContent> createDisplayPlateCallback(final IViewContext<?> viewContext)
    {
        return new AbstractAsyncCallback<PlateContent>(viewContext)
            {
                @Override
                protected void process(PlateContent plateContent)
                {
                    removeAll();
                    setLayout(new RowLayout());
                    setScrollMode(Scroll.AUTO);

                    renderPlate(plateContent, viewContext);
                    addImageAnalysisButton(plateContent, viewContext);

                    layout();
                }
            };
    }

    private void addImageAnalysisButton(final PlateContent plateContent,
            final IViewContext<?> viewContext)
    {
        Component analysisPanel;
        final DatasetReference dataset = plateContent.tryGetImageAnalysisDataset();
        if (dataset != null)
        {
            Button generateButton =
                    new Button("Show Image Analysis Results", new SelectionListener<ButtonEvent>()
                        {
                            @Override
                            public void componentSelected(ButtonEvent ce)
                            {
                                generateImageAnalysisReport(viewContext, dataset, plateContent);
                            }
                        });
            analysisPanel = generateButton;
        } else
        {
            analysisPanel = new Text("No image analysis data are available.");
        }
        add(analysisPanel, createMarginLayoutData());
    }

    private void generateImageAnalysisReport(IViewContext<?> viewContext, DatasetReference dataset,
            PlateContent plateContent)
    {
        DatastoreServiceDescription service = createImageAnalysisReporter(dataset, plateContent);
        DisplayedOrSelectedDatasetCriteria criteria =
                DisplayedOrSelectedDatasetCriteria.createSelectedItems(Arrays.asList(dataset
                        .getCode()));
        DataSetReportGenerator.generate(service, criteria, viewContext.getCommonViewContext());
    }

    private DatastoreServiceDescription createImageAnalysisReporter(DatasetReference dataset,
            PlateContent plateContent)
    {
        String reportLabel = "Image Analysis of " + plateContent.getPlate().getCode();
        return new DatastoreServiceDescription(ScreeningConstants.PLATE_IMAGE_ANALYSIS_REPORT_KEY,
                reportLabel, new String[] {}, dataset.getDatastoreCode());
    }

    private void renderPlate(PlateContent plateContent, IViewContext<?> viewContext)
    {
        LayoutContainer container = new LayoutContainer();
        container.add(renderWellsMatrix(plateContent, viewContext));
        container.add(renderWellsLegend());

        add(container, createMarginLayoutData());
    }

    private RowData createMarginLayoutData()
    {
        RowData layoutData = new RowData();
        layoutData.setMargins(new Margins(10));
        return layoutData;
    }

    private LayoutContainer renderWellsLegend()
    {
        LayoutContainer legend = new LayoutContainer();
        legend.setLayout(new TableLayout(2));

        TableData mergedColumns = new TableData();
        mergedColumns.setColspan(2);

        Component verticalSeparator = createBox();
        verticalSeparator.setHeight("10");
        legend.add(verticalSeparator, mergedColumns);

        legend.add(new Text("Put a mouse on a well or click on it to get the details."),
                mergedColumns);
        legend.add(new Text("Legend:"), mergedColumns);

        legend.add(createNonEmptyWell(false));
        legend.add(new Text("Non-empty well"));

        legend.add(createNonEmptyWell(true));
        legend.add(new Text("Control well"));

        legend.add(createEmptyWellWidget());
        legend.add(new Text("Empty well"));

        return legend;
    }

    private LayoutContainer renderWellsMatrix(PlateContent plateContent, IViewContext<?> viewContext)
    {
        WellData[][] wellMatrix = createMatrix(plateContent);
        List<Widget> wellWidgets = createWellWidgets(wellMatrix, plateContent, viewContext);

        LayoutContainer plateMatrix = new LayoutContainer();
        TableLayout layout = new TableLayout(getColumnsNum(wellMatrix));
        layout.setCellSpacing(2);
        plateMatrix.setLayout(layout);
        for (Widget wellWidget : wellWidgets)
        {
            plateMatrix.add(wellWidget);
        }
        return plateMatrix;
    }

    private static List<Widget> createWellWidgets(WellData[][] wellMatrix,
            PlateContent plateContent, IViewContext<?> viewContext)
    {
        List<Widget> wellWidgets = new ArrayList<Widget>();
        int rowsNum = wellMatrix.length;
        int colsNum = getColumnsNum(wellMatrix);
        for (int row = 0; row < rowsNum; row++)
        {
            for (int col = 0; col < colsNum; col++)
            {
                Widget wellWidget = tryCreateLabelWidget(row, col);
                if (wellWidget == null)
                {
                    WellData wellData = wellMatrix[row][col];
                    if (wellData == null)
                    {
                        wellWidget = createEmptyWellWidget();
                    } else
                    {
                        wellWidget = createWellWidget(wellData, plateContent, viewContext);
                    }
                }
                wellWidgets.add(wellWidget);
            }
        }
        return wellWidgets;
    }

    private static int getColumnsNum(WellData[][] wellMatrix)
    {
        int rowsNum = wellMatrix.length;
        return (rowsNum == 0 ? 0 : wellMatrix[0].length);
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
            text = asWellLeter(row);
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

    // TODO 2009-12-07, Tomasz Pylak: use ConversionUtils.parseSpreadsheetLocation at server side
    private static String asWellLeter(int row)
    {
        return "" + (char) ('A' + row - 1);
    }

    private static int getColFromCode(String wellCode)
    {
        return new Integer(wellCode.substring(1));
    }

    private static int getRowFromCode(String wellCode)
    {
        return wellCode.toUpperCase().charAt(0) - 'A' + 1;
    }

    // ------ end todo

    private static Component createEmptyWellWidget()
    {
        Component widget = createBox();
        widget.setEnabled(false);
        return PlateStyleSetter.setEmptyWellStyle(widget);
    }

    private static Component createWellWidget(final WellData wellData,
            final PlateContent plateContent, final IViewContext<?> viewContext)
    {
        Component widget = createContentWell(wellData);
        widget.addListener(Events.OnMouseDown, new Listener<BaseEvent>()
            {
                public void handleEvent(BaseEvent ce)
                {
                    WellContentDialog.showContentDialog(wellData, viewContext);
                }
            });
        widget.sinkEvents(Events.OnMouseDown.getEventCode());
        setWellDescription(wellData, widget);
        return widget;
    }

    private static Component createContentWell(WellData wellData)
    {
        WellMetadata metadata = wellData.tryGetMetadata();
        if (metadata == null)
        {
            Component widget = createBox();
            // we may have images but at the same time no metadata
            return PlateStyleSetter.setEmptyWellStyle(widget);
        } else
        {
            boolean isControlWell = isControlWell(metadata);
            return createNonEmptyWell(isControlWell);
        }
    }

    private static Component createNonEmptyWell(boolean isControlWell)
    {
        Component widget = createBox();
        return setNonEmptyWellStyle(widget, isControlWell);
    }

    private static Component createBox()
    {
        return new Text("");
    }

    private static Component setNonEmptyWellStyle(Component widget, boolean isControlWell)
    {
        if (isControlWell)
        {
            return PlateStyleSetter.setControlWellStyle(widget);
        } else
        {
            return PlateStyleSetter.setNonControlWellStyle(widget);
        }
    }

    private static boolean isControlWell(WellMetadata metadata)
    {
        Material content = metadata.tryGetContent();
        return content != null && content.getEntityType().getCode().equals(CONTROL_MATERIAL_TYPE);
    }

    private static void setWellDescription(final WellData wellData, Component widget)
    {
        WellMetadata metadata = wellData.tryGetMetadata();
        if (metadata == null)
        {
            return;
        }
        Material content = metadata.tryGetContent();
        if (content != null)
        {
            String tooltip =
                    "Well: " + wellData.getWellSubcode() + ". Content: " + content.getIdentifier();
            Material gene = metadata.tryGetGene();
            if (gene != null)
            {
                tooltip += ". Inhibited gene: " + gene.getCode();
            }
            widget.setToolTip(tooltip);
        }
    }

    // Elements may contain null if well is empty.
    // Numbering starts with 1 so row and column with index 0 are left empty.
    private static WellData[][] createMatrix(PlateContent plateContent)
    {
        WellData[][] matrix =
                new WellData[plateContent.getRowsNum() + 1][plateContent.getColsNum() + 1];
        TileImages plateImages = plateContent.tryGetImages();
        if (plateImages != null)
        {
            addImagePaths(plateContent, matrix, plateImages.getImages());
        }
        addMetadata(plateContent, matrix, plateContent.getWells());
        return matrix;
    }

    private static void addMetadata(PlateContent plateContent, WellData[][] matrix,
            List<WellMetadata> wells)
    {
        for (WellMetadata well : wells)
        {
            String wellCode = well.getWellSample().getSubCode();
            int row = getRowFromCode(wellCode);
            int col = getColFromCode(wellCode);
            WellData wellData = matrix[row][col];
            if (wellData == null)
            {
                wellData = WellData.create(plateContent);
                matrix[row][col] = wellData;
            }
            wellData.setMetadata(well);
        }
    }

    private static void addImagePaths(PlateContent plateContent, WellData[][] matrix,
            List<TileImage> imagesList)
    {
        for (TileImage image : imagesList)
        {
            int row = image.getRow();
            int col = image.getColumn();
            WellData wellData = matrix[row][col];
            if (wellData == null)
            {
                wellData = WellData.create(plateContent);
                matrix[row][col] = wellData;
            }
            wellData.addImage(image);
        }
    }
}
