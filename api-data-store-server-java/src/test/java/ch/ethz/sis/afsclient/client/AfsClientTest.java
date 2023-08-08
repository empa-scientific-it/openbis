package ch.ethz.sis.afsclient.client;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AfsClientTest
{

    private static DummyHttpServer httpServer;

    private AfsClient afsClient;

    private static final int HTTP_SERVER_PORT = 8085;

    private static final String HTTP_SERVER_PATH = "/fileserver";

    @Before
    public void setUp() throws Exception
    {
        httpServer = new DummyHttpServer(HTTP_SERVER_PORT, HTTP_SERVER_PATH);
        httpServer.start();
        afsClient = new AfsClient(
                new URI("http", null, "localhost", HTTP_SERVER_PORT,
                        HTTP_SERVER_PATH, null, null));
        afsClient.setTransactionManagerKey("TransactionManagerKey");
        afsClient.setInteractiveSessionKey("InteractiveSessionKey");
    }

    @After
    public void tearDown()
    {
        httpServer.stop();
    }

    @Test
    public void login_methodIsPost() throws Exception
    {
        final String token = afsClient.login("test", "test");
        assertNotNull(token);
        assertEquals(token, afsClient.getSessionToken());
        assertEquals("POST", httpServer.getHttpExchange().getRequestMethod());
        assertTrue(httpServer.getLastRequestBody().length > 0);
    }

    @Test
    public void isSessionValid_withoutLogin_throwsException() throws Exception
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
    public void isSessionValid_afterLogin_methodIsGet() throws Exception
    {
        afsClient.login("test", "test");
        httpServer.setNextResponse("{\"result\": true}");

        Boolean result = afsClient.isSessionValid();

        assertTrue(result);
        assertEquals("GET", httpServer.getHttpExchange().getRequestMethod());
        assertArrayEquals(httpServer.getLastRequestBody(), new byte[0]);
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
    public void logout_sessionTokenIsCleared() throws Exception
    {
        afsClient.login("test", "test");
        assertNotNull(afsClient.getSessionToken());

        httpServer.setNextResponse("{\"result\": true}");

        Boolean result = afsClient.logout();
        assertTrue(result);
        assertNull(afsClient.getSessionToken());
        assertEquals("POST", httpServer.getHttpExchange().getRequestMethod());
        assertTrue(httpServer.getLastRequestBody().length > 0);
    }

    @Test
    public void list_methodIsGet() throws Exception
    {
        login();

        httpServer.setNextResponse("{\"result\": null}");
        afsClient.list("", "", true);

        assertEquals("GET", httpServer.getHttpExchange().getRequestMethod());
        assertArrayEquals(httpServer.getLastRequestBody(), new byte[0]);
    }

    @Test
    public void read_methodIsGet() throws Exception
    {
        login();

        byte[] data = "ABCD".getBytes();
        httpServer.setNextResponse(data);

        byte[] result = afsClient.read("", "", 0L, 1000);

        assertEquals("GET", httpServer.getHttpExchange().getRequestMethod());
        assertArrayEquals(data, result);
        assertArrayEquals(httpServer.getLastRequestBody(), new byte[0]);
    }

    @Test
    public void resumeRead_methodIsGet() throws Exception
    {
        login();

        final String sourceFileName = "afs-test-src.txt";
        final Path sourceFilePath = Path.of(sourceFileName);
        final String destinationFileName = "afs-test-dst.txt";
        final Path destinationFilePath = Path.of(destinationFileName);
        final String fileNameJson = String.format("{\n"
                + "  \"id\" : \"1\",\n"
                + "  \"result\" : [ \"java.util.ArrayList\", [ [ \"ch.ethz.sis.afsapi.dto.File\", {\n"
                + "    \"path\" : \"%s\",\n"
                + "    \"name\" : \"%s\",\n"
                + "    \"directory\" : false,\n"
                + "    \"size\" : 4,\n"
                + "    \"lastModifiedTime\" : \"2023-06-27T17:18:08.900154283+02:00\",\n"
                + "    \"creationTime\" : \"2023-06-27T17:18:08.900154283+02:00\",\n"
                + "    \"lastAccessTime\" : \"2023-06-27T17:18:08.900154283+02:00\"\n"
                + "  } ] ] ],\n"
                + "  \"error\" : null\n"
                + "}", sourceFileName, sourceFileName);
        byte[] fileData = "ABCD".getBytes();
        Files.write(sourceFilePath, fileData);

        httpServer.setNextResponses(new byte[][] {fileNameJson.getBytes(), fileData}, new String[] {"application/json", "application/octet-stream"});

        afsClient.resumeRead("", sourceFileName, destinationFilePath, 0L);

        assertEquals("GET", httpServer.getHttpExchange().getRequestMethod());
        assertArrayEquals(fileData, Files.readAllBytes(destinationFilePath));
        assertEquals(0, httpServer.getLastRequestBody().length);

        sourceFilePath.toFile().delete();
        destinationFilePath.toFile().delete();
    }

    @Test
    public void resumeWrite_methodIsPost() throws Exception
    {
        login();

        final String sourceFileName = "afs-test-src.txt";
        final Path sourceFilePath = Path.of(sourceFileName);
        final String destinationFileName = "afs-test-dst.txt";
        final byte[] sourceFileData = "ABCD".getBytes();
        Files.write(sourceFilePath, sourceFileData);

        httpServer.setNextResponse("{\"result\": true}");
        final Boolean result = afsClient.resumeWrite("", destinationFileName, sourceFilePath, 0L);

        assertEquals("POST", httpServer.getHttpExchange().getRequestMethod());
        assertTrue(result);
        assertTrue(httpServer.getLastRequestBody().length > 0);

        sourceFilePath.toFile().delete();
    }

    @Test
    public void write_methodIsPost() throws Exception
    {
        login();

        httpServer.setNextResponse("{\"result\": true}");
        Boolean result = afsClient.write("", "", 0L, new byte[0], new byte[0]);

        assertEquals("POST", httpServer.getHttpExchange().getRequestMethod());
        assertTrue(result);
        assertTrue(httpServer.getLastRequestBody().length > 0);
    }

    @Test
    public void delete_methodIsDelete() throws Exception
    {
        login();

        httpServer.setNextResponse("{\"result\": true}");
        Boolean result = afsClient.delete("", "");

        assertEquals("DELETE", httpServer.getHttpExchange().getRequestMethod());
        assertTrue(result);
        assertTrue(httpServer.getLastRequestBody().length > 0);
    }

    @Test
    public void copy_methodIsPost() throws Exception
    {
        login();

        httpServer.setNextResponse("{\"result\": true}");
        Boolean result = afsClient.copy("", "", "", "");

        assertEquals("POST", httpServer.getHttpExchange().getRequestMethod());
        assertTrue(result);
        assertTrue(httpServer.getLastRequestBody().length > 0);
    }

    @Test
    public void move_methodIsPost() throws Exception
    {
        login();

        httpServer.setNextResponse("{\"result\": true}");
        Boolean result = afsClient.move("", "", "", "");

        assertEquals("POST", httpServer.getHttpExchange().getRequestMethod());
        assertTrue(result);
        assertTrue(httpServer.getLastRequestBody().length > 0);
    }

    @Test
    public void begin_methodIsPost() throws Exception
    {
        login();

        UUID transactionId = UUID.randomUUID();

        httpServer.setNextResponse("{\"result\": null}");
        afsClient.begin(transactionId);

        assertEquals("POST", httpServer.getHttpExchange().getRequestMethod());
    }

    @Test
    public void prepare_methodIsPost() throws Exception
    {
        login();

        httpServer.setNextResponse("{\"result\": true}");
        Boolean result = afsClient.prepare();

        assertEquals("POST", httpServer.getHttpExchange().getRequestMethod());
        assertTrue(result);
    }

    @Test
    public void commit_methodIsPost() throws Exception
    {
        login();
        httpServer.setNextResponse("{\"result\": null}");

        afsClient.commit();
        assertEquals("POST", httpServer.getHttpExchange().getRequestMethod());
    }

    @Test
    public void rollback_methodIsPost() throws Exception
    {
        login();

        httpServer.setNextResponse("{\"result\": null}");

        afsClient.rollback();
        assertEquals("POST", httpServer.getHttpExchange().getRequestMethod());
    }

    @Test
    public void recover_methodIsPost() throws Exception
    {
        login();

        httpServer.setNextResponse("{\"result\": null}");

        List<UUID> result = afsClient.recover();
        assertEquals("POST", httpServer.getHttpExchange().getRequestMethod());
        assertNull(result);
    }

    @Test
    public void create_folder_methodIdPost() throws Exception
    {
        login();

        httpServer.setNextResponse("{\"result\": true}");
        final Boolean result = afsClient.create("", "", true);

        assertEquals("POST", httpServer.getHttpExchange().getRequestMethod());
        assertTrue(result);
    }

    @Test
    public void create_file_methodIdPost() throws Exception
    {
        login();

        httpServer.setNextResponse("{\"result\": true}");
        final Boolean result = afsClient.create("", "", false);

        assertEquals("POST", httpServer.getHttpExchange().getRequestMethod());
        assertTrue(result);
    }

    private void login() throws Exception
    {
        afsClient.login("test", "test");
    }

}