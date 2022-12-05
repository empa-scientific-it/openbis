package ch.ethz.sis.afsserver.impl;


import ch.ethz.sis.afsserver.core.AbstractPublicAPIWrapper;
import ch.ethz.sis.afsserver.server.APIServer;
import ch.ethz.sis.afsserver.server.Response;
import ch.ethz.sis.afsserver.server.impl.ApiRequest;
import ch.ethz.sis.afsserver.server.impl.ApiResponseBuilder;
import ch.ethz.sis.afsserver.server.performance.PerformanceAuditor;
import ch.ethz.sis.shared.log.LogManager;
import ch.ethz.sis.shared.log.Logger;

import java.util.Map;
import java.util.UUID;

public class PublicAPIAdapterWrapper extends AbstractPublicAPIWrapper {

    private static final Logger logger = LogManager.getLogger(PublicAPIAdapterWrapper.class);

    private APIServer apiServer;
    private final ApiResponseBuilder apiResponseBuilder;

    public PublicAPIAdapterWrapper(APIServer apiServerAdapter) {
        this.apiServer = apiServerAdapter;
        this.apiResponseBuilder = new ApiResponseBuilder();
    }

    public <E> E process(String method, Map<String, Object> args) {
        PerformanceAuditor performanceAuditor = new PerformanceAuditor();
        // Random Session token just works for tests with dummy authentication
        ApiRequest request = new ApiRequest("test", method, args, UUID.randomUUID().toString(), null, null);

        try {
            Response response = apiServer.processOperation(request, apiResponseBuilder, performanceAuditor);
            return (E) response.getResult();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

}
