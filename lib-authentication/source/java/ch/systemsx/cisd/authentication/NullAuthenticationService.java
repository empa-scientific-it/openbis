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

package ch.systemsx.cisd.authentication;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;

/**
 * An implementation of {@link IAuthenticationService} which supports as a <code>null</code> object.
 * 
 * @author Franz-Josef Elmer
 */
public class NullAuthenticationService implements IAuthenticationService
{
    @Override
    public String authenticateApplication()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean authenticateUser(String applicationToken, String user, String password)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Principal tryGetAndAuthenticateUser(String applicationToken, String user,
            String passwordOrNull)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Principal tryGetAndAuthenticateUserByEmail(String applicationToken, String email, String passwordOrNull)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Principal getPrincipal(String applicationToken, String user)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void check() throws EnvironmentFailureException, ConfigurationFailureException
    {
    }

    @Override
    public boolean isRemote()
    {
        return false;
    }

    @Override
    public List<Principal> listPrincipalsByEmail(String applicationToken, String emailQuery)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Principal> listPrincipalsByLastName(String applicationToken, String lastNameQuery)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Principal> listPrincipalsByUserId(String applicationToken, String userIdQuery)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsListingByEmail()
    {
        return false;
    }

    @Override
    public boolean supportsListingByLastName()
    {
        return false;
    }

    @Override
    public boolean supportsListingByUserId()
    {
        return false;
    }

    @Override
    public boolean supportsAuthenticatingByEmail()
    {
        return true;
    }

    @Override
    public boolean authenticateUser(String user, String password)
    {
        return false;
    }

    @Override
    public Principal getPrincipal(String user) throws IllegalArgumentException
    {
        return null;
    }

    @Override
    public List<Principal> listPrincipalsByEmail(String emailQuery) throws IllegalArgumentException
    {
        return null;
    }

    @Override
    public List<Principal> listPrincipalsByLastName(String lastNameQuery)
            throws IllegalArgumentException
    {
        return null;
    }

    @Override
    public List<Principal> listPrincipalsByUserId(String userIdQuery)
            throws IllegalArgumentException
    {
        return null;
    }

    @Override
    public Principal tryGetAndAuthenticateUser(String user, String passwordOrNull)
    {
        return null;
    }

    @Override
    public Principal tryGetAndAuthenticateUserByEmail(String email, String passwordOrNull)
    {
        return null;
    }

    @Override
    public boolean isConfigured()
    {
        return false;
    }

}
