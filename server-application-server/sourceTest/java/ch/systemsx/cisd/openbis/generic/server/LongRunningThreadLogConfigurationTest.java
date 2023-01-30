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

import java.io.IOException;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.action.IDelegatedActionWithResult;
import ch.systemsx.cisd.common.logging.ControlFileBasedLogConfiguration;
import ch.systemsx.cisd.common.logging.ControlFileBasedTest;

/**
 * @author pkupczyk
 */
public class LongRunningThreadLogConfigurationTest extends ControlFileBasedTest
{

    @Test
    public void testIsLoggingEnabled() throws IOException
    {
        final LongRunningThreadLogConfiguration config = createConfig();

        testSwitchBooleanParameter(LongRunningThreadLogConfiguration.LONG_RUNNING_THREAD_LOGGING, true, new IDelegatedActionWithResult<Boolean>()
            {
                @Override
                public Boolean execute(boolean didOperationSucceed)
                {
                    return config.isLoggingEnabled();
                }
            });
    }

    @Test
    public void testLogInterval() throws IOException
    {
        final LongRunningThreadLogConfiguration config = createConfig();

        testSwitchLongParameter(LongRunningThreadLogConfiguration.LONG_RUNNING_THREAD_LOGGING_INTERVAL, 60000, new IDelegatedActionWithResult<Long>()
            {
                @Override
                public Long execute(boolean didOperationSucceed)
                {
                    return config.logInterval();
                }
            });
    }

    @Test
    public void testMaxValidInvocationLength() throws IOException
    {
        final LongRunningThreadLogConfiguration config = createConfig();

        testSwitchLongParameter(LongRunningThreadLogConfiguration.LONG_RUNNING_THREAD_ALERT_THRESHOLD, 15000, new IDelegatedActionWithResult<Long>()
            {
                @Override
                public Long execute(boolean didOperationSucceed)
                {
                    return config.maxValidInvocationLength();
                }
            });
    }

    private LongRunningThreadLogConfiguration createConfig()
    {
        final ControlFileBasedLogConfiguration config = new ControlFileBasedLogConfiguration(getControlFileDirectory(), -1);
        return new LongRunningThreadLogConfiguration(config);
    }

}
