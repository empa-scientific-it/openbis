/*
 * Copyright 2012 ETH Zuerich, CISD
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

import ch.systemsx.cisd.common.jython.evaluator.EvaluatorException;
import ch.systemsx.cisd.common.jython.evaluator.IJythonEvaluator;
import ch.systemsx.cisd.openbis.generic.shared.IJythonEvaluatorPool;
import ch.systemsx.cisd.openbis.generic.shared.calculator.AbstractCalculator;
import ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins.api.IEntityAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IAtomicEvaluation;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IEvaluationRunner;

/**
 * @author Jakub Straszewski
 */
public class JythonEntityValidationCalculator
{
    private static final String ENTITY_VARIABLE_NAME = "__entity";

    private static final String IS_NEW_ENTITY_VARIABLE_NAME = "__isNewEntity";

    private static final String INVOKE_CALCULATE_EXPR = "validate(" + ENTITY_VARIABLE_NAME + ", "
            + IS_NEW_ENTITY_VARIABLE_NAME + ")";

    private static final String CALCULATOR_VARIABLE = "__calculator";

    private static final String VALIDATION_REQUEST_FUNCTION = "def requestValidation(entity):\n  "
            + CALCULATOR_VARIABLE + ".requestValidation(entity)\n";

    public interface IValidationRequestDelegate<T>
    {
        public void requestValidation(T o);
    }

    private final IEvaluationRunner runner;

    private final IValidationRequestDelegate<Object> wrappedValidationRequestedDelegate;

    /**
     * Creates a calculator for given <code>expression</code>.
     * <p>
     * The script is expected to contain validate method with two parameters: "entity" and "isNewEntity"
     */
    public static JythonEntityValidationCalculator create(String expression,
            final IValidationRequestDelegate<INonAbstractEntityAdapter> validationRequestedDelegate,
            IJythonEvaluatorPool jythonEvaluatorPool)
    {
        String initialScript = AbstractCalculator.getBasicInitialScript();
        initialScript += VALIDATION_REQUEST_FUNCTION;
        initialScript += expression;
        String calculatedExpression = INVOKE_CALCULATE_EXPR;

        return new JythonEntityValidationCalculator(jythonEvaluatorPool.getRunner(
                calculatedExpression, Math.class, initialScript), validationRequestedDelegate);

    }

    public JythonEntityValidationCalculator(IEvaluationRunner runner,
            final IValidationRequestDelegate<INonAbstractEntityAdapter> validationRequested)
    {
        this.runner = runner;

        // wrap the request validation with argument checking, so that the implementators of the
        // interface can focus on logic
        wrappedValidationRequestedDelegate = new IValidationRequestDelegate<Object>()
            {
                @Override
                public void requestValidation(Object entity)
                {
                    if (entity == null)
                    {
                        return;
                    }

                    if (false == entity instanceof INonAbstractEntityAdapter)
                    {
                        throw new IllegalArgumentException(
                                "Trying to force the validation of an object of invalid type "
                                        + entity.getClass());
                    }
                    validationRequested.requestValidation((INonAbstractEntityAdapter) entity);
                }
            };
    }

    public String eval(final IEntityAdaptor entity, final boolean isNewEntity)
            throws EvaluatorException
    {
        return runner.evaluate(new IAtomicEvaluation<String>()
            {
                @Override
                public String evaluate(IJythonEvaluator evaluator)
                {
                    evaluator.set(CALCULATOR_VARIABLE, wrappedValidationRequestedDelegate);
                    evaluator.set(ENTITY_VARIABLE_NAME, entity);
                    evaluator.set(IS_NEW_ENTITY_VARIABLE_NAME, isNewEntity);
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
