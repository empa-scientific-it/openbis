/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
