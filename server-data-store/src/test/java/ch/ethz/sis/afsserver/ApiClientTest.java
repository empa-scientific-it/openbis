/*
 *  Copyright ETH 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ch.ethz.sis.afsserver;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

import java.net.URI;
import java.util.List;

import org.junit.*;

import ch.ethz.sis.afs.manager.TransactionConnection;
import ch.ethz.sis.afsclient.client.AfsClient;
import ch.ethz.sis.afsserver.server.Server;
import ch.ethz.sis.afsserver.server.observer.impl.DummyServerObserver;
import ch.ethz.sis.afsserver.startup.AtomicFileSystemServerParameter;
import ch.ethz.sis.shared.startup.Configuration;

public final class ApiClientTest
{
    private static Server<TransactionConnection, ?> afsServer;

    private static AfsClient afsClient;

    private static int httpServerPort;

    private static String httpServerPath;

    @BeforeClass
    public static void classSetUp() throws Exception
    {
        final Configuration configuration =
                new Configuration(List.of(AtomicFileSystemServerParameter.class),
                        "src/test/resources/test-server-config.properties");
        final DummyServerObserver dummyServerObserver = new DummyServerObserver();
        afsServer = new Server<>(configuration, dummyServerObserver, dummyServerObserver);
        httpServerPort =
                configuration.getIntegerProperty(AtomicFileSystemServerParameter.httpServerPort);
        httpServerPath =
                configuration.getStringProperty(AtomicFileSystemServerParameter.httpServerUri);
    }

    @Before
    public void setUp() throws Exception
    {
        afsClient = new AfsClient(
                new URI("http", null, "localhost", httpServerPort,
                        httpServerPath, null, null));
    }

    private String login() throws Exception
    {
        return afsClient.login("test", "test");
    }

    @AfterClass
    public static void classTearDown() throws Exception
    {
        afsServer.shutdown(true);
    }

    @Test
    public void login_sessionTokenIsNotNull() throws Exception
    {
        final String token = login();
        assertNotNull(token);
    }

    @Test
    public void isSessionValid_throwsException() throws Exception
    {
        try
        {
            afsClient.isSessionValid();
            fail();
        } catch (IllegalStateException e)
        {
            assertThat(e.getMessage(), containsString("No session information detected!"));
        }
    }

    @Test
    public void isSessionValid_returnsTrue() throws Exception
    {
        login();

        final Boolean isValid = afsClient.isSessionValid();
        assertTrue(isValid);
    }

    @Test
    public void logout_withoutLogin_throwsException() throws Exception
    {
        try
        {
            afsClient.logout();
            fail();
        } catch (IllegalStateException e)
        {
            assertThat(e.getMessage(), containsString("No session information detected!"));
        }
    }

    @Test
    public void logout_withLogin_returnsTrue() throws Exception
    {
        login();

        final Boolean result = afsClient.logout();

        assertTrue(result);
    }

}
