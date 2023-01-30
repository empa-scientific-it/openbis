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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.ContentCopy;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.ContentCopySearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.AuthorisationInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.ISQLAuthorisationInformationProviderDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.dao.ISQLSearchDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.hibernate.IID2PEMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Manages detailed search with external data management system search criteria.
 *
 * @author Viktor Kovtun
 */
public class ContentCopySearchManager extends AbstractLocalSearchManager<ContentCopySearchCriteria, ContentCopy, Long>
{

    public ContentCopySearchManager(final ISQLSearchDAO searchDAO, final ISQLAuthorisationInformationProviderDAO authProvider,
            final IID2PEMapper<Long, Long> idsMapper)
    {
        super(searchDAO, authProvider, idsMapper);
    }

    @Override
    protected AbstractCompositeSearchCriteria createEmptyCriteria(final boolean negated)
    {
        return new ContentCopySearchCriteria();
    }

    @Override
    protected Set<Long> doFilterIDsByUserRights(final Set<Long> ids, final AuthorisationInformation authorisationInformation)
    {
        return ids;
    }

    @Override
    public Set<Long> searchForIDs(final Long userId, final AuthorisationInformation authorisationInformation,
            final ContentCopySearchCriteria criteria,
            final AbstractCompositeSearchCriteria parentCriteria, final String idsColumnName)
    {
        return super.searchForIDs(userId, authorisationInformation, criteria, ColumnNames.ID_COLUMN, TableMapper.CONTENT_COPIES);
    }

    @Override
    public List<Long> sortIDs(final Collection<Long> ids, final SortOptions<ContentCopy> sortOptions) {
        return doSortIDs(ids, sortOptions, TableMapper.CONTENT_COPIES);
    }

}
