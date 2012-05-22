/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

import org.springframework.test.annotation.Rollback;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityOperationsLogDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityOperationsLogEntryPE;

/**
 * Test cases for {@link EntityOperationsLogDAO}.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
@Test(groups =
    { "db" })
public final class EntityOperationsLogDAOTest extends AbstractDAOTest
{

    @Test
    @Rollback(false)
    public void testCreateLogEntry()
    {
        final IEntityOperationsLogDAO eaolDao = daoFactory.getEntityOperationsLogDAO();
        eaolDao.addLogEntry(new Long(1));
    }

    @Test(dependsOnMethods = "testCreateLogEntry")
    public void testFindLogEntry()
    {
        final IEntityOperationsLogDAO eaolDao = daoFactory.getEntityOperationsLogDAO();
        EntityOperationsLogEntryPE logEntry = eaolDao.tryFindLogEntry(new Long(1));
        assertNotNull("Could not find the log entry we created in testCreateLogEntry", logEntry);
    }

    @Test(dependsOnMethods = "testCreateLogEntry")
    public void testFindNonExistentLogEntry()
    {
        final IEntityOperationsLogDAO eaolDao = daoFactory.getEntityOperationsLogDAO();
        EntityOperationsLogEntryPE logEntry = eaolDao.tryFindLogEntry(new Long(2));
        assertNull("Should not have found a non-existent log entry", logEntry);

    }

    @Test(dependsOnMethods = "testCreateLogEntry")
    public void testCreateDuplicateLogEntry()
    {
        final IEntityOperationsLogDAO eaolDao = daoFactory.getEntityOperationsLogDAO();
        eaolDao.addLogEntry(new Long(1));
    }

}