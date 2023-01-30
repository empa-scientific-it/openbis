package ch.ethz.sis.afsserver.server;

public interface ResponseBuilder<E extends Response> {
    E build(String id, Object result);
}