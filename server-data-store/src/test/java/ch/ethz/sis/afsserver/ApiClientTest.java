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

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import ch.ethz.sis.afsapi.dto.File;
import ch.ethz.sis.shared.io.IOUtils;
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

    private static String storageRoot;

    public static final String FILE_A = "A.txt";

    public static final byte[] DATA = "ABCD".getBytes();
    public static final String FILE_B = "B.txt";

    public static String owner = UUID.randomUUID().toString();

    private String testDataRoot;


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
        storageRoot = configuration.getStringProperty(AtomicFileSystemServerParameter.storageRoot);
    }

    @AfterClass
    public static void classTearDown() throws Exception
    {
        afsServer.shutdown(true);
    }

    @Before
    public void setUp() throws Exception
    {
        testDataRoot = IOUtils.getPath(storageRoot, owner.toString());
        IOUtils.createDirectories(testDataRoot);
        String testDataFile = IOUtils.getPath(testDataRoot, FILE_A);
        IOUtils.createFile(testDataFile);
        IOUtils.write(testDataFile, 0, DATA);

        afsClient = new AfsClient(
                new URI("http", null, "localhost", httpServerPort,
                        httpServerPath, null, null));
    }

    @After
    public void deleteTestData() throws IOException
    {
        IOUtils.delete(storageRoot);
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

    @Test
    public void list_getsDataListFromTemporaryFolder() throws Exception
    {
        login();

        List<File> list = afsClient.list(owner, "", Boolean.TRUE);
        assertEquals(1, list.size());
        assertEquals(FILE_A, list.get(0).getName());
    }

    @Test
    public void read_getsDataFromTemporaryFile() throws Exception {
        login();

        byte[] bytes = afsClient.read(owner, FILE_A, 0L, DATA.length);
        assertArrayEquals(DATA, bytes);
    }

    @Test
    public void write_zeroOffset_createsFile() throws Exception {
        login();

        Boolean result = afsClient.write(owner, FILE_B, 0L, DATA, IOUtils.getMD5(DATA));
        assertTrue(result);

        byte[] testDataFile = IOUtils.readFully(IOUtils.getPath(testDataRoot, FILE_B));
        assertArrayEquals(DATA, testDataFile);
    }

    @Test
    public void write_nonZeroOffset_createsFile() throws Exception {
        login();

        Long offset = 65L;
        Boolean result = afsClient.write(owner, FILE_B, offset, DATA, IOUtils.getMD5(DATA));
        assertTrue(result);

        byte[] testDataFile = IOUtils.readFully(IOUtils.getPath(testDataRoot, FILE_A));
        assertArrayEquals(DATA, testDataFile);
    }

    @Test
    public void delete_fileIsGone() throws Exception {
        login();

        Boolean deleted = afsClient.delete(owner, FILE_A);
        assertTrue(deleted);

        List<ch.ethz.sis.afs.api.dto.File> list =  IOUtils.list(testDataRoot, true);
        assertEquals(0, list.size());
    }

    @Test
    public void copy_newFileIsCreated() throws Exception {
        login();

        Boolean result = afsClient.copy(owner, FILE_A, owner, FILE_B);
        assertTrue(result);

        byte[] testDataFile = IOUtils.readFully(IOUtils.getPath(testDataRoot, FILE_B));
        assertArrayEquals(DATA, testDataFile);
    }

    @Test
    public void move_fileIsRenamed() throws Exception {
        login();

        Boolean result = afsClient.move(owner, FILE_A, owner, FILE_B);
        assertTrue(result);

        List<ch.ethz.sis.afs.api.dto.File> list =  IOUtils.list(testDataRoot, true);
        assertEquals(1, list.size());
        assertEquals(FILE_B, list.get(0).getName());

        byte[] testDataFile = IOUtils.readFully(IOUtils.getPath(testDataRoot, FILE_B));
        assertArrayEquals(DATA, testDataFile);
    }



    private String login() throws Exception
    {
        return afsClient.login("test", "test");
    }

}
