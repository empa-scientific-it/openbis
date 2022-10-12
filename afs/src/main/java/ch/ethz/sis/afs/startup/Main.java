package ch.ethz.sis.afs.startup;

import ch.ethz.sis.afs.manager.TransactionManager;
import ch.ethz.sis.shared.json.JSONObjectMapper;
import ch.ethz.sis.shared.log.LogFactory;
import ch.ethz.sis.shared.log.LogFactoryFactory;
import ch.ethz.sis.shared.log.LogManager;
import ch.ethz.sis.shared.startup.Configuration;

import java.io.File;
import java.util.List;

public class Main {

    public static List getParameterClasses() {
        return List.of(AtomicFileSystemParameter.class);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Current Working Directory: " + (new File("")).getCanonicalPath());
        Configuration configuration = new Configuration(getParameterClasses(), "../openbis-afs/src/main/resources/afs-config.properties");

        // Initializing LogManager
        LogFactoryFactory logFactoryFactory = new LogFactoryFactory();
        LogFactory logFactory = logFactoryFactory.create(configuration.getStringProperty(AtomicFileSystemParameter.logFactoryClass));
        logFactory.configure(configuration.getStringProperty(AtomicFileSystemParameter.logConfigFile));
        LogManager.setLogFactory(logFactory);

        //
        JSONObjectMapper jsonObjectMapper = configuration.getSharableInstance(AtomicFileSystemParameter.jsonObjectMapperClass);
        String writeAheadLogRoot = configuration.getStringProperty(AtomicFileSystemParameter.writeAheadLogRoot);
        String storageRoot = configuration.getStringProperty(AtomicFileSystemParameter.storageRoot);

        TransactionManager transactionManager = new TransactionManager(jsonObjectMapper, writeAheadLogRoot, storageRoot);
        transactionManager.reCommitTransactionsAfterCrash();
    }
}
