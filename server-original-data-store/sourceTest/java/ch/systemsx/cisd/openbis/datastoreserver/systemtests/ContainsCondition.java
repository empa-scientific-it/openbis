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
 * Condition which is fulfilled if the log message contains the specified snippet.
 *
 * @author Franz-Josef Elmer
 */
public class ContainsCondition implements ILogMonitoringStopCondition
{
    private String logMessageSnippet;

    public ContainsCondition(String logMessageSnippet)
    {
        this.logMessageSnippet = logMessageSnippet;
    }

    @Override
    public boolean stopConditionFulfilled(ParsedLogEntry logEntry)
    {
        return logEntry.getLogMessage().contains(logMessageSnippet);
    }

    @Override
    public String toString()
    {
        return "Contains: " + logMessageSnippet;
    }

}
