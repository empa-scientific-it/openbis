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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.authorizationgroup;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.AuthorizationGroupPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.AbstractListObjectById;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationGroupDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.AuthorizationGroupPE;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ListAuthorizationGroupByPermId extends AbstractListObjectById<AuthorizationGroupPermId, AuthorizationGroupPE>
{
    private IAuthorizationGroupDAO authorizationGroupDAO;

    public ListAuthorizationGroupByPermId(IAuthorizationGroupDAO authorizationGroupDAO)
    {
        this.authorizationGroupDAO = authorizationGroupDAO;
    }
    
    @Override
    protected Class<AuthorizationGroupPermId> getIdClass()
    {
        return AuthorizationGroupPermId.class;
    }

    @Override
    public AuthorizationGroupPermId createId(AuthorizationGroupPE entity)
    {
        return new AuthorizationGroupPermId(entity.getCode());
    }

    @Override
    public List<AuthorizationGroupPE> listByIds(IOperationContext context, List<AuthorizationGroupPermId> ids)
    {
        Set<String> codes = new LinkedHashSet<>();
        for (AuthorizationGroupPermId permId : ids)
        {
            codes.add(permId.getPermId());
        }
        return authorizationGroupDAO.listByCodes(codes);
    }

}
