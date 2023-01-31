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
public class TabularDataHistogramConfiguration extends TabularDataGraphConfiguration
{

    private final int numberOfBins;

    /**
     * Constructor for a histogram configuration.
     * 
     * @param title The title for the graph
     * @param histogramColumn The data column used for the histogram
     * @param imageWidth The desired width of the resulting image
     * @param imageHeight The desired height of the resulting image
     * @param numberOfBins The number of bins in the histogram
     */
    protected TabularDataHistogramConfiguration(String title, CodeAndLabel histogramColumn,
            int imageWidth, int imageHeight, int numberOfBins)
    {
        super(GraphType.HISTOGRAM, title, histogramColumn, histogramColumn, imageWidth, imageHeight);
        assert numberOfBins > 0;
        this.numberOfBins = numberOfBins;
    }

    public int getNumberOfBins()
    {
        return numberOfBins;
    }

}
