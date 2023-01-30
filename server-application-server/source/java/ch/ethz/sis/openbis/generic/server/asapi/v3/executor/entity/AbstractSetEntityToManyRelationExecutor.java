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
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.ICreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.SetRelationProgress;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentityHolder;

/**
 * @author pkupczyk
 */
public abstract class AbstractSetEntityToManyRelationExecutor<ENTITY_CREATION extends ICreation, ENTITY_PE extends IIdentityHolder, RELATED_ID extends IObjectId, RELATED_PE>
{

    @Resource(name = ComponentNames.RELATIONSHIP_SERVICE)
    protected IRelationshipService relationshipService;

    public void set(final IOperationContext context, final MapBatch<ENTITY_CREATION, ENTITY_PE> batch)
    {
        final Map<ENTITY_CREATION, Map<RELATED_ID, RELATED_PE>> relatedMap = getRelatedMap(context, batch);

        new MapBatchProcessor<ENTITY_CREATION, ENTITY_PE>(context, batch)
            {
                @Override
                public void process(ENTITY_CREATION creation, ENTITY_PE entity)
                {
                    Map<RELATED_ID, RELATED_PE> relatedByIds = relatedMap.get(creation);

                    if (relatedByIds != null && false == relatedByIds.isEmpty())
                    {
                        setRelated(context, entity, relatedByIds.values());
                    }
                }

                @Override
                public IProgress createProgress(ENTITY_CREATION creation, ENTITY_PE entity, int objectIndex, int totalObjectCount)
                {
                    return new SetRelationProgress(entity, creation, getRelationName(), objectIndex, totalObjectCount);
                }
            };

        postSet(context, batch, relatedMap);
    }

    private Map<ENTITY_CREATION, Map<RELATED_ID, RELATED_PE>> getRelatedMap(final IOperationContext context,
            final MapBatch<ENTITY_CREATION, ENTITY_PE> batch)
    {
        final Map<ENTITY_CREATION, Map<RELATED_ID, RELATED_PE>> relatedMap =
                new IdentityHashMap<ENTITY_CREATION, Map<RELATED_ID, RELATED_PE>>();
        final Set<RELATED_ID> toLoadIds = new HashSet<RELATED_ID>();

        new MapBatchProcessor<ENTITY_CREATION, ENTITY_PE>(context, batch)
            {

                @Override
                public void process(ENTITY_CREATION creation, ENTITY_PE entity)
                {
                    Collection<? extends RELATED_ID> relatedIds = getRelatedIds(context, creation);
                    if (relatedIds != null)
                    {
                        toLoadIds.addAll(relatedIds);
                    }
                }

                @Override
                public IProgress createProgress(ENTITY_CREATION creation, ENTITY_PE entity, int objectIndex, int totalObjectCount)
                {
                    return new SetRelationProgress(entity, creation, getRelationName(), objectIndex, totalObjectCount);
                }
            };

        if (false == toLoadIds.isEmpty())
        {
            final Map<RELATED_ID, RELATED_PE> loadedMap = map(context, toLoadIds);
            final Set<RELATED_PE> checked = new HashSet<RELATED_PE>();

            new MapBatchProcessor<ENTITY_CREATION, ENTITY_PE>(context, batch)
                {
                    @Override
                    public void process(ENTITY_CREATION creation, ENTITY_PE entity)
                    {
                        Collection<? extends RELATED_ID> relatedIds = getRelatedIds(context, creation);

                        if (relatedIds != null)
                        {
                            Map<RELATED_ID, RELATED_PE> relatedByIds = new LinkedHashMap<>();

                            for (RELATED_ID relatedId : relatedIds)
                            {
                                RELATED_PE related = loadedMap.get(relatedId);

                                if (related == null)
                                {
                                    throw new ObjectNotFoundException(relatedId);
                                } else
                                {
                                    if (false == checked.contains(related))
                                    {
                                        check(context, relatedId, related);
                                        checked.add(related);
                                    }
                                    relatedByIds.put(relatedId, related);
                                }
                            }

                            relatedMap.put(creation, relatedByIds);
                        }
                    }

                    @Override
                    public IProgress createProgress(ENTITY_CREATION creation, ENTITY_PE entity, int objectIndex, int totalObjectCount)
                    {
                        return new SetRelationProgress(entity, creation, getRelationName(), objectIndex, totalObjectCount);
                    }
                };
        }

        return relatedMap;
    }

    protected void postSet(IOperationContext context, MapBatch<ENTITY_CREATION, ENTITY_PE> batch,
            Map<ENTITY_CREATION, Map<RELATED_ID, RELATED_PE>> relatedMap)
    {
        Collection<RELATED_PE> allSet = new HashSet<RELATED_PE>();
        for (Map<RELATED_ID, RELATED_PE> map : relatedMap.values())
        {
            allSet.addAll(map.values());
        }
        postSet(context, allSet);
    }

    protected void postSet(IOperationContext context, Collection<RELATED_PE> allSet)
    {
        // by default do nothing
    }

    protected abstract RELATED_ID getCreationId(IOperationContext context, ENTITY_CREATION creation);

    protected abstract String getRelationName();

    protected abstract Collection<? extends RELATED_ID> getRelatedIds(IOperationContext context, ENTITY_CREATION creation);

    protected abstract Map<RELATED_ID, RELATED_PE> map(IOperationContext context, Collection<? extends RELATED_ID> relatedIds);

    protected abstract void check(IOperationContext context, RELATED_ID relatedId, RELATED_PE related);

    protected abstract void setRelated(IOperationContext context, ENTITY_PE entity, Collection<RELATED_PE> relatedByIds);

}
