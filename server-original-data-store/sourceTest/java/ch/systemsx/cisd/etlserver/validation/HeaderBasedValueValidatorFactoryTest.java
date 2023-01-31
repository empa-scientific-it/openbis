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

import static ch.systemsx.cisd.etlserver.validation.HeaderBasedValueValidatorFactory.HEADER_PATTERN_KEY;

import java.util.Properties;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * @author Franz-Josef Elmer
 */
public class HeaderBasedValueValidatorFactoryTest extends AssertJUnit
{
    @Test
    public void testEmptyProperties()
    {
        HeaderBasedValueValidatorFactory factory = new HeaderBasedValueValidatorFactory(new Properties());

        try
        {
            factory.createValidator("blabla");
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("No value validator found for header 'blabla': ", ex.getMessage());
        }
    }

    @Test
    public void testUndefinedHeaderTypes()
    {
        Properties properties = new Properties();
        properties.setProperty(HeaderBasedValueValidatorFactory.HEADER_TYPES_KEY, "a, b");

        try
        {
            new HeaderBasedValueValidatorFactory(properties);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Given key 'header-pattern' not found in properties '[]'", ex.getMessage());
        }
    }

    @Test
    public void testInvalidPattern()
    {
        Properties properties = new Properties();
        properties.setProperty(HeaderBasedValueValidatorFactory.HEADER_TYPES_KEY, "a");
        properties.setProperty("a." + HEADER_PATTERN_KEY, "][");

        try
        {
            new HeaderBasedValueValidatorFactory(properties);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Invalid header pattern for header type 'a': ][", ex.getMessage());
        }
    }

    @Test
    public void testNonMatchingHeader()
    {
        Properties properties = new Properties();
        properties.setProperty(HeaderBasedValueValidatorFactory.HEADER_TYPES_KEY, "a, b");
        properties.setProperty("a." + HEADER_PATTERN_KEY, "a.*");
        properties.setProperty("b." + HEADER_PATTERN_KEY, "b.*");
        HeaderBasedValueValidatorFactory factory = new HeaderBasedValueValidatorFactory(properties);

        try
        {
            factory.createValidator("123");
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("No value validator found for header '123': \n" +
                    "Regular expression for headers of type 'a' have to match: a.*\n" +
                    "Regular expression for headers of type 'b' have to match: b.*", ex.getMessage());

        }
    }

    @Test
    public void testSwitchValidator()
    {
        Properties properties = new Properties();
        properties.setProperty(HeaderBasedValueValidatorFactory.HEADER_TYPES_KEY, "a, b");
        properties.setProperty("a." + HEADER_PATTERN_KEY, "a.*");
        properties.setProperty("a." + DefaultValueValidatorFactory.VALUE_TYPE_KEY, "numeric");
        properties.setProperty("a." + NumericValidatorFactory.VALUE_RANGE_KEY, "(0,1]");
        properties.setProperty("b." + HEADER_PATTERN_KEY, "b.*");
        HeaderBasedValueValidatorFactory factory = new HeaderBasedValueValidatorFactory(properties);

        IValidator validator = factory.createValidator("a1");
        validator.assertValid("0.123");
        try
        {
            validator.assertValid("1.25");
            fail("UserFailureException expected.");
        } catch (UserFailureException ex)
        {
            assertEquals("Number to large: 1.25 > 1.0", ex.getMessage());
        }

        factory.createValidator("bc").assertValid("1.25");
    }
}
