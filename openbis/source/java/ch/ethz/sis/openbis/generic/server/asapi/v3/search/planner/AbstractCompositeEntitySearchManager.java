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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractCompositeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetContainerSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchRelation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleContainerSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchRelation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.relationship.IGetRelationshipIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.AuthorisationInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.ISQLAuthorisationInformationProviderDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.dao.ISQLSearchDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.hibernate.IID2PEMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractCompositeEntitySearchManager<CRITERIA extends AbstractCompositeSearchCriteria,
        OBJECT, OBJECT_PE> extends AbstractLocalSearchManager<CRITERIA, OBJECT, OBJECT_PE>
{

    public AbstractCompositeEntitySearchManager(final ISQLSearchDAO searchDAO, final ISQLAuthorisationInformationProviderDAO authProvider,
            final IID2PEMapper<Long, OBJECT_PE> idsTranslator)
    {
        super(searchDAO, authProvider, idsTranslator);
    }

    protected abstract Class<? extends AbstractCompositeSearchCriteria> getParentsSearchCriteriaClass();

    protected abstract Class<? extends AbstractCompositeSearchCriteria> getChildrenSearchCriteriaClass();

    protected abstract CRITERIA createEmptyCriteria();

    protected Set<Long> doSearchForIDs(final Long userId, final AuthorisationInformation authorisationInformation, final CRITERIA criteria, final SearchOperator searchOperator, final String idsColumnName,
            final TableMapper tableMapper)
    {
        final Class<? extends AbstractCompositeSearchCriteria> parentsSearchCriteriaClass = getParentsSearchCriteriaClass();
        final Class<? extends AbstractCompositeSearchCriteria> childrenSearchCriteriaClass = getChildrenSearchCriteriaClass();
        final Collection<ISearchCriteria> parentsCriteria = getCriteria(criteria, parentsSearchCriteriaClass);
        final Collection<ISearchCriteria> childrenCriteria = getCriteria(criteria, childrenSearchCriteriaClass);
        final Collection<ISearchCriteria> mainCriteria;
        if (parentsSearchCriteriaClass != null && childrenSearchCriteriaClass != null)
        {
            mainCriteria = getOtherCriteriaThan(criteria, parentsSearchCriteriaClass, childrenSearchCriteriaClass);
        } else if (parentsSearchCriteriaClass == null && childrenSearchCriteriaClass == null)
        {
            mainCriteria = criteria.getCriteria();
        } else
        {
            throw new RuntimeException("Either both or none of parent/child search criteria should be null.");
        }
        final SearchOperator finalSearchOperator = (searchOperator == null) ? criteria.getOperator() : searchOperator;

        return doSearchForIDs(userId, parentsCriteria, childrenCriteria, mainCriteria, finalSearchOperator, idsColumnName, tableMapper,
                authorisationInformation);
    }

    protected Set<Long> doSearchForIDs(final Long userId,
            final Collection<ISearchCriteria> upstreamRelationshipsCriteria,
            final Collection<ISearchCriteria> downstreamRelationshipsCriteria,
            final Collection<ISearchCriteria> mainCriteria,
            final SearchOperator finalSearchOperator, final String idsColumnName, final TableMapper tableMapper,
            final AuthorisationInformation authorisationInformation)
    {
        // upstreamRelationshipsCriteria & downstreamRelationshipsCriteria are relationships from the relationships
        // table, for datasets it is both the parent-child relationships and containers but for samples it is only the
        // parent-child ones.
        final Set<Long> mainCriteriaIntermediateResults;
        if (!mainCriteria.isEmpty())
        {
            // The main criteria have no recursive ISearchCriteria into it, to facilitate building a query
            final DummyCompositeSearchCriterion containerCriterion = new DummyCompositeSearchCriterion(mainCriteria, finalSearchOperator);
            mainCriteriaIntermediateResults = getSearchDAO().queryDBForIdsAndRanksWithNonRecursiveCriteria(userId, containerCriterion, tableMapper,
                    idsColumnName, authorisationInformation);
        } else
        {
            mainCriteriaIntermediateResults = null;
        }

        final Set<Long> parentCriteriaIntermediateResults;
        final Set<Long> containerCriteriaIntermediateResults;
        if (!upstreamRelationshipsCriteria.isEmpty())
        {
            final Collection<ISearchCriteria> filteredParentsCriteria =
                    getCriteriaByRelationshipType(upstreamRelationshipsCriteria,
                            IGetRelationshipIdExecutor.RelationshipType.PARENT_CHILD);

            if (!filteredParentsCriteria.isEmpty())
            {
                final Set<Long> finalParentIds = findFinalRelationshipIds(userId, authorisationInformation,
                        finalSearchOperator, filteredParentsCriteria, tableMapper);
                final Set<Long> finalParentIdsFiltered = filterIDsByUserRights(userId, authorisationInformation,
                        finalParentIds);
                parentCriteriaIntermediateResults = getChildrenIdsOf(finalParentIdsFiltered, tableMapper,
                        IGetRelationshipIdExecutor.RelationshipType.PARENT_CHILD);
            } else
                {
                parentCriteriaIntermediateResults = null;
            }

            final Collection<ISearchCriteria> filteredContainersCriteria =
                    getCriteriaByRelationshipType(upstreamRelationshipsCriteria,
                            IGetRelationshipIdExecutor.RelationshipType.CONTAINER_COMPONENT);

            if (!filteredContainersCriteria.isEmpty())
            {
                final Set<Long> finalContainerIds = findFinalRelationshipIds(userId, authorisationInformation,
                        finalSearchOperator, filteredContainersCriteria, tableMapper);
                final Set<Long> finalContainerIdsFiltered = filterIDsByUserRights(userId, authorisationInformation,
                        finalContainerIds);
                containerCriteriaIntermediateResults = getChildrenIdsOf(finalContainerIdsFiltered, tableMapper,
                        IGetRelationshipIdExecutor.RelationshipType.CONTAINER_COMPONENT);
            } else
            {
                containerCriteriaIntermediateResults = null;
            }
        } else
        {
            parentCriteriaIntermediateResults = null;
            containerCriteriaIntermediateResults = null;
        }

        final Set<Long> childrenCriteriaIntermediateResults;
        if (!downstreamRelationshipsCriteria.isEmpty())
        {
            final Collection<ISearchCriteria> filteredChildrenCriteria =
                    getCriteriaByRelationshipType(downstreamRelationshipsCriteria,
                            IGetRelationshipIdExecutor.RelationshipType.PARENT_CHILD);

            final Set<Long> finalChildrenIds = findFinalRelationshipIds(userId, authorisationInformation,
                    finalSearchOperator, filteredChildrenCriteria, tableMapper);
            final Set<Long> finalChildrenIdsFiltered = filterIDsByUserRights(userId,
                    authorisationInformation, finalChildrenIds);
            childrenCriteriaIntermediateResults = getParentsIdsOf(finalChildrenIdsFiltered, tableMapper,
                    IGetRelationshipIdExecutor.RelationshipType.PARENT_CHILD);
        } else
        {
            childrenCriteriaIntermediateResults = null;
        }

        // Reaching this point we have the intermediate results of all recursive queries
        final Set<Long> results;
        if (containsValues(mainCriteriaIntermediateResults) || containsValues(parentCriteriaIntermediateResults)
                || containsValues(containerCriteriaIntermediateResults)
                || containsValues(childrenCriteriaIntermediateResults))
        {
            // If we have results, we merge them
            results = mergeResults(finalSearchOperator,
                    mainCriteriaIntermediateResults != null
                            ? Collections.singleton(mainCriteriaIntermediateResults) : Collections.emptySet(),
                    childrenCriteriaIntermediateResults != null
                            ? Collections.singleton(childrenCriteriaIntermediateResults) : Collections.emptySet(),
                    parentCriteriaIntermediateResults != null
                            ? Collections.singleton(parentCriteriaIntermediateResults) : Collections.emptySet(),
                    containerCriteriaIntermediateResults != null
                            ? Collections.singleton(containerCriteriaIntermediateResults) : Collections.emptySet());
        } else if (mainCriteria.isEmpty() && upstreamRelationshipsCriteria.isEmpty() && downstreamRelationshipsCriteria.isEmpty())
        {
            // If we don't have results and criteria are empty, return all.
            results = getAllIds(userId, authorisationInformation, idsColumnName, tableMapper);
        } else
        {
            // If we don't have results and criteria are not empty, there are no results.
            results = Collections.emptySet();
        }

        return filterIDsByUserRights(userId, authorisationInformation, results);
    }

    private Collection<ISearchCriteria> getCriteriaByRelationshipType(final Collection<ISearchCriteria> criteria,
            IGetRelationshipIdExecutor.RelationshipType relationshipType)
    {
        switch (relationshipType)
        {
            case PARENT_CHILD:
            {
                return criteria.stream().filter(criterion -> !isContainerCriterion(criterion))
                        .collect(Collectors.toList());
            }
            case CONTAINER_COMPONENT:
            {
                return criteria.stream().filter(this::isContainerCriterion)
                        .collect(Collectors.toList());
            }
            default:
            {
                throw new IllegalArgumentException();
            }
        }
    }

    private boolean isContainerCriterion(final ISearchCriteria criterion)
    {
        return criterion instanceof DataSetContainerSearchCriteria &&
                ((DataSetContainerSearchCriteria) criterion).getRelation() ==
                        DataSetSearchRelation.CONTAINER ||
        criterion instanceof SampleContainerSearchCriteria &&
                ((SampleContainerSearchCriteria) criterion).getRelation() ==
                        SampleSearchRelation.CONTAINER;
    }

    /**
     * Returns IDs using parent or child relationship criteria.
     *
     * @param authorisationInformation
     * @param operator the operator used to merge the results.
     * @param relatedEntitiesCriteria parent or child criteria.
     * @param tableMapper
     * @return IDs found from parent/child criteria.
     */
    @SuppressWarnings("unchecked")
    private Set<Long> findFinalRelationshipIds(final Long userId, final AuthorisationInformation authorisationInformation, final SearchOperator operator,
            final Collection<ISearchCriteria> relatedEntitiesCriteria, final TableMapper tableMapper)
    {
        final List<Set<Long>> relatedIds = relatedEntitiesCriteria.stream().flatMap(entitySearchCriteria ->
        {
            final Set<Long> foundParentIds = doSearchForIDs(userId, authorisationInformation,
                    (CRITERIA) entitySearchCriteria, operator, ColumnNames.ID_COLUMN, tableMapper);
            return foundParentIds.isEmpty() ? Stream.empty() : Stream.of(foundParentIds);
        }).collect(Collectors.toList());

        return mergeResults(operator, relatedIds);
    }

    // These methods require a simple SQL query to the database

    /**
     * Queries the DB to return all entity IDs.
     *
     * @return set of IDs of all entities.
     * @param userId requesting user ID.
     * @param authorisationInformation user authorisation information.
     * @param idsColumnName the name of the column, whose values to be returned.
     * @param tableMapper the table mapper to be used during translation.
     */
    protected Set<Long> getAllIds(final Long userId, final AuthorisationInformation authorisationInformation, final String idsColumnName,
            final TableMapper tableMapper)
    {
        final CRITERIA criteria = createEmptyCriteria();
        final DummyCompositeSearchCriterion containerCriterion = new DummyCompositeSearchCriterion();
        containerCriterion.setCriteria(Collections.singletonList(criteria));
        return getSearchDAO().queryDBForIdsAndRanksWithNonRecursiveCriteria(userId, containerCriterion, tableMapper, idsColumnName, authorisationInformation);
    }

    private Set<Long> getChildrenIdsOf(final Set<Long> parentIdSet, final TableMapper tableMapper,
            final IGetRelationshipIdExecutor.RelationshipType relationshipType)
    {
        return getSearchDAO().findChildIDs(tableMapper, parentIdSet, relationshipType);
    }

    private Set<Long> getParentsIdsOf(final Set<Long> childIdSet, final TableMapper tableMapper,
            final IGetRelationshipIdExecutor.RelationshipType relationshipType)
    {
        return getSearchDAO().findParentIDs(tableMapper, childIdSet, relationshipType);
    }

}
