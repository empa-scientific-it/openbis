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

package ch.systemsx.cisd.dbmigration.logging;

import ch.systemsx.cisd.common.logging.ControlFileBasedLogConfiguration;
import ch.systemsx.cisd.common.logging.event.BooleanEvent;
import ch.systemsx.cisd.common.logging.event.LongEvent;

/**
 * @author pkupczyk
 */
public class DbConnectionLogConfiguration
{

    static final String DB_CONNECTIONS_SEPARATE_LOG_FILE = "db-connections-separate-log-file";

    static final String DB_CONNECTIONS_PRINT_ACTIVE = "db-connections-print-active";

    static final String DB_CONNECTIONS_STACKTRACE = "db-connections-stacktrace";

    static final String DB_CONNECTIONS_DEBUG = "db-connections-debug";

    private static final DbConnectionLogConfiguration instance = new DbConnectionLogConfiguration();

    private ControlFileBasedLogConfiguration config;

    DbConnectionLogConfiguration()
    {
        this(new ControlFileBasedLogConfiguration());
    }

    DbConnectionLogConfiguration(ControlFileBasedLogConfiguration config)
    {
        this.config = config;
        this.config.addBooleanParameter(DB_CONNECTIONS_SEPARATE_LOG_FILE, false);
        this.config.addLongEvent(DB_CONNECTIONS_PRINT_ACTIVE);
        this.config.addBooleanEvent(DB_CONNECTIONS_STACKTRACE);
        this.config.addBooleanEvent(DB_CONNECTIONS_DEBUG);
    }

    public boolean isDbConnectionsSeparateLogFileEnabled()
    {
        return config.getBooleanParameterValue(DB_CONNECTIONS_SEPARATE_LOG_FILE);
    }

    public LongEvent getDbConnectionsPrintActiveEvent()
    {
        return config.getLongEvent(DB_CONNECTIONS_PRINT_ACTIVE);
    }

    public BooleanEvent getDbConnectionsStacktraceEvent()
    {
        return config.getBooleanEvent(DB_CONNECTIONS_STACKTRACE);
    }

    public BooleanEvent getDbConnectionsDebugEvent()
    {
        return config.getBooleanEvent(DB_CONNECTIONS_DEBUG);
    }

    public static DbConnectionLogConfiguration getInstance()
    {
        return instance;
    }

}
