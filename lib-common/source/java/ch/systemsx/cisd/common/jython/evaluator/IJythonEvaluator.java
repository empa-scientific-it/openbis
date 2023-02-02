/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.jython.evaluator;

import java.math.BigInteger;
import java.util.Collection;

import org.python.core.PyNone;

import ch.systemsx.cisd.common.jython.evaluator.Evaluator.ReturnType;

/**
 * @author Jakub Straszewski
 */
public interface IJythonEvaluator
{
    /**
     * Returns <code>true</code> if specified function is defined in the script.
     */
    public boolean hasFunction(String functionName);

    /**
     * Evaluates specified function with specified arguments. The arguments are turned into Python Strings if they are Java {@link String} objects.
     * The return value of the function is returned as a Java object or <code>null</code>.
     * 
     * @throws EvaluatorException if evaluation fails.
     */
    public Object evalFunction(String functionName, Object... args);

    /**
     * Sets the variable <var>name</var> to <var>value</var> in the evaluator's name space.
     */
    public void set(String name, Object value);

    public Object get(String name);

    /**
     * Deletes the variable <var>name</var> from the evaluator's name space.
     */
    public void delete(String name);

    /**
     * Returns <code>true</code> if and only if the variable <var>name</var> exists in the evaluator's name space.
     */
    public boolean has(String name);

    /**
     * Returns the {@link ReturnType} of the expression of this evaluator.
     */
    public ReturnType getType();

    /**
     * Evaluates the expression of this evaluator and returns the result. Use this method if you do not know what will be the result type.
     * <p>
     * <i>This is a legacy function to mimic the old Jython 2.2 Evaluator's behavior which will only return Long, Double or String and doesn't know
     * boolean.</i>
     * 
     * @return evaluation result which can be of Long, Double or String type. All other types are converted to String representation except
     *         {@link PyNone} that represents null value and will be converted to <code>null</code>.
     */
    public Object evalLegacy2_2();

    /**
     * Evaluates the expression of this evaluator and returns the result. Use this method if you do not know what will be the result type.
     * 
     * @return evaluation result as translated by the Jython interpreter..
     */
    public Object eval();

    /**
     * Evaluates the expression of this evaluator and returns the result, assuming that the expression has a boolean return type.
     */
    public boolean evalToBoolean();

    /**
     * Evaluates the expression of this evaluator and returns the result, assuming that the expression has an integer return type.
     */
    public int evalToInt();

    /**
     * Evaluates the expression of this evaluator and returns the result, assuming that the expression has a big integer return type.
     */
    public BigInteger evalToBigInt();

    /**
     * Evaluates the expression of this evaluator and returns the result, assuming that the expression has a floating point (double) return type.
     */
    public double evalToDouble();

    /**
     * Evaluates the expression of this evaluator and returns the result as a String. This method can always be called.
     * <p>
     * <i>This is a legacy function to mimic the old Jython 2.2 Evaluator's behavior which first translates to Long and Double and doesn't know
     * boolean.</i>
     * <p>
     * NOTE: null will be returned if expression results in {@link PyNone}
     */
    public String evalAsStringLegacy2_2();

    /**
     * Evaluates the expression of this evaluator and returns the result as a String. This method can always be called.
     * <p>
     * NOTE: null will be returned if expression results in {@link PyNone}
     */
    public String evalAsString();

    public void releaseResources();

    public Collection<String> getGlobalVariables();
}
