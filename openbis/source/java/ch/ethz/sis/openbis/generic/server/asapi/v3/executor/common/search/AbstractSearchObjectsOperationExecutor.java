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

import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.ID_COLUMN;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.CacheMode;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.EntityWithPropertiesSortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.Sorting;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.cache.SearchCacheCleanupListener;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.OperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.cache.ICacheManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.SortAndPage;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.AuthorisationInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ILocalSearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.authorization.AuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author pkupczyk
 */
public abstract class AbstractSearchObjectsOperationExecutor<OBJECT, OBJECT_PE, CRITERIA extends AbstractSearchCriteria, FETCH_OPTIONS extends FetchOptions<OBJECT>>
        extends OperationExecutor<SearchObjectsOperation<CRITERIA, FETCH_OPTIONS>, SearchObjectsOperationResult<OBJECT>>
        implements ISearchObjectsOperationExecutor
{

    private static final String[] SORTS_TO_IGNORE = new String[]
            {
                    EntityWithPropertiesSortOptions.FETCHED_FIELDS_SCORE
            };

    private static final Logger OPERATION_LOG = LogFactory.getLogger(LogCategory.OPERATION,
            AbstractSearchObjectsOperationExecutor.class);

    @Autowired
    private AuthorizationConfig authorizationConfig;

    @Autowired
    private ICacheManager cacheManager;

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

    private Collection<OBJECT> searchAndTranslate(IOperationContext context, CRITERIA criteria,
            FETCH_OPTIONS fetchOptions)
    {
        final CacheMode cacheMode = getCacheManager().getCacheClass() != null ? fetchOptions.getCacheMode()
                : CacheMode.NO_CACHE;
        OPERATION_LOG.info("Cache mode: " + cacheMode);

        if (CacheMode.NO_CACHE.equals(cacheMode))
        {
            return doSearchAndTranslate(context, criteria, fetchOptions);
        } else if (CacheMode.CACHE.equals(cacheMode) || CacheMode.RELOAD_AND_CACHE.equals(cacheMode))
        {
            final Set<OBJECT_PE> ids;

            synchronized (context.getSession())
            {
                final Set<OBJECT_PE> cachedIds = getCacheEntry(context, criteria, fetchOptions);

                if (cachedIds == null)
                {
                    ids = new LinkedHashSet<>(doSearch(context, criteria, fetchOptions));
                    populateCache(context, criteria, Collections.unmodifiableSet(ids));
                } else
                {
                    OPERATION_LOG.info("Found cache entry " + cachedIds.hashCode() +
                            " that contains search result with " + cachedIds.size() + " object(s).");
                    ids = cachedIds;
                }
            }

            return doTranslate(context, fetchOptions, ids);
        } else
        {
            throw new IllegalArgumentException("Unsupported cache mode: " + cacheMode);
        }
    }

    protected void populateCache(final IOperationContext context, final CRITERIA criteria, final Collection<OBJECT_PE> ids)
    {
        final String key = getMD5Hash(criteria);

        final ICache<Object> cache = getCacheManager().getCache(context);
        cache.put(key, ids);
    }

    protected Collection<OBJECT> doSearchAndTranslate(IOperationContext context, CRITERIA criteria, FETCH_OPTIONS fetchOptions)
    {
        OPERATION_LOG.info("Searching...");
        final Collection<OBJECT_PE> ids = doSearch(context, criteria, fetchOptions);
        OPERATION_LOG.info("Found " + ids.size() + " object id(s).");

        return doTranslate(context, fetchOptions, ids);
    }

    private Collection<OBJECT> doTranslate(final IOperationContext context, final FETCH_OPTIONS fetchOptions,
            final Collection<OBJECT_PE> ids)
    {
        final TranslationContext translationContext = new TranslationContext(context.getSession());
        final Map<OBJECT_PE, OBJECT> idToObjectMap = doTranslate(translationContext, ids, fetchOptions);
        final Collection<OBJECT> objects = idToObjectMap.values();

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

    private <T> Set<T> getCacheEntry(final IOperationContext context, final CRITERIA criteria,
            final FETCH_OPTIONS fetchOptions)
    {
        final String key = getMD5Hash(criteria);

        final int sessionHashCode = context.getSession().hashCode();
        if (OPERATION_LOG.isDebugEnabled())
        {
            OPERATION_LOG.debug("Will try to lock on session " + sessionHashCode);
        }

        final Set<T> entry;
        synchronized (context.getSession())
        {
            if (OPERATION_LOG.isDebugEnabled())
            {
                OPERATION_LOG.debug("Locked on session " + sessionHashCode);
            }

            final ICache<Object> cache = getCacheManager().getCache(context);

            if (CacheMode.RELOAD_AND_CACHE.equals(fetchOptions.getCacheMode()))
            {
                cache.remove(key);
            }

            entry = (Set<T>) cache.get(key);

            if (entry == null)
            {
                context.getSession().addCleanupListener(new SearchCacheCleanupListener(cache, key));
            }

            if (OPERATION_LOG.isDebugEnabled())
            {
                OPERATION_LOG.debug("Released lock on session " + sessionHashCode);
            }
        }

        return entry;
    }

    public Collection<Long> executeDirectSQLSearchForIds(final PersonPE personPE,
            final CRITERIA criteria, final FETCH_OPTIONS fetchOptions)
    {
        final AuthorisationInformation authorisationInformation = AuthorisationInformation.getInstance(personPE,
                authorizationConfig);
        final Long userId = personPE.getId();
        final Set<Long> allResultsIds = performDirectSearch(criteria, authorisationInformation, userId);
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

        Set<Long> ids = getIds(context, criteria, fetchOptions);

        final Collection<Long> pagedResultIds = sortAndPage(ids, fetchOptions);
        final Collection<OBJECT_PE> pagedResultPEs = getSearchManager().map(pagedResultIds);
        final TranslationContext translationContext = new TranslationContext(context.getSession());
        // TODO: doTranslate() should only filter nested objects of the results (parents, children, components...).
        final Map<OBJECT_PE, OBJECT> pagedResultV3DTOs = doTranslate(translationContext, pagedResultPEs, fetchOptions);

        if (pagedResultPEs.size() != pagedResultV3DTOs.size())
        {
            throw new RuntimeException(String.format("Number of results after translation has changed. "
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

        final SearchResult<OBJECT> searchResult = new SearchResult<>(objectResults, ids.size());
        return getOperationResult(searchResult);
    }

    private Set<Long> getIds(final IOperationContext context, final CRITERIA criteria, final FETCH_OPTIONS fetchOptions)
    {
        final PersonPE personPE = context.getSession().tryGetPerson();
        final AuthorisationInformation authorisationInformation = AuthorisationInformation.getInstance(personPE,
                authorizationConfig);

        final Long userId = personPE.getId();

        final CacheMode cacheMode = getCacheManager().getCacheClass() != null ? fetchOptions.getCacheMode()
                : CacheMode.NO_CACHE;
        OPERATION_LOG.info("Cache mode: " + cacheMode);

        if (CacheMode.NO_CACHE.equals(cacheMode))
        {
            return performDirectSearch(criteria, authorisationInformation, userId);
        } else if (CacheMode.CACHE.equals(cacheMode) || CacheMode.RELOAD_AND_CACHE.equals(cacheMode))
        {
            Set<Long> ids = getCacheEntry(context, criteria, fetchOptions);
            if (ids == null)
            {
                ids = performDirectSearch(criteria, authorisationInformation, userId);
                final String key = getMD5Hash(criteria);

                final ICache<Object> cache = getCacheManager().getCache(context);
                // put the entry to the cache again to trigger the size recalculation
                cache.put(key, Collections.unmodifiableSet(ids));
            } else
            {
                OPERATION_LOG.info("Found cache entry " + ids.hashCode() + " that contains search result with "
                        + ids.size() + " object(s).");
            }
            return ids;
        } else
        {
            throw new IllegalArgumentException("Unsupported cache mode: " + cacheMode);
        }
    }

    protected static String getMD5Hash(Object criteria)
    {
        try
        {
            final MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(criteria.toString().getBytes(StandardCharsets.UTF_8));
            final byte[] digest = md.digest();
            return DatatypeConverter.printHexBinary(digest).toUpperCase();
        } catch (final NoSuchAlgorithmException e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
    }

    private Set<Long> performDirectSearch(final CRITERIA criteria,
            final AuthorisationInformation authorisationInformation, final Long userId)
    {
        return getSearchManager().searchForIDs(userId, authorisationInformation, criteria, null, ID_COLUMN);
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

    public ICacheManager getCacheManager()
    {
        return cacheManager;
    }

    protected void setCacheManager(final ICacheManager cacheManager)
    {
        this.cacheManager = cacheManager;
    }

}
