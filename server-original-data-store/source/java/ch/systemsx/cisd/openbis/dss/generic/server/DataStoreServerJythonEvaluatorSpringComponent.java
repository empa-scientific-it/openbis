/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.generic.server;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.jython.evaluator.IJythonEvaluatorFactory;
import ch.systemsx.cisd.common.jython.evaluator.JythonEvaluatorSpringComponent;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Jython27ClassLoader;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Jython27FactoriesProvider;


/**
 * Jython evaluator component for DSS. Jython27EvaulatorFactory is loaded by {@link Jython27ClassLoader}.
 *
 * @author Franz-Josef Elmer
 */
public class DataStoreServerJythonEvaluatorSpringComponent extends JythonEvaluatorSpringComponent
{
    private final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DataStoreServerJythonEvaluatorSpringComponent.class);

    public DataStoreServerJythonEvaluatorSpringComponent(ExposablePropertyPlaceholderConfigurer propertyConfigurer)
    {
        super(propertyConfigurer);
    }

    @Override
    protected IJythonEvaluatorFactory createJython27EvaluatorFactory()
    {
        IJythonEvaluatorFactory evaluatorFactory = Jython27FactoriesProvider.getEvaluatorFactory();
        operationLog.info("Class loader of " + evaluatorFactory + ": " + evaluatorFactory.getClass().getClassLoader());
        return evaluatorFactory;
    }

}
