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

import java.util.List;
import java.util.Properties;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.HibernateSearchDataProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * <i>Data Access Object</i> for accessing <i>Hibernate Search</i>.
 * 
 * @author Christian Ribeaud
 */
public interface IHibernateSearchDAO
{
    /**
     * Searches in entities of type <var>searchableEntity</var> for given <var>searchTerm</var> using all the indexed fields.
     * 
     * @param searchTerm could be something like "<code>C11 AND System User</code>".
     * @param maxSize Maximum number of entries.
     */
    public List<MatchingEntity> searchEntitiesByTerm(final String userId,
            final SearchableEntity searchableEntity, final String searchTerm,
            final HibernateSearchDataProvider dataProvider, boolean useWildcardSearchMode,
            int alreadyFoundEntities, int maxSize) throws DataAccessException;

    /** search for entity ids using the specified criteria */
    public List<Long> searchForEntityIds(final String userId, DetailedSearchCriteria criteria,
            EntityKind entityKind);

    /**
     * Returns the maximum size of a search result set. The standard implementations returns <code>hibernate.search.maxResults</code> of
     * <code>service.properties</code>.
     */
    public int getResultSetSizeLimit();

    public void setProperties(Properties serviceProperties);
}
