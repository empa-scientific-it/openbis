/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver;

import static ch.systemsx.cisd.etlserver.AbstractDataSetInfoExtractor.GROUP_CODE;
import static ch.systemsx.cisd.etlserver.DefaultDataSetInfoExtractor.INDEX_OF_GROUP_CODE;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Properties;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.parser.MandatoryPropertyMissingException;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * Test cases for the {@link DefaultDataSetInfoExtractor}.
 * 
 * @author Bernd Rinn
 */
public final class DefaultDataSetInfoExtractorTest extends CodeExtractortTestCase
{
    private static final File WORKING_DIRECTORY =
            new File("targets/unit-test-wd/DefaultDataSetInfoExtractorTest");

    @BeforeTest
    public void setUp()
    {
        LogInitializer.init();
        FileUtilities.deleteRecursively(WORKING_DIRECTORY);
        assertTrue(WORKING_DIRECTORY.mkdirs());
    }

    @Test
    public void testExtractContainedSampleIdentifier()
    {
        Properties properties = new Properties();
        properties.setProperty(GROUP_CODE, "my-group");
        final IDataSetInfoExtractor extractor = new DefaultDataSetInfoExtractor(properties);

        final DataSetInformation dsInfo1 =
                extractor.getDataSetInformation(new File("BOX_1&S_1"), null);
        final DataSetInformation dsInfo2 =
                extractor.getDataSetInformation(new File("BOX_1:S_1"), null);

        assertEquals("BOX_1:S_1", dsInfo1.getSampleIdentifier().getSampleCode());
        assertEquals("S_1", dsInfo1.getSampleIdentifier().getSampleSubCode());
        assertEquals("BOX_1:S_1", dsInfo2.getSampleIdentifier().getSampleCode());
        assertEquals("S_1", dsInfo2.getSampleIdentifier().getSampleSubCode());
    }

    @Test
    public void testExtractExperimentIdentifierWithoutGroup()
    {
        Properties properties = new Properties();
        properties.setProperty(INDEX_OF_EXPERIMENT_IDENTIFIER, "1");
        properties.setProperty(GROUP_CODE, "my-group");
        final IDataSetInfoExtractor extractor = new DefaultDataSetInfoExtractor(properties);

        final DataSetInformation dsInfo =
                extractor.getDataSetInformation(new File("bla.XY&123.bla"), null);

        assertEquals("MY-GROUP", dsInfo.getExperimentIdentifier().getSpaceCode());
        assertEquals("XY", dsInfo.getExperimentIdentifier().getProjectCode());
        assertEquals("123", dsInfo.getExperimentIdentifier().getExperimentCode());
        assertEquals(null, dsInfo.getSampleIdentifier());
    }

    @Test
    public void testExtractExperimentIdentifierWithGroup()
    {
        Properties properties = new Properties();
        properties.setProperty(INDEX_OF_EXPERIMENT_IDENTIFIER, "1");
        final IDataSetInfoExtractor extractor = new DefaultDataSetInfoExtractor(properties);

        final DataSetInformation dsInfo =
                extractor.getDataSetInformation(new File("bla.abc&xy&123.bla"), null);

        assertEquals("ABC", dsInfo.getExperimentIdentifier().getSpaceCode());
        assertEquals("XY", dsInfo.getExperimentIdentifier().getProjectCode());
        assertEquals("123", dsInfo.getExperimentIdentifier().getExperimentCode());
        assertEquals(null, dsInfo.getSampleIdentifier());
    }

    @Test
    public void testHappyCaseWithDefaultProperties()
    {
        final String barcode = "XYZ123";
        final IDataSetInfoExtractor extractor = new DefaultDataSetInfoExtractor(new Properties());

        final DataSetInformation dsInfo =
                extractor.getDataSetInformation(new File("bla.bla." + barcode), null);

        assertNull(dsInfo.getExperimentIdentifier());
        assertEquals(barcode, dsInfo.getSampleIdentifier().getSampleCode());
        assertEquals(0, dsInfo.getParentDataSetCodes().size());
        assertEquals(null, dsInfo.getProducerCode());
        assertEquals(null, dsInfo.getProductionDate());
    }

    @Test
    public void testHappyCaseWithProducerCodeAndProductionDate()
    {
        final Properties properties = new Properties();
        properties.setProperty(INDEX_OF_DATA_PRODUCER_CODE, "-2");
        properties.setProperty(INDEX_OF_DATA_PRODUCTION_DATE, "0");
        final IDataSetInfoExtractor extractor = new DefaultDataSetInfoExtractor(properties);
        final String producerCode = "M1";
        final String productionDate = "20070903181312";
        final String barcode = "XYZ123";

        final DataSetInformation dsInfo =
                extractor.getDataSetInformation(new File(productionDate + ".A.B." + producerCode
                        + "." + barcode), null);

        assertEquals(barcode, dsInfo.getSampleIdentifier().getSampleCode());
        assertEquals(producerCode, dsInfo.getProducerCode());
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        assertEquals(productionDate, dateFormat.format(dsInfo.getProductionDate()));
    }

    @Test
    // set group code as the global settings for all datasets
    public void testGroupCodeGlobal()
    {
        final Properties properties = new Properties();
        properties.setProperty(INDEX_OF_GROUP_CODE, "");
        String globalGroupCode = "g1";
        properties.setProperty(GROUP_CODE, globalGroupCode);

        final DataSetInformation dsInfo = extractDatasetInfo(properties, "sampleCode");

        assertEquals(globalGroupCode, dsInfo.getSpaceCode());
    }

    @Test
    // set group code as the local setting for one datasets. It should override the global setting.
    public void testGroupCodeDatasetSpecific()
    {
        final Properties properties = new Properties();
        properties.setProperty(INDEX_OF_GROUP_CODE, "-2");
        String globalGroupCode = "globalGroup";

        properties.setProperty(GROUP_CODE, globalGroupCode);

        String localGroupCode = "localGroup";
        String fileName =
                localGroupCode + AbstractDataSetInfoExtractor.DEFAULT_ENTITY_SEPARATOR
                        + "sampleCode";
        final DataSetInformation dsInfo = extractDatasetInfo(properties, fileName);

        assertEquals(localGroupCode, dsInfo.getSpaceCode());
    }

    @Test(expectedExceptions = UserFailureException.class)
    // missing group code for a datasets if it's required for each dataset causes failure
    public void testGroupCodeDatasetSpecificMissingFails()
    {
        final Properties properties = new Properties();
        properties.setProperty(INDEX_OF_GROUP_CODE, "-2");
        properties.setProperty(GROUP_CODE, "any");

        extractDatasetInfo(properties, "sampleCode");
    }

    private static DataSetInformation extractDatasetInfo(final Properties properties,
            String fileName)
    {
        final IDataSetInfoExtractor extractor = new DefaultDataSetInfoExtractor(properties);
        return extractor.getDataSetInformation(new File(fileName), null);
    }

    @Test
    public void testHappyCaseWithAllPropertiesSet()
    {
        final Properties properties = new Properties();
        final String separator = "=";
        properties.setProperty(ENTITY_SEPARATOR, separator);
        String subSeparator = "%";
        properties.setProperty(SUB_ENTITY_SEPARATOR, subSeparator);
        properties.setProperty(INDEX_OF_SAMPLE_CODE, "0");
        properties.setProperty(INDEX_OF_PARENT_DATA_SET_CODE, "1");
        properties.setProperty(INDEX_OF_DATA_PRODUCER_CODE, "-2");
        properties.setProperty(INDEX_OF_DATA_PRODUCTION_DATE, "-1");
        final String format = "yyyy-MM-dd";
        properties.setProperty(DATA_PRODUCTION_DATE_FORMAT, format);
        properties.setProperty(DATA_SET_PROPERTIES_FILE_NAME_KEY, "props.tsv");
        final IDataSetInfoExtractor extractor = new DefaultDataSetInfoExtractor(properties);
        final String producerCode = "M1";
        final String parentDataSet1 = "1234-8";
        final String parentDataSet2 = "3456-9";
        final String productionDate = "2007-09-03";
        final String barcode = "XYZ-123";
        File incoming =
                new File(WORKING_DIRECTORY, barcode + separator + parentDataSet1 + subSeparator
                        + parentDataSet2 + separator + "A" + separator + producerCode + separator
                        + productionDate);
        incoming.mkdir();
        FileUtilities.writeToFile(new File(incoming, "props.tsv"),
                "property\tvalue\np1\tv1\np2\tv2");
        final DataSetInformation dsInfo = extractor.getDataSetInformation(incoming, null);
        assertEquals(barcode, dsInfo.getSampleIdentifier().getSampleCode());
        assertEquals("[" + parentDataSet1 + ", " + parentDataSet2 + "]", dsInfo
                .getParentDataSetCodes().toString());
        assertEquals(producerCode, dsInfo.getProducerCode());
        final SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        assertEquals(productionDate, dateFormat.format(dsInfo.getProductionDate()));
        assertEquals("[NewProperty{property=p1,value=v1}, NewProperty{property=p2,value=v2}]",
                dsInfo.getDataSetProperties().toString());
    }

    @Test
    public void testHappyCaseWithDataSetPropertiesFileConfiguredButNotPresent()
    {
        final Properties properties = new Properties();
        final String separator = "=";
        properties.setProperty(DATA_SET_PROPERTIES_FILE_NAME_KEY, "props-unexistent.tsv");
        final IDataSetInfoExtractor extractor = new DefaultDataSetInfoExtractor(properties);
        final String producerCode = "M1";
        final String parentDataSetCode = "1234-8";
        final String productionDate = "2007-09-03";
        final String barcode = "XYZ-123";
        File incoming =
                new File(WORKING_DIRECTORY, barcode + separator + parentDataSetCode + separator
                        + "A" + separator + producerCode + separator + productionDate);
        incoming.mkdir();
        extractor.getDataSetInformation(incoming, null);
    }

    @Test
    public void testFailWithDataSetPropertiesFileWithoutHeader()
    {
        final Properties properties = new Properties();
        final String separator = "=";
        properties.setProperty(DATA_SET_PROPERTIES_FILE_NAME_KEY, "props.tsv");
        final IDataSetInfoExtractor extractor = new DefaultDataSetInfoExtractor(properties);
        final String producerCode = "M1";
        final String parentDataSetCode = "1234-8";
        final String productionDate = "2007-09-03";
        final String barcode = "XYZ-123";
        File incoming =
                new File(WORKING_DIRECTORY, barcode + separator + parentDataSetCode + separator
                        + "A" + separator + producerCode + separator + productionDate);
        incoming.mkdir();
        FileUtilities.writeToFile(new File(incoming, "props.tsv"), "p1\tv1\np2\tv2");
        boolean exceptionThrown = false;
        try
        {
            extractor.getDataSetInformation(incoming, null);
        } catch (MandatoryPropertyMissingException ex)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void testWrongProductionDateFormat()
    {
        final Properties properties = new Properties();
        properties.setProperty(INDEX_OF_DATA_PRODUCTION_DATE, "0");
        final IDataSetInfoExtractor extractor = new DefaultDataSetInfoExtractor(properties);
        try
        {
            extractor.getDataSetInformation(new File("blabla.XYZ-123"), null);
            fail("UserFailureException expected");
        } catch (final UserFailureException e)
        {
            assertEquals("Could not parse data production date 'blabla' "
                    + "because it violates the following format: yyyyMMddHHmmss", e.getMessage());
        }
    }

    @Test
    public void testIndexTooLarge()
    {
        final Properties properties = new Properties();
        properties.setProperty(INDEX_OF_SAMPLE_CODE, "1");
        final IDataSetInfoExtractor extractor = new DefaultDataSetInfoExtractor(properties);
        try
        {
            extractor.getDataSetInformation(new File("XYZ-123"), null);
            fail("UserFailureException expected");
        } catch (final UserFailureException e)
        {
            assertEquals("Invalid data set name 'XYZ-123'. We need 2 entities, separated by '.', "
                    + "but got only 1.", e.getMessage());
        }
    }

    @Test
    public void testIndexTooSmall()
    {
        final Properties properties = new Properties();
        properties.setProperty(ENTITY_SEPARATOR, "-");
        properties.setProperty(INDEX_OF_SAMPLE_CODE, "-3");
        final IDataSetInfoExtractor extractor = new DefaultDataSetInfoExtractor(properties);
        try
        {
            extractor.getDataSetInformation(new File("XYZ-123"), null);
            fail("UserFailureException expected");
        } catch (final UserFailureException e)
        {
            assertEquals("Invalid data set name 'XYZ-123'. We need 3 entities, separated by '-', "
                    + "but got only 2.", e.getMessage());
        }
    }

}
