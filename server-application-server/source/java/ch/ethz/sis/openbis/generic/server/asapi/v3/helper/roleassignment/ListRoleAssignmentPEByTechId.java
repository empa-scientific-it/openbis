/*
 * Copyright 2017 ETH Zuerich, SIS
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.id.RoleAssignmentTechId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.AbstractListObjectById;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IRoleAssignmentDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ListRoleAssignmentPEByTechId extends AbstractListObjectById<RoleAssignmentTechId, RoleAssignmentPE>
{
    private IRoleAssignmentDAO roleAssignmentDAO;

    public ListRoleAssignmentPEByTechId(IRoleAssignmentDAO roleAssignmentDAO)
    {
        this.roleAssignmentDAO = roleAssignmentDAO;
        
    }
    
    @Override
    protected Class<RoleAssignmentTechId> getIdClass()
    {
        return RoleAssignmentTechId.class;
    }

    @Override
    public RoleAssignmentTechId createId(RoleAssignmentPE entity)
    {
        return new RoleAssignmentTechId(entity.getId());
    }

    @Override
    public List<RoleAssignmentPE> listByIds(IOperationContext context, List<RoleAssignmentTechId> ids)
    {
        Set<Long> plainIds = new HashSet<>();
        for (RoleAssignmentTechId id : ids)
        {
            plainIds.add(id.getTechId());
        }
        List<RoleAssignmentPE> result = new ArrayList<>();
        for (RoleAssignmentPE entity : roleAssignmentDAO.listAllEntities())
        {
            if (plainIds.contains(entity.getId()))
            {
                result.add(entity);
            }
        }
        return result;
    }
}
