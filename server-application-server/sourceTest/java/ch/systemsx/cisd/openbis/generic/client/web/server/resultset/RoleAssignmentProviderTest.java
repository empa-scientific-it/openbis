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
import java.util.List;

import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.authorization.TestAuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;

/**
 * @author Franz-Josef Elmer
 */
public class RoleAssignmentProviderTest extends AbstractProviderTest
{

    @Test
    public void testWithProjectAuthorizationDisabled()
    {
        test(new TestAuthorizationConfig(false, false));
    }

    @Test
    public void testWithProjectAuthorizationEnabled()
    {
        test(new TestAuthorizationConfig(true, true));
    }

    private void test(IAuthorizationConfig authorizationConfig)
    {
        final RoleAssignment r1 = new RoleAssignment();
        r1.setCode("r1");
        DatabaseInstance instance = new DatabaseInstance();
        instance.setCode("db");
        r1.setInstance(instance);

        final RoleAssignment r2 = new RoleAssignment();
        r2.setCode("r2");
        AuthorizationGroup group = new AuthorizationGroup();
        group.setCode("UG");
        r2.setAuthorizationGroup(group);
        Person person = new Person();
        person.setUserId("user");
        r2.setPerson(person);
        Space space = new Space();
        space.setCode("s1");
        r2.setSpace(space);

        final RoleAssignment r3 = new RoleAssignment();
        r3.setCode("r3");
        r3.setPerson(person);
        Project project = new Project();
        project.setIdentifier("/SPACE/PROJECT");
        r3.setProject(project);

        context.checking(new Expectations()
            {
                {
                    one(server).listRoleAssignments(SESSION_TOKEN);
                    will(returnValue(Arrays.asList(r1, r2, r3)));
                }
            });

        RoleAssignmentProvider provider = new RoleAssignmentProvider(server, authorizationConfig, SESSION_TOKEN);
        TypedTableModel<RoleAssignment> model = provider.createTableModel();

        if (authorizationConfig.isProjectLevelEnabled())
        {
            assertEquals("[PERSON, AUTHORIZATION_GROUP, SPACE, PROJECT, ROLE]", getHeaderIDs(model).toString());

            List<TableModelRowWithObject<RoleAssignment>> rows = model.getRows();
            assertEquals(3, rows.size());

            assertSame(r1, rows.get(0).getObjectOrNull());
            assertEquals("[, , , , r1]", rows.get(0).getValues().toString());
            assertSame(r2, rows.get(1).getObjectOrNull());
            assertEquals("[user, UG, s1, , r2]", rows.get(1).getValues().toString());
            assertSame(r3, rows.get(2).getObjectOrNull());
            assertEquals("[user, , , /SPACE/PROJECT, r3]", rows.get(2).getValues().toString());
        } else
        {
            assertEquals("[PERSON, AUTHORIZATION_GROUP, SPACE, ROLE]", getHeaderIDs(model).toString());

            List<TableModelRowWithObject<RoleAssignment>> rows = model.getRows();
            assertEquals(2, rows.size());

            assertSame(r1, rows.get(0).getObjectOrNull());
            assertEquals("[, , , r1]", rows.get(0).getValues().toString());
            assertSame(r2, rows.get(1).getObjectOrNull());
            assertEquals("[user, UG, s1, r2]", rows.get(1).getValues().toString());
        }

        context.assertIsSatisfied();
    }
}
