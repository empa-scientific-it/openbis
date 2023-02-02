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

import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.util.MaterialConfigurationProvider;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class QiagenScreeningLibraryColumnExtractorTest extends AssertJUnit
{

    private static final String[] HEADER_TOKENS =
    { "barcode", "row", "col", "sirna", "productId", "productName", "reseqMrnas", "geneId",
            "symbol", "description" };

    private static final boolean STRICT_MATERIAL_CODES = false;

    private static final boolean RELAXED_MATERIAL_CODES = true;

    private QiagenScreeningLibraryColumnExtractor extractor;

    private MaterialConfigurationProvider oldProvider;

    public void setUp(boolean isRelaxedMaterialCodes)
    {
        oldProvider = MaterialConfigurationProvider.initializeForTesting(isRelaxedMaterialCodes);
        extractor =
                new QiagenScreeningLibraryColumnExtractor(HEADER_TOKENS,
                        MaterialConfigurationProvider.getInstance());
    }

    @AfterMethod
    public void tearDown()
    {
        MaterialConfigurationProvider.restoreFromTesting(oldProvider);
    }

    @Test
    public void testWellCode()
    {
        setUp(STRICT_MATERIAL_CODES);
        assertEquals("A1", extractor.getWellCode(getRow()));

        // Test the hypothetical situation that the col is 01 instead of 1
        // The well code should still be A1
        String[] row = getRow();
        row[2] = "01";
        assertEquals("A1", extractor.getWellCode(row));
    }

    @Test
    public void testStrictMaterialCodes()
    {
        setUp(STRICT_MATERIAL_CODES);
        assertEquals("BMP15", extractor.getGeneId(getRow()));
        assertEquals("A-a_5.B_c_C__D_d", extractor.getGeneId(getRowWithGeneId("A-a_5.B(c:C)/D%d")));
    }

    @Test
    public void testRelaxedMaterialCodes()
    {
        setUp(RELAXED_MATERIAL_CODES);
        assertEquals("BMP15", extractor.getGeneId(getRow()));
        assertEquals("A-a_5.B(c:C)/D%d", extractor.getGeneId(getRowWithGeneId("A-a_5.B(c:C)/D%d")));
    }

    private String[] getRow()
    {
        return getRowWithGeneId("BMP15");
    }

    private String[] getRowWithGeneId(String geneId)
    {
        String[] row =
        { "H001-1A", "A", "1", "TCCCGTATAAGTATGTTCCAA", "SI00077350", "Hs_BMP15_3",
                "NM_005448,9210", geneId, "bone morphogenetic protein 15" };
        return row;
    }

}
