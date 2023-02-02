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

package ch.systemsx.cisd.common.properties;

import java.util.Properties;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.properties.ExtendedProperties;

/**
 * Test cases for the {@link ExtendedProperties} class.
 * 
 * @author Christian Ribeaud
 */
public final class ExtendedPropertiesTest extends AssertJUnit
{
    private ExtendedProperties extendedProperties;

    @BeforeMethod
    public final void setUp()
    {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("one", "eins");
        props.setProperty("un", "${one}");
        props.setProperty("two", "zwei");
        props.setProperty("three", "drei");
        props.setProperty("four", "${un}${three}");
        extendedProperties = props;
    }

    @Test
    public final void testConstructor()
    {
        ExtendedProperties newProps = ExtendedProperties.createWith(extendedProperties);
        assertEquals(5, extendedProperties.size());
        assertEquals(extendedProperties.size(), newProps.size());
        String key = "one";
        String expectedValue = "eins";
        assertEquals(expectedValue, extendedProperties.getProperty(key));
        assertEquals(extendedProperties.getProperty(key), newProps.getProperty(key));
    }

    @Test
    public final void testGetPropertyString()
    {
        assertEquals("eins", extendedProperties.getProperty("one"));
        assertEquals("eins", extendedProperties.getProperty("un"));
        assertEquals("einsdrei", extendedProperties.getProperty("four", "abc"));
    }

    @Test
    public void testMultipleReplacements()
    {
        extendedProperties.setProperty("greetings", "hello ${one}, hi ${un}, hi ${two}");

        assertEquals("hello eins, hi eins, hi zwei", extendedProperties.getProperty("greetings"));
    }

    @Test
    public void testReplacementWithDefaultValue()
    {
        extendedProperties.setProperty("greetings", "hello ${un}, hi ${1:eins}");

        assertEquals("hello eins, hi eins", extendedProperties.getProperty("greetings"));
    }

    @Test
    public final void testGetSubsetString()
    {
        int size = extendedProperties.size();
        extendedProperties.removeSubset("t");
        assert extendedProperties.size() == size - 2;

        size = extendedProperties.size();
        extendedProperties.removeSubset("t");
        assert extendedProperties.size() == size; // nothing removed
    }

    @Test
    public final void testRemoveSubsetString()
    {
        ExtendedProperties props = extendedProperties.getSubset("t", false);
        assert props.size() == 2;
        assert props.getProperty("two").equals("zwei");
        assert props.getProperty("three").equals("drei");

        props = extendedProperties.getSubset("un", false);
        assert props.size() == 1;
        assert props.getProperty("un").equals("eins");

        props = extendedProperties.getSubset("t", true);
        assert props.size() == 2;
        assert props.getProperty("wo").equals("zwei");
        assert props.getProperty("hree").equals("drei");

        props = extendedProperties.getSubset("un", true);
        assert props.size() == 1;
        assert props.getProperty("").equals("eins");
    }

    @Test
    public final void testCyclicDependency()
    {
        // Two nodes cyclic dependencies
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("a", "${b}");
        props.setProperty("b", "${a}");
        assertEquals("${b}", props.getProperty("b"));
        // Three nodes cyclic dependencies
        props = new ExtendedProperties();
        props.setProperty("a", "A${b}");
        props.setProperty("b", "B${c}");
        props.setProperty("c", "C${a}");
        assertEquals("ABC${a}", props.getProperty("a"));
    }

    @Test
    public final void testGetUnalteredProperty()
    {
        assertEquals("${one}", extendedProperties.getUnalteredProperty("un"));
    }

    @Test
    public final void testGetPropertyWithInheritTree()
    {
        Properties properties = new Properties();
        properties.setProperty("default.code", "42");
        properties.setProperty("type.code", "ABC-${default.code}");
        properties.setProperty("type.label", "abc");
        properties.setProperty("validator.order", "1");
        properties.setProperty("validator.type.", "type.");
        properties.setProperty("my.validator.", "validator.");
        properties.setProperty("my.validator.order", "2");
        properties.setProperty("my.validator.type.label", "a-b-c");
        properties.setProperty("another.validator.", "my.validator.");
        properties.setProperty("another.validator.type.code", "alpha-${default.code}");
        Properties props = ExtendedProperties.createWith(properties);

        assertEquals("2", props.getProperty("my.validator.order"));
        assertEquals("ABC-42", props.getProperty("my.validator.type.code"));
        assertEquals("a-b-c", props.getProperty("my.validator.type.label"));
        assertEquals("2", props.getProperty("another.validator.order"));
        assertEquals("alpha-42", props.getProperty("another.validator.type.code"));
        assertEquals("a-b-c", props.getProperty("another.validator.type.label"));
    }

    @Test
    public final void testGetPropertyWithCyclicInheritance()
    {
        Properties properties = new Properties();
        properties.setProperty("a.", "b.");
        properties.setProperty("b.", "a.");
        properties.setProperty("my.", "b.");
        Properties props = ExtendedProperties.createWith(properties);

        try
        {
            props.getProperty("my.code");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Cyclic definition of property 'b.code'.", ex.getMessage());
        }
    }

    @Test
    public final void testGetSubsetDroppingPrefixWithInheritTree()
    {
        ExtendedProperties properties = new ExtendedProperties();
        properties.setProperty("default.code", "42");
        properties.setProperty("type.code", "ABC-${default.code}");
        properties.setProperty("type.label", "abc");
        properties.setProperty("validator.order", "1");
        properties.setProperty("validator.type.", "type.");
        properties.setProperty("my.validator.", "validator.");
        properties.setProperty("my.validator.order", "2");
        properties.setProperty("my.validator.type.label", "a-b-c");
        Properties subset = properties.getSubset("my.validator.", true);

        assertEquals("2", subset.getProperty("order"));
        assertEquals("ABC-42", subset.getProperty("type.code"));
        assertEquals("a-b-c", subset.getProperty("type.label"));
        assertEquals(3, subset.size());
    }

    @Test
    public final void testGetSubsetNotDroppingPrefixWithInheritTree()
    {
        ExtendedProperties properties = new ExtendedProperties();
        properties.setProperty("default.code", "42");
        properties.setProperty("type.code", "ABC-${default.code}");
        properties.setProperty("type.label", "abc");
        properties.setProperty("validator.order", "1");
        properties.setProperty("validator.type.", "type.");
        properties.setProperty("my.validator.", "validator.");
        properties.setProperty("my.validator.order", "2");
        properties.setProperty("my.validator.type.label", "a-b-c");
        Properties subset = properties.getSubset("my.", false);

        assertEquals("2", subset.getProperty("my.validator.order"));
        assertEquals("ABC-42", subset.getProperty("my.validator.type.code"));
        assertEquals("a-b-c", subset.getProperty("my.validator.type.label"));
        assertEquals(3, subset.size());
    }

    @Test
    public final void testGetSubsetWithCyclicInheritance()
    {
        Properties properties = new Properties();
        properties.setProperty("a.", "b.");
        properties.setProperty("b.", "a.");
        properties.setProperty("my.", "b.");
        ExtendedProperties props = ExtendedProperties.createWith(properties);

        try
        {
            props.getSubset("my.", true);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Cyclic definition of property 'b.'.", ex.getMessage());
        }
    }

}
