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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISpaceUpdates;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * Business object of a data space. Holds an instance of {@link SpacePE}.
 * 
 * @author Christian Ribeaud
 */
public interface ISpaceBO extends IEntityBusinessObject
{

    /**
     * Defines a new space of specified code for the home database instance. After invocation of this method {@link IBusinessObject#save()} should be
     * invoked to store the new group in the <i>Data Access Layer</i>.
     * 
     * @throws UserFailureException if <code>group</code> does already exist.
     */
    public void define(String spaceCode, String descriptionOrNull) throws UserFailureException;

    /**
     * Loads a space described by identifier from Database Layer.
     * 
     * @throws UserFailureException if <code>groupIdentifier</code> does not describe existing group.
     */
    public void load(SpaceIdentifier spaceIdentifier) throws UserFailureException;

    /**
     * Returns the space or <code>null</code>.
     */
    public SpacePE getSpace() throws UserFailureException;

    /**
     * Updates the space.
     */
    public void update(ISpaceUpdates updates);

    /**
     * Deletes space for specified reason.
     * 
     * @param spaceId space technical identifier
     * @throws UserFailureException if space with given technical identifier is not found.
     */
    public void deleteByTechId(TechId spaceId, String reason);

}
