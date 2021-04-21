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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.*;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.AuthorisationInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.ISQLAuthorisationInformationProviderDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.dao.ISQLSearchDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.hibernate.IID2PEMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages detailed search with complex search criteria.
 *
 * @author Viktor Kovtun
 */
public abstract class AbstractLocalSearchManager<CRITERIA extends ISearchCriteria, OBJECT, OBJECT_PE>
        extends AbstractSearchManager<OBJECT>
        implements ILocalSearchManager<CRITERIA, OBJECT, OBJECT_PE>
{

    private final IID2PEMapper<Long, OBJECT_PE> idsTranslator;

    public AbstractLocalSearchManager(final ISQLSearchDAO searchDAO, final ISQLAuthorisationInformationProviderDAO authProvider,
            final IID2PEMapper<Long, OBJECT_PE> idsTranslator)
    {
        super(authProvider, searchDAO);
        this.idsTranslator = idsTranslator;
    }

    protected List<ISearchCriteria> getOtherCriteriaThan(final AbstractCompositeSearchCriteria searchCriteria,
            final Class<? extends ISearchCriteria>... classes)
    {
        return searchCriteria.getCriteria().stream().filter(
                criterion -> Arrays.stream(classes).noneMatch(clazz -> clazz.isInstance(criterion))).
                collect(Collectors.toList());
    }

    protected static <E> Set<E> mergeResults(final SearchOperator operator,
            final Collection<Set<E>>... intermediateResultsToMerge)
    {
        final Collection<Set<E>> intermediateResults = Arrays.stream(intermediateResultsToMerge).reduce(
                new ArrayList<>(), (sets1, sets2) ->
                {
                    if (sets2 != null)
                    {
                        sets1.addAll(sets2);
                    }
                    return sets1;
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

    public Collection<OBJECT_PE> map(final Collection<Long> ids)
    {
        return idsTranslator.map(ids);
    }

    protected <T, C extends AbstractFieldSearchCriteria<T>> C convertToOtherCriterion(final AbstractFieldSearchCriteria<T> criterion,
            IFieldSearchCriterionFactory<C> factory)
    {
        final C result = factory.create();
        result.setFieldValue(criterion.getFieldValue());
        return result;
    }

    protected Set<Long> searchForIDs(final Long userId, final AuthorisationInformation authorisationInformation,
            final AbstractCompositeSearchCriteria criteria, final String idsColumnName, final TableMapper tableMapper)
    {
        final AbstractCompositeSearchCriteria emptyCriteria = createEmptyCriteria();
        final List<CRITERIA> nestedCriteria = (List<CRITERIA>) getCriteria(criteria, emptyCriteria.getClass());
        final List<ISearchCriteria> mainCriteria = getOtherCriteriaThan(criteria, emptyCriteria.getClass());

        final AbstractCompositeSearchCriteria containerCriterion = createEmptyCriteria();
        containerCriterion.withOperator(criteria.getOperator());
        containerCriterion.setCriteria(mainCriteria);

        final Set<Long> mainCriteriaIntermediateResults;
        if (!mainCriteria.isEmpty())
        {
            mainCriteriaIntermediateResults = getSearchDAO().queryDBForIdsWithGlobalSearchMatchCriteria(
                    userId, containerCriterion, tableMapper, idsColumnName, authorisationInformation);
        } else
        {
            mainCriteriaIntermediateResults = null;
        }

        final Collection<Set<Long>> nestedCriteriaIntermediateResults;
        if (!nestedCriteria.isEmpty())
        {
            nestedCriteriaIntermediateResults = nestedCriteria.stream().map(nestedCriterion ->
                    searchForIDs(userId, authorisationInformation, nestedCriterion, null, idsColumnName))
                    .collect(Collectors.toList());
        } else
        {
            nestedCriteriaIntermediateResults = Collections.emptyList();
        }

        final Set<Long> resultBeforeFiltering;
        if (containsValues(mainCriteriaIntermediateResults) || containsValues(nestedCriteriaIntermediateResults))
        {
            // If we have results, we merge them
            resultBeforeFiltering = mergeResults(criteria.getOperator(),
                    mainCriteriaIntermediateResults != null
                            ? Collections.singleton(mainCriteriaIntermediateResults) : Collections.emptySet(),
                    nestedCriteriaIntermediateResults);
        } else if (mainCriteria.isEmpty() && nestedCriteria.isEmpty())
        {
            // If we don't have results and criteria are empty, return all.
            resultBeforeFiltering = getAllIds(userId, authorisationInformation, idsColumnName, tableMapper,
                    containerCriterion);
        } else
        {
            // If we don't have results and criteria are not empty, there are no results.
            resultBeforeFiltering = Collections.emptySet();
        }

        return filterIDsByUserRights(userId, authorisationInformation, resultBeforeFiltering);
    }

    protected Set<Long> searchForIDsByCriteriaCollection(final Long userId,
            final AuthorisationInformation authorisationInformation, final Collection<ISearchCriteria> criteria,
            final SearchOperator finalSearchOperator, final TableMapper tableMapper,
            final String idsColumnName)
    {
        if (!criteria.isEmpty())
        {
            final DummyCompositeSearchCriterion containerCriterion =
                    new DummyCompositeSearchCriterion(criteria, finalSearchOperator);
            final Set<Long> mainCriteriaNotFilteredResults = getSearchDAO().queryDBForIdsWithGlobalSearchMatchCriteria(userId,
                    containerCriterion, tableMapper, idsColumnName, authorisationInformation);
            return filterIDsByUserRights(userId, authorisationInformation, mainCriteriaNotFilteredResults);
        } else
        {
            return Collections.emptySet();
        }
    }

    protected abstract AbstractCompositeSearchCriteria createEmptyCriteria();

    /**
     * Queries the DB to return all entity IDs.
     *
     * @return set of IDs of all entities.
     * @param userId requesting user ID.
     * @param authorisationInformation user authorisation information.
     * @param idsColumnName the name of the column, whose values to be returned.
     * @param tableMapper the table mapper to be used during translation.
     * @param containerCriterion container criterion which can be ignored or reused in overridden methods.
     */
    protected Set<Long> getAllIds(final Long userId, final AuthorisationInformation authorisationInformation,
            final String idsColumnName, final TableMapper tableMapper,
            final AbstractCompositeSearchCriteria containerCriterion)
    {
        final AbstractCompositeSearchCriteria emptyCriteria = createEmptyCriteria();
        final AbstractCompositeSearchCriteria emptyContainerCriterion = createEmptyCriteria();
        emptyContainerCriterion.setCriteria(Collections.singletonList(emptyCriteria));
        return getSearchDAO().queryDBForIdsWithGlobalSearchMatchCriteria(userId, emptyContainerCriterion,
                tableMapper, idsColumnName, authorisationInformation);
    }

}
