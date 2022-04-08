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

import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.InputSource;

import ch.systemsx.cisd.common.exceptions.ExceptionUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.customcolumn.core.CustomColumnMethod;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.customcolumn.extension.link.CustomColumnLinkParam;
import ch.systemsx.cisd.openbis.generic.shared.calculator.customcolumn.CustomColumnJSONBuilder;
import ch.systemsx.cisd.openbis.generic.shared.calculator.customcolumn.CustomColumnJSONServerData;

/**
 * Set of standard functions used in jython expressions.
 * <p>
 * All public methods of this class are part of the Filter/Calculated Column API.
 * 
 * @author Franz-Josef Elmer
 */
public final class StandardFunctions
{
    static final Double DOUBLE_DEFAULT_VALUE = Double.NaN;

    static final Integer INTEGER_DEFAULT_VALUE = Integer.valueOf(Integer.MIN_VALUE);

    private static final XPath XPATH = XPathFactory.newInstance().newXPath();

    /**
     * Returns <code>true</code> if the specified string matches the specified regular expression.
     */
    public static boolean matches(String regex, String string)
    {
        return Pattern.matches(regex, string);
    }

    /**
     * Evaluates the specified XPath expression onto the specified XML string.
     */
    public static String evalXPath(String xPath, String xmlString)
    {
        try
        {
            XPathExpression expression = XPATH.compile(xPath);
            String str = StringEscapeUtils.unescapeXml(xmlString);
            String result = expression.evaluate(new InputSource(new StringReader(str)));
            return result;
        } catch (XPathExpressionException ex)
        {
            return "ERROR " + ExceptionUtils.getEndOfChain(ex).toString();
        }
    }

    /**
     * Returns the specified value as an integer. Returns the smallest integer if <code>value</code> is <code>null</code> or its trimmed
     * <code>toString()</code> representation is an empty string.
     * 
     * @throws NumberFormatException if <code>value.toString()</code> can not be parsed as an integer.
     */
    public static Integer toInt(Object value)
    {
        return toInt(value, INTEGER_DEFAULT_VALUE);
    }

    /**
     * Returns the specified value as an integer. Returns <code>defaultValue</code> if <code>value</code> is <code>null</code> or its trimmed
     * <code>toString()</code> representation is an empty string.
     * 
     * @throws NumberFormatException if <code>value.toString()</code> can not be parsed as an integer.
     */
    public static Integer toInt(Object value, Integer defaultValue)
    {
        if (value instanceof Number)
        {
            Number number = (Number) value;
            return number.intValue();
        }
        return isBlank(value) ? defaultValue : Integer.valueOf(value.toString());
    }

    /**
     * Returns the specified value as a floating-point number. Returns the smallest floating-point number if <code>value</code> is <code>null</code>
     * or its trimmed <code>toString()</code> representation is an empty string.
     * 
     * @throws NumberFormatException if <code>value.toString()</code> can not be parsed as a floating-point number.
     */
    public static Double toFloat(Object value)
    {
        return toFloat(value, DOUBLE_DEFAULT_VALUE);
    }

    /**
     * Returns the specified value as a floating-point number. Returns <code>defaultValue</code> if <code>value</code> is <code>null</code> or its
     * trimmed <code>toString()</code> representation is an empty string.
     * 
     * @throws NumberFormatException if <code>value.toString()</code> can not be parsed as a floating-point number.
     */
    public static Double toFloat(Object value, Double defaultValue)
    {
        if (value instanceof Number)
        {
            Number number = (Number) value;
            return number.doubleValue();
        }
        return isBlank(value) ? defaultValue : Double.valueOf(value.toString());
    }

    /**
     * Returns <code>thenValue</code> if <code>condition == true</code> otherwise <code>elseValue</code> is returned.
     * 
     * @return <code>elseValue</code> if <code>condition == null</code>.
     */
    public static Object choose(Boolean condition, Object thenValue, Object elseValue)
    {
        return condition != null && condition.booleanValue() ? thenValue : elseValue;
    }

    /**
     * Calculates the sum of the specified values. Blank strings or <code>null</code> values in the list are ignored.
     * 
     * @throws NumberFormatException if an element can not be parsed as a floating-point number.
     * @throws IllegalArgumentException if the list is empty.
     */
    public static Double sum(List<Object> values)
    {
        List<Double> array = toDoubleArray(values);
        String functionName = "sum";
        assertNotEmpty(array, functionName);
        double sum = 0.0;
        for (double value : array)
        {
            sum += value;
        }
        return sum;
    }

    /**
     * Calculates the mean of the specified values. Blank strings or <code>null</code> values in the list are ignored.
     * 
     * @throws NumberFormatException if an element can not be parsed as a floating-point number.
     * @throws IllegalArgumentException if the list is empty.
     */
    public static Double avg(List<Object> values)
    {
        List<Double> array = toDoubleArray(values);
        assertNotEmpty(array, "avg");
        double sum = 0.0;
        for (double value : array)
        {
            sum += value;
        }
        return sum / array.size();
    }

    /**
     * Calculates the standard deviation of the specified values. Blank strings or <code>null</code> values in the list are ignored.
     * 
     * @throws NumberFormatException if an element can not be parsed as a floating-point number.
     * @throws IllegalArgumentException if the list is empty.
     */
    public static Double stdev(List<Object> values)
    {
        List<Double> array = toDoubleArray(values);
        assertNotEmpty(array, "stdev");
        double s1 = 0.0;
        double s2 = 0.0;
        for (double value : array)
        {
            s1 += value;
            s2 += value * value;
        }
        int n = array.size();
        double m1 = s1 / n;
        double m2 = s2 / n;
        return Math.sqrt(Math.max(0, m2 - m1 * m1));
    }

    /**
     * Calculates the median of the specified values. Blank strings or <code>null</code> values in the list are ignored.
     * 
     * @throws NumberFormatException if an element can not be parsed as a floating-point number.
     * @throws IllegalArgumentException if the list is empty.
     */
    public static Double median(List<Object> values)
    {
        List<Double> array = toDoubleArray(values);
        assertNotEmpty(array, "median");
        Collections.sort(array);
        int i = array.size() / 2;
        return array.size() % 2 == 0 ? (array.get(i - 1) + array.get(i)) / 2 : array.get(i);
    }

    /**
     * Calculates the minimum of the specified values. Blank strings or <code>null</code> values in the list are ignored.
     * 
     * @throws NumberFormatException if an element can not be parsed as a floating-point number.
     * @throws IllegalArgumentException if the list is empty.
     */
    public static Double min(List<Object> values)
    {
        List<Double> array = toDoubleArray(values);
        assertNotEmpty(array, "min");
        Collections.sort(array);
        return array.get(0);
    }

    /**
     * Calculates the maximum of the specified values. Blank strings or <code>null</code> values in the list are ignored.
     * 
     * @throws NumberFormatException if an element can not be parsed as a floating-point number.
     * @throws IllegalArgumentException if the list is empty.
     */
    public static Double max(List<Object> values)
    {
        List<Double> array = toDoubleArray(values);
        assertNotEmpty(array, "max");
        Collections.sort(array);
        return array.get(array.size() - 1);
    }

    /**
     * Like sum, but returns the default value if the list is empty.
     * 
     * @throws NumberFormatException if an element can not be parsed as a floating-point number.
     */
    public static Double sumOrDefault(List<Object> values, Double defaultValue)
    {
        if (values.isEmpty())
            return defaultValue;
        return sum(values);
    }

    /**
     * Like avg, but returns the default value if the list is empty.
     * 
     * @throws NumberFormatException if an element can not be parsed as a floating-point number.
     */
    public static Double avgOrDefault(List<Object> values, Double defaultValue)
    {
        if (values.isEmpty())
            return defaultValue;
        return avg(values);
    }

    /**
     * Like stdev, but returns the default value if the list is empty.
     * 
     * @throws NumberFormatException if an element can not be parsed as a floating-point number.
     */
    public static Double stdevOrDefault(List<Object> values, Double defaultValue)
    {
        if (values.isEmpty())
            return defaultValue;
        return stdev(values);
    }

    /**
     * Like median, but returns the default value if the list is empty.
     * 
     * @throws NumberFormatException if an element can not be parsed as a floating-point number.
     */
    public static Double medianOrDefault(List<Object> values, Double defaultValue)
    {
        if (values.isEmpty())
            return defaultValue;
        return median(values);
    }

    /**
     * Like min, but returns the default value if the list is empty.
     * 
     * @throws NumberFormatException if an element can not be parsed as a floating-point number.
     */
    public static Double minOrDefault(List<Object> values, Double defaultValue)
    {
        if (values.isEmpty())
            return defaultValue;
        return min(values);
    }

    /**
     * Like max, but returns the default value if the list is empty.
     * 
     * @throws NumberFormatException if an element can not be parsed as a floating-point number.
     */
    public static Double maxOrDefault(List<Object> values, Double defaultValue)
    {
        if (values.isEmpty())
            return defaultValue;
        return max(values);
    }

    /** Returns current date. */
    public static Date currentDate()
    {
        return new Date();
    }

    /** Returns the given date as a formatted String */
    public static String getDateAsString(Date date)
    {
        DateFormat d = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return d.format(date);
    }

    public static String createLink(String linkText, String linkUrl)
    {
        CustomColumnJSONServerData data = new CustomColumnJSONServerData(CustomColumnMethod.LINK);
        data.addParam(CustomColumnLinkParam.LINK_TEXT.name(), linkText);
        data.addParam(CustomColumnLinkParam.LINK_URL.name(), linkUrl);
        return CustomColumnJSONBuilder.build(data);
    }

    private static boolean isBlank(Object value)
    {
        return value == null || value.toString().trim().length() == 0;
    }

    private static List<Double> toDoubleArray(List<Object> values)
    {
        ArrayList<Double> list = new ArrayList<Double>();
        for (Object value : values)
        {
            if (value == null)
            {
                continue;
            }
            if (value instanceof Number)
            {
                Number number = (Number) value;
                list.add(number.doubleValue());
            } else
            {
                String stringValue = value.toString();
                if (StringUtils.isBlank(stringValue) == false)
                {
                    list.add(Double.valueOf(stringValue));
                }
            }
        }
        return list;
    }

    private static void assertNotEmpty(List<Double> values, String functionName)
    {
        if (values.isEmpty())
        {
            throw new IllegalArgumentException("Argument of function '" + functionName
                    + "' is an empty array.");
        }
    }

    private StandardFunctions()
    {
    }

}