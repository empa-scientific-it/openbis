package ch.ethz.sis.afsserver.impl;

import ch.ethz.sis.afs.manager.TransactionConnection;
import ch.ethz.sis.afsserver.ServerClientEnvironmentFS;
import ch.ethz.sis.afsserver.api.PublicAPI;
import ch.ethz.sis.afsserver.core.PublicApiTest;
import ch.ethz.sis.afsserver.server.APIServer;
import ch.ethz.sis.afsserver.server.Worker;
import ch.ethz.sis.afsserver.server.observer.impl.DummyServerObserver;
import ch.ethz.sis.afsserver.startup.AtomicFileSystemServerParameter;
import ch.ethz.sis.afsserver.worker.ConnectionFactory;
import ch.ethz.sis.afsserver.worker.WorkerFactory;
import ch.ethz.sis.shared.pool.Pool;
import ch.ethz.sis.shared.startup.Configuration;

public class ApiServerTest extends PublicApiTest {

    @Override
    public PublicAPI getPublicAPI() throws Exception {
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
        return new PublicAPIAdapterWrapper(apiServer);
    }
}
