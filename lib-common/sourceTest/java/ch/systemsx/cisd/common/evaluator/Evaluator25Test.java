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

package ch.systemsx.cisd.common.evaluator;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.jython.evaluator.IJythonEvaluatorFactory;
import ch.systemsx.cisd.common.jython.v25.Jython25EvaluatorFactory;

/**
 * @author Jakub Straszewski
 */
@Test
public class Evaluator25Test extends EvaluatorTest
{
    private IJythonEvaluatorFactory factory;

    @Override
    public IJythonEvaluatorFactory getFactory()
    {
        if (factory == null)
        {
            factory = new Jython25EvaluatorFactory();
        }
        return factory;
    }

}
