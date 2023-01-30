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
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecution;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionAvailability;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionState;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.fetchoptions.OperationExecutionDetailsFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.fetchoptions.OperationExecutionFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.fetchoptions.OperationExecutionSummaryFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.id.IOperationExecutionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.id.OperationExecutionPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.search.OperationExecutionSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.config.IOperationExecutionConfig;
import ch.systemsx.cisd.common.maintenance.MaintenancePlugin;
import ch.systemsx.cisd.openbis.generic.server.MaintenanceTaskStarter;

/**
 * @author pkupczyk
 */
public class AbstractOperationExecutionTest extends AbstractTest
{

    @Autowired
    private IOperationExecutionConfig operationExecutionConfig;

    @Autowired
    private MaintenanceTaskStarter maintenanceTaskStarter;

    protected SpaceCreation spaceCreation()
    {
        return spaceCreation(UUID.randomUUID().toString().toUpperCase());
    }

    protected SpaceCreation spaceCreation(String code)
    {
        SpaceCreation creation = new SpaceCreation();
        creation.setCode(code);
        return creation;
    }

    protected OperationExecutionFetchOptions emptyOperationExecutionFetchOptions()
    {
        return new OperationExecutionFetchOptions();
    }

    protected OperationExecutionFetchOptions fullOperationExecutionFetchOptions()
    {
        OperationExecutionFetchOptions fo = new OperationExecutionFetchOptions();

        OperationExecutionSummaryFetchOptions sfo = fo.withSummary();
        sfo.withError();
        sfo.withOperations();
        sfo.withProgress();
        sfo.withResults();

        OperationExecutionDetailsFetchOptions dfo = fo.withDetails();
        dfo.withError();
        dfo.withOperations();
        dfo.withProgress();
        dfo.withResults();

        return fo;
    }

    protected List<OperationExecution> listExecutions(String sessionToken)
    {
        return listExecutions(sessionToken, new OperationExecutionFetchOptions());
    }

    protected List<OperationExecution> listExecutions(String sessionToken, OperationExecutionFetchOptions fo)
    {
        SearchResult<OperationExecution> result = v3api.searchOperationExecutions(sessionToken, new OperationExecutionSearchCriteria(), fo);
        return result.getObjects();
    }

    protected OperationExecution getExecution(String sessionToken, OperationExecutionPermId executionId, OperationExecutionFetchOptions fo)
    {
        return getExecutionInState(sessionToken, executionId, null, fo);
    }

    protected OperationExecution getExecutionInState(String sessionToken, OperationExecutionPermId executionId,
            OperationExecutionState stateOrNull, OperationExecutionFetchOptions fo)
    {
        Map<IOperationExecutionId, OperationExecution> map =
                v3api.getOperationExecutions(sessionToken, Arrays.asList(executionId), fo);

        OperationExecution execution = map.get(executionId);

        if (execution != null)
        {
            if (stateOrNull == null || stateOrNull.equals(execution.getState()))
            {
                return execution;
            } else
            {
                return null;
            }
        } else
        {
            return null;
        }
    }

    protected OperationExecution waitAndGetExecution(String sessionToken, OperationExecutionPermId executionId, OperationExecutionFetchOptions fo)
    {
        return waitAndGetExecutionInState(sessionToken, executionId, null, fo);
    }

    protected OperationExecution waitAndGetExecutionInState(String sessionToken, OperationExecutionPermId executionId,
            OperationExecutionState stateOrNull,
            OperationExecutionFetchOptions fo)
    {
        long timeout = System.currentTimeMillis() + 5000;
        OperationExecution execution = null;

        while (true)
        {
            Map<IOperationExecutionId, OperationExecution> map = v3api.getOperationExecutions(sessionToken, Arrays.asList(executionId), fo);

            execution = map.get(executionId);

            if (execution != null)
            {
                if (stateOrNull == null || stateOrNull.equals(execution.getState()))
                {
                    return execution;
                } else
                {
                    if (System.currentTimeMillis() > timeout)
                    {
                        throw new RuntimeException("Timed out waiting for execution " + executionId + " to change state to " + stateOrNull);
                    } else
                    {
                        try
                        {
                            Thread.sleep(100);
                        } catch (Exception e)
                        {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }

    protected MaintenancePlugin getMarkTimeOutPendingMaintenancePlugin()
    {
        return getMaintenancePlugin(operationExecutionConfig.getMarkTimeOutPendingTaskName());
    }

    protected MaintenancePlugin getMarkTimedOutOrDeletedMaintenancePlugin()
    {
        return getMaintenancePlugin(operationExecutionConfig.getMarkTimedOutOrDeletedTaskName());
    }

    private MaintenancePlugin getMaintenancePlugin(String pluginName)
    {
        for (MaintenancePlugin plugin : maintenanceTaskStarter.getPlugins())
        {
            if (plugin.getPluginName().equals(pluginName))
            {
                return plugin;
            }
        }
        return null;
    }

    protected void assertAvailabilities(OperationExecution execution, OperationExecutionAvailability availability,
            Integer availabilityTime, OperationExecutionAvailability summaryAvailability, Integer summaryAvailabilityTime,
            OperationExecutionAvailability detailsAvailability, Integer detailsAvailabilityTime)
    {
        assertEquals(execution.getAvailability(), availability);
        assertEquals(execution.getSummaryAvailability(), summaryAvailability);
        assertEquals(execution.getDetailsAvailability(), detailsAvailability);

        assertEquals(execution.getAvailabilityTime(), availabilityTime);
        assertEquals(execution.getSummaryAvailabilityTime(), summaryAvailabilityTime);
        assertEquals(execution.getDetailsAvailabilityTime(), detailsAvailabilityTime);
    }

    protected int defaultAvalability()
    {
        return operationExecutionConfig.getAvailabilityTimeDefault();
    }

    protected int defaultSummaryAvalability()
    {
        return operationExecutionConfig.getSummaryAvailabilityTimeDefault();
    }

    protected int defaultDetailsAvalability()
    {
        return operationExecutionConfig.getDetailsAvailabilityTimeDefault();
    }

}
