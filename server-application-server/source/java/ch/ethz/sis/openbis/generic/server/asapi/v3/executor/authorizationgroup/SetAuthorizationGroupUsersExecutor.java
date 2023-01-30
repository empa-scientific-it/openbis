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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.create.AuthorizationGroupCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.IPersonId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractSetEntityToManyRelationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.person.IMapPersonByIdExecutor;
import ch.systemsx.cisd.openbis.generic.shared.dto.AuthorizationGroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Component
public class SetAuthorizationGroupUsersExecutor 
        extends AbstractSetEntityToManyRelationExecutor<AuthorizationGroupCreation, AuthorizationGroupPE, IPersonId, PersonPE> 
        implements ISetAuthorizationGroupUsersExecutor
{
    @Autowired
    private IMapPersonByIdExecutor mapPersonByIdExecutor;

    @Override
    protected IPersonId getCreationId(IOperationContext context, AuthorizationGroupCreation creation)
    {
        return null;
    }

    @Override
    protected String getRelationName()
    {
        return "authorization-group-users";
    }

    @Override
    protected Collection<? extends IPersonId> getRelatedIds(IOperationContext context, AuthorizationGroupCreation creation)
    {
        return creation.getUserIds();
    }

    @Override
    protected Map<IPersonId, PersonPE> map(IOperationContext context, Collection<? extends IPersonId> relatedIds)
    {
        return mapPersonByIdExecutor.map(context, relatedIds);
    }

    @Override
    protected void check(IOperationContext context, IPersonId relatedId, PersonPE related)
    {
    }

    @Override
    protected void setRelated(IOperationContext context, AuthorizationGroupPE entity, Collection<PersonPE> related)
    {
        for (PersonPE person : related)
        {
            entity.addPerson(person);
        }
    }
}
