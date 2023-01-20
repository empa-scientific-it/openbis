package ch.ethz.sis.microservices.download.server.logging;


public interface Logger {
    //
    // Trace API - Used for debugging
    //
    void traceAccess(String message, Object... args);

    <R> R traceExit(R arg);

    void catching(Throwable ex);

    //
    // INFO API
    //
    void info(String message, Object... args);
}