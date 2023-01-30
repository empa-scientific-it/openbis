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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.store;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.OperationExecutionAvailability;
import ch.systemsx.cisd.openbis.generic.shared.dto.OperationExecutionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.OperationExecutionState;

/**
 * @author pkupczyk
 */
@Component
public class OperationExecutionDBStore implements IOperationExecutionDBStore
{

    @Autowired
    private IOperationExecutionDBStoreDAO dao;

    public OperationExecutionDBStore()
    {
    }

    OperationExecutionDBStore(IOperationExecutionDBStoreDAO dao)
    {
        this.dao = dao;
    }

    @Override
    public void executionNew(String code, Long owner, String description, String notification, List<String> operations, long availabilityTime,
            long summaryAvailabilityTime, long detailsAvailabilityTime, String detailsPath)
    {
        OperationExecutionPE executionPE = new OperationExecutionPE();

        executionPE.setCode(code);
        executionPE.setState(OperationExecutionState.NEW);
        executionPE.setOwner(dao.findPersonById(owner));
        executionPE.setNotification(notification);
        executionPE.setDescription(description);
        executionPE.setAvailability(initialAvailability(availabilityTime));
        executionPE.setAvailabilityTime(availabilityTime);
        executionPE.setCreationDate(new Date());
        executionPE.setSummaryOperationsList(operations);
        executionPE.setSummaryAvailability(initialAvailability(summaryAvailabilityTime));
        executionPE.setSummaryAvailabilityTime(summaryAvailabilityTime);

        executionPE.setDetailsAvailability(initialAvailability(detailsAvailabilityTime));
        executionPE.setDetailsAvailabilityTime(detailsAvailabilityTime);
        executionPE.setDetailsPath(detailsPath);

        try
        {
            dao.createExecution(executionPE);
        } catch (DataAccessException e)
        {
            DataAccessExceptionTranslator.throwException(e, "Operation execution " + code, null);
        }
    }

    @Override
    public void executionScheduled(String code)
    {
        OperationExecutionPE executionPE = dao.findExecutionByCode(code);
        executionPE.setState(OperationExecutionState.SCHEDULED);
    }

    @Override
    public void executionRunning(String code)
    {
        OperationExecutionPE executionPE = dao.findExecutionByCode(code);
        executionPE.setState(OperationExecutionState.RUNNING);
        executionPE.setStartDate(new Date());
    }

    @Override
    public void executionProgressed(String code, String progress)
    {
        // This method may be called when the execution state is already FINISHED (progress is reported with some delay by a different thread - other
        // than the execution thread). Because progress can be not null only if the execution state is RUNNING or FAILED the update has to be done
        // with appropriate condition. Also the execution might not exist at this point anymore.

        dao.updateExecutionProgress(code, progress);
    }

    @Override
    public void executionFailed(String code, String error)
    {
        OperationExecutionPE executionPE = dao.findExecutionByCode(code);
        executionPE.setState(OperationExecutionState.FAILED);
        executionPE.setSummaryError(error);
        Date now = new Date();
        if (executionPE.getStartDate() == null)
        {
            executionPE.setStartDate(now);
        }
        executionPE.setFinishDate(now);
    }

    @Override
    public void executionFinished(String code, List<String> results)
    {
        OperationExecutionPE executionPE = dao.findExecutionByCode(code);
        executionPE.setState(OperationExecutionState.FINISHED);
        executionPE.setSummaryProgress(null);
        executionPE.setSummaryResultsList(results);
        executionPE.setFinishDate(new Date());
    }

    @Override
    public void executionAvailability(String code, OperationExecutionAvailability availability)
    {
        OperationExecutionPE executionPE = dao.findExecutionByCode(code);

        if (executionPE != null)
        {
            if (OperationExecutionAvailability.TIMED_OUT.equals(availability) || OperationExecutionAvailability.DELETED.equals(availability))
            {
                try
                {
                    dao.deleteExecution(executionPE);
                } catch (DataAccessException e)
                {
                    DataAccessExceptionTranslator.throwException(e, "Operation execution " + code, null);
                }
            } else
            {
                executionPE.setAvailability(availability);
            }
        }
    }

    @Override
    public void executionSummaryAvailability(String code, OperationExecutionAvailability summaryAvailability)
    {
        OperationExecutionPE executionPE = dao.findExecutionByCode(code);

        if (executionPE != null)
        {
            executionPE.setSummaryAvailability(summaryAvailability);

            if (OperationExecutionAvailability.TIMED_OUT.equals(summaryAvailability)
                    || OperationExecutionAvailability.DELETED.equals(summaryAvailability))
            {
                executionPE.setSummaryOperations(null);
                executionPE.setSummaryProgress(null);
                executionPE.setSummaryError(null);
                executionPE.setSummaryResults(null);
            }
        }
    }

    @Override
    public void executionDetailsAvailability(String code, OperationExecutionAvailability detailsAvailability)
    {
        OperationExecutionPE executionPE = dao.findExecutionByCode(code);

        if (executionPE != null)
        {
            executionPE.setDetailsAvailability(detailsAvailability);

            if (OperationExecutionAvailability.TIMED_OUT.equals(detailsAvailability)
                    || OperationExecutionAvailability.DELETED.equals(detailsAvailability))
            {
                executionPE.setDetailsPath(null);
            }
        }
    }

    @Override
    public OperationExecutionPE getExecution(String code)
    {
        return dao.findExecutionByCode(code);
    }

    @Override
    public List<OperationExecutionPE> getExecutions()
    {
        return dao.findAllExecutions();
    }

    @Override
    public List<OperationExecutionPE> getExecutionsToBeFailedAfterServerRestart(Date serverStartDate)
    {
        return dao.findExecutionsToBeFailedAfterServerRestart(serverStartDate);
    }

    @Override
    public List<OperationExecutionPE> getExecutionsToBeTimeOutPending()
    {
        return dao.findExecutionsToBeTimeOutPending();
    }

    @Override
    public List<OperationExecutionPE> getExecutionsToBeTimedOut()
    {
        return dao.findExecutionsToBeTimedOut();
    }

    @Override
    public List<OperationExecutionPE> getExecutionsToBeDeleted()
    {
        return dao.findExecutionsToBeDeleted();
    }

    private OperationExecutionAvailability initialAvailability(Long availabilityTime)
    {
        return availabilityTime != null && availabilityTime > 0 ? OperationExecutionAvailability.AVAILABLE
                : OperationExecutionAvailability.TIME_OUT_PENDING;
    }

}
