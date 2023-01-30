package ch.ethz.sis.afsserver.server;

import java.util.Map;
import java.util.UUID;

public interface Request {
    String getId();

    String getMethod();

    Map<String, Object> getParams();

    String getSessionToken();

    String getInteractiveSessionKey();

    String getTransactionManagerKey();
}
