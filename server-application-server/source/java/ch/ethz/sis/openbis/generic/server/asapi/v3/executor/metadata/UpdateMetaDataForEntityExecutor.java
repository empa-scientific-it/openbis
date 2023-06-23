/*
 *  Copyright ETH 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.metadata;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IMetaDataUpdateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.UpdateRelationProgress;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityWithMetaData;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class UpdateMetaDataForEntityExecutor<ENTITY_UPDATE extends IMetaDataUpdateHolder, ENTITY_PE extends IEntityWithMetaData>
        implements IUpdateMetaDataForEntityExecutor<ENTITY_UPDATE, ENTITY_PE>
{
    @Override
    public void update(final IOperationContext context,
            final MapBatch<ENTITY_UPDATE, ENTITY_PE> batch)
    {
        new MapBatchProcessor<ENTITY_UPDATE, ENTITY_PE>(context, batch)
        {
            @Override
            public void process(ENTITY_UPDATE update, ENTITY_PE entity)
            {
                updateSpecific(update, entity);
            }

            @Override
            public IProgress createProgress(ENTITY_UPDATE update, ENTITY_PE entity, int objectIndex,
                    int totalObjectCount)
            {
                return new UpdateRelationProgress(update, entity, "metadata", objectIndex,
                        totalObjectCount);
            }

        };

    }

    @Override
    public void updateSpecific(ENTITY_UPDATE update, ENTITY_PE entity)
    {
        Map<String, String> metaData = new HashMap<>();
        if (entity.getMetaData() != null)
        {
            metaData.putAll(entity.getMetaData());
        }
        AtomicBoolean metaDataChanged = new AtomicBoolean(false);
        for (ListUpdateValue.ListUpdateAction<Object> action : update.getMetaData().getActions())
        {
            if (action instanceof ListUpdateValue.ListUpdateActionAdd<?>)
            {
                addTo(metaData, action, metaDataChanged);
            } else if (action instanceof ListUpdateValue.ListUpdateActionRemove<?>)
            {
                for (String key : (Collection<String>) action.getItems())
                {
                    metaDataChanged.set(true);
                    metaData.remove(key);
                }
            } else if (action instanceof ListUpdateValue.ListUpdateActionSet<?>)
            {
                metaDataChanged.set(true);
                Collection<Map<String, String>> items = (Collection<Map<String, String>>) action.getItems();
                for (Map<String, String> item : items)
                {
                    metaData = item;
                }
            }
        }
        if (metaDataChanged.get())
        {
            entity.setMetaData(metaData.isEmpty() ? null : metaData);
        }
    }

    @SuppressWarnings("unchecked")
    private void addTo(Map<String, String> metaData,
            ListUpdateValue.ListUpdateAction<?> lastSetAction, AtomicBoolean metaDataChanged)
    {
        Collection<Map<String, String>> maps =
                (Collection<Map<String, String>>) lastSetAction.getItems();
        for (Map<String, String> map : maps)
        {
            if (map.isEmpty() == false)
            {
                metaDataChanged.set(true);
                metaData.putAll(map);
            }
        }
    }
}
