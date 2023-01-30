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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.test.context.transaction.TestTransaction;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;

import junit.framework.Assert;

/**
 * @author pkupczyk
 */
public class DeleteSampleTest extends AbstractDeletionTest
{

    @Test
    public void testDeleteEmptyList()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleDeletionOptions options = new SampleDeletionOptions();
        options.setReason("It is just a test");

        IDeletionId deletionId = v3api.deleteSamples(sessionToken, new ArrayList<SamplePermId>(), options);
        Assert.assertNull(deletionId);
    }

    @Test
    public void testDeleteSharedSampleWithHomelessPowerUser()
    {
        final SamplePermId permId = new SamplePermId("200811050947161-653");

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(TEST_NO_HOME_SPACE, PASSWORD);

                    SampleDeletionOptions options = new SampleDeletionOptions();
                    options.setReason("It is just a test");

                    v3api.deleteSamples(sessionToken, Collections.singletonList(permId), options);
                }
            }, permId);
    }

    @Test
    public void testDeleteSampleWithAdminUserInAnotherSpace()
    {
        final SamplePermId permId = new SamplePermId("200902091250077-1060");

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(TEST_ROLE_V3, PASSWORD);

                    SampleDeletionOptions options = new SampleDeletionOptions();
                    options.setReason("It is just a test");

                    v3api.deleteSamples(sessionToken, Collections.singletonList(permId), options);
                }
            }, permId);
    }

    @Test
    public void testDeleteWithIndexCheck() throws Exception
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentPermId experimentPermId =
                createCisdExperiment(new ProjectIdentifier("/CISD/DEFAULT"), java.util.UUID.randomUUID().toString().toUpperCase());
        SamplePermId samplePermId = createCisdSample(experimentPermId, java.util.UUID.randomUUID().toString().toUpperCase());
        TestTransaction.flagForCommit();
        TestTransaction.end();

        SampleDeletionOptions options = new SampleDeletionOptions();
        options.setReason("It is just a test");

        TestTransaction.start();
        List<SamplePE> samples = daoFactory.getSampleDAO().listByPermID(Arrays.asList(samplePermId.getPermId()));
        assertEquals(samples.size(), 1);

        v3api.deleteSamples(sessionToken, Collections.singletonList(samplePermId), options);
        TestTransaction.flagForCommit();
        TestTransaction.end();
        assertSamplesRemoved(samples.get(0).getId());

        TestTransaction.start();
        ExperimentDeletionOptions experimentDeletionOptions = new ExperimentDeletionOptions();
        experimentDeletionOptions.setReason("It is just a test");
        v3api.deleteExperiments(sessionToken, Collections.singletonList(experimentPermId), experimentDeletionOptions);
        TestTransaction.flagForCommit();
        TestTransaction.end();
    }

    @Test
    public void testDeleteSample()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId experimentPermId = createCisdExperiment();
        SamplePermId samplePermId = createCisdSample(experimentPermId);

        SampleDeletionOptions options = new SampleDeletionOptions();
        options.setReason("It is just a test");

        assertExperimentExists(experimentPermId);
        assertSampleExists(samplePermId);

        IDeletionId deletionId = v3api.deleteSamples(sessionToken, Collections.singletonList(samplePermId), options);
        Assert.assertNotNull(deletionId);

        assertExperimentExists(experimentPermId);
        assertSampleDoesNotExist(samplePermId);
    }

    @Test
    public void testDeleteSampleWithDataSet()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SamplePermId samplePermId = new SamplePermId("200902091225616-1027");
        DataSetPermId dataSetPermId1 = new DataSetPermId("20081105092159333-3");
        DataSetPermId dataSetPermId2 = new DataSetPermId("20110805092359990-17");

        SampleDeletionOptions options = new SampleDeletionOptions();
        options.setReason("It is just a test");

        assertSampleExists(samplePermId);
        assertDataSetExists(dataSetPermId1);
        assertDataSetExists(dataSetPermId2);

        IDeletionId deletionId = v3api.deleteSamples(sessionToken, Collections.singletonList(samplePermId), options);
        Assert.assertNotNull(deletionId);

        assertSampleDoesNotExist(samplePermId);
        assertDataSetDoesNotExist(dataSetPermId1);
        assertDataSetDoesNotExist(dataSetPermId2);
    }

    @Test
    public void testDeleteSampleWithComponentsSamples()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SamplePermId samplePermId = new SamplePermId("200811050919915-8");
        SamplePermId componentPermId1 = new SamplePermId("200811050919915-9");
        SamplePermId componentPermId2 = new SamplePermId("200811050919915-10");

        SampleDeletionOptions options = new SampleDeletionOptions();
        options.setReason("It is just a test");

        assertSampleExists(samplePermId);
        assertSampleExists(componentPermId1);
        assertSampleExists(componentPermId2);

        IDeletionId deletionId = v3api.deleteSamples(sessionToken, Collections.singletonList(samplePermId), options);
        Assert.assertNotNull(deletionId);

        assertSampleDoesNotExist(samplePermId);
        assertSampleDoesNotExist(componentPermId1);
        assertSampleDoesNotExist(componentPermId2);
    }

    @Test
    public void testDeleteSampleWithUnauthorizedSample()
    {
        final SamplePermId permId = createCisdSample(null);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

                    SampleDeletionOptions options = new SampleDeletionOptions();
                    options.setReason("It is just a test");

                    v3api.deleteSamples(sessionToken, Collections.singletonList(permId), options);
                }
            }, permId);
    }

    @Test
    public void testDeleteSampleOfASampleProperty()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);
        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType);
        SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setCode("SAMPLE_WITH_SAMPLE_PROPERTY");
        sampleCreation.setTypeId(sampleType);
        sampleCreation.setSpaceId(new SpacePermId("TEST-SPACE"));
        SamplePermId propertySamplePermId = createCisdSample(createCisdExperiment());
        sampleCreation.setProperty(propertyType.getPermId(), propertySamplePermId.getPermId());
        SamplePermId samplePermId = v3api.createSamples(sessionToken, Arrays.asList(sampleCreation)).get(0);
        SampleDeletionOptions deletionOptions = new SampleDeletionOptions();
        deletionOptions.setReason("a test");

        // When
        IDeletionId deletionId = v3api.deleteSamples(sessionToken, Arrays.asList(propertySamplePermId), deletionOptions);

        // Then
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Sample sample = v3api.getSamples(sessionToken, Arrays.asList(samplePermId), fetchOptions).get(samplePermId);
        assertEquals(sample.getSampleProperties().toString(), "{}");
        assertEquals(sample.getProperties().toString(), "{}");

        v3api.confirmDeletions(sessionToken, Arrays.asList(deletionId));
        sample = v3api.getSamples(sessionToken, Arrays.asList(samplePermId), fetchOptions).get(samplePermId);
        assertEquals(sample.getSampleProperties().toString(), "{}");
        assertEquals(sample.getProperties().toString(), "{}");
    }

    @Test
    public void testDeleteSampleWithSampleProperty()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);
        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType);
        SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setCode("SAMPLE_WITH_SAMPLE_PROPERTY");
        sampleCreation.setTypeId(sampleType);
        sampleCreation.setSpaceId(new SpacePermId("TEST-SPACE"));
        SamplePermId propertySamplePermId = createCisdSample(createCisdExperiment());
        sampleCreation.setProperty(propertyType.getPermId(), propertySamplePermId.getPermId());
        SamplePermId samplePermId = v3api.createSamples(sessionToken, Arrays.asList(sampleCreation)).get(0);
        SampleDeletionOptions deletionOptions = new SampleDeletionOptions();
        deletionOptions.setReason("a test");

        // When
        IDeletionId deletionId = v3api.deleteSamples(sessionToken, Arrays.asList(samplePermId), deletionOptions);

        // Then
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        assertEquals(v3api.getSamples(sessionToken, Arrays.asList(samplePermId), fetchOptions).toString(), "{}");
        v3api.confirmDeletions(sessionToken, Arrays.asList(deletionId));
        assertEquals(v3api.getSamples(sessionToken, Arrays.asList(samplePermId), fetchOptions).toString(), "{}");
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testDeleteWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String sessionToken = v3api.login(user.getUserId(), PASSWORD);

        ISampleId sampleId = new SampleIdentifier("/TEST-SPACE/TEST-PROJECT/EV-TEST");

        SampleDeletionOptions options = new SampleDeletionOptions();
        options.setReason("It is just a test");

        if (user.isDisabledProjectUser())
        {
            assertAuthorizationFailureException(new IDelegatedAction()
                {
                    @Override
                    public void execute()
                    {
                        v3api.deleteSamples(sessionToken, Arrays.asList(sampleId), options);
                    }
                });
        } else if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            v3api.deleteSamples(sessionToken, Arrays.asList(sampleId), options);
        } else
        {
            assertUnauthorizedObjectAccessException(new IDelegatedAction()
                {
                    @Override
                    public void execute()
                    {
                        v3api.deleteSamples(sessionToken, Arrays.asList(sampleId), options);
                    }
                }, sampleId);
        }
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleDeletionOptions o = new SampleDeletionOptions();
        o.setReason("test-reason");

        v3api.deleteSamples(sessionToken, Arrays.asList(new SamplePermId("TEST-LOGGING-1"), new SamplePermId("TEST-LOGGING-2")), o);

        assertAccessLog(
                "delete-samples  SAMPLE_IDS('[TEST-LOGGING-1, TEST-LOGGING-2]') DELETION_OPTIONS('SampleDeletionOptions[reason=test-reason]')");
    }

}
