package ch.ethz.sis.afs.manager.operation;

import ch.ethz.sis.afs.dto.Transaction;
import ch.ethz.sis.afs.dto.operation.Operation;
import ch.ethz.sis.shared.io.File;
import ch.ethz.sis.shared.io.IOUtils;
import lombok.NonNull;

public interface OperationExecutor<E extends Operation> {

    static @NonNull
    String getTransactionLogDir(Transaction transaction) {
        return IOUtils.getPath(transaction.getWriteAheadLogRoot(), transaction.getUuid().toString());
    }

    static @NonNull
    String getTransactionLog(@NonNull File transactionDir, boolean isCommitted) {
        String name;
        if (isCommitted) {
            name = "transaction-committed.json";
        } else {
            name = "transaction-prepared.json";
        }
        return IOUtils.getPath(transactionDir.getPath(), name);
    }

    static @NonNull
    String getTransactionLog(@NonNull Transaction transaction, boolean isCommitted) {
        String name;
        if (isCommitted) {
            name = "transaction-committed.json";
        } else {
            name = "transaction-prepared.json";
        }
        return IOUtils.getPath(transaction.getWriteAheadLogRoot(), transaction.getUuid().toString(), name);
    }

    static @NonNull
    String getRealPath(@NonNull Transaction transaction, @NonNull String source) {
        return IOUtils.getPath(transaction.getStorageRoot(), source.substring(1));
    }

    static @NonNull
    String getStoragePath(@NonNull Transaction transaction, @NonNull String source) {
        return source.substring(transaction.getStorageRoot().length());
    }

    static @NonNull
    String getTempPath(@NonNull Transaction transaction, @NonNull String source) {
        String transDir = getTransactionLogDir(transaction);
        return IOUtils.getPath(transDir, source);
    }

    /*
    * The first step
    * If the operation is a write operation is pre written to the transaction commit log directory.
    *
    * The idea is to reduce the commit operation to an atomic move or delete
    */
    boolean prepare(@NonNull Transaction transaction, @NonNull E operation) throws Exception;

    /*
     * Commit operation should be reduced to atomic move and delete operation to avoid
     * file system corruption, this requires sometimes duplication of files,
     * exchanging performance and space for transaction safeness
     */
    boolean commit(@NonNull Transaction transaction, @NonNull E operation) throws Exception;

}
