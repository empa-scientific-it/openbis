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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.roleassignment;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.fetchoptions.RoleAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.get.GetRoleAssignmentsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.get.GetRoleAssignmentsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.id.IRoleAssignmentId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.get.GetObjectsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.get.IMapObjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.roleassignment.IRoleAssignmentTranslator;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Component
public class GetRoleAssignmentsOperationExecutor 
        extends GetObjectsOperationExecutor<IRoleAssignmentId, RoleAssignment, RoleAssignmentFetchOptions>
        implements IGetRoleAssignmentsOperationExecutor
{
    @Autowired
    private IMapRoleAssignmentTechIdByIdExecutor mapExecutor;
    
    @Autowired
    private IRoleAssignmentTranslator translator;

    @Override
    protected Class<? extends GetObjectsOperation<IRoleAssignmentId, RoleAssignmentFetchOptions>> getOperationClass()
    {
        return GetRoleAssignmentsOperation.class;
    }
    
    @Override
    protected IMapObjectByIdExecutor<IRoleAssignmentId, Long> getExecutor()
    {
        return mapExecutor;
    }

    @Override
    protected ITranslator<Long, RoleAssignment, RoleAssignmentFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected GetObjectsOperationResult<IRoleAssignmentId, RoleAssignment> getOperationResult(Map<IRoleAssignmentId, RoleAssignment> objectMap)
    {
        return new GetRoleAssignmentsOperationResult(objectMap);
    }

}
