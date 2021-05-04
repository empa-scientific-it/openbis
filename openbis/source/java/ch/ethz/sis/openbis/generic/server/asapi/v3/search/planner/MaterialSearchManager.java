/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractCompositeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.PermIdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.ModifierSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleParentsSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.AuthorisationInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.ISQLAuthorisationInformationProviderDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.dao.ISQLSearchDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.hibernate.IID2PEMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Manages detailed search with material search criteria.
 *
 * @author Viktor Kovtun
 */
public class MaterialSearchManager extends AbstractCompositeEntitySearchManager<MaterialSearchCriteria, Material, Long>
{

    public MaterialSearchManager(final ISQLSearchDAO searchDAO, final ISQLAuthorisationInformationProviderDAO authProvider,
            final IID2PEMapper<Long, Long> idsMapper)
    {
        super(searchDAO, authProvider, idsMapper);
    }

    @Override
    protected Class<? extends AbstractCompositeSearchCriteria> getParentsSearchCriteriaClass()
    {
        return null;
    }

    @Override
    protected Class<? extends AbstractCompositeSearchCriteria> getChildrenSearchCriteriaClass()
    {
        return null;
    }

    @Override
    protected MaterialSearchCriteria createEmptyCriteria(final boolean negated)
    {
        return new MaterialSearchCriteria();
    }

    @Override
    protected Set<Long> doFilterIDsByUserRights(final Set<Long> ids, final AuthorisationInformation authorisationInformation)
    {
        return ids;
    }

    @Override
    public Set<Long> searchForIDs(final Long userId, final AuthorisationInformation authorisationInformation,
            final MaterialSearchCriteria criteria,
            final AbstractCompositeSearchCriteria parentCriteria, final String idsColumnName) {
        if (criteria.getCriteria().stream().anyMatch((criterion) -> criterion instanceof PermIdSearchCriteria))
        {
            // Perm ID equals to string is not supported.
            throw new UnsupportedOperationException("Please use criteria.withId().thatEquals(new MaterialPermId('CODE','TYPE')) instead.");
        }
        if (criteria.getCriteria().stream().anyMatch((criterion) -> criterion instanceof ModifierSearchCriteria))
        {
            // Search by modifier is not supported but the result should be empty.
            return Collections.emptySet();
        }
        return doSearchForIDs(userId, authorisationInformation, criteria, idsColumnName, TableMapper.MATERIAL);
    }

    @Override
    public List<Long> sortIDs(final Collection<Long> ids, final SortOptions<Material> sortOptions) {
        return doSortIDs(ids, sortOptions, TableMapper.MATERIAL);
    }

}
