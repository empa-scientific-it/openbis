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

package ch.systemsx.cisd.common.converter;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.Date;

import org.testng.annotations.Test;

/**
 * Test cases for the {@link ConverterPool}.
 * 
 * @author Christian Ribeaud
 */
public final class ConverterPoolTest
{

    private static final char NULL = '\0';

    @Test
    public final void testRegisterConverter()
    {
        boolean exceptionThrown = false;
        try
        {
            ConverterPool.getInstance().registerConverter(null, null);
        } catch (AssertionError ex)
        {
            exceptionThrown = true;
        }
        assertTrue("Null type is not allowed.", exceptionThrown);

        exceptionThrown = false;
        try
        {
            ConverterPool.getInstance().registerConverter(String.class, null);
        } catch (AssertionError ex)
        {
            exceptionThrown = true;
        }
        assertTrue("Null converter is not allowed.", exceptionThrown);

        assertNull(ConverterPool.getInstance().getConverter(String.class));
        assertNull(ConverterPool.getInstance().getConverter(Date.class));
        ConverterPool.getInstance().registerConverter(Date.class, new DateConverter("dd.MM.yyyy"));
        assertNotNull(ConverterPool.getInstance().getConverter(Date.class));
        assertNull(ConverterPool.getInstance().getConverter(String.class));
    }

    @Test(dependsOnMethods = "testRegisterConverter")
    public final void testUnRegisterConverter()
    {
        assertNotNull(ConverterPool.getInstance().getConverter(Date.class));
        boolean exceptionThrown = false;
        try
        {
            ConverterPool.getInstance().unregisterConverter(null);

        } catch (AssertionError ex)
        {
            exceptionThrown = true;
        }
        assertTrue("Null type is not allowed.", exceptionThrown);
        ConverterPool.getInstance().unregisterConverter(Date.class);
        assertNull(ConverterPool.getInstance().getConverter(Date.class));
    }

    @Test(dependsOnMethods = "testUnRegisterConverter")
    public final void testConvert()
    {
        boolean exceptionThrown = false;
        try
        {
            ConverterPool.getInstance().convert(null, null);

        } catch (AssertionError ex)
        {
            exceptionThrown = true;
        }
        assertTrue("Null type is not allowed.", exceptionThrown);
        ConverterPool pool = ConverterPool.getInstance();
        // String
        assertNull(pool.convert(null, String.class));
        assertEquals("", pool.convert("", String.class));
        // Integer
        assertEquals(Integer.valueOf(1), pool.convert("1", Integer.class));
        assertEquals(Integer.valueOf(1), pool.convert("1", Integer.TYPE));
        try
        {
            pool.convert("notParsable", Integer.class);
            fail("Could not be parsed.");
        } catch (NumberFormatException ex)
        {
            // Nothing to do here.
        }
        String msg = "No converter for type 'java.util.Date'.";
        try
        {
            pool.convert("", Date.class);
            fail(msg);
        } catch (IllegalArgumentException ex)
        {
            assertEquals(msg, ex.getMessage());
        }
        // Boolean
        assertEquals(Boolean.FALSE, pool.convert("1", Boolean.class));
        assertEquals(Boolean.FALSE, pool.convert("1", Boolean.TYPE));
        assertEquals(Boolean.TRUE, pool.convert("TrUe", Boolean.TYPE));
        // Character
        assertEquals(Character.valueOf('c'), pool.convert("c", Character.class));
        assertEquals(Character.valueOf('c'), pool.convert("c", Character.TYPE));
        assertEquals(Character.valueOf('c'), pool.convert("choubidou", Character.class));
        assertEquals(Character.valueOf(NULL), pool.convert("", Character.class));
        // Customized Integer converter
        pool.registerConverter(Integer.class, new IntegerConverter());
        assertEquals(Integer.valueOf(13), pool.convert("1", Integer.class));
        assertEquals(Integer.valueOf(12), pool.convert("", Integer.class));
        try
        {
            assertEquals(Integer.valueOf(12), pool.convert("", Integer.TYPE));
            fail("Converter only registered for 'Integer.class' and not for 'Integer.TYPE'");
        } catch (NumberFormatException ex)
        {
            // Nothing to do here.
        }
    }

    //
    // Helper Classes
    //

    private final static class IntegerConverter implements Converter<Integer>
    {

        //
        // Converter
        //

        @Override
        public final Integer convert(String value)
        {
            try
            {
                Integer.parseInt(value);
                return Integer.valueOf(13);
            } catch (NumberFormatException ex)
            {
                return null;
            }
        }

        @Override
        public final Integer getDefaultValue()
        {
            return Integer.valueOf(12);
        }
    }
}