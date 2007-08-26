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

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

/**
 * A {@link ISimpleLogger} that is based on log4j.
 *
 * @author Bernd Rinn
 */
public class Log4jSimpleLogger implements ISimpleLogger
{
    private final Priority log4jPriority;
    
    private final Logger log4jLogger;
    
    public Log4jSimpleLogger(Priority log4jPriority, Logger log4jLogger)
    {
        this.log4jPriority = log4jPriority;
        this.log4jLogger = log4jLogger;
    }

    public void log(String message)
    {
        log4jLogger.log(log4jPriority, message);
    }

    public void log(String messageTemplate, Object... args)
    {
        log4jLogger.log(log4jPriority, String.format(messageTemplate, args));
    }

}
