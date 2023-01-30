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
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.create.TagCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.fetchoptions.TagFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagCode;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;

/**
 * @author pkupczyk
 */
public class GetTagTest extends AbstractTest
{

    @Test
    public void testGetByPermId()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        TagPermId permId1 = new TagPermId(TEST_USER, "TEST_METAPROJECTS");
        TagPermId permId2 = new TagPermId(TEST_USER, "ANOTHER_TEST_METAPROJECTS");

        Map<ITagId, Tag> map =
                v3api.getTags(sessionToken, Arrays.asList(permId1, permId2),
                        new TagFetchOptions());

        assertEquals(2, map.size());

        Iterator<Tag> iter = map.values().iterator();
        assertEquals(iter.next().getPermId(), permId1);
        assertEquals(iter.next().getPermId(), permId2);

        assertEquals(map.get(permId1).getPermId(), permId1);
        assertEquals(map.get(permId2).getPermId(), permId2);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByCode()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        TagCode code1 = new TagCode("TEST_METAPROJECTS");
        TagCode code2 = new TagCode("ANOTHER_TEST_METAPROJECTS");

        Map<ITagId, Tag> map =
                v3api.getTags(sessionToken, Arrays.asList(code1, code2),
                        new TagFetchOptions());

        assertEquals(2, map.size());

        Iterator<Tag> iter = map.values().iterator();
        assertEquals(iter.next().getCode(), code1.getCode());
        assertEquals(iter.next().getCode(), code2.getCode());

        assertEquals(map.get(code1).getCode(), code1.getCode());
        assertEquals(map.get(code2).getCode(), code2.getCode());

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByPermIdCaseSensitivity()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        // user name is case sensitive, code is case insensitive
        TagPermId permId1 = new TagPermId("teST", "TEST_METAPROJECTS");
        TagPermId permId2 = new TagPermId("test", "TEST_METAPROJECTS");
        TagPermId permId3 = new TagPermId("test", "ANOTHER_test_METAPROJECTS");

        Map<ITagId, Tag> map =
                v3api.getTags(sessionToken, Arrays.asList(permId1, permId2, permId3),
                        new TagFetchOptions());

        assertEquals(2, map.size());

        Iterator<Tag> iter = map.values().iterator();
        assertEquals(iter.next().getPermId(), permId2);
        assertEquals(iter.next().getPermId(), permId3);

        assertEquals(map.get(permId2).getPermId(), permId2);
        assertEquals(map.get(permId3).getPermId(), permId3);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByCodeCaseSensitivity()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        // code is case insensitive
        TagCode code1 = new TagCode("TEST_metaprojects");
        TagCode code2 = new TagCode("ANOTHER_test_METAPROJECTS");

        Map<ITagId, Tag> map =
                v3api.getTags(sessionToken, Arrays.asList(code1, code2),
                        new TagFetchOptions());

        assertEquals(2, map.size());

        Iterator<Tag> iter = map.values().iterator();
        assertEquals(iter.next().getCode(), code1.getCode());
        assertEquals(iter.next().getCode(), code2.getCode());

        assertEquals(map.get(code1).getCode(), code1.getCode());
        assertEquals(map.get(code2).getCode(), code2.getCode());

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdsNonexistent()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        TagPermId permId1 = new TagPermId(TEST_USER, "IDONTEXIST");
        TagPermId permId2 = new TagPermId(TEST_USER, "TEST_METAPROJECTS");
        TagCode code = new TagCode("IDONTEXIST");

        Map<ITagId, Tag> map =
                v3api.getTags(sessionToken,
                        Arrays.asList(permId1, permId2, code),
                        new TagFetchOptions());

        assertEquals(1, map.size());

        Iterator<Tag> iter = map.values().iterator();
        assertEquals(iter.next().getPermId(), permId2);
        assertEquals(map.get(permId2).getPermId(), permId2);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdsDuplicated()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        TagPermId permId = new TagPermId(TEST_USER, "TEST_METAPROJECTS");
        TagCode code = new TagCode("TEST_METAPROJECTS");

        Map<ITagId, Tag> map =
                v3api.getTags(sessionToken, Arrays.asList(permId, code), new TagFetchOptions());

        assertEquals(2, map.size());

        assertEquals(map.get(permId).getPermId(), permId);
        assertEquals(map.get(code).getCode(), code.getCode());

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdsUnauthorized()
    {
        TagPermId permId1 = new TagPermId(TEST_USER, "TEST_METAPROJECTS");
        TagPermId permId2 = new TagPermId(TEST_POWER_USER_CISD, "TEST_METAPROJECTS");

        List<? extends ITagId> ids = Arrays.asList(permId1, permId2);

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<ITagId, Tag> map = v3api.getTags(sessionToken, ids, new TagFetchOptions());

        assertEquals(map.size(), 1);
        assertEquals(map.get(permId1).getPermId(), permId1);
        assertEquals(map.get(permId2), null);

        v3api.logout(sessionToken);

        sessionToken = v3api.login(TEST_POWER_USER_CISD, PASSWORD);
        map = v3api.getTags(sessionToken, ids, new TagFetchOptions());

        assertEquals(map.size(), 1);
        assertEquals(map.get(permId2).getPermId(), permId2);
        assertEquals(map.get(permId1), null);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdsWithFetchOptionsEmpty()
    {
        TagPermId permId = new TagPermId(TEST_USER, "TEST_METAPROJECTS");

        TagFetchOptions fetchOptions = new TagFetchOptions();

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<ITagId, Tag> map = v3api.getTags(sessionToken, Arrays.asList(permId), fetchOptions);

        Tag tag = map.get(permId);

        assertEquals(tag.getPermId(), permId);
        assertEquals(tag.getCode(), "TEST_METAPROJECTS");
        assertEquals(tag.getDescription(), "Example metaproject no. 1");
        assertTrue(tag.isPrivate());

        assertOwnerNotFetched(tag);
        assertExperimentsNotFetched(tag);
        assertSamplesNotFetched(tag);
        assertDataSetsNotFetched(tag);
        assertMaterialsNotFetched(tag);
    }

    @Test
    public void testGetByIdsWithOwner()
    {
        TagPermId permId = new TagPermId(TEST_USER, "TEST_METAPROJECTS");

        TagFetchOptions fetchOptions = new TagFetchOptions();
        fetchOptions.withOwner();

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<ITagId, Tag> map = v3api.getTags(sessionToken, Arrays.asList(permId), fetchOptions);

        Tag tag = map.get(permId);

        assertEquals(tag.getPermId(), permId);
        assertEquals(tag.getCode(), "TEST_METAPROJECTS");
        assertEquals(tag.getDescription(), "Example metaproject no. 1");
        assertTrue(tag.isPrivate());
        assertEquals(tag.getOwner().getUserId(), TEST_USER);

        assertExperimentsNotFetched(tag);
        assertSamplesNotFetched(tag);
        assertDataSetsNotFetched(tag);
        assertMaterialsNotFetched(tag);
    }

    @Test
    public void testGetByIdsWithExperiments()
    {
        TagPermId permId = new TagPermId(TEST_USER, "TEST_METAPROJECTS");

        TagFetchOptions fetchOptions = new TagFetchOptions();
        fetchOptions.withExperiments();

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<ITagId, Tag> map = v3api.getTags(sessionToken, Arrays.asList(permId), fetchOptions);

        Tag tag = map.get(permId);

        assertEquals(tag.getPermId(), permId);
        assertEquals(tag.getCode(), "TEST_METAPROJECTS");
        assertEquals(tag.getDescription(), "Example metaproject no. 1");
        assertTrue(tag.isPrivate());
        assertExperimentIdentifiers(tag.getExperiments(), "/CISD/NEMO/EXP11", "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");

        assertOwnerNotFetched(tag);
        assertSamplesNotFetched(tag);
        assertDataSetsNotFetched(tag);
        assertMaterialsNotFetched(tag);
    }

    @Test
    public void testGetByIdsWithSamples()
    {
        TagPermId permId = new TagPermId(TEST_SPACE_USER, "TEST_METAPROJECTS");

        TagFetchOptions fetchOptions = new TagFetchOptions();
        fetchOptions.withSamples();

        String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);
        Map<ITagId, Tag> map = v3api.getTags(sessionToken, Arrays.asList(permId), fetchOptions);

        Tag tag = map.get(permId);

        assertEquals(tag.getPermId(), permId);
        assertEquals(tag.getCode(), "TEST_METAPROJECTS");
        assertEquals(tag.getDescription(), "Example metaproject no. 1");
        assertTrue(tag.isPrivate());
        assertSampleIdentifiers(tag.getSamples(), "/TEST-SPACE/TEST-PROJECT/FV-TEST", "/TEST-SPACE/TEST-PROJECT/EV-TEST");

        assertOwnerNotFetched(tag);
        assertExperimentsNotFetched(tag);
        assertDataSetsNotFetched(tag);
        assertMaterialsNotFetched(tag);
    }

    @Test
    public void testGetByIdsWithDataSets()
    {
        TagPermId permId = new TagPermId(TEST_SPACE_USER, "TEST_METAPROJECTS");

        TagFetchOptions fetchOptions = new TagFetchOptions();
        fetchOptions.withDataSets();

        String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);
        Map<ITagId, Tag> map = v3api.getTags(sessionToken, Arrays.asList(permId), fetchOptions);

        Tag tag = map.get(permId);

        assertEquals(tag.getPermId(), permId);
        assertEquals(tag.getCode(), "TEST_METAPROJECTS");
        assertEquals(tag.getDescription(), "Example metaproject no. 1");
        assertTrue(tag.isPrivate());
        assertDataSetCodes(tag.getDataSets(), "20120619092259000-22", "20120628092259000-24");

        assertOwnerNotFetched(tag);
        assertExperimentsNotFetched(tag);
        assertSamplesNotFetched(tag);
        assertMaterialsNotFetched(tag);
    }

    @Test
    public void testGetByIdsWithMaterials()
    {
        TagPermId permId = new TagPermId(TEST_USER, "TEST_METAPROJECTS");

        TagFetchOptions fetchOptions = new TagFetchOptions();
        fetchOptions.withMaterials();

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<ITagId, Tag> map = v3api.getTags(sessionToken, Arrays.asList(permId), fetchOptions);

        Tag tag = map.get(permId);

        assertEquals(tag.getPermId(), permId);
        assertEquals(tag.getCode(), "TEST_METAPROJECTS");
        assertEquals(tag.getDescription(), "Example metaproject no. 1");
        assertTrue(tag.isPrivate());
        assertMaterialPermIds(tag.getMaterials(), new MaterialPermId("AD3", "VIRUS"));

        assertOwnerNotFetched(tag);
        assertExperimentsNotFetched(tag);
        assertSamplesNotFetched(tag);
        assertDataSetsNotFetched(tag);
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testGetWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String sessionToken = v3api.login(user.getUserId(), PASSWORD);

        TagCreation creation = new TagCreation();
        creation.setCode("TAG_TO_GET");

        TagPermId permId = createTag(user.getUserId(), creation);

        if (user.isDisabledProjectUser())
        {
            assertAuthorizationFailureException(new IDelegatedAction()
                {
                    @Override
                    public void execute()
                    {
                        v3api.getTags(sessionToken, Arrays.asList(permId), new TagFetchOptions());
                    }
                });
        } else
        {
            Map<ITagId, Tag> map = v3api.getTags(sessionToken, Arrays.asList(permId), new TagFetchOptions());
            assertEquals(map.size(), 1);
        }
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        TagFetchOptions fo = new TagFetchOptions();
        fo.withOwner();
        fo.withExperiments();

        v3api.getTags(sessionToken,
                Arrays.asList(new TagCode("TEST_METAPROJECTS"), new TagPermId(TEST_USER, "ANOTHER_TEST_METAPROJECTS")), fo);

        assertAccessLog(
                "get-tags  TAG_IDS('[TEST_METAPROJECTS, /test/ANOTHER_TEST_METAPROJECTS]') FETCH_OPTIONS('Tag\n    with Experiments\n    with Owner\n')");
    }

}
