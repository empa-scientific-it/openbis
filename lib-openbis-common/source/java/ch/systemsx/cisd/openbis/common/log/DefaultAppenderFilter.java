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

import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

/**
 * @author anttil
 */
public class DefaultAppenderFilter extends Filter
{
    public DefaultAppenderFilter()
    {
        System.err.println("filter");
    }

    @Override
    public int decide(LoggingEvent event)
    {
        String loggerName = event.getLoggerName();
        Object message = event.getMessage();
        if (message instanceof String)
        {
            return decide(loggerName, message.toString());
        } else
        {
            return Filter.NEUTRAL;
        }
    }

    private int decide(String logger, String message)
    {
        if (((logger.startsWith("ACCESS.") || logger.startsWith("TRACKING."))
                && logger.endsWith("Logger")
                && message.contains("(START)"))
                || logger.equals("org.hibernate.orm.deprecation"))
        {
            return Filter.DENY;
        } else
        {
            return Filter.NEUTRAL;
        }
    }
}
