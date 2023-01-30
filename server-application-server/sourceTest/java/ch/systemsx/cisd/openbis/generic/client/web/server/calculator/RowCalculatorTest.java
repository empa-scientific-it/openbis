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

package ch.systemsx.cisd.openbis.generic.client.web.server.calculator;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.jython.evaluator.EvaluatorException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ParameterWithValue;

/**
 * @author Franz-Josef Elmer
 */
public class RowCalculatorTest extends AssertJUnit
{
    private static final class MockDataProvider implements ITableDataProvider
    {
        private final String expectedColumnID;

        MockDataProvider(String expectedColumnID)
        {
            this.expectedColumnID = expectedColumnID;
        }

        @Override
        public List<List<? extends Comparable<?>>> getRows()
        {
            return null;
        }

        @Override
        public Comparable<?> getValue(String columnID, List<? extends Comparable<?>> rowValues)
        {
            assertEquals(expectedColumnID, columnID);
            assertEquals(1, rowValues.size());
            return rowValues.get(0);
        }

        @Override
        public Collection<String> getAllColumnIDs()
        {
            return null;
        }

        @Override
        public List<String> getAllColumnTitles()
        {
            return null;
        }

        @Override
        public String tryToGetProperty(String columnID, String key)
        {
            return null;
        }
    }

    private ITableDataProvider dataProvider;

    @BeforeMethod
    public void setUp()
    {
        dataProvider = new MockDataProvider("VALUE");
    }

    @Test
    public void testEvalToDoubleWithStrangeParameterName()
    {
        Set<ParameterWithValue> parameters = createParameters("f.*r", "42");
        RowCalculator calculator = createCalculator(parameters, "row.col('VALUE') * ${f.*r}");

        calculator.setRowData(Arrays.asList(2.5));
        assertEquals(105.0, calculator.evalToDouble());

        calculator.setRowData(Arrays.asList(10));
        assertEquals(420, calculator.evalToInt());
    }

    @Test
    public void testEvalToDoubleWithTwoParameters()
    {
        Set<ParameterWithValue> parameters = createParameters("x", "10");
        parameters.addAll(createParameters("y", "1"));
        RowCalculator calculator = createCalculator(parameters, "${x} * row.col('VALUE') + ${y}");

        calculator.setRowData(Arrays.asList(2.5));
        assertEquals(26.0, calculator.evalToDouble());

        calculator.setRowData(Arrays.asList(10));
        assertEquals(101, calculator.evalToInt());
    }

    @Test
    public void testEvalToBoolean()
    {
        Set<ParameterWithValue> parameters = createParameters("x", "42");
        RowCalculator calculator = createCalculator(parameters, "row.col('VALUE') < ${x}");

        calculator.setRowData(Arrays.asList(2.5));
        assertEquals(true, calculator.evalToBoolean());

        calculator.setRowData(Arrays.asList(43));
        assertEquals(false, calculator.evalToBoolean());
    }

    @Test
    public void testEvalToInt()
    {
        Set<ParameterWithValue> parameters = createParameters("x", "42");
        RowCalculator calculator = createCalculator(parameters, "int(row.col('VALUE') * ${x})");

        calculator.setRowData(Arrays.asList(2.5));
        assertEquals(105, calculator.evalToInt());

        calculator.setRowData(Arrays.asList(10));
        assertEquals(420, calculator.evalToInt());
    }

    @Test
    public void testEvalToBigInt()
    {
        Set<ParameterWithValue> parameters = createParameters("x", "10");
        RowCalculator calculator = createCalculator(parameters, "${x} ** int(row.col('VALUE'))");

        calculator.setRowData(Arrays.asList(10));
        assertEquals(new BigInteger("10000000000"), calculator.evalToBigInt());
    }

    @Test
    public void testEvalAsString()
    {
        Set<ParameterWithValue> parameters = createParameters("x", "42");
        RowCalculator calculator = createCalculator(parameters, "str(${x} + row.col('VALUE'))");

        calculator.setRowData(Arrays.asList(10));
        assertEquals("52", calculator.evalAsString());

        calculator.setRowData(Arrays.asList((-10)));
        assertEquals("32", calculator.evalAsString());
    }

    @Test
    public void testMathFunctionAndStandardFunction()
    {
        Set<ParameterWithValue> parameters = createParameters("x", "0.0");
        RowCalculator calculator =
                createCalculator(parameters, "choose(row.col('VALUE') < PI, cos(${x}), ${x})");

        calculator.setRowData(Arrays.asList(3.14));
        assertEquals(1.0, calculator.evalToDouble());

        calculator.setRowData(Arrays.asList(3.15));
        assertEquals(0.0, calculator.evalToDouble());
    }

    @Test
    public void testIntFunctionOverloaded()
    {
        Set<ParameterWithValue> parameters = createParameters("x", "0.0");
        RowCalculator calculator = createCalculator(parameters, "map(int, [${x},'  ',2,2.5,'3'])");

        assertEquals("[0, -2147483648, 2, 2, 3]", calculator.evalAsString());

    }

    @Test
    public void testFloatFunctionOverloaded()
    {
        Set<ParameterWithValue> parameters = createParameters("x", "0.0");
        RowCalculator calculator =
                createCalculator(parameters, "map(float, [${x},'  ',2,2.5,'3'])");

        assertEquals("[0.0, NaN, 2.0, 2.5, 3.0]", calculator.evalAsString());

    }

    @Test
    public void testMinFunctionOverloaded()
    {
        Set<ParameterWithValue> parameters = createParameters("x", "0.0");
        RowCalculator calculator = createCalculator(parameters, "min([${x},None,'  ',-2,'-3'])");

        assertEquals(-3.0, calculator.evalToDouble());

    }

    @Test
    public void testMaxFunctionOverloaded()
    {
        Set<ParameterWithValue> parameters = createParameters("x", "0.0");
        RowCalculator calculator = createCalculator(parameters, "max([${x},None,'  ',-2,'-3'])");

        assertEquals(0.0, calculator.evalToDouble());

    }

    @Test
    public void testMinOrDefaultFunction()
    {
        Set<ParameterWithValue> parameters = createParameters("x", "0.0");
        RowCalculator calculator =
                createCalculator(parameters, "minOrDefault([${x},None,'  ',-2,'-3'])");

        try
        {
            calculator.evalToDouble();
            fail("EvaluatorException expected");
        } catch (EvaluatorException e)
        {
            assertEquals(
                    "Error occurred in line 1 of the script when evaluating 'minOrDefault([0.0,None,'  ',-2,'-3'])': "
                            + "TypeError: minOrDefault(): expected 2 args; got 1", e.getMessage());
        }

        calculator = createCalculator(parameters, "minOrDefault([${x},None,'  ',-2,'-3'], 38.6)");
        assertEquals(-3.0, calculator.evalToDouble());

        calculator = createCalculator(null, "minOrDefault([], 38.6)");
        assertEquals(38.6, calculator.evalToDouble());

    }

    @Test
    public void testMaxOrDefaultFunction()
    {
        Set<ParameterWithValue> parameters = createParameters("x", "0.0");
        RowCalculator calculator =
                createCalculator(parameters, "maxOrDefault([${x},None,'  ',-2,'-3'])");
        try
        {
            calculator.evalToDouble();
            fail("EvaluatorException expected");
        } catch (EvaluatorException e)
        {
            assertEquals(
                    "Error occurred in line 1 of the script when evaluating 'maxOrDefault([0.0,None,'  ',-2,'-3'])': "
                            + "TypeError: maxOrDefault(): expected 2 args; got 1", e.getMessage());
        }

        calculator = createCalculator(parameters, "maxOrDefault([${x},None,'  ',-2,'-3'], 38.6)");
        assertEquals(0.0, calculator.evalToDouble());

        calculator = createCalculator(null, "maxOrDefault([], 38.6)");
        assertEquals(38.6, calculator.evalToDouble());
    }

    private RowCalculator createCalculator(Set<ParameterWithValue> parameters, String expression)
    {
        if (parameters != null)
        {
            return new RowCalculator(dataProvider, expression, parameters);
        } else
        {
            return new RowCalculator(dataProvider, expression);
        }
    }

    private Set<ParameterWithValue> createParameters(String name, String value)
    {
        ParameterWithValue parameter = new ParameterWithValue();
        parameter.setParameter(name);
        parameter.setValue(value);
        return new LinkedHashSet<ParameterWithValue>(Arrays.asList(parameter));
    }
}
