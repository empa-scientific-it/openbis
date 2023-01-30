/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

import static org.testng.Assert.assertEquals;

import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;

/**
 * @author pkupczyk
 */
public class CreateCodesTest extends AbstractTest
{

    @Test
    public void testCreateOne()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        int initialValue = getCurrentSequenceValue(EntityKind.SAMPLE);

        List<String> codes = v3api.createCodes(sessionToken, "ABC-", EntityKind.SAMPLE, 1);

        assertEquals(codes.size(), 1);
        assertEquals(codes.get(0), "ABC-" + (initialValue + 1));
    }

    @Test
    public void testCreateMultiple()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        int initialValue = getCurrentSequenceValue(EntityKind.SAMPLE);

        List<String> codes = v3api.createCodes(sessionToken, "ABC-", EntityKind.SAMPLE, 3);

        assertEquals(codes.size(), 3);
        assertEquals(codes.get(0), "ABC-" + (initialValue + 1));
        assertEquals(codes.get(1), "ABC-" + (initialValue + 2));
        assertEquals(codes.get(2), "ABC-" + (initialValue + 3));
    }

    @Test
    public void testCreateWithoutPrefix()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        int initialValue = getCurrentSequenceValue(EntityKind.SAMPLE);

        List<String> codes = v3api.createCodes(sessionToken, null, EntityKind.SAMPLE, 2);

        assertEquals(codes.size(), 2);
        assertEquals(codes.get(0), String.valueOf(initialValue + 1));
        assertEquals(codes.get(1), String.valueOf(initialValue + 2));
    }

    @Test
    public void testCreateWithoutEntityKind()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createCodes(sessionToken, "ABC-", null, 2);
                }
            }, "Entity kind cannot be null");
    }

    @Test
    public void testCreateWithEntityKind()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        for (EntityKind entityKind : EntityKind.values())
        {
            int initialValue = getCurrentSequenceValue(entityKind);

            List<String> codes = v3api.createCodes(sessionToken, "ABC-", entityKind, 2);

            assertEquals(codes.size(), 2);
            assertEquals(codes.get(0), "ABC-" + (initialValue + 1));
            assertEquals(codes.get(1), "ABC-" + (initialValue + 2));
        }
    }

    @Test
    public void testCreateWithZeroCount()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createCodes(sessionToken, "ABC-", EntityKind.SAMPLE, 0);
                }
            }, "Count cannot be <= 0");
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testCreateWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String sessionToken = v3api.login(user.getUserId(), PASSWORD);

        if (user.isDisabledProjectUser())
        {
            assertAuthorizationFailureException(new IDelegatedAction()
                {
                    @Override
                    public void execute()
                    {
                        v3api.createCodes(sessionToken, "ABC-", EntityKind.SAMPLE, 1);
                    }
                });
        } else
        {
            int initialValue = getCurrentSequenceValue(EntityKind.SAMPLE);

            List<String> codes = v3api.createCodes(sessionToken, "ABC-", EntityKind.SAMPLE, 1);

            assertEquals(codes.size(), 1);
            assertEquals(codes.get(0), "ABC-" + (initialValue + 1));
        }
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        v3api.createCodes(sessionToken, "ABC-", EntityKind.SAMPLE, 3);

        assertAccessLog("create-codes  PREFIX('ABC-') ENTITY_KIND('SAMPLE') COUNT('3')");
    }

    private int getCurrentSequenceValue(EntityKind entityKind)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        List<String> codes = v3api.createCodes(sessionToken, "DOESNOTMATTER-", entityKind, 1);
        assertEquals(codes.size(), 1);
        return Integer.valueOf(codes.get(0).split("-")[1]);
    }

}
