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

import ch.systemsx.cisd.openbis.generic.server.dataaccess.migration.MigrationStepFrom025To026.ExternalData;

/**
 * Test cases for the {@link MigrationStepFrom025To026}.
 * 
 * @author Izabela Adamczyk
 */
public final class MigrationStepFrom025To026Test
{

    @Test
    public final void testGetNewLocationWithRealLocation()
    {

        final ExternalData externalData =
                new ExternalData(
                        1L,
                        "Instance_C199AD7D-5AC0-4DE1-BC19-682DE43CD0CA/Group_CISD/Project_NEMO/Experiment_EXP1/ObservableType_HCS_IMAGE/Sample_3VCP1/Dataset_microX-3VCP1");
        final String newLocation = MigrationStepFrom025To026.getNewLocation(externalData.location);
        assertEquals(
                "Instance_C199AD7D-5AC0-4DE1-BC19-682DE43CD0CA/Group_CISD/Project_NEMO/Experiment_EXP1/DataSetType_HCS_IMAGE/Sample_3VCP1/Dataset_microX-3VCP1",
                newLocation);
    }

    @Test
    public final void testGetNewLocationNoObservableType()
    {

        String location = "Instance_DB1/Group_G/Project_P/NotObservableType_XXX";
        final ExternalData externalData = new ExternalData(1L, location);
        assertEquals(location, MigrationStepFrom025To026.getNewLocation(externalData.location));
    }

    @Test
    public final void testGetNewLocationAlreadyMigrated()
    {

        String location = "Instance_DB1/Group_G/Project_P/DataSetType_XXX";
        final ExternalData externalData = new ExternalData(1L, location);
        assertEquals(location, MigrationStepFrom025To026.getNewLocation(externalData.location));
    }

    @Test
    public final void testGetNewLocationDuplicatedObservableType()
    {

        final ExternalData externalData =
                new ExternalData(1L,
                        "Instance_DB1/Group_G/Project_P/ObservableType_XXX/ObservableType_XXX/");
        final String newLocation = MigrationStepFrom025To026.getNewLocation(externalData.location);
        assertEquals("Instance_DB1/Group_G/Project_P/DataSetType_XXX/ObservableType_XXX/",
                newLocation);
    }
}
