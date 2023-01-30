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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IdListUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue.ListUpdateAction;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;

/**
 * @author pkupczyk
 */
@Component
public abstract class AbstractUpdateEntityMultipleRelationsExecutor<ENTITY_UPDATE, ENTITY_PE, RELATED_ID, RELATED_PE> implements
        IUpdateEntityRelationsExecutor<ENTITY_UPDATE, ENTITY_PE>
{

    @Override
    public void update(IOperationContext context, MapBatch<ENTITY_UPDATE, ENTITY_PE> batch)
    {
        Map<RELATED_ID, RELATED_PE> relatedMap = getRelatedMap(context, batch.getObjects().keySet());
        update(context, batch, relatedMap);
    }

    private Map<RELATED_ID, RELATED_PE> getRelatedMap(IOperationContext context, Collection<ENTITY_UPDATE> updates)
    {
        Set<RELATED_ID> relatedIds = new HashSet<RELATED_ID>();

        for (ENTITY_UPDATE update : updates)
        {
            addRelatedIds(relatedIds, update);
        }

        if (false == relatedIds.isEmpty())
        {
            return map(context, relatedIds);
        } else
        {
            return new HashMap<RELATED_ID, RELATED_PE>();
        }
    }

    protected void addRelatedIds(Set<RELATED_ID> relatedIds, FieldUpdateValue<RELATED_ID> update)
    {
        if (update != null && update.isModified() && update.getValue() != null)
        {
            relatedIds.add(update.getValue());
        }
    }

    protected void addRelatedIds(Set<RELATED_ID> relatedIds, IdListUpdateValue<RELATED_ID> update)
    {
        if (update != null && update.hasActions())
        {
            for (ListUpdateAction<RELATED_ID> action : update.getActions())
            {
                relatedIds.addAll(action.getItems());
            }
        }
    }

    protected abstract void addRelatedIds(Set<RELATED_ID> relatedIds, ENTITY_UPDATE update);

    protected abstract Map<RELATED_ID, RELATED_PE> map(IOperationContext context, Collection<RELATED_ID> relatedIds);

    protected abstract void update(IOperationContext context, MapBatch<ENTITY_UPDATE, ENTITY_PE> batch, Map<RELATED_ID, RELATED_PE> relatedMap);

}
