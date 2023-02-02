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

import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanInitializationException;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.jython.v25.Jython25EvaluatorFactory;
import ch.systemsx.cisd.common.jython.v27.Jython27EvaluatorFactory;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;

/**
 * @author Jakub Straszewski
 */
public class JythonEvaluatorSpringComponent
{
    public static final String JYTHON_VERSION_KEY = "jython-version";

    @Private
    final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            JythonEvaluatorSpringComponent.class);

    public JythonEvaluatorSpringComponent(ExposablePropertyPlaceholderConfigurer propertyConfigurer)
    {
        this(propertyConfigurer.getResolvedProps());
    }

    public JythonEvaluatorSpringComponent(Properties resolvedProps)
    {
        String jythonVersion = resolvedProps.getProperty(JYTHON_VERSION_KEY);
        if ("2.7".equals(jythonVersion))
        {
            Evaluator.setFactory(createJython27EvaluatorFactory());
        } else if ("2.5".equals(jythonVersion))
        {
            Evaluator.setFactory(createJython25EvaluatorFactory());
        } else
        {
            String msg =
                    "The jython-version property must be specified in service.properties - "
                            + "possible values are 2.5 and 2.7. Since openBIS version 16.04 recommended jython version is 2.7. "
                            + "There might be compatibility issues. For details see page 'Jython Version for Various Plugins' "
                            + "in openBIS documentation.";
            operationLog.error(msg);
            throw new BeanInitializationException(msg);
        }
    }

    protected IJythonEvaluatorFactory createJython25EvaluatorFactory()
    {
        return new Jython25EvaluatorFactory();
    }

    protected IJythonEvaluatorFactory createJython27EvaluatorFactory()
    {
        return new Jython27EvaluatorFactory();
    }
}
