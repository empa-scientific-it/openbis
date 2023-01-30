/*
 * Copyright 2010 ETH Zuerich, CISD
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

import java.util.Map;

import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;

/**
 * Test view locator from the Javascript side.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class ViewLocatorTest extends AbstractGWTTestCase
{
    public void testURLParsingOfValidParameters()
    {
        String urlParameterString =
                "action=search&entity=dataset&code=184029&experiment=%2FCISD%2FNEMO%2FEXP1";
        ViewLocator viewLocator = new ViewLocator(urlParameterString);
        assertEquals("search", viewLocator.tryGetAction());
        assertEquals("dataset", viewLocator.tryGetEntity());
        Map<String, String> parameters = viewLocator.getParameters();
        assertEquals("184029", parameters.get("code"));
        assertEquals("/CISD/NEMO/EXP1", parameters.get("experiment"));
        assertTrue(viewLocator.isValid());
    }

    public void testDefaultingOfParameters()
    {
        String urlParameterString = "entity=dataset&code=184029";
        ViewLocator viewLocator = new ViewLocator(urlParameterString);
        assertEquals("VIEW", viewLocator.tryGetAction());
        assertTrue(viewLocator.isValid());
    }

    public void testURLParsingOfInvalidParameters()
    {
        String urlParameterString = "code=184029";
        ViewLocator viewLocator = new ViewLocator(urlParameterString);
        assertEquals(null, viewLocator.tryGetAction());
        assertFalse(viewLocator.isValid());
        assertTrue(viewLocator.isInvalid());
    }
}
