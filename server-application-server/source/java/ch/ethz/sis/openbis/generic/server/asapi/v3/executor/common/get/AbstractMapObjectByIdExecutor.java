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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.get;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.CreationId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.CreationIdCache;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.IListObjectById;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.MapObjectById;

/**
 * @author pkupczyk
 */
public abstract class AbstractMapObjectByIdExecutor<ID extends IObjectId, OBJECT> implements IMapObjectByIdExecutor<ID, OBJECT>
{

    protected abstract void checkAccess(IOperationContext context);

    @Override
    public Map<ID, OBJECT> map(IOperationContext context, Collection<? extends ID> ids)
    {
        return map(context, ids, true);
    }

    @Override
    public Map<ID, OBJECT> map(IOperationContext context, Collection<? extends ID> ids, boolean checkAccess)
    {
        if (checkAccess)
        {
            checkAccess(context);
        }

        if (ids == null)
        {
            throw new IllegalArgumentException("Ids were null");
        }
        if (ids.isEmpty())
        {
            return Collections.emptyMap();
        }

        if (containsCreationIds(ids))
        {
            Map<ID, OBJECT> map1 = doMapByCreationIds(context, ids);
            Map<ID, OBJECT> map2 = doMapByNonCreationIds(context, ids);
            map1.putAll(map2);
            return map1;
        } else
        {
            return doMapByAllIds(context, ids);
        }
    }

    private boolean containsCreationIds(Collection<? extends ID> ids)
    {
        for (ID id : ids)
        {
            if (id instanceof CreationId)
            {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private Map<ID, OBJECT> doMapByCreationIds(IOperationContext context, Collection<? extends ID> ids)
    {
        CreationIdCache creationIdCache = CreationIdCache.getInstance(context);
        Collection<ID> realIds = new LinkedList<ID>();

        for (ID id : ids)
        {
            if (id instanceof CreationId)
            {
                ID realId = (ID) creationIdCache.getRealId((CreationId) id);
                realIds.add(realId);
            }
        }

        Map<ID, OBJECT> realIdToObjectMap = doMapByAllIds(context, realIds);
        Map<ID, OBJECT> creationIdToObjectMap = new HashMap<ID, OBJECT>();

        for (Map.Entry<ID, OBJECT> entry : realIdToObjectMap.entrySet())
        {
            CreationId creationId = creationIdCache.getCreationId(entry.getKey());
            creationIdToObjectMap.put((ID) creationId, entry.getValue());
        }

        return creationIdToObjectMap;
    }

    private Map<ID, OBJECT> doMapByNonCreationIds(IOperationContext context, Collection<? extends ID> ids)
    {
        Collection<ID> realIds = new LinkedList<ID>();

        for (ID id : ids)
        {
            if (false == (id instanceof CreationId))
            {
                realIds.add(id);
            }
        }

        return doMapByAllIds(context, realIds);
    }

    private Map<ID, OBJECT> doMapByAllIds(IOperationContext context, Collection<? extends ID> ids)
    {
        List<IListObjectById<? extends ID, OBJECT>> listers = new ArrayList<IListObjectById<? extends ID, OBJECT>>();
        addListers(context, listers);
        return new MapObjectById<ID, OBJECT>().map(context, listers, ids);
    }

    protected abstract void addListers(IOperationContext context, List<IListObjectById<? extends ID, OBJECT>> listers);

}
