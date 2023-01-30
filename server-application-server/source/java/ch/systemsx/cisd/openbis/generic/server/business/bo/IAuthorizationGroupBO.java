/*
 * Copyright 2009 ETH Zuerich, CISD
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

import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroupUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.dto.AuthorizationGroupPE;

/**
 * Authorization group business object.
 * 
 * @author Izabela Adamczyk
 */
public interface IAuthorizationGroupBO extends IBusinessObject
{
    /**
     * Defines a authorization group. After invocation of this method {@link IAuthorizationGroupBO#save()} should be invoked to store the new
     * authorization group in the Data Access Layer.
     */
    void define(final NewAuthorizationGroup newAuthorizationGroup) throws UserFailureException;

    /**
     * Deletes the authorization group.
     * 
     * @throws UserFailureException if authorization group with given technical identifier is not found.
     */
    public void deleteByTechId(TechId authGroupId, String reason);

    /**
     * Updates the authorization group.
     */
    public void update(AuthorizationGroupUpdates updates);

    /**
     * Returns loaded authorization group.
     */
    public AuthorizationGroupPE getAuthorizationGroup();

    /**
     * Loads authorization group with given technical id or throws exception if no such group exists.
     */
    public void loadByTechId(TechId authorizatonGroupId);

    /**
     * Adds persons with given codes to the loaded authorization group. Returns a list of users codes not registered in the system.
     */
    public List<String> addPersons(List<String> personsCodes);

    /**
     * Removes persons with given codes from the loaded authorization group.
     */
    public void removePersons(List<String> personsCodes);
}
