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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.IAuthorizationGroupId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.get.AbstractMapObjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.authorizationgroup.ListAuthorizationGroupByPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.IListObjectById;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.AuthorizationGroupPE;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Component
public class MapGroupPEByIdExecutor 
        extends AbstractMapObjectByIdExecutor<IAuthorizationGroupId, AuthorizationGroupPE> 
        implements IMapGroupPEByIdExecutor
{
    @Autowired
    private IDAOFactory daoFactory;
    
    @Autowired
    private IAuthorizationGroupAuthorizationExecutor authorizationExecutor;

    @Override
    protected void checkAccess(IOperationContext context)
    {
        authorizationExecutor.canGet(context);
    }

    @Override
    protected void addListers(IOperationContext context, List<IListObjectById<? extends IAuthorizationGroupId, AuthorizationGroupPE>> listers)
    {
        listers.add(new ListAuthorizationGroupByPermId(daoFactory.getAuthorizationGroupDAO()));
    }

}
