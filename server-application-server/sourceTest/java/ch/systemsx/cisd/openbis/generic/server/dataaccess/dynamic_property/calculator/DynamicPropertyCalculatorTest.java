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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator;

import java.util.Arrays;
import java.util.Collection;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.jython.evaluator.EvaluatorException;
import ch.systemsx.cisd.openbis.generic.server.TestJythonEvaluatorPool;
import ch.systemsx.cisd.openbis.generic.shared.IJythonEvaluatorPool;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins.api.IEntityAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins.api.IEntityPropertyAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.util.XmlUtilsTest;

/**
 * @author Piotr Buczek
 */
public class DynamicPropertyCalculatorTest extends AssertJUnit
{

    @Test
    public void testGetEntityCode()
    {
        final String entityCode1 = "ecode1";
        final String entityCode2 = "ecode2";
        final JythonDynamicPropertyCalculator calculator =
                JythonDynamicPropertyCalculator.create("entity.code()", getPool());

        assertEquals(entityCode1, calculator.eval(createEntity(entityCode1, null)));

        assertEquals(entityCode2, calculator.eval(createEntity(entityCode2, null)));
    }

    @Test
    public void testGetEntityPropertyValue()
    {
        final JythonDynamicPropertyCalculator calculator =
                JythonDynamicPropertyCalculator.create("entity.propertyValue('p2')", getPool());

        final String entityCode = "ecode";

        IEntityPropertyAdaptor p1 = createProperty("P1", "v1");
        IEntityPropertyAdaptor p21 = createProperty("P2", "v21");
        IEntityPropertyAdaptor p22 = createProperty("P2", "v22");

        assertEquals("", calculator.eval(createEntity(entityCode, null)));

        assertEquals("",
                calculator.eval(createEntity(entityCode, Arrays.asList(new IEntityPropertyAdaptor[]
                { p1 })))); // non-existent property

        assertEquals(p21.valueAsString(),
                calculator.eval(createEntity(entityCode, Arrays.asList(new IEntityPropertyAdaptor[]
                { p1, p21 }))));

        assertEquals(p22.valueAsString(),
                calculator.eval(createEntity(entityCode, Arrays.asList(new IEntityPropertyAdaptor[]
                { p1, p22 }))));
    }

    @Test
    public void testGetEntityPropertyRenderedValue()
    {
        final JythonDynamicPropertyCalculator normalPropertyCalculator =
                JythonDynamicPropertyCalculator.create("entity.propertyRendered('normalProperty')", getPool());
        final JythonDynamicPropertyCalculator xmlPropertyCalculator =
                JythonDynamicPropertyCalculator.create("entity.propertyRendered('xmlProperty')", getPool());

        final String entityCode = "ecode";

        IEntityPropertyAdaptor normalProperty = createProperty("normalProperty", "normalValue");
        IEntityPropertyAdaptor xmlProperty =
                createXmlProperty("xmlProperty", XmlUtilsTest.SIMPLE_XML, XmlUtilsTest.SIMPLE_XSLT);

        assertEquals(
                "normalValue",
                normalPropertyCalculator.eval(createEntity(entityCode,
                        Arrays.asList(new IEntityPropertyAdaptor[]
                        { normalProperty, xmlProperty }))));

        assertEquals(
                XmlUtilsTest.SIMPLE_XML_TRANSFORMED,
                xmlPropertyCalculator.eval(createEntity(entityCode,
                        Arrays.asList(new IEntityPropertyAdaptor[]
                        { normalProperty, xmlProperty }))));
    }

    @Test
    public void testEvaluateMultilineExpression()
    {
        final String expression = "def calculate():\n" + "\treturn entity.code()";
        final String entityCode = "ecode";
        final JythonDynamicPropertyCalculator calculator =
                JythonDynamicPropertyCalculator.create(expression, getPool());

        assertEquals(entityCode, calculator.eval(createEntity(entityCode, null)));
    }

    @Test
    public void testEvaluateMultilineExpressionFailsWithNoCalculateFunction()
    {
        final String expression = "def calc():\n" + "\treturn entity.code()";
        final String entityCode = "ecode";
        final JythonDynamicPropertyCalculator calculator =
                JythonDynamicPropertyCalculator.create(expression, getPool());

        try
        {
            calculator.eval(createEntity(entityCode, null));
            fail("expected EvaluatorException");
        } catch (EvaluatorException e)
        {
            final String expectedMsg =
                    "Error occurred in line 1 of the script when evaluating 'calculate()': "
                            + "NameError: name 'calculate' is not defined";
            assertEquals("expected exception message: " + expectedMsg, expectedMsg, e.getMessage());
        }
    }

    @Test
    public void testMaterialFunction()
    {
        final String code = "CODE";
        final String typeCode = "TYPE";

        final JythonDynamicPropertyCalculator calculator =
                JythonDynamicPropertyCalculator.create("material('" + code + "', '" + typeCode
                        + "')", getPool());
        assertEquals(MaterialIdentifier.print(code, typeCode), calculator.eval(null));
    }

    private static IEntityAdaptor createEntity(final String code,
            final Collection<IEntityPropertyAdaptor> properties)
    {
        return new AbstractEntityAdaptor(code, properties);
    }

    private static IEntityPropertyAdaptor createProperty(final String propertyTypeCode,
            final String value)
    {
        return new BasicPropertyAdaptor(propertyTypeCode, value);
    }

    private static IEntityPropertyAdaptor createXmlProperty(final String propertyTypeCode,
            final String value, final String xmlTransformation)
    {
        return new XmlPropertyAdaptor(propertyTypeCode, value, xmlTransformation);
    }

    public IJythonEvaluatorPool getPool()
    {
        return new TestJythonEvaluatorPool();
    }
}
