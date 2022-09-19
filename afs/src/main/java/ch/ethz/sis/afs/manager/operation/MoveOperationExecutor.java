package ch.ethz.sis.afs.manager.operation;

import ch.ethz.sis.afs.dto.Transaction;
import ch.ethz.sis.afs.dto.operation.MoveOperation;
import ch.ethz.sis.afs.dto.operation.OperationName;
import ch.ethz.sis.afs.exception.AFSExceptions;
import ch.ethz.sis.shared.io.IOUtils;

public class MoveOperationExecutor implements OperationExecutor<MoveOperation> {

    //
    // Singleton
    //

    private static final MoveOperationExecutor instance;

    static {
        instance = new MoveOperationExecutor();
    }

    private MoveOperationExecutor() {
    }

    public static MoveOperationExecutor getInstance() {
        return instance;
    }

    //
    // Operation
    //

    @Override
    public boolean prepare(Transaction transaction, MoveOperation operation) throws Exception {
        if (!IOUtils.exists(operation.getSource())) {
            AFSExceptions.throwInstance(AFSExceptions.PathNotInStore, OperationName.Move.name(), operation.getSource());
        }
        if (IOUtils.exists(operation.getTarget())) {
            AFSExceptions.throwInstance(AFSExceptions.PathInStore, OperationName.Move.name(), operation.getTarget());
        }
        return true;
    }

    @Override
    public boolean commit(Transaction transaction, MoveOperation operation) throws Exception {
        if (IOUtils.exists(operation.getSource())) {
            if (!IOUtils.exists(operation.getTarget())) {
                IOUtils.createDirectories(IOUtils.getParentPath(operation.getTarget()));
            }
            IOUtils.move(operation.getSource(), operation.getTarget());
        }
        return false;
    }
}
