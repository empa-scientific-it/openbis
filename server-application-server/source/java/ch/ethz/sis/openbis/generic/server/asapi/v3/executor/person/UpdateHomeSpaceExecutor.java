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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.person;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.update.PersonUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractUpdateEntityToOneRelationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.space.IMapSpaceByIdExecutor;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class UpdateHomeSpaceExecutor
        extends AbstractUpdateEntityToOneRelationExecutor<PersonUpdate, PersonPE, ISpaceId, SpacePE>
        implements IUpdateHomeSpaceExecutor
{
    @Autowired
    private IMapSpaceByIdExecutor mapSpaceByIdExecutor;

    @Autowired
    private IPersonAuthorizationExecutor authorizationExecutor;
    
    @Override
    protected String getRelationName()
    {
        return "person-space";
    }

    @Override
    protected ISpaceId getRelatedId(SpacePE related)
    {
        return new SpacePermId(related.getCode());
    }

    @Override
    protected SpacePE getCurrentlyRelated(PersonPE entity)
    {
        return entity.getHomeSpace();
    }

    @Override
    protected FieldUpdateValue<ISpaceId> getRelatedUpdate(PersonUpdate update)
    {
        return update.getSpaceId();
    }

    @Override
    protected Map<ISpaceId, SpacePE> map(IOperationContext context, List<ISpaceId> relatedIds)
    {
        return mapSpaceByIdExecutor.map(context, relatedIds);
    }

    @Override
    protected void check(IOperationContext context, PersonPE user, ISpaceId relatedId, SpacePE newHomeSpace)
    {
        PersonPE sessionUser = context.getSession().tryGetPerson();
        if (sessionUser.equals(user))
        {
            // The user can always change its own home space. 
        } else
        {
            authorizationExecutor.canUpdateHomeSpace(context, newHomeSpace);
        }
    }

    @Override
    protected void update(IOperationContext context, PersonPE entity, SpacePE related)
    {
        entity.setHomeSpace(related);
    }

}
