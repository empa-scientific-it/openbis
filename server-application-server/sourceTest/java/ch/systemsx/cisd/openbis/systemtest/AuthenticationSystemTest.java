/*
 * Copyright ETH 2007 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.systemtest;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * @author Franz-Josef Elmer
 */
@Test(groups = "system test")
public class AuthenticationSystemTest extends SystemTestCase
{
    @Test
    public void testTryToAuthenticateWithMissingPassword()
    {
        try
        {
            commonClientService.tryToLogin("test", "");
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("No 'password' specified.", ex.getMessage());
        }
    }

    @Test
    public void testTryToAuthenticateAsSystem()
    {
        SessionContextDTO systemUser = commonServer.tryToAuthenticateAsSystem();
        String sessionToken = systemUser.getSessionToken();
        PersonPE person = commonServer.getAuthSession(sessionToken).tryGetPerson();
        RoleAssignmentPE role = person.getAllPersonRoles().iterator().next();
        assertEquals(null, role.getSpace());
        assertEquals(RoleCode.ADMIN, role.getRole());
    }
}
