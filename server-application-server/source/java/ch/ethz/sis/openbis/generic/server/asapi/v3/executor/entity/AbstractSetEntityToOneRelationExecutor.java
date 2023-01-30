/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.ICreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.SetRelationProgress;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;

/**
 * @author pkupczyk
 */
@Component
public abstract class AbstractSetEntityToOneRelationExecutor<ENTITY_CREATION extends ICreation, ENTITY_PE extends IIdHolder, RELATED_ID, RELATED_PE>
        implements ISetEntityRelationsExecutor<ENTITY_CREATION, ENTITY_PE>
{

    @Autowired
    protected IRelationshipService relationshipService;

    @Override
    public void set(final IOperationContext context, MapBatch<ENTITY_CREATION, ENTITY_PE> batch)
    {
        List<RELATED_ID> relatedIds = new LinkedList<RELATED_ID>();

        for (ENTITY_CREATION creation : batch.getObjects().keySet())
        {
            RELATED_ID relatedId = getRelatedId(creation);

            if (relatedId != null)
            {
                relatedIds.add(relatedId);
            }
        }

        final Map<RELATED_ID, RELATED_PE> relatedMap = map(context, relatedIds);

        new MapBatchProcessor<ENTITY_CREATION, ENTITY_PE>(context, batch)
            {
                @Override
                public void process(ENTITY_CREATION creation, ENTITY_PE entity)
                {
                    RELATED_ID relatedId = getRelatedId(creation);

                    if (relatedId == null)
                    {
                        check(context, entity, null, null);
                        set(context, entity, null);
                    } else
                    {
                        RELATED_PE related = relatedMap.get(relatedId);

                        if (related == null)
                        {
                            throw new ObjectNotFoundException((IObjectId) relatedId);
                        }

                        check(context, entity, relatedId, related);
                        set(context, entity, related);
                    }
                }

                @Override
                public IProgress createProgress(ENTITY_CREATION creation, ENTITY_PE entity, int objectIndex, int totalObjectCount)
                {
                    return new SetRelationProgress(entity, creation, getRelationName(), objectIndex, totalObjectCount);
                }

            };
    }

    protected abstract String getRelationName();

    protected abstract RELATED_ID getRelatedId(ENTITY_CREATION creation);

    protected abstract Map<RELATED_ID, RELATED_PE> map(IOperationContext context, List<RELATED_ID> relatedIds);

    protected abstract void check(IOperationContext context, ENTITY_PE entity, RELATED_ID relatedId, RELATED_PE related);

    protected abstract void set(IOperationContext context, ENTITY_PE entity, RELATED_PE related);

}
