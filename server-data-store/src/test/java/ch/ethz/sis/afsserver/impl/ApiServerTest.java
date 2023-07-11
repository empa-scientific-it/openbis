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

import ch.ethz.sis.afs.manager.TransactionConnection;
import ch.ethz.sis.afsserver.ServerClientEnvironmentFS;
import ch.ethz.sis.afsapi.api.PublicAPI;
import ch.ethz.sis.afsserver.core.PublicApiTest;
import ch.ethz.sis.afsserver.server.APIServer;
import ch.ethz.sis.afsserver.server.Worker;
import ch.ethz.sis.afsserver.server.observer.impl.DummyServerObserver;
import ch.ethz.sis.afsserver.startup.AtomicFileSystemServerParameter;
import ch.ethz.sis.afsserver.worker.ConnectionFactory;
import ch.ethz.sis.afsserver.worker.WorkerFactory;
import ch.ethz.sis.shared.pool.Pool;
import ch.ethz.sis.shared.startup.Configuration;

import java.util.UUID;

public class ApiServerTest extends PublicApiTest {

    protected APIServer getAPIServer() throws Exception {
        Configuration configuration = ServerClientEnvironmentFS.getInstance().getDefaultServerConfiguration();

        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.init(configuration);

        WorkerFactory workerFactory = new WorkerFactory();
        int poolSize = configuration.getIntegerProperty(AtomicFileSystemServerParameter.poolSize);

        Pool<Configuration, TransactionConnection> connectionsPool = new Pool<>(poolSize, configuration, connectionFactory);
        Pool<Configuration, Worker> workersPool = new Pool<>(poolSize, configuration, workerFactory);

        String interactiveSessionKey = configuration.getStringProperty(AtomicFileSystemServerParameter.apiServerInteractiveSessionKey);
        String transactionManagerKey = configuration.getStringProperty(AtomicFileSystemServerParameter.apiServerTransactionManagerKey);
        int apiServerWorkerTimeout = configuration.getIntegerProperty(AtomicFileSystemServerParameter.apiServerWorkerTimeout);
        DummyServerObserver observer = new DummyServerObserver();
        observer.init(configuration);
        APIServer apiServer = new APIServer(connectionsPool, workersPool, PublicAPI.class, interactiveSessionKey, transactionManagerKey, apiServerWorkerTimeout, observer);
        observer.init(apiServer, configuration);
        return apiServer;
    }

    @Override
    public PublicAPI getPublicAPI() throws Exception {
        String sessionToken = UUID.randomUUID().toString();
        return new APIServerWrapper(getAPIServer(), null, null, sessionToken);
    }

    @Override
    public PublicAPI getPublicAPI(String interactiveSessionKey, String transactionManagerKey)
            throws Exception
    {
        String sessionToken = UUID.randomUUID().toString();
        return new APIServerWrapper(getAPIServer(), interactiveSessionKey, transactionManagerKey, sessionToken);
    }
}
