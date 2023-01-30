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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.entity_validation;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.JythonEntityValidationCalculator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.entity_validation.api.IEntityValidator;
import ch.systemsx.cisd.openbis.generic.shared.IJythonEvaluatorPool;
import ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins.api.IEntityAdaptor;

/**
 * Jython implementation of entity validators.
 * 
 * @author Pawel Glyzewski
 */
public class JythonEntityValidator extends AbstractEntityValidator implements IEntityValidator
{
    private final String script;

    private final IJythonEvaluatorPool jythonEvaluatorPool;

    public JythonEntityValidator(String script, IJythonEvaluatorPool jythonEvaluatorPool)
    {
        this.script = script;
        this.jythonEvaluatorPool = jythonEvaluatorPool;
    }

    @Override
    public String validate(IEntityAdaptor entity, boolean isNew)
    {
        JythonEntityValidationCalculator calculator =
                JythonEntityValidationCalculator.create(script, validationRequestedDelegate, jythonEvaluatorPool);
        return calculator.eval(entity, isNew);
    }
}
