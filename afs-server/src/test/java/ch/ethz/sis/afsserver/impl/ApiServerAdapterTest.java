package ch.ethz.sis.afsserver.impl;

import ch.ethz.sis.afsserver.ServerClientEnvironmentFS;
import ch.ethz.sis.afsserver.api.PublicAPI;
import ch.ethz.sis.afsserver.server.APIServer;
import ch.ethz.sis.afsserver.server.impl.ApiServerAdapter;
import ch.ethz.sis.afsserver.startup.AtomicFileSystemServerParameter;
import ch.ethz.sis.shared.json.JSONObjectMapper;
import ch.ethz.sis.shared.startup.Configuration;

public class ApiServerAdapterTest extends ApiServerTest {

    @Override
    public PublicAPI getPublicAPI() throws Exception {
        APIServer apiServer = getAPIServer();
        Configuration configuration = ServerClientEnvironmentFS.getInstance().getDefaultServerConfiguration();
        JSONObjectMapper jsonObjectMapper = configuration.getSharableInstance(AtomicFileSystemServerParameter.jsonObjectMapperClass);
        ApiServerAdapter apiServerAdapter = new ApiServerAdapter(apiServer, jsonObjectMapper);
        return new APIServerAdapterWrapper(apiServerAdapter, jsonObjectMapper);
    }
}
