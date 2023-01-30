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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.person;

import java.util.Collections;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.Me;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.AbstractListObjectById;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ListPersonByMe extends AbstractListObjectById<Me, PersonPE>
{

    private IPersonDAO personDAO;

    public ListPersonByMe(IPersonDAO personDAO)
    {
        this.personDAO = personDAO;
    }

    @Override
    public Me createId(PersonPE entity)
    {
        return new Me();
    }

    @Override
    public List<PersonPE> listByIds(IOperationContext context, List<Me> ids)
    {
        PersonPE person = context.getSession().tryGetPerson();
        if (person == null)
        {
            throw new UserFailureException("Can not resolve 'Me' because there is no session user.");
        }
        // The session user has to be loaded from the database because it isn't in the Hibernate session.
        // This is important for example when adding the session user to an authorization group.
        PersonPE reloadedPerson = personDAO.getByTechId(TechId.create(person));
        return Collections.nCopies(ids.size(), reloadedPerson);
    }

    @Override
    protected Class<Me> getIdClass()
    {
        return Me.class;
    }

}
