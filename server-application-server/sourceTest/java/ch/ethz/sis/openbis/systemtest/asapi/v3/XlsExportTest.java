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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class XlsExportTest extends AbstractTest
{

    protected String sessionToken;

    @BeforeMethod
    public void beforeTest()
    {
        sessionToken = v3api.login(TEST_USER, PASSWORD);
    }

    @AfterMethod
    public void afterTest()
    {
        v3api.logout(sessionToken);
    }

    @Test
    public void testDataExport()
    {
        // TODO: implement
    }

}
