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

package ch.systemsx.cisd.openbis.generic.shared.calculator;

import static ch.systemsx.cisd.openbis.generic.shared.calculator.StandardFunctions.DOUBLE_DEFAULT_VALUE;
import static ch.systemsx.cisd.openbis.generic.shared.calculator.StandardFunctions.INTEGER_DEFAULT_VALUE;
import static ch.systemsx.cisd.openbis.generic.shared.calculator.StandardFunctions.avg;
import static ch.systemsx.cisd.openbis.generic.shared.calculator.StandardFunctions.avgOrDefault;
import static ch.systemsx.cisd.openbis.generic.shared.calculator.StandardFunctions.choose;
import static ch.systemsx.cisd.openbis.generic.shared.calculator.StandardFunctions.max;
import static ch.systemsx.cisd.openbis.generic.shared.calculator.StandardFunctions.maxOrDefault;
import static ch.systemsx.cisd.openbis.generic.shared.calculator.StandardFunctions.median;
import static ch.systemsx.cisd.openbis.generic.shared.calculator.StandardFunctions.medianOrDefault;
import static ch.systemsx.cisd.openbis.generic.shared.calculator.StandardFunctions.min;
import static ch.systemsx.cisd.openbis.generic.shared.calculator.StandardFunctions.minOrDefault;
import static ch.systemsx.cisd.openbis.generic.shared.calculator.StandardFunctions.stdev;
import static ch.systemsx.cisd.openbis.generic.shared.calculator.StandardFunctions.stdevOrDefault;
import static ch.systemsx.cisd.openbis.generic.shared.calculator.StandardFunctions.toFloat;
import static ch.systemsx.cisd.openbis.generic.shared.calculator.StandardFunctions.toInt;

import java.util.Arrays;
import java.util.Date;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * @author Franz-Josef Elmer
 */
public class StandardFunctionsTest extends AssertJUnit
{
    @Test
    public void testGetDateAsString()
    {
        Date date = new Date(1382785041979L);
        assertEquals("2013-10-26 12:57:21", StandardFunctions.getDateAsString(date));
    }

    @Test
    public void testCurrentDate()
    {
        Date d1 = new Date();
        Date d2 = StandardFunctions.currentDate();
        Date d3 = new Date();
        assertTrue(d1.getTime() + ">" + d2.getTime(), d1.getTime() <= d2.getTime());
        assertTrue(d2.getTime() + ">" + d3.getTime(), d2.getTime() <= d3.getTime());
    }

    @Test
    public void testEvalXPath()
    {
        String xPath = "//property[@name='greetings']";
        String xmlString = "<root><property name='greetings'>hello</property></root>";
        assertEquals("hello", StandardFunctions.evalXPath(xPath, xmlString));
    }

    @Test
    public void testEvalXPathWithInvalidXPath()
    {
        String xPath = "//property[@name='greetings')";
        String xmlString = "<root><property name='greetings'>hello</property></root>";
        assertEquals("ERROR javax.xml.transform.TransformerException: Expected ], but found: )",
                StandardFunctions.evalXPath(xPath, xmlString));
    }

    @Test
    public void testEvalXPathWithNotWellFormedXML()
    {
        String xPath = "//property[@name='greetings']";
        String xmlString = "<root><property name='greetings'>hello</property><root>";
        assertTrue("message check",
                StandardFunctions.evalXPath(xPath, xmlString).contains("XML document structures must start and end within the same entity."));
    }

    @Test
    public void testToIntegerWithNullArgument()
    {
        assertEquals(INTEGER_DEFAULT_VALUE, toInt(null));
        assertEquals(42, toInt(null, 42).intValue());
    }

    @Test
    public void testToIntegerWithBlankArgument()
    {
        assertEquals(INTEGER_DEFAULT_VALUE, toInt("  "));
        assertEquals(42, toInt("  ", 42).intValue());
    }

    @Test
    public void testToIntegerWithNumberArgument()
    {
        assertEquals(42, toInt(42).intValue());
        assertEquals(42, toInt(42, 4711).intValue());
    }

    @Test
    public void testToIntegerWithParsableArgument()
    {
        assertEquals(42, toInt("42").intValue());
        assertEquals(42, toInt("42", 4711).intValue());
    }

    @Test
    public void testToIntegerWithUnParsableArgument()
    {
        try
        {
            toInt("abc");
            fail("NumberFormatException expected");
        } catch (NumberFormatException ex)
        {
            assertEquals("For input string: \"abc\"", ex.getMessage());
        }
    }

    @Test
    public void testToFloatWithNullArgument()
    {
        assertEquals(DOUBLE_DEFAULT_VALUE, toFloat(null));
        assertEquals(42.5, toFloat(null, 42.5).doubleValue());
    }

    @Test
    public void testToFloatWithBlankArgument()
    {
        assertEquals(DOUBLE_DEFAULT_VALUE, toFloat("  "));
        assertEquals(42.5, toFloat("  ", 42.5).doubleValue());
    }

    @Test
    public void testToFloatWithNumberArgument()
    {
        assertEquals(42.5, toFloat(42.5).doubleValue());
        assertEquals(42.5, toFloat(42.5, 4711.0).doubleValue());
    }

    @Test
    public void testToFloatWithParsableArgument()
    {
        assertEquals(42.5, toFloat("42.5").doubleValue());
        assertEquals(42.5, toFloat("42.5", 4711.0).doubleValue());
    }

    @Test
    public void testToFloatWithUnParsableArgument()
    {
        try
        {
            toFloat("abc");
            fail("NumberFormatException expected");
        } catch (NumberFormatException ex)
        {
            assertEquals("For input string: \"abc\"", ex.getMessage());
        }
    }

    @Test
    public void testIfThenElse()
    {
        assertEquals("no", choose(null, "yes", "no"));
        assertEquals("no", choose(false, "yes", "no"));
        assertEquals("yes", choose(true, "yes", "no"));
    }

    @Test
    public void testAvg()
    {
        assertEquals(1.5, avg(Arrays.<Object> asList(1.5)));
        assertEquals(5.0, avg(Arrays.<Object> asList(1, 4, 10)));
        assertEquals(5.5, avg(Arrays.<Object> asList(null, 1, 10)));
        assertEquals(5.5, avg(Arrays.<Object> asList(" ", 1, "10")));
        try
        {
            avg(Arrays.<Object> asList());
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Argument of function 'avg' is an empty array.", ex.getMessage());
        }
        try
        {
            avg(Arrays.<Object> asList("a"));
            fail("NumberFormatException expected");
        } catch (NumberFormatException ex)
        {
            // ignored
        }
    }

    @Test
    public void testStdev()
    {
        assertEquals(0.0, stdev(Arrays.<Object> asList(1.5)));
        assertEquals(2.0, stdev(Arrays.<Object> asList(2, 4, 4, 4, 5, 5, 7, 9)));
        assertEquals(0.0, stdev(Arrays.<Object> asList(null, 1)));
        assertEquals(4.5, stdev(Arrays.<Object> asList(" ", 1, "10")));
        try
        {
            stdev(Arrays.<Object> asList());
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Argument of function 'stdev' is an empty array.", ex.getMessage());
        }
        try
        {
            stdev(Arrays.<Object> asList("a"));
            fail("NumberFormatException expected");
        } catch (NumberFormatException ex)
        {
            // ignored
        }
    }

    @Test
    public void testMedian()
    {
        assertEquals(1.5, median(Arrays.<Object> asList(1.5)));
        assertEquals(4.0, median(Arrays.<Object> asList(4, 1, 10)));
        assertEquals(5.5, median(Arrays.<Object> asList(null, 1, 10)));
        assertEquals(5.5, median(Arrays.<Object> asList(" ", 1, "10")));
        try
        {
            median(Arrays.<Object> asList());
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Argument of function 'median' is an empty array.", ex.getMessage());
        }
        try
        {
            median(Arrays.<Object> asList("a"));
            fail("NumberFormatException expected");
        } catch (NumberFormatException ex)
        {
            // ignored
        }
    }

    @Test
    public void testMin()
    {
        assertEquals(1.5, min(Arrays.<Object> asList(1.5)));
        assertEquals(1.0, min(Arrays.<Object> asList(4, 1, 10)));
        assertEquals(1.0, min(Arrays.<Object> asList(null, 1, 10)));
        assertEquals(1.0, min(Arrays.<Object> asList(" ", 1, "10")));
        try
        {
            min(Arrays.<Object> asList());
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Argument of function 'min' is an empty array.", ex.getMessage());
        }
        try
        {
            min(Arrays.<Object> asList("a"));
            fail("NumberFormatException expected");
        } catch (NumberFormatException ex)
        {
            // ignored
        }
    }

    @Test
    public void testMax()
    {
        assertEquals(1.5, max(Arrays.<Object> asList(1.5)));
        assertEquals(10.0, max(Arrays.<Object> asList(4, 1, 10)));
        assertEquals(10.0, max(Arrays.<Object> asList(null, 1, 10)));
        assertEquals(10.0, max(Arrays.<Object> asList(" ", 1, "10")));
        try
        {
            max(Arrays.<Object> asList());
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Argument of function 'max' is an empty array.", ex.getMessage());
        }
        try
        {
            max(Arrays.<Object> asList("a"));
            fail("NumberFormatException expected");
        } catch (NumberFormatException ex)
        {
            // ignored
        }
    }

    @Test
    public void testAvgOrDefault()
    {
        assertEquals(-47.8, avgOrDefault(Arrays.<Object> asList(), -47.8));
        assertEquals(5.0, avgOrDefault(Arrays.<Object> asList(2, 4, 4, 4, 5, 5, 7, 9), -47.8));
    }

    @Test
    public void testStdevOrDefault()
    {
        assertEquals(384.21, stdevOrDefault(Arrays.<Object> asList(), 384.21));
        assertEquals(2.0, stdevOrDefault(Arrays.<Object> asList(2, 4, 4, 4, 5, 5, 7, 9), 384.21));
    }

    @Test
    public void testMedianOrDefault()
    {
        assertEquals(-47.8, medianOrDefault(Arrays.<Object> asList(), -47.8));
        assertEquals(4.0, medianOrDefault(Arrays.<Object> asList(4, 1, 10), -47.8));
    }

    @Test
    public void testMinOrDefault()
    {
        assertEquals(-47.8, minOrDefault(Arrays.<Object> asList(), -47.8));
        assertEquals(1.0, minOrDefault(Arrays.<Object> asList(4, 1, 10), -47.8));
    }

    @Test
    public void testMaxOrDefault()
    {
        assertEquals(-47.8, maxOrDefault(Arrays.<Object> asList(), -47.8));
        assertEquals(10.0, maxOrDefault(Arrays.<Object> asList(4, 1, 10), -47.8));
    }

}
