/*
 * Copyright ETH 2019 - 2023 Zürich, Scientific IT Services
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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.Event;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.fetchoptions.EventSortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.search.EventSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.AuthorisationInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.ISQLAuthorisationInformationProviderDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.dao.ISQLSearchDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.hibernate.IdentityMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author pkupczyk
 */
@Component
public class EventSearchManager extends AbstractLocalSearchManager<EventSearchCriteria, Event, Long>
{

    @Autowired
    public EventSearchManager(final ISQLSearchDAO searchDAO, final ISQLAuthorisationInformationProviderDAO authProvider)
    {
        super(searchDAO, authProvider, new IdentityMapper());
    }

    @Override
    protected AbstractCompositeSearchCriteria createEmptyCriteria(final boolean negated)
    {
        return new EventSearchCriteria();
    }

    @Override
    protected Set<Long> doFilterIDsByUserRights(final Set<Long> ids, final AuthorisationInformation authorisationInformation)
    {
        return ids;
    }

    @Override
    public Set<Long> searchForIDs(final Long userId, final AuthorisationInformation authorisationInformation, final EventSearchCriteria criteria,
            final AbstractCompositeSearchCriteria parentCriteria, final String idsColumnName)
    {
        return super.searchForIDs(userId, authorisationInformation, criteria, idsColumnName, TableMapper.EVENT);
    }

    @Override
    public List<Long> sortIDs(final Collection<Long> ids, final SortOptions<Event> sortOptions)
    {
        EventSortOptions eventSortOptions = (EventSortOptions) sortOptions;

        if (eventSortOptions.getId() == null)
        {
            eventSortOptions.id();
        }

        return doSortIDs(ids, sortOptions, TableMapper.EVENT);
    }

}
