/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.junit.Assert.assertNull;
import static org.testng.Assert.assertNotNull;

import java.util.Arrays;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.PersonalAccessToken;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.delete.PersonalAccessTokenDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.IPersonalAccessTokenId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.PersonalAccessTokenPermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author pkupczyk
 */
@Test
public class DeletePersonalAccessTokenTest extends AbstractPersonalAccessTokenTest
{

    @Test
    public void testDelete()
    {
        PersonalAccessToken beforeToken = createToken(TEST_USER, PASSWORD, testCreation());
        assertNotNull(beforeToken);

        PersonalAccessToken afterToken = deleteToken(TEST_USER, PASSWORD, beforeToken.getPermId());
        assertNull(afterToken);
    }

    @Test
    public void testDeleteByOwner()
    {
        testDeleteBy(TEST_GROUP_OBSERVER, TEST_GROUP_OBSERVER);
    }

    @Test
    public void testDeleteByNonOwnerETLServerUser()
    {
        testDeleteBy(TEST_GROUP_OBSERVER, TEST_INSTANCE_ETLSERVER);
    }

    @Test
    public void testDeleteByNonOwnerInstanceAdminUser()
    {
        testDeleteBy(TEST_GROUP_OBSERVER, INSTANCE_ADMIN_USER);
    }

    @Test
    public void testDeleteByNonOwnerRegularUser()
    {
        assertUnauthorizedObjectAccessException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                testDeleteBy(TEST_USER, TEST_GROUP_OBSERVER);
            }
        }, null);
    }

    private void testDeleteBy(String ownerId, String updaterId)
    {
        PersonalAccessToken token = createToken(ownerId, PASSWORD, testCreation());
        deleteToken(updaterId, PASSWORD, token.getPermId());
    }

    @Test
    public void testDeleteWithRegularSessionTokenAsSessionToken()
    {
        PersonalAccessToken token = createToken(TEST_USER, PASSWORD, testCreation());

        PersonalAccessTokenDeletionOptions options = new PersonalAccessTokenDeletionOptions();
        options.setReason("It is just a test");

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        v3api.deletePersonalAccessTokens(sessionToken, Arrays.asList(token.getPermId()), options);

        assertNull(getToken(TEST_USER, PASSWORD, token.getPermId()));
    }

    @Test
    public void testDeleteWithPersonalAccessTokenAsSessionToken()
    {
        PersonalAccessToken token = createToken(TEST_USER, PASSWORD, testCreation());

        PersonalAccessTokenDeletionOptions options = new PersonalAccessTokenDeletionOptions();
        options.setReason("It is just a test");

        assertUserFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.deletePersonalAccessTokens(token.getHash(), Arrays.asList(token.getPermId()), options);
            }
        }, "Personal access tokens cannot be used to manage personal access tokens");
    }

    @Test
    public void testDeleteWithIdNonexistent()
    {
        deleteToken(TEST_USER, PASSWORD, new PersonalAccessTokenPermId("IDONTEXIST"));
    }

    @Test
    public void testLogging()
    {
        PersonalAccessToken token = createToken(TEST_USER, PASSWORD, testCreation());

        deleteToken(TEST_USER, PASSWORD, token.getPermId());

        assertAccessLog("delete-personal-access-tokens  PERSONAL_ACCESS_TOKEN_IDS('[" + token.getHash()
                + "]') DELETION_OPTIONS('PersonalAccessTokenDeletionOptions[reason=It is just a test]')");
    }

    protected PersonalAccessToken deleteToken(String user, String password, IPersonalAccessTokenId tokenId)
    {
        String sessionToken = v3api.login(user, password);

        PersonalAccessTokenDeletionOptions options = new PersonalAccessTokenDeletionOptions();
        options.setReason("It is just a test");

        v3api.deletePersonalAccessTokens(sessionToken, Arrays.asList(tokenId), options);

        return getToken(user, password, tokenId);
    }

}
