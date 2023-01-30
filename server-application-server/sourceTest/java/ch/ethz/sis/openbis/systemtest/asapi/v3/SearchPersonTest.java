/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.testng.Assert.assertEquals;

import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.Me;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.PersonSearchCriteria;

/**
 * @author Franz-Josef Elmer
 */
public class SearchPersonTest extends AbstractTest
{
    @Test
    public void testSearchPersonByUserId()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PersonSearchCriteria searchCriteria = new PersonSearchCriteria();
        searchCriteria.withOrOperator();
        searchCriteria.withUserId().thatStartsWith("observer");
        searchCriteria.withUserId().thatContains("role");
        searchCriteria.withUserId().thatEndsWith("active");
        PersonFetchOptions fetchOptions = new PersonFetchOptions();
        fetchOptions.withSpace();
        fetchOptions.withRoleAssignments().withSpace();
        fetchOptions.withRegistrator();

        // Then
        List<Person> persons = v3api.searchPersons(sessionToken, searchCriteria, fetchOptions).getObjects();

        // When
        assertEquals(renderPersons(persons), "[inactive] inactive, home space:CISD, []\n"
                + "observer, home space:CISD, [SPACE_OBSERVER Space TESTGROUP]\n"
                + "observer_cisd, home space:CISD, [SPACE_ADMIN Space TESTGROUP, SPACE_OBSERVER Space CISD]\n"
                + "test_role, home space:CISD, [SPACE_POWER_USER Space CISD], registrator: test\n");
    }

    @Test
    public void testSearchPersonById()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PersonSearchCriteria searchCriteria = new PersonSearchCriteria();
        searchCriteria.withId().thatEquals(new PersonPermId(TEST_GROUP_OBSERVER));
        PersonFetchOptions fetchOptions = new PersonFetchOptions();
        fetchOptions.withSpace();
        fetchOptions.withRoleAssignments().withSpace();
        fetchOptions.withRegistrator();

        // Then
        List<Person> persons = v3api.searchPersons(sessionToken, searchCriteria, fetchOptions).getObjects();

        // When
        assertEquals(renderPersons(persons), "observer, home space:CISD, [SPACE_OBSERVER Space TESTGROUP]\n");
    }

    @Test
    public void testSearchPersonByMe()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PersonSearchCriteria searchCriteria = new PersonSearchCriteria();
        searchCriteria.withId().thatEquals(new Me());
        PersonFetchOptions fetchOptions = new PersonFetchOptions();
        fetchOptions.withSpace();
        fetchOptions.withRoleAssignments().withSpace();
        fetchOptions.withRegistrator();

        // Then
        List<Person> persons = v3api.searchPersons(sessionToken, searchCriteria, fetchOptions).getObjects();

        // When
        assertEquals(renderPersons(persons), "test, home space:CISD, [INSTANCE_ADMIN, INSTANCE_ETL_SERVER, "
                + "SPACE_ADMIN Space CISD, SPACE_ADMIN Space TESTGROUP, SPACE_ETL_SERVER Space CISD], "
                + "registrator: system\n");
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PersonSearchCriteria c = new PersonSearchCriteria();
        c.withUserId().thatEquals("test");

        PersonFetchOptions fo = new PersonFetchOptions();
        fo.withRoleAssignments();
        fo.withAllWebAppSettings();
        fo.withWebAppSettings("wa");

        v3api.searchPersons(sessionToken, c, fo);

        assertAccessLog(
                "search-persons  SEARCH_CRITERIA:\n'PERSON\n    with attribute 'userId' equal to 'test'\n'\nFETCH_OPTIONS:\n'Person\n    with RoleAssignments\n    with WebAppSettings [wa]\n    with AllWebAppSettings\n'");
    }

}
