package ch.ethz.sis.afs.manager.operation;

import ch.ethz.sis.afs.dto.Transaction;
import ch.ethz.sis.afs.dto.operation.Operation;
import lombok.NonNull;

public interface NonModifyingOperationExecutor<E extends Operation> {
    <RESULT> RESULT executeOperation(@NonNull Transaction transaction, @NonNull E operation) throws Exception;
}
