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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;
import junit.framework.Assert;

/**
 * @author pkupczyk
 */
public class DeleteExperimentTest extends AbstractDeletionTest
{

    @Test
    public void testDeleteEmptyList()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentDeletionOptions options = new ExperimentDeletionOptions();
        options.setReason("It is just a test");

        IDeletionId deletionId = v3api.deleteExperiments(sessionToken, new ArrayList<ExperimentPermId>(), options);
        Assert.assertNull(deletionId);
    }

    @Test
    public void testDeleteWithIndexCheck() throws Exception
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId permId = createCisdExperiment(new ProjectIdentifier("/CISD/DEFAULT"), java.util.UUID.randomUUID().toString().toUpperCase());
        TestTransaction.flagForCommit();
        TestTransaction.end();

        ExperimentDeletionOptions options = new ExperimentDeletionOptions();
        options.setReason("It is just a test");

        TestTransaction.start();
        List<ExperimentPE> experiments = daoFactory.getExperimentDAO().listByPermID(Arrays.asList(permId.getPermId()));
        assertEquals(experiments.size(), 1);

        v3api.deleteExperiments(sessionToken, Collections.singletonList(permId), options);
        TestTransaction.flagForCommit();
        TestTransaction.end();
        assertExperimentsRemoved(experiments.get(0).getId());
    }

    @Test
    public void testDeleteExperiment()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId permId = createCisdExperiment();

        ExperimentDeletionOptions options = new ExperimentDeletionOptions();
        options.setReason("It is just a test");

        assertExperimentExists(permId);

        IDeletionId deletionId = v3api.deleteExperiments(sessionToken, Collections.singletonList(permId), options);
        Assert.assertNotNull(deletionId);

        assertExperimentDoesNotExist(permId);
    }

    @Test
    public void testDeleteExperimentWithSample()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId experimentPermId = createCisdExperiment();
        SamplePermId samplePermId = createCisdSample(experimentPermId);

        ExperimentDeletionOptions options = new ExperimentDeletionOptions();
        options.setReason("It is just a test");

        assertExperimentExists(experimentPermId);
        assertSampleExists(samplePermId);

        IDeletionId deletionId = v3api.deleteExperiments(sessionToken, Collections.singletonList(experimentPermId), options);
        Assert.assertNotNull(deletionId);

        assertExperimentDoesNotExist(experimentPermId);
        assertSampleDoesNotExist(samplePermId);
    }

    @Test
    public void testDeleteExperimentWithDataSet()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId experimentPermId = new ExperimentPermId("200902091255058-1035");
        DataSetPermId dataSetPermId1 = new DataSetPermId("20081105092159333-3");
        DataSetPermId dataSetPermId2 = new DataSetPermId("20110805092359990-17");

        ExperimentDeletionOptions options = new ExperimentDeletionOptions();
        options.setReason("It is just a test");

        assertExperimentExists(experimentPermId);
        assertDataSetExists(dataSetPermId1);
        assertDataSetExists(dataSetPermId2);

        IDeletionId deletionId = v3api.deleteExperiments(sessionToken, Collections.singletonList(experimentPermId), options);
        Assert.assertNotNull(deletionId);

        assertExperimentDoesNotExist(experimentPermId);
        assertDataSetDoesNotExist(dataSetPermId1);
        assertDataSetDoesNotExist(dataSetPermId2);
    }

    @Test
    public void testDeleteExperimentWithUnauthorizedExperiment()
    {
        final ExperimentPermId permId = createCisdExperiment();

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

                    ExperimentDeletionOptions options = new ExperimentDeletionOptions();
                    options.setReason("It is just a test");

                    v3api.deleteExperiments(sessionToken, Collections.singletonList(permId), options);
                }
            }, permId);
    }

    @Test
    public void testDeleteWithAdminUserInAnotherSpace()
    {
        final ExperimentPermId permId = new ExperimentPermId("200902091255058-1037");

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(TEST_ROLE_V3, PASSWORD);

                    ExperimentDeletionOptions options = new ExperimentDeletionOptions();
                    options.setReason("It is just a test");

                    v3api.deleteExperiments(sessionToken, Collections.singletonList(permId), options);
                }
            }, permId);
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testDeleteWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String testSessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_TO_DELETE");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT"));
        creation.setProperty("DESCRIPTION", "a description");

        List<ExperimentPermId> permIds = v3api.createExperiments(testSessionToken, Collections.singletonList(creation));

        String sessionToken = v3api.login(user.getUserId(), PASSWORD);

        ExperimentDeletionOptions options = new ExperimentDeletionOptions();
        options.setReason("It is just a test");

        assertExperimentExists(permIds.get(0));

        if (user.isDisabledProjectUser())
        {
            assertAuthorizationFailureException(new IDelegatedAction()
                {
                    @Override
                    public void execute()
                    {
                        v3api.deleteExperiments(sessionToken, permIds, options);
                    }
                });
        } else if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            IDeletionId deletionId = v3api.deleteExperiments(sessionToken, permIds, options);
            Assert.assertNotNull(deletionId);
            assertExperimentDoesNotExist(permIds.get(0));
        } else
        {
            assertUnauthorizedObjectAccessException(new IDelegatedAction()
                {
                    @Override
                    public void execute()
                    {
                        v3api.deleteExperiments(sessionToken, permIds, options);
                    }
                }, permIds.get(0));
        }
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentDeletionOptions o = new ExperimentDeletionOptions();
        o.setReason("test-reason");

        v3api.deleteExperiments(sessionToken, Arrays.asList(new ExperimentPermId("TEST-LOGGING-1"), new ExperimentPermId("TEST-LOGGING-2")), o);

        assertAccessLog(
                "delete-experiments  EXPERIMENT_IDS('[TEST-LOGGING-1, TEST-LOGGING-2]') DELETION_OPTIONS('ExperimentDeletionOptions[reason=test-reason]')");
    }

    @Test
    public void testDeleteSampleOfASampleProperty()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);
        EntityTypePermId experimentType = createAnExperimentType(sessionToken, true, propertyType);
        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_SAMPLE_PROPERTY");
        creation.setTypeId(experimentType);
        creation.setProjectId(new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT"));
        SamplePermId propertySamplePermId = createCisdSample(createCisdExperiment());
        creation.setProperty(propertyType.getPermId(), propertySamplePermId.getPermId());
        ExperimentPermId experimentPermId = v3api.createExperiments(sessionToken, Arrays.asList(creation)).get(0);
        SampleDeletionOptions deletionOptions = new SampleDeletionOptions();
        deletionOptions.setReason("a test");

        // When
        IDeletionId deletionId = v3api.deleteSamples(sessionToken, Arrays.asList(propertySamplePermId), deletionOptions);

        // Then
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Experiment experiment = v3api.getExperiments(sessionToken, Arrays.asList(experimentPermId), fetchOptions).get(experimentPermId);
        assertEquals(experiment.getSampleProperties().toString(), "{}");
        assertEquals(experiment.getProperties().toString(), "{}");

        v3api.confirmDeletions(sessionToken, Arrays.asList(deletionId));
        experiment = v3api.getExperiments(sessionToken, Arrays.asList(experimentPermId), fetchOptions).get(experimentPermId);
        assertEquals(experiment.getSampleProperties().toString(), "{}");
        assertEquals(experiment.getProperties().toString(), "{}");
    }

    @Test
    public void testDeleteSampleWithSampleProperty()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);
        EntityTypePermId experimentType = createAnExperimentType(sessionToken, true, propertyType);
        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_SAMPLE_PROPERTY");
        creation.setTypeId(experimentType);
        creation.setProjectId(new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT"));
        SamplePermId propertySamplePermId = createCisdSample(createCisdExperiment());
        creation.setProperty(propertyType.getPermId(), propertySamplePermId.getPermId());
        ExperimentPermId experimentPermId = v3api.createExperiments(sessionToken, Arrays.asList(creation)).get(0);
        ExperimentDeletionOptions deletionOptions = new ExperimentDeletionOptions();
        deletionOptions.setReason("a test");

        // When
        IDeletionId deletionId = v3api.deleteExperiments(sessionToken, Arrays.asList(experimentPermId), deletionOptions);

        // Then
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        assertEquals(v3api.getExperiments(sessionToken, Arrays.asList(experimentPermId), fetchOptions).toString(), "{}");
        v3api.confirmDeletions(sessionToken, Arrays.asList(deletionId));
        assertEquals(v3api.getExperiments(sessionToken, Arrays.asList(experimentPermId), fetchOptions).toString(), "{}");
    }

}
