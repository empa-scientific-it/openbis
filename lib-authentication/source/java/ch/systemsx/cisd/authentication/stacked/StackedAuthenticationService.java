/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.authentication.stacked;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;

/**
 * An authentication service that uses a list of delegate authentication services to authenticate a user. The first authentication service that can
 * authenticate a user wins.
 * 
 * @author Bernd Rinn
 */
public class StackedAuthenticationService implements IAuthenticationService
{
    private final List<IAuthenticationService> delegates;

    private final boolean remote;

    private final boolean supportsListingByUserId;

    private final boolean supportsListingByEmail;

    private final boolean supportsListingByLastName;

    private final boolean supportsAuthenticatingByEmail;

    public StackedAuthenticationService(List<IAuthenticationService> authenticationServices)
    {
        this.delegates = filterConfigured(authenticationServices);
        boolean foundRemote = false;
        boolean foundSupportsListingByUserId = false;
        boolean foundSupportsListingByEmail = false;
        boolean foundSupportsListingByLastName = false;
        boolean foundSupportsAuthenticateByEmail = false;
        for (IAuthenticationService service : delegates)
        {
            foundRemote |= service.isRemote();
            foundSupportsListingByUserId |= service.supportsListingByUserId();
            foundSupportsListingByEmail |= service.supportsListingByEmail();
            foundSupportsListingByLastName |= service.supportsListingByLastName();
            foundSupportsAuthenticateByEmail |= service.supportsAuthenticatingByEmail();
        }
        this.remote = foundRemote;
        this.supportsListingByUserId = foundSupportsListingByUserId;
        this.supportsListingByEmail = foundSupportsListingByEmail;
        this.supportsListingByLastName = foundSupportsListingByLastName;
        this.supportsAuthenticatingByEmail = foundSupportsAuthenticateByEmail;
    }

    private static List<IAuthenticationService> filterConfigured(
            List<IAuthenticationService> services)
    {
        final List<IAuthenticationService> configuredServices =
                new ArrayList<IAuthenticationService>(services.size());
        for (IAuthenticationService service : services)
        {
            if (service.isConfigured())
            {
                configuredServices.add(service);
            }
        }
        return configuredServices;
    }

    @Override
    public String authenticateApplication()
    {
        return "DUMMY-TOKEN";
    }

    @Override
    public boolean authenticateUser(String dummyToken, String user, String password)
    {
        return authenticateUser(user, password);
    }

    @Override
    public boolean authenticateUser(String user, String password)
    {
        final Principal principalOrNull = tryGetAndAuthenticateUser(user, password);
        return Principal.isAuthenticated(principalOrNull);
    }

    @Override
    public Principal getPrincipal(String dummyToken, String user) throws IllegalArgumentException
    {
        return getPrincipal(user);
    }

    @Override
    public Principal getPrincipal(String user) throws IllegalArgumentException
    {
        final Principal principalOrNull = tryGetAndAuthenticateUser(user, null);
        if (principalOrNull == null)
        {
            throw new IllegalArgumentException("Cannot find user '" + user + "'.");
        }
        return principalOrNull;
    }

    @Override
    public Principal tryGetAndAuthenticateUser(String dummyToken, String user, String passwordOrNull)
    {
        return tryGetAndAuthenticateUser(user, passwordOrNull);
    }

    @Override
    public Principal tryGetAndAuthenticateUser(String user, String passwordOrNull)
    {
        for (IAuthenticationService service : delegates)
        {
            final Principal principal = service.tryGetAndAuthenticateUser(user, passwordOrNull);
            if (principal != null)
            {
                return principal;
            }
        }
        return null;
    }

    @Override
    public Principal tryGetAndAuthenticateUserByEmail(String applicationToken, String email,
            String passwordOrNull)
    {
        return tryGetAndAuthenticateUserByEmail(email, passwordOrNull);
    }

    @Override
    public Principal tryGetAndAuthenticateUserByEmail(String email, String passwordOrNull)
    {
        for (IAuthenticationService service : delegates)
        {
            if (service.supportsListingByEmail() == false)
            {
                continue;
            }
            final Principal principal =
                    service.tryGetAndAuthenticateUserByEmail(email, passwordOrNull);
            if (principal != null)
            {
                return principal;
            }
        }
        return null;
    }

    @Override
    public List<Principal> listPrincipalsByEmail(String applicationToken, String emailQuery)
    {
        return listPrincipalsByEmail(emailQuery);
    }

    @Override
    public List<Principal> listPrincipalsByEmail(String emailQuery)
    {
        if (supportsListingByEmail == false)
        {
            throw new UnsupportedOperationException();
        }
        final List<Principal> principals = new ArrayList<Principal>();
        for (IAuthenticationService service : delegates)
        {
            if (service.supportsListingByEmail())
            {
                principals.addAll(service.listPrincipalsByEmail(emailQuery));
            }
        }
        return principals;
    }

    @Override
    public List<Principal> listPrincipalsByLastName(String applicationToken, String lastNameQuery)
    {
        return listPrincipalsByLastName(lastNameQuery);
    }

    @Override
    public List<Principal> listPrincipalsByLastName(String lastNameQuery)
    {
        if (supportsListingByLastName == false)
        {
            throw new UnsupportedOperationException();
        }
        final List<Principal> principals = new ArrayList<Principal>();
        for (IAuthenticationService service : delegates)
        {
            if (service.supportsListingByLastName())
            {
                principals.addAll(service.listPrincipalsByLastName(lastNameQuery));
            }
        }
        return principals;
    }

    @Override
    public List<Principal> listPrincipalsByUserId(String applicationToken, String userIdQuery)
    {
        return listPrincipalsByUserId(userIdQuery);
    }

    @Override
    public List<Principal> listPrincipalsByUserId(String userIdQuery)
    {
        if (supportsListingByUserId == false)
        {
            throw new UnsupportedOperationException();
        }
        final List<Principal> principals = new ArrayList<Principal>();
        for (IAuthenticationService service : delegates)
        {
            if (service.supportsListingByUserId())
            {
                principals.addAll(service.listPrincipalsByUserId(userIdQuery));
            }
        }
        return principals;
    }

    public boolean allServicesSupportListingByUserId()
    {
        boolean result = true;
        for (IAuthenticationService service : delegates)
        {
            result = result && service.supportsListingByUserId();
        }
        return result;
    }

    @Override
    public boolean supportsListingByEmail()
    {
        return supportsListingByEmail;
    }

    @Override
    public boolean supportsListingByLastName()
    {
        return supportsListingByLastName;
    }

    @Override
    public boolean supportsListingByUserId()
    {
        return supportsListingByUserId;
    }

    @Override
    public boolean supportsAuthenticatingByEmail()
    {
        return supportsAuthenticatingByEmail;
    }

    @Override
    public void check() throws EnvironmentFailureException, ConfigurationFailureException
    {
        for (IAuthenticationService service : delegates)
        {
            service.check();
        }
    }

    @Override
    public boolean isRemote()
    {
        return remote;
    }

    @Override
    public boolean isConfigured()
    {
        return (delegates.isEmpty() == false);
    }

}
