/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.logging;

import java.util.Date;

import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.TriggeringEventEvaluator;

/**
 * A {@link BundledTimestampTriggeringEventEvaluator} that triggers every 10 minutes. Can be used, e.g. to make the
 * {@link org.apache.log4j.net.SMTPAppender} send email also on non-error conditions.; REMARK: This is a solution for Mario Emmenlauer. It should not
 * be recommended because it can lead to the fact that important log events aren't sent for days because no triggering event appears.
 * 
 * @author Althea Parker
 */
public class BundledTimestampTriggeringEventEvaluator implements TriggeringEventEvaluator
{

    private long timestamp;

    public static long timePeriod = 10 * 60000;

    public BundledTimestampTriggeringEventEvaluator()
    {
        timestamp = new Date().getTime();
    }

    @Override
    public boolean isTriggeringEvent(LoggingEvent event)
    {
        long current_time = new Date().getTime();
        if (current_time >= timestamp + timePeriod)
        {
            timestamp = current_time;
            return true;
        }
        else
        {
            return false;
        }
    }

}
