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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.dto.AuthorizationGroupPE;

/**
 * Test cases for {@link AuthorizationGroupDAO}.
 * 
 * @author Izabela Adamczyk
 */
@Test(groups =
{ "db", "authorization_group" })
public class AuthorizationGroupDAOTest extends AbstractDAOTest
{

    @Test
    public void testCreateAndListAuthorizationGroup() throws Exception
    {
        int existing = daoFactory.getAuthorizationGroupDAO().list().size();
        int create = 3;
        for (int i = existing; i < existing + create; i++)
        {
            AuthorizationGroupPE authGroup = createAuthorizationGroup("code-" + i, "desc-" + i);
            daoFactory.getAuthorizationGroupDAO().create(authGroup);
            List<AuthorizationGroupPE> list = daoFactory.getAuthorizationGroupDAO().list();
            AssertJUnit.assertEquals(i + 1, list.size());
            AssertJUnit.assertTrue(list.contains(authGroup));
        }
    }

    @Test
    public void testTryFindByCode() throws Exception
    {
        String code = "code";
        AssertJUnit.assertNull(daoFactory.getAuthorizationGroupDAO().tryFindByCode(code));
        AuthorizationGroupPE authGroup = createAuthorizationGroup(code, "description");
        daoFactory.getAuthorizationGroupDAO().create(authGroup);
        AssertJUnit.assertNotNull(daoFactory.getAuthorizationGroupDAO().tryFindByCode(code));
    }

    @Test(expectedExceptions = AssertionError.class)
    public void testTryCreateWithNull() throws Exception
    {
        daoFactory.getAuthorizationGroupDAO().create(null);
    }

    @Test(expectedExceptions = DataIntegrityViolationException.class)
    public void testTryCreateEmpty() throws Exception
    {
        daoFactory.getAuthorizationGroupDAO().create(new AuthorizationGroupPE());
    }

    @Test(expectedExceptions = DataIntegrityViolationException.class)
    public void testTryCreateWithTooShortCode() throws Exception
    {
        daoFactory.getAuthorizationGroupDAO().create(createAuthorizationGroup("", "desc"));
    }

    @Test(expectedExceptions = DataIntegrityViolationException.class)
    public void testTryCreateWithTooLongCode() throws Exception
    {
        daoFactory.getAuthorizationGroupDAO().create(
                createAuthorizationGroup(EXCEED_CODE_LENGTH_CHARACTERS, "desc"));
    }

    @Test(expectedExceptions = DataIntegrityViolationException.class)
    public void testTryCreateWithCodeWIthInvalidChars() throws Exception
    {
        daoFactory.getAuthorizationGroupDAO().create(createAuthorizationGroup("***", "desc"));
    }

    @Test
    public void testDeleteAuthGroup() throws Exception
    {
        String code = "code";
        AuthorizationGroupPE authGroup = createAuthorizationGroup(code, "description");
        daoFactory.getAuthorizationGroupDAO().create(authGroup);
        AssertJUnit.assertNotNull(daoFactory.getAuthorizationGroupDAO().tryFindByCode(code));
        daoFactory.getAuthorizationGroupDAO().delete(authGroup);
        AssertJUnit.assertNull(daoFactory.getAuthorizationGroupDAO().tryFindByCode(code));
    }

    @Test(expectedExceptions = DataIntegrityViolationException.class)
    public void testTryDeleteAuthGroupWithUsers() throws Exception
    {
        String code = "code";
        AuthorizationGroupPE authGroup = createAuthorizationGroup(code, "description");
        authGroup.addPerson(daoFactory.getPersonDAO().listPersons().get(0));
        daoFactory.getAuthorizationGroupDAO().create(authGroup);
        AssertJUnit.assertNotNull(daoFactory.getAuthorizationGroupDAO().tryFindByCode(code));
        daoFactory.getAuthorizationGroupDAO().delete(authGroup);
    }

    @Test
    public void testEditAuthGroup() throws Exception
    {
        String code = "code";
        String oldDescription = "old description";
        AuthorizationGroupPE authGroup = createAuthorizationGroup(code, oldDescription);
        daoFactory.getAuthorizationGroupDAO().create(authGroup);
        AssertJUnit.assertEquals(oldDescription, daoFactory.getAuthorizationGroupDAO()
                .tryFindByCode(code).getDescription());
        String newDescription = "new description";
        authGroup.setDescription(newDescription);
        AssertJUnit.assertEquals(newDescription, daoFactory.getAuthorizationGroupDAO()
                .tryFindByCode(code).getDescription());
    }

}
