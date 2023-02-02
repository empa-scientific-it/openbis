/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.common.log;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.spi.LoggingEvent;

/**
 * @author anttil
 */
public class ServiceCallAppender extends DailyRollingFileAppender
{

    int count = 0;

    @Override
    public void append(LoggingEvent event)
    {
        String message = event.getMessage().toString();

        if (message.contains("(START)"))
        {
            synchronized (this)
            {
                count++;
            }
        } else if (message.contains("ms)"))
        {
            synchronized (this)
            {
                if (count == 0)
                {
                    return;
                }
                count--;
            }
        } else
        {
            return;
        }

        super.append(event);
    }
}
