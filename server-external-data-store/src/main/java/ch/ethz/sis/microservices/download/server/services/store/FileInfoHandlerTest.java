package ch.ethz.sis.microservices.download.server.services.store;

public class FileInfoHandlerTest
{
    public static void main(String[] args) throws Exception
    {
        AbstractFileServiceTest.test(
                "http://localhost:8888/openbis/openbis/rmi-application-server-v3",
                "http://localhost:8080/file-information",
                "admin",
                "admin",
        			null);
    }
}
