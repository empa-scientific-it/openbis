/*
 * Copyright 2010 ETH Zuerich, CISD
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

import java.math.BigInteger;

import ch.systemsx.cisd.common.jython.evaluator.Evaluator;
import ch.systemsx.cisd.common.jython.evaluator.EvaluatorException;
import ch.systemsx.cisd.common.jython.evaluator.IJythonEvaluator;
import ch.systemsx.cisd.openbis.generic.shared.basic.PrimitiveValue;

/**
 * @author Piotr Buczek
 */
public class AbstractCalculator
{
    protected static String NEWLINE = "\n";

    public static String importFunctions(Class<?> clazz)
    {
        return "from " + clazz.getCanonicalName() + " import *" + NEWLINE;
    }

    public final static String getBasicInitialScript()
    {
        return importFunctions(StandardFunctions.class) + "def int(x):return toInt(x)" + NEWLINE
                + "def float(x):return toFloat(x)" + NEWLINE;
    }

    protected final IJythonEvaluator evaluator;

    public AbstractCalculator(IJythonEvaluator evaluator)
    {
        this.evaluator = evaluator;
    }

    public PrimitiveValue getTypedResult()
    {
        Object value = evaluator.evalLegacy2_2();
        if (value == null)
        {
            return PrimitiveValue.NULL;
        }
        if (value instanceof Long)
        {
            return new PrimitiveValue((Long) value);
        } else if (value instanceof Double)
        {
            return new PrimitiveValue((Double) value);
        } else
        {
            return new PrimitiveValue(value.toString());
        }
    }

    public boolean evalToBoolean() throws EvaluatorException
    {
        return evaluator.evalToBoolean();
    }

    public int evalToInt() throws EvaluatorException
    {
        return evaluator.evalToInt();
    }

    public BigInteger evalToBigInt() throws EvaluatorException
    {
        return evaluator.evalToBigInt();
    }

    public double evalToDouble() throws EvaluatorException
    {
        return evaluator.evalToDouble();
    }

    public String evalAsString() throws EvaluatorException
    {
        return evaluator.evalAsStringLegacy2_2();
    }

    public Object eval() throws EvaluatorException
    {
        return evaluator.evalLegacy2_2();
    }

}