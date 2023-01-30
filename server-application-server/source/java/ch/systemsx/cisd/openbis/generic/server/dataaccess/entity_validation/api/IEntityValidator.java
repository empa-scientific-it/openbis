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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.entity_validation.api;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.INonAbstractEntityAdapter;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.JythonEntityValidationCalculator.IValidationRequestDelegate;
import ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins.api.IEntityAdaptor;

/**
 * This interface needs to be implemented by all entity validators.
 * 
 * @author Pawel Glyzewski
 */
public interface IEntityValidator
{
    /**
     * Before the validation is triggered, the validator is initialized by calling this method.
     * 
     * @param validationRequestedDelegate object responsible for handling requests for entity validation
     */
    public void init(
            IValidationRequestDelegate<INonAbstractEntityAdapter> validationRequestedDelegate);

    /**
     * Main method, that performs actual validation
     * 
     * @param entity entity that needs to be validated
     * @param isNew <code>true</code> if the entity is freshly created, <code>false</code> if the entity was only updated.
     */
    public String validate(IEntityAdaptor entity, boolean isNew);
}
