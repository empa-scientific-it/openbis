package ch.ethz.sis.microservices.download.server.logging;

public class LogManager {
    private static LogFactory factory;

    private static boolean isNotInitialized() {
        return factory == null;
    }

    public static void setLogFactory(LogFactory logFactory) {
        if (isNotInitialized()) {
            factory = logFactory;
        }
    }

    public static <T> Logger getLogger(Class<T> clazz) {
        if (isNotInitialized()) {
            throw new RuntimeException("LogFactory not initialized.");
        }
        return factory.getLogger(clazz);
    }
}
