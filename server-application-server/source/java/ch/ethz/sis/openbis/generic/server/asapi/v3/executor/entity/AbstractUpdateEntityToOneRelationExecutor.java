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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.UpdateRelationProgress;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentityHolder;

/**
 * @author pkupczyk
 */
public abstract class AbstractUpdateEntityToOneRelationExecutor<ENTITY_UPDATE extends IUpdate, ENTITY_PE extends IIdentityHolder, RELATED_ID, RELATED_PE>
        implements IUpdateEntityRelationsExecutor<ENTITY_UPDATE, ENTITY_PE>,
        IUpdateEntityRelationsWithCacheExecutor<ENTITY_UPDATE, ENTITY_PE, RELATED_ID, RELATED_PE>
{

    @Autowired
    protected IRelationshipService relationshipService;

    @Autowired
    protected ICommonBusinessObjectFactory boFactory;

    @Override
    public void update(IOperationContext context, MapBatch<ENTITY_UPDATE, ENTITY_PE> batch)
    {
        List<RELATED_ID> relatedIds = new LinkedList<RELATED_ID>();

        for (ENTITY_UPDATE update : batch.getObjects().keySet())
        {
            FieldUpdateValue<RELATED_ID> relatedUpdate = getRelatedUpdate(update);

            if (relatedUpdate != null && relatedUpdate.isModified())
            {
                relatedIds.add(relatedUpdate.getValue());
            }
        }

        if (false == relatedIds.isEmpty())
        {
            Map<RELATED_ID, RELATED_PE> relatedMap = map(context, relatedIds);
            updateCommon(context, batch, relatedMap);
        }
    }

    @Override
    public void update(IOperationContext context, MapBatch<ENTITY_UPDATE, ENTITY_PE> batch, Map<RELATED_ID, RELATED_PE> relatedMap)
    {
        updateCommon(context, batch, relatedMap);
    }

    private void updateCommon(final IOperationContext context, final MapBatch<ENTITY_UPDATE, ENTITY_PE> batch,
            final Map<RELATED_ID, RELATED_PE> relatedMap)
    {
        final Set<RELATED_PE> allAdded = new HashSet<RELATED_PE>();
        final Set<RELATED_PE> allRemoved = new HashSet<RELATED_PE>();

        new MapBatchProcessor<ENTITY_UPDATE, ENTITY_PE>(context, batch)
            {
                @Override
                public void process(ENTITY_UPDATE update, ENTITY_PE entity)
                {
                    FieldUpdateValue<RELATED_ID> relatedUpdate = getRelatedUpdate(update);
                    RELATED_PE currentlyRelated = getCurrentlyRelated(entity);

                    if (relatedUpdate != null && relatedUpdate.isModified())
                    {
                        RELATED_ID relatedId = relatedUpdate.getValue();

                        if (relatedId == null)
                        {
                            if (currentlyRelated != null)
                            {
                                check(context, entity, getRelatedId(currentlyRelated), currentlyRelated);
                                update(context, entity, null);
                                allRemoved.add(currentlyRelated);
                            }
                        } else
                        {
                            RELATED_PE related = relatedMap.get(relatedId);

                            if (related == null)
                            {
                                throw new ObjectNotFoundException((IObjectId) relatedId);
                            }

                            if (false == related.equals(currentlyRelated))
                            {
                                check(context, entity, relatedId, related);
                                update(context, entity, related);
                                allAdded.add(related);
                                if (currentlyRelated != null)
                                {
                                    allRemoved.add(currentlyRelated);
                                }
                            }
                        }

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

    protected abstract String getRelationName();

    protected abstract RELATED_ID getRelatedId(RELATED_PE related);

    protected abstract RELATED_PE getCurrentlyRelated(ENTITY_PE entity);

    protected abstract FieldUpdateValue<RELATED_ID> getRelatedUpdate(ENTITY_UPDATE update);

    protected abstract Map<RELATED_ID, RELATED_PE> map(IOperationContext context, List<RELATED_ID> relatedIds);

    protected abstract void check(IOperationContext context, ENTITY_PE entity, RELATED_ID relatedId, RELATED_PE related);

    protected abstract void update(IOperationContext context, ENTITY_PE entity, RELATED_PE related);

}
