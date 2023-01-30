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

package ch.systemsx.cisd.openbis.generic.server.business.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IHibernateSearchDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchSubCriteria;

/**
 * Manages detailed search with complex search criteria.
 * 
 * @author Piotr Buczek
 * @author Kaloyan Enimanev
 */
public class AbstractSearchManager<T>
{
    protected final IHibernateSearchDAO searchDAO;

    protected final T lister;

    public AbstractSearchManager(IHibernateSearchDAO searchDAO, T lister)
    {
        this.searchDAO = searchDAO;
        this.lister = lister;
    }

    protected Collection<Long> restrictResultSetIfNecessary(Collection<Long> ids)
    {
        if (ids == null)
        {
            return new ArrayList<Long>();
        } else
        {
            int maxSize = searchDAO.getResultSetSizeLimit();

            if (ids.size() <= maxSize)
            {
                return ids;
            }
            return new ArrayList<Long>(ids).subList(0, maxSize);
        }
    }

    protected void mergeSubCriteria(DetailedSearchCriteria criteria,
            DetailedSearchSubCriteria subCriteriaToMerge)
    {
        criteria.getCriteria().addAll(subCriteriaToMerge.getCriteria().getCriteria());
        criteria.setConnection(subCriteriaToMerge.getCriteria().getConnection());
        criteria.setUseWildcardSearchMode(subCriteriaToMerge.getCriteria()
                .isUseWildcardSearchMode());
    }

    interface IRelationshipHandler
    {
        Collection<Long> findRelatedIdsByCriteria(String userId, DetailedSearchCriteria criteria,
                List<DetailedSearchSubCriteria> otherSubCriterias);

        Map<Long, Set<Long>> listIdsToRelatedIds(Collection<Long> ids);

        Map<Long, Set<Long>> listRelatedIdsToIds(Collection<Long> relatedIds);
    }

    protected Collection<Long> filterSearchResultsBySubcriteria(String userId,
            Collection<Long> idsToFilter, DetailedSearchCriteria criteria,
            IRelationshipHandler relationshipHandler)
    {
        Collection<Long> relatedIds =
                relationshipHandler.findRelatedIdsByCriteria(userId, criteria,
                        Collections.<DetailedSearchSubCriteria> emptyList());

        if (idsToFilter == null)
        {
            Map<Long, Set<Long>> relatedIdsToIds =
                    relationshipHandler.listRelatedIdsToIds(relatedIds);
            Set<Long> result = new HashSet<Long>();

            for (Set<Long> relatedIdsIds : relatedIdsToIds.values())
            {
                result.addAll(relatedIdsIds);
            }

            return result;
        } else
        {
            if (idsToFilter.size() > relatedIds.size())
            {
                Map<Long, Set<Long>> relatedIdsToIds =
                        relationshipHandler.listRelatedIdsToIds(relatedIds);
                return intersection(idsToFilter, relatedIdsToIds.values());
            } else
            {
                Map<Long, Set<Long>> idsToRelatedIds =
                        relationshipHandler.listIdsToRelatedIds(idsToFilter);
                return filteIdsByRelationship(idsToFilter, relatedIds, idsToRelatedIds);
            }
        }
    }

    /**
     * @return the intersection of a collection and a multi set (collection of sets).
     */
    protected Collection<Long> intersection(Collection<Long> collection,
            Collection<Set<Long>> multiSet)
    {
        Set<Long> intersection = new HashSet<Long>();
        for (Set<Long> set : multiSet)
        {
            set.retainAll(collection);
            intersection.addAll(set);
        }
        return intersection;
    }

    /**
     * Filters search results by a relationship. This comes handy when filtering search results based on a subcriteria which operates on a different
     * entity than the encapsulating criteria.
     * 
     * @param idsToFilter the ids to be filtered.
     * @param relatedIds ids matching a subcriteria
     * @param relationshipMap a relationship map in the form <id, set<related-ids>>
     * @return all id-s having relationship to at least on id from relatedIds.
     */
    protected Collection<Long> filteIdsByRelationship(Collection<Long> idsToFilter,
            Collection<Long> relatedIds, Map<Long, Set<Long>> relationshipMap)
    {
        for (Iterator<Long> iterator = idsToFilter.iterator(); iterator.hasNext();)
        {
            Long id = iterator.next();
            Set<Long> relatedIdSet = relationshipMap.get(id);
            if (relatedIdSet == null || Collections.disjoint(relatedIds, relatedIdSet))
            {
                iterator.remove();
            }
        }
        return idsToFilter;
    }

}
