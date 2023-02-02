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

import java.util.List;

import ch.systemsx.cisd.common.geometry.ConversionUtils;
import ch.systemsx.cisd.common.geometry.Point;
import ch.systemsx.cisd.openbis.generic.shared.util.MaterialConfigurationProvider;

/**
 * Provides structured information from one row of the QIAGEN library.
 * 
 * @author Tomasz Pylak
 */
public class QiagenScreeningLibraryColumnExtractor extends AbstractColumnExtractor
{
    // ----- column names

    private final static String PLATE_NAME = "barcode";

    private final static String WELL_ROW = "row";

    private final static String WELL_COL = "col";

    // gene

    private final static String GENE_ID = "geneId";

    private final static String GENE_SYMBOL = "symbol";

    private final static String GENE_DESC = "description";

    // oligo

    private final static String RNA_SEQUENCE = "sirna";

    private final static String SIRNA_ID = "productId";

    private final static String[] EXPECTED_COLUMNS = new String[]
    { PLATE_NAME, WELL_ROW, WELL_COL, RNA_SEQUENCE, GENE_ID, GENE_SYMBOL, GENE_DESC, SIRNA_ID };

    private final MaterialConfigurationProvider materialConfig;

    // -------------

    public QiagenScreeningLibraryColumnExtractor(String[] headerTokens,
            MaterialConfigurationProvider materialConfig)
    {
        super(headerTokens, EXPECTED_COLUMNS);
        this.materialConfig = materialConfig;
    }

    public List<String> getAdditionalOligoPropertyNames()
    {
        return getUnknownColumnNames();
    }

    // ------------

    public String getPlateCode(String[] row)
    {
        return getCodeValue(row, PLATE_NAME);
    }

    public String getWellCode(String[] row)
    {
        String wellRow = getWellRow(row);
        String wellCol = getWellCol(row);

        // Canonicalize the well name
        Point loc = ConversionUtils.parseSpreadsheetLocation(wellRow + wellCol);
        return ConversionUtils.convertToSpreadsheetLocation(loc);
    }

    private String getWellCol(String[] row)
    {
        return getValue(row, WELL_COL);
    }

    private String getWellRow(String[] row)
    {
        return getValue(row, WELL_ROW);
    }

    public String getRNASequence(String[] row)
    {
        return getValue(row, RNA_SEQUENCE);
    }

    public String getSiRNAId(String[] row)
    {
        return getValue(row, SIRNA_ID);
    }

    public String getGeneId(String[] row)
    {
        return getMaterialCodeValue(row, GENE_ID);
    }

    private String getMaterialCodeValue(String[] row, String columnName)
    {
        if (materialConfig.isStrictCodeConstraints())
        {
            return getCodeValue(row, columnName);
        } else
        {
            return getValue(row, columnName);
        }
    }

    public String getGeneSymbol(String[] row)
    {
        return getValue(row, GENE_SYMBOL);
    }

    public String getGeneDescription(String[] row)
    {
        return getValue(row, GENE_DESC);
    }

    public List<String> getAdditionalOligoPropertyValues(String[] row, List<String> columnNames)
    {
        return getUnknownColumnValues(row, columnNames);
    }

    public PlateWellLocation getPlateWellLocation(String[] row)
    {
        return new PlateWellLocation(getPlateCode(row), getWellRow(row), getWellCol(row));
    }

    public GeneDetails getGeneDetails(String[] row)
    {
        return new GeneDetails(getGeneId(row), getGeneDescription(row));
    }

    public static class GeneDetails
    {
        private String symbol, description;

        public GeneDetails(String symbol, String description)
        {
            this.symbol = symbol;
            this.description = description;
        }

        public String getSymbol()
        {
            return symbol;
        }

        public void setSymbol(String symbol)
        {
            this.symbol = symbol;
        }

        public String getDescription()
        {
            return description;
        }

        public void setDescription(String description)
        {
            this.description = description;
        }
    }
}
