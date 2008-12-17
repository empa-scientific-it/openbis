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

import org.hibernate.validator.Validator;

import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;

/**
 * Checks a field annotated with {@link InternalNamespace}.
 * 
 * @author Christian Ribeaud
 */
public final class InternalNamespaceValidator implements Validator<InternalNamespace>, Serializable
{

    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    private boolean internalNamespace;

    //
    // Validator
    //

    public final void initialize(final InternalNamespace annotation)
    {
        this.internalNamespace = annotation.value();
    }

    public final boolean isValid(final Object value)
    {
        return ((Boolean) value) == internalNamespace;
    }

}
