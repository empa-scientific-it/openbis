package ch.ethz.sis.afsserver.startup;

import ch.ethz.sis.afsserver.http.HttpServer;
import ch.ethz.sis.afsserver.server.APIServer;
import ch.ethz.sis.afsserver.server.Worker;
import ch.ethz.sis.afsserver.server.observer.APIServerObserver;
import ch.ethz.sis.afsserver.server.observer.ServerObserver;
import ch.ethz.sis.shared.json.jackson.JacksonObjectMapper;
import ch.ethz.sis.shared.log.LogFactory;
import ch.ethz.sis.shared.log.LogFactoryFactory;
import ch.ethz.sis.shared.log.LogManager;
import ch.ethz.sis.shared.log.Logger;
import ch.ethz.sis.shared.pool.Factory;
import ch.ethz.sis.shared.pool.Pool;
import ch.ethz.sis.shared.startup.Configuration;
import lombok.NonNull;

public final class Server<CONNECTION, API> {

    private Logger logger;
    private Pool<Configuration, CONNECTION> connectionsPool;
    private Pool<Configuration, Worker<CONNECTION>> workersPool;
    private APIServer apiServer;
    private JacksonObjectMapper jsonObjectMapper;
    private ApiServerProxy<CONNECTION, API> apiServerProxy;
    private HttpServer httpServer;
    private boolean shutdown;
    private ServerObserver<CONNECTION> observer;

    public Server(Configuration configuration,
                  @NonNull ServerObserver<CONNECTION> serverObserver,
                  @NonNull APIServerObserver apiServerObserver) throws Exception {
        //1. Load logging plugin, Initializing LogManager
        shutdown = false;
        observer = serverObserver;
        LogFactoryFactory logFactoryFactory = new LogFactoryFactory();
        LogFactory logFactory = logFactoryFactory.create(configuration.getStringProperty(AtomicFileSystemServerParameter.logFactoryClass));
        logFactory.configure(configuration.getStringProperty(AtomicFileSystemServerParameter.logConfigFile));
        LogManager.setLogFactory(logFactory);
        logger = LogManager.getLogger(Server.class);

        logger.info("=== Server Bootstrap ===");
        logger.info("Running with java.version: " + System.getProperty("java.version"));

        // 2.1 Load DB plugin
        logger.info("Creating Connection Factory");
        Factory<Configuration, Configuration, CONNECTION> connectionFactory = configuration.getSharableInstance(AtomicFileSystemServerParameter.connectionFactoryClass);
        connectionFactory.init(configuration);

        logger.info("Creating Workers Factory");
        Factory<Configuration, Configuration, Worker<CONNECTION>> workerFactory = configuration.getSharableInstance(AtomicFileSystemServerParameter.workerFactoryClass);
        workerFactory.init(configuration);

        // 2.2 Creating workers pool
        logger.info("Creating server workers");
        int poolSize = configuration.getIntegerProperty(AtomicFileSystemServerParameter.poolSize);


        connectionsPool = new Pool<>(poolSize, configuration, connectionFactory);
        workersPool = new Pool<>(poolSize, configuration, workerFactory);

        // 2.3 Init API Server observer
        apiServerObserver.init(configuration);

        // 2.4 Creating API Server
        logger.info("Creating API server");
        Class<?> publicApiInterface = configuration.getInterfaceClass(AtomicFileSystemServerParameter.publicApiInterface);
        String interactiveSessionKey = configuration.getStringProperty(AtomicFileSystemServerParameter.apiServerInteractiveSessionKey);
        String transactionManagerKey = configuration.getStringProperty(AtomicFileSystemServerParameter.apiServerTransactionManagerKey);
        int apiServerWorkerTimeout = configuration.getIntegerProperty(AtomicFileSystemServerParameter.apiServerWorkerTimeout);
        apiServer = new APIServer(connectionsPool, workersPool, publicApiInterface, interactiveSessionKey, transactionManagerKey, apiServerWorkerTimeout, apiServerObserver);

        // 2.5 Creating JSON RPC Service
        logger.info("Creating API Server proxy");
        jsonObjectMapper = configuration.getSharableInstance(AtomicFileSystemServerParameter.jsonObjectMapperClass);
        apiServerProxy = new ApiServerProxy(apiServer, jsonObjectMapper);

        // 2.6 Creating HTTP Service
        int httpServerPort = configuration.getIntegerProperty(AtomicFileSystemServerParameter.httpServerPort);
        int maxContentLength = configuration.getIntegerProperty(AtomicFileSystemServerParameter.httpMaxContentLength);
        logger.info("Starting HTTP Service on port " + httpServerPort + " with maxContentLength " + maxContentLength);
        httpServer = configuration.getSharableInstance(AtomicFileSystemServerParameter.httpServerClass);
        String httpServerUri = configuration.getStringProperty(AtomicFileSystemServerParameter.httpServerUri);
        httpServer.start(httpServerPort, maxContentLength, httpServerUri, apiServerProxy);

        // 2.7 Init observer
        observer.init(apiServer, configuration);
        observer.beforeStartup();

        // 3 Startup
        logger.info("=== Server ready ===");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    shutdown(true);
                } catch (Exception e) {
                    logger.catching(e);
                }
            }
        });
    }

    public void shutdown(boolean gracefully) throws Exception {
        if (!shutdown) {
            observer.beforeShutdown();
            shutdown = true;
            logger.info("Shutting down - http server");
            httpServer.shutdown(gracefully);
            logger.info("Shutting down - api server");
            apiServer.shutdown();
            logger.info("Shutting down - waiting for api server workers to finish");
            if (gracefully) {
                while (apiServer.hasWorkersInUse()) {
                    Thread.sleep(100);
                }
            }
            logger.info("Shutting down - connection pool");
            connectionsPool.shutdown();
        }
    }
}
