/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.dto.hibernate;

import java.io.Serializable;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import ch.systemsx.cisd.openbis.generic.shared.IServer;

/**
 * Checks a field annotated with {@link ManagedInternally}.
 * 
 * @author Christian Ribeaud
 */
public final class ManagedInternallyValidator implements
        ConstraintValidator<ManagedInternally, Boolean>, Serializable
{

    private static final long serialVersionUID = IServer.VERSION;

    private boolean managedInternally;

    //
    // Validator
    //

    @Override
    public final void initialize(final ManagedInternally annotation)
    {
        this.managedInternally = annotation.value();
    }

    @Override
    public final boolean isValid(final Boolean value, ConstraintValidatorContext constraintContext)
    {
        return value == managedInternally;
    }

}
