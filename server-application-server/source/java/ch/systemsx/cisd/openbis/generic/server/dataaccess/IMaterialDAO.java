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

package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import java.util.Collection;
import java.util.List;

import org.hibernate.Session;
import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.openbis.generic.server.business.bo.materiallister.IMaterialLister;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * <i>Data Access Object</i> for {@link MaterialPE}.
 * 
 * @author Izabela Adamczyk
 */
public interface IMaterialDAO extends IGenericDAO<MaterialPE>
{

    /**
     * Lists materials of given type. Fetches also properties.
     * 
     * @deprecated Because of performance issues use this method only in tests, otherwise use {@link IMaterialLister#list}
     */
    @Deprecated
    public List<MaterialPE> listMaterialsWithProperties(MaterialTypePE type);

    /** Inserts or updates given {@link MaterialPE}s into the database. */
    public void createOrUpdateMaterials(List<MaterialPE> materials);

    /** @return material with the given identifier or null if it is not found. */
    public MaterialPE tryFindMaterial(MaterialIdentifier identifier);

    /** same as {@link #tryFindMaterial(MaterialIdentifier)} but works with given session */
    public MaterialPE tryFindMaterial(Session session, MaterialIdentifier identifier);

    public List<MaterialPE> listMaterialsById(final Collection<Long> ids);

    public List<MaterialPE> listMaterialsByMaterialIdentifier(final Collection<MaterialIdentifier> ids);

    /**
     * Delete materials by specified registrator and reason.
     */
    void delete(List<TechId> materialIds, PersonPE registrator, String reason)
            throws DataAccessException;

}
