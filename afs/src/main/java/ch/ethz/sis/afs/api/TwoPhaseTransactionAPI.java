package ch.ethz.sis.afs.api;

import java.util.List;
import java.util.UUID;

public interface TwoPhaseTransactionAPI {

    //
    // 2PT API
    //

    void begin(UUID transactionId) throws Exception; // Starts or recovers an existing transaction

    Boolean prepare() throws Exception; // Prepares the transaction serializing it to disk, if is already prepared ignores the command

    void commit() throws Exception; // Commits the transaction, under any circumstances

    void rollback() throws Exception; // Rollback a begin or prepared transaction

    List<UUID> recover() throws Exception; // Returns the list of transactions on prepare state, used internally on crash recovery scenarios.

}