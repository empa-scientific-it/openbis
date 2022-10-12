package ch.ethz.sis.afs.dto;

import ch.ethz.sis.afs.dto.operation.Operation;
import lombok.Value;

import java.util.List;
import java.util.UUID;

/*
 * Transactions are modeled as dtos and are serialized to disk on the commit step
 * before finally executing with the intention they can be replayed in case of system failure.
 */
@Value
public class Transaction {

    /*
     * Base directory where the commitLog directory will be written
     */
    private String writeAheadLogRoot;

    /*
     * Base directory where the final storage is
     */
    private String storageRoot;

    /*
     * Identifier of the transaction, will be used as directory name on the commitLog
     */
    private UUID uuid;


    private List<Operation> operations;
}
