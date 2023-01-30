package ch.ethz.sis.afsserver.worker;

import ch.ethz.sis.afs.api.TransactionalFileSystem;
import ch.ethz.sis.afsserver.server.performance.PerformanceAuditor;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class WorkerContext {
    private PerformanceAuditor performanceAuditor;
    private UUID transactionId;
    private TransactionalFileSystem connection;
    private String sessionToken;
    private Boolean sessionExists;
    private boolean transactionManagerMode;
}
