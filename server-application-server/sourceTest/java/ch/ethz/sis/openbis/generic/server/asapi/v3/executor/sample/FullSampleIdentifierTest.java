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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sample.FullSampleIdentifier;

/**
 * @author Franz-Josef Elmer
 */
public class FullSampleIdentifierTest
{

    @Test
    public void testHappyCases()
    {
        assertSampId("/S1");
        assertSampId("/C1:S1");
        assertSampId("/SPACE1/S2");
        assertSampId("/SPACE1/S2:A02");
        assertSampId("/SPACE1/PROJECT1/S1");
        assertSampId("/SPACE1/PROJECT1/S1:A02");
        assertSampIdWithHomeSpace("/HS/S1", "//s1", "HS");
        assertSampIdWithHomeSpace("/HS/C1:S1", "//c1:s1", "HS");
        assertSampIdWithHomeSpace("/HS/PROJECT1/S1", "//Project1/s1", "HS");
        assertSampIdWithHomeSpace("/HS/PROJECT1/C1:S1", "//project1/c1:s1", "HS");
        assertSampIdWithHomeSpace("/SP1/PROJECT1/C1:S1", "/sp1/project1/c1:s1", "HS");
    }

    @Test
    public void testFailingCases()
    {
        assertInvalidSampId("Unspecified sample identifier.", null);
        assertInvalidSampId("Unspecified sample identifier.", "");
        assertInvalidSampId("Sample identifier has to start with a '/': A/BC", "A/BC");
        assertInvalidSampId("Sample identifier can not contain more than three '/': /A/B/C/D", "/A/B/C/D");
        assertInvalidSampId("Sample code can not contain more than one ':': /A/B:C:D", "/A/B:C:D");

        assertInvalidSampId("Sample identifier don't contain any codes: ///", "///");
        assertInvalidSampId("Sample identifier don't contain any codes: //", "//");
        assertInvalidSampId("Sample identifier don't contain any codes: /", "/");

        assertInvalidSampId("Space code can not be an empty string.", "//S1");
        assertInvalidSampId("Project code can not be an empty string.", "/S//S1");
        assertInvalidSampId("Sample code starts or ends with ':': /S/:S1", "/S/:S1");
        assertInvalidSampId("Sample code starts or ends with ':': /S/C1:", "/S/C1:");
        assertInvalidSampId("Sample code can not contain more than one ':': /S/C1:S1:", "/S/C1:S1:");

        String prefix = " containing other characters than letters, numbers, '_', '-' and '.': ";
        assertInvalidSampId("Sample code" + prefix + "S1&*", "/S1&*");
        assertInvalidSampId("Space code" + prefix + "SPA&CE1", "/SPA&CE1/S2");
        assertInvalidSampId("Container sample code" + prefix + "S^2", "/SPACE1/S^2:A02");
        assertInvalidSampId("Project code" + prefix + "PRO<>JECT1", "/SPACE1/PRO<>JECT1/S1");
        assertInvalidSampId("Sample subcode" + prefix + "A0(2", "/SPACE1/PROJECT1/S1:A0(2");
    }

    private void assertInvalidSampId(String expectedErrorMsg, String identifier)
    {
        assertInvalidSampId(expectedErrorMsg, identifier, null);
    }

    private void assertInvalidSampId(String expectedErrorMsg, String identifier, String homeSpaceOrNull)
    {
        try
        {
            new FullSampleIdentifier(identifier, homeSpaceOrNull);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex)
        {
            assertEquals(expectedErrorMsg, ex.getMessage());
        }
    }

    private void assertSampId(String identifier)
    {
        assertEquals(new FullSampleIdentifier(identifier, null).toString(), identifier);
    }

    private void assertSampIdWithHomeSpace(String expectedIdentifier, String identifier, String homeSpace)
    {
        assertEquals(new FullSampleIdentifier(identifier, homeSpace).toString(), expectedIdentifier);
    }

}
