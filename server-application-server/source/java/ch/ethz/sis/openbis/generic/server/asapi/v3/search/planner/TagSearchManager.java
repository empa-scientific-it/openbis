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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.*;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.search.TagSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.AuthorisationInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.ISQLAuthorisationInformationProviderDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.dao.ISQLSearchDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.hibernate.IID2PEMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Manages detailed search with tag search criteria.
 *
 * @author Viktor Kovtun
 */
public class TagSearchManager extends AbstractLocalSearchManager<TagSearchCriteria, Tag, Long>
{

    public TagSearchManager(final ISQLSearchDAO searchDAO, final ISQLAuthorisationInformationProviderDAO authProvider,
            final IID2PEMapper<Long, Long> idsMapper)
    {
        super(searchDAO, authProvider, idsMapper);
    }

    @Override
    protected AbstractCompositeSearchCriteria createEmptyCriteria(final boolean negated)
    {
        return new TagSearchCriteria();
    }

    @Override
    protected Set<Long> doFilterIDsByUserRights(final Set<Long> ids, final AuthorisationInformation authorisationInformation)
    {
        return ids;
    }

    private NameSearchCriteria convertToNameSearchCriterion(final AbstractFieldSearchCriteria<AbstractStringValue> criterion)
    {
        return convertToOtherCriterion(criterion, () ->
        {
            final NameSearchCriteria result = new NameSearchCriteria();
            if (criterion instanceof StringFieldSearchCriteria &&
                    ((StringFieldSearchCriteria) criterion).isUseWildcards())
            {
                result.withWildcards();
            }
            return result;
        });
    }

    @Override
    public Set<Long> searchForIDs(final Long userId, final AuthorisationInformation authorisationInformation, final TagSearchCriteria criteria,
            final AbstractCompositeSearchCriteria parentCriteria, final String idsColumnName)
    {
        // Replacing perm ID search criteria with name search criteria, because for tags perm ID is equivalent to name
        final Collection<ISearchCriteria> newCriteria = criteria.getCriteria().stream().map(searchCriterion ->
        {
            if (searchCriterion instanceof PermIdSearchCriteria)
            {
                return convertToNameSearchCriterion((PermIdSearchCriteria) searchCriterion);
            } else
            {
                return searchCriterion;
            }
        }).collect(Collectors.toList());

        final Set<Long> mainCriteriaIntermediateResults = getSearchDAO().queryDBForIdsWithGlobalSearchMatchCriteria(userId,
                new DummyCompositeSearchCriterion(newCriteria, criteria.getOperator()), TableMapper.TAG, idsColumnName, authorisationInformation);

        if (!containsValues(mainCriteriaIntermediateResults))
        {
            return Collections.emptySet();
        }

        final Set<Long> resultAfterFiltering = getAuthProvider().getTagsOfUser(mainCriteriaIntermediateResults, userId);

        if (!containsValues(resultAfterFiltering))
        {
            return Collections.emptySet();
        }

        return filterIDsByUserRights(userId, authorisationInformation, resultAfterFiltering);
    }

    @Override
    public List<Long> sortIDs(final Collection<Long> ids, final SortOptions<Tag> sortOptions)
    {
        return doSortIDs(ids, sortOptions, TableMapper.TAG);
    }

}
