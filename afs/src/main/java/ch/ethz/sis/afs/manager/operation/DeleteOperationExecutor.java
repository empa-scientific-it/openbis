package ch.ethz.sis.afs.manager.operation;

import ch.ethz.sis.shared.io.IOUtils;
import ch.ethz.sis.afs.dto.Transaction;
import ch.ethz.sis.afs.dto.operation.DeleteOperation;
import ch.ethz.sis.afs.dto.operation.OperationName;
import ch.ethz.sis.afs.exception.AFSExceptions;

import static ch.ethz.sis.afs.exception.AFSExceptions.PathNotInStore;

public class DeleteOperationExecutor implements OperationExecutor<DeleteOperation> {

    //
    // Singleton
    //

    private static final DeleteOperationExecutor instance;

    static {
        instance = new DeleteOperationExecutor();
    }

    private DeleteOperationExecutor() {
    }

    public static DeleteOperationExecutor getInstance() {
        return instance;
    }

    //
    // Operation
    //

    @Override
    public boolean prepare(Transaction transaction, DeleteOperation operation) throws Exception {
        if (!IOUtils.exists(operation.getSource())) {
            AFSExceptions.throwInstance(PathNotInStore, OperationName.Move.name(), operation.getSource());
        }
        return true;
    }

    @Override
    public boolean commit(Transaction transaction, DeleteOperation operation) throws Exception {
        if (IOUtils.exists(operation.getSource())) {
            IOUtils.delete(operation.getSource());
        }
        return true;
    }
}
