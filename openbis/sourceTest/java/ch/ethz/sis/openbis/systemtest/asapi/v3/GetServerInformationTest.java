/*
 * Copyright 2018 ETH Zuerich, SIS
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

import java.util.Map;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.BuildAndEnvironmentInfo;

/**
 * @author Franz-Josef Elmer
 */
public class GetServerInformationTest extends AbstractTest
{
    @Test
    public void testGetServerInformation()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        // When
        Map<String, String> result = v3api.getServerInformation(sessionToken);

        // Then
        assertEquals(result.size(), 13);
        assertEquals(result.get("api-version"), "3.5");
        assertEquals(result.get("archiving-configured"), "false");
        assertEquals(result.get("authentication-service"), "dummy-authentication-service");
        assertEquals(result.get("authentication-service.switch-aai.link"), null);
        assertEquals(result.get("authentication-service.switch-aai.label"), null);
        assertEquals(result.get("enabled-technologies"), "test-.*");
        assertEquals(result.get("project-samples-enabled"), "true");
        assertEquals(result.get("personal-access-tokens-enabled"), "true");
        assertEquals(result.get("personal-access-tokens-max-validity-period"), "2592000");
        assertEquals(result.get("personal-access-tokens-validity-warning-period"), "432000");
        assertEquals(result.get("create-continuous-sample-codes"), "false");
        assertEquals(result.get("openbis-version"), BuildAndEnvironmentInfo.INSTANCE.getVersion());
        assertEquals(result.get("openbis.support.email"), "openbis-support@id.ethz.ch");

        v3api.logout(sessionToken);
    }
}
