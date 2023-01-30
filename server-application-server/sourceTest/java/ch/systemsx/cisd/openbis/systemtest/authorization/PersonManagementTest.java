/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest.authorization;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Level;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.openbis.generic.server.CommonServer;
import ch.systemsx.cisd.openbis.generic.server.ServiceForDataStoreServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.plugin.generic.server.GenericServer;
import ch.systemsx.cisd.openbis.plugin.query.server.QueryServer;
import ch.systemsx.cisd.openbis.systemtest.base.BaseTest;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * This test tests registering and deactivating a person. It also tests authorization of {@link CommonServer}, {@link ServiceForDataStoreServer},
 * {@link GenericServer}, and {@link QueryServer}.
 * 
 * @author Franz-Josef Elmer
 */
public class PersonManagementTest extends BaseTest
{
    private BufferedAppender logRecorder;

    private int initialNumberOfActivePersons;

    @BeforeMethod
    public void setUpLogger()
    {
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.INFO, ".*AUTH.CommonServer");
        initialNumberOfActivePersons = etlService.countActivePersons(systemSessionToken);

    }

    @AfterMethod
    public void resetLogger()
    {
        logRecorder.reset();
    }

    @Test
    public void testForCommonServerCountActivePersons()
    {
        String sessionToken = create(aSession().withInstanceRole(RoleWithHierarchy.INSTANCE_ADMIN));

        int numberOfActivePersons = commonServer.countActivePersons(sessionToken);

        assertEquals(1, numberOfActivePersons - initialNumberOfActivePersons);
    }

    @Test
    public void testForETLServiceCountActivePersons()
    {
        String sessionToken = create(aSession().withInstanceRole(RoleWithHierarchy.INSTANCE_ADMIN));

        int numberOfActivePersons = etlService.countActivePersons(sessionToken);

        assertEquals(1, numberOfActivePersons - initialNumberOfActivePersons);
    }

    @Test
    public void testForGenericServerCountActivePersons()
    {
        String sessionToken = create(aSession().withInstanceRole(RoleWithHierarchy.INSTANCE_ADMIN));

        int numberOfActivePersons = genericServer.countActivePersons(sessionToken);

        assertEquals(1, numberOfActivePersons - initialNumberOfActivePersons);
    }

    @Test
    public void testForQueryServerCountActivePersons()
    {
        String sessionToken = create(aSession().withInstanceRole(RoleWithHierarchy.INSTANCE_ADMIN));

        int numberOfActivePersons = queryServer.countActivePersons(sessionToken);

        assertEquals(1, numberOfActivePersons - initialNumberOfActivePersons);
    }

    @Test(expectedExceptions =
    { AuthorizationFailureException.class })
    public void testForCommonServerCountActivePersonsFailedBecauseOfInsufficientAuthorization()
    {
        String sessionToken =
                create(aSession().withInstanceRole(RoleWithHierarchy.INSTANCE_OBSERVER));

        commonServer.countActivePersons(sessionToken);
    }

    @Test(expectedExceptions =
    { AuthorizationFailureException.class })
    public void testForETLServiceCountActivePersonsFailedBecauseOfInsufficientAuthorization()
    {
        String sessionToken =
                create(aSession().withInstanceRole(RoleWithHierarchy.INSTANCE_OBSERVER));

        etlService.countActivePersons(sessionToken);
    }

    @Test(expectedExceptions =
    { AuthorizationFailureException.class })
    public void testForGenericServerCountActivePersonsFailedBecauseOfInsufficientAuthorization()
    {
        String sessionToken =
                create(aSession().withInstanceRole(RoleWithHierarchy.INSTANCE_OBSERVER));

        genericServer.countActivePersons(sessionToken);
    }

    @Test(expectedExceptions =
    { AuthorizationFailureException.class })
    public void testForQueryServerCountActivePersonsFailedBecauseOfInsufficientAuthorization()
    {
        String sessionToken =
                create(aSession().withInstanceRole(RoleWithHierarchy.INSTANCE_OBSERVER));

        queryServer.countActivePersons(sessionToken);
    }

    @Test
    public void testDeactivatePersons()
    {
        String sessionToken = create(aSession().withInstanceRole(RoleWithHierarchy.INSTANCE_ADMIN));

        commonServer.deactivatePersons(sessionToken, Arrays.asList("system"));

        assertEquals(0, commonServer.countActivePersons(sessionToken) - initialNumberOfActivePersons);
        try
        {
            commonServer.tryAuthenticate("system", "password");
            fail("UserFailureException expected");
        } catch (UserFailureException e)
        {
            assertEquals("User 'system' has no role assignments and thus is not permitted to login.", e.getMessage());
        }
        assertEquals("INFO  AUTH.CommonServer - User 'system' has no role assignments "
                + "and thus is not permitted to login.", logRecorder.getLogContent());
    }

    @Test
    public void testRegisterPerson()
    {
        String sessionToken = create(aSession().withInstanceRole(RoleWithHierarchy.INSTANCE_ADMIN));

        commonServer.registerPerson(sessionToken, "einstein");

        List<Person> persons = commonServer.listPersons(sessionToken);
        assertPersonExists(persons, "einstein");
    }

    @Test(expectedExceptions =
    { AuthorizationFailureException.class })
    public void testRegisterPersonFailedBecauseOfInsufficientAuthorization()
    {
        String sessionToken =
                create(aSession().withInstanceRole(RoleWithHierarchy.INSTANCE_OBSERVER));

        commonServer.registerPerson(sessionToken, "einstein");
    }

    @Test
    public void testSetSessionUser()
    {
        String sessionToken = create(aSession().withInstanceRole(RoleWithHierarchy.INSTANCE_ADMIN));
        commonServer.registerPerson(sessionToken, "einstein");

        commonServer.setSessionUser(sessionToken, "einstein");

        try
        {
            commonServer.listPersons(sessionToken);
            fail("AuthorizationFailureException expected");
        } catch (AuthorizationFailureException ex)
        {
            assertEquals("Authorization failure: ERROR: \"No role assignments could be found "
                    + "for user 'einstein'.\".", ex.getMessage());
        }
    }

    @Test(expectedExceptions =
    { AuthorizationFailureException.class })
    public void testSetSessionUserFailedBecauseOfInsufficientAuthorization()
    {
        String sessionToken =
                create(aSession().withInstanceRole(RoleWithHierarchy.INSTANCE_OBSERVER));

        commonServer.setSessionUser(sessionToken, "system");
    }

    private void assertPersonExists(List<Person> persons, String userID)
    {
        Set<String> userIDs = new TreeSet<String>();
        for (Person person : persons)
        {
            String userId = person.getUserId();
            userIDs.add(userId);
            if (userId.equals(userID))
            {
                return;
            }
        }
        fail("Person '" + userID + "' does not exist: " + userIDs);
    }
}
