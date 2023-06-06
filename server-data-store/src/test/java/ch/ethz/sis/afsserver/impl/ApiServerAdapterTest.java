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

public class ApiServerAdapterTest extends ApiServerTest {

    @Override
    public PublicAPI getPublicAPI() throws Exception {
        APIServer apiServer = getAPIServer();
        Configuration configuration = ServerClientEnvironmentFS.getInstance().getDefaultServerConfiguration();
        JsonObjectMapper jsonObjectMapper = configuration.getSharableInstance(AtomicFileSystemServerParameter.jsonObjectMapperClass);
        ApiServerAdapter apiServerAdapter = new ApiServerAdapter(apiServer, jsonObjectMapper);
        return new APIServerAdapterWrapper(apiServerAdapter);
    }
}
