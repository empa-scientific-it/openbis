/*
 * Copyright 2015 ETH Zuerich, CISD
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
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IdListUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue.ListUpdateAction;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue.ListUpdateActionAdd;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue.ListUpdateActionRemove;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue.ListUpdateActionSet;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.UpdateRelationProgress;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentityHolder;

/**
 * @author pkupczyk
 */
public abstract class AbstractUpdateEntityToManyRelationExecutor<ENTITY_UPDATE extends IUpdate, ENTITY_PE extends IIdentityHolder, RELATED_ID, RELATED_PE>
        implements
        IUpdateEntityRelationsWithCacheExecutor<ENTITY_UPDATE, ENTITY_PE, RELATED_ID, RELATED_PE>
{

    @Resource(name = ComponentNames.RELATIONSHIP_SERVICE)
    protected IRelationshipService relationshipService;

    @Override
    public void update(final IOperationContext context, final MapBatch<ENTITY_UPDATE, ENTITY_PE> batch, final Map<RELATED_ID, RELATED_PE> relatedMap)
    {
        final Collection<RELATED_PE> allAdded = new HashSet<RELATED_PE>();
        final Collection<RELATED_PE> allRemoved = new HashSet<RELATED_PE>();

        new MapBatchProcessor<ENTITY_UPDATE, ENTITY_PE>(context, batch)
            {
                @Override
                public void process(ENTITY_UPDATE update, ENTITY_PE entity)
                {
                    IdListUpdateValue<? extends RELATED_ID> listUpdate = getRelatedUpdate(context, update);

                    if (listUpdate != null && listUpdate.hasActions())
                    {
                        for (ListUpdateAction<? extends RELATED_ID> action : listUpdate.getActions())
                        {
                            Collection<RELATED_PE> relatedCollection = new LinkedList<RELATED_PE>();

                            if (action instanceof ListUpdateActionSet<?> || action instanceof ListUpdateActionAdd<?>)
                            {
                                for (RELATED_ID relatedId : action.getItems())
                                {
                                    RELATED_PE related = relatedMap.get(relatedId);
                                    if (related == null)
                                    {
                                        throw new ObjectNotFoundException((IObjectId) relatedId);
                                    }
                                    check(context, entity, relatedId, related);
                                    relatedCollection.add(related);
                                }
                                if (action instanceof ListUpdateActionSet<?>)
                                {
                                    set(context, entity, relatedCollection, allAdded, allRemoved);
                                } else
                                {
                                    add(context, entity, relatedCollection, allAdded);
                                }
                            } else if (action instanceof ListUpdateActionRemove<?>)
                            {
                                for (RELATED_ID relatedId : action.getItems())
                                {
                                    RELATED_PE related = relatedMap.get(relatedId);
                                    if (related != null)
                                    {
                                        relatedCollection.add(related);
                                        check(context, entity, relatedId, related);
                                    }
                                }
                                remove(context, entity, relatedCollection, allRemoved);
                            }
                        }
                    }
                    Collection<? extends RELATED_ID> relatedIds = getRelatedForRelationshipUpdate(context, update);
                    for (RELATED_ID relatedId : relatedIds)
                    {
                        RELATED_PE related = relatedMap.get(relatedId);
                        updateRelationships(context, update, entity, relatedId, related);
                    }
                }

                @Override
                public IProgress createProgress(ENTITY_UPDATE key, ENTITY_PE value, int objectIndex, int totalObjectCount)
                {
                    return new UpdateRelationProgress(key, value, getRelationName(), objectIndex, totalObjectCount);
                }
            };

        postUpdate(context, allAdded, allRemoved);
    }

    protected void postUpdate(IOperationContext context, Collection<RELATED_PE> allAdded, Collection<RELATED_PE> allRemoved)
    {
        // by default do nothing
    }

    protected void set(IOperationContext context, ENTITY_PE entity, Collection<RELATED_PE> related, Collection<RELATED_PE> allAdded,
            Collection<RELATED_PE> allRemoved)
    {
        Set<RELATED_PE> existingRelated = new HashSet<RELATED_PE>(getCurrentlyRelated(entity));
        Set<RELATED_PE> newRelated = new HashSet<RELATED_PE>(related);

        for (RELATED_PE anExistingRelated : existingRelated)
        {
            if (false == newRelated.contains(anExistingRelated))
            {
                remove(context, entity, anExistingRelated);
                allRemoved.add(anExistingRelated);
            }
        }

        for (RELATED_PE aNewRelated : newRelated)
        {
            if (false == existingRelated.contains(aNewRelated))
            {
                add(context, entity, aNewRelated);
                allAdded.add(aNewRelated);
            }
        }
    }

    protected void add(IOperationContext context, ENTITY_PE entity, Collection<RELATED_PE> related, Collection<RELATED_PE> allAdded)
    {
        Set<RELATED_PE> existingRelated = new HashSet<RELATED_PE>(getCurrentlyRelated(entity));

        for (RELATED_PE aRelated : related)
        {
            if (false == existingRelated.contains(aRelated))
            {
                add(context, entity, aRelated);
                allAdded.add(aRelated);
            }
        }
    }

    protected void remove(IOperationContext context, ENTITY_PE entity, Collection<RELATED_PE> related, Collection<RELATED_PE> allRemoved)
    {
        Set<RELATED_PE> existingRelated = new HashSet<RELATED_PE>(getCurrentlyRelated(entity));

        for (RELATED_PE aRelated : related)
        {
            if (existingRelated.contains(aRelated))
            {
                remove(context, entity, aRelated);
                allRemoved.add(aRelated);
            }
        }
    }

    protected Collection<? extends RELATED_ID> getRelatedForRelationshipUpdate(IOperationContext context, ENTITY_UPDATE update)
    {
        return Collections.emptySet();
    }

    protected void updateRelationships(IOperationContext context, ENTITY_UPDATE update, ENTITY_PE entity, RELATED_ID relatedId, RELATED_PE related)
    {
    }

    protected abstract String getRelationName();

    protected abstract Collection<RELATED_PE> getCurrentlyRelated(ENTITY_PE entity);

    protected abstract IdListUpdateValue<? extends RELATED_ID> getRelatedUpdate(IOperationContext context, ENTITY_UPDATE update);

    protected abstract void check(IOperationContext context, ENTITY_PE entity, RELATED_ID relatedId, RELATED_PE related);

    protected abstract void add(IOperationContext context, ENTITY_PE entity, RELATED_PE related);

    protected abstract void remove(IOperationContext context, ENTITY_PE entity, RELATED_PE related);

}
