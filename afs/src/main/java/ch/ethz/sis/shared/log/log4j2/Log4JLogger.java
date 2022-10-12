package ch.ethz.sis.shared.log.log4j2;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.AbstractLogger;
import org.apache.logging.log4j.spi.ExtendedLoggerWrapper;

class Log4JLogger extends ExtendedLoggerWrapper implements ch.ethz.sis.shared.log.Logger {

    private final String FQCN;

    Log4JLogger(final Logger logger) {
        super((AbstractLogger) logger, logger.getName(), logger.getMessageFactory());
        FQCN = this.getClass().getName();
    }

    @Override
    public void traceAccess(String message, Object... args) {
        this.logMessage(FQCN,
                Level.TRACE,
                ENTRY_MARKER,
                entryMsg(message, args),
                (Throwable) null);
    }

    @Override
    public <R> R traceExit(R result) {
        this.logMessage(FQCN,
                Level.TRACE,
                EXIT_MARKER,
                exitMsg((String) null, result),
                (Throwable) null);
        return result;
    }

    @Override
    public void catching(Throwable ex) {
        this.logMessage(FQCN,
                Level.ERROR,
                CATCHING_MARKER,
                catchingMsg(ex),
                ex);
    }

    @Override
    public <T extends Throwable> T throwing(T ex) {
        this.logMessage(FQCN,
                Level.ERROR,
                THROWING_MARKER,
                throwingMsg(ex),
                ex);
        return ex;
    }

    @Override
    public void info(String message, Object... args) {
        this.logMessage(FQCN,
                Level.INFO,
                null,
                logger.getMessageFactory().newMessage(message, args),
                (Throwable) null);
    }
}
