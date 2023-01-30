/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.search;

import java.util.List;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.openbis.generic.server.business.bo.materiallister.IMaterialLister;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IHibernateSearchDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;

/**
 * @author jakubs
 */
public class MaterialSearchManager extends AbstractSearchManager<IMaterialLister>
{

    public MaterialSearchManager(IHibernateSearchDAO searchDAO, IMaterialLister lister)
    {
        super(searchDAO, lister);
    }

    public List<Material> searchForMaterials(String userId, DetailedSearchCriteria criteria)
            throws DataAccessException
    {
        List<Long> materialIds =
                searchDAO.searchForEntityIds(userId, criteria,
                        ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind.MATERIAL);

        return lister.list(ListMaterialCriteria.createFromMaterialIds(materialIds), true);
    }
}
