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

package ch.systemsx.cisd.openbis.systemtest.api.v1;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl.EncapsulatedCommonServer;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl.MasterDataRegistrationScriptRunner;
import ch.systemsx.cisd.openbis.systemtest.SystemTestCase;

/**
 * @author Franz-Josef Elmer
 */
public class MasterDataRegistrationScriptRunnerTest extends SystemTestCase
{
    private String sessionToken;

    private MasterDataRegistrationScriptRunner scriptRunner;

    @BeforeMethod
    public void beforeMethod()
    {
        sessionToken = commonServer.tryAuthenticate("test", "a").getSessionToken();
        EncapsulatedCommonServer server =
                EncapsulatedCommonServer.create(commonServer, sessionToken);

        scriptRunner = new MasterDataRegistrationScriptRunner(server);
    }

    @AfterMethod
    public void afterMethod()
    {
        commonServer.logout(sessionToken);
    }

    @Test
    public void testGetOrCreateExistingSystemVocabulary()
    {
        scriptRunner
                .executeScript("import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType as DataType\n"
                        + "tr = service.transaction()\n"
                        + "vocabulary = tr.getOrCreateNewVocabulary('PLATE_GEOMETRY')\n"
                        + "vocabulary.setDescription('The geometry or dimensions of a plate')\n"
                        + "vocabulary.setUrlTemplate(None)\n"
                        + "vocabulary.setManagedInternally(True)\n"
                        + "vocabulary.setManagedInternally(True)", null);
    }

    @Test
    public void testGetOrCreateExistingSystemProperty()
    {
        scriptRunner
                .executeScript("import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType as DataType\n"
                        + "tr = service.transaction()\n"
                        + "prop_type = tr.getOrCreateNewPropertyType('PLATE_GEOMETRY', DataType.VARCHAR)\n"
                        + "prop_type.setLabel('Plate Geometry')\n"
                        + "prop_type.setManagedInternally(True)\n"
                        + "prop_type.setManagedInternally(True)", null);
    }

}
