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

package ch.systemsx.cisd.authentication;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Dummy authentication services.
 * 
 * @author Franz-Josef Elmer
 */
public final class DummyAuthenticationService implements IAuthenticationService
{

    final String[] firstNames =
    { "St\u00e9phane", "G\u00fcnter", "Elfriede", "Ryszard", "Karel", "Claude" };

    final String[] lastNames =
    { "Mallarm\u00e9", "Grass", "Jelinek", "Kapu\u015Bci\u0144ski", "\u010Capek",
            "L\u00e9vi-Strauss" };

    //
    // IAuthenticationService
    //
    @Override
    public final String authenticateApplication()
    {
        // Up to the contract, if it returns <code>null</code> here, it assumes that the application
        // did not authenticate successfully.
        return StringUtils.EMPTY;
    }

    /**
     * Always returns <code>true</code>, meaning that the login was successful.
     */
    @Override
    public final boolean authenticateUser(final String user, final String password)
    {
        return true;
    }

    /**
     * Always returns <code>true</code>, meaning that the login was successful.
     */
    @Override
    public final boolean authenticateUser(final String applicationToken, final String user,
            final String password)
    {
        return true;
    }

    @Override
    public final Principal getPrincipal(final String user)
    {
        // Generate a random first and last name combination
        final String firstName;
        final String lastName;
        int idx = (int) Math.floor(Math.random() * firstNames.length);
        firstName = firstNames[idx];
        idx = (int) Math.floor(Math.random() * lastNames.length);
        lastName = lastNames[idx];
        return new Principal(user, firstName, lastName, "franz-josef.elmer@systemsx.ch", false);
    }

    @Override
    public final Principal getPrincipal(final String applicationToken, final String user)
    {
        return getPrincipal(user);
    }

    @Override
    public Principal tryGetAndAuthenticateUser(String user, String passwordOrNull)
    {
        final Principal principal = getPrincipal(user);
        principal.setAuthenticated(true);
        return principal;
    }

    @Override
    public Principal tryGetAndAuthenticateUser(String applicationToken, String user,
            String passwordOrNull)
    {
        return tryGetAndAuthenticateUser(user, passwordOrNull);
    }

    @Override
    public Principal tryGetAndAuthenticateUserByEmail(String email, String passwordOrNull)
    {
        final Principal principal = getPrincipal(email);
        principal.setAuthenticated(true);
        return principal;
    }

    @Override
    public Principal tryGetAndAuthenticateUserByEmail(String applicationToken, String email,
            String passwordOrNull)
    {
        return tryGetAndAuthenticateUserByEmail(email, passwordOrNull);
    }

    @Override
    public boolean isRemote()
    {
        return false;
    }

    @Override
    public List<Principal> listPrincipalsByEmail(String emailQuery)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Principal> listPrincipalsByEmail(String applicationToken, String emailQuery)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Principal> listPrincipalsByLastName(String lastNameQuery)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Principal> listPrincipalsByLastName(String applicationToken, String lastNameQuery)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Principal> listPrincipalsByUserId(String userIdQuery)
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
    public final void check()
    {
        // Always available.
    }

    @Override
    public boolean isConfigured()
    {
        return true;
    }

}