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

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
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
@Test(groups = { "before remote api" })
public class CreateTagTest extends AbstractTest
{
    @Test
    public void testCreateWithObserver()
    {
        TagCreation creation = new TagCreation();
        creation.setCode("TEST_TAG");
        creation.setDescription("test description");

        Tag tag = createTag(TEST_GROUP_OBSERVER, PASSWORD, creation);

        assertEquals(tag.getDescription(), creation.getDescription());
    }

    @Test
    public void testCreateWithCodeNull()
    {
        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    TagCreation creation = new TagCreation();

                    createTag(TEST_USER, PASSWORD, creation);
                }
            }, "Code cannot be empty");
    }

    @Test
    public void testCreateWithCodeExisting()
    {
        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    TagCreation creation = new TagCreation();
                    creation.setCode("TEST_METAPROJECTS");

                    createTag(TEST_USER, PASSWORD, creation);
                }
            }, "Tag already exists in the database and needs to be unique");
    }

    @Test
    public void testCreateWithCodeIncorrect()
    {
        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    TagCreation creation = new TagCreation();
                    creation.setCode("i am incorrect");

                    createTag(TEST_USER, PASSWORD, creation);
                }
            }, "Tag name cannot contain white spaces, commas, slashes or backslashes");
    }

    @Test
    public void testCreateWithDescription()
    {
        TagCreation creation = new TagCreation();
        creation.setCode("TEST_TAG");
        creation.setDescription("test description");

        Tag tag = createTag(TEST_USER, PASSWORD, creation);

        assertEquals(tag.getDescription(), creation.getDescription());
    }

    @Test
    public void testCreateWithExperiments()
    {
        final ExperimentIdentifier experimentId = new ExperimentIdentifier("/CISD/NEMO/EXP10");

        TagCreation creation = new TagCreation();
        creation.setCode("TEST_TAG");
        creation.setExperimentIds(Arrays.asList(experimentId));

        Tag tag = createTag(TEST_USER, PASSWORD, creation);

        assertExperimentIdentifiers(tag.getExperiments(), experimentId.getIdentifier());
        assertExperimentsExists("200811050952663-1029");
    }

    @Test
    public void testCreateWithExperimentsUnauthorized()
    {
        final ExperimentIdentifier experimentId = new ExperimentIdentifier("/CISD/NEMO/EXP10");

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    TagCreation creation = new TagCreation();
                    creation.setCode("TEST_TAG");
                    creation.setExperimentIds(Arrays.asList(experimentId));

                    createTag(TEST_SPACE_USER, PASSWORD, creation);
                }
            }, experimentId);
    }

    @Test
    public void testCreateWithExperimentsNonexistent()
    {
        final ExperimentIdentifier experimentId = new ExperimentIdentifier("/CISD/NEMO/IDONTEXIST");

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    TagCreation creation = new TagCreation();
                    creation.setCode("TEST_TAG");
                    creation.setExperimentIds(Arrays.asList(experimentId));

                    createTag(TEST_USER, PASSWORD, creation);
                }
            }, experimentId);
    }

    @Test
    public void testCreateWithSamples()
    {
        final SampleIdentifier sampleId = new SampleIdentifier("/CISD/NEMO/CP-TEST-1");

        TagCreation creation = new TagCreation();
        creation.setCode("TEST_TAG");
        creation.setSampleIds(Arrays.asList(sampleId));

        Tag tag = createTag(TEST_USER, PASSWORD, creation);

        assertSampleIdentifiers(tag.getSamples(), sampleId.getIdentifier());
        assertSamplesExists("200902091219327-1025");
    }

    @Test
    public void testCreateWithSamplesUnauthorized()
    {
        final SampleIdentifier sampleId = new SampleIdentifier("/CISD/NEMO/CP-TEST-1");

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    TagCreation creation = new TagCreation();
                    creation.setCode("TEST_TAG");
                    creation.setSampleIds(Arrays.asList(sampleId));

                    createTag(TEST_SPACE_USER, PASSWORD, creation);
                }
            }, sampleId);
    }

    @Test
    public void testCreateWithSamplesNonexistent()
    {
        final SampleIdentifier sampleId = new SampleIdentifier("/CISD/IDONTEXIST");

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    TagCreation creation = new TagCreation();
                    creation.setCode("TEST_TAG");
                    creation.setSampleIds(Arrays.asList(sampleId));

                    createTag(TEST_USER, PASSWORD, creation);
                }
            }, sampleId);
    }

    @Test
    public void testCreateWithDataSets()
    {
        final DataSetPermId dataSetId = new DataSetPermId("20120619092259000-22");

        TagCreation creation = new TagCreation();
        creation.setCode("TEST_TAG");
        creation.setDataSetIds(Arrays.asList(dataSetId));

        Tag tag = createTag(TEST_USER, PASSWORD, creation);

        assertDataSetCodes(tag.getDataSets(), "20120619092259000-22");
        assertDataSetsExists("20120619092259000-22");
    }

    @Test
    public void testCreateWithDataSetsUnauthorized()
    {
        // data set connected to experiment /CISD/NEMO/EXP-TEST-1
        final DataSetPermId dataSetId = new DataSetPermId("20081105092159111-1");

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    TagCreation creation = new TagCreation();
                    creation.setCode("TEST_TAG");
                    creation.setDataSetIds(Arrays.asList(dataSetId));

                    createTag(TEST_SPACE_USER, PASSWORD, creation);
                }
            }, dataSetId);
    }

    @Test
    public void testCreateWithDataSetsNonexistent()
    {
        final DataSetPermId dataSetId = new DataSetPermId("IDONTEXIST");

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    TagCreation creation = new TagCreation();
                    creation.setCode("TEST_TAG");
                    creation.setDataSetIds(Arrays.asList(dataSetId));

                    createTag(TEST_USER, PASSWORD, creation);
                }
            }, dataSetId);
    }

    @Test
    public void testCreateWithMaterials()
    {
        MaterialPermId materialId = new MaterialPermId("AD3", "VIRUS");

        TagCreation creation = new TagCreation();
        creation.setCode("TEST_TAG");
        creation.setMaterialIds(Arrays.asList(materialId));

        Tag tag = createTag(TEST_USER, PASSWORD, creation);

        assertMaterialPermIds(tag.getMaterials(), materialId);
        assertMaterialsExists(materialId);
    }

    @Test
    public void testCreateWithMaterialsUnauthorized()
    {
        // nothing to test as the materials can be accessed by every user
    }

    @Test
    public void testCreateWithMaterialsNonexistent()
    {
        final MaterialPermId materialId = new MaterialPermId("IDONTEXIST", "VIRUS");

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    TagCreation creation = new TagCreation();
                    creation.setCode("TEST_TAG");
                    creation.setMaterialIds(Arrays.asList(materialId));

                    createTag(TEST_USER, PASSWORD, creation);
                }
            }, materialId);
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testCreateWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        TagCreation creation = new TagCreation();
        creation.setCode("TEST_TAG");
        creation.setDescription("test description");

        if (user.isDisabledProjectUser())
        {
            assertUnauthorizedObjectAccessException(new IDelegatedAction()
                {
                    @Override
                    public void execute()
                    {
                        createTag(user.getUserId(), PASSWORD, creation);
                    }
                }, new TagCode(creation.getCode()));
        } else
        {
            Tag tag = createTag(user.getUserId(), PASSWORD, creation);
            assertEquals(tag.getCode(), creation.getCode());
        }
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        TagCreation creation = new TagCreation();
        creation.setCode("LOG_TEST_1");

        TagCreation creation2 = new TagCreation();
        creation2.setCode("LOG_TEST_2");

        v3api.createTags(sessionToken, Arrays.asList(creation, creation2));

        assertAccessLog("create-tags  NEW_TAGS('[TagCreation[code=LOG_TEST_1], TagCreation[code=LOG_TEST_2]]')");
    }

    private Tag createTag(String user, String password, TagCreation creation)
    {
        String sessionToken = v3api.login(user, password);

        List<TagPermId> ids = v3api.createTags(sessionToken, Arrays.asList(creation));

        assertEquals(ids.size(), 1);

        TagFetchOptions fetchOptions = new TagFetchOptions();
        fetchOptions.withExperiments();
        fetchOptions.withSamples();
        fetchOptions.withDataSets();
        fetchOptions.withMaterials();

        Map<ITagId, Tag> map = v3api.getTags(sessionToken, ids, fetchOptions);

        assertEquals(map.size(), 1);

        return map.get(ids.get(0));
    }

}
