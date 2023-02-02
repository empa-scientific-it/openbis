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

import java.io.IOException;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.action.IDelegatedActionWithResult;
import ch.systemsx.cisd.common.logging.ControlFileBasedLogConfiguration;
import ch.systemsx.cisd.common.logging.ControlFileBasedTest;

/**
 * @author pkupczyk
 */
public class ServiceCallLogConfigurationTest extends ControlFileBasedTest
{

    @Test
    public void testLogServiceCallStart() throws IOException
    {
        final ServiceCallLogConfiguration config = createConfig();

        testSwitchBooleanParameter(ServiceCallLogConfiguration.LOG_SERVICE_CALL_START, false, new IDelegatedActionWithResult<Boolean>()
            {
                @Override
                public Boolean execute(boolean didOperationSucceed)
                {
                    return config.isLogServiceCallStartEnabled();
                }
            });
    }

    private ServiceCallLogConfiguration createConfig()
    {
        final ControlFileBasedLogConfiguration config = new ControlFileBasedLogConfiguration(getControlFileDirectory(), -1);
        return new ServiceCallLogConfiguration(config);
    }

}
