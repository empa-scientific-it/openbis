/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.Rollback;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ColumnSetting;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * Test cases for corresponding {@link PersonDAO} class.
 * 
 * @author Christian Ribeaud
 */
@Test(groups = { "db", "person" })
public final class PersonDAOTest extends AbstractDAOTest
{
    public final static String getTestUserId()
    {
        return "ribose".toUpperCase();
    }

    public final static PersonPE createPerson()
    {
        return createPerson(2L, getTestUserId());
    }

    public final static PersonPE createPerson(long id, String userId)
    {
        final PersonPE person = new PersonPE();
        person.setId(id);
        person.setFirstName("Christian");
        person.setLastName("Ribeaud");
        person.setEmail("christian.ribeaud@systemsx.ch");
        person.setUserId(userId);
        return person;
    }

    @Test
    @Rollback(false)
    public final void testCreatePerson()
    {
        final IPersonDAO personDAO = daoFactory.getPersonDAO();
        final PersonPE testPerson = createPerson(2L, getTestUserId().toUpperCase());
        final PersonPE testPerson2 = createPerson(3L, getTestUserId().toLowerCase());
        final PersonPE testPerson3 = createPerson(4L, getTestUserId() + "X");
        try
        {
            // Try with <code>null</code>
            personDAO.createPerson(null);
            fail("AssertionError expected");
        } catch (final AssertionError e)
        {
            assertEquals("Given person can not be null.", e.getMessage());
        }
        personDAO.createPerson(testPerson);
        personDAO.createPerson(testPerson2);
        personDAO.createPerson(testPerson3);
        final List<PersonPE> persons = personDAO.listPersons();
        assertEquals(24, persons.size());
        final PersonPE testPersonFromDB = personDAO.getPerson(testPerson.getId());
        assertEquals(testPerson, testPersonFromDB);
        final PersonPE testPersonFromDB2 = personDAO.getPerson(testPerson2.getId());
        assertEquals(testPerson2, testPersonFromDB2);
    }

    @Test
    public final void testCreatePersonWithValidationFailed_longUserId()
    {
        final IPersonDAO personDAO = daoFactory.getPersonDAO();
        final PersonPE testPerson = createPerson();
        testPerson.setUserId(StringUtils.repeat("A", 257));
        // User id too long
        try
        {
            personDAO.createPerson(testPerson);
            fail("User id exceeds the maximum length");
        } catch (final DataIntegrityViolationException ex)
        {
            assertTrue(ex.getMessage().indexOf("is too long") > -1);
        }
    }

    @Test
    public final void testCreatePersonWithValidationFailed_longEmail()
    {
        final IPersonDAO personDAO = daoFactory.getPersonDAO();
        final PersonPE testPerson = createPerson();
        testPerson.setEmail(StringUtils.repeat("A", 64) + "@" + StringUtils.repeat("A", 255) + ".ch");
        // email too long
        try
        {
            personDAO.createPerson(testPerson);
            fail("Email exceeds the maximum length");
        } catch (final DataIntegrityViolationException ex)
        {
            assertTrue(ex.getMessage().indexOf("is too long") > -1);
        }
    }

    @Test
    public final void testCreatePersonWithWhiteSpacesInEmail()
    {
        final IPersonDAO personDAO = daoFactory.getPersonDAO();
        final PersonPE testPerson = createPerson();
        testPerson.setUserId("myUserId");
        // White spaces in email address
        testPerson.setEmail("h@g.com ");
        personDAO.createPerson(testPerson);
    }

    @Test(dependsOnMethods = "testCreatePerson")
    public final void testTryFindPersonIdByUserID()
    {
        final PersonPE testPerson = createPerson(2L, getTestUserId());
        final PersonPE testPerson2 = createPerson(4L, getTestUserId() + "X");
        final IPersonDAO personDAO = daoFactory.getPersonDAO();
        boolean fail = true;
        try
        {
            personDAO.tryFindPersonByUserId(null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertEquals(false, fail);
        // Get a person given its user Id.
        // This user exists only once regardless of capitalization:
        final String mangledUserId = StringUtils.capitalize(testPerson2.getUserId().toLowerCase());
        final PersonPE person = personDAO.tryFindPersonByUserId(mangledUserId);
        assertNotNull(person.getId());
        assertFalse(mangledUserId.equals(person.getUserId()));
        assertTrue(mangledUserId.toLowerCase().equals(person.getUserId().toLowerCase()));
        // This user exists twice with different capitalization, but we choose a user id that exists
        // exactly
        final PersonPE person2 = personDAO.tryFindPersonByUserId(testPerson.getUserId());
        assertNotNull(person2.getId());
    }

    @Test(dependsOnMethods = "testCreatePerson")
    public final void testTryFindPersonIdByEmail()
    {
        final PersonPE testPerson = createPerson();
        final IPersonDAO personDAO = daoFactory.getPersonDAO();
        boolean fail = true;
        try
        {
            personDAO.tryFindPersonByEmail(null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertEquals(false, fail);
        // Get a person given its user Id.
        final Long id = personDAO.tryFindPersonByEmail(testPerson.getEmail()).getId();
        assertNotNull(id);
    }

    @Test
    public final void testListPersons()
    {
        final IPersonDAO personDAO = daoFactory.getPersonDAO();
        final List<PersonPE> list = personDAO.listPersons();
        assertTrue(list.size() > 0);
    }

    @Test
    public final void testListAndCountActivePersons()
    {
        final IPersonDAO personDAO = daoFactory.getPersonDAO();
        final List<PersonPE> listAll = personDAO.listPersons();
        final List<PersonPE> listActive = personDAO.listActivePersons();
        assertEquals(listAll.size() - 1, listActive.size());

        assertEquals(listActive.size(), personDAO.countActivePersons());
    }

    @Test
    public final void testGetPersonForId()
    {
        final IPersonDAO personDAO = daoFactory.getPersonDAO();
        try
        {
            personDAO.getPerson(100L);
            fail("Given id does not exist.");
        } catch (final DataAccessException ex)
        {
            // Nothing to do here.
        }
        final PersonPE personDTO = personDAO.listPersons().get(0);
        final Long id = personDTO.getId();
        assertEquals(personDTO, personDAO.getPerson(id));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testUpdate()
    {
        IPersonDAO personDAO = daoFactory.getPersonDAO();
        List<PersonPE> persons = personDAO.listPersons();
        assertEquals(false, persons.isEmpty());

        PersonPE person = persons.get(0);
        assertEquals(0, person.getDisplaySettings().getColumnSettings().size());

        DisplaySettings displaySettings = new DisplaySettings();
        ColumnSetting columnSetting = new ColumnSetting();
        columnSetting.setColumnID("column1");
        displaySettings.getColumnSettings().put("id", Arrays.asList(columnSetting));
        person.setDisplaySettings(displaySettings);

        personDAO.updatePerson(person);

        PersonPE reloadedPerson = personDAO.tryFindPersonByUserId(person.getUserId());
        displaySettings = reloadedPerson.getDisplaySettings();
        Map<String, List<ColumnSetting>> columnSettings = displaySettings.getColumnSettings();
        List<ColumnSetting> settings = columnSettings.get("id");
        assertEquals(columnSetting.getColumnID(), settings.get(0).getColumnID());
    }
}