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

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.IObjectAuthorizationExecutor;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public interface IRoleAssignmentAuthorizationExecutor extends IObjectAuthorizationExecutor
{
    public void canGet(IOperationContext context);
    
    public void canCreateInstanceRole(IOperationContext context);
    
    public void canCreateSpaceRole(IOperationContext context, SpacePE space);
    
    public void canCreateProjectRole(IOperationContext context, ProjectPE project);

    public void canSearch(IOperationContext context);

    public void canDeleteInstanceRole(IOperationContext context);
    
    public void canDeleteSpaceRole(IOperationContext context, SpacePE space);
    
    public void canDeleteProjectRole(IOperationContext context, ProjectPE project);

}
