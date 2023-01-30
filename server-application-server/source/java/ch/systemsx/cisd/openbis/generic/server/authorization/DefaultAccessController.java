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

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.reflection.MethodUtils;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleLevel;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;

/**
 * A default <code>IAccessController</code> implementation.
 * 
 * @author Christian Ribeaud
 */
public final class DefaultAccessController implements IAccessController
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DefaultAccessController.class);

    @Private
    static final String MATCHING_ROLE_NOT_FOUND_TEMPLATE =
            "None of method roles '%s' could be found in roles of user '%s'.";

    @Private
    static final String USER_ROLE_ASSIGNMENTS_NOT_FOUND_TEMPLATE =
            "No role assignments could be found for user '%s'.";

    @Private
    static final String METHOD_ROLES_NOT_FOUND_TEMPLATE =
            "No roles have been found for method '%s'.";

    /**
     * Cache for the method roles as they are <code>static</code>.
     */
    private final Map<Method, Set<RoleWithHierarchy>> methodRolesCache =
            new HashMap<Method, Set<RoleWithHierarchy>>();

    private final Map<Method, Map<String, Set<RoleWithHierarchy>>> argumentRolesCache = new HashMap<>();

    private final CapabilityMap capabilities;

    private PredicateExecutor predicateExecutor;

    private IAuthorizationDAOFactory daoFactory;

    public DefaultAccessController(final IAuthorizationDAOFactory daoFactory)
    {
        capabilities = new CapabilityMap(new File("etc/capabilities"), daoFactory.getAuthorizationConfig());
        predicateExecutor = new PredicateExecutor();
        predicateExecutor.setPredicateFactory(new PredicateFactory());
        predicateExecutor.setDAOFactory(daoFactory);
        this.daoFactory = daoFactory;
    }

    public final static List<RoleWithIdentifier> getUserRoles(final PersonPE person)
    {
        Set<RoleAssignmentPE> roleAssignments = person.getAllPersonRoles();
        return extractRolesWithIdentifiers(roleAssignments);
    }

    private static List<RoleWithIdentifier> extractRolesWithIdentifiers(
            Set<RoleAssignmentPE> roleAssignments)
    {
        final List<RoleWithIdentifier> roles = new ArrayList<RoleWithIdentifier>();
        for (final RoleAssignmentPE roleAssignment : roleAssignments)
        {
            roles.add(RoleWithIdentifier.createRole(roleAssignment));
        }
        return roles;
    }

    private final static void logTimeTaken(final StopWatch stopWatch, final Method method)
    {
        stopWatch.stop();
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("Controlling access to method '%s' took %s",
                    MethodUtils.describeMethod(method), stopWatch));
        }
    }

    @Override
    public final Status isAuthorized(final IAuthSession session, final Method method,
            final Argument<?>[] arguments) throws UserFailureException
    {
        assert session != null : "Unspecified session";
        assert method != null : "Unspecified method";
        assert arguments != null : "Unspecified method arguments";
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try
        {
            final Set<RoleWithHierarchy> methodRoles = getMethodRoles(method);
            if (methodRoles.size() == 0)
            {
                String msg = String.format(METHOD_ROLES_NOT_FOUND_TEMPLATE, MethodUtils.describeMethod(method));
                return Status.createError(msg);
            }
            PersonPE person = session.tryGetPerson();
            if (person == null || person.getAllPersonRoles().size() == 0)
            {
                String msg = String.format(USER_ROLE_ASSIGNMENTS_NOT_FOUND_TEMPLATE, session.getUserName());
                return Status.createError(msg);
            }
            final List<RoleWithIdentifier> userRoles = getUserRoles(person);
            Status status = Status.OK;
            if (arguments.length > 0)
            {
                for (final Argument<?> argument : arguments)
                {
                    Set<RoleWithHierarchy> argumentRoles = getArgumentRoles(method, argument, methodRoles);
                    List<RoleWithIdentifier> relevantRoles = getRelevantRoles(userRoles, argumentRoles);
                    relevantRoles = retainConfiguredRoles(person.getUserId(), relevantRoles);
                    status = checkNotEmpty(relevantRoles, argumentRoles, session);

                    if (status.isError())
                    {
                        break;
                    }
                    status = predicateExecutor.evaluate(person, relevantRoles, argument);
                    if (status.isError())
                    {
                        break;
                    }
                }
            } else
            {
                List<RoleWithIdentifier> relevantRoles = getRelevantRoles(userRoles, methodRoles);
                relevantRoles = retainConfiguredRoles(person.getUserId(), relevantRoles);
                status = checkNotEmpty(relevantRoles, methodRoles, session);
            }
            return status;
        } finally
        {
            logTimeTaken(stopWatch, method);
        }
    }

    private Set<RoleWithHierarchy> getMethodRoles(final Method method)
    {
        synchronized (methodRolesCache)
        {
            Set<RoleWithHierarchy> roles = methodRolesCache.get(method);
            if (roles == null)
            {
                roles = new LinkedHashSet<RoleWithHierarchy>();
                for (RoleWithHierarchy role : getRootRoles(method))
                {
                    roles.addAll(role.getRoles());
                }
                methodRolesCache.put(method, roles);
            }
            return roles;
        }
    }

    private Collection<RoleWithHierarchy> getRootRoles(final Method method)
    {
        Collection<RoleWithHierarchy> rootRoles = capabilities.tryGetRoles(method, null);
        if (rootRoles == null)
        {
            final RolesAllowed rolesAllowed = method.getAnnotation(RolesAllowed.class);
            if (rolesAllowed != null)
            {
                rootRoles = Arrays.asList(rolesAllowed.value());
            } else
            {
                rootRoles = Collections.emptyList();
            }
        }
        return rootRoles;
    }

    private Set<RoleWithHierarchy> getArgumentRoles(Method method, Argument<?> argument,
            Set<RoleWithHierarchy> defaultRoles)
    {
        synchronized (argumentRolesCache)
        {
            AuthorizationGuard predicateCandidate = argument.getPredicateCandidate();
            if (predicateCandidate == null)
            {
                return defaultRoles;
            }
            String name = predicateCandidate.name();
            Map<String, Set<RoleWithHierarchy>> map = argumentRolesCache.get(method);
            if (map == null)
            {
                map = new HashMap<>();
                argumentRolesCache.put(method, map);
            }
            Set<RoleWithHierarchy> roles = map.get(name);
            if (roles == null)
            {
                roles = new LinkedHashSet<>();
                for (RoleWithHierarchy role : getRootRoles(method, predicateCandidate, defaultRoles))
                {
                    roles.addAll(role.getRoles());
                }
                map.put(name, Collections.unmodifiableSet(roles));
            }
            return roles;
        }
    }

    private Collection<RoleWithHierarchy> getRootRoles(Method method, AuthorizationGuard predicateCandidate,
            Set<RoleWithHierarchy> defaultRoles)
    {
        Collection<RoleWithHierarchy> roles = capabilities.tryGetRoles(method, predicateCandidate.name());
        if (roles == null)
        {
            RoleWithHierarchy[] rolesAllowed = predicateCandidate.rolesAllowed();
            if (rolesAllowed.length == 0)
            {
                roles = defaultRoles;
            } else
            {
                roles = Arrays.asList(rolesAllowed);
            }
        }
        return roles;
    }

    private Status checkNotEmpty(List<RoleWithIdentifier> relevantRoles, Set<RoleWithHierarchy> argumentRoles,
            IAuthSession session)
    {
        if (relevantRoles.isEmpty() == false)
        {
            return Status.OK;
        }
        final String msg = String.format(MATCHING_ROLE_NOT_FOUND_TEMPLATE, argumentRoles, session.getUserName());
        return Status.createError(msg);
    }

    private List<RoleWithIdentifier> getRelevantRoles(
            final List<RoleWithIdentifier> userRoles, final Set<RoleWithHierarchy> methodOrParameterRoles)
    {
        List<RoleWithIdentifier> result = new ArrayList<>();
        for (RoleWithIdentifier roleWithIdentifier : userRoles)
        {
            if (methodOrParameterRoles.contains(roleWithIdentifier.getRole()))
            {
                result.add(roleWithIdentifier);
            }
        }
        return result;
    }

    private List<RoleWithIdentifier> retainConfiguredRoles(String userId, List<RoleWithIdentifier> roles)
    {
        IAuthorizationConfig config = daoFactory.getAuthorizationConfig();

        if (config.isProjectLevelEnabled() && config.isProjectLevelUser(userId))
        {
            return roles;
        } else
        {
            List<RoleWithIdentifier> nonProjectRoles = new ArrayList<RoleWithIdentifier>();

            for (RoleWithIdentifier role : roles)
            {
                if (false == RoleLevel.PROJECT.equals(role.getRoleLevel()))
                {
                    nonProjectRoles.add(role);
                }
            }

            return nonProjectRoles;
        }
    }

    /**
     * Retains {@link RoleWithIdentifier}s with {@link RoleWithIdentifier#getRole()} included in the set of {@link RoleWithHierarchy}s.
     * 
     * @return retained user roles
     */
    public static List<RoleWithIdentifier> retainMatchingRoleWithIdentifiers(
            final List<RoleWithIdentifier> userRoles, final Set<RoleWithHierarchy> methodRoles)
    {
        Iterator<RoleWithIdentifier> it = userRoles.iterator();
        while (it.hasNext())
        {
            RoleWithIdentifier roleWithIdentifier = it.next();
            if (methodRoles.contains(roleWithIdentifier.getRole()) == false)
            {
                it.remove();
            }
        }
        return userRoles;
    }
}
