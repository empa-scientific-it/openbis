package ch.ethz.sis.afs.manager.operation;

import ch.ethz.sis.afs.api.dto.File;
import ch.ethz.sis.afs.dto.Transaction;
import ch.ethz.sis.afs.dto.operation.ListOperation;
import ch.ethz.sis.shared.io.IOUtils;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ListOperationExecutor implements NonModifyingOperationExecutor<ListOperation> {
    //
    // Singleton
    //

    private static final ListOperationExecutor instance;

    static {
        instance = new ListOperationExecutor();
    }

    private ListOperationExecutor() {
    }

    public static ListOperationExecutor getInstance() {
        return instance;
    }

    @Override
    public List<File> executeOperation(@NonNull Transaction transaction, @NonNull ListOperation operation) throws Exception {
        List<File> files = IOUtils.list(operation.getSource(), operation.isRecursively());
        List<File> filesFromRoot = new ArrayList<>();
        for (File file : files) {
            filesFromRoot.add(file.toBuilder().path(OperationExecutor.getStoragePath(transaction, file.getPath())).build());
        }
        return filesFromRoot;
    }
}
