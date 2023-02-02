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

package ch.systemsx.cisd.openbis.common.logging;

import ch.systemsx.cisd.common.logging.ControlFileBasedLogConfiguration;

/**
 * @author pkupczyk
 */
public class ServiceCallLogConfiguration
{

    static final String LOG_SERVICE_CALL_START = "log-service-call-start";

    private static final ServiceCallLogConfiguration instance = new ServiceCallLogConfiguration();

    private ControlFileBasedLogConfiguration config;

    ServiceCallLogConfiguration()
    {
        this(new ControlFileBasedLogConfiguration());
    }

    ServiceCallLogConfiguration(ControlFileBasedLogConfiguration config)
    {
        this.config = config;
        this.config.addBooleanParameter(LOG_SERVICE_CALL_START, false);
    }

    public boolean isLogServiceCallStartEnabled()
    {
        return config.getBooleanParameterValue(LOG_SERVICE_CALL_START);
    }

    public static ServiceCallLogConfiguration getInstance()
    {
        return instance;
    }

}
