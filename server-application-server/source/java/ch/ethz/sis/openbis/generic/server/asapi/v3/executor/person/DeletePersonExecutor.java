/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.delete.PersonDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.IPersonId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractDeleteEntityExecutor;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author pkupczyk
 */
@Component
public class DeletePersonExecutor extends AbstractDeleteEntityExecutor<Void, IPersonId, PersonPE, PersonDeletionOptions>
        implements IDeletePersonExecutor
{

    @Autowired
    private IMapPersonByIdExecutor mapPersonByIdExecutor;

    @Autowired
    private IPersonAuthorizationExecutor authorizationExecutor;

    @Override
    protected Map<IPersonId, PersonPE> map(IOperationContext context, List<? extends IPersonId> entityIds, PersonDeletionOptions deletionOptions)
    {
        return mapPersonByIdExecutor.map(context, entityIds);
    }

    @Override
    protected void checkAccess(IOperationContext context, IPersonId entityId, PersonPE entity)
    {
        authorizationExecutor.canDelete(context);
    }

    @Override
    protected void updateModificationDateAndModifier(IOperationContext context, PersonPE entity)
    {
        // nothing to do
    }

    @Override
    protected Void delete(IOperationContext context, Collection<PersonPE> persons, PersonDeletionOptions deletionOptions)
    {
        for (PersonPE person : persons)
        {
            if (person.equals(context.getSession().tryGetPerson()) || person.equals(context.getSession().tryGetCreatorPerson()))
            {
                throw new UserFailureException("You cannot remove your own user.");
            }

            daoFactory.getPersonDAO().delete(person);
        }

        return null;
    }

}
