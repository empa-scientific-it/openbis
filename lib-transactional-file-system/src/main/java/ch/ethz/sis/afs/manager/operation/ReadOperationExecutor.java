package ch.ethz.sis.afs.manager.operation;

import ch.ethz.sis.afs.dto.Transaction;
import ch.ethz.sis.afs.dto.operation.ReadOperation;
import ch.ethz.sis.shared.io.IOUtils;
import lombok.NonNull;

public class ReadOperationExecutor implements NonModifyingOperationExecutor<ReadOperation> {
    //
    // Singleton
    //

    private static final ReadOperationExecutor instance;

    static {
        instance = new ReadOperationExecutor();
    }

    private ReadOperationExecutor() {
    }

    public static ReadOperationExecutor getInstance() {
        return instance;
    }

    @Override
    public byte[] executeOperation(@NonNull Transaction transaction, @NonNull ReadOperation operation) throws Exception {
        return IOUtils.read(operation.getSource(), operation.getOffset(), operation.getLimit());
    }
}
