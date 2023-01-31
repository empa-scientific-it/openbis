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

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * @author Franz-Josef Elmer
 */
public class UniqueValidatorFactoryTest extends AssertJUnit
{
    @Test
    public void testNoPattern()
    {
        UniqueValidatorFactory factory = new UniqueValidatorFactory(new Properties());
        IValidator validator = factory.createValidator("blabla");

        validator.assertValid("a");
        validator.assertValid("b");
        try
        {
            validator.assertValid("a");
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("The following value is not unique: a", ex.getMessage());
        }
    }

    @Test
    public void testWithPattern()
    {
        Properties properties = new Properties();
        properties.setProperty(StringValidatorFactory.VALUE_PATTERN_KEY, "a[0-9]*");
        UniqueValidatorFactory factory = new UniqueValidatorFactory(properties);
        IValidator validator = factory.createValidator("blabla");

        validator.assertValid("a");
        validator.assertValid("a1");
        validator.assertValid("a123");
        try
        {
            validator.assertValid("ab");
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("'ab' is invalid: Does not match the following regular expression: "
                    + "a[0-9]*", ex.getMessage());
        }
    }

    @Test
    public void testCreateValidatorReturnsFreshValidator()
    {
        UniqueValidatorFactory factory = new UniqueValidatorFactory(new Properties());
        IValidator validator = factory.createValidator("blabla");

        validator.assertValid("a");

        validator = factory.createValidator("blabla");

        validator.assertValid("a");
    }
}
