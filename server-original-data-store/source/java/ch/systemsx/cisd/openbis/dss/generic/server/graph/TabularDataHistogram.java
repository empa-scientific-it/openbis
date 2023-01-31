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

import java.io.OutputStream;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.general.Dataset;
import org.jfree.data.statistics.HistogramDataset;

import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ITabularData;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class TabularDataHistogram extends
        AbstractTabularDataGraph<TabularDataHistogramConfiguration>
{

    /**
     * @param configuration
     */
    public TabularDataHistogram(TabularDataHistogramConfiguration configuration,
            ITabularData fileLines, OutputStream out)
    {
        super(configuration, fileLines, out);
    }

    @Override
    protected HistogramDataset tryCreateChartDataset()
    {
        final double[] series = new double[fileLines.getDataLines().size()];
        final int[] finiteValueCount = new int[1];
        finiteValueCount[0] = 0;

        boolean success = tryIterateOverFileLinesUsing(new ILineProcessor()
            {
                @Override
                public void processLine(String xString, String yString, int index)
                {
                    // The x and y should have the same value, since a histogram requires only one
                    // dimension, so it doesn't matter which one we take.
                    double v = parseDouble(xString);
                    series[index] = v;
                    if (isFinite(v))
                    {
                        finiteValueCount[0]++;
                    }
                }
            });

        if (false == success)
        {
            return null;
        }

        HistogramDataset dataset = new HistogramDataset();
        if (finiteValueCount[0] > 0)
        {
            dataset.addSeries(getTitle(), series, configuration.getNumberOfBins());
        }
        return dataset;
    }

    @Override
    protected JFreeChart createDataChart(Dataset dataset)
    {
        JFreeChart chart = ChartFactory.createHistogram(getTitle(), // title
                getXAxisLabel(), // x-axis label
                "Count", // y-axis label
                (HistogramDataset) dataset, // data
                PlotOrientation.VERTICAL, // plot orientation
                false, // create legend?
                false, // generate tooltips?
                false // generate URLs?
                );

        XYItemRenderer r = ((XYPlot) chart.getPlot()).getRenderer();
        if (r instanceof XYBarRenderer)
        {
            XYBarRenderer renderer = (XYBarRenderer) r;
            renderer.setShadowVisible(false);
            renderer.setBarPainter(new StandardXYBarPainter());
        }

        return chart;
    }
}
