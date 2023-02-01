package ch.ethz.sis.microservices.download.server.services.store;

public class DownloadHandlerWithOffsetTest
{

    public static void main(String[] args) throws Exception
    {
        AbstractFileServiceTest.test(
                "http://localhost:8888/openbis/openbis/rmi-application-server-v3",
                "http://localhost:8080/download",
                "admin",
                "admin",
                430129557L);
    }
}
