/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatch;
import ch.systemsx.cisd.common.collection.CycleFoundException;
import ch.systemsx.cisd.common.collection.GroupingDAG;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;

/**
 * @author pkupczyk
 */
@Component
public abstract class AbstractVerifyEntityCyclesExecutor<ENTITY_PE> implements IVerifyEntityRelationsExecutor<ENTITY_PE>
{

    @Autowired
    protected IDAOFactory daoFactory;

    @Override
    public void verify(IOperationContext context, CollectionBatch<ENTITY_PE> batch)
    {
        Map<Long, Collection<Long>> graph = getGraph(context, batch.getObjects());

        checkCycles(graph);
    }

    private Map<Long, Collection<Long>> getGraph(IOperationContext context, Collection<ENTITY_PE> entities)
    {
        Map<Long, Collection<Long>> relationsMap = new LinkedHashMap<Long, Collection<Long>>();
        Set<Long> currentLevelIds = new HashSet<Long>();
        Set<Long> visitedIds = new HashSet<Long>();

        for (ENTITY_PE entity : entities)
        {
            Long entityId = getId(entity);
            currentLevelIds.add(entityId);
        }

        while (false == currentLevelIds.isEmpty())
        {
            Map<Long, Set<Long>> relatedIdsMap = getRelatedIdsMap(context, currentLevelIds);

            visitedIds.addAll(currentLevelIds);
            currentLevelIds = new HashSet<Long>();

            for (Map.Entry<Long, Set<Long>> relatedIdsEntry : relatedIdsMap.entrySet())
            {
                Long entityId = relatedIdsEntry.getKey();
                Set<Long> relatedIds = relatedIdsEntry.getValue();
                relationsMap.put(entityId, relatedIds);

                for (Long relatedId : relatedIds)
                {
                    if (false == visitedIds.contains(relatedId))
                    {
                        currentLevelIds.add(relatedId);
                    }
                }
            }
        }

        return relationsMap;
    }

    private void checkCycles(Map<Long, Collection<Long>> graph)
    {
        try
        {
            GroupingDAG.groupByDepencies(graph);
        } catch (CycleFoundException e)
        {
            Long entityId = (Long) e.getCycleRoot();
            String entityIdentifier = getIdentifier(entityId);
            throw new UserFailureException("Circular dependency found: " + entityIdentifier, e);
        }
    }

    protected abstract Long getId(ENTITY_PE entity);

    protected abstract String getIdentifier(Long entityId);

    protected abstract Map<Long, Set<Long>> getRelatedIdsMap(IOperationContext context, Set<Long> entityIds);

}
