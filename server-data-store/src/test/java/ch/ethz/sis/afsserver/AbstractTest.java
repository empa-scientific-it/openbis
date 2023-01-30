package ch.ethz.sis.afsserver;

import ch.ethz.sis.afsserver.startup.AtomicFileSystemServerParameter;
import ch.ethz.sis.shared.log.LogFactory;
import ch.ethz.sis.shared.log.LogFactoryFactory;
import ch.ethz.sis.shared.log.LogManager;

import java.io.File;


public class AbstractTest {
    static {
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static private void init() throws Exception {
        System.out.println("Current Working Directory: " + (new File("")).getCanonicalPath());
        // Initializing LogManager
        LogFactoryFactory logFactoryFactory = new LogFactoryFactory();
        LogFactory logFactory = logFactoryFactory.create(ServerClientEnvironmentFS.getInstance().getDefaultServerConfiguration().getStringProperty(AtomicFileSystemServerParameter.logFactoryClass));
        LogManager.setLogFactory(logFactory);
    }
}
