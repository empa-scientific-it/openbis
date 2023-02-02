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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.server.library_tools;

/**
 * Provides structured information from one row of the plate images analysis results.
 * 
 * @author Tomasz Pylak
 */
public class PlateImageAnalysisColumnExtractor extends AbstractColumnExtractor
{
    // ----- column names

    private final static String PLATE_NAME = "barcode";

    private final static String WELL_ROW = "row";

    private final static String WELL_COL = "col";

    private final static String[] EXPECTED_COLUMNS = new String[]
    { PLATE_NAME, WELL_ROW, WELL_COL };

    // -------------

    public PlateImageAnalysisColumnExtractor(String[] headerTokens)
    {
        super(headerTokens, EXPECTED_COLUMNS);
    }

    private String getPlateCode(String[] row)
    {
        return getCodeValue(row, PLATE_NAME);
    }

    private String getWellCol(String[] row)
    {
        return getValue(row, WELL_COL);
    }

    private String getWellRow(String[] row)
    {
        return getValue(row, WELL_ROW);
    }

    public PlateWellLocation getPlateWellLocation(String[] row)
    {
        return new PlateWellLocation(getPlateCode(row), getWellRow(row), getWellCol(row));
    }
}
