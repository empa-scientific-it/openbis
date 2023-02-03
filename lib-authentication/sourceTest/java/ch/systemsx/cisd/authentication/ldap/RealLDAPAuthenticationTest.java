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

package ch.systemsx.cisd.authentication.ldap;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.logging.LogInitializer;

import static org.testng.AssertJUnit.*;

/**
 * A real test case for LDAP authentication.
 * 
 * @author Bernd Rinn
 */
public class RealLDAPAuthenticationTest
{
    private final static String SERVER_URL = null; // FIX

    private final static String SECURITY_PRINCIPAL_DISTINGUISHED_NAME = null; // FIX

    private final static String SECURITY_PRINCIPAL_PASSWORD = null; // FIX

    private final static String USER_ID_TO_AUTHENTICATE = null;

    private final static String USER_PASSWORD_TO_AUTHENTICATE = null;

    private final static String MAIL_ALIAS_OF_USER = null;

    private IAuthenticationService service;

    @BeforeTest
    public void setUp()
    {
        LogInitializer.init();
        final LDAPDirectoryConfiguration config = new LDAPDirectoryConfiguration();
        config.setServerUrl(SERVER_URL);
        config.setSecurityPrincipalDistinguishedName(SECURITY_PRINCIPAL_DISTINGUISHED_NAME);
        config.setSecurityPrincipalPassword(SECURITY_PRINCIPAL_PASSWORD);
        config.setQueryEmailForAliases("true");
        config.setTimeoutStr("1000");
        config.setTimeToWaitAfterFailureStr("1000");
        service = new LDAPAuthenticationService(config);
    }

    @Test(groups = "broken")
    public void testAuthenticateUser()
    {
        assertTrue(service.authenticateUser(USER_ID_TO_AUTHENTICATE, USER_PASSWORD_TO_AUTHENTICATE));
    }

    @Test(groups = "broken")
    public void testAuthenticateUserByEmailAlias()
    {
        assertTrue(Principal.isAuthenticated(service.tryGetAndAuthenticateUserByEmail(
                MAIL_ALIAS_OF_USER, USER_PASSWORD_TO_AUTHENTICATE)));
    }
}
