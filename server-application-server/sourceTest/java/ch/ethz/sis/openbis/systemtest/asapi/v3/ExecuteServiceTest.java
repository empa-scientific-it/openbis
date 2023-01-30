/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.CustomASServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id.CustomASServiceCode;

/**
 * @author Franz-Josef Elmer
 */
public class ExecuteServiceTest extends AbstractTest
{

    @Test
    public void testSearchServices()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        CustomASServiceExecutionOptions options = new CustomASServiceExecutionOptions();
        options.withParameter("name", Math.PI);

        Object result = v3api.executeCustomASService(sessionToken, new CustomASServiceCode("simple-service"), options);

        assertEquals(result, "hello 3.14159265359. Spaces: [Space CISD]");
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        CustomASServiceExecutionOptions o = new CustomASServiceExecutionOptions();
        // values of these parameters must not be logged as they can contain sensitive data
        o.withParameter("a", "1");
        o.withParameter("b", "2");

        v3api.executeCustomASService(sessionToken, new CustomASServiceCode("simple-service"), o);

        assertAccessLog(
                "execute-custom-as-service  SERVICE_ID('simple-service') EXECUTION_OPTIONS('CustomASServiceExecutionOptions: parameterKeys=[a, b]')");
    }

}