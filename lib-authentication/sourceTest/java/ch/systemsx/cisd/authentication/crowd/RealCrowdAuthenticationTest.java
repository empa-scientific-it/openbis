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

package ch.systemsx.cisd.authentication.crowd;

import static org.testng.AssertJUnit.*;

import org.testng.annotations.Test;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.Principal;

/**
 * A real test case for the Crowd authentication.
 * <p>
 * Note: this test case is dependent on the environment and is usually broken. If you want to use it, do this:
 * <ol>
 * <li>Enter the right name of the authentication host in the source code</li>
 * <li>Enter the right port of the authentication service in the source code</li>
 * <li>Enter the right password of the test service</li>
 * <li>Enter the right password of the test user</li>
 * <li>Enter property <code>-Djavax.net.ssl.trustStore=&lt;path to keystore with certificate of authentication 
 * host&gt;<code> to the java command line that runs the command</li>
 * <li>Remove the test case from the "broken" group.</li>
 * <li>Last but not least: check the output and compare with the Crowd console!</li>
 * </ol>
 * 
 * @author Bernd Rinn
 */
public class RealCrowdAuthenticationTest
{

    private static final String PORT_OF_AUTHENTICATION_SERVICE = null; // FIX!

    private static final String HOST_NAME_OF_AUTHENTICATION_SERVICE = null; // FIX!

    private static final String NAME_OF_TEST_APPLICATION = "test";

    private static final String PASSWORD_OF_TEST_APPLICATION = null; // FIX!

    private static final String NAME_OF_TEST_USER = "test";

    private static final String PASSWORD_OF_TEST_USER = null; // FIX!

    @Test(groups = "broken")
    public void testCrowdTestAccount()
    {
        final IAuthenticationService as =
                new CrowdAuthenticationService(HOST_NAME_OF_AUTHENTICATION_SERVICE,
                        PORT_OF_AUTHENTICATION_SERVICE, NAME_OF_TEST_APPLICATION,
                        PASSWORD_OF_TEST_APPLICATION);
        assertTrue(as.authenticateUser(NAME_OF_TEST_USER, PASSWORD_OF_TEST_USER));
        final Principal p = as.getPrincipal(NAME_OF_TEST_USER);
        assertEquals(NAME_OF_TEST_USER, p.getUserId());
        System.out.println("firstName=" + p.getFirstName());
        System.out.println("lastName=" + p.getLastName());
        System.out.println("email=" + p.getEmail());
        for (String key : p.getPropertyNames())
        {
            System.out.println(key + " : " + p.getProperty(key));
        }
    }

    @Test(groups = "broken")
    public void testCrowdTestAccountWithTryGetAndAuthenticateUser()
    {
        final IAuthenticationService as =
                new CrowdAuthenticationService(HOST_NAME_OF_AUTHENTICATION_SERVICE,
                        PORT_OF_AUTHENTICATION_SERVICE, NAME_OF_TEST_APPLICATION,
                        PASSWORD_OF_TEST_APPLICATION);
        final Principal principal =
                as.tryGetAndAuthenticateUser(NAME_OF_TEST_USER, PASSWORD_OF_TEST_USER);
        assertNotNull(principal);
        assertEquals(NAME_OF_TEST_USER, principal.getUserId());
        assertTrue(principal.isAuthenticated());
    }
}
