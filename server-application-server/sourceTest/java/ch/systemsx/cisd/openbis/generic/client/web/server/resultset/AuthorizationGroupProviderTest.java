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

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.PersonBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class AuthorizationGroupProviderTest extends AbstractProviderTest
{
    @Test
    public void test()
    {
        final AuthorizationGroup group = new AuthorizationGroup();
        group.setCode("my-group");
        group.setDescription("my lovely group");
        group.setRegistrationDate(new Date(4711));
        group.setRegistrator(new PersonBuilder().name("Issac", "Newton").getPerson());
        group.setModificationDate(new Date(5711));
        context.checking(new Expectations()
            {
                {
                    one(server).listAuthorizationGroups(SESSION_TOKEN);
                    will(returnValue(Arrays.asList(group)));
                }
            });
        AuthorizationGroupProvider provider = new AuthorizationGroupProvider(server, SESSION_TOKEN);
        TypedTableModel<AuthorizationGroup> tableModel = provider.getTableModel(10);

        assertEquals("[CODE, DESCRIPTION, REGISTRATOR, REGISTRATION_DATE, MODIFICATION_DATE]",
                getHeaderIDs(tableModel).toString());
        assertEquals("[VARCHAR, VARCHAR, VARCHAR, TIMESTAMP, TIMESTAMP]",
                getHeaderDataTypes(tableModel).toString());
        List<TableModelRowWithObject<AuthorizationGroup>> rows = tableModel.getRows();
        assertSame(group, rows.get(0).getObjectOrNull());
        assertEquals(
                "[my-group, my lovely group, Newton, Issac, Thu Jan 01 01:00:04 CET 1970, Thu Jan 01 01:00:05 CET 1970]",
                rows.get(0).getValues().toString());
        assertEquals(1, rows.size());
        context.assertIsSatisfied();
    }
}
