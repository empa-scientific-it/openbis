package ch.ethz.sis.afsserver.startup;

import ch.ethz.sis.afs.manager.TransactionConnection;
import ch.ethz.sis.afsserver.server.APIServer;
import ch.ethz.sis.afsserver.server.Worker;
import ch.ethz.sis.afsserver.server.observer.APIServerObserver;
import ch.ethz.sis.afsserver.server.observer.ServerObserver;
import ch.ethz.sis.shared.startup.Configuration;

import java.util.Map;

public class DummyServerObserver implements ServerObserver<TransactionConnection>, APIServerObserver<TransactionConnection> {

    @Override
    public void init(Configuration configuration) throws Exception {

    }

    @Override
    public void beforeAPICall(Worker<TransactionConnection> worker, String method, Map<String, Object> params) throws Exception {

    }

    @Override
    public void afterAPICall(Worker<TransactionConnection> worker, String method, Map<String, Object> params) throws Exception {

    }

    @Override
    public void init(APIServer<TransactionConnection, ?, ?, ?> apiServer, Configuration configuration) throws Exception {

    }

    @Override
    public void beforeStartup() throws Exception {

    }

    @Override
    public void beforeShutdown() throws Exception {

    }
}
