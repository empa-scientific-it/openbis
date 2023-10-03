/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;

import java.io.Serializable;

/**
 * A property value validator.
 * 
 * @author Christian Ribeaud
 */
public interface IPropertyValueValidator
{
    /**
     * Validates given <var>value</var> against given {@link PropertyTypePE} and returns the validated value as <code>String</code>.
     * 
     * @return the validated value. It does not implicitly equal given <var>value</var>
     */
    public Serializable validatePropertyValue(final PropertyTypePE propertyType, final Serializable value)
            throws UserFailureException;

}
