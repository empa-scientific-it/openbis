/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.threev;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.io.File;
import java.util.Properties;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.etlserver.CodeExtractortTestCase;
import ch.systemsx.cisd.etlserver.IDataSetInfoExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * @author Franz-Josef Elmer
 */
public class DataSetInfoExtractorForImageAnalysisTest extends CodeExtractortTestCase
{
    private static final String INDICES_OF_PARENT_DATA_SET_CODE_ENTITIES =
            DataSetInfoExtractorForImageAnalysis.INDICES_OF_PARENT_DATA_SET_CODE_ENTITIES;

    @Test
    public void testHappyCaseWithOnlyMandatoryPorperties()
    {
        Properties properties = new Properties();
        properties.setProperty(INDICES_OF_PARENT_DATA_SET_CODE_ENTITIES, "1, 0");
        IDataSetInfoExtractor extractor = new DataSetInfoExtractorForImageAnalysis(properties);

        DataSetInformation dataSetInfo =
                extractor.getDataSetInformation(new File("alpha.42.beta"), null);
        assertEquals(1, dataSetInfo.getParentDataSetCodes().size());
        assertEquals("42.alpha", dataSetInfo.getParentDataSetCodes().iterator().next());
        assertEquals("beta", dataSetInfo.getSampleIdentifier().getSampleCode());
        assertEquals(null, dataSetInfo.getDataSetCode());
        assertEquals(null, dataSetInfo.getProducerCode());
        assertEquals(null, dataSetInfo.getProductionDate());
    }

    @Test
    public void testConstructorWithMissingMandatoryProperty()
    {
        try
        {
            new DataSetInfoExtractorForImageAnalysis(new Properties());
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException e)
        {
            String message = e.getMessage();
            assertEquals(
                    "Given key 'indices-of-parent-data-set-code-entities' not found in properties '[]'",
                    message);
        }
    }
}
