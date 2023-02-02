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

package ch.systemsx.cisd.common.parser;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import org.apache.commons.lang3.ArrayUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * Test cases for corresponding {@link AbstractParserObjectFactory} class.
 * 
 * @author Christian Ribeaud
 */
public final class AbstractParserObjectFactoryTest
{

    private static final int BEAN_NUMBER = 1;

    private static final String BEAN_DESCRIPTION = "Bean Description";

    private static final String BEAN_NAME = "Bean Name";

    private final static IPropertyMapper createPropertyMapper(final boolean mixedCase)
    {
        final String[] strings;
        if (mixedCase)
        {
            strings = new String[]
            { "NAME", "Description", "NuMbEr" };
        } else
        {
            strings = new String[]
            { "name", "description", "number" };
        }
        return new DefaultPropertyMapper(strings, null);
    }

    private final static String[] createDefaultLineTokens()
    {
        return new String[]
        { BEAN_NAME, BEAN_DESCRIPTION, "1" };
    }

    private final void checkBean(final Bean bean)
    {
        assertEquals(BEAN_NAME, bean.name);
        assertEquals(BEAN_DESCRIPTION, bean.description);
        assertEquals(BEAN_NUMBER, bean.number);
    }

    @Test(expectedExceptions = AssertionError.class)
    public final void testConstructorWithNull()
    {
        new BeanFactory(null, null);
    }

    @Test
    public final void testPropertyMapperWithUnmatchedProperties()
    {
        final IPropertyMapper propertyMapper = new DefaultPropertyMapper(new String[]
        { "name", "description", "IsNotIn" }, null);
        try
        {
            new BeanFactory(Bean.class, propertyMapper);
            fail("Following properties '[isnotin]' are not part of 'Bean'.");
        } catch (final UnmatchedPropertiesException ex)
        {
            assertEquals(
                    "Columns 'isnotin' specified in the header are not expected (mandatory colums are 'name', optional colums are 'description', 'number')",
                    ex.getMessage());
        }
    }

    @Test
    public final void testMandatoryFields()
    {
        final DefaultPropertyMapper propertyMapper = new DefaultPropertyMapper(new String[]
        { "description" }, null);
        try
        {
            final BeanFactory beanFactory = new BeanFactory(Bean.class, propertyMapper);
            final String[] lineTokens = new String[]
            { "1. experiment" };
            beanFactory.createObject(lineTokens);
            fail("Field/Property code 'name' is mandatory.");
        } catch (final MandatoryPropertyMissingException ex)
        {
            AssertJUnit.assertTrue(ex.getMessage().indexOf("name") > 1);
        }
    }

    @Test
    public final void testMandatoryFieldWithoutValue()
    {
        final IPropertyMapper propertyMapper = createPropertyMapper(false);
        final BeanFactory beanFactory = new BeanFactory(Bean.class, propertyMapper);
        final String[] lineTokens = new String[]
        { "", "desc", "1" };
        String errMsg = "Missing value for the mandatory column 'name'.";
        try
        {
            beanFactory.createObject(lineTokens);
        } catch (MandatoryPropertyMissingException e)
        {
            assertEquals(errMsg, e.getMessage());
            return;
        }
        fail("Error expected: " + errMsg);
    }

    @Test
    public final void testTooManyDataColumns()
    {
        final IPropertyMapper propertyMapper = createPropertyMapper(false);
        final BeanFactory beanFactory = new BeanFactory(Bean.class, propertyMapper);
        final String[] lineTokens = (String[]) ArrayUtils.add(createDefaultLineTokens(), "notUsed");
        final Bean bean = beanFactory.createObject(lineTokens);
        checkBean(bean);
    }

    @Test
    public final void testNotEnoughDataColumns()
    {
        final IPropertyMapper propertyMapper = createPropertyMapper(false);
        final BeanFactory beanFactory = new BeanFactory(Bean.class, propertyMapper);
        final String[] defaultTokens = createDefaultLineTokens();
        final String[] lineTokens =
                (String[]) ArrayUtils.remove(defaultTokens, defaultTokens.length - 1);
        final String msg =
                "Not enough columns available. Looking for 3rd column but we have only 2 columns ('Bean Name', 'Bean Description').";
        try
        {
            beanFactory.createObject(lineTokens);
            fail(msg);
        } catch (final IndexOutOfBoundsException ex)
        {
            assertEquals(msg, ex.getMessage());
        }
    }

    @Test
    public final void testRegisterConverterWithNull()
    {
        final IPropertyMapper propertyMapper = createPropertyMapper(false);
        final BeanFactory beanFactory = new BeanFactory(Bean.class, propertyMapper);
        try
        {
            beanFactory.registerConverter(null, null);
            fail("Null type is not allowed.");
        } catch (final AssertionError ex)
        {
            // Nothing to do here.
        }
        beanFactory.registerConverter(String.class, null);
    }

    @Test
    public final void testCaseInsensitivity()
    {
        final IPropertyMapper propertyMapper = createPropertyMapper(true);
        final BeanFactory beanFactory = new BeanFactory(Bean.class, propertyMapper);
        final Bean bean = beanFactory.createObject(createDefaultLineTokens());
        checkBean(bean);
    }

    //
    // Helper Classes
    //

    private final static class BeanFactory extends AbstractParserObjectFactory<Bean>
    {

        BeanFactory(final Class<Bean> clazz, final IPropertyMapper propertyMapper)
        {
            super(clazz, propertyMapper);
        }

    }

    public final static class Bean
    {
        private String name;

        private String description;

        private int number;

        public final String getName()
        {
            return name;
        }

        @BeanProperty(label = "name")
        public final void setName(final String name)
        {
            this.name = name;
        }

        public final int getNumber()
        {
            return number;
        }

        @BeanProperty(label = "number", optional = true)
        public final void setNumber(final int number)
        {
            this.number = number;
        }

        public final String getDescription()
        {
            return description;
        }

        @BeanProperty(label = "description", optional = true)
        public final void setDescription(final String description)
        {
            this.description = description;
        }
    }
}