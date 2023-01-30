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

package ch.systemsx.cisd.openbis.generic.server.authorization;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.aop.Advisor;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISpaceDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * Utility methods for {@link AuthorizationAdvisor}. Can be used to test authorization of concrete methods.
 * 
 * @author Tomasz Pylak
 */
public final class AuthorizationTestUtil
{
    private AuthorizationTestUtil()
    {
        // Can not be instantiated.
    }

    /**
     * Creates an instance of the specified proxy interface and wraps it with authorization advisor.
     * <p>
     * Sets expectations for the database calls about existing groups and database instances.
     * </p>
     */
    public final static <T> T createAndPrepareAuthorizationProxy(final Class<T> proxyInterface,
            final Mockery context, final List<SpacePE> groups, final String homeDbCode,
            final String dbCode)
    {
        final IAuthorizationDAOFactory daoFactory =
                context.mock(IAuthorizationDAOFactory.class, "authorization IDAOFactory mock");
        final T proxyInstance =
                createAuthorizationProxy(createUncheckedMock(proxyInterface), daoFactory);
        final ISpaceDAO groupDAO = context.mock(ISpaceDAO.class, "authorization IGroupDAO mock");
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getSpaceDAO();
                    will(returnValue(groupDAO));

                    allowing(groupDAO).listSpaces();
                    will(returnValue(groups));
                }
            });
        return proxyInstance;
    }

    /**
     * Wraps it with authorization advisor.
     * <p>
     * Sets expectations for the database calls about existing groups and database instances.
     * </p>
     */
    public final static <T> T prepareAuthorizationProxy(final T proxyInstance,
            final Mockery context, final List<SpacePE> groups, final String homeDbCode,
            final String... otherDatabasesCodes)
    {
        final IAuthorizationDAOFactory daoFactory =
                context.mock(IAuthorizationDAOFactory.class, "authorizarion IDAOFactory mock");
        final T newProxyInstance = createAuthorizationProxy(proxyInstance, daoFactory);
        prepareAuthorizationExpectations(context, daoFactory, groups, homeDbCode,
                otherDatabasesCodes);
        return newProxyInstance;
    }

    /** Creates an instance of the specified proxy interface and wraps it with authorization advisor. */
    private final static <T> T createAuthorizationProxy(final T proxyInstance,
            final IAuthorizationDAOFactory daoFactory)
    {
        final Advisor authorizationAdvisor =
                new AuthorizationAdvisor(new ActiveAuthorization(daoFactory));
        return wrapWithAdvisor(proxyInstance, authorizationAdvisor);
    }

    // creates a proxy of the manager using specified advisor
    private static <T> T wrapWithAdvisor(final T proxyInstance, final Advisor advisor)
    {
        final BeanPostProcessor processor = new AbstractAutoProxyCreator()
            {
                private static final long serialVersionUID = 1L;

                //
                // AbstractAutoProxyCreator
                //

                @SuppressWarnings("rawtypes")
                @Override
                protected final Object[] getAdvicesAndAdvisorsForBean(final Class beanClass,
                        final String beanName, final TargetSource customTargetSource)
                        throws BeansException
                {
                    return new Object[]
                    { advisor };
                }
            };
        final Object proxy =
                processor.postProcessAfterInitialization(proxyInstance, "proxy of "
                        + proxyInstance.getClass().getName());
        return cast(proxy);
    }

    /**
     * Use this method to create a mock of an interface when you do not want to specify which methods are expected to be called.
     */
    private final static <T> T createUncheckedMock(final Class<T> mockInterface)
    {
        final InvocationHandler emptyHandler = new InvocationHandler()
            {

                //
                // InvocationHandler
                //

                @Override
                public final Object invoke(final Object proxy, final Method method,
                        final Object[] args) throws Throwable
                {
                    return null;
                }
            };
        final Class<?>[] interfaces = new Class<?>[]
        { mockInterface };
        return cast(Proxy
                .newProxyInstance(mockInterface.getClassLoader(), interfaces, emptyHandler));
    }

    @SuppressWarnings("unchecked")
    private final static <T> T cast(final Object proxy)
    {
        return (T) proxy;
    }

    // ----------------

    private final static void prepareAuthorizationExpectations(final Mockery context,
            final IAuthorizationDAOFactory daoFactory, final List<SpacePE> groups,
            final String homeDbCode, final String... otherDatabasesCodes)
    {
        prepareAuthorizationCalls(context, daoFactory, groups);
    }

    private final static void prepareAuthorizationCalls(final Mockery context,
            final IAuthorizationDAOFactory daoFactory, final List<SpacePE> groups)
    {
        final ISpaceDAO groupDAO = context.mock(ISpaceDAO.class, "authorizarion IGroupDAO mock");
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getSpaceDAO();
                    will(returnValue(groupDAO));

                    allowing(groupDAO).listSpaces();
                    will(returnValue(groups));
                }
            });
    }

    public static IAuthSession createSession(final RoleAssignmentPE... roleAssignments)
    {
        final PersonPE person = new PersonPE();
        person.setUserId("john_doe");
        person.setFirstName("John");
        person.setLastName("Doe");
        person.setEmail("John.Doe@group.org");
        person.setId(Long.valueOf(42));
        person.setRoleAssignments(new HashSet<RoleAssignmentPE>(Arrays.asList(roleAssignments)));
        return new IAuthSession()
            {
                private static final long serialVersionUID = 1L;

                @Override
                public PersonPE tryGetPerson()
                {
                    return person;
                }

                @Override
                public PersonPE tryGetCreatorPerson()
                {
                    return person;
                }

                @Override
                public String tryGetHomeGroupCode()
                {
                    SpacePE homeGroup = person.getHomeSpace();
                    return homeGroup == null ? null : homeGroup.getCode();
                }

                @Override
                public String getUserName()
                {
                    return person.getFirstName() + " " + person.getLastName();
                }

            };
    }

    public final static RoleAssignmentPE createInstanceRoleAssignment(final RoleCode roleCode,
            final String instanceCode)
    {
        final RoleAssignmentPE roleAssignment = new RoleAssignmentPE();
        roleAssignment.setRole(roleCode);
        return roleAssignment;
    }

    public final static RoleAssignmentPE createGroupRoleAssignment(final RoleCode roleCode,
            final String instanceCode, final String groupCode)
    {
        final RoleAssignmentPE roleAssignment = new RoleAssignmentPE();
        roleAssignment.setRole(roleCode);
        final SpacePE group = createGroup(instanceCode, groupCode);
        roleAssignment.setSpace(group);
        return roleAssignment;
    }

    public final static SpacePE createGroup(final String dbCode, final String groupCode)
    {
        final SpacePE group = new SpacePE();
        group.setCode(CodeConverter.tryToDatabase(groupCode));
        return group;
    }

}
