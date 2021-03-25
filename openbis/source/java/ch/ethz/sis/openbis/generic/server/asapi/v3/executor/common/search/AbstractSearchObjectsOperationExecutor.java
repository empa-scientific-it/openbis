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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search;

import java.util.*;
import java.util.stream.Collectors;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.*;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.AuthorisationInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ILocalSearchManager;
import ch.systemsx.cisd.openbis.generic.shared.authorization.AuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.cache.ISearchCache;
import ch.ethz.sis.openbis.generic.server.asapi.v3.cache.SearchCacheCleanupListener;
import ch.ethz.sis.openbis.generic.server.asapi.v3.cache.SearchCacheEntry;
import ch.ethz.sis.openbis.generic.server.asapi.v3.cache.SearchCacheKey;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.OperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.SortAndPage;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.ID_COLUMN;

/**
 * @author pkupczyk
 */
public abstract class AbstractSearchObjectsOperationExecutor<OBJECT, OBJECT_PE, CRITERIA extends AbstractSearchCriteria, FETCH_OPTIONS extends FetchOptions<OBJECT>>
        extends OperationExecutor<SearchObjectsOperation<CRITERIA, FETCH_OPTIONS>, SearchObjectsOperationResult<OBJECT>>
        implements ISearchObjectsOperationExecutor
{

    private static final String[] SORTS_TO_IGNORE = new String[] {
            EntityWithPropertiesSortOptions.FETCHED_FIELDS_SCORE
    };

    private static final Logger OPERATION_LOG = LogFactory.getLogger(LogCategory.OPERATION,
            AbstractSearchObjectsOperationExecutor.class);

    @Autowired
    protected ISearchCache<CRITERIA, FETCH_OPTIONS, OBJECT> cache;

    @Autowired
    private AuthorizationConfig authorizationConfig;

    protected abstract List<OBJECT_PE> doSearch(IOperationContext context, CRITERIA criteria, FETCH_OPTIONS fetchOptions);

    protected abstract Map<OBJECT_PE, OBJECT> doTranslate(TranslationContext translationContext, Collection<OBJECT_PE> ids, FETCH_OPTIONS fetchOptions);

    protected abstract SearchObjectsOperationResult<OBJECT> getOperationResult(SearchResult<OBJECT> searchResult);

    protected abstract ILocalSearchManager<CRITERIA, OBJECT, OBJECT_PE> getSearchManager();

    @Override
    protected SearchObjectsOperationResult<OBJECT> doExecute(IOperationContext context, SearchObjectsOperation<CRITERIA, FETCH_OPTIONS> operation)
    {
        CRITERIA criteria = operation.getCriteria();
        FETCH_OPTIONS fetchOptions = operation.getFetchOptions();

        if (criteria == null)
        {
            throwIllegalArgumentException("Criteria cannot be null.");
        }
        if (fetchOptions == null)
        {
            throwIllegalArgumentException("Fetch options cannot be null.");
        }

        Collection<OBJECT> allResults = searchAndTranslate(context, criteria, fetchOptions);
        List<OBJECT> sortedAndPaged = sortAndPage(allResults, criteria, fetchOptions);

        SearchResult<OBJECT> searchResult = new SearchResult<OBJECT>(sortedAndPaged, allResults.size());
        return getOperationResult(searchResult);
    }

    private Collection<OBJECT> searchAndTranslate(IOperationContext context, CRITERIA criteria, FETCH_OPTIONS fetchOptions)
    {
        CacheMode cacheMode = fetchOptions.getCacheMode();
        OPERATION_LOG.info("Cache mode: " + cacheMode);

        if (CacheMode.NO_CACHE.equals(cacheMode))
        {
            return doSearchAndTranslate(context, criteria, fetchOptions);
        } else if (CacheMode.CACHE.equals(cacheMode) || CacheMode.RELOAD_AND_CACHE.equals(cacheMode))
        {
            SearchCacheEntry<OBJECT> entry = getCacheEntry(context, criteria, fetchOptions);
            populateCacheEntry(context, criteria, fetchOptions, entry);
            return entry.getObjects();
        } else
        {
            throw new IllegalArgumentException("Unsupported cache mode: " + cacheMode);
        }
    }

    protected Collection<OBJECT> doSearchAndTranslate(IOperationContext context, CRITERIA criteria, FETCH_OPTIONS fetchOptions)
    {
        OPERATION_LOG.info("Searching...");

        List<OBJECT_PE> ids = doSearch(context, criteria, fetchOptions);

        OPERATION_LOG.info("Found " + ids.size() + " object id(s).");

        TranslationContext translationContext = new TranslationContext(context.getSession());
        Map<OBJECT_PE, OBJECT> idToObjectMap = doTranslate(translationContext, ids, fetchOptions);
        Collection<OBJECT> objects = idToObjectMap.values();

        OPERATION_LOG.info("Translated " + objects.size() + " object(s).");

        return objects;
    }

    private List<OBJECT> sortAndPage(Collection<OBJECT> results, CRITERIA criteria, FETCH_OPTIONS fetchOptions)
    {
        if (results == null || results.isEmpty())
        {
            return Collections.emptyList();
        }

        SortAndPage sap = new SortAndPage();
        Collection<OBJECT> objects = sap.sortAndPage(results, criteria, fetchOptions);

        OPERATION_LOG.info("Return " + objects.size() + " object(s) after sorting and paging.");

        return new ArrayList<OBJECT>(objects);
    }

    private SearchCacheEntry<OBJECT> getCacheEntry(IOperationContext context, CRITERIA criteria, FETCH_OPTIONS fetchOptions)
    {
        SearchCacheKey<CRITERIA, FETCH_OPTIONS> key =
                new SearchCacheKey<CRITERIA, FETCH_OPTIONS>(context.getSession().getSessionToken(), criteria, fetchOptions);
        SearchCacheEntry<OBJECT> entry = null;

        if (OPERATION_LOG.isDebugEnabled())
        {
            OPERATION_LOG.debug("Will try to lock on session " + context.getSession().hashCode());
        }

        synchronized (context.getSession())
        {
            if (OPERATION_LOG.isDebugEnabled())
            {
                OPERATION_LOG.debug("Locked on session " + context.getSession().hashCode());
            }

            if (CacheMode.RELOAD_AND_CACHE.equals(fetchOptions.getCacheMode()))
            {
                cache.remove(key);
            }

            entry = cache.get(key);

            if (entry == null)
            {
                entry = new SearchCacheEntry<OBJECT>();
                cache.put(key, entry);
                context.getSession().addCleanupListener(new SearchCacheCleanupListener<CRITERIA, FETCH_OPTIONS>(cache, key));
            }

            if (OPERATION_LOG.isDebugEnabled())
            {
                OPERATION_LOG.debug("Released lock on session " + context.getSession().hashCode());
            }
        }

        return entry;
    }

    private void populateCacheEntry(IOperationContext context, CRITERIA criteria, FETCH_OPTIONS fetchOptions, SearchCacheEntry<OBJECT> entry)
    {
        if (OPERATION_LOG.isDebugEnabled())
        {
            OPERATION_LOG.debug("Will try to lock on cache entry " + entry.hashCode());
        }

        synchronized (entry)
        {
            if (OPERATION_LOG.isDebugEnabled())
            {
                OPERATION_LOG.debug("Locked on cache entry " + entry.hashCode());
            }

            if (entry.getObjects() == null)
            {
                entry.setObjects(doSearchAndTranslate(context, criteria, fetchOptions));
                SearchCacheKey<CRITERIA, FETCH_OPTIONS> key =
                        new SearchCacheKey<CRITERIA, FETCH_OPTIONS>(context.getSession().getSessionToken(), criteria, fetchOptions);
                // put the entry to the cache again to trigger the size recalculation
                cache.put(key, entry);
            } else
            {
                OPERATION_LOG.info("Found cache entry " + entry.hashCode() + " that contains search result with " + entry.getObjects().size()
                        + " object(s).");
            }

            if (OPERATION_LOG.isDebugEnabled())
            {
                OPERATION_LOG.debug("Released lock on cache entry " + entry.hashCode());
            }
        }
    }

    public Collection<Long> executeDirectSQLSearchForIds(final PersonPE personPE,
            final CRITERIA criteria, final FETCH_OPTIONS fetchOptions)
    {
        final AuthorisationInformation authorisationInformation = AuthorisationInformation.getInstance(personPE,
                authorizationConfig);
        final Long userId = personPE.getId();
        final Set<Long> allResultsIds = getSearchManager().searchForIDs(userId, authorisationInformation, criteria,
                null, ID_COLUMN);
        return sortAndPage(allResultsIds, fetchOptions);
    }

    protected SearchObjectsOperationResult<OBJECT> executeDirectSQLSearch(final IOperationContext context,
            final SearchObjectsOperation<CRITERIA, FETCH_OPTIONS> operation)
    {
        final CRITERIA criteria = operation.getCriteria();
        final FETCH_OPTIONS fetchOptions = operation.getFetchOptions();

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

        final Long userId = personPE.getId();
        final TranslationContext translationContext = new TranslationContext(context.getSession());

        final Set<Long> allResultsIds = getSearchManager().searchForIDs(userId, authorisationInformation, criteria,
                null, ID_COLUMN);
        final Collection<Long> pagedResultIds = sortAndPage(allResultsIds, fetchOptions);
        final Collection<OBJECT_PE> pagedResultPEs = getSearchManager().map(pagedResultIds);
        // TODO: doTranslate() should only filter nested objects of the results (parents, children, components...).
        final Map<OBJECT_PE, OBJECT> pagedResultV3DTOs = doTranslate(translationContext, pagedResultPEs, fetchOptions);

        if (pagedResultPEs.size() != pagedResultV3DTOs.size())
        {
            OPERATION_LOG.warn(String.format("Number of results after translation has changed. "
                            + "Total count value will be incorrect. "
                            + "[pagedResultPEs.size()=%d, pagedResultV3DTOs.size()=%d]",
                    pagedResultPEs.size(), pagedResultV3DTOs.size()));
        }

        // Reordering of pagedResultV3DTOs is needed because translation mixes the order
        final List<OBJECT> objectResults = pagedResultPEs.stream().map(pagedResultV3DTOs::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Sorting and paging parents and children in a "conventional" way.
        new SortAndPage().nest(objectResults, criteria, fetchOptions);

        final SearchResult<OBJECT> searchResult = new SearchResult<>(objectResults, allResultsIds.size());
        return getOperationResult(searchResult);
    }

    private Collection<Long> sortAndPage(final Set<Long> ids, final FETCH_OPTIONS fetchOptions)
    {
        SortOptions<OBJECT> sortOptions = fetchOptions.getSortBy();

        // Filter out sorts to ignore
        if (sortOptions != null) {
            List<Sorting> sortingToRemove = new ArrayList<>();
            for (Sorting sorting : sortOptions.getSortings()) {
                for (String sortToIgnore : SORTS_TO_IGNORE) {
                    if (sorting.getField().equals(sortToIgnore)) {
                        sortingToRemove.add(sorting);
                    }
                }
            }

            for (Sorting sorting : sortingToRemove) {
                sortOptions.getSortings().remove(sorting);
                OPERATION_LOG.warn("[SQL Query Engine - backwards compatibility warning - stop using this feature] " +
                        "SORTING ORDER IGNORED!: " + sorting.getField());
            }

            if (sortOptions.getSortings().isEmpty()) {
                sortOptions = null;
            }
        }

        final List<Long> sortedIds = (sortOptions != null) ? getSearchManager().sortIDs(ids, sortOptions)
                : new ArrayList<>(ids);

        final List<Long> toPage;
        if (sortedIds.size() < ids.size())
        {
            if (sortOptions != null && sortOptions.getSortings().size() > 1)
            {
                throwIllegalArgumentException("Sorting by multiple fields when one or more properties are missing " +
                        "in the result set entities is not supported.");
            }

            final Set<Long> combiningSet = new LinkedHashSet<>(sortedIds);
            combiningSet.addAll(ids);
            toPage = new ArrayList<>(combiningSet);
        } else
        {
            toPage = sortedIds;
        }

        final Integer foFromRecord = fetchOptions.getFrom();
        final Integer foRecordsCount = fetchOptions.getCount();
        final boolean hasPaging = foFromRecord != null || foRecordsCount != null;
        if (hasPaging)
        {
            final int fromRecord = foFromRecord != null ? foFromRecord : 0;
            final int toRecord = foRecordsCount != null ? Math.min(fromRecord + foRecordsCount, toPage.size())
                    : toPage.size();
            return fromRecord <= toRecord ? toPage.subList(fromRecord, toRecord) : Collections.emptyList();
        } else
        {
            return toPage;
        }
    }

    private static void throwIllegalArgumentException(final String message) throws RuntimeException {
        throw new IllegalArgumentException(message);
    }

}
