/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.validation;

import java.io.File;
import java.io.Reader;
import java.util.Properties;

import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;

/**
 * @author Franz-Josef Elmer
 */
public class DataSetValidatorTest extends AbstractFileSystemTestCase
{
    private static final String EXPECTED_DATA_SET_TYPE_KEY = "expected-data-set-type";

    private static final String EXPECTED_FILE_KEY = "expected-file";

    public static class MockValidator implements IDataSetValidator
    {
        private final String expectedDataSetType;

        private final String expectedFile;

        public MockValidator(Properties properties)
        {
            expectedDataSetType = properties.getProperty(EXPECTED_DATA_SET_TYPE_KEY);
            expectedFile = properties.getProperty(EXPECTED_FILE_KEY);
        }

        @Override
        public void assertValidDataSet(DataSetType dataSetType, File incomingDataSetFileOrFolder)
        {
            assertEquals(expectedDataSetType, dataSetType.getCode());
            assertEquals(expectedFile, incomingDataSetFileOrFolder.toString());
        }

        @Override
        public void assertValidDataSet(DataSetType dataSetType, Reader reader, String dataSourceName)
        {
        }
    }

    @Test
    public void testNoValidatorsDefined()
    {
        DataSetValidator dataSetValidator = new DataSetValidator(new Properties());
        dataSetValidator.assertValidDataSet(new DataSetType(), new File("."));
    }

    @Test
    public void testMissingDataSetTypeProperty()
    {
        Properties properties = new Properties();
        properties.setProperty(DataSetValidator.DATA_SET_VALIDATORS_KEY, "v");
        try
        {
            new DataSetValidator(properties);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Missing mandatory property: v.data-set-type", ex.getMessage());
        }
    }

    @Test
    public void testUnknownValidatorClass()
    {
        Properties properties = new Properties();
        properties.setProperty(DataSetValidator.DATA_SET_VALIDATORS_KEY, "v");
        properties.setProperty("v." + DataSetValidator.DATA_SET_TYPE_KEY, "MY-DATA-TYPE");
        properties.setProperty("v." + DataSetValidator.VALIDATOR_KEY, "blabla");
        try
        {
            new DataSetValidator(properties);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Error occured while creating data set validator 'v': "
                    + "java.lang.ClassNotFoundException: blabla", ex.getMessage());
        }
    }

    @Test
    public void testAssertValidDataSet()
    {
        DataSetType dataSetType1 = new DataSetType();
        dataSetType1.setCode("T1");
        Properties properties = new Properties();
        properties.setProperty(DataSetValidator.DATA_SET_VALIDATORS_KEY, "v");
        properties.setProperty("v." + DataSetValidator.DATA_SET_TYPE_KEY, dataSetType1.getCode());
        properties
                .setProperty("v." + DataSetValidator.VALIDATOR_KEY, MockValidator.class.getName());
        properties.setProperty("v." + EXPECTED_DATA_SET_TYPE_KEY, dataSetType1.getCode());
        File file = new File(".");
        properties.setProperty("v." + EXPECTED_FILE_KEY, file.toString());
        DataSetValidator validator = new DataSetValidator(properties);

        validator.assertValidDataSet(dataSetType1, file);
        validator.assertValidDataSet(new DataSetType(), file);
    }

    @Test
    public void testThatShowsWhichValidatorAndWhichColumnDefinitionAreInvalid()
    {
        DataSetType dataSetType1 = new DataSetType();
        dataSetType1.setCode("T1");
        Properties properties = new Properties();
        properties.setProperty(DataSetValidator.DATA_SET_VALIDATORS_KEY, "v");
        properties.setProperty("v." + DataSetValidator.DATA_SET_TYPE_KEY, dataSetType1.getCode());
        properties.setProperty("v." + DataSetValidatorForTSV.COLUMNS_KEY, "col");
        properties.setProperty("v.col." + DefaultValueValidatorFactory.VALUE_TYPE_KEY, "??");
        try
        {
            new DataSetValidator(properties);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Error occured while creating data set validator 'v': "
                    + ConfigurationFailureException.class.getName() + ": "
                    + "Couldn't create column definition 'col': " + "Invalid value-type: ??",
                    ex.getMessage());
        }
    }

    @Test
    public void testAssertValidDataSetWithTSVFile()
    {
        DataSetType dataSetType1 = new DataSetType();
        dataSetType1.setCode("T1");
        Properties properties = new Properties();
        properties.setProperty(DataSetValidator.DATA_SET_VALIDATORS_KEY, "v");
        properties.setProperty("v." + DataSetValidator.DATA_SET_TYPE_KEY, dataSetType1.getCode());
        properties.setProperty("v." + DataSetValidatorForTSV.PATH_PATTERNS_KEY, "*.txt");
        properties.setProperty("v." + DataSetValidatorForTSV.STRICT_ROW_SIZE_KEY, "false");
        properties.setProperty("v." + DataSetValidatorForTSV.COLUMNS_KEY, "c1,c2");
        properties.setProperty("v.c1." + DefaultValueValidatorFactory.VALUE_TYPE_KEY, "unique");
        properties.setProperty("v.c2." + DefaultValueValidatorFactory.VALUE_TYPE_KEY, "numeric");
        properties.setProperty("v.c2." + AbstractValidatorFactory.ALLOW_EMPTY_VALUES_KEY, "true");
        DataSetValidator validator = new DataSetValidator(properties);
        FileUtilities.writeToFile(new File(workingDirectory, "table.txt"), "ID\tValue\n" + "s1\n"
                + "s2\t42\n");

        validator.assertValidDataSet(dataSetType1, workingDirectory);
    }

    @Test
    public void testAssertValidDataSetWithInvalidTSVFile()
    {
        DataSetType dataSetType1 = new DataSetType();
        dataSetType1.setCode("T1");
        Properties properties = new Properties();
        properties.setProperty(DataSetValidator.DATA_SET_VALIDATORS_KEY, "v");
        properties.setProperty("v." + DataSetValidator.DATA_SET_TYPE_KEY, dataSetType1.getCode());
        properties.setProperty("v." + DataSetValidatorForTSV.PATH_PATTERNS_KEY, "*.txt");
        properties.setProperty("v." + DataSetValidatorForTSV.COLUMNS_KEY, "c1,c2");
        properties.setProperty("v.c1." + DefaultValueValidatorFactory.VALUE_TYPE_KEY, "unique");
        properties.setProperty("v.c2." + DefaultValueValidatorFactory.VALUE_TYPE_KEY, "numeric");
        properties.setProperty("v.c2." + AbstractValidatorFactory.ALLOW_EMPTY_VALUES_KEY, "true");
        DataSetValidator validator = new DataSetValidator(properties);
        FileUtilities.writeToFile(new File(workingDirectory, "table.txt"), "ID\tValue\n"
                + "s1\t42\n" + "s2\tabc\n");

        try
        {
            validator.assertValidDataSet(dataSetType1, workingDirectory);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            String msg = ex.getMessage();
            AssertionUtil.assertContains("Error in file", msg);
            AssertionUtil.assertContains("table.txt': 2. cell in line 3: Not a number: abc", msg);
        }
    }
}
