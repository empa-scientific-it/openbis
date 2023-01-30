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

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.PersonBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class PersonsProviderTest extends AbstractProviderTest
{
    @Test
    public void testWithNoAuthorizationGroup()
    {
        final PersonBuilder p1 = new PersonBuilder();
        p1.name("Isaac", "Newton").userID("in").email("in@o.uk").registrationDate(new Date(4711L));
        final PersonBuilder p2 = new PersonBuilder();
        p2.name("Albert", "Einstein").userID("ae").email("ae@c.de").registrator(p1.getPerson());
        context.checking(new Expectations()
            {
                {
                    one(server).listPersons(SESSION_TOKEN);
                    will(returnValue(Arrays.asList(p1.getPerson(), p2.getPerson())));
                }
            });

        PersonsProvider personsProvider = new PersonsProvider(server, SESSION_TOKEN, null);
        TypedTableModel<Person> tableModel = personsProvider.getTableModel(10);

        assertEquals(
                "[USER_ID, FIRST_NAME, LAST_NAME, EMAIL, REGISTRATOR, REGISTRATION_DATE, IS_ACTIVE]",
                getHeaderIDs(tableModel).toString());
        assertEquals("[VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, TIMESTAMP, VARCHAR]",
                getHeaderDataTypes(tableModel).toString());
        List<TableModelRowWithObject<Person>> rows = tableModel.getRows();
        assertSame(p1.getPerson(), rows.get(0).getObjectOrNull());
        assertEquals("[in, Isaac, Newton, in@o.uk, , Thu Jan 01 01:00:04 CET 1970, true]", rows
                .get(0).getValues().toString());
        assertSame(p2.getPerson(), rows.get(1).getObjectOrNull());
        assertEquals("[ae, Albert, Einstein, ae@c.de, Newton, Isaac, , true]", rows.get(1)
                .getValues().toString());
        assertEquals(2, rows.size());
        context.assertIsSatisfied();
    }

    @Test
    public void testWithAuthorizationGroup()
    {
        final PersonBuilder p = new PersonBuilder();
        p.name("Isaac", "Newton").userID("in").email("in@o.uk").registrationDate(new Date(4711L));
        final TechId groupId = new TechId(42);
        context.checking(new Expectations()
            {
                {
                    one(server).listPersonInAuthorizationGroup(SESSION_TOKEN, groupId);
                    will(returnValue(Arrays.asList(p.getPerson())));
                }
            });

        PersonsProvider personsProvider = new PersonsProvider(server, SESSION_TOKEN, groupId);
        TypedTableModel<Person> tableModel = personsProvider.getTableModel(10);

        assertEquals(
                "[USER_ID, FIRST_NAME, LAST_NAME, EMAIL, REGISTRATOR, REGISTRATION_DATE, IS_ACTIVE]",
                getHeaderIDs(tableModel).toString());
        assertEquals("[VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, TIMESTAMP, VARCHAR]",
                getHeaderDataTypes(tableModel).toString());
        List<TableModelRowWithObject<Person>> rows = tableModel.getRows();
        assertSame(p.getPerson(), rows.get(0).getObjectOrNull());
        assertEquals("[in, Isaac, Newton, in@o.uk, , Thu Jan 01 01:00:04 CET 1970, true]", rows
                .get(0).getValues().toString());
        assertEquals(1, rows.size());
        context.assertIsSatisfied();
    }
}
