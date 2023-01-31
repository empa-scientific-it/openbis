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
import java.text.SimpleDateFormat;
import java.util.Properties;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.etlserver.CodeExtractortTestCase;
import ch.systemsx.cisd.etlserver.IDataSetInfoExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * @author Franz-Josef Elmer
 */
public class DataSetInfoExtractorForDataAcquisitionTest extends CodeExtractortTestCase
{
    private static final String INDICES_OF_DATA_SET_CODE_ENTITIES =
            DataSetInfoExtractorForDataAcquisition.INDICES_OF_DATA_SET_CODE_ENTITIES;

    private static final String DATA_SET_CODE_ENTITIES_GLUE =
            AbstractDataSetInfoExtractorFor3V.DATA_SET_CODE_ENTITIES_GLUE;

    @Test
    public void testHappyCaseWithOnlyMandatoryPorperties()
    {
        final Properties properties = new Properties();
        properties.setProperty(INDICES_OF_DATA_SET_CODE_ENTITIES, "1, 0");
        final IDataSetInfoExtractor extractor =
                new DataSetInfoExtractorForDataAcquisition(properties);

        final DataSetInformation dataSetInfo =
                extractor.getDataSetInformation(new File("alpha.42.beta"), null);
        assertEquals("42.alpha", dataSetInfo.getDataSetCode());
        assertEquals("beta", dataSetInfo.getSampleIdentifier().getSampleCode());
        assertEquals(0, dataSetInfo.getParentDataSetCodes().size());
        assertEquals(null, dataSetInfo.getProducerCode());
        assertEquals(null, dataSetInfo.getProductionDate());
    }

    @Test
    public void testHappyCaseWithAllProperties()
    {
        final Properties properties = new Properties();
        properties.setProperty(INDICES_OF_DATA_SET_CODE_ENTITIES, "-1 -2");
        properties.setProperty(DATA_SET_CODE_ENTITIES_GLUE, "-");
        properties.setProperty(ENTITY_SEPARATOR, "_");
        properties.setProperty(INDEX_OF_SAMPLE_CODE, "0");
        properties.setProperty(INDEX_OF_PARENT_DATA_SET_CODE, "1");
        properties.setProperty(INDEX_OF_DATA_PRODUCER_CODE, "2");
        properties.setProperty(INDEX_OF_DATA_PRODUCTION_DATE, "3");
        final String dateFormat = "yyyy-MM-dd";
        properties.setProperty(DATA_PRODUCTION_DATE_FORMAT, dateFormat);
        final IDataSetInfoExtractor extractor =
                new DataSetInfoExtractorForDataAcquisition(properties);

        final String date = "2007-12-24";
        final DataSetInformation dataSetInfo =
                extractor.getDataSetInformation(new File("a_b_c_" + date), null);
        assertEquals("2007-12-24-c", dataSetInfo.getDataSetCode());
        assertEquals("a", dataSetInfo.getSampleIdentifier().getSampleCode());
        assertEquals(1, dataSetInfo.getParentDataSetCodes().size());
        assertEquals("b", dataSetInfo.getParentDataSetCodes().iterator().next());
        assertEquals("c", dataSetInfo.getProducerCode());
        assertEquals(date, new SimpleDateFormat(dateFormat).format(dataSetInfo.getProductionDate()));
    }

    @Test
    public void testConstructorWithMissingMandatoryProperty()
    {
        try
        {
            new DataSetInfoExtractorForDataAcquisition(new Properties());
            fail("ConfigurationFailureException expected");
        } catch (final ConfigurationFailureException e)
        {
            final String message = e.getMessage();
            assertEquals(
                    "Given key 'indices-of-data-set-code-entities' not found in properties '[]'",
                    message);
        }
    }

    @Test
    public void testConstructorWithInvalidValuesForPropertyIndicesOfDataSetCodeEntities()
    {
        try
        {
            final Properties properties = new Properties();
            properties.setProperty(INDICES_OF_DATA_SET_CODE_ENTITIES, "2,u");
            new DataSetInfoExtractorForDataAcquisition(properties);
            fail("ConfigurationFailureException expected");
        } catch (final ConfigurationFailureException e)
        {
            assertEquals("2. index in property 'indices-of-data-set-code-entities' "
                    + "isn't a number: 2,u", e.getMessage());
        }

    }
}
