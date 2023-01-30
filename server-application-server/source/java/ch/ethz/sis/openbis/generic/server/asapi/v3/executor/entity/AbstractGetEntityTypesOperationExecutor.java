/*
 * Copyright 2018 ETH Zuerich, CISD
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
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.get.GetObjectsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.get.IMapObjectByIdExecutor;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author pkupczyk
 */
public abstract class AbstractGetEntityTypesOperationExecutor<OBJECT, FETCH_OPTIONS extends FetchOptions<?>>
        extends GetObjectsOperationExecutor<IEntityTypeId, OBJECT, FETCH_OPTIONS>
{

    @Autowired
    private IMapEntityTypeByIdExecutor mapExecutor;

    protected abstract EntityKind getEntityKind();

    @Override
    protected IMapObjectByIdExecutor<IEntityTypeId, Long> getExecutor()
    {
        return new IMapObjectByIdExecutor<IEntityTypeId, Long>()
            {

                @Override
                public Map<IEntityTypeId, Long> map(IOperationContext context, Collection<? extends IEntityTypeId> ids)
                {
                    return map(context, ids, true);
                }

                @Override
                public Map<IEntityTypeId, Long> map(IOperationContext context, Collection<? extends IEntityTypeId> ids, boolean checkAccess)
                {
                    Map<IEntityTypeId, EntityTypePE> map = mapExecutor.map(context, getEntityKind(), ids);
                    Map<IEntityTypeId, Long> idMap = new LinkedHashMap<IEntityTypeId, Long>();

                    for (Map.Entry<IEntityTypeId, EntityTypePE> entry : map.entrySet())
                    {
                        idMap.put(entry.getKey(), entry.getValue().getId());
                    }

                    return idMap;
                }
            };
    }

}
