/*
 * Copyright 2013 ETH Zuerich, CISD
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

import java.util.LinkedList;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.AbstractListObjectById;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author pkupczyk
 */
public class ListPersonByPermId extends AbstractListObjectById<PersonPermId, PersonPE>
{

    private IPersonDAO personDAO;

    public ListPersonByPermId(IPersonDAO personDAO)
    {
        this.personDAO = personDAO;
    }

    @Override
    public Class<PersonPermId> getIdClass()
    {
        return PersonPermId.class;
    }

    @Override
    public PersonPermId createId(PersonPE person)
    {
        return new PersonPermId(person.getUserId());
    }

    @Override
    public List<PersonPE> listByIds(IOperationContext context, List<PersonPermId> ids)
    {
        List<String> userIds = new LinkedList<String>();

        for (PersonPermId id : ids)
        {
            userIds.add(id.getPermId());
        }

        return personDAO.listByCodes(userIds);
    }

}
