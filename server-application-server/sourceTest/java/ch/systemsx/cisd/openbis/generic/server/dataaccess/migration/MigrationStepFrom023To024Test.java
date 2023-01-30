/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.migration;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.migration.MigrationStepFrom023To024.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.migration.MigrationStepFrom023To024.ExternalData;

/**
 * Test cases for the {@link MigrationStepFrom023To024}.
 * 
 * @author Christian Ribeaud
 */
public final class MigrationStepFrom023To024Test
{

    @Test
    public final void testGetNewLocation()
    {
        final DatabaseInstance databaseInstance = new DatabaseInstance("DB1", "111-222");
        final ExternalData externalData = new ExternalData(1L, "Instance_DB1/Group_G/Project_P");
        final String newLocation =
                MigrationStepFrom023To024.getNewLocation(externalData, databaseInstance);
        assertEquals("Instance_111-222/Group_G/Project_P", newLocation);
    }
}
