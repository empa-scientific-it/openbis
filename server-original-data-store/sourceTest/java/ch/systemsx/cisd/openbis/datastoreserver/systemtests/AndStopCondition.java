/*
 * Copyright 2014 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests;

/**
 * Condition which is fulfilled if both wrapped conditions are fulfilled.
 *
 * @author Franz-Josef Elmer
 */
public class AndStopCondition implements ILogMonitoringStopCondition
{
    private ILogMonitoringStopCondition condition1;

    private ILogMonitoringStopCondition condition2;

    public AndStopCondition(ILogMonitoringStopCondition condition1, ILogMonitoringStopCondition condition2)
    {
        this.condition1 = condition1;
        this.condition2 = condition2;
    }

    @Override
    public boolean stopConditionFulfilled(ParsedLogEntry logEntry)
    {
        return condition1.stopConditionFulfilled(logEntry) && condition2.stopConditionFulfilled(logEntry);
    }

    @Override
    public String toString()
    {
        return "(" + condition1 + ") and (" + condition2 + ")";
    }

}
