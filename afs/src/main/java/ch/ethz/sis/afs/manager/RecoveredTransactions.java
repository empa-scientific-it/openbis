package ch.ethz.sis.afs.manager;

import ch.ethz.sis.afs.dto.Transaction;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RecoveredTransactions {
    private Map<UUID, Transaction> recoveredTransactions;

    public RecoveredTransactions() {
        this.recoveredTransactions = new ConcurrentHashMap<>();
    }

    public void addRecovered(Transaction transaction) {
        recoveredTransactions.put(transaction.getUuid(), transaction);
    }

    public void removeCommitted(UUID transactionId) {
        recoveredTransactions.remove(transactionId);
    }

    public boolean contains(UUID transactionId) {
        return recoveredTransactions.containsKey(transactionId);
    }

    public Transaction getRecovered(UUID transactionId) {
        return recoveredTransactions.get(transactionId);
    }

    public Set<UUID> getRecovered() {
        return recoveredTransactions.keySet();
    }

    public boolean isEmpty() {
        return recoveredTransactions.isEmpty();
    }

}
