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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractCompositeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.AuthorisationInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.ISQLAuthorisationInformationProviderDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.dao.ISQLSearchDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractSearchManager<OBJECT>
{

    protected final ISQLAuthorisationInformationProviderDAO authProvider;

    private final ISQLSearchDAO searchDAO;

    public AbstractSearchManager(final ISQLAuthorisationInformationProviderDAO authProvider, final ISQLSearchDAO searchDAO)
    {
        this.authProvider = authProvider;
        this.searchDAO = searchDAO;
    }

    /**
     * Checks whether a collection contains any values.
     *
     * @param collection collection to be checked for values.
     * @return {@code false} if collection is {@code null} or empty, true otherwise.
     */
    protected static boolean containsValues(final Collection<?> collection)
    {
        return collection != null && !collection.isEmpty();
    }

    public Set<Long> filterIDsByUserRights(final Long userId, final AuthorisationInformation authorisationInformation, final Set<Long> ids)
    {
        if (authorisationInformation.isInstanceRole())
        {
            return ids;
        } else
        {
            return doFilterIDsByUserRights(ids, authorisationInformation);
        }
    }

    protected abstract Set<Long> doFilterIDsByUserRights(final Set<Long> ids, final AuthorisationInformation authorisationInformation);

    protected List<ISearchCriteria> getOtherCriteriaThan(final AbstractCompositeSearchCriteria searchCriteria,
            final Class<? extends ISearchCriteria>... classes)
    {
        return searchCriteria.getCriteria().stream().filter(
                criterion -> Arrays.stream(classes).noneMatch(clazz -> clazz.isInstance(criterion))).
                collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    protected <T extends ISearchCriteria> List<T> getCriteria(
            AbstractCompositeSearchCriteria compositeSearchCriteria, Class<T> clazz)
    {
        return (clazz != null)
                ? (List<T>) compositeSearchCriteria.getCriteria().stream().filter(clazz::isInstance)
                        .collect(Collectors.toList())
                : Collections.emptyList();
    }

    protected static <E> Set<E> mergeResults(final SearchOperator operator,
            final Collection<Set<E>>... intermediateResultsToMerge)
    {
        final Collection<Set<E>> intermediateResults = Arrays.stream(intermediateResultsToMerge).reduce(new ArrayList<>(), (sets, sets2) ->
                {
                    if (sets2 != null)
                    {
                        sets.addAll(sets2);
                    }
                    return sets;
                });

        switch (operator)
        {
            case AND:
            {
                return intersection(intermediateResults);
            }
            case OR:
            {
                return union(intermediateResults);
            }
            default:
            {
                throw new IllegalArgumentException("Unexpected value for search operator: " + operator);
            }
        }
    }

    protected static <E> Set<E> intersection(final Collection<Set<E>> sets)
    {
        return !sets.isEmpty() ? sets.stream().reduce(new HashSet<>(sets.iterator().next()), (set1, set2) ->
                {
                    if (set2 != null)
                    {
                        set1.retainAll(set2);
                    }
                    return set1;
                }) : new HashSet<>(0);
    }

    protected static <E> Set<E> union(final Collection<Set<E>> sets)
    {
        return sets.stream().reduce(new HashSet<>(), (set1, set2) ->
                {
                    if (set2 != null)
                    {
                        set1.addAll(set2);
                    }
                    return set1;
                });
    }

    /**
     * Find the smallest set.
     *
     * @param candidates collection of sets to search in.
     * @param <E> types of parameters of the sets.
     * @return the set with the smallest number of items.
     */
    protected static <E> Set<E> getSmallestSet(final Collection<Set<E>> candidates)
    {
        final Set<E> smallestSet = candidates.stream().min((o1, o2) ->
                {
                    if (o1 == null)
                    {
                        return (o2 == null) ? 0 : 1;
                    } else
                    {
                        return (o2 == null) ? -1 : o1.size() - o2.size();
                    }
                }).orElse(null);
        return smallestSet;
    }

    protected ISQLSearchDAO getSearchDAO()
    {
        return searchDAO;
    }

    protected ISQLAuthorisationInformationProviderDAO getAuthProvider()
    {
        return authProvider;
    }

    protected List<Long> doSortIDs(final Collection<Long> filteredIDs, final SortOptions<OBJECT> sortOptions, final TableMapper tableMapper)
    {
        return getSearchDAO().sortIDs(tableMapper, filteredIDs, sortOptions);
    }

    protected <T, C extends AbstractFieldSearchCriteria<T>> C convertToOtherCriterion(final AbstractFieldSearchCriteria<T> criterion,
            IFieldSearchCriterionFactory<C> factory)
    {
        final C result = factory.create();
        result.setFieldValue(criterion.getFieldValue());
        return result;
    }

    protected Set<Long> searchForIDs(final Long userId, final AuthorisationInformation authorisationInformation,
            final AbstractCompositeSearchCriteria criteria, final String idsColumnName,
            final TableMapper tableMapper)
    {
        final Set<Long> mainCriteriaIntermediateResults = getSearchDAO().queryDBForIdsWithGlobalSearchMatchCriteria(userId,
                criteria, tableMapper, idsColumnName, authorisationInformation);

        // If we have results, we use them
        // If we don't have results and criteria are not empty, there are no results.
        final Set<Long> resultBeforeFiltering =
                containsValues(mainCriteriaIntermediateResults) ? mainCriteriaIntermediateResults : Collections.emptySet();

        return filterIDsByUserRights(userId, authorisationInformation, resultBeforeFiltering);
    }

    protected Set<Long> searchForIDsByCriteriaCollection(final Long userId, final AuthorisationInformation authorisationInformation,
            final Collection<ISearchCriteria> criteria, final SearchOperator finalSearchOperator, final TableMapper tableMapper,
            final String idsColumnName)
    {
        if (!criteria.isEmpty())
        {
            final DummyCompositeSearchCriterion containerCriterion = new DummyCompositeSearchCriterion(criteria, finalSearchOperator);
            final Set<Long> mainCriteriaNotFilteredResults = getSearchDAO().queryDBForIdsWithGlobalSearchMatchCriteria(userId, containerCriterion, tableMapper,
                    idsColumnName, authorisationInformation);
            return filterIDsByUserRights(userId, authorisationInformation, mainCriteriaNotFilteredResults);
        } else
        {
            return Collections.emptySet();
        }
    }

}
