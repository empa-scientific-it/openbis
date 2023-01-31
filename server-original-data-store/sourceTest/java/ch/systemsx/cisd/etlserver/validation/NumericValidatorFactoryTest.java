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

import java.util.Properties;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * @author Franz-Josef Elmer
 */
public class NumericValidatorFactoryTest extends AssertJUnit
{
    @Test
    public void testInvalidRange()
    {
        try
        {
            createValidator("[]");
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Invalid range: []", ex.getMessage());
        }
    }

    @Test
    public void testMissingOpeningBracketInRangeDescription()
    {
        try
        {
            createValidator("2,3]");
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Range has to start with either '(' or '[': 2,3]", ex.getMessage());
        }
    }

    @Test
    public void testMissingClosingBracketInRangeDescription()
    {
        try
        {
            createValidator("(2,3}");
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Range has to end with either ')' or ']': (2,3}", ex.getMessage());
        }
    }

    @Test
    public void testMissingCommaInRangeDescription()
    {
        try
        {
            createValidator("(2 3)");
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Missing comma in range definition: (2 3)", ex.getMessage());
        }
    }

    @Test
    public void testInvalidMinimumInRangeDescription()
    {
        try
        {
            createValidator("(abc,3)");
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Invalid minimum in range definition: (abc,3)", ex.getMessage());
        }
    }

    @Test
    public void testInvalidMaximumInRangeDescription()
    {
        try
        {
            createValidator("(1,abc)");
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Invalid maximum in range definition: (1,abc)", ex.getMessage());
        }
    }

    @Test
    public void testMinLargerThanMaxInRangeDescription()
    {
        try
        {
            createValidator("(1,0.999)");
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Minimum is larger than maximum in range description: (1,0.999)", ex.getMessage());
        }
    }

    @Test
    public void testInvalidNumber()
    {
        NumericValidatorFactory validatorFactory = new NumericValidatorFactory(new Properties());
        IValidator validator = validatorFactory.createValidator("blabla");

        assertNotANumber(validator, "abc");
        assertNotANumber(validator, " -0-");
    }

    @Test
    public void testEmptyValue()
    {
        NumericValidatorFactory validatorFactory = new NumericValidatorFactory(new Properties());
        IValidator validator = validatorFactory.createValidator("blabla");

        assertFailingOnBlankValue(validator, null);
        assertFailingOnBlankValue(validator, "");
        assertFailingOnBlankValue(validator, " ");
    }

    @Test
    public void testAllowEmptyValues()
    {
        Properties properties = new Properties();
        properties.setProperty(AbstractValidatorFactory.ALLOW_EMPTY_VALUES_KEY, "true");
        properties.setProperty(NumericValidatorFactory.VALUE_RANGE_KEY, "(0,1]");
        NumericValidatorFactory validatorFactory = new NumericValidatorFactory(properties);
        IValidator validator = validatorFactory.createValidator("blabla");

        validator.assertValid(null);
        validator.assertValid("");
        validator.assertValid("  ");
        validator.assertValid("  0.9999");
        assertFailingToLarge("> 1.0", validator, "1.25");
        assertFailingToSmall("<= 0.0", validator, "0.0");
    }

    @Test
    public void testAllowEmptyValueSynonyms()
    {
        Properties properties = new Properties();
        properties.setProperty(AbstractValidatorFactory.ALLOW_EMPTY_VALUES_KEY, "true");
        properties.setProperty(AbstractValidatorFactory.EMPTY_VALUE_SYNONYMS_KEY, "-, N/A");
        properties.setProperty(NumericValidatorFactory.VALUE_RANGE_KEY, "(0,1]");
        NumericValidatorFactory validatorFactory = new NumericValidatorFactory(properties);
        IValidator validator = validatorFactory.createValidator("blabla");

        validator.assertValid(null);
        validator.assertValid("");
        validator.assertValid("  ");
        validator.assertValid("-");
        validator.assertValid("N/A");
        assertNotANumber(validator, "n/a");
        assertFailingToLarge("> 1.0", validator, "1.25");
        assertFailingToSmall("<= 0.0", validator, "0.0");
    }

    @Test
    public void testNoRange()
    {
        NumericValidatorFactory validatorFactory = new NumericValidatorFactory(new Properties());
        IValidator validator = validatorFactory.createValidator("blabla");

        validator.assertValid("-Infinity");
        validator.assertValid("-1");
        validator.assertValid("-100");
        validator.assertValid("-1.78e-9");
        validator.assertValid("100.45");
        validator.assertValid("1.78e19");
        validator.assertValid("Infinity");
    }

    @Test
    public void testNegativeNumbers()
    {
        IValidator validator = createValidator("(-Infinity,0)");

        validator.assertValid("-1");
        validator.assertValid("-100");
        validator.assertValid("-1.78e-9");
        assertFailingToSmall("<= -Infinity", validator, "-Infinity");
        assertFailingToLarge(">= 0.0", validator, "0.0");
    }

    @Test
    public void testNonNegativeNumbers()
    {
        IValidator validator = createValidator("[ 0 , Infinity )");

        validator.assertValid("1");
        validator.assertValid("100");
        validator.assertValid("1.78e-9");
        validator.assertValid("0");
        assertFailingToSmall("< 0.0", validator, "-1.0E-9");
        assertFailingToLarge(">= Infinity", validator, "Infinity");
    }

    private IValidator createValidator(String range)
    {
        Properties properties = new Properties();
        properties.setProperty(NumericValidatorFactory.VALUE_RANGE_KEY, range);
        NumericValidatorFactory validatorFactory = new NumericValidatorFactory(properties);
        return validatorFactory.createValidator("blabla");
    }

    private void assertFailingOnBlankValue(IValidator validator, String value)
    {
        try
        {
            validator.assertValid(value);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Empty value is not allowed.", ex.getMessage());
        }
    }

    private void assertNotANumber(IValidator validator, String string)
    {
        try
        {
            validator.assertValid(string);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Not a number: " + string, ex.getMessage());
        }
    }

    private void assertFailingToSmall(String expectedPostfix, IValidator validator, String number)
    {
        try
        {
            validator.assertValid(number);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Number to small: " + number + " " + expectedPostfix, ex.getMessage());
        }
    }

    private void assertFailingToLarge(String expectedPostfix, IValidator validator, String number)
    {
        try
        {
            validator.assertValid(number);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Number to large: " + number + " " + expectedPostfix, ex.getMessage());
        }
    }
}
