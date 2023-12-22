/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.server;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IRoleAssignmentDAO;
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * @author Kaloyan Enimanev
 */
public class AbstractServerTest extends AssertJUnit
{
    private final String USERNAME = "username";

    private final String ETL_SERVER = "etlserver";

    private final String PASSWORD = "password";

    private final String SESSION_TOKEN = "sessionToken";

    private Mockery context;

    private AbstractServer<Object> server;

    private IOpenBisSessionManager sessionManager;

    private IDAOFactory daoFactory;

    private IPersonDAO personDAO;

    private IRoleAssignmentDAO roleAssigmentDAO;

    @SuppressWarnings(
    { "cast", "unchecked" })
    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        sessionManager = (IOpenBisSessionManager) context.mock(IOpenBisSessionManager.class);
        daoFactory = context.mock(IDAOFactory.class);
        personDAO = context.mock(IPersonDAO.class);
        roleAssigmentDAO = context.mock(IRoleAssignmentDAO.class);

        server = new AbstractServer<Object>(sessionManager, daoFactory, null)
            {

                @Override
                public Object createLogger(IInvocationLoggerContext loggerContext)
                {
                    return null;
                }

            };
        server.setDisplaySettingsProvider(new DisplaySettingsProvider());

        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getPersonDAO();
                    will(returnValue(personDAO));

                    allowing(daoFactory).getRoleAssignmentDAO();
                    will(returnValue(roleAssigmentDAO));
                }
            });
    }

    @AfterMethod
    public void afterMethod(Method method)
    {
        try
        {
            context.assertIsSatisfied();
        } catch (Error err)
        {
            fail(method.getName() + ": " + err.getMessage());
        }
    }

    @Test
    public void testFirstLoggedUserGetsAdminRoleAssignment()
    {
        prepareLoginExpectations(USERNAME, PASSWORD);

        final RecordingMatcher<RoleAssignmentPE> matcher = new RecordingMatcher<RoleAssignmentPE>();
        context.checking(new Expectations()
        {
            {
                one(personDAO).tryFindPersonByUserId(with(USERNAME));
                will(returnValue(null));
                one(personDAO).lock(with((PersonPE) null));

                one(personDAO).listPersons();
                List<PersonPE> personsList =
                        Arrays.asList(createSystemUser(), createUser(ETL_SERVER));
                will(returnValue(personsList));

                one(roleAssigmentDAO).createRoleAssignment(with(matcher));
            }
        });
        SessionContextDTO session = server.tryAuthenticate(USERNAME, PASSWORD);
        assertNotNull(session);
        RoleAssignmentPE roleAssigment = matcher.recordedObject();
        assertEquals(USERNAME, roleAssigment.getPerson().getUserId());
        assertEquals(RoleCode.ADMIN, roleAssigment.getRole());

    }

    @Test
    public void testSecondLoggedUserGetsNoRoleAssignment()
    {
        prepareLoginExpectations(USERNAME, PASSWORD);

        context.checking(new Expectations()
        {
            {
                one(personDAO).tryFindPersonByUserId(with(USERNAME));
                will(returnValue(null));
                one(personDAO).lock(with((PersonPE) null));

                one(personDAO).listPersons();
                List<PersonPE> personsList =
                        Arrays.asList(createSystemUser(), createUser("test-admin-user"));
                will(returnValue(personsList));
            }
        });
        assertNotAuthorized(USERNAME);
    }

    @Test
    public void testFirstLoggedEtlServerGetsRoleAssignment()
    {
        prepareLoginExpectations(ETL_SERVER, PASSWORD);

        final RecordingMatcher<RoleAssignmentPE> matcher = new RecordingMatcher<RoleAssignmentPE>();
        context.checking(new Expectations()
        {
            {
                one(personDAO).tryFindPersonByUserId(with(ETL_SERVER));
                will(returnValue(null));
                one(personDAO).lock(with((PersonPE) null));

                one(personDAO).listPersons();
                List<PersonPE> personsList =
                        Arrays.asList(createSystemUser(), createUser("admin"));
                will(returnValue(personsList));

                one(roleAssigmentDAO).createRoleAssignment(with(matcher));
            }
        });
        SessionContextDTO session = server.tryAuthenticate(ETL_SERVER, PASSWORD);
        assertNotNull(session);
        RoleAssignmentPE roleAssigment = matcher.recordedObject();
        assertEquals(ETL_SERVER, roleAssigment.getPerson().getUserId());
        assertEquals(RoleCode.ETL_SERVER, roleAssigment.getRole());
    }

    @Test
    public void testSecondLoggedEtlServerGetsNoRoleAssignment()
    {
        prepareLoginExpectations(ETL_SERVER, PASSWORD);

        context.checking(new Expectations()
        {
            {
                one(personDAO).tryFindPersonByUserId(with(ETL_SERVER));
                will(returnValue(null));
                one(personDAO).lock(with((PersonPE) null));

                one(personDAO).listPersons();
                List<PersonPE> personsList =
                        Arrays.asList(createSystemUser(), createUser("etlserver1"));
                will(returnValue(personsList));
            }
        });
        assertNotAuthorized(ETL_SERVER);
    }

    private void assertNotAuthorized(String user)
    {
        try
        {
            server.tryAuthenticate(user, PASSWORD);
            fail("UserFailureException expected");
        } catch (UserFailureException e)
        {
            assertEquals("User '" + user + "' has no role assignments and thus is not permitted to login.", e.getMessage());
        }
    }

    private void prepareLoginExpectations(final String username, final String password)
    {
        context.checking(new Expectations()
            {
                {
                    one(sessionManager).tryToOpenSession(username, password);
                    will(returnValue(SESSION_TOKEN));

                    Principal principal =
                            new Principal(username, "firstname", "lastname", "email@email.ch");
                    Session session =
                            new Session(username, SESSION_TOKEN, principal, "localhost", 1);
                    one(sessionManager).getSession(SESSION_TOKEN);
                    will(returnValue(session));

                    one(personDAO).tryFindPersonByUserId(username);
                    will(returnValue(null));

                    PersonPE person = new PersonPE();
                    person.setUserId(username);
                    one(personDAO).createPerson(person);

                    allowing(personDAO).tryFindPersonByUserId(PersonPE.SYSTEM_USER_ID);
                    will(returnValue(createSystemUser()));
                }
            });

    }

    private PersonPE createSystemUser()
    {
        return createUser(PersonPE.SYSTEM_USER_ID);
    }

    private PersonPE createUser(String userId)
    {
        PersonPE result = new PersonPE();
        result.setUserId(userId);
        result.setActive(true);
        return result;
    }
}
