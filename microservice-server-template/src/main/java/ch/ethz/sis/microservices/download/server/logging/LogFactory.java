package ch.ethz.sis.microservices.download.server.logging;

public interface LogFactory {
    <T> Logger getLogger(Class<T> clazz);
}