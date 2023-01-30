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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.update.AuthorizationGroupUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.IPersonId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractUpdateEntityMultipleRelationsExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.person.IMapPersonByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.systemsx.cisd.openbis.generic.shared.dto.AuthorizationGroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Component
public class UpdateAuthorizationGroupUsersExecutor 
        extends AbstractUpdateEntityMultipleRelationsExecutor<AuthorizationGroupUpdate, AuthorizationGroupPE, 
                    IPersonId, PersonPE>
        implements IUpdateAuthorizationGroupUsersExecutor
{
    @Autowired
    private IMapPersonByIdExecutor mapPersonByIdExecutor;
    
    @Autowired
    private IUpdateAuthorizationGroupUsersWithCacheExecutor updateAuthorizationGroupUsersWithCacheExecutor;

    @Override
    protected void addRelatedIds(Set<IPersonId> relatedIds, AuthorizationGroupUpdate update)
    {
        addRelatedIds(relatedIds, update.getUserIds());
    }

    @Override
    protected Map<IPersonId, PersonPE> map(IOperationContext context, Collection<IPersonId> relatedIds)
    {
        return mapPersonByIdExecutor.map(context, relatedIds);
    }

    @Override
    protected void update(IOperationContext context, MapBatch<AuthorizationGroupUpdate, AuthorizationGroupPE> batch,
            Map<IPersonId, PersonPE> relatedMap)
    {
        updateAuthorizationGroupUsersWithCacheExecutor.update(context, batch, relatedMap);
    }

}
