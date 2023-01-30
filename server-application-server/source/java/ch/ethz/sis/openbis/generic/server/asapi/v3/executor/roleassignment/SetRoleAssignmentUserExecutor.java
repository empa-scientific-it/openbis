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

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.IPersonId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.create.RoleAssignmentCreation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractSetEntityToOneRelationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.person.IMapPersonByIdExecutor;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Component
public class SetRoleAssignmentUserExecutor
        extends AbstractSetEntityToOneRelationExecutor<RoleAssignmentCreation, RoleAssignmentPE, IPersonId, PersonPE>
        implements ISetRoleAssignmentUserExecutor
{
    @Autowired
    private IMapPersonByIdExecutor mapPersonByIdExecutor;

    @Override
    protected String getRelationName()
    {
        return "roleassignment-user";
    }

    @Override
    protected IPersonId getRelatedId(RoleAssignmentCreation creation)
    {
        return creation.getUserId();
    }

    @Override
    protected Map<IPersonId, PersonPE> map(IOperationContext context, List<IPersonId> relatedIds)
    {
        return mapPersonByIdExecutor.map(context, relatedIds);
    }

    @Override
    protected void check(IOperationContext context, RoleAssignmentPE entity, IPersonId relatedId, PersonPE related)
    {
    }

    @Override
    protected void set(IOperationContext context, RoleAssignmentPE entity, PersonPE related)
    {
        entity.setPersonInternal(related);
    }

}
