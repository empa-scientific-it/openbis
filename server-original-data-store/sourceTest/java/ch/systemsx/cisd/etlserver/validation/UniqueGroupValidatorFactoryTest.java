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
 * Test cases for {@link UniqueGroupValidatorFactory}.
 * 
 * @author Izabela Adamczyk
 */
public class UniqueGroupValidatorFactoryTest extends AssertJUnit
{
    private static final String HEADER = "Code";

    @Test
    public void testNoPattern()
    {
        boolean exceptionThrown = false;
        try
        {
            Properties properties = new Properties();
            new UniqueGroupValidatorFactory(properties);
        } catch (ConfigurationFailureException ex)
        {
            exceptionThrown = true;
            assertEquals("Given key 'value-pattern' not found in properties '[]'", ex.getMessage());
        }
        assertTrue(exceptionThrown);
    }

    public void testEmptyPattern()
    {
        boolean exceptionThrown = false;
        try
        {
            Properties properties = new Properties();
            properties.put("value-pattern", "");
            new UniqueGroupValidatorFactory(properties);
        } catch (ConfigurationFailureException ex)
        {
            exceptionThrown = true;
            assertEquals("Given key 'value-pattern' not found in properties '[]'", ex.getMessage());
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void testNoGroups()
    {
        boolean exceptionThrown = false;
        try
        {
            Properties properties = new Properties();
            properties.put("value-pattern", "a");
            new UniqueGroupValidatorFactory(properties);
        } catch (ConfigurationFailureException ex)
        {
            exceptionThrown = true;
            assertEquals("Given key 'groups' not found in properties '[value-pattern]'", ex
                    .getMessage());
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void testEmptyGroups()
    {
        boolean exceptionThrown = false;
        try
        {
            createFactory("a", "");
        } catch (ConfigurationFailureException ex)
        {
            exceptionThrown = true;
            assertEquals("Given key 'groups' not found in properties '[groups, value-pattern]'", ex.getMessage());
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void testGroupTooSmallZero()
    {
        boolean exceptionThrown = false;
        try
        {
            createFactory("a", "1,0");
        } catch (ConfigurationFailureException ex)
        {
            exceptionThrown = true;
            assertEquals("Illegal group: 0", ex.getMessage());
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void testGroupTooSmallLessThanZero()
    {
        boolean exceptionThrown = false;
        try
        {
            createFactory("a", "1,-1");
        } catch (ConfigurationFailureException ex)
        {
            exceptionThrown = true;
            assertEquals("Illegal group: -1", ex.getMessage());
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void testGroupNotANumber()
    {
        boolean exceptionThrown = false;
        try
        {
            createFactory("a", "(abc), (.*)");
        } catch (ConfigurationFailureException ex)
        {
            exceptionThrown = true;
            assertEquals("Illegal group: (abc)", ex.getMessage());
        }
        assertTrue(exceptionThrown);
    }

    private UniqueGroupValidatorFactory createFactory(String pattern, String groups)
    {
        return new UniqueGroupValidatorFactory(createProperties(pattern, groups));
    }

    private Properties createProperties(String pattern, String groups)
    {
        Properties properties = new Properties();
        properties.put("value-pattern", pattern);
        properties.put("groups", groups);
        return properties;
    }

    @Test
    public void testUnknownGroup()
    {
        String pattern = "a";
        IValidator validator = createValidator(pattern, "1");
        boolean exceptionThrown = false;
        try
        {
            validator.assertValid("a");
        } catch (UserFailureException ex)
        {
            exceptionThrown = true;
            assertEquals("Group '1' not found. Value: 'a', pattern: 'a'", ex.getMessage());
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void testUnmatchedPattern()
    {
        String pattern = "a";
        IValidator validator = createValidator(pattern, "1");
        boolean exceptionThrown = false;
        try
        {
            validator.assertValid("b");
        } catch (UserFailureException ex)
        {
            exceptionThrown = true;
            assertEquals("Value 'b' does not match the pattern 'a'", ex.getMessage());
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void testUniqeValuesFullText()
    {
        String pattern = "(a*)";
        IValidator validator = createValidator(pattern, "1");
        validator.assertValid("a");
        validator.assertValid("aa");
        validator.assertValid("aaa");
    }

    @Test
    public void testNotUniqeValuesFullText()
    {
        String pattern = "(a*)";
        IValidator validator = createValidator(pattern, "1");
        validator.assertValid("a");
        validator.assertValid("aa");
        validator.assertValid("aaa");
        boolean exceptionThrown = false;
        try
        {
            validator.assertValid("aa");
        } catch (UserFailureException ex)
        {
            exceptionThrown = true;
            assertEquals("Record 'aa' breaks group uniqueness (repeated group: '[aa]')", ex
                    .getMessage());
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void testUniqeValuesPartOfTheText()
    {
        String pattern = "(a*)";
        IValidator validator = createValidator(pattern, "1");
        validator.assertValid("a");
        validator.assertValid("aa");
        validator.assertValid("aaa");
    }

    @Test
    public void testNotUniqeValuesPartOfTheTextEmpty()
    {
        String pattern = "a*(b*)c*";
        IValidator validator = createValidator(pattern, "1");
        validator.assertValid("a");
        boolean exceptionThrown = false;
        try
        {
            validator.assertValid("aac");
        } catch (UserFailureException ex)
        {
            exceptionThrown = true;
            assertEquals("Record 'aac' breaks group uniqueness (repeated group: '[]')", ex
                    .getMessage());
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void testNotUniqeValuesPartOfTheTextNotEmpty()
    {
        String pattern = "a*(b*)c*";
        IValidator validator = createValidator(pattern, "1");
        validator.assertValid("ab");
        validator.assertValid("abb");
        boolean exceptionThrown = false;
        try
        {
            validator.assertValid("aabc");
        } catch (UserFailureException ex)
        {
            exceptionThrown = true;
            assertEquals("Record 'aabc' breaks group uniqueness (repeated group: '[b]')", ex
                    .getMessage());
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void testNotUniqeValuesTwoGroups()
    {
        String pattern = "(a*)b*(c*)";
        IValidator validator = createValidator(pattern, "1,2");
        validator.assertValid("abc");
        validator.assertValid("abcc");
        boolean exceptionThrown = false;
        try
        {
            validator.assertValid("abbbbbbc");
        } catch (UserFailureException ex)
        {
            exceptionThrown = true;
            assertEquals("Record 'abbbbbbc' breaks group uniqueness (repeated group: '[a, c]')", ex
                    .getMessage());
        }
        assertTrue(exceptionThrown);
    }

    private IValidator createValidator(String pattern, String groups)
    {
        UniqueGroupValidatorFactory factory = createFactory(pattern, groups);
        IValidator validator = factory.createValidator(HEADER);
        return validator;
    }

    @Test
    public void testCreateValidatorReturnsFreshValidator()
    {
        UniqueGroupValidatorFactory factory = createFactory("(a*)", "1");
        IValidator validator = factory.createValidator(HEADER);

        validator.assertValid("a");

        validator = factory.createValidator(HEADER);

        validator.assertValid("a");
    }
}
