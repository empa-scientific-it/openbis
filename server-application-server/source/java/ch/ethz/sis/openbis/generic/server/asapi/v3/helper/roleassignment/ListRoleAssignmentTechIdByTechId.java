/*
 * Copyright ETH 2017 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.roleassignment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.id.RoleAssignmentTechId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.AbstractListTechIdById;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.lemnik.eodsql.QueryTool;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ListRoleAssignmentTechIdByTechId extends AbstractListTechIdById<RoleAssignmentTechId>
{
    
    @Override
    protected Class<RoleAssignmentTechId> getIdClass()
    {
        return RoleAssignmentTechId.class;
    }

    @Override
    protected Map<Long, RoleAssignmentTechId> createIdsByTechIdsMap(IOperationContext context, List<RoleAssignmentTechId> ids)
    {
        LongSet longs = new LongOpenHashSet();
        for (RoleAssignmentTechId techId : ids)
        {
            longs.add(techId.getTechId());
        }
        RoleAssignmentQuery query = QueryTool.getManagedQuery(RoleAssignmentQuery.class);
        List<Long> idsOfExistingRoleAssigments = query.listRoleAssignmentTechIdsByTechIds(longs);
        Map<Long, RoleAssignmentTechId> map = new HashMap<Long, RoleAssignmentTechId>();
        for (Long id : idsOfExistingRoleAssigments)
        {
            map.put(id, new RoleAssignmentTechId(id));
        }
        return map;
    }

}
