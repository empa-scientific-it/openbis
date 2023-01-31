/*
 * Copyright 2020 ETH Zuerich, SIS
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

import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;

/**
 * @author Franz-Josef Elmer
 */
public class CountStopCondition implements ILogMonitoringStopCondition
{
    private static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, CountStopCondition.class);

    private final ILogMonitoringStopCondition stopCondition;

    private final int n;
    
    private int count;

    public CountStopCondition(ILogMonitoringStopCondition stopCondition, int n)
    {
        this.stopCondition = stopCondition;
        this.n = n;
    }

    @Override
    public boolean stopConditionFulfilled(ParsedLogEntry logEntry)
    {
        if (count >= n)
        {
            return true;
        }
        if (stopCondition.stopConditionFulfilled(logEntry))
        {
            count++;
            operationLog.info(count + " of " + n + " expected entry detected at line " + logEntry.getLineIndex() + " at "
                    + new SimpleDateFormat(BasicConstant.DATE_HOURS_MINUTES_SECONDS_PATTERN).format(logEntry.getTimestamp()));
            return count >= n;
        }
        return false;
    }

}
