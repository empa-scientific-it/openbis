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

package ch.systemsx.cisd.openbis.plugin.screening.dbbackup;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.dss.etl.ImagingDatabaseVersionHolder;
import ch.systemsx.cisd.openbis.dss.generic.server.dbbackup.BackupDatabaseDescriptionGenerator;

/**
 * @author Kaloyan Enimanev
 */
public class BackupDatabaseDescriptionTest extends AssertJUnit
{
    @Test
    public void testVerifyImagingDbVersionHolder()
    {
        assertEquals(
                "The HCS imaging database won't be backed up correctly if this assertion fails.",
                BackupDatabaseDescriptionGenerator.HCS_IMAGING_DB_VERSION_HOLDER,
                ImagingDatabaseVersionHolder.class.getName());

    }

}
