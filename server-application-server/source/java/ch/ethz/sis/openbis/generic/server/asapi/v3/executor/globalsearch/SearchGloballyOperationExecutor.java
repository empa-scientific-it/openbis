/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.globalsearch;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.GlobalSearchObject;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.fetchoptions.GlobalSearchObjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.*;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.ISearchObjectExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SearchObjectsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.SortAndPage;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.AuthorisationInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.GlobalSearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ILocalSearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql.AbstractSQLExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.globalsearch.IGlobalSearchObjectTranslator;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.authorization.AuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.ethz.sis.openbis.generic.asapi.v3.dto.global.fetchoptions.GlobalSearchObjectSortOptions.SCORE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.GlobalSearchCriteriaTranslator.TOTAL_COUNT_ALIAS;

/**
 * @author pkupczyk
 */
@Component
public class SearchGloballyOperationExecutor
        extends SearchObjectsOperationExecutor<GlobalSearchObject, MatchingEntity, GlobalSearchCriteria, GlobalSearchObjectFetchOptions>
        implements ISearchGloballyOperationExecutor
{

    private static final Logger OPERATION_LOG = LogFactory.getLogger(LogCategory.OPERATION,
            SearchGloballyOperationExecutor.class);

    @Autowired
    private IGlobalSearchExecutor searchExecutor;

    @Autowired
    private IGlobalSearchObjectTranslator translator;

    @Autowired
    private IGlobalAuthorizationExecutor authorizationExecutor;

    @Autowired
    private GlobalSearchManager globalSearchManager;

    @Autowired
    private AuthorizationConfig authorizationConfig;

    @Override
    protected SearchObjectsOperationResult<GlobalSearchObject> doExecute(IOperationContext context,
            SearchObjectsOperation<GlobalSearchCriteria, GlobalSearchObjectFetchOptions> operation)
    {
        authorizationExecutor.canSearch(context);

        final GlobalSearchCriteria criteria = operation.getCriteria();
        final GlobalSearchObjectFetchOptions fetchOptions = operation.getFetchOptions();

        if (criteria == null)
        {
            throw new IllegalArgumentException("Criteria cannot be null.");
        }
        if (fetchOptions == null)
        {
            throw new IllegalArgumentException("Fetch options cannot be null.");
        }

        final PersonPE personPE = context.getSession().tryGetPerson();
        final AuthorisationInformation authorisationInformation = AuthorisationInformation.getInstance(personPE,
                authorizationConfig);

        final Long userId = context.getSession().tryGetPerson().getId();
        final TranslationContext translationContext = new TranslationContext(context.getSession());

        final Set<GlobalSearchObjectKind> objectKinds = getObjectKinds(criteria);

        // There results from the manager should already be filtered.
        final Collection<Map<String, Object>> shortRecords = globalSearchManager.searchForIDs(userId,
                authorisationInformation, criteria, null, objectKinds, fetchOptions, false);
        final Collection<Map<String, Object>> detailsRecords = shortRecords.isEmpty() ? Collections.emptyList() :
                globalSearchManager.searchForDetails(shortRecords, userId, authorisationInformation, criteria, null,
                        objectKinds, fetchOptions);

        final Collection<MatchingEntity> pagedMatchingEntities = globalSearchManager.map(detailsRecords,
                fetchOptions.hasMatch());

        // TODO: doTranslate() should only filter nested objects of the results (parents, children, components...).
        final Map<MatchingEntity, GlobalSearchObject> pagedResultV3DTOs = doTranslate(translationContext,
                pagedMatchingEntities, fetchOptions);

        if (pagedMatchingEntities.size() != pagedResultV3DTOs.size())
        {
            throw new RuntimeException(String.format("Number of results after translation has changed. "
                            + "Total count value will be incorrect. "
                            + "[pagedResultPEs.size()=%d, pagedResultV3DTOs.size()=%d]",
                    pagedMatchingEntities.size(), pagedResultV3DTOs.size()));
        }

        // Reordering of pagedResultV3DTOs is needed because translation mixes the order
        final List<GlobalSearchObject> objectResults = pagedMatchingEntities.stream().map(pagedResultV3DTOs::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Sorting and paging parents and children in a "conventional" way.
        new SortAndPage().nest(objectResults, criteria, fetchOptions);

        final Collection<Map<String, Object>> totalCountCollection = shortRecords.isEmpty()
                ? globalSearchManager.searchForIDs(userId, authorisationInformation, criteria, null, objectKinds,
                        fetchOptions, true)
                : shortRecords;
        final int totalCount = (int) (long) totalCountCollection.iterator().next().get(TOTAL_COUNT_ALIAS);
        return getOperationResult(new SearchResult<>(objectResults, totalCount));
    }

    private static Set<GlobalSearchObjectKind> getObjectKinds(final GlobalSearchCriteria globalSearchCriteria)
    {
        final Stream<GlobalSearchObjectKindCriteria> objectKindCriteriaStream = globalSearchCriteria.getCriteria()
                .stream().filter((criterion) -> criterion instanceof GlobalSearchObjectKindCriteria)
                .map((criterion) -> (GlobalSearchObjectKindCriteria) criterion);
        final Set<GlobalSearchObjectKind> objectKinds;
        switch (globalSearchCriteria.getOperator())
        {
            case OR:
            {
                objectKinds = EnumSet.noneOf(GlobalSearchObjectKind.class);
                objectKindCriteriaStream.forEach(
                        (objectKindCriterion) -> objectKinds.addAll(objectKindCriterion.getObjectKinds()));
                break;
            }
            case AND:
            {
                objectKinds = EnumSet.allOf(GlobalSearchObjectKind.class);
                objectKindCriteriaStream.forEach(
                        (objectKindCriterion) -> objectKinds.retainAll(objectKindCriterion.getObjectKinds()));
                break;
            }
            default:
            {
                throw new RuntimeException();
            }
        }

        return objectKinds.isEmpty() ? EnumSet.allOf(GlobalSearchObjectKind.class) : objectKinds;
    }

    @Override
    protected Class<? extends SearchObjectsOperation<GlobalSearchCriteria, GlobalSearchObjectFetchOptions>> getOperationClass()
    {
        return SearchGloballyOperation.class;
    }

    @Override
    protected ISearchObjectExecutor<GlobalSearchCriteria, MatchingEntity> getExecutor()
    {
        return searchExecutor;
    }

    @Override
    protected ITranslator<MatchingEntity, GlobalSearchObject, GlobalSearchObjectFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected ILocalSearchManager<GlobalSearchCriteria, GlobalSearchObject, MatchingEntity> getSearchManager()
    {
        throw new IllegalStateException("This method should not be executed. globalSearchManager should be used instead.");
    }

    @Override
    protected SearchObjectsOperationResult<GlobalSearchObject> getOperationResult(SearchResult<GlobalSearchObject> searchResult)
    {
        return new SearchGloballyOperationResult(searchResult);
    }

}
