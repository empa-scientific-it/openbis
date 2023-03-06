package ch.ethz.sis.afsclient.client;

import static org.junit.Assert.*;

import java.net.URI;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class AfsClientTest
{

    private static DummyHttpServer httpServer;

    private static AfsClient afsClient;

    private static int httpServerPort;

    private static String httpServerPath;

    @BeforeClass
    public static void classSetUp() throws Exception
    {
        httpServerPort = 8085;
        httpServerPath = "/fileserver";
        httpServer = new DummyHttpServer(httpServerPort, httpServerPath);
        httpServer.start();
        afsClient = new AfsClient(
                new URI("http", null, "localhost", httpServerPort, httpServerPath, null, null));
    }

    @AfterClass
    public static void classTearDown() throws Exception
    {
        httpServer.stop();
    }

    @Test
    public void testLogin() throws Exception
    {
        final String token = afsClient.login("test", "test");
        assertNotNull(token);
    }

    @Test
    public void testIsSessionValid()
    {
    }

    @Test
    public void testLogout()
    {
    }

    @Test
    public void testList()
    {
    }

    @Test
    public void testRead()
    {
    }

    @Test
    public void testWrite()
    {
    }

    @Test
    public void testDelete()
    {
    }

    @Test
    public void testCopy()
    {
    }

    @Test
    public void testMove()
    {
    }

    @Test
    public void testBegin()
    {
    }

    @Test
    public void testPrepare()
    {
    }

    @Test
    public void testCommit()
    {
    }

    @Test
    public void testRollback()
    {
    }

    @Test
    public void testRecover()
    {
    }

}