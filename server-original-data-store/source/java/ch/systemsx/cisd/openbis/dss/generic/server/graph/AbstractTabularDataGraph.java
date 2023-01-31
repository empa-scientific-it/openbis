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
import java.awt.Font;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.Dataset;
import org.jfree.data.xy.DefaultXYDataset;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ITabularData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;

/**
 * Abstract superclass for the different kinds of graphs.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
abstract class AbstractTabularDataGraph<T extends TabularDataGraphConfiguration> implements
        ITabularDataGraph
{
    protected static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            AbstractTabularDataGraph.class);

    private static final int SMALL_TICK_LABEL_FONT_SIZE = 7;

    private static final int SMALL_LABEL_FONT_SIZE = 9;

    private static final int SMALL_TITLE_FONT_SIZE = 12;

    private static final int SMALL_FONT_TRANSITION_SIZE = 400;

    protected final T configuration;

    protected final ITabularData fileLines;

    protected final OutputStream out;

    /**
     * Parse the string as a double.
     * 
     * @return A finite value or NaN if the string could not be parsed.
     */
    protected final static double parseDouble(String string)
    {
        double value;
        try
        {
            value = Double.parseDouble(string);
        } catch (NumberFormatException e)
        {
            value = Double.NaN;
        }
        return value;
    }

    /**
     * Check if the double is a finite value (not <i>inf</i> and not <i>NaN</i>).
     * 
     * @return True if the double is finite
     */
    protected final static boolean isFinite(double v)
    {
        return false == Double.isInfinite(v) && false == Double.isNaN(v);
    }

    protected AbstractTabularDataGraph(T configuration, ITabularData fileLines, OutputStream out)
    {
        this.configuration = configuration;
        this.fileLines = fileLines;
        this.out = out;
    }

    // Public API
    /**
     * Create an image from the file lines and write it to the output stream.
     * 
     * @throws IOException
     */
    @Override
    public void generateImage() throws IOException
    {
        generateImage(configuration.getImageWidth(), configuration.getImageHeight());
    }

    /**
     * Create an image,overriding the width and height in the configuration from the file lines and write it to the output stream.
     * 
     * @param imageWidth The desired width of the image
     * @param imageHeight The desired height of the image
     * @throws IOException
     */
    @Override
    public void generateImage(int imageWidth, int imageHeight) throws IOException
    {
        JFreeChart chart = createChart(imageWidth, imageHeight);
        ChartUtilities.writeChartAsPNG(out, chart, imageWidth, imageHeight);
    }

    // For Subclasses
    /**
     * Return the configuration for this graph.
     */
    protected T getConfiguration()
    {
        return configuration;
    }

    /**
     * Get the title (delegates to the configuration).
     */
    protected String getTitle()
    {
        return configuration.getTitle();
    }

    /**
     * Returns x-axis label.
     */
    protected String getXAxisLabel()
    {
        return getColumnLabel(configuration.getXAxisColumn());
    }

    /**
     * Returns y-axis label.
     */
    protected String getYAxisLabel()
    {
        return getColumnLabel(configuration.getYAxisColumn());
    }

    /**
     * Maps specified column code onto a column label. Returns column code if mapping doesn't work.
     */
    protected String getColumnLabel(CodeAndLabel columnCode)
    {
        String label = columnCode.getLabel();
        if (label != null)
        {
            return label;
        }
        int columnNumber = tryColumnNumberForHeader(columnCode);
        return columnNumber < 0 ? columnCode.getCode() : fileLines.getHeaderLabels()[columnNumber];
    }

    /**
     * Return the column number for the X column or -1 if none was found
     */
    protected int tryXColumnNumber()
    {
        return tryColumnNumberForHeader(configuration.getXAxisColumn());
    }

    /**
     * Return the column number for the Y column or -1 if none was found
     */
    protected int tryYColumnNumber()
    {
        return tryColumnNumberForHeader(configuration.getYAxisColumn());
    }

    /**
     * Return the column number for the code of the column header or -1 if none was found.
     */
    protected int tryColumnNumberForHeader(CodeAndLabel columnHeaderCode)
    {
        String code = columnHeaderCode.getCode();
        String[] headers = fileLines.getHeaderCodes();
        int i = 0;
        for (String header : headers)
        {
            if (code.equals(header))
            {
                return i;
            }
            ++i;
        }

        return -1;
    }

    /**
     * Create a chart with an error message.
     */
    protected JFreeChart createErrorChart()
    {
        DefaultXYDataset dataset = new DefaultXYDataset();
        JFreeChart chart = ChartFactory.createXYAreaChart("", // title
                "", // x-axis label
                "", // y-axis label
                dataset, // data
                PlotOrientation.VERTICAL, // plot orientation
                false, // create legend?
                false, // generate tooltips?
                false // generate URLs?
                );

        // Set the background color
        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);

        // Configure the plot
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinesVisible(false);
        plot.setDomainCrosshairVisible(false);
        plot.setRangeCrosshairVisible(false);
        plot.setDomainZeroBaselineVisible(false);
        plot.setRangeZeroBaselineVisible(false);
        plot.setForegroundAlpha(0.f);
        plot.setOutlineVisible(false);
        plot.setDomainAxis(null);
        plot.setRangeAxis(null);

        return chart;
    }

    /**
     * Convert the data to a {@link Dataset}. If there were problems converting, create an error chart. If conversion was successful, create a chart
     * using the data set.
     */
    protected JFreeChart createChart(int imageWidth, int imageHeight)
    {
        Dataset dataset = tryCreateChartDataset();
        if (null == dataset)
        {
            return createErrorChart();
        }

        JFreeChart chart = createDataChart(dataset);
        configureChart(chart, imageWidth, imageHeight);
        return chart;
    }

    /**
     * Create a chart using the supplied dataset.
     */
    protected abstract JFreeChart createDataChart(Dataset dataset);

    /**
     * Try to convert the DatasetFileLines into a {@link Dataset}. Return null on failure.
     */
    protected abstract Dataset tryCreateChartDataset();

    /**
     * An interface used to abstract iterating and processing tabular data.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    protected interface ILineProcessor
    {
        public void processLine(String xString, String yString, int index);
    }

    /**
     * Try to iterate over the data file lines, passing the columns specified in the configuration into the line processor. Return false if I could
     * not iterate (e.g., because the file was not of the expected format); return true on success.
     */
    protected boolean tryIterateOverFileLinesUsing(ILineProcessor proc)
    {
        int xColumn = tryXColumnNumber();
        int yColumn = tryYColumnNumber();
        // We could not find the necessary columns in the dataset
        if (xColumn < 0 || yColumn < 0)
        {
            if (xColumn < 0)
            {
                logFailureToFindColumnHeader(configuration.getXAxisColumn());
            }
            if (yColumn < 0)
            {
                logFailureToFindColumnHeader(configuration.getYAxisColumn());
            }
            return false;
        }

        List<String[]> lines = fileLines.getDataLines();
        int i = 0;
        for (String[] line : lines)
        {
            proc.processLine(line[xColumn], line[yColumn], i++);
        }

        return true;
    }

    protected void logFailureToFindColumnHeader(CodeAndLabel codeAndLabel)
    {
        String[] headerCodes = fileLines.getHeaderCodes();
        operationLog.warn("Could not find column with header " + codeAndLabel + " among the "
                + headerCodes.length + " header(s): " + Arrays.toString(headerCodes));
    }

    protected void configureChart(JFreeChart chart, int imageWidth, int imageHeight)
    {
        // Set the background color
        chart.setBackgroundPaint(Color.WHITE);

        // Set the font size
        if (imageWidth < SMALL_FONT_TRANSITION_SIZE)
        {
            TextTitle title = chart.getTitle();
            Font oldFont = title.getFont();
            title.setFont(new Font(oldFont.getName(), oldFont.getStyle(), SMALL_TITLE_FONT_SIZE));
        }

        // Configure the plot
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setDomainCrosshairVisible(false);
        plot.setRangeCrosshairVisible(false);

        // Configure the domain axis
        ValueAxis axis = plot.getDomainAxis();
        axis.setStandardTickUnits(new TabularDataTickUnitSource());
        axis.setAutoRange(true);
        configureAxisFonts(imageWidth, axis);

        // Configure the range axis
        axis = plot.getRangeAxis();
        axis.setStandardTickUnits(new TabularDataTickUnitSource());
        axis.setAutoRange(true);
        configureAxisFonts(imageWidth, axis);
    }

    protected void configureAxisFonts(int imageWidth, ValueAxis axis)
    {
        if (imageWidth < SMALL_FONT_TRANSITION_SIZE)
        {
            Font oldFont = axis.getLabelFont();
            axis.setLabelFont(new Font(oldFont.getName(), oldFont.getStyle(), SMALL_LABEL_FONT_SIZE));
            oldFont = axis.getTickLabelFont();
            axis.setTickLabelFont(new Font(oldFont.getName(), oldFont.getStyle(),
                    SMALL_TICK_LABEL_FONT_SIZE));
        }
    }

    protected void setAxisLabelFontSize(Axis axis)
    {
        Font labelFont = axis.getLabelFont();
        axis.setLabelFont(cloneFontWithNewSize(labelFont, 10));
    }

    protected Font cloneFontWithNewSize(Font font, int newSize)
    {
        return new Font(font.getName(), font.getStyle(), newSize);
    }
}
