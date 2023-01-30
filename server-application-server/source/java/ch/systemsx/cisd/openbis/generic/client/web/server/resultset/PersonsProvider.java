/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PersonGridColumnIDs.EMAIL;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PersonGridColumnIDs.FIRST_NAME;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PersonGridColumnIDs.IS_ACTIVE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PersonGridColumnIDs.LAST_NAME;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PersonGridColumnIDs.REGISTRATION_DATE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PersonGridColumnIDs.REGISTRATOR;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.PersonGridColumnIDs.USER_ID;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;

/**
 * Provider of {@link Person} instances.
 * 
 * @author Franz-Josef Elmer
 */
public class PersonsProvider extends AbstractCommonTableModelProvider<Person>
{

    private final TechId authorizationGroupIdOrNull;

    public PersonsProvider(ICommonServer commonServer, String sessionToken,
            TechId authorizationGroupIdOrNull)
    {
        super(commonServer, sessionToken);
        this.authorizationGroupIdOrNull = authorizationGroupIdOrNull;
    }

    @Override
    protected TypedTableModel<Person> createTableModel()
    {
        List<Person> persons;
        if (authorizationGroupIdOrNull == null)
        {
            persons = commonServer.listPersons(sessionToken);
        } else
        {
            persons =
                    commonServer.listPersonInAuthorizationGroup(sessionToken,
                            authorizationGroupIdOrNull);
        }

        TypedTableModelBuilder<Person> builder = new TypedTableModelBuilder<Person>();
        builder.addColumn(USER_ID);
        builder.addColumn(FIRST_NAME);
        builder.addColumn(LAST_NAME);
        builder.addColumn(EMAIL).withDefaultWidth(200);
        builder.addColumn(REGISTRATOR);
        builder.addColumn(REGISTRATION_DATE).withDefaultWidth(300);
        builder.addColumn(IS_ACTIVE).withDefaultWidth(80);
        for (Person person : persons)
        {
            builder.addRow(person);
            builder.column(USER_ID).addString(person.getUserId());
            builder.column(FIRST_NAME).addString(person.getFirstName());
            builder.column(LAST_NAME).addString(person.getLastName());
            builder.column(EMAIL).addString(person.getEmail());
            builder.column(REGISTRATOR).addPerson(person.getRegistrator());
            builder.column(REGISTRATION_DATE).addDate(person.getRegistrationDate());
            builder.column(IS_ACTIVE).addString(Boolean.toString(person.isActive()));
        }
        return builder.getModel();
    }

}
