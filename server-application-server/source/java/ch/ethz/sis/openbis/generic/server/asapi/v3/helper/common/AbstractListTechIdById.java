/*
 * Copyright 2015 ETH Zuerich, SIS
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;

/**
 * @author Franz-Josef Elmer
 */
public abstract class AbstractListTechIdById<ID> extends AbstractListObjectById<ID, Long>
{
    private Map<Long, ID> idsByTechIds = new HashMap<Long, ID>();

    @Override
    public ID createId(Long techId)
    {
        return idsByTechIds.get(techId);
    }

    @Override
    public List<Long> listByIds(IOperationContext context, List<ID> ids)
    {
        idsByTechIds = createIdsByTechIdsMap(context, ids);
        return new ArrayList<>(idsByTechIds.keySet());
    }

    protected abstract Map<Long, ID> createIdsByTechIdsMap(IOperationContext context, List<ID> ids);

}
