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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * @author Franz-Josef Elmer
 */
public class DataSetValidatorForTSVTest extends AbstractFileSystemTestCase
{
    private static final String MOCK_FACTORY = MockValidatorFactory.class.getName();

    private static final String EXPECTED_VALUES_KEY = "expected-values";

    private static final String NAME_KEY = "validator-name";

    public static final class MockValidatorFactory implements IValidatorFactory
    {
        private static final List<MockValidator> validators = new ArrayList<MockValidator>();

        static void clearValidators()
        {
            validators.clear();
        }

        static void assertSatisfied()
        {
            for (MockValidator validator : validators)
            {
                validator.assertSatisfied();
            }
            clearValidators();
        }

        private static final class MockValidator implements IValidator
        {
            private final String name;

            private final String[] expectedValues;

            private int index;

            MockValidator(Properties properties)
            {
                name = properties.getProperty(NAME_KEY, "unknown");
                String property = properties.getProperty(EXPECTED_VALUES_KEY);
                expectedValues = property == null ? new String[0] : property.split(",");
                validators.add(this);
            }

            @Override
            public void assertValid(String value)
            {
                assertTrue((index + 1) + ". value for validator '" + name + "' unexpected: "
                        + value, index < expectedValues.length);
                assertEquals((index + 1) + ". value for validator '" + name + "'",
                        expectedValues[index++], value);
            }

            void assertSatisfied()
            {
                assertEquals("Number of calls for validator '" + name + "'", expectedValues.length, index);
            }
        }

        private final IValidator validator;

        public MockValidatorFactory(Properties properties)
        {
            validator = new MockValidator(properties);
        }

        @Override
        public IValidator createValidator(String header)
        {
            return validator;
        }
    }

    @BeforeMethod
    public void init()
    {
        MockValidatorFactory.clearValidators();
    }

    @Test
    public void testPathPatterns()
    {
        Properties properties = new Properties();
        properties.setProperty(DataSetValidatorForTSV.PATH_PATTERNS_KEY, "*.txt, *.tsv");
        properties.setProperty(DataSetValidatorForTSV.COLUMNS_KEY, "c");
        properties.setProperty("c." + ColumnDefinition.VALUE_VALIDATOR_KEY, MOCK_FACTORY);
        properties.setProperty("c." + EXPECTED_VALUES_KEY, "1,2");
        DataSetValidatorForTSV validator = new DataSetValidatorForTSV(properties);

        FileUtilities.writeToFile(new File(workingDirectory, "a.txt"), "a\n1\n");
        FileUtilities.writeToFile(new File(workingDirectory, "b.tsv"), "b\n2\n");

        validator.assertValidDataSet(null, workingDirectory);

        MockValidatorFactory.assertSatisfied();
    }

    @Test
    public void testExcludePathPatterns()
    {
        Properties properties = new Properties();
        properties.setProperty(DataSetValidatorForTSV.EXCLUDE_PATH_PATTERNS_KEY, "*.tsv");
        properties.setProperty(DataSetValidatorForTSV.COLUMNS_KEY, "c");
        properties.setProperty("c." + ColumnDefinition.VALUE_VALIDATOR_KEY, MOCK_FACTORY);
        properties.setProperty("c." + EXPECTED_VALUES_KEY, "1");
        DataSetValidatorForTSV validator = new DataSetValidatorForTSV(properties);

        FileUtilities.writeToFile(new File(workingDirectory, "a.txt"), "a\n1\n");
        FileUtilities.writeToFile(new File(workingDirectory, "b.tsv"), "b\n2\n");

        validator.assertValidDataSet(null, workingDirectory);

        MockValidatorFactory.assertSatisfied();
    }

    @Test
    public void testColumnDefinitionCreationErrorMessage()
    {
        Properties properties = new Properties();
        properties.setProperty(DataSetValidatorForTSV.COLUMNS_KEY, "c1");
        properties.setProperty("c1." + ColumnDefinition.ORDER_KEY, "0");
        try
        {
            new DataSetValidatorForTSV(properties);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Couldn't create column definition 'c1': " +
                    "Order value has to be positive: 0", ex.getMessage());
        }
    }

    @Test
    public void testColumnOrderUsedTwice()
    {
        Properties properties = new Properties();
        properties.setProperty(DataSetValidatorForTSV.COLUMNS_KEY, "c1, c2");
        properties.setProperty("c1." + ColumnDefinition.ORDER_KEY, "2");
        properties.setProperty("c2." + ColumnDefinition.ORDER_KEY, "2");
        try
        {
            new DataSetValidatorForTSV(properties);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Couldn't create column definition 'c2': " +
                    "There is already a column definied for order 2.", ex.getMessage());
        }
    }

    @Test
    public void testNoColumnDefinitionFound()
    {
        Properties properties = new Properties();
        properties.setProperty(DataSetValidatorForTSV.PATH_PATTERNS_KEY, "*");
        properties.setProperty(DataSetValidatorForTSV.COLUMNS_KEY, "c1");
        properties.setProperty("c1." + ColumnDefinition.HEADER_PATTERN_KEY, "Name");
        properties.setProperty("c1." + ColumnDefinition.VALUE_VALIDATOR_KEY, MOCK_FACTORY);
        properties.setProperty("c1." + NAME_KEY, "c1");
        DataSetValidatorForTSV validator = new DataSetValidatorForTSV(properties);

        FileUtilities.writeToFile(new File(workingDirectory, "a.txt"), "ID\n" + "a\n" + "b\n");

        try
        {
            validator.assertValidDataSet(null, workingDirectory);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("No column definition matches the header of the 1. column: ID\n"
                    + "Column Definition 'c1' does not match: Reason: "
                    + "Does not match the following regular expression: Name", ex.getMessage());
        }

        MockValidatorFactory.assertSatisfied();
    }

    @Test
    public void testNoColumnDefinitionFound2()
    {
        Properties properties = new Properties();
        properties.setProperty(DataSetValidatorForTSV.PATH_PATTERNS_KEY, "*");
        properties.setProperty(DataSetValidatorForTSV.COLUMNS_KEY, "id, a, b, c");
        properties.setProperty("id." + ColumnDefinition.HEADER_PATTERN_KEY, "ID");
        properties.setProperty("id." + ColumnDefinition.MANDATORY_KEY, "true");
        properties.setProperty("id." + ColumnDefinition.ORDER_KEY, "1");
        properties.setProperty("id." + ColumnDefinition.VALUE_VALIDATOR_KEY, MOCK_FACTORY);
        properties.setProperty("a." + ColumnDefinition.HEADER_PATTERN_KEY, "A");
        properties.setProperty("a." + ColumnDefinition.ORDER_KEY, "2");
        properties.setProperty("a." + ColumnDefinition.VALUE_VALIDATOR_KEY, MOCK_FACTORY);
        properties.setProperty("b." + ColumnDefinition.HEADER_PATTERN_KEY, "B");
        properties.setProperty("b." + ColumnDefinition.ORDER_KEY, "3");
        properties.setProperty("b." + ColumnDefinition.VALUE_VALIDATOR_KEY, MOCK_FACTORY);
        properties.setProperty("c." + ColumnDefinition.HEADER_PATTERN_KEY, "C[0-9]*");
        properties.setProperty("c." + ColumnDefinition.CAN_DEFINE_MULTIPLE_COLUMNS_KEY, "true");
        properties.setProperty("c." + ColumnDefinition.VALUE_VALIDATOR_KEY, MOCK_FACTORY);
        DataSetValidatorForTSV validator = new DataSetValidatorForTSV(properties);

        FileUtilities.writeToFile(new File(workingDirectory, "a.txt"), "ID\ta\tC1\n");

        try
        {
            validator.assertValidDataSet(null, workingDirectory);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("No column definition matches the header of the 2. column: a\n"
                    + "Column Definition 'c' does not match: Reason: "
                    + "Does not match the following regular expression: C[0-9]*\n"
                    + "Column Definition 'a' does not match: Reason: "
                    + "Does not match the following regular expression: A", ex.getMessage());
        }

        MockValidatorFactory.assertSatisfied();
    }

    @Test
    public void testMissingMandatoryColumn()
    {
        Properties properties = new Properties();
        properties.setProperty(DataSetValidatorForTSV.PATH_PATTERNS_KEY, "*");
        properties.setProperty(DataSetValidatorForTSV.COLUMNS_KEY, "c1, c2");
        properties.setProperty("c1." + ColumnDefinition.HEADER_PATTERN_KEY, "Name");
        properties.setProperty("c1." + ColumnDefinition.VALUE_VALIDATOR_KEY, MOCK_FACTORY);
        properties.setProperty("c1." + NAME_KEY, "c1");
        properties.setProperty("c2." + ColumnDefinition.HEADER_PATTERN_KEY, "ID");
        properties.setProperty("c2." + ColumnDefinition.MANDATORY_KEY, "true");
        properties.setProperty("c2." + ColumnDefinition.VALUE_VALIDATOR_KEY, MOCK_FACTORY);
        properties.setProperty("c2." + NAME_KEY, "c2");
        DataSetValidatorForTSV validator = new DataSetValidatorForTSV(properties);

        FileUtilities.writeToFile(new File(workingDirectory, "a.txt"), "Name\n" + "a\n" + "b\n");

        try
        {
            validator.assertValidDataSet(null, workingDirectory);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("No column(s) found for the following mandatory column definition(s): c2",
                    ex.getMessage());
        }

        MockValidatorFactory.assertSatisfied();
    }

    @Test
    public void testMissingMandatoryOrderedColumn()
    {
        Properties properties = new Properties();
        properties.setProperty(DataSetValidatorForTSV.PATH_PATTERNS_KEY, "*");
        properties.setProperty(DataSetValidatorForTSV.IGNORE_EMPTY_LINES_KEY, "no");
        properties.setProperty(DataSetValidatorForTSV.COLUMNS_KEY, "c1");
        properties.setProperty("c1." + ColumnDefinition.HEADER_PATTERN_KEY, "Name");
        properties.setProperty("c1." + ColumnDefinition.ORDER_KEY, "1");
        properties.setProperty("c1." + ColumnDefinition.MANDATORY_KEY, "true");
        properties.setProperty("c1." + ColumnDefinition.VALUE_VALIDATOR_KEY, MOCK_FACTORY);
        properties.setProperty("c1." + NAME_KEY, "c1");
        DataSetValidatorForTSV validator = new DataSetValidatorForTSV(properties);

        FileUtilities.writeToFile(new File(workingDirectory, "a.txt"), "\n" + "\n");

        try
        {
            validator.assertValidDataSet(null, workingDirectory);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("1. column [name=c1] is mandatory but missing because there are only 0 column headers.",
                    ex.getMessage());
        }

        MockValidatorFactory.assertSatisfied();
    }

    @Test
    public void testMissingOrderedColumn()
    {
        Properties properties = new Properties();
        properties.setProperty(DataSetValidatorForTSV.PATH_PATTERNS_KEY, "*");
        properties.setProperty(DataSetValidatorForTSV.COLUMNS_KEY, "c1, c2");
        properties.setProperty("c1." + ColumnDefinition.HEADER_PATTERN_KEY, "Name");
        properties.setProperty("c1." + ColumnDefinition.ORDER_KEY, "1");
        properties.setProperty("c1." + ColumnDefinition.VALUE_VALIDATOR_KEY, MOCK_FACTORY);
        properties.setProperty("c1." + NAME_KEY, "c1");
        properties.setProperty("c2." + ColumnDefinition.HEADER_PATTERN_KEY, "a[0-9]+");
        properties.setProperty("c2." + ColumnDefinition.VALUE_VALIDATOR_KEY, MOCK_FACTORY);
        properties.setProperty("c2." + ColumnDefinition.CAN_DEFINE_MULTIPLE_COLUMNS_KEY, "yes");
        properties.setProperty("c2." + NAME_KEY, "c2");
        properties.setProperty("c2." + EXPECTED_VALUES_KEY, "1,2");
        DataSetValidatorForTSV validator = new DataSetValidatorForTSV(properties);

        FileUtilities.writeToFile(new File(workingDirectory, "a.txt"), "a1\ta2\n" + "1\t2\n");

        validator.assertValidDataSet(null, workingDirectory);

        MockValidatorFactory.assertSatisfied();
    }

    @Test
    public void testRowsLongerThanHeaders()
    {
        Properties properties = new Properties();
        properties.setProperty(DataSetValidatorForTSV.PATH_PATTERNS_KEY, "*");
        properties.setProperty(DataSetValidatorForTSV.COLUMNS_KEY, "c1, c2");
        properties.setProperty("c1." + ColumnDefinition.HEADER_PATTERN_KEY, "Name");
        properties.setProperty("c1." + ColumnDefinition.VALUE_VALIDATOR_KEY, MOCK_FACTORY);
        properties.setProperty("c1." + NAME_KEY, "c1");
        properties.setProperty("c2." + ColumnDefinition.HEADER_PATTERN_KEY, "ID");
        properties.setProperty("c2." + ColumnDefinition.VALUE_VALIDATOR_KEY, MOCK_FACTORY);
        properties.setProperty("c2." + NAME_KEY, "c2");
        properties.setProperty("c2." + EXPECTED_VALUES_KEY, "a");
        DataSetValidatorForTSV validator = new DataSetValidatorForTSV(properties);

        FileUtilities.writeToFile(new File(workingDirectory, "a.txt"), "ID\n" + "a\n" + "b\tc\n");

        try
        {
            validator.assertValidDataSet(null, workingDirectory);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("2. data row has 2 instead of 1 cells.", ex.getMessage());
        }

        MockValidatorFactory.assertSatisfied();
    }

    @Test
    public void testColumnOrder()
    {
        Properties properties = new Properties();
        properties.setProperty(DataSetValidatorForTSV.PATH_PATTERNS_KEY, "a.txt, b.txt");
        properties.setProperty(DataSetValidatorForTSV.COLUMNS_KEY, "c1, c2, c3, c4");
        properties.setProperty("c1." + ColumnDefinition.HEADER_PATTERN_KEY, "Name");
        properties.setProperty("c1." + ColumnDefinition.ORDER_KEY, "2");
        properties.setProperty("c1." + ColumnDefinition.VALUE_VALIDATOR_KEY, MOCK_FACTORY);
        properties.setProperty("c1." + NAME_KEY, "c1");
        properties.setProperty("c1." + EXPECTED_VALUES_KEY, "a,b,c,d");
        properties.setProperty("c2." + ColumnDefinition.HEADER_PATTERN_KEY, "ID|id");
        properties.setProperty("c2." + ColumnDefinition.ORDER_KEY, "1");
        properties.setProperty("c2." + ColumnDefinition.VALUE_VALIDATOR_KEY, MOCK_FACTORY);
        properties.setProperty("c2." + NAME_KEY, "c2");
        properties.setProperty("c2." + EXPECTED_VALUES_KEY, "1,2,3,4");
        properties.setProperty("c3." + ColumnDefinition.HEADER_PATTERN_KEY, "Alpha");
        properties.setProperty("c3." + ColumnDefinition.VALUE_VALIDATOR_KEY, MOCK_FACTORY);
        properties.setProperty("c3." + NAME_KEY, "c3");
        properties.setProperty("c3." + EXPECTED_VALUES_KEY, "a1,a2,a3,a4");
        properties.setProperty("c4." + ColumnDefinition.HEADER_PATTERN_KEY, "Beta");
        properties.setProperty("c4." + ColumnDefinition.VALUE_VALIDATOR_KEY, MOCK_FACTORY);
        properties.setProperty("c4." + NAME_KEY, "c4");
        properties.setProperty("c4." + EXPECTED_VALUES_KEY, "b1,b2");
        DataSetValidatorForTSV validator = new DataSetValidatorForTSV(properties);

        FileUtilities.writeToFile(new File(workingDirectory, "a.txt"), "ID\tName\tAlpha\n"
                + "1\ta\ta1\n" + "2\tb\ta2\n");
        FileUtilities.writeToFile(new File(workingDirectory, "b.txt"), "id\tName\tBeta\tAlpha\n"
                + "3\tc\tb1\ta3\n" + "4\td\tb2\ta4\n");

        validator.assertValidDataSet(null, workingDirectory);

        MockValidatorFactory.assertSatisfied();
    }

    @Test
    public void testUniqueColumnHeaders()
    {
        Properties properties = new Properties();
        properties.setProperty(DataSetValidatorForTSV.PATH_PATTERNS_KEY, "*");
        DataSetValidatorForTSV validator = new DataSetValidatorForTSV(properties);
        FileUtilities.writeToFile(new File(workingDirectory, "a.txt"), "A\tA\n");

        try
        {
            validator.assertValidDataSet(null, workingDirectory);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Column header 'A' appeared twice.", ex.getMessage());
        }

        MockValidatorFactory.assertSatisfied();
    }

    @Test
    public void testColumnDefinitionWhichCanBeUsedToDefineMultipleColumnsOrder()
    {
        Properties properties = new Properties();
        properties.setProperty(DataSetValidatorForTSV.PATH_PATTERNS_KEY, "a.txt");
        properties.setProperty(DataSetValidatorForTSV.COLUMNS_KEY, "c1, c2, c3, c4");
        properties.setProperty("c1." + ColumnDefinition.HEADER_PATTERN_KEY, "ID");
        properties.setProperty("c1." + ColumnDefinition.MANDATORY_KEY, "yes");
        properties.setProperty("c1." + ColumnDefinition.ORDER_KEY, "1");
        properties.setProperty("c1." + ColumnDefinition.VALUE_VALIDATOR_KEY, MOCK_FACTORY);
        properties.setProperty("c1." + NAME_KEY, "c1");
        properties.setProperty("c1." + EXPECTED_VALUES_KEY, "1,2");
        properties.setProperty("c2." + ColumnDefinition.HEADER_PATTERN_KEY, "A[0-9]*");
        properties.setProperty("c2." + ColumnDefinition.CAN_DEFINE_MULTIPLE_COLUMNS_KEY, "true");
        properties.setProperty("c2." + ColumnDefinition.VALUE_VALIDATOR_KEY, MOCK_FACTORY);
        properties.setProperty("c2." + NAME_KEY, "c2");
        properties.setProperty("c2." + EXPECTED_VALUES_KEY, "a,b,c,d");
        DataSetValidatorForTSV validator = new DataSetValidatorForTSV(properties);

        FileUtilities.writeToFile(new File(workingDirectory, "a.txt"), "ID\tA6\tA42\n"
                + "1\ta\tb\n" + "2\tc\td\n");

        validator.assertValidDataSet(null, workingDirectory);

        MockValidatorFactory.assertSatisfied();
    }

    @Test
    public void testMissingColumnWithOrder()
    {
        Properties properties = new Properties();
        properties.setProperty(DataSetValidatorForTSV.PATH_PATTERNS_KEY, "a.txt");
        properties.setProperty(DataSetValidatorForTSV.COLUMNS_KEY, "c1, c2");
        properties.setProperty("c1." + ColumnDefinition.HEADER_PATTERN_KEY, "Name");
        properties.setProperty("c1." + ColumnDefinition.ORDER_KEY, "2");
        properties.setProperty("c1." + ColumnDefinition.VALUE_VALIDATOR_KEY, MOCK_FACTORY);
        properties.setProperty("c1." + NAME_KEY, "c1");
        properties.setProperty("c2." + ColumnDefinition.HEADER_PATTERN_KEY, "ID|id");
        properties.setProperty("c2." + ColumnDefinition.ORDER_KEY, "1");
        properties.setProperty("c2." + ColumnDefinition.VALUE_VALIDATOR_KEY, MOCK_FACTORY);
        properties.setProperty("c2." + NAME_KEY, "c2");
        properties.setProperty("c2." + EXPECTED_VALUES_KEY, "1,2");
        DataSetValidatorForTSV validator = new DataSetValidatorForTSV(properties);

        FileUtilities.writeToFile(new File(workingDirectory, "a.txt"), "ID\n"
                + "1\n" + "2\n");

        validator.assertValidDataSet(null, workingDirectory);

        MockValidatorFactory.assertSatisfied();
    }

    @Test
    public void testInvalidHeaderOfColumnWithOrder()
    {
        Properties properties = new Properties();
        properties.setProperty(DataSetValidatorForTSV.PATH_PATTERNS_KEY, "a.txt");
        properties.setProperty(DataSetValidatorForTSV.COLUMNS_KEY, "c1");
        properties.setProperty("c1." + ColumnDefinition.HEADER_PATTERN_KEY, "Name");
        properties.setProperty("c1." + ColumnDefinition.ORDER_KEY, "1");
        properties.setProperty("c1." + ColumnDefinition.VALUE_VALIDATOR_KEY, MOCK_FACTORY);
        properties.setProperty("c1." + NAME_KEY, "c1");
        DataSetValidatorForTSV validator = new DataSetValidatorForTSV(properties);

        FileUtilities.writeToFile(new File(workingDirectory, "a.txt"), "ID\n" + "1\n");

        try
        {
            validator.assertValidDataSet(null, workingDirectory);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("No column definition matches the header of the 1. column: ID\n"
                    + "Column Definition 'c1' does not match: Reason: "
                    + "Does not match the following regular expression: Name", ex.getMessage());
        }

        MockValidatorFactory.assertSatisfied();
    }

}
