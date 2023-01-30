/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.locator;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * @author Kaloyan Enimanev
 */
public class MaterialCodeUtilsTest extends AssertJUnit
{

    @Test
    public void testDecode()
    {
        assertEquals("A_a(c:C).%d", MaterialCodeUtils.decode("A_a%28c%3AC%29.%25d"));
        assertEquals("Äpfel & Nüße", MaterialCodeUtils.decode("%C3%84pfel%20%26%20N%C3%BC%C3%9Fe"));
        assertEquals("Äpfel & Nüße", MaterialCodeUtils.decode("Äpfel%20%26%20N%C3%BCße"));
        // Chrome-style 'decoded' history tokens
        assertEquals("A_a(c:C).%d", MaterialCodeUtils.decode("A_a(c%3AC).%d"));
    }
}
