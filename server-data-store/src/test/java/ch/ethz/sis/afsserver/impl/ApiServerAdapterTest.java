/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.ethz.sis.afsserver.impl;

import ch.ethz.sis.afsserver.ServerClientEnvironmentFS;
import ch.ethz.sis.afsapi.api.PublicAPI;
import ch.ethz.sis.afsserver.server.APIServer;
import ch.ethz.sis.afsserver.server.impl.ApiServerAdapter;
import ch.ethz.sis.afsserver.startup.AtomicFileSystemServerParameter;
import ch.ethz.sis.afsjson.JsonObjectMapper;
import ch.ethz.sis.shared.startup.Configuration;
import org.junit.Test;

import java.util.UUID;

public class ApiServerAdapterTest extends ApiServerTest {


    public UUID sessionToken = UUID.randomUUID();

    public PublicAPI getPublicAPI(String interactiveSessionKey, String transactionManagerKey) throws Exception {
        APIServer apiServer = getAPIServer();
        Configuration configuration = ServerClientEnvironmentFS.getInstance().getDefaultServerConfiguration();
        JsonObjectMapper jsonObjectMapper = configuration.getSharableInstance(AtomicFileSystemServerParameter.jsonObjectMapperClass);
        ApiServerAdapter apiServerAdapter = new ApiServerAdapter(apiServer, jsonObjectMapper);
        return new APIServerAdapterWrapper(apiServerAdapter, interactiveSessionKey, transactionManagerKey, this.sessionToken.toString());
    }

    @Override
    public PublicAPI getPublicAPI() throws Exception {
        APIServer apiServer = getAPIServer();
        Configuration configuration = ServerClientEnvironmentFS.getInstance().getDefaultServerConfiguration();
        JsonObjectMapper jsonObjectMapper = configuration.getSharableInstance(AtomicFileSystemServerParameter.jsonObjectMapperClass);
        ApiServerAdapter apiServerAdapter = new ApiServerAdapter(apiServer, jsonObjectMapper);
        return new APIServerAdapterWrapper(apiServerAdapter, null, null, sessionToken.toString());
    }

    @Test
    public void operation_state_begin_succeed() throws Exception {
        PublicAPI publicAPI = getPublicAPI("1234", "5678");
        publicAPI.begin(sessionToken);
    }

    @Test
    public void operation_state_prepare_succeed() throws Exception {
        PublicAPI publicAPI = getPublicAPI("1234", "5678");
        publicAPI.begin(sessionToken);
        publicAPI.prepare();
    }

    @Test
    public void operation_state_rollback_succeed() throws Exception {
        PublicAPI publicAPI = getPublicAPI("1234", "5678");
        publicAPI.begin(sessionToken);
        publicAPI.prepare();
        publicAPI.rollback();
    }

    @Test
    public void operation_state_commit_succeed() throws Exception {
        PublicAPI publicAPI = getPublicAPI("1234", "5678");
        publicAPI.begin(sessionToken);
        publicAPI.commit();
    }

    @Test
    public void operation_state_commitPrepared_succeed() throws Exception {
        PublicAPI publicAPI = getPublicAPI("1234", "5678");
        publicAPI.begin(sessionToken);
        publicAPI.prepare();
        publicAPI.commit();
    }

    @Test
    public void operation_state_commit_reuse_succeed() throws Exception {
        PublicAPI publicAPI = getPublicAPI("1234", "5678");
        publicAPI.begin(sessionToken);
        publicAPI.prepare();
        publicAPI.commit();
        publicAPI.begin(sessionToken);
    }

    @Test
    public void operation_state_rollback_reuse_succeed() throws Exception {
        PublicAPI publicAPI = getPublicAPI("1234", "5678");
        publicAPI.begin(sessionToken);
        publicAPI.prepare();
        publicAPI.rollback();
        publicAPI.begin(sessionToken);
    }

    @Test(expected = RuntimeException.class)
    public void operation_state_begin_reuse_fails() throws Exception {
        PublicAPI publicAPI = getPublicAPI("1234", "5678");
        publicAPI.begin(sessionToken);
        publicAPI.begin(sessionToken);
    }

    @Test
    public void operation_state_prepare_reuse_succeed() throws Exception {
        PublicAPI publicAPI = getPublicAPI("1234", "5678");
        publicAPI.begin(sessionToken);
        publicAPI.prepare();
        publicAPI.begin(sessionToken);
    }
}
