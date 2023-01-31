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

package ch.systemsx.cisd.etlserver;

import java.io.File;

import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;

/**
 * Test cases for corresponding {@link ETLDaemon} class.
 * 
 * @author Christian Ribeaud
 */
@Friend(toClasses = ETLDaemon.class)
public final class MainTest extends AbstractFileSystemTestCase
{
    private static final String GROUP_PREFIX = "Space_";

    private static final String INSTANCE_PREFIX = "Instance_";

    private final static DatabaseInstance createDatabaseInstance()
    {
        final DatabaseInstance databaseInstancePE = new DatabaseInstance();
        databaseInstancePE.setCode("XXX");
        databaseInstancePE.setUuid("1111-2222");
        return databaseInstancePE;
    }

    @Test
    public final void testMigrateStoreRootDir()
    {
        final File instanceDir =
                new File(new File(workingDirectory, INSTANCE_PREFIX + "CISD"), GROUP_PREFIX
                        + "CISD");
        instanceDir.mkdirs();
        assertTrue(instanceDir.exists());
        final DatabaseInstance databaseInstancePE = createDatabaseInstance();
        // Not same code
        ETLDaemon.migrateStoreRootDir(workingDirectory, databaseInstancePE);
        assertTrue(instanceDir.exists());
        databaseInstancePE.setCode("CISD");
        // Same code
        ETLDaemon.migrateStoreRootDir(workingDirectory, databaseInstancePE);
        assertFalse(instanceDir.exists());
        assertTrue(new File(workingDirectory, INSTANCE_PREFIX + databaseInstancePE.getUuid())
                .exists());
        // Trying again does not change anything
        ETLDaemon.migrateStoreRootDir(workingDirectory, databaseInstancePE);
        assertFalse(instanceDir.exists());
        assertTrue(new File(workingDirectory, INSTANCE_PREFIX + databaseInstancePE.getUuid())
                .exists());
    }
}
