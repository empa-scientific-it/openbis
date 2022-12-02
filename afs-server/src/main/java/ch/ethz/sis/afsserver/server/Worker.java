package ch.ethz.sis.afsserver.server;

import ch.ethz.sis.afsserver.api.PublicAPI;
import ch.ethz.sis.afsserver.worker.PerformanceAuditor;

public interface Worker<CONNECTION> extends PublicAPI {
    void createContext(PerformanceAuditor performanceAuditor);

    void cleanContext();

    void setConnection(CONNECTION connection);

    CONNECTION getConnection();

    void cleanConnection() throws Exception;

    void setSessionToken(String sessionToken) throws Exception;

    String getSessionToken();

    void setTransactionManagerMode(boolean transactionManagerMode);

    boolean isTransactionManagerMode();

}
