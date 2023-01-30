/*
 * Copyright 2007 ETH Zuerich, CISD
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
import java.util.Map;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterialWithType;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialUpdateDTO;

/**
 * Read-only table for materials. Holds a collection of instances of {@link MaterialPE}.
 * 
 * @author Izabela Adamczyk
 */
public interface IMaterialTable
{

    /** Returns the loaded {@link MaterialPE}. */
    public List<MaterialPE> getMaterials();

    /**
     * Defines new materials of specified type.<br>
     * Calls of this method cannot be mixed with calls to {@link #update}.
     */
    public void add(List<NewMaterial> newMaterials, MaterialTypePE materialTypePE);

    /**
     * Defines new materials with mixed type, given the cache of the {@link MaterialTypePE}'s. Calls of this method cannot be mixed with calls to
     * {@link #update}.
     */
    public void add(List<NewMaterialWithType> newMaterials, Map<String, MaterialTypePE> materialTypePE);

    /**
     * Changes given materials. Currently allowed changes: properties.<br>
     * Calls of this method cannot be mixed with calls to {@link #add}.
     */
    public void update(List<MaterialUpdateDTO> materialsUpdate);

    /**
     * Saves new materials in the database.
     */
    public void save();

    /**
     * Deletes materials for specified reason.
     * 
     * @param materialIds sample technical identifiers
     * @throws UserFailureException if one of the materials can not be deleted.
     */
    public void deleteByTechIds(List<TechId> materialIds, String reason)
            throws UserFailureException;

}
