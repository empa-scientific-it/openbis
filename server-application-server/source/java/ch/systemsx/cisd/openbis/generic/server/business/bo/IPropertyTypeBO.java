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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IPropertyTypeUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;

/**
 * Business object of a property type. Holds an instance of {@link PropertyTypePE}.
 * 
 * @author Christian Ribeaud
 */
public interface IPropertyTypeBO extends IBusinessObject
{

    /**
     * Defines a new property type.
     * <p>
     * After invocation of this method {@link IBusinessObject#save()} should be invoked to store the new property type in the <i>Data Access
     * Layer</i>.
     * </p>
     * 
     * @throws UserFailureException if <var>propertyType</var> does already exist.
     */
    public void define(final PropertyType propertyType) throws UserFailureException;

    /**
     * Returns the loaded {@link PropertyTypePE}.
     */
    public PropertyTypePE getPropertyType();

    /**
     * Updates the property type.
     */
    public void update(IPropertyTypeUpdates updates);

    /**
     * Deletes property type for specified reason.
     * 
     * @param propertyTypeId property type technical identifier
     * @throws UserFailureException if property type with given technical identifier is not found.
     */
    void deleteByTechId(TechId propertyTypeId, String reason);

}
