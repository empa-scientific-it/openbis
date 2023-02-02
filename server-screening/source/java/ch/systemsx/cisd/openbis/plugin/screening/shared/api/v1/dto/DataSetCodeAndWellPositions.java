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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for parsing strings containing data set code and well positions. Two forms are accepted:
 * 
 * <pre>
 *   &lt;data set code&gt;
 * </pre>
 * 
 * and
 * 
 * <pre>
 *   &lt;data set code&gt;:&lt;row 1&gt;.&lt;col 1&gt; &lt;row 2&gt;.&lt;col 2&gt; ...
 * </pre>
 *
 * @since 1.4
 * @author Franz-Josef Elmer
 */
public class DataSetCodeAndWellPositions
{
    private final String dataSetCode;

    private final List<WellPosition> wellPositions = new ArrayList<WellPosition>();

    /**
     * Creates an instane from the specified description.
     * 
     * @throws IllegalArgumentException in case of parsing error.
     */
    public DataSetCodeAndWellPositions(String dataSetCodeAndWellPositionsDescription)
    {
        int indexOfColon = dataSetCodeAndWellPositionsDescription.indexOf(':');
        if (indexOfColon < 0)
        {
            dataSetCode = dataSetCodeAndWellPositionsDescription;
        } else
        {
            dataSetCode = dataSetCodeAndWellPositionsDescription.substring(0, indexOfColon);
            wellPositions.addAll(WellPosition
                    .parseWellPositions(dataSetCodeAndWellPositionsDescription
                            .substring(indexOfColon + 1)));
        }
    }

    /**
     * Returns the data set code.
     */
    public final String getDataSetCode()
    {
        return dataSetCode;
    }

    /**
     * Returns the well positions. An empty list is returned in case of missing well position descriptions.
     */
    public final List<WellPosition> getWellPositions()
    {
        return wellPositions;
    }

}
