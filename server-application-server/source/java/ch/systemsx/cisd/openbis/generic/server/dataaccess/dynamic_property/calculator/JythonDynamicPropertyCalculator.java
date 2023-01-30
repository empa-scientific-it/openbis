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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator;

import ch.systemsx.cisd.common.jython.evaluator.Evaluator;
import ch.systemsx.cisd.common.jython.evaluator.EvaluatorException;
import ch.systemsx.cisd.common.jython.evaluator.IJythonEvaluator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IDynamicPropertyCalculator;
import ch.systemsx.cisd.openbis.generic.shared.IJythonEvaluatorPool;
import ch.systemsx.cisd.openbis.generic.shared.calculator.AbstractCalculator;
import ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins.api.IEntityAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IAtomicEvaluation;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IEvaluationRunner;

/**
 * Calculator for dynamic properties.
 * <p>
 * Variables : <code>entity<code> of type {@link IEntityAdaptor}
 * 
 * @author Piotr Buczek
 * @author Jakub Straszewski
 * @author Pawel Glyzewski
 */
public class JythonDynamicPropertyCalculator implements IDynamicPropertyCalculator
{
    private static final String ENTITY_VARIABLE_NAME = "entity";

    private static final String INVOKE_CALCULATE_EXPR = "calculate()";

    private final IEvaluationRunner runner;

    /**
     * Creates a calculator for given <code>expression</code>.
     * <p>
     * Result of the calculation depends on whether the expression is multiline or not. Returned value will be equal to:
     * <ul>
     * <li>result of calculation of a *single line* expression
     * <li>result of invocation of 'calculate()' function for a *multiline* expression
     * </ul>
     */
    public static JythonDynamicPropertyCalculator create(String expression, IJythonEvaluatorPool pool)
    {
        String calculatedExpression = expression;
        String initialScript = AbstractCalculator.getBasicInitialScript();
        initialScript += AbstractCalculator.importFunctions(DynamicPropertyFunctions.class);
        if (Evaluator.isMultiline(expression))
        {
            initialScript += expression;
            calculatedExpression = INVOKE_CALCULATE_EXPR;
        }
        return new JythonDynamicPropertyCalculator(pool.getRunner(
                calculatedExpression, Math.class, initialScript));

    }

    private JythonDynamicPropertyCalculator(IEvaluationRunner runner)
    {
        this.runner = runner;
    }

    @Override
    public String eval(final IEntityAdaptor entity) throws EvaluatorException
    {
        return runner.evaluate(new IAtomicEvaluation<String>()
            {
                @Override
                public String evaluate(IJythonEvaluator evaluator)
                {
                    evaluator.set(ENTITY_VARIABLE_NAME, entity);
                    return evaluator.evalAsStringLegacy2_2();
                }
            });
    }

    public void checkScriptCompilation()
    {
        runner.evaluate(new IAtomicEvaluation<Void>()
            {
                @Override
                public Void evaluate(IJythonEvaluator evaluator)
                {
                    return null;
                }
            });
    }
}
