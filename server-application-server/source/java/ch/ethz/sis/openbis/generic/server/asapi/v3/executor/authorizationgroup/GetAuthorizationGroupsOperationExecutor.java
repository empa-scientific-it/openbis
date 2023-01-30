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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.authorizationgroup;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.AuthorizationGroup;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.fetchoptions.AuthorizationGroupFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.get.GetAuthorizationGroupsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.get.GetAuthorizationGroupsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.IAuthorizationGroupId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetObjectsOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.get.GetObjectsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.get.IMapObjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Component
public class GetAuthorizationGroupsOperationExecutor 
        extends GetObjectsOperationExecutor<IAuthorizationGroupId, AuthorizationGroup, AuthorizationGroupFetchOptions> 
        implements IGetAuthorizationGroupsOperationExecutor
{
    @Autowired
    private IMapGroupTechIdByIdExecutor mapExecutor;
    
    @Autowired
    private IAuthorizationGroupTranslator translator;
    
    @Override
    protected Class<? extends GetObjectsOperation<IAuthorizationGroupId, AuthorizationGroupFetchOptions>> getOperationClass()
    {
        return GetAuthorizationGroupsOperation.class;
    }

    @Override
    protected IMapObjectByIdExecutor<IAuthorizationGroupId, Long> getExecutor()
    {
        return mapExecutor;
    }

    @Override
    protected ITranslator<Long, AuthorizationGroup, AuthorizationGroupFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected GetObjectsOperationResult<IAuthorizationGroupId, AuthorizationGroup> getOperationResult(
            Map<IAuthorizationGroupId, AuthorizationGroup> objectMap)
    {
        return new GetAuthorizationGroupsOperationResult(objectMap);
    }

}
