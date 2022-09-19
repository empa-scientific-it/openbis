package ch.ethz.sis.afs;

import ch.ethz.sis.shared.log.LogFactory;
import ch.ethz.sis.shared.log.LogManager;
import ch.ethz.sis.shared.log.log4j2.Log4J2LogFactory;


public class AbstractTest {

    static {
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static private void init() throws Exception {
        // Initializing LogManager
        LogFactory logFactory = new Log4J2LogFactory();
        LogManager.setLogFactory(logFactory);
    }

}

