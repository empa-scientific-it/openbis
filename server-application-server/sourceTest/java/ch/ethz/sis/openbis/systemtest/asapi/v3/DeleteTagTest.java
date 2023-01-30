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
import static org.testng.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.create.TagCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.delete.TagDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.fetchoptions.TagFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagCode;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;

/**
 * @author pkupczyk
 */
public class DeleteTagTest extends AbstractDeletionTest
{

    @Test
    public void testDeleteEmptyList()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        TagDeletionOptions options = new TagDeletionOptions();
        options.setReason("It is just a test");

        v3api.deleteTags(sessionToken, new ArrayList<TagPermId>(), options);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ".*Entity ids cannot be null.*")
    public void testDeleteWithNullTagIds()
    {
        TagDeletionOptions options = new TagDeletionOptions();
        options.setReason("It is just a test");

        deleteTag(TEST_USER, PASSWORD, null, options);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ".*Deletion options cannot be null.*")
    public void testDeleteWithNullOptions()
    {
        TagCreation creation = new TagCreation();
        creation.setCode("TAG_TO_DELETE");

        Tag before = createTag(TEST_USER, PASSWORD, creation);

        deleteTag(TEST_USER, PASSWORD, before.getPermId(), null);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ".*Deletion reason cannot be null.*")
    public void testDeleteWithNullReason()
    {
        TagCreation creation = new TagCreation();
        creation.setCode("TAG_TO_DELETE");

        Tag before = createTag(TEST_USER, PASSWORD, creation);

        TagDeletionOptions options = new TagDeletionOptions();

        deleteTag(TEST_USER, PASSWORD, before.getPermId(), options);
    }

    @Test
    public void testDeleteWithEmptyTag()
    {
        TagCreation creation = new TagCreation();
        creation.setCode("TAG_TO_DELETE");

        Tag before = createTag(TEST_USER, PASSWORD, creation);

        TagDeletionOptions options = new TagDeletionOptions();
        options.setReason("It is just a test");

        Tag after = deleteTag(TEST_USER, PASSWORD, before.getPermId(), options);
        assertNull(after);
    }

    @Test
    public void testDeleteWithNotEmptyTag()
    {
        ExperimentPermId experimentId = new ExperimentPermId("200811050952663-1029");
        SamplePermId sampleId = new SamplePermId("200902091219327-1025");
        DataSetPermId dataSetId = new DataSetPermId("20120619092259000-22");
        MaterialPermId materialId = new MaterialPermId("AD3", "VIRUS");

        TagCreation creation = new TagCreation();
        creation.setCode("TAG_TO_DELETE");
        creation.setExperimentIds(Arrays.asList(experimentId));
        creation.setSampleIds(Arrays.asList(sampleId));
        creation.setDataSetIds(Arrays.asList(dataSetId));
        creation.setMaterialIds(Arrays.asList(materialId));

        Tag before = createTag(TEST_USER, PASSWORD, creation);

        TagDeletionOptions options = new TagDeletionOptions();
        options.setReason("It is just a test");

        Tag after = deleteTag(TEST_USER, PASSWORD, before.getPermId(), options);
        assertNull(after);

        assertExperimentsExists(experimentId.getPermId());
        assertSamplesExists(sampleId.getPermId());
        assertDataSetsExists(dataSetId.getPermId());
        assertMaterialsExists(materialId);
    }

    @Test
    public void testDeleteWithObserver()
    {
        TagCreation creation = new TagCreation();
        creation.setCode("TAG_TO_DELETE");

        Tag before = createTag(TEST_GROUP_OBSERVER, PASSWORD, creation);

        TagDeletionOptions options = new TagDeletionOptions();
        options.setReason("It is just a test");

        Tag after = deleteTag(TEST_GROUP_OBSERVER, PASSWORD, before.getPermId(), options);
        assertNull(after);
    }

    @Test
    public void testDeleteWithUnauthorizedTag()
    {
        TagCreation creation = new TagCreation();
        creation.setCode("TAG_TO_DELETE");

        final Tag before = createTag(TEST_SPACE_USER, PASSWORD, creation);

        final TagDeletionOptions options = new TagDeletionOptions();
        options.setReason("It is just a test");

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    deleteTag(TEST_USER, PASSWORD, before.getPermId(), options);
                }
            }, before.getPermId());
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testDeleteWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        TagCreation creation = new TagCreation();
        creation.setCode("TAG_TO_DELETE");

        final TagPermId tagId = createTag(user.getUserId(), creation);

        final TagDeletionOptions options = new TagDeletionOptions();
        options.setReason("It is just a test");

        if (user.isDisabledProjectUser())
        {
            assertAuthorizationFailureException(new IDelegatedAction()
                {
                    @Override
                    public void execute()
                    {
                        deleteTag(user.getUserId(), PASSWORD, tagId, options);
                    }
                });
        } else
        {
            deleteTag(user.getUserId(), PASSWORD, tagId, options);
        }
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        TagDeletionOptions o = new TagDeletionOptions();
        o.setReason("test-reason");

        v3api.deleteTags(sessionToken,
                Arrays.asList(new TagCode("TEST-LOGGING-1"), new TagPermId("/test/TEST-LOGGING-2")), o);

        assertAccessLog(
                "delete-tags  TAG_IDS('[TEST-LOGGING-1, /test/TEST-LOGGING-2]') DELETION_OPTIONS('TagDeletionOptions[reason=test-reason]')");
    }

    private Tag createTag(String user, String password, TagCreation creation)
    {
        String sessionToken = v3api.login(user, password);

        List<TagPermId> permIds = v3api.createTags(sessionToken, Arrays.asList(creation));

        Map<ITagId, Tag> map = v3api.getTags(sessionToken, permIds, new TagFetchOptions());
        assertEquals(map.size(), 1);

        return map.get(permIds.get(0));
    }

    private Tag deleteTag(String user, String password, ITagId id, TagDeletionOptions options)
    {
        String sessionToken = v3api.login(user, password);

        List<ITagId> ids = null;

        if (id != null)
        {
            ids = Arrays.asList(id);
        }

        v3api.deleteTags(sessionToken, ids, options);

        Map<ITagId, Tag> map = v3api.getTags(sessionToken, ids, new TagFetchOptions());

        return map.get(id);
    }

}
