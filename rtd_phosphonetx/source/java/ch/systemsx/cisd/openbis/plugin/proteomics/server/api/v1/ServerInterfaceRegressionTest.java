/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.proteomics.server.api.v1;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.RegressionTestCase;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.api.v1.IProteomicsDataService;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ServerInterfaceRegressionTest extends RegressionTestCase
{
    @Test
    public void testServerAnnotations()
    {
        assertMandatoryMethodAnnotations(IProteomicsDataService.class, ProteomicsDataService.class,
                "logout: RolesAllowed\n" + "tryToAuthenticateAtRawDataServer: RolesAllowed\n");
    }
}
