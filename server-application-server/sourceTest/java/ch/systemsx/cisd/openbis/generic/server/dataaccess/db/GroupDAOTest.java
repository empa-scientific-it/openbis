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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collections;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISpaceDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * Test cases for corresponding {@link SpaceDAO} class.
 * 
 * @author Franz-Josef Elmer
 */
@Test(groups =
{ "db", "group" })
@Friend(toClasses = SpacePE.class)
public final class GroupDAOTest extends AbstractDAOTest
{
    private static final String NEW_TEST_GROUP = "test-group";

    @Test
    public void testCreateGroup()
    {
        final String groupCode = NEW_TEST_GROUP;
        final SpacePE group = createSpace(groupCode);

        final SpacePE retrievedGroup =
                daoFactory.getSpaceDAO().tryFindSpaceByCode(groupCode);
        AssertJUnit.assertNotNull(retrievedGroup);
        assertEquals(group.getRegistrator(), retrievedGroup.getRegistrator());
    }

    @Test
    public void testListGroups()
    {
        final List<SpacePE> groups = daoFactory.getSpaceDAO().listSpaces();
        Collections.sort(groups);
        assertEquals("CISD", groups.get(0).getCode());
        assertEquals("TEST-SPACE", groups.get(1).getCode());
        assertEquals("TESTGROUP", groups.get(2).getCode());
        assertEquals(3, groups.size());
    }

    @Test
    public void testListGroupsOfHomeDatabaseInstance()
    {
        final List<SpacePE> groups =
                daoFactory.getSpaceDAO().listSpaces();
        Collections.sort(groups);
        assertEquals("CISD", groups.get(0).getCode());
        assertEquals("TEST-SPACE", groups.get(1).getCode());
        assertEquals("TESTGROUP", groups.get(2).getCode());
        assertEquals(3, groups.size());
    }

    @Test(groups = "broken")
    public void testListGroupsOfAnotherDatabaseInstance()
    {
        createSpace("test-group");
        final List<SpacePE> groups = daoFactory.getSpaceDAO().listSpaces();
        assertEquals("TEST-GROUP", groups.get(0).getCode());
        assertEquals(1, groups.size());
    }

    @Test
    public final void testDelete()
    {
        // create new group with no connections
        final String groupCode = NEW_TEST_GROUP;
        createSpace(groupCode);

        final ISpaceDAO groupDAO = daoFactory.getSpaceDAO();
        final SpacePE deletedGroup = findGroup(groupCode);

        // Deleted group should have all collections which prevent it from deletion empty.
        assertTrue(deletedGroup.getProjects().isEmpty());

        // delete
        groupDAO.delete(deletedGroup);

        // test successful deletion of group
        assertNull(groupDAO.tryGetByTechId(TechId.create(deletedGroup)));
    }

    @Test(expectedExceptions = DataIntegrityViolationException.class)
    public final void testDeleteFail()
    {
        final ISpaceDAO groupDAO = daoFactory.getSpaceDAO();
        final SpacePE deletedGroup = findGroup("TESTGROUP");

        // Deleted project should have projects which prevent it from deletion.
        assertFalse(deletedGroup.getProjects().isEmpty());

        // delete
        groupDAO.delete(deletedGroup);
    }

    private final SpacePE findGroup(String code)
    {
        final ISpaceDAO groupDAO = daoFactory.getSpaceDAO();
        final SpacePE group = groupDAO.tryFindSpaceByCode(code);
        assertNotNull(group);

        return group;
    }
}
