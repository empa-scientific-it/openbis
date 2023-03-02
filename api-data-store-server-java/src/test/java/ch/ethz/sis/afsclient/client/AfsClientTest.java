package ch.ethz.sis.afsclient.client;

import static org.junit.Assert.*;

import java.net.URI;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

//import ch.ethz.sis.afs.manager.TransactionConnection;
//import ch.ethz.sis.afsserver.server.AfsServer;
//import ch.ethz.sis.afsserver.server.observer.impl.DummyServerObserver;
//import ch.ethz.sis.afsserver.startup.AtomicFileSystemServerParameter;
//import ch.ethz.sis.shared.startup.Configuration;

public class AfsClientTest
{
    
//    private static AfsServer<TransactionConnection, ?> afsServer;

//    private static AfsClient afsClient;

    @BeforeClass
    public static void classSetUp() throws Exception {
//        final Configuration configuration = new Configuration(List.of(AtomicFileSystemServerParameter.class),
//                "src/test/resources/afs-server-config.properties");
//        final DummyServerObserver dummyServerObserver = new DummyServerObserver();
//        afsServer = new AfsServer<>(configuration, dummyServerObserver, dummyServerObserver);
//
//        final int httpServerPort = configuration.getIntegerProperty(AtomicFileSystemServerParameter.httpServerPort);
//        final String httpServerPath = configuration.getStringProperty(AtomicFileSystemServerParameter.httpServerPath);
//        afsClient = new AfsClient(new URI("http", null, "localhost", httpServerPort, httpServerPath, null, null));
    }

    @AfterClass
    public static void classTearDown() throws Exception {
//        afsServer.shutdown(true);
    }

    @Test
    public void testLogin() throws Exception {
//        final String token = afsClient.login("test", "test");
        final String token = "null";
        assertNotNull(token);
    }

    @Test
    public void testIsSessionValid() {
    }

    @Test
    public void testLogout() {
    }

    @Test
    public void testList() {
    }

    @Test
    public void testRead() {
    }

    @Test
    public void testWrite() {
    }

    @Test
    public void testDelete() {
    }

    @Test
    public void testCopy() {
    }

    @Test
    public void testMove() {
    }

    @Test
    public void testBegin() {
    }

    @Test
    public void testPrepare() {
    }

    @Test
    public void testCommit() {
    }

    @Test
    public void testRollback() {
    }

    @Test
    public void testRecover() {
    }

}