/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server;

import ch.systemsx.cisd.common.jython.evaluator.Evaluator;
import ch.systemsx.cisd.common.jython.evaluator.IJythonEvaluator;
import ch.systemsx.cisd.openbis.generic.shared.IJythonEvaluatorPool;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IAtomicEvaluation;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IEvaluationRunner;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.ManagedPropertyFunctions;

/**
 * @author anttil
 */
public class TestJythonEvaluatorPool implements IJythonEvaluatorPool
{
    @Override
    public IEvaluationRunner getRunner(final String expression, final Class<?> clazz, final String script)
    {
        return new IEvaluationRunner()
            {
                private IJythonEvaluator evaluator;

                {
                    Class<?> importClass = clazz;
                    if (importClass == null)
                    {
                        importClass = Math.class;
                    }
                    this.evaluator = Evaluator.getFactory().create(expression, importClass, script);
                }

                @Override
                public <T> T evaluate(IAtomicEvaluation<T> evaluation)
                {
                    return evaluation.evaluate(evaluator);
                }

            };
    }

    @Override
    public IEvaluationRunner getManagedPropertiesRunner(String script)
    {
        return getRunner("", ManagedPropertyFunctions.class, script);
    }
}
