package ch.ethz.sis.afsserver.worker;

import ch.ethz.sis.afs.manager.TransactionManager;
import ch.ethz.sis.afs.startup.AtomicFileSystemParameter;
import ch.ethz.sis.shared.pool.AbstractFactory;
import ch.ethz.sis.shared.startup.Configuration;
import ch.ethz.sis.afs.manager.TransactionConnection;

public class ConnectionFactory extends AbstractFactory<Configuration, Configuration, TransactionConnection> {

    private TransactionManager transactionManager;

    @Override
    public void init(Configuration configuration) throws Exception {
        transactionManager = new TransactionManager(
                configuration.getSharableInstance(AtomicFileSystemParameter.jsonObjectMapperClass),
                configuration.getStringProperty(AtomicFileSystemParameter.writeAheadLogRoot),
                configuration.getStringProperty(AtomicFileSystemParameter.storageRoot));
        transactionManager.reCommitTransactionsAfterCrash();
    }

    @Override
    public TransactionConnection create(Configuration configuration) throws Exception {
        return transactionManager.getTransactionConnection();
    }
}
