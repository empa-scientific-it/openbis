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

package ch.systemsx.cisd.openbis.dss.generic.server.dbbackup;

import ch.systemsx.cisd.dbmigration.DatabaseEngine;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * @author Kaloyan Enimanev
 */
public class BackupDatabaseDescriptionGeneratorTest extends AssertJUnit
{
    @Test
    public void testParser()
    {
        BackupDatabaseDescriptionGenerator generator = new BackupDatabaseDescriptionGenerator();
        generator.process(new String[]
                { getResourceFileName("openBIS-server/jetty/etc/service.properties"),
                        getResourceFileName("datastore_server/etc/service.properties") });

        String username = System.getProperty("user.name");
        String resultTemplate =
                "database=imaging_barkind;username=%s;password=\n"
                        + "database=internal_db;username=%s;password=\n"
                        + "database=openbis_fookind;username=%s;password=";

        String databaseHost = DatabaseEngine.getTestEnvironmentHostOrConfigured(null);
        if (databaseHost != null)
        {
            resultTemplate += ";host=" + databaseHost;
        }

        String expectedResult = String.format(resultTemplate, username, username, username, databaseHost);

        assertEquals(expectedResult, generator.getResult());
    }

    private String getResourceFileName(String fileName)
    {
        return "../datastore_server/resource/test-data/" + getClass().getSimpleName() + "/" + fileName;
    }

}
