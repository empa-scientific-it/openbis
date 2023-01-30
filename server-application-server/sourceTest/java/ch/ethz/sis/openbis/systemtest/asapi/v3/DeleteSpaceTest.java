/*
 * Copyright 2015 ETH Zuerich, CISD
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.delete.SpaceDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author pkupczyk
 */
public class DeleteSpaceTest extends AbstractDeletionTest
{

    @Test
    public void testDeleteEmptyList()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SpaceDeletionOptions options = new SpaceDeletionOptions();
        options.setReason("It is just a test");

        v3api.deleteSpaces(sessionToken, new ArrayList<SpacePermId>(), options);
    }

    @Test
    public void testDeleteSpaceWithAdminUserInAnotherSpace()
    {
        final String sessionToken = v3api.login(TEST_ROLE_V3, PASSWORD);

        final SpaceDeletionOptions options = new SpaceDeletionOptions();
        options.setReason("It is just a test");

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.deleteSpaces(sessionToken, Arrays.asList(new SpacePermId("TEST-SPACE")), options);
                }
            }, new SpacePermId("TEST-SPACE"));
    }

    @Test
    public void testDeleteEmptySpace()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SpaceCreation creation = new SpaceCreation();
        creation.setCode("SPACE_TO_DELETE");

        List<SpacePermId> permIds = v3api.createSpaces(sessionToken, Arrays.asList(creation));

        Map<ISpaceId, Space> map = v3api.getSpaces(sessionToken, permIds, new SpaceFetchOptions());
        assertEquals(map.size(), 1);

        SpaceDeletionOptions options = new SpaceDeletionOptions();
        options.setReason("It is just a test");

        v3api.deleteSpaces(sessionToken, permIds, options);

        map = v3api.getSpaces(sessionToken, permIds, new SpaceFetchOptions());
        assertEquals(map.size(), 0);
    }

    @Test
    public void testDeleteNotEmptySpace()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SpaceDeletionOptions options = new SpaceDeletionOptions();
        options.setReason("It is just a test");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.deleteSpaces(sessionToken, Arrays.asList(new SpacePermId("CISD")), options);
                }
            }, "Space 'CISD' is being used. Delete all connected data  first.");
    }

    @Test
    public void testDeleteUnauthorizedSpace()
    {
        String adminSessionToken = v3api.login(TEST_USER, PASSWORD);

        SpaceCreation creation = new SpaceCreation();
        creation.setCode("SPACE_TO_DELETE");

        final List<SpacePermId> permIds = v3api.createSpaces(adminSessionToken, Arrays.asList(creation));

        Map<ISpaceId, Space> map = v3api.getSpaces(adminSessionToken, permIds, new SpaceFetchOptions());
        assertEquals(map.size(), 1);

        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final SpaceDeletionOptions options = new SpaceDeletionOptions();
        options.setReason("It is just a test");

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.deleteSpaces(sessionToken, permIds, options);
                }
            }, permIds.get(0));
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SpaceDeletionOptions o = new SpaceDeletionOptions();
        o.setReason("test-reason");

        v3api.deleteSpaces(sessionToken, Arrays.asList(new SpacePermId("TEST-LOGGING-1"), new SpacePermId("TEST-LOGGING-2")), o);

        assertAccessLog("delete-spaces  SPACE_IDS('[TEST-LOGGING-1, TEST-LOGGING-2]') DELETION_OPTIONS('SpaceDeletionOptions[reason=test-reason]')");
    }

}
