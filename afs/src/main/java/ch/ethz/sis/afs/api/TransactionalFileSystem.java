package ch.ethz.sis.afs.api;

public interface TransactionalFileSystem extends TwoPhaseTransactionAPI, OperationsAPI {
    boolean isTwoPhaseCommit();
}
