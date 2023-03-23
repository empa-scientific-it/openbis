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

package ch.ethz.sis.afsserver.client;

import static org.junit.Assert.*;

import ch.ethz.sis.afsapi.dto.ExceptionReason;
import ch.ethz.sis.afsapi.exception.ThrowableReason;
import ch.ethz.sis.afsserver.server.Server;
import ch.ethz.sis.afsserver.server.observer.impl.DummyServerObserver;
import ch.ethz.sis.afsserver.startup.AtomicFileSystemServerParameter;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.rights.Rights;
import ch.ethz.sis.shared.io.IOUtils;
import ch.ethz.sis.shared.startup.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class OpenBisAuthApiClientTest extends BaseApiClientTest
{

    /** Match this value with openBISUrl from properties file */
    private static final int OPENBIS_DUMMY_SERVER_PORT = 8084;

    private static final String OPENBIS_DUMMY_SERVER_PATH = "/";

    private DummyOpenBisServer dummyOpenBisServer;

    @BeforeClass
    public static void classSetUp() throws Exception
    {
        final Configuration configuration =
                new Configuration(List.of(AtomicFileSystemServerParameter.class),
                        "src/test/resources/test-server-with-auth-config.properties");
        final DummyServerObserver dummyServerObserver = new DummyServerObserver();

        afsServer = new Server<>(configuration, dummyServerObserver, dummyServerObserver);
        httpServerPort =
                configuration.getIntegerProperty(AtomicFileSystemServerParameter.httpServerPort);
        httpServerPath =
                configuration.getStringProperty(AtomicFileSystemServerParameter.httpServerUri);
        storageRoot = configuration.getStringProperty(AtomicFileSystemServerParameter.storageRoot);
    }

    @Before
    public void setUpDummyOpenBis() throws Exception
    {
        dummyOpenBisServer =
                new DummyOpenBisServer(OPENBIS_DUMMY_SERVER_PORT, OPENBIS_DUMMY_SERVER_PATH);
        dummyOpenBisServer.start();
    }

    @After
    public void tearDownDummyOpenBis()
    {
        dummyOpenBisServer.stop();
    }

    @Test
    public void list_callFailsDueToMissingPermissions() throws Exception
    {
        login();

        dummyOpenBisServer.setResponses(Map.of("getSamples", Map.of()));

        try
        {
            afsClient.list(owner, "", Boolean.TRUE);
            fail();
        } catch (Exception e)
        {
            ThrowableReason reason = (ThrowableReason) e.getCause();
            String message = ((ExceptionReason) reason.getReason()).getMessage();
            System.out.println(message);
            assertTrue(message.matches(
                    "(?s).*Session .* don't have rights \\[Read\\] over .*to perform the operation List(?s).*"));
        }

    }

    @Test
    public void list_failsDueToExpiredSession() throws Exception
    {
        login();
        dummyOpenBisServer.setResponses(Map.of("isSessionActive", false));
        try
        {
            afsClient.list(owner, "", Boolean.TRUE);
            fail();
        } catch (Exception e)
        {
            ThrowableReason reason = (ThrowableReason) e.getCause();
            String message = ((ExceptionReason) reason.getReason()).getMessage();
            System.out.println(message);
            assertTrue(message.matches("(?s).*Session .* doesn't exist(?s).*"));
        }
    }

    @Test
    public void write_failsDueToMissingPermission_noFileCreated() throws Exception
    {
        login();

        dummyOpenBisServer.setResponses(Map.of("getRights", new Rights(Set.of())));

        try
        {
            afsClient.write(owner, FILE_B, 0L, DATA, IOUtils.getMD5(DATA));
            fail();
        } catch (Exception e)
        {
            ThrowableReason reason = (ThrowableReason) e.getCause();
            String message = ((ExceptionReason) reason.getReason()).getMessage();
            System.out.println(message);
            assertTrue(message.matches(
                    "(?s).*Session .* don't have rights \\[Write\\] over .* to perform the operation Write(?s).*"));
        }
        assertFalse(IOUtils.exists(IOUtils.getPath(testDataRoot, FILE_B)));

    }

    @Test
    public void move_failsDueToMissingPermissions() throws Exception
    {
        login();

        dummyOpenBisServer.setResponses(Map.of("getRights", new Rights(Set.of()),
                "getSamples", Map.of()));

        try
        {
            afsClient.move(owner, FILE_A, owner, FILE_B);
            fail();
        } catch (Exception e)
        {
            ThrowableReason reason = (ThrowableReason) e.getCause();
            String message = ((ExceptionReason) reason.getReason()).getMessage();
            System.out.println(message);
            assertTrue(message.matches(
                    "(?s).*Session .* don't have rights \\[(Write|Read), (Write|Read)\\] over .* to perform the operation Move(?s).*"));
        }
        assertFalse(IOUtils.exists(IOUtils.getPath(testDataRoot, FILE_B)));
    }

}
