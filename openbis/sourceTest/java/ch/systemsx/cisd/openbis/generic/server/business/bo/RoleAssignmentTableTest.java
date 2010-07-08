/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.Arrays;

import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewRoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;

/**
 * Test cases for corresponding {@link RoleAssignmentTable} class.
 * 
 * @author Christian Ribeaud
 */
public final class RoleAssignmentTableTest extends AbstractBOTest
{

    private final RoleAssignmentTable createRoleAssignmentTable()
    {
        return new RoleAssignmentTable(daoFactory, ManagerTestTool.EXAMPLE_SESSION);
    }

    @Test
    public final void testAdd()
    {
        final NewRoleAssignment newRoleAssignment = new NewRoleAssignment();
        final String databaseInstanceCode = "DB2";
        final String groupCode = "cisd";
        final GroupIdentifier groupIdentifier =
                new GroupIdentifier(databaseInstanceCode, groupCode);
        newRoleAssignment.setSpaceIdentifier(groupIdentifier);
        newRoleAssignment.setRole(RoleCode.OBSERVER);
        final String userId = "test";
        // TODO 2009-07-31,IA: add test for auth group
        newRoleAssignment.setGrantee(Grantee.createPerson(userId));
        final PersonPE personPE = new PersonPE();
        personPE.setUserId(userId);
        final DatabaseInstancePE databaseInstancePE = new DatabaseInstancePE();
        databaseInstancePE.setCode(databaseInstanceCode);
        final long databaseInstanceId = 12L;
        databaseInstancePE.setId(databaseInstanceId);
        final GroupPE groupPE = new GroupPE();
        groupPE.setCode(groupCode);
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getPersonDAO();
                    will(returnValue(personDAO));

                    one(personDAO).listPersons();
                    will(returnValue(Arrays.asList(new PersonPE[]
                        { personPE })));

                    one(databaseInstanceDAO).tryFindDatabaseInstanceByCode(databaseInstanceCode);
                    will(returnValue(databaseInstancePE));

                    one(groupDAO).tryFindGroupByCodeAndDatabaseInstance(groupCode.toUpperCase(),
                            databaseInstancePE);
                    will(returnValue(groupPE));
                }
            });
        createRoleAssignmentTable().add(newRoleAssignment);
        context.assertIsSatisfied();
    }
}
