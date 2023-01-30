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

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.update.AuthorizationGroupUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IdListUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.IPersonId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractUpdateEntityToManyRelationExecutor;
import ch.systemsx.cisd.openbis.generic.shared.dto.AuthorizationGroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Component
public class UpdateAuthorizationGroupUsersWithCacheExecutor 
        extends AbstractUpdateEntityToManyRelationExecutor<AuthorizationGroupUpdate, AuthorizationGroupPE, IPersonId, PersonPE> 
        implements IUpdateAuthorizationGroupUsersWithCacheExecutor
{
    @Override
    protected String getRelationName()
    {
        return "authorization-group-users";
    }

    @Override
    protected Collection<PersonPE> getCurrentlyRelated(AuthorizationGroupPE entity)
    {
        return entity.getPersons();
    }

    @Override
    protected IdListUpdateValue<? extends IPersonId> getRelatedUpdate(IOperationContext context, AuthorizationGroupUpdate update)
    {
        return update.getUserIds();
    }

    @Override
    protected void check(IOperationContext context, AuthorizationGroupPE entity, IPersonId relatedId, PersonPE related)
    {
    }

    @Override
    protected void add(IOperationContext context, AuthorizationGroupPE entity, PersonPE related)
    {
        entity.addPerson(related);
    }

    @Override
    protected void remove(IOperationContext context, AuthorizationGroupPE entity, PersonPE related)
    {
        entity.removePerson(related);
    }
    
}
