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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.PhysicalDataCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DataSetDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.FileFormatTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.ProprietaryStorageFormatPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.RelativeLocationLocatorTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.DataStorePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.confirm.ConfirmDeletionsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.DeletionTechId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.SynchronousOperationExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author pkupczyk
 */
public class ConfirmDeletionTest extends AbstractDeletionTest
{

    @Test
    public void testConfirmDeletionOfDataSetOfTypeWithDisallowDeletionFalse()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetTypeCreation typeCreation = new DataSetTypeCreation();
        typeCreation.setCode("TYPE_WITH_DELETION_ALLOWED");
        v3api.createDataSetTypes(sessionToken, Arrays.asList(typeCreation));

        DataSetCreation dataSetCreation = dataSetCreation(typeCreation.getCode(), "DATA_SET_WITH_DELETION_ALLOWED");
        List<DataSetPermId> dataSetIds = v3api.createDataSets(sessionToken, Arrays.asList(dataSetCreation));
        DataSetPermId dataSetId = dataSetIds.get(0);

        assertDataSetExists(dataSetId);

        DataSetDeletionOptions options = new DataSetDeletionOptions();
        options.setReason("testing");

        IDeletionId deletionId = v3api.deleteDataSets(sessionToken, Collections.singletonList(dataSetId), options);

        assertDeletionExists(deletionId);
        assertDataSetDoesNotExist(dataSetId);

        v3api.confirmDeletions(sessionToken, Collections.singletonList(deletionId));

        assertDeletionDoesNotExist(deletionId);
        assertDataSetDoesNotExist(dataSetId);
    }

    @Test
    public void testConfirmDeletionOfDataSetOfTypeWithDisallowDeletionTrueUnforced()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetTypeCreation typeCreation = new DataSetTypeCreation();
        typeCreation.setCode("TYPE_WITH_DELETION_DISALLOWED");
        v3api.createDataSetTypes(sessionToken, Arrays.asList(typeCreation));

        DataSetCreation dataSetCreation = dataSetCreation(typeCreation.getCode(), "DATA_SET_WITH_DELETION_DISALLOWED");
        List<DataSetPermId> dataSetIds = v3api.createDataSets(sessionToken, Arrays.asList(dataSetCreation));
        DataSetPermId dataSetId = dataSetIds.get(0);

        assertDataSetExists(dataSetId);

        DataSetDeletionOptions options = new DataSetDeletionOptions();
        options.setReason("testing");

        IDeletionId deletionId = v3api.deleteDataSets(sessionToken, Collections.singletonList(dataSetId), options);

        assertDeletionExists(deletionId);
        assertDataSetDoesNotExist(dataSetId);

        DataSetTypeUpdate dataSetTypeUpdate = new DataSetTypeUpdate();
        dataSetTypeUpdate.setTypeId(new EntityTypePermId("TYPE_WITH_DELETION_DISALLOWED"));
        dataSetTypeUpdate.setDisallowDeletion(true);
        v3api.updateDataSetTypes(sessionToken, Arrays.asList(dataSetTypeUpdate));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.confirmDeletions(sessionToken, Collections.singletonList(deletionId));
                }
            }, "Deletion failed because the following data sets have 'Disallow deletion' flag set to true in their type.");
    }

    @Test
    public void testConfirmDeletionOfDataSetOfTypeWithDisallowDeletionTrueForced()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetTypeCreation typeCreation = new DataSetTypeCreation();
        typeCreation.setCode("TYPE_WITH_DELETION_DISALLOWED");
        v3api.createDataSetTypes(sessionToken, Arrays.asList(typeCreation));

        DataSetCreation dataSetCreation = dataSetCreation(typeCreation.getCode(), "DATA_SET_WITH_DELETION_DISALLOWED");
        List<DataSetPermId> dataSetIds = v3api.createDataSets(sessionToken, Arrays.asList(dataSetCreation));
        DataSetPermId dataSetId = dataSetIds.get(0);

        assertDataSetExists(dataSetId);

        DataSetDeletionOptions options = new DataSetDeletionOptions();
        options.setReason("testing");

        IDeletionId deletionId = v3api.deleteDataSets(sessionToken, Collections.singletonList(dataSetId), options);

        assertDeletionExists(deletionId);
        assertDataSetDoesNotExist(dataSetId);

        DataSetTypeUpdate dataSetTypeUpdate = new DataSetTypeUpdate();
        dataSetTypeUpdate.setTypeId(new EntityTypePermId("TYPE_WITH_DELETION_DISALLOWED"));
        dataSetTypeUpdate.setDisallowDeletion(true);
        v3api.updateDataSetTypes(sessionToken, Arrays.asList(dataSetTypeUpdate));

        ConfirmDeletionsOperation confirmOperation = new ConfirmDeletionsOperation(Arrays.asList(deletionId));
        confirmOperation.setForceDeletion(true);

        v3api.executeOperations(sessionToken, Arrays.asList(confirmOperation), new SynchronousOperationExecutionOptions());

        assertDeletionDoesNotExist(deletionId);
        assertDataSetDoesNotExist(dataSetId);
    }

    @Test
    public void testConfirmDeletionOfExperimentWithSample()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId experimentId = createCisdExperiment();
        SamplePermId sampleId = createCisdSample(experimentId);

        ExperimentDeletionOptions deletionOptions = new ExperimentDeletionOptions();
        deletionOptions.setReason("It is just a test");

        assertExperimentExists(experimentId);
        assertSampleExists(sampleId);

        IDeletionId deletionId = v3api.deleteExperiments(sessionToken, Collections.singletonList(experimentId), deletionOptions);

        assertDeletionExists(deletionId);
        assertExperimentDoesNotExist(experimentId);
        assertSampleDoesNotExist(sampleId);

        v3api.confirmDeletions(sessionToken, Collections.singletonList(deletionId));

        assertDeletionDoesNotExist(deletionId);
        assertExperimentDoesNotExist(experimentId);
        assertSampleDoesNotExist(sampleId);
    }

    @Test
    public void testConfirmDeletionOfSampleWithDataSet()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SamplePermId sampleId = new SamplePermId("200902091225616-1027");
        DataSetPermId dataSetId1 = new DataSetPermId("20081105092159333-3");
        DataSetPermId dataSetId2 = new DataSetPermId("20110805092359990-17");

        SampleDeletionOptions deletionOptions = new SampleDeletionOptions();
        deletionOptions.setReason("It is just a test");

        assertSampleExists(sampleId);
        assertDataSetExists(dataSetId1);
        assertDataSetExists(dataSetId2);

        IDeletionId deletionId = v3api.deleteSamples(sessionToken, Collections.singletonList(sampleId), deletionOptions);

        assertDeletionExists(deletionId);
        assertSampleDoesNotExist(sampleId);
        assertDataSetDoesNotExist(dataSetId1);
        assertDataSetDoesNotExist(dataSetId2);

        v3api.confirmDeletions(sessionToken, Collections.singletonList(deletionId));

        assertDeletionDoesNotExist(deletionId);
        assertSampleDoesNotExist(sampleId);
        assertDataSetDoesNotExist(dataSetId1);
        assertDataSetDoesNotExist(dataSetId2);
    }

    @Test
    public void testConfirmDeletionWithNonexistentDeletion()
    {
        final IDeletionId deletionId = new DeletionTechId(-1L);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(TEST_USER, PASSWORD);
                    v3api.confirmDeletions(sessionToken, Collections.singletonList(deletionId));
                }
            }, deletionId);
    }

    @Test
    public void testConfirmDeletionWithUnauthorizedDeletion()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId experimentId = createCisdExperiment();

        ExperimentDeletionOptions deletionOptions = new ExperimentDeletionOptions();
        deletionOptions.setReason("It is just a test");
        final IDeletionId deletionId = v3api.deleteExperiments(sessionToken, Collections.singletonList(experimentId), deletionOptions);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken2 = v3api.login(TEST_SPACE_USER, PASSWORD);
                    v3api.confirmDeletions(sessionToken2, Collections.singletonList(deletionId));
                }
            }, deletionId);
    }

    @Test
    public void testConfirmDeletionWithDeletionIdsThatContainsNulls()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId experimentId = createCisdExperiment();

        ExperimentDeletionOptions deletionOptions = new ExperimentDeletionOptions();
        deletionOptions.setReason("It is just a test");

        assertExperimentExists(experimentId);

        IDeletionId deletionId = v3api.deleteExperiments(sessionToken, Arrays.asList(experimentId), deletionOptions);

        assertDeletionExists(deletionId);
        assertExperimentDoesNotExist(experimentId);

        // We do not want it to fail but rather ignore the nulls. Ignoring the nulls allows us to
        // pass the result of the deleteXXX method directly to confirmDeletions without any checks
        // (the deleteXXX methods return null when an object to be deleted does not exist, e.g. it had been already deleted)

        v3api.confirmDeletions(sessionToken, Arrays.asList(null, deletionId, null));

        assertDeletionDoesNotExist(deletionId);
        assertExperimentDoesNotExist(experimentId);
    }

    @Test
    public void testConfirmDeletionWithDeletionIdsNull()
    {
        assertUserFailureException(new IDelegatedAction()
            {

                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(TEST_USER, PASSWORD);
                    v3api.confirmDeletions(sessionToken, null);
                }
            }, "Deletion ids cannot be null");
    }

    @Test
    public void testConfirmDeletionWithAdminUserInAnotherSpace()
    {
        String sessionToken = v3api.login(TEST_POWER_USER_CISD, PASSWORD);

        ExperimentPermId experimentId = createCisdExperiment();

        ExperimentDeletionOptions deletionOptions = new ExperimentDeletionOptions();
        deletionOptions.setReason("It is just a test");
        final IDeletionId deletionId = v3api.deleteExperiments(sessionToken, Collections.singletonList(experimentId), deletionOptions);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken2 = v3api.login(TEST_OBSERVER_CISD, PASSWORD);
                    v3api.confirmDeletions(sessionToken2, Collections.singletonList(deletionId));
                }
            }, deletionId);
    }

    @Test
    public void testConfirmDeletionWithSameAdminUserInAnotherSpace()
    {
        String sessionToken = v3api.login(TEST_NO_HOME_SPACE, PASSWORD);

        ExperimentPermId experimentId = createCisdExperiment();

        ExperimentDeletionOptions deletionOptions = new ExperimentDeletionOptions();
        deletionOptions.setReason("It is just a test");
        final IDeletionId deletionId = v3api.deleteExperiments(sessionToken, Collections.singletonList(experimentId), deletionOptions);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken2 = v3api.login(TEST_NO_HOME_SPACE, PASSWORD);
                    v3api.confirmDeletions(sessionToken2, Collections.singletonList(deletionId));
                }
            }, deletionId);
    }

    @Test
    public void testLogging()
    {
        // given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentPermId experimentId = createCisdExperiment();
        ExperimentDeletionOptions deletionOptions = new ExperimentDeletionOptions();
        deletionOptions.setReason("It is just a test");
        assertExperimentExists(experimentId);
        IDeletionId deletionId = v3api.deleteExperiments(sessionToken, Collections.singletonList(experimentId), deletionOptions);
        assertDeletionExists(deletionId);
        // when
        v3api.confirmDeletions(sessionToken, Collections.singletonList(deletionId));
        // then
        assertAccessLog("confirm-deletions  DELETION_IDS('[" + deletionId + "]')");
    }


    private DataSetCreation dataSetCreation(String typeCode, String dataSetCode)
    {
        PhysicalDataCreation physicalCreation = new PhysicalDataCreation();
        physicalCreation.setLocation("a/b/c");
        physicalCreation.setFileFormatTypeId(new FileFormatTypePermId("TIFF"));
        physicalCreation.setLocatorTypeId(new RelativeLocationLocatorTypePermId());
        physicalCreation.setStorageFormatId(new ProprietaryStorageFormatPermId());

        DataSetCreation creation = new DataSetCreation();
        creation.setCode(dataSetCode);
        creation.setDataSetKind(DataSetKind.PHYSICAL);
        creation.setTypeId(new EntityTypePermId(typeCode));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));
        creation.setDataStoreId(new DataStorePermId("STANDARD"));
        creation.setPhysicalData(physicalCreation);

        return creation;
    }

}
