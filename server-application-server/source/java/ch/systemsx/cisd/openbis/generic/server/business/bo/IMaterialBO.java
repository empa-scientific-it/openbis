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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.material.IMaterialId;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialUpdateDTO;

/**
 * A generic material <i>Business Object</i>.
 * 
 * @author Izabela Adamczyk
 */
public interface IMaterialBO extends IEntityBusinessObject
{

    /**
     * Returns a material found by the given id or null if it does not exist. Does not change the state of this object, especially the result of
     * {@link #getMaterial()}.
     */
    public MaterialPE tryFindByMaterialId(IMaterialId materialId);

    /** Returns the material which has been loaded. */
    MaterialPE getMaterial();

    /**
     * Loads the material by a given identifier.
     */
    public void loadByMaterialIdentifier(MaterialIdentifier identifier);

    /** Adds properties */
    public void enrichWithProperties();

    /**
     * Changes given material. Currently allowed changes: properties.
     */
    public void update(MaterialUpdateDTO materialUpdate);

    /**
     * Deletes material for specified reason.
     * 
     * @param materialId material technical identifier
     * @throws UserFailureException if material with given technical identifier is not found.
     */
    void deleteByTechId(TechId materialId, String reason);

    /**
     * Changes the value of a managed property.
     */
    void updateManagedProperty(IManagedProperty managedProperty);

}
