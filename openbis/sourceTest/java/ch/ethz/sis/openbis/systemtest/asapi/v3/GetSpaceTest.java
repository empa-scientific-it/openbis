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
import static org.testng.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;

/**
 * @author pkupczyk
 */
public class GetSpaceTest extends AbstractTest
{

    @Test
    public void testGetByPermId()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SpacePermId permId1 = new SpacePermId("CISD");
        SpacePermId permId2 = new SpacePermId("/TEST-SPACE");

        Map<ISpaceId, Space> map =
                v3api.getSpaces(sessionToken, Arrays.asList(permId1, permId2),
                        new SpaceFetchOptions());

        assertEquals(2, map.size());

        Iterator<Space> iter = map.values().iterator();
        assertEquals(iter.next().getPermId(), permId1);
        assertEquals(iter.next().getPermId(), permId2);

        assertEquals(map.get(permId1).getPermId(), permId1);
        assertEquals(map.get(permId2).getPermId(), permId2);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByPermIdCaseInsensitive()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SpacePermId permId1 = new SpacePermId("CisD");
        SpacePermId permId2 = new SpacePermId("/test-SPACE");

        Map<ISpaceId, Space> map =
                v3api.getSpaces(sessionToken, Arrays.asList(permId1, permId2),
                        new SpaceFetchOptions());

        assertEquals(2, map.size());

        Iterator<Space> iter = map.values().iterator();
        assertEquals(iter.next().getPermId(), permId1);
        assertEquals(iter.next().getPermId(), permId2);

        assertEquals(map.get(permId1).getPermId().getPermId(), "CISD");
        assertEquals(map.get(new SpacePermId("CISD")).getPermId().getPermId(), "CISD");

        assertEquals(map.get(permId2).getPermId().getPermId(), "TEST-SPACE");
        assertEquals(map.get(new SpacePermId("/TEST-SPACE")).getPermId().getPermId(), "TEST-SPACE");

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdsNonexistent()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SpacePermId permId1 = new SpacePermId("/CISD");
        SpacePermId permId2 = new SpacePermId("NONEXISTENT_SPACE");
        SpacePermId permId3 = new SpacePermId("TEST-SPACE");

        Map<ISpaceId, Space> map =
                v3api.getSpaces(sessionToken,
                        Arrays.asList(permId1, permId2, permId3),
                        new SpaceFetchOptions());

        assertEquals(2, map.size());

        Iterator<Space> iter = map.values().iterator();
        assertEquals(iter.next().getPermId(), permId1);
        assertEquals(iter.next().getPermId(), permId3);

        assertEquals(map.get(permId1).getPermId(), permId1);
        assertEquals(map.get(permId3).getPermId(), permId3);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdsDuplicated()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SpacePermId permId1 = new SpacePermId("CISD");
        SpacePermId permId2 = new SpacePermId("/CISD");

        Map<ISpaceId, Space> map =
                v3api.getSpaces(sessionToken, Arrays.asList(permId1, permId2), new SpaceFetchOptions());

        assertEquals(1, map.size());

        assertEquals(map.get(permId1).getPermId(), permId1);
        assertEquals(map.get(permId2).getPermId(), permId2);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdsUnauthorized()
    {
        SpacePermId permId1 = new SpacePermId("CISD");
        SpacePermId permId2 = new SpacePermId("TEST-SPACE");

        List<? extends ISpaceId> ids = Arrays.asList(permId1, permId2);

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<ISpaceId, Space> map = v3api.getSpaces(sessionToken, ids, new SpaceFetchOptions());

        assertEquals(map.size(), 2);
        v3api.logout(sessionToken);

        sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);
        map = v3api.getSpaces(sessionToken, ids, new SpaceFetchOptions());

        assertEquals(map.size(), 1);

        assertEquals(map.get(permId2).getPermId(), permId2);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdsWithFetchOptionsEmpty()
    {
        SpacePermId permId = new SpacePermId("TEST-SPACE");

        SpaceFetchOptions fetchOptions = new SpaceFetchOptions();

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<ISpaceId, Space> map = v3api.getSpaces(sessionToken, Arrays.asList(permId), fetchOptions);

        Space space = map.get(permId);

        assertEquals(space.getPermId(), permId);
        assertEquals(space.getCode(), "TEST-SPACE");
        assertEquals(space.getDescription(), "myDescription");
        assertEqualsDate(space.getRegistrationDate(), "2008-11-05 09:18:11");
        assertEqualsDate(space.getModificationDate(), "2008-11-05 09:18:11");

        assertProjectsNotFetched(space);
        assertSamplesNotFetched(space);
        assertRegistratorNotFetched(space);
    }

    @Test
    public void testGetByIdsWithProjects()
    {
        SpacePermId permId = new SpacePermId("TEST-SPACE");

        SpaceFetchOptions fetchOptions = new SpaceFetchOptions();
        fetchOptions.withProjects();

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<ISpaceId, Space> map = v3api.getSpaces(sessionToken, Arrays.asList(permId), fetchOptions);

        Space space = map.get(permId);
        List<Project> projects = space.getProjects();

        assertProjectIdentifiers(projects, "/TEST-SPACE/TEST-PROJECT", "/TEST-SPACE/NOE", "/TEST-SPACE/PROJECT-TO-DELETE");

        assertSamplesNotFetched(space);
        assertRegistratorNotFetched(space);
    }

    @Test
    public void testGetByIdsWithSamples()
    {
        SpacePermId permId = new SpacePermId("TEST-SPACE");

        SpaceFetchOptions fetchOptions = new SpaceFetchOptions();
        fetchOptions.withSamples();

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<ISpaceId, Space> map = v3api.getSpaces(sessionToken, Arrays.asList(permId), fetchOptions);

        Space space = map.get(permId);
        List<Sample> samples = space.getSamples();

        assertSampleIdentifiers(samples, "/TEST-SPACE/TEST-PROJECT/FV-TEST", "/TEST-SPACE/TEST-PROJECT/EV-TEST", 
                "/TEST-SPACE/TEST-PROJECT/EV-INVALID", "/TEST-SPACE/TEST-PROJECT/EV-NOT_INVALID",
                "/TEST-SPACE/TEST-PROJECT/EV-PARENT", "/TEST-SPACE/TEST-PROJECT/EV-PARENT-NORMAL",
                "/TEST-SPACE/NOE/CP-TEST-4", "/TEST-SPACE/TEST-PROJECT/SAMPLE-TO-DELETE");

        assertProjectsNotFetched(space);
        assertRegistratorNotFetched(space);
    }

    @Test
    public void testGetByIdsWithRegistrator()
    {
        SpacePermId permId = new SpacePermId("TEST-SPACE");

        SpaceFetchOptions fetchOptions = new SpaceFetchOptions();
        fetchOptions.withRegistrator().withSpace();
        fetchOptions.withRegistrator().withRegistrator();

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<ISpaceId, Space> map = v3api.getSpaces(sessionToken, Arrays.asList(permId), fetchOptions);

        Space space = map.get(permId);
        Person registrator = space.getRegistrator();

        assertEquals(registrator.getUserId(), "test");
        assertNotNull(registrator.getFirstName());
        assertNotNull(registrator.getLastName());
        assertEquals(registrator.getEmail(), "franz-josef.elmer@systemsx.ch");
        assertEqualsDate(registrator.getRegistrationDate(), "2008-11-05 09:18:10");
        assertEquals(registrator.isActive(), Boolean.TRUE);
        assertEquals(registrator.getSpace().getCode(), "CISD");
        assertEquals(registrator.getRegistrator().getUserId(), "system");

        assertProjectsNotFetched(space);
        assertSamplesNotFetched(space);
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testGetWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SpacePermId permId1 = new SpacePermId("/CISD");
        SpacePermId permId2 = new SpacePermId("/TEST-SPACE");

        List<? extends ISpaceId> ids = Arrays.asList(permId1, permId2);

        String sessionToken = v3api.login(user.getUserId(), PASSWORD);

        if (user.isDisabledProjectUser())
        {
            assertAuthorizationFailureException(new IDelegatedAction()
                {
                    @Override
                    public void execute()
                    {
                        v3api.getSpaces(sessionToken, ids, new SpaceFetchOptions());
                    }
                });
        } else
        {
            Map<ISpaceId, Space> map = v3api.getSpaces(sessionToken, ids, new SpaceFetchOptions());

            if (user.isInstanceUser())
            {
                assertEquals(map.size(), 2);
            } else if (user.isTestSpaceUser() || user.isTestProjectUser())
            {
                assertEquals(map.size(), 1);
                assertEquals(map.get(permId2).getPermId(), permId2);
            } else
            {
                assertEquals(map.size(), 0);
            }
        }

        v3api.logout(sessionToken);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SpaceFetchOptions fo = new SpaceFetchOptions();
        fo.withProjects();
        fo.withSamples();

        v3api.getSpaces(sessionToken, Arrays.asList(new SpacePermId("CISD"), new SpacePermId("/TEST-SPACE")), fo);

        assertAccessLog("get-spaces  SPACE_IDS('[CISD, TEST-SPACE]') FETCH_OPTIONS('Space\n    with Samples\n    with Projects\n')");
    }

}
