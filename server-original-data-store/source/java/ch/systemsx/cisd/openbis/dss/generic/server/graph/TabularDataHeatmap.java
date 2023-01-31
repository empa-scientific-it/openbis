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

package ch.systemsx.cisd.openbis.dss.generic.server.graph;

import java.awt.Color;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.data.Range;
import org.jfree.data.general.Dataset;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

import ch.systemsx.cisd.hcs.Location;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ITabularData;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class TabularDataHeatmap extends AbstractTabularDataGraph<TabularDataHeatmapConfiguration>
{

    private static final int PAINT_SCALE_NUM_STEPS = 11;

    /**
     * @param configuration
     */
    public TabularDataHeatmap(TabularDataHeatmapConfiguration configuration,
            ITabularData fileLines, OutputStream out)
    {
        super(configuration, fileLines, out);
    }

    @Override
    protected XYZDataset tryCreateChartDataset()
    {
        // DUPLICATES LOGIC IN tryIterateOverFileLinesUsing
        int xColumn = tryXColumnNumber();
        int yColumn = tryYColumnNumber();
        int zColumn = tryColumnNumberForHeader(configuration.getZAxisColumn());
        // We could not find the necessary columns in the dataset
        if (xColumn < 0 || yColumn < 0 || zColumn < 0)
        {
            if (xColumn < 0)
            {
                logFailureToFindColumnHeader(configuration.getXAxisColumn());
            }
            if (yColumn < 0)
            {
                logFailureToFindColumnHeader(configuration.getYAxisColumn());
            }
            if (zColumn < 0)
            {
                logFailureToFindColumnHeader(configuration.getZAxisColumn());
            }
            return null;
        }

        // first parse the data into HeatmapElements
        HeatmapData data = parseData(xColumn, yColumn, zColumn);
        double[][] dataArray = convertHeatmapDataToArray(data);

        DefaultXYZDataset simpleDataset = new DefaultXYZDataset();
        simpleDataset.addSeries(getTitle(), dataArray);
        HeatmapDataset dataset = new HeatmapDataset(simpleDataset);
        dataset.setRange(new Range(data.minZ, data.maxZ));
        return dataset;
    }

    @Override
    protected JFreeChart createDataChart(Dataset dataset)
    {
        JFreeChart chart = createHeatmap(getTitle(), // title
                // don't use the use-provided label for the wells, just use a blank string
                "", // x-axis label
                "", // y-axis label
                (HeatmapDataset) dataset, // data
                PlotOrientation.HORIZONTAL, // plot orientation
                false, // create legend?
                false, // generate tooltips?
                false // generate URLs?
                );

        return chart;
    }

    private static JFreeChart createHeatmap(String title, String xAxisLabel, String yAxisLabel,
            HeatmapDataset dataset, PlotOrientation orientation, boolean legend, boolean tooltips,
            boolean urls)
    {
        if (orientation == null)
        {
            throw new IllegalArgumentException("Null 'orientation' argument.");
        }
        NumberAxis xAxis = new NumberAxis(xAxisLabel);
        xAxis.setTickUnit(new SpreadsheetColumnTickUnit(1.0));
        xAxis.setInverted(true);
        NumberAxis yAxis = new NumberAxis(yAxisLabel);

        XYBlockRenderer renderer = new XYBlockRenderer();
        renderer.setBlockAnchor(RectangleAnchor.CENTER);
        PaintScale paintScale = getPaintScale(dataset);
        renderer.setPaintScale(paintScale);

        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, null);
        plot.setOrientation(orientation);
        plot.setForegroundAlpha(1.f);
        plot.setRenderer(renderer);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.BLACK);
        plot.setAxisOffset(new RectangleInsets(5, 5, 5, 5));

        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, legend);
        ChartFactory.getChartTheme().apply(chart);

        NumberAxis scaleAxis = new NumberAxis("Scale");
        scaleAxis.setRange(dataset.getRange());
        scaleAxis.setStandardTickUnits(new TabularDataTickUnitSource());
        scaleAxis.setTickUnit(new TabularDataTickUnit(dataset.getRange().getLength()
                / PAINT_SCALE_NUM_STEPS));
        scaleAxis.setAutoRange(true);
        PaintScaleLegend psl = new PaintScaleLegend(paintScale, scaleAxis);
        psl.setMargin(new RectangleInsets(5, 5, 5, 5));
        psl.setPosition(RectangleEdge.RIGHT);
        psl.setAxisOffset(5.0);
        chart.addSubtitle(psl);

        return chart;
    }

    /**
     * Create a LookupPaintScale based on the <a href="http://colorbrewer.org/">Color Brewer</a> RdBu color scheme.
     */
    private static PaintScale getPaintScale(HeatmapDataset dataset)
    {
        // Use the Color Brewer RdBu color scheme with 11 steps
        Range range = dataset.getRange();
        double lowerBound = range.getLowerBound();
        double upperBound = range.getUpperBound();

        // Handle the degenerate case
        if (lowerBound == upperBound)
        {
            LookupPaintScale paintScale =
                    new LookupPaintScale(lowerBound, lowerBound + 1, Color.WHITE);
            paintScale.add(lowerBound, new Color(247, 247, 247));
            return paintScale;
        }

        LookupPaintScale paintScale = new LookupPaintScale(lowerBound, upperBound, Color.WHITE);
        double binMin = range.getLowerBound();
        double binStep = range.getLength() / PAINT_SCALE_NUM_STEPS;
        // 1
        paintScale.add(binMin, new Color(5, 48, 97));
        // 2
        binMin += binStep;
        paintScale.add(binMin, new Color(33, 102, 172));
        // 3
        binMin += binStep;
        paintScale.add(binMin, new Color(67, 147, 195));
        // 4
        binMin += binStep;
        paintScale.add(binMin, new Color(146, 197, 222));
        // 5
        binMin += binStep;
        paintScale.add(binMin, new Color(209, 229, 240));
        // 6
        binMin += binStep;
        paintScale.add(binMin, new Color(247, 247, 247));
        // 7
        binMin += binStep;
        paintScale.add(binMin, new Color(253, 219, 199));
        // 8
        binMin += binStep;
        paintScale.add(binMin, new Color(244, 165, 130));
        // 9
        binMin += binStep;
        paintScale.add(binMin, new Color(214, 96, 77));
        // 10
        binMin += binStep;
        paintScale.add(binMin, new Color(178, 24, 43));
        // 11
        binMin += binStep;
        paintScale.add(binMin, new Color(103, 0, 31));
        return paintScale;
    }

    @Override
    protected void configureChart(JFreeChart chart, int imageWidth, int imageHeight)
    {
        super.configureChart(chart, imageWidth, imageHeight);
        PaintScaleLegend psl = (PaintScaleLegend) chart.getSubtitle(0);
        ValueAxis axis = psl.getAxis();
        configureAxisFonts(imageWidth, axis);
    }

    private HeatmapData parseData(int xColumn, int yColumn, int zColumn)
    {
        HeatmapData heatmapData = new HeatmapData();

        // Note what the max x and max y values are, so we can convert to an array
        heatmapData.maxX = 0;
        heatmapData.maxY = 0;
        boolean areZBoundsInitialized = false;
        List<String[]> lines = fileLines.getDataLines();
        for (String[] line : lines)
        {
            HeatmapElement element = new HeatmapElement();
            if (configuration.isXYSplit())
            {
                try
                {
                    element.x = (int) Float.parseFloat(line[xColumn]);
                } catch (NumberFormatException ex)
                {
                    // handle a case when X is alphanumeric
                    element.x =
                            Location.tryCreateLocationFromTransposedMatrixCoordinate(
                                    line[xColumn] + "1").getY();
                }
                element.y = (int) Float.parseFloat(line[yColumn]);
            } else
            {
                Location loc =
                        Location.tryCreateLocationFromTransposedMatrixCoordinate(line[xColumn]);
                // Transpose the x and y
                element.x = loc.getY();
                element.y = loc.getX();
            }

            element.z = parseDouble(line[zColumn]);

            // Update the x/y bounds of the heatmap.
            // We can assume that x and y are finite integers
            if (element.x > heatmapData.maxX)
            {
                heatmapData.maxX = element.x;
            }
            if (element.y > heatmapData.maxY)
            {
                heatmapData.maxY = element.y;
            }

            double zValue = element.z;
            // If the zValue is not finite
            if (false == isFinite(zValue))
            {

            } else
            {
                areZBoundsInitialized =
                        updateZMinMaxBounds(heatmapData, areZBoundsInitialized, zValue);
                heatmapData.elements.add(element);
            }
        }

        return heatmapData;
    }

    /**
     * Update the bounds of the heatmap data.
     * 
     * @param heatmapData The heatmap data to update
     * @param areZBoundsInitialized Have the Z-bounds been initialized yet? If not, they are initialized to finiteDouble
     * @param finiteDouble A double value which is not NaN and not +/- inf
     * @return Return the new value of areZBoundsInitialized
     */
    private boolean updateZMinMaxBounds(HeatmapData heatmapData, boolean areZBoundsInitialized,
            double finiteDouble)
    {
        // If bounds haven't been initialized yet, do it now
        if (false == areZBoundsInitialized)
        {
            heatmapData.minZ = finiteDouble;
            heatmapData.maxZ = finiteDouble;
            return true;
        }

        // Update the bounds
        if (finiteDouble < heatmapData.minZ)
        {
            heatmapData.minZ = finiteDouble;
        }
        if (finiteDouble > heatmapData.maxZ)
        {
            heatmapData.maxZ = finiteDouble;
        }
        return areZBoundsInitialized;
    }

    private double[][] convertHeatmapDataToArray(HeatmapData data)
    {
        double[][] dataArray = new double[3][];
        double[] xArray = new double[data.elements.size()];
        double[] yArray = new double[data.elements.size()];
        double[] zArray = new double[data.elements.size()];
        dataArray[0] = xArray;
        dataArray[1] = yArray;
        dataArray[2] = zArray;

        int i = 0;
        for (HeatmapElement elt : data.elements)
        {
            xArray[i] = elt.x;
            yArray[i] = elt.y;
            zArray[i++] = elt.z;
        }
        return dataArray;
    }

    private class HeatmapElement
    {
        private int x;

        private int y;

        private double z;
    }

    private class HeatmapData
    {
        private int maxX;

        private int maxY;

        private double minZ = 0;

        private double maxZ = 0;

        private final ArrayList<HeatmapElement> elements = new ArrayList<HeatmapElement>();
    }

}
