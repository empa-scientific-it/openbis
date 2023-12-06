/*
 *  Copyright ETH 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ch.ethz.sis.openbis.generic.dss.systemtest.api.v3;

import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.CustomDSSServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.id.CustomDssServiceCode;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.id.ICustomDSSServiceId;
import org.testng.annotations.Test;

public class CustomServiceTest extends AbstractFileTest
{

    @Test
    public void testCustomServicePong()
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);

        ICustomDSSServiceId serviceId = new CustomDssServiceCode("test-custom-service");
        CustomDSSServiceExecutionOptions options = new CustomDSSServiceExecutionOptions();
        options.withParameter("key", "PING");
        String result = (String) dss.executeCustomDSSService(sessionToken, serviceId, options);

        // Then
        assertEquals("PONG", result);

        as.logout(sessionToken);
    }


    @Test
    public void testCustomServiceWithScriptPong()
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);

        ICustomDSSServiceId serviceId = new CustomDssServiceCode("test-custom-service-with-script");
        CustomDSSServiceExecutionOptions options = new CustomDSSServiceExecutionOptions();
        options.withParameter("key", "PING");
        String result = (String) dss.executeCustomDSSService(sessionToken, serviceId, options);

        // Then
        assertEquals("PONG", result);

        as.logout(sessionToken);
    }


}
