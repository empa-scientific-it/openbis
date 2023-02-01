package ch.ethz.sis.microservices.download.server.services.store;

import java.util.HashMap;
import java.util.Map;

import ch.ethz.sis.microservices.download.server.logging.LogManager;
import ch.ethz.sis.microservices.download.server.logging.log4j.Log4J2LogFactory;
import ch.ethz.sis.microservices.download.server.startup.HttpClient;
import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

public class AbstractFileServiceTest
{
    static
    {
        // Configuring Logging
        LogManager.setLogFactory(new Log4J2LogFactory());
    }

    public static void test(String openbisURL, String serviceURL, String user, String pass, Long offset) throws Exception
    {
        // Service
        String externalDMSCode = "ADMIN-BS-MBPR28.D.ETHZ.CH-E96954A7";
        String contentCopyPath = "/Users/localadmin/obis_data/data1";
        String datasetPermId = "20180523115921026-50";
        String datasetPathToFile = "openBIS-installation-standard-technologies-SNAPSHOT-r1526484921.tar.gz";

        // Obtain session token from openBIS
        int timeout = 10000;
        IApplicationServerApi v3As = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, openbisURL, timeout);
        String sessionToken = v3As.login(user, pass);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("sessionToken", sessionToken);
        parameters.put("datasetPermId", datasetPermId);
        parameters.put("externalDMSCode", externalDMSCode);
        parameters.put("contentCopyPath", contentCopyPath);
        parameters.put("datasetPathToFile", datasetPathToFile);
        if(offset != null) {
        		parameters.put("offset", offset.toString());
        }
        

        long start = System.currentTimeMillis();
        byte[] response = HttpClient.doGet(serviceURL, parameters);
        long end = System.currentTimeMillis();
        System.out.println("Response Size: " + response.length);
        System.out.println("Time: " + (end - start));
    }
}
