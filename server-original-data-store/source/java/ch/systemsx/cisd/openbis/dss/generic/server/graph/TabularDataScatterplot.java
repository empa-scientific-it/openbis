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

import java.awt.Shape;
import java.io.OutputStream;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.general.Dataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ITabularData;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class TabularDataScatterplot extends
        AbstractTabularDataGraph<TabularDataScatterplotConfiguration>
{

    /**
     * @param configuration
     */
    public TabularDataScatterplot(TabularDataScatterplotConfiguration configuration,
            ITabularData fileLines, OutputStream out)
    {
        super(configuration, fileLines, out);
    }

    @Override
    protected XYDataset tryCreateChartDataset()
    {
        final XYSeries series = new XYSeries(getTitle());

        boolean success = tryIterateOverFileLinesUsing(new ILineProcessor()
            {
                @Override
                public void processLine(String xString, String yString, int index)
                {
                    double x = parseDouble(xString);
                    double y = parseDouble(yString);
                    series.add(x, y);
                }
            });

        if (false == success)
        {
            return null;
        }

        XYSeriesCollection dataset = new XYSeriesCollection(series);
        return dataset;
    }

    @Override
    protected JFreeChart createDataChart(Dataset dataset)
    {
        JFreeChart chart = ChartFactory.createScatterPlot(getTitle(), // title
                getXAxisLabel(), // x-axis label
                getYAxisLabel(), // y-axis label
                (XYDataset) dataset, // data
                PlotOrientation.VERTICAL, // plot orientation
                false, // create legend?
                false, // generate tooltips?
                false // generate URLs?
                );

        return chart;
    }

    @Override
    protected void configureChart(JFreeChart chart, int imageWidth, int imageHeight)
    {
        super.configureChart(chart, imageWidth, imageHeight);

        XYPlot plot = (XYPlot) chart.getPlot();
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        float width = Math.max(1, imageWidth / 300);
        float height = width;
        Shape plotShape = new java.awt.geom.Ellipse2D.Float(0.f, 0.f, width, height);
        renderer.setSeriesShape(0, plotShape);
    }

}
