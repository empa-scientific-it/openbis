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

package ch.systemsx.cisd.openbis.generic.server;

import ch.systemsx.cisd.common.logging.ControlFileBasedLogConfiguration;

/**
 * @author anttil
 */
public class LongRunningThreadLogConfiguration
{
    static final String LONG_RUNNING_THREAD_LOGGING = "long-running-thread-logging";

    static final String LONG_RUNNING_THREAD_LOGGING_INTERVAL = "long-running-thread-logging-interval";

    static final String LONG_RUNNING_THREAD_ALERT_THRESHOLD = "long-running-thread-alert-threshold";

    private static final LongRunningThreadLogConfiguration instance = new LongRunningThreadLogConfiguration();

    private ControlFileBasedLogConfiguration config;

    LongRunningThreadLogConfiguration()
    {
        this(new ControlFileBasedLogConfiguration());
    }

    LongRunningThreadLogConfiguration(ControlFileBasedLogConfiguration config)
    {
        this.config = config;
        this.config.addBooleanParameter(LONG_RUNNING_THREAD_LOGGING, true);
        this.config.addLongParameter(LONG_RUNNING_THREAD_LOGGING_INTERVAL, 60000);
        this.config.addLongParameter(LONG_RUNNING_THREAD_ALERT_THRESHOLD, 15000);
    }

    public boolean isLoggingEnabled()
    {
        return config.getBooleanParameterValue(LONG_RUNNING_THREAD_LOGGING);
    }

    public long logInterval()
    {
        return config.getLongParameterValue(LONG_RUNNING_THREAD_LOGGING_INTERVAL);
    }

    public long maxValidInvocationLength()
    {
        return config.getLongParameterValue(LONG_RUNNING_THREAD_ALERT_THRESHOLD);
    }

    public static LongRunningThreadLogConfiguration getInstance()
    {
        return instance;
    }

}
