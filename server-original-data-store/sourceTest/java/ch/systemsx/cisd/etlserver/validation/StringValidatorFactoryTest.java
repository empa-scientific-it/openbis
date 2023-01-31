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
public class StringValidatorFactoryTest extends AssertJUnit
{
    @Test
    public void testMissingPattern()
    {
        try
        {
            new StringValidatorFactory(new Properties());
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Given key '" + StringValidatorFactory.VALUE_PATTERN_KEY
                    + "' not found in properties '[]'", ex.getMessage());
        }
    }

    @Test
    public void testEmptyValuesNotAllowed()
    {
        Properties properties = new Properties();
        properties.setProperty(StringValidatorFactory.VALUE_PATTERN_KEY, "a.*");
        StringValidatorFactory factory = new StringValidatorFactory(properties);
        IValidator validator = factory.createValidator("blabla");

        validator.assertValid("a");
        validator.assertValid("a1");
        validator.assertValid("abc");
        try
        {
            validator.assertValid("bc");
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("'bc' is invalid: Does not match the following regular expression: "
                    + "a.*", ex.getMessage());
        }
        try
        {
            validator.assertValid("  ");
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Empty value is not allowed.", ex.getMessage());
        }
    }

    @Test
    public void testEmptyValuesAllowed()
    {
        Properties properties = new Properties();
        properties.setProperty(StringValidatorFactory.VALUE_PATTERN_KEY, "a.*");
        properties.setProperty(AbstractValidatorFactory.ALLOW_EMPTY_VALUES_KEY, "yes");
        StringValidatorFactory factory = new StringValidatorFactory(properties);
        IValidator validator = factory.createValidator("blabla");

        validator.assertValid("a");
        validator.assertValid("a1");
        validator.assertValid("abc");
        validator.assertValid(null);
        validator.assertValid("");
        validator.assertValid("  ");
        try
        {
            validator.assertValid("bc");
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("'bc' is invalid: Does not match the following regular expression: a.*",
                    ex.getMessage());
        }
    }

    @Test
    public void testEmptyValueSynonyms()
    {
        Properties properties = new Properties();
        properties.setProperty(StringValidatorFactory.VALUE_PATTERN_KEY, "a.*");
        properties.setProperty(AbstractValidatorFactory.ALLOW_EMPTY_VALUES_KEY, "yes");
        properties.setProperty(AbstractValidatorFactory.EMPTY_VALUE_SYNONYMS_KEY, "-");
        StringValidatorFactory factory = new StringValidatorFactory(properties);
        IValidator validator = factory.createValidator("blabla");

        validator.assertValid("a");
        validator.assertValid("a1");
        validator.assertValid("abc");
        validator.assertValid(null);
        validator.assertValid("");
        validator.assertValid("  ");
        validator.assertValid("-");
        try
        {
            validator.assertValid("N/A");
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("'N/A' is invalid: Does not match the following regular expression: a.*",
                    ex.getMessage());
        }
    }
}
