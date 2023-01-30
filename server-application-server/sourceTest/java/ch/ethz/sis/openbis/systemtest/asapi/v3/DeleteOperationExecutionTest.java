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
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionAvailability.DELETE_PENDING;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecution;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.SynchronousOperationExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.delete.OperationExecutionDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.fetchoptions.OperationExecutionFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.id.IOperationExecutionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.id.OperationExecutionPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.get.GetSpacesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;

import junit.framework.Assert;

/**
 * @author pkupczyk
 */
public class DeleteOperationExecutionTest extends AbstractOperationExecutionTest
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
    public void testDeleteWithOperationExecutionUnauthorized()
    {
        String sessionTokenAdmin = v3api.login(TEST_USER, PASSWORD);

        final SynchronousOperationExecutionOptions options = new SynchronousOperationExecutionOptions();
        options.setExecutionId(new OperationExecutionPermId());

        List<? extends IOperation> operations =
                Arrays.asList(new GetSpacesOperation(Arrays.asList(new SpacePermId("CISD")), new SpaceFetchOptions()));

        v3api.executeOperations(sessionTokenAdmin, operations, options);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionTokenUser = v3api.login(TEST_SPACE_USER, PASSWORD);

                    OperationExecutionDeletionOptions deletionOptions = new OperationExecutionDeletionOptions();
                    deletionOptions.setReason("test reason");

                    v3api.deleteOperationExecutions(sessionTokenUser, Arrays.asList(options.getExecutionId()), deletionOptions);
                }
            }, options.getExecutionId());
    }

    @Test
    public void testDeleteWithOperationExecutionUnauthorizedInstanceObserver()
    {
        String sessionTokenAdmin = v3api.login(TEST_USER, PASSWORD);

        final SynchronousOperationExecutionOptions options = new SynchronousOperationExecutionOptions();
        options.setExecutionId(new OperationExecutionPermId());

        List<? extends IOperation> operations =
                Arrays.asList(new GetSpacesOperation(Arrays.asList(new SpacePermId("CISD")), new SpaceFetchOptions()));

        v3api.executeOperations(sessionTokenAdmin, operations, options);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionTokenUser = v3api.login(TEST_INSTANCE_OBSERVER, PASSWORD);

                    OperationExecutionDeletionOptions deletionOptions = new OperationExecutionDeletionOptions();
                    deletionOptions.setReason("test reason");

                    v3api.deleteOperationExecutions(sessionTokenUser, Arrays.asList(options.getExecutionId()), deletionOptions);
                }
            }, options.getExecutionId());
    }

    @Test
    public void testDeleteWithOperationExecutionNonexistent()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final IOperationExecutionId executionId = new OperationExecutionPermId("IDONTEXIST");
        final OperationExecutionDeletionOptions deletionOptions = new OperationExecutionDeletionOptions();
        deletionOptions.setReason("test reason");

        // should not fail

        v3api.deleteOperationExecutions(sessionToken, Arrays.asList(executionId), deletionOptions);
    }

    @Test
    public void testDelete()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SynchronousOperationExecutionOptions options = new SynchronousOperationExecutionOptions();
        options.setExecutionId(new OperationExecutionPermId());

        List<? extends IOperation> operations =
                Arrays.asList(new GetSpacesOperation(Arrays.asList(new SpacePermId("CISD")), new SpaceFetchOptions()));

        // execute

        v3api.executeOperations(sessionToken, operations, options);

        // everything is available

        OperationExecution execution = getExecution(sessionToken, options.getExecutionId(), new OperationExecutionFetchOptions());
        assertAvailabilities(execution, AVAILABLE, defaultAvalability(), AVAILABLE, defaultSummaryAvalability(), AVAILABLE,
                defaultDetailsAvalability());

        OperationExecutionDeletionOptions deletionOptions = new OperationExecutionDeletionOptions();
        deletionOptions.setReason("test reason");

        // request a deletion

        v3api.deleteOperationExecutions(sessionToken, Arrays.asList(options.getExecutionId()), deletionOptions);

        // availability should change to DELETE_PENDING

        execution = getExecution(sessionToken, options.getExecutionId(), new OperationExecutionFetchOptions());
        assertAvailabilities(execution, DELETE_PENDING, defaultAvalability(), AVAILABLE, defaultSummaryAvalability(), AVAILABLE,
                defaultDetailsAvalability());

        getMarkTimedOutOrDeletedMaintenancePlugin().execute();

        // execution should be deleted once the maintenance task runs

        execution = getExecution(sessionToken, options.getExecutionId(), new OperationExecutionFetchOptions());
        Assert.assertNull(execution);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        OperationExecutionDeletionOptions o = new OperationExecutionDeletionOptions();
        o.setReason("test-reason");

        v3api.deleteOperationExecutions(sessionToken,
                Arrays.asList(new OperationExecutionPermId("TEST-LOGGING-1"), new OperationExecutionPermId("TEST-LOGGING-2")), o);

        assertAccessLog(
                "delete-operation-executions  EXECUTION_IDS('[TEST-LOGGING-1, TEST-LOGGING-2]') DELETION_OPTIONS('OperationExecutionDeletionOptions[reason=test-reason]')");
    }

}
