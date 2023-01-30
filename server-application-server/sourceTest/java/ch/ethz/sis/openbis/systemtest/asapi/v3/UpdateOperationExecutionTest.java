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

import static ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionAvailability.AVAILABLE;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionAvailability.DELETED;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionAvailability.DELETE_PENDING;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecution;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.SynchronousOperationExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.SynchronousOperationExecutionResults;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.fetchoptions.OperationExecutionFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.id.IOperationExecutionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.id.OperationExecutionPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.update.OperationExecutionUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.get.GetSpacesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.get.GetSpacesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import junit.framework.Assert;

/**
 * @author pkupczyk
 */
public class UpdateOperationExecutionTest extends AbstractOperationExecutionTest
{

    @Override
    @BeforeClass(alwaysRun = true)
    public void beforeClass()
    {
        super.beforeClass();
        getMarkTimeOutPendingMaintenancePlugin().shutdown();
        getMarkTimedOutOrDeletedMaintenancePlugin().shutdown();
    }

    @Override
    @AfterClass(alwaysRun = true)
    public void afterClass()
    {
        super.afterClass();
        getMarkTimeOutPendingMaintenancePlugin().start();
        getMarkTimedOutOrDeletedMaintenancePlugin().start();
    }

    @Test
    public void testUpdateWithOperationExecutionUnauthorized()
    {
        String sessionTokenAdmin = v3api.login(TEST_USER, PASSWORD);

        final SynchronousOperationExecutionOptions options = new SynchronousOperationExecutionOptions();
        options.setExecutionId(new OperationExecutionPermId());

        List<? extends IOperation> operations =
                Arrays.asList(new GetSpacesOperation(Arrays.asList(new SpacePermId("CISD")), new SpaceFetchOptions()));

        SynchronousOperationExecutionResults results =
                (SynchronousOperationExecutionResults) v3api.executeOperations(sessionTokenAdmin, operations, options);

        Assert.assertEquals(1, results.getResults().size());
        GetSpacesOperationResult result = (GetSpacesOperationResult) results.getResults().get(0);

        Map<ISpaceId, Space> spaceMap = result.getObjectMap();
        Assert.assertEquals(1, spaceMap.size());

        Space space = spaceMap.get(new SpacePermId("CISD"));
        Assert.assertEquals("CISD", space.getCode());

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionTokenUser = v3api.login(TEST_SPACE_USER, PASSWORD);

                    OperationExecutionUpdate update = new OperationExecutionUpdate();
                    update.setExecutionId(options.getExecutionId());

                    v3api.updateOperationExecutions(sessionTokenUser, Arrays.asList(update));
                }
            }, options.getExecutionId());
    }

    @Test
    public void testUpdateWithOperationExecutionNonexistent()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final IOperationExecutionId executionId = new OperationExecutionPermId("IDONTEXIST");
        final OperationExecutionUpdate update = new OperationExecutionUpdate();
        update.setExecutionId(executionId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateOperationExecutions(sessionToken, Arrays.asList(update));
                }
            }, executionId);
    }

    @Test
    public void testUpdateWithDeleteSummary()
    {
        testUpdateWithDeletePart(true, false);
    }

    @Test
    public void testUpdateWithDeleteDetails()
    {
        testUpdateWithDeletePart(false, true);
    }

    @Test
    public void testUpdateWithDeleteSummaryAndDetails()
    {
        testUpdateWithDeletePart(true, true);
    }

    private void testUpdateWithDeletePart(boolean deleteSummary, boolean deleteDetails)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SynchronousOperationExecutionOptions options = new SynchronousOperationExecutionOptions();
        options.setExecutionId(new OperationExecutionPermId());

        List<? extends IOperation> operations =
                Arrays.asList(new GetSpacesOperation(Arrays.asList(new SpacePermId("CISD")), new SpaceFetchOptions()));

        // execute

        v3api.executeOperations(sessionToken, operations, options);

        OperationExecutionFetchOptions fo = new OperationExecutionFetchOptions();
        fo.withSummary().withOperations();
        fo.withDetails().withOperations();

        // everything is available

        OperationExecution execution = getExecution(sessionToken, options.getExecutionId(), fo);
        Assert.assertEquals(1, execution.getSummary().getOperations().size());
        Assert.assertEquals(1, execution.getDetails().getOperations().size());
        assertAvailabilities(execution, AVAILABLE, defaultAvalability(), AVAILABLE, defaultSummaryAvalability(), AVAILABLE,
                defaultDetailsAvalability());

        OperationExecutionUpdate update = new OperationExecutionUpdate();
        update.setExecutionId(options.getExecutionId());

        if (deleteSummary)
        {
            update.deleteSummary();
        }
        if (deleteDetails)
        {
            update.deleteDetails();
        }

        // request a deletion

        v3api.updateOperationExecutions(sessionToken, Arrays.asList(update));

        // availability should change to DELETE_PENDING

        execution = getExecution(sessionToken, options.getExecutionId(), fo);
        Assert.assertEquals(1, execution.getSummary().getOperations().size());
        Assert.assertEquals(1, execution.getDetails().getOperations().size());
        assertAvailabilities(execution, AVAILABLE, defaultAvalability(), deleteSummary ? DELETE_PENDING : AVAILABLE, defaultSummaryAvalability(),
                deleteDetails ? DELETE_PENDING : AVAILABLE, defaultDetailsAvalability());

        getMarkTimedOutOrDeletedMaintenancePlugin().execute();

        // availability should change to DELETED once the maintenance task runs

        execution = getExecution(sessionToken, options.getExecutionId(), fo);

        if (deleteSummary)
        {
            Assert.assertNull(execution.getSummary());
        } else
        {
            Assert.assertEquals(1, execution.getSummary().getOperations().size());
        }

        if (deleteDetails)
        {
            Assert.assertNull(execution.getDetails());
        } else
        {
            Assert.assertEquals(1, execution.getDetails().getOperations().size());
        }

        assertAvailabilities(execution, AVAILABLE, defaultAvalability(), deleteSummary ? DELETED : AVAILABLE, defaultSummaryAvalability(),
                deleteDetails ? DELETED : AVAILABLE, defaultDetailsAvalability());

        // request a deletion again

        v3api.updateOperationExecutions(sessionToken, Arrays.asList(update));

        // nothing should change

        execution = getExecution(sessionToken, options.getExecutionId(), fo);

        if (deleteSummary)
        {
            Assert.assertNull(execution.getSummary());
        } else
        {
            Assert.assertEquals(1, execution.getSummary().getOperations().size());
        }

        if (deleteDetails)
        {
            Assert.assertNull(execution.getDetails());
        } else
        {
            Assert.assertEquals(1, execution.getDetails().getOperations().size());
        }

        assertAvailabilities(execution, AVAILABLE, defaultAvalability(), deleteSummary ? DELETED : AVAILABLE, defaultSummaryAvalability(),
                deleteDetails ? DELETED : AVAILABLE, defaultDetailsAvalability());
    }

    @Test
    public void testUpdateWithDescription()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SynchronousOperationExecutionOptions options = new SynchronousOperationExecutionOptions();
        options.setDescription("initial description");
        options.setExecutionId(new OperationExecutionPermId());

        List<? extends IOperation> operations =
                Arrays.asList(new GetSpacesOperation(Arrays.asList(new SpacePermId("CISD")), new SpaceFetchOptions()));

        v3api.executeOperations(sessionToken, operations, options);

        OperationExecution executionBefore = getExecution(sessionToken, options.getExecutionId(), new OperationExecutionFetchOptions());
        Assert.assertEquals(options.getDescription(), executionBefore.getDescription());

        OperationExecutionUpdate update = new OperationExecutionUpdate();
        update.setExecutionId(options.getExecutionId());
        update.setDescription("updated description");

        v3api.updateOperationExecutions(sessionToken, Arrays.asList(update));

        OperationExecution executionAfter = getExecution(sessionToken, options.getExecutionId(), new OperationExecutionFetchOptions());
        Assert.assertEquals(update.getDescription().getValue(), executionAfter.getDescription());
    }

    @Test
    public void testUpdateWithDescriptionWithInstanceObserver()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SynchronousOperationExecutionOptions options = new SynchronousOperationExecutionOptions();
        options.setDescription("initial description");
        options.setExecutionId(new OperationExecutionPermId());

        List<? extends IOperation> operations =
                Arrays.asList(new GetSpacesOperation(Arrays.asList(new SpacePermId("CISD")), new SpaceFetchOptions()));

        v3api.executeOperations(sessionToken, operations, options);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionTokenInstance = v3api.login(TEST_INSTANCE_OBSERVER, PASSWORD);
                    OperationExecution executionBefore =
                            getExecution(sessionTokenInstance, options.getExecutionId(), new OperationExecutionFetchOptions());
                    Assert.assertEquals(options.getDescription(), executionBefore.getDescription());

                    OperationExecutionUpdate update = new OperationExecutionUpdate();
                    update.setExecutionId(options.getExecutionId());
                    update.setDescription("updated description");

                    v3api.updateOperationExecutions(sessionTokenInstance, Arrays.asList(update));

                    OperationExecution executionAfter =
                            getExecution(sessionTokenInstance, options.getExecutionId(), new OperationExecutionFetchOptions());
                    Assert.assertEquals(update.getDescription().getValue(), executionAfter.getDescription());
                }
            }, options.getExecutionId());
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SynchronousOperationExecutionOptions options = new SynchronousOperationExecutionOptions();
        options.setExecutionId(new OperationExecutionPermId("test-logging-execution"));

        List<? extends IOperation> operations =
                Arrays.asList(new GetSpacesOperation(Arrays.asList(new SpacePermId("CISD")), new SpaceFetchOptions()));

        v3api.executeOperations(sessionToken, operations, options);

        OperationExecutionUpdate update = new OperationExecutionUpdate();
        update.setExecutionId(options.getExecutionId());

        v3api.updateOperationExecutions(sessionToken, Arrays.asList(update));

        assertAccessLog(
                "update-operation-executions  OPERATION_EXECUTION_UPDATES('[OperationExecutionUpdate[executionId=test-logging-execution]]')");
    }

}
