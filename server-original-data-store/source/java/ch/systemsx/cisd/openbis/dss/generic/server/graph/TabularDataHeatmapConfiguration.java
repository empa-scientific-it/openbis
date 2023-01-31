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

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class TabularDataHeatmapConfiguration extends TabularDataGraphConfiguration
{

    // the column that is used as the y-axis
    private final CodeAndLabel zAxisColumn;

    private final boolean isXYSplit;

    /**
     * Create a heatmap configuration where the x and y values are in separate columns.
     * 
     * @param title The title for the graph
     * @param xAxisColumn The data column used for the x-values
     * @param yAxisColumn The data column used for the y-values
     * @param zAxisColumn The data column used for the histogram
     * @param imageWidth The desired width of the resulting image
     * @param imageHeight The desired height of the resulting image
     */
    protected TabularDataHeatmapConfiguration(String title, CodeAndLabel xAxisColumn, CodeAndLabel yAxisColumn,
            CodeAndLabel zAxisColumn, int imageWidth, int imageHeight)
    {
        super(GraphType.HEATMAP, title, xAxisColumn, yAxisColumn, imageWidth, imageHeight);
        this.zAxisColumn = zAxisColumn;
        isXYSplit = true;
    }

    /**
     * Create a heatmap configuration where the x and y values are one column and needs to be parsed.
     * 
     * @param title The title for the graph
     * @param indexColumn The data column used for the x/y values
     * @param zAxisColumn The data column used for the histogram
     * @param imageWidth The desired width of the resulting image
     * @param imageHeight The desired height of the resulting image
     */
    protected TabularDataHeatmapConfiguration(String title, CodeAndLabel indexColumn, CodeAndLabel zAxisColumn,
            int imageWidth, int imageHeight)
    {
        super(GraphType.HEATMAP, title, indexColumn, indexColumn, imageWidth, imageHeight);
        this.zAxisColumn = zAxisColumn;
        isXYSplit = false;
    }

    protected CodeAndLabel getZAxisColumn()
    {
        return zAxisColumn;
    }

    protected boolean isXYSplit()
    {
        return isXYSplit;
    }

}
