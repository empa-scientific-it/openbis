package ch.ethz.sis.afsserver.server;

public interface Response {
    String getId();

    Object getResult();

    Object getError();
}
