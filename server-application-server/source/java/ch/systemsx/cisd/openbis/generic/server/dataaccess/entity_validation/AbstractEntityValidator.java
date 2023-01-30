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

import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.INonAbstractEntityAdapter;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.JythonEntityValidationCalculator.IValidationRequestDelegate;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.entity_validation.api.IEntityValidator;

/**
 * @author Pawel Glyzewski
 */
public abstract class AbstractEntityValidator implements IEntityValidator
{
    protected IValidationRequestDelegate<INonAbstractEntityAdapter> validationRequestedDelegate;

    @Override
    public final void init(@SuppressWarnings("hiding") IValidationRequestDelegate<INonAbstractEntityAdapter> validationRequestedDelegate)
    {
        this.validationRequestedDelegate = validationRequestedDelegate;
    }

    protected final void requestValidation(INonAbstractEntityAdapter entity)
    {
        validationRequestedDelegate.requestValidation(entity);
    }
}
