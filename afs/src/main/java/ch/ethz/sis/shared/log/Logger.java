package ch.ethz.sis.shared.log;


public interface Logger {
    //
    // Trace API - Used for debugging, not intended for production usage
    //
    void traceAccess(String message, Object... args);

    <R> R traceExit(R arg);

    //
    // Catching API - Used to record errors
    //
    void catching(Throwable ex);

    <T extends Throwable> T throwing(T ex);

    //
    // INFO API - Used to record important system events
    //
    void info(String message, Object... args);
}