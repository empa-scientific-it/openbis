/*
 * Copyright ETH 2016 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.config;

/**
 * @author pkupczyk
 */
public interface IOperationExecutionConfig
{

    String getStorePath();

    String getThreadPoolName();

    int getThreadPoolCoreSize();

    int getThreadPoolMaxSize();

    int getThreadPoolKeepAliveTime();

    String getProgressThreadName();

    int getProgressInterval();

    int getAvailabilityTimeOrDefault(Integer availabilityTimeOrNull);

    int getAvailabilityTimeDefault();

    int getAvailabilityTimeMax();

    int getSummaryAvailabilityTimeOrDefault(Integer summaryAvailabilityTimeOrNull);

    int getSummaryAvailabilityTimeDefault();

    int getSummaryAvailabilityTimeMax();

    int getDetailsAvailabilityTimeOrDefault(Integer detailsAvailabilityTimeOrNull);

    int getDetailsAvailabilityTimeDefault();

    int getDetailsAvailabilityTimeMax();

    String getMarkFailedAfterServerRestartTaskName();
    
    String getMarkTimeOutPendingTaskName();

    int getMarkTimeOutPendingTaskInterval();

    String getMarkTimedOutOrDeletedTaskName();

    int getMarkTimedOutOrDeletedTaskInterval();

    String getCacheClearanceTaskName();

    int getCacheClearanceTaskInterval();

}
