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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.get;

import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.AuthorizationGroup;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.IAuthorizationGroupId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetObjectsOperationResult;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@JsonObject("as.dto.authorizationgroup.get.GetAuthorizationGroupsOperationResult")
public class GetAuthorizationGroupsOperationResult extends GetObjectsOperationResult<IAuthorizationGroupId, AuthorizationGroup>
{

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private GetAuthorizationGroupsOperationResult()
    {
    }

    public GetAuthorizationGroupsOperationResult(Map<IAuthorizationGroupId, AuthorizationGroup> objectMap)
    {
        super(objectMap);
    }
}
