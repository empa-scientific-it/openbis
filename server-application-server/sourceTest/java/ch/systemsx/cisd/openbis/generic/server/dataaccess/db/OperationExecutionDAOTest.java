/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import static ch.systemsx.cisd.openbis.generic.shared.dto.OperationExecutionAvailability.AVAILABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.OperationExecutionAvailability.DELETED;
import static ch.systemsx.cisd.openbis.generic.shared.dto.OperationExecutionAvailability.DELETE_PENDING;
import static ch.systemsx.cisd.openbis.generic.shared.dto.OperationExecutionAvailability.TIMED_OUT;
import static ch.systemsx.cisd.openbis.generic.shared.dto.OperationExecutionAvailability.TIME_OUT_PENDING;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IOperationExecutionDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.OperationExecutionAvailability;
import ch.systemsx.cisd.openbis.generic.shared.dto.OperationExecutionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.OperationExecutionState;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * Test cases for {@link OperationExecutionDAO}.
 * 
 * @author pkupczyk
 */
@Test(groups = { "db", "script" })
public final class OperationExecutionDAOTest extends AbstractDAOTest
{

    private IOperationExecutionDAO executionDAO;

    private PersonPE testUser;

    @BeforeClass
    public void beforeClass()
    {
        executionDAO = daoFactory.getOperationExecutionDAO();
        testUser = daoFactory.getPersonDAO().tryFindPersonByUserId("test");
    }

    @Test
    public void testTryFindByCode()
    {
        OperationExecutionPE execution = executionDAO.tryFindByCode("testCode");
        assertNull(execution);

        createNew("testCode");

        execution = executionDAO.tryFindByCode("testCode");
        assertNotNull(execution);
    }

    @Test
    public void testTryFindByCodes()
    {
        List<OperationExecutionPE> executions = executionDAO.findByCodes(Arrays.asList("testCode1", "testCode2"));
        assertEquals(executions.size(), 0);

        createNew("testCode1");
        createNew("testCode2");
        createNew("testCode3");

        executions = executionDAO.findByCodes(Arrays.asList("testCode1", "testCode2"));
        assertEquals(executions.size(), 2);
        assertEquals(executions.get(0).getCode(), "testCode1");
        assertEquals(executions.get(1).getCode(), "testCode2");
    }

    @Test(expectedExceptions = DataIntegrityViolationException.class)
    public void testCreateRunningWithoutStartDate()
    {
        OperationExecutionPE execution = new OperationExecutionPE();
        execution.setCode("testCode");
        execution.setOwner(testUser);
        execution.setState(OperationExecutionState.RUNNING);
        execution.setStartDate(null);
        createOrUpdateAndReload(execution);
    }

    public void testCreateRunningWithStartDate()
    {
        OperationExecutionPE execution = new OperationExecutionPE();
        execution.setCode("testCode");
        execution.setOwner(testUser);
        execution.setState(OperationExecutionState.RUNNING);
        execution.setStartDate(new Date());
        createOrUpdateAndReload(execution);
    }

    @Test(expectedExceptions = DataIntegrityViolationException.class)
    public void testCreateNotRunningWithStartDate()
    {
        OperationExecutionPE execution = new OperationExecutionPE();
        execution.setCode("testCode");
        execution.setOwner(testUser);
        execution.setState(OperationExecutionState.NEW);
        execution.setStartDate(new Date());
        createOrUpdateAndReload(execution);
    }

    @Test(expectedExceptions = DataIntegrityViolationException.class)
    public void testCreateFinishedWithoutFinishDate()
    {
        OperationExecutionPE execution = new OperationExecutionPE();
        execution.setCode("testCode");
        execution.setOwner(testUser);
        execution.setState(OperationExecutionState.FINISHED);
        execution.setStartDate(new Date());
        execution.setFinishDate(null);
        createOrUpdateAndReload(execution);
    }

    @Test
    public void testCreateFinishedWithFinishDate()
    {
        OperationExecutionPE execution = new OperationExecutionPE();
        execution.setCode("testCode");
        execution.setOwner(testUser);
        execution.setState(OperationExecutionState.FINISHED);
        execution.setStartDate(new Date());
        execution.setFinishDate(new Date());
        createOrUpdateAndReload(execution);
    }

    @Test(expectedExceptions = DataIntegrityViolationException.class)
    public void testCreateNotFinishedWithFinishDate()
    {
        OperationExecutionPE execution = new OperationExecutionPE();
        execution.setCode("testCode");
        execution.setOwner(testUser);
        execution.setState(OperationExecutionState.RUNNING);
        execution.setStartDate(new Date());
        execution.setFinishDate(new Date());
        createOrUpdateAndReload(execution);
    }

    @Test
    public void testCreateAndUpdateAndFind()
    {
        final String CODE = "testCode";
        final String DESCRIPTION = "testDescription";
        final String ERROR = "testError";

        OperationExecutionPE execution = new OperationExecutionPE();
        execution.setCode(CODE);
        execution.setOwner(testUser);
        execution.setState(OperationExecutionState.NEW);
        execution.setDescription(DESCRIPTION);
        execution = createOrUpdateAndReload(execution);

        Assert.assertNotNull(execution.getId());
        Assert.assertEquals(execution.getCode(), CODE);
        Assert.assertEquals(execution.getState(), OperationExecutionState.NEW);
        Assert.assertEquals(execution.getDescription(), DESCRIPTION);
        Assert.assertNotNull(execution.getCreationDate());
        Assert.assertNull(execution.getSummaryError());
        Assert.assertNull(execution.getStartDate());
        Assert.assertNull(execution.getFinishDate());

        execution.setState(OperationExecutionState.RUNNING);
        execution.setStartDate(new Date());
        execution = createOrUpdateAndReload(execution);

        Assert.assertNotNull(execution.getId());
        Assert.assertEquals(execution.getCode(), CODE);
        Assert.assertEquals(execution.getState(), OperationExecutionState.RUNNING);
        Assert.assertEquals(execution.getDescription(), DESCRIPTION);
        Assert.assertNotNull(execution.getCreationDate());
        Assert.assertNotNull(execution.getStartDate());
        Assert.assertNull(execution.getSummaryError());
        Assert.assertNull(execution.getFinishDate());

        execution.setState(OperationExecutionState.FINISHED);
        execution.setFinishDate(new Date());
        execution = createOrUpdateAndReload(execution);

        Assert.assertNotNull(execution.getId());
        Assert.assertEquals(execution.getCode(), CODE);
        Assert.assertEquals(execution.getState(), OperationExecutionState.FINISHED);
        Assert.assertEquals(execution.getDescription(), DESCRIPTION);
        Assert.assertNotNull(execution.getCreationDate());
        Assert.assertNotNull(execution.getStartDate());
        Assert.assertNotNull(execution.getFinishDate());
        Assert.assertNull(execution.getSummaryError());

        execution.setState(OperationExecutionState.FAILED);
        execution.setSummaryError(ERROR);
        execution = createOrUpdateAndReload(execution);

        Assert.assertNotNull(execution.getId());
        Assert.assertEquals(execution.getCode(), CODE);
        Assert.assertEquals(execution.getState(), OperationExecutionState.FAILED);
        Assert.assertEquals(execution.getDescription(), DESCRIPTION);
        Assert.assertNotNull(execution.getCreationDate());
        Assert.assertNotNull(execution.getStartDate());
        Assert.assertNotNull(execution.getFinishDate());
        Assert.assertEquals(execution.getSummaryError(), ERROR);
    }

    @Test
    public void testGetExecutionsToBeTimeOutPending()
    {
        long now = System.currentTimeMillis();

        // new
        setAvailabilities(createNew(), AVAILABLE, 0);
        setAvailabilities(createNew(), DELETE_PENDING, 0);
        setAvailabilities(createNew(), DELETED, 0);
        setAvailabilities(createNew(), TIME_OUT_PENDING, 0);
        setAvailabilities(createNew(), TIMED_OUT, 0);

        // scheduled
        setAvailabilities(createScheduled(), AVAILABLE, 0);
        setAvailabilities(createScheduled(), DELETE_PENDING, 0);
        setAvailabilities(createScheduled(), DELETED, 0);
        setAvailabilities(createScheduled(), TIME_OUT_PENDING, 0);
        setAvailabilities(createScheduled(), TIMED_OUT, 0);

        // running
        setAvailabilities(createRunning(), AVAILABLE, 0);
        setAvailabilities(createRunning(), DELETE_PENDING, 0);
        setAvailabilities(createRunning(), DELETED, 0);
        setAvailabilities(createRunning(), TIME_OUT_PENDING, 0);
        setAvailabilities(createRunning(), TIMED_OUT, 0);

        // finished
        setAvailabilities(createFinished(now), AVAILABLE, DateUtils.MILLIS_PER_HOUR);
        setAvailabilities(createFinished(now), DELETE_PENDING, DateUtils.MILLIS_PER_HOUR);
        setAvailabilities(createFinished(now), DELETED, DateUtils.MILLIS_PER_HOUR);
        setAvailabilities(createFinished(now), TIME_OUT_PENDING, DateUtils.MILLIS_PER_HOUR);
        setAvailabilities(createFinished(now), TIMED_OUT, DateUtils.MILLIS_PER_HOUR);

        // finished expired
        OperationExecutionPE finished1 = setAvailabilities(createFinished(now - DateUtils.MILLIS_PER_SECOND), AVAILABLE, 1);
        OperationExecutionPE finished10 = setAvailabilities(createFinished(now - 10 * DateUtils.MILLIS_PER_SECOND), AVAILABLE, 1);
        setAvailabilities(createFinished(now - 1), DELETE_PENDING, 1);
        setAvailabilities(createFinished(now - 1), DELETED, 1);
        setAvailabilities(createFinished(now - 1), TIME_OUT_PENDING, 1);
        setAvailabilities(createFinished(now - 1), TIMED_OUT, 1);

        // failed
        setAvailabilities(createFailed(now), AVAILABLE, DateUtils.MILLIS_PER_HOUR);
        setAvailabilities(createFailed(now), DELETE_PENDING, DateUtils.MILLIS_PER_HOUR);
        setAvailabilities(createFailed(now), DELETED, DateUtils.MILLIS_PER_HOUR);
        setAvailabilities(createFailed(now), TIME_OUT_PENDING, DateUtils.MILLIS_PER_HOUR);
        setAvailabilities(createFailed(now), TIMED_OUT, DateUtils.MILLIS_PER_HOUR);

        // failed expired
        OperationExecutionPE failed2 = setAvailabilities(createFailed(now - 2 * DateUtils.MILLIS_PER_SECOND), AVAILABLE, 1);
        OperationExecutionPE failed20 = setAvailabilities(createFailed(now - 20 * DateUtils.MILLIS_PER_SECOND), AVAILABLE, 1);
        setAvailabilities(createFailed(now - 1), DELETE_PENDING, 1);
        setAvailabilities(createFailed(now - 1), DELETED, 1);
        setAvailabilities(createFailed(now - 1), TIME_OUT_PENDING, 1);
        setAvailabilities(createFailed(now - 1), TIMED_OUT, 1);

        List<OperationExecutionPE> executions = executionDAO.getExecutionsToBeTimeOutPending();
        assertEquals(executions.size(), 4);
        assertEquals(executions.get(0).getCode(), finished1.getCode());
        assertEquals(executions.get(1).getCode(), finished10.getCode());
        assertEquals(executions.get(2).getCode(), failed2.getCode());
        assertEquals(executions.get(3).getCode(), failed20.getCode());
    }

    @Test
    public void testGetExecutionsToBeTimedOut()
    {
        long now = System.currentTimeMillis();

        // new
        setAvailabilities(createNew(), AVAILABLE, 0);
        setAvailabilities(createNew(), DELETE_PENDING, 0);
        setAvailabilities(createNew(), DELETED, 0);
        setAvailabilities(createNew(), TIME_OUT_PENDING, 0);
        setAvailabilities(createNew(), TIMED_OUT, 0);

        // scheduled
        setAvailabilities(createScheduled(), AVAILABLE, 0);
        setAvailabilities(createScheduled(), DELETE_PENDING, 0);
        setAvailabilities(createScheduled(), DELETED, 0);
        setAvailabilities(createScheduled(), TIME_OUT_PENDING, 0);
        setAvailabilities(createScheduled(), TIMED_OUT, 0);

        // running
        setAvailabilities(createRunning(), AVAILABLE, 0);
        setAvailabilities(createRunning(), DELETE_PENDING, 0);
        setAvailabilities(createRunning(), DELETED, 0);
        setAvailabilities(createRunning(), TIME_OUT_PENDING, 0);
        setAvailabilities(createRunning(), TIMED_OUT, 0);

        // finished
        setAvailabilities(createFinished(now), AVAILABLE, DateUtils.MILLIS_PER_HOUR);
        setAvailabilities(createFinished(now), DELETE_PENDING, DateUtils.MILLIS_PER_HOUR);
        setAvailabilities(createFinished(now), DELETED, DateUtils.MILLIS_PER_HOUR);
        OperationExecutionPE finishedExecution1 = setAvailabilities(createFinished(now), TIME_OUT_PENDING, DateUtils.MILLIS_PER_HOUR);
        setAvailabilities(createFinished(now), TIMED_OUT, DateUtils.MILLIS_PER_HOUR);

        // finished expired
        setAvailabilities(createFinished(now - 1), AVAILABLE, 1);
        setAvailabilities(createFinished(now - 1), DELETE_PENDING, 1);
        setAvailabilities(createFinished(now - 1), DELETED, 1);
        OperationExecutionPE finishedExecution2 = setAvailabilities(createFinished(now - 1), TIME_OUT_PENDING, 1);
        setAvailabilities(createFinished(now - 1), TIMED_OUT, 1);

        // failed
        setAvailabilities(createFailed(now), AVAILABLE, DateUtils.MILLIS_PER_HOUR);
        setAvailabilities(createFailed(now), DELETE_PENDING, DateUtils.MILLIS_PER_HOUR);
        setAvailabilities(createFailed(now), DELETED, DateUtils.MILLIS_PER_HOUR);
        OperationExecutionPE failedExecution1 = setAvailabilities(createFailed(now), TIME_OUT_PENDING, DateUtils.MILLIS_PER_HOUR);
        setAvailabilities(createFailed(now), TIMED_OUT, DateUtils.MILLIS_PER_HOUR);

        // failed expired
        setAvailabilities(createFailed(now - 1), AVAILABLE, 1);
        setAvailabilities(createFailed(now - 1), DELETE_PENDING, 1);
        setAvailabilities(createFailed(now - 1), DELETED, 1);
        OperationExecutionPE failedExecution2 = setAvailabilities(createFailed(now - 1), TIME_OUT_PENDING, 1);
        setAvailabilities(createFailed(now - 1), TIMED_OUT, 1);

        List<OperationExecutionPE> executions = executionDAO.getExecutionsToBeTimedOut();
        assertEquals(executions.size(), 4);
        assertEquals(executions.get(0).getCode(), finishedExecution1.getCode());
        assertEquals(executions.get(1).getCode(), finishedExecution2.getCode());
        assertEquals(executions.get(2).getCode(), failedExecution1.getCode());
        assertEquals(executions.get(3).getCode(), failedExecution2.getCode());
    }

    @Test
    public void testGetExecutionsToBeDeleted()
    {
        long now = System.currentTimeMillis();

        // new
        setAvailabilities(createNew(), AVAILABLE, 0);
        setAvailabilities(createNew(), DELETE_PENDING, 0);
        setAvailabilities(createNew(), DELETED, 0);
        setAvailabilities(createNew(), TIME_OUT_PENDING, 0);
        setAvailabilities(createNew(), TIMED_OUT, 0);

        // scheduled
        setAvailabilities(createScheduled(), AVAILABLE, 0);
        setAvailabilities(createScheduled(), DELETE_PENDING, 0);
        setAvailabilities(createScheduled(), DELETED, 0);
        setAvailabilities(createScheduled(), TIME_OUT_PENDING, 0);
        setAvailabilities(createScheduled(), TIMED_OUT, 0);

        // running
        setAvailabilities(createRunning(), AVAILABLE, 0);
        setAvailabilities(createRunning(), DELETE_PENDING, 0);
        setAvailabilities(createRunning(), DELETED, 0);
        setAvailabilities(createRunning(), TIME_OUT_PENDING, 0);
        setAvailabilities(createRunning(), TIMED_OUT, 0);

        // finished
        setAvailabilities(createFinished(now), AVAILABLE, DateUtils.MILLIS_PER_HOUR);
        OperationExecutionPE finishedExecution1 = setAvailabilities(createFinished(now), DELETE_PENDING, DateUtils.MILLIS_PER_HOUR);
        setAvailabilities(createFinished(now), DELETED, DateUtils.MILLIS_PER_HOUR);
        setAvailabilities(createFinished(now), TIME_OUT_PENDING, DateUtils.MILLIS_PER_HOUR);
        setAvailabilities(createFinished(now), TIMED_OUT, DateUtils.MILLIS_PER_HOUR);

        // finished expired
        setAvailabilities(createFinished(now - 1), AVAILABLE, 1);
        OperationExecutionPE finishedExecution2 = setAvailabilities(createFinished(now - 1), DELETE_PENDING, 1);
        setAvailabilities(createFinished(now - 1), DELETED, 1);
        setAvailabilities(createFinished(now - 1), TIME_OUT_PENDING, 1);
        setAvailabilities(createFinished(now - 1), TIMED_OUT, 1);

        // failed
        setAvailabilities(createFailed(now), AVAILABLE, DateUtils.MILLIS_PER_HOUR);
        OperationExecutionPE failedExecution1 = setAvailabilities(createFailed(now), DELETE_PENDING, DateUtils.MILLIS_PER_HOUR);
        setAvailabilities(createFailed(now), DELETED, DateUtils.MILLIS_PER_HOUR);
        setAvailabilities(createFailed(now), TIME_OUT_PENDING, DateUtils.MILLIS_PER_HOUR);
        setAvailabilities(createFailed(now), TIMED_OUT, DateUtils.MILLIS_PER_HOUR);

        // failed expired
        setAvailabilities(createFailed(now - 1), AVAILABLE, 1);
        OperationExecutionPE failedExecution2 = setAvailabilities(createFailed(now - 1), DELETE_PENDING, 1);
        setAvailabilities(createFailed(now - 1), DELETED, 1);
        setAvailabilities(createFailed(now - 1), TIME_OUT_PENDING, 1);
        setAvailabilities(createFailed(now - 1), TIMED_OUT, 1);

        List<OperationExecutionPE> executions = executionDAO.getExecutionsToBeDeleted();
        assertEquals(executions.size(), 4);
        assertEquals(executions.get(0).getCode(), finishedExecution1.getCode());
        assertEquals(executions.get(1).getCode(), finishedExecution2.getCode());
        assertEquals(executions.get(2).getCode(), failedExecution1.getCode());
        assertEquals(executions.get(3).getCode(), failedExecution2.getCode());
    }

    private OperationExecutionPE createNew()
    {
        return createNew(UUID.randomUUID().toString());
    }

    private OperationExecutionPE createNew(String code)
    {
        OperationExecutionPE execution = new OperationExecutionPE();
        execution.setCode(code);
        execution.setOwner(testUser);
        execution.setState(OperationExecutionState.NEW);
        return createOrUpdateAndReload(execution);
    }

    private OperationExecutionPE createScheduled()
    {
        OperationExecutionPE execution = new OperationExecutionPE();
        execution.setCode(UUID.randomUUID().toString());
        execution.setOwner(testUser);
        execution.setState(OperationExecutionState.SCHEDULED);
        return createOrUpdateAndReload(execution);
    }

    private OperationExecutionPE createRunning()
    {
        OperationExecutionPE execution = new OperationExecutionPE();
        execution.setCode(UUID.randomUUID().toString());
        execution.setOwner(testUser);
        execution.setState(OperationExecutionState.RUNNING);
        execution.setStartDate(new Date());
        return createOrUpdateAndReload(execution);
    }

    private OperationExecutionPE createFinished(long finishDate)
    {
        OperationExecutionPE execution = new OperationExecutionPE();
        execution.setCode(UUID.randomUUID().toString());
        execution.setOwner(testUser);
        execution.setState(OperationExecutionState.FINISHED);
        execution.setStartDate(new Date());
        execution.setFinishDate(new Date(finishDate));
        return createOrUpdateAndReload(execution);
    }

    private OperationExecutionPE createFailed(long finishDate)
    {
        OperationExecutionPE execution = new OperationExecutionPE();
        execution.setCode(UUID.randomUUID().toString());
        execution.setOwner(testUser);
        execution.setState(OperationExecutionState.FAILED);
        execution.setStartDate(new Date());
        execution.setFinishDate(new Date(finishDate));
        return createOrUpdateAndReload(execution);
    }

    private OperationExecutionPE createOrUpdateAndReload(OperationExecutionPE execution)
    {
        executionDAO.createOrUpdate(execution);
        daoFactory.getSessionFactory().getCurrentSession().flush();
        daoFactory.getSessionFactory().getCurrentSession().clear();
        return executionDAO.tryFindByCode(execution.getCode());
    }

    private OperationExecutionPE setAvailabilities(OperationExecutionPE execution, OperationExecutionAvailability availability, long availabilityTime)
    {
        execution.setAvailability(availability);
        execution.setSummaryAvailability(availability);
        execution.setDetailsAvailability(availability);

        execution.setAvailabilityTime(availabilityTime);
        execution.setSummaryAvailabilityTime(availabilityTime);
        execution.setDetailsAvailabilityTime(availabilityTime);

        return execution;
    }

}