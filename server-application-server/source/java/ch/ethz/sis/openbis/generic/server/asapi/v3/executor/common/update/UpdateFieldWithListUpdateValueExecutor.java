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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.update;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IdListUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue.ListUpdateAction;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue.ListUpdateActionAdd;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue.ListUpdateActionRemove;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue.ListUpdateActionSet;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;

/**
 * @author pkupczyk
 */
public abstract class UpdateFieldWithListUpdateValueExecutor<K, V>
{

    protected void update(IOperationContext context, Map<K, IdListUpdateValue<V>> listUpdateMap)
    {
        for (Entry<K, IdListUpdateValue<V>> listUpdateEntry : listUpdateMap.entrySet())
        {
            K key = listUpdateEntry.getKey();
            IdListUpdateValue<V> listUpdate = listUpdateEntry.getValue();

            if (listUpdate != null && listUpdate.hasActions())
            {
                for (ListUpdateAction<V> action : listUpdate.getActions())
                {
                    if (action instanceof ListUpdateActionSet)
                    {
                        setValues(context, key, action.getItems());
                    } else if (action instanceof ListUpdateActionAdd)
                    {
                        addValues(context, key, action.getItems());
                    } else if (action instanceof ListUpdateActionRemove)
                    {
                        removeValues(context, key, action.getItems());
                    }
                }
            }
        }
    }

    protected abstract void setValues(IOperationContext context, K key, Collection<? extends V> values);

    protected abstract void addValues(IOperationContext context, K key, Collection<? extends V> values);

    protected abstract void removeValues(IOperationContext context, K key, Collection<? extends V> values);

}
