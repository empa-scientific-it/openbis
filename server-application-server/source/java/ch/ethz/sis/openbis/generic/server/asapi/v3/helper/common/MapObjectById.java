/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnsupportedObjectIdException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;

/**
 * @author pkupczyk
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class MapObjectById<ID, OBJECT>
{

    private Map<Class, List> groupIdsByClass(Collection ids)
    {
        Map<Class, List> idClassToIdListMap =
                new HashMap<Class, List>();

        for (Object id : ids)
        {
            if (id == null)
            {
                continue;
            }

            List idList = idClassToIdListMap.get(id.getClass());

            if (idList == null)
            {
                idList = new LinkedList<ID>();
                idClassToIdListMap.put(id.getClass(), idList);
            }

            idList.add(id);
        }
        return idClassToIdListMap;
    }

    private Map mapByIds(IOperationContext context, List<IListObjectById<? extends ID, OBJECT>> listers,
            Map<Class, List> idClassToIdListMap)
    {
        final Map idToObject = new HashMap();

        for (Class idClass : idClassToIdListMap.keySet())
        {
            List idList = idClassToIdListMap.get(idClass);
            IListObjectById listerForIdClass = findLister(listers, idClass, idList);
            List objects = listerForIdClass.listByIds(context, idList);
            if (objects != null)
            {
                for (Object object : objects)
                {
                    Object createId = listerForIdClass.createId(object);
                    idToObject.put(createId, object);
                }
            }
        }

        return idToObject;
    }

    private IListObjectById findLister(List<IListObjectById<? extends ID, OBJECT>> listers, Class idClass, List idList)
    {
        for (IListObjectById lister : listers)
        {
            if (lister.acceptsIdClass(idClass))
            {
                return lister;
            }
        }
        throw new UnsupportedObjectIdException((IObjectId) idList.iterator().next());
    }

    public Map<ID, OBJECT> map(IOperationContext context, List<IListObjectById<? extends ID, OBJECT>> listers,
            Collection<? extends ID> ids)
    {
        Map<Class, List> idClassToIdListMap = groupIdsByClass(ids);
        Map idToObjectMap = mapByIds(context, listers, idClassToIdListMap);
        Map orderedMap = new LinkedHashMap();

        for (ID id : ids)
        {
            Object object = idToObjectMap.get(id);
            if (object != null)
            {
                orderedMap.put(id, object);
            }
        }

        return orderedMap;
    }

}
