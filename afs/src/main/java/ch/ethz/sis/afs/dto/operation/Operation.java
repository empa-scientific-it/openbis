package ch.ethz.sis.afs.dto.operation;

import ch.ethz.sis.afs.dto.Lock;

import java.util.List;
import java.util.UUID;

public interface Operation {
    /*
     * Before executing, these locks are set on LockManager by the TransactionConnection
     * With all the other locks from all the other operation of the transaction.
     *
     * The same resource can have several read locks (Shared) or only one write lock (Exclusive)
     */
    List<Lock<UUID, String>> getLocks();

    UUID getOwner();

    OperationName getName();
}