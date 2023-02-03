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

import java.io.IOException;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.action.IDelegatedActionWithResult;
import ch.systemsx.cisd.common.logging.ControlFileBasedLogConfiguration;
import ch.systemsx.cisd.common.logging.ControlFileBasedTest;
import ch.systemsx.cisd.common.logging.event.BooleanEvent;
import ch.systemsx.cisd.common.logging.event.LongEvent;

/**
 * @author pkupczyk
 */
public class DbConnectionLogConfigurationTest extends ControlFileBasedTest
{

    @Test
    public void testDbConnectionsSeparateLogFile() throws IOException
    {
        final DbConnectionLogConfiguration config = createConfig();

        testSwitchBooleanParameter(DbConnectionLogConfiguration.DB_CONNECTIONS_SEPARATE_LOG_FILE, false, new IDelegatedActionWithResult<Boolean>()
            {
                @Override
                public Boolean execute(boolean didOperationSucceed)
                {
                    return config.isDbConnectionsSeparateLogFileEnabled();
                }
            });
    }

    @Test
    public void testDbConnectionsPrintActive() throws IOException
    {
        final DbConnectionLogConfiguration config = createConfig();

        testTriggerLongEvent(DbConnectionLogConfiguration.DB_CONNECTIONS_PRINT_ACTIVE, new IDelegatedActionWithResult<LongEvent>()
            {
                @Override
                public LongEvent execute(boolean didOperationSucceed)
                {
                    return config.getDbConnectionsPrintActiveEvent();
                }
            });
    }

    @Test
    public void testDbConnectionsDebug() throws IOException
    {
        final DbConnectionLogConfiguration config = createConfig();

        testTriggerBooleanEvent(DbConnectionLogConfiguration.DB_CONNECTIONS_DEBUG, new IDelegatedActionWithResult<BooleanEvent>()
            {
                @Override
                public BooleanEvent execute(boolean didOperationSucceed)
                {
                    return config.getDbConnectionsDebugEvent();
                }
            });
    }

    @Test
    public void testDbConnectionsStacktrace() throws IOException
    {
        final DbConnectionLogConfiguration config = createConfig();

        testTriggerBooleanEvent(DbConnectionLogConfiguration.DB_CONNECTIONS_STACKTRACE, new IDelegatedActionWithResult<BooleanEvent>()
            {
                @Override
                public BooleanEvent execute(boolean didOperationSucceed)
                {
                    return config.getDbConnectionsStacktraceEvent();
                }
            });
    }

    private DbConnectionLogConfiguration createConfig()
    {
        final ControlFileBasedLogConfiguration config = new ControlFileBasedLogConfiguration(getControlFileDirectory(), -1);
        return new DbConnectionLogConfiguration(config);
    }

}
