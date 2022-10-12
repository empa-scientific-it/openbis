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

/**
 * @author pkupczyk
 */
public class GetServerPublicInformationTest extends AbstractTest
{
    @Test
    public void testGetServerPublicInformation()
    {
        Map<String, String> result = v3api.getServerPublicInformation();

        assertEquals(result.size(), 4);
        assertEquals(result.get("authentication-service"), "dummy-authentication-service");
        assertEquals(result.get("authentication-service.switch-aai.link"), null);
        assertEquals(result.get("authentication-service.switch-aai.label"), null);
        assertEquals(result.get("openbis.support.email"), "openbis-support@id.ethz.ch");
    }
}
