/*
 * Copyright 2022 ETH Zürich, SIS
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

import ch.ethz.sis.afs.api.TransactionalFileSystem;
import ch.ethz.sis.afs.dto.Transaction;
import ch.ethz.sis.afs.exception.AFSExceptions;
import ch.ethz.sis.shared.io.File;
import ch.ethz.sis.shared.io.IOUtils;
import ch.ethz.sis.shared.json.JSONObjectMapper;
import ch.ethz.sis.afs.dto.Lock;
import ch.ethz.sis.afs.dto.LockType;
import ch.ethz.sis.afs.dto.operation.CopyOperation;
import ch.ethz.sis.afs.dto.operation.DeleteOperation;
import ch.ethz.sis.afs.dto.operation.MoveOperation;
import ch.ethz.sis.afs.dto.operation.Operation;
import ch.ethz.sis.afs.dto.operation.OperationName;
import ch.ethz.sis.afs.dto.operation.WriteOperation;
import ch.ethz.sis.afs.manager.operation.CopyOperationExecutor;
import ch.ethz.sis.afs.manager.operation.DeleteOperationExecutor;
import ch.ethz.sis.afs.manager.operation.MoveOperationExecutor;
import ch.ethz.sis.afs.manager.operation.OperationExecutor;
import ch.ethz.sis.afs.manager.operation.WriteOperationExecutor;

import java.util.*;

import static ch.ethz.sis.shared.collection.List.safe;

public class TransactionConnection implements TransactionalFileSystem {

    private static final Map<OperationName, OperationExecutor> operationExecutors;

    static {
        operationExecutors = Map.of(OperationName.Copy, CopyOperationExecutor.getInstance(),
                OperationName.Delete, DeleteOperationExecutor.getInstance(),
                OperationName.Move, MoveOperationExecutor.getInstance(),
                OperationName.Write, WriteOperationExecutor.getInstance());
    }

    private LockManager<UUID, String> lockManager;
    private JSONObjectMapper jsonObjectMapper;
    private Transaction transaction;
    private State state;
    private String writeAheadLogRoot;
    private String storageRoot;
    private RecoveredTransactions recoveredTransactions;

    /*
     * Used only to create new transactions
     */
    TransactionConnection(LockManager<UUID, String> lockManager,
                          JSONObjectMapper jsonObjectMapper,
                          String writeAheadLogRoot,
                          String storageRoot,
                          RecoveredTransactions recoveredTransactions) {
        this(lockManager, jsonObjectMapper, null);
        this.writeAheadLogRoot = writeAheadLogRoot;
        this.storageRoot = storageRoot;
        this.recoveredTransactions = recoveredTransactions;
    }

    /*
     * Can be used to recover a committed transactions after a crash
     */
    TransactionConnection(LockManager<UUID, String> lockManager,
                          JSONObjectMapper jsonObjectMapper,
                          Transaction transaction) {
        this.lockManager = lockManager;
        this.jsonObjectMapper = jsonObjectMapper;
        this.transaction = transaction;

        if (transaction != null) {
            state = State.Prepare;
            for (Operation operation : transaction.getOperations()) {
                boolean locksObtained = lockManager.add(operation.getLocks());
                if (!locksObtained) {
                    AFSExceptions.throwInstance(AFSExceptions.OperationCantBeRecovered, transaction.getUuid().toString(), operation.getName().toString());
                }
            }
        } else {
            state = State.New;
        }
    }

    //
    // Transaction control
    //

    public Transaction getTransaction() {
        return transaction;
    }

    public State getState() {
        return state;
    }

    @Override
    public void begin(UUID transactionId) throws Exception {
        /*
         * This resets the transaction, in practice to make the connection reusable across workers
         */
        if (state == State.New) {
            // New just created transaction
        } else if (state == State.Executed || state == State.Rollback || state == State.Prepare) {
            // Clean transaction, can ve reused
            transaction = null;
            state = State.New;
            copiedSourceToTarget.clear();
            copiedTargetToSource.clear();
            movedSourceToTarget.clear();
            movedTargetToSource.clear();
            written.clear();
            deleted.clear();
        } else {
            AFSExceptions.throwInstance(AFSExceptions.TransactionReuse, transaction.getUuid(), state.name());
        }

        if (recoveredTransactions.contains(transactionId)) {
            transaction = recoveredTransactions.getRecovered(transactionId);
            state = State.Prepare;
        } else if (state == State.New) {
            transaction = new Transaction(writeAheadLogRoot, storageRoot, transactionId, new ArrayList<>());
            String transactionLogDir = OperationExecutor.getTransactionLogDir(transaction);
            IOUtils.createDirectories(transactionLogDir);
            state = State.Begin;
        }
    }

    private void writeTransactionLog(boolean isCommitted) throws Exception {
        byte[] bytes = jsonObjectMapper.writeValue(transaction);
        String transactionLog = OperationExecutor.getTransactionLog(transaction, isCommitted);
        IOUtils.createFile(transactionLog);
        IOUtils.write(transactionLog, 0, bytes);
    }

    @Override
    public Boolean prepare() throws Exception {
        if (state == State.Begin) {
            writeTransactionLog(false);
            state = State.Prepare;
            if (!recoveredTransactions.contains(transaction.getUuid())) {
                recoveredTransactions.addRecovered(transaction);
            }
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    @Override
    public void commit() throws Exception {
        if (state == State.Begin || state == State.Prepare) {
            writeTransactionLog(true);
            state = State.Commit;
        }

        if (state == State.Commit) {
            for (Operation operation : transaction.getOperations()) {
                operationExecutors.get(operation.getName()).commit(transaction, operation);
            }
            cleanTransaction();
            state = State.Executed;
        }
    }

    @Override
    public void rollback() throws Exception {
        if (state == State.Begin || state == State.Prepare) {
            cleanTransaction();
            state = State.Rollback;
        }
    }

    @Override
    public List<UUID> recover() throws Exception {
        if (recoveredTransactions == null) {
            return List.of();
        } else {
            Set<UUID> recovered = recoveredTransactions.getRecovered();
            return new ArrayList<>(recovered);
        }
    }

    private void cleanTransaction() throws Exception {
        String transactionLogDir = OperationExecutor.getTransactionLogDir(transaction);
        IOUtils.delete(transactionLogDir);

        for (Operation operation : transaction.getOperations()) {
            lockManager.remove(operation.getLocks());
        }
        if (recoveredTransactions.contains(transaction.getUuid())) {
            recoveredTransactions.removeCommitted(transaction.getUuid());
        }
    }

    //
    // Operations
    //

    @Override
    public List<File> list(String source, boolean recursively) throws Exception {
        source = getSafePath(OperationName.List, source);
        validateOperationAndPaths(OperationName.List, source, null);
        if (IOUtils.getFile(source).getDirectory()) {
            List<Lock<UUID, String>> locks = List.of(new Lock<>(transaction.getUuid(), source, LockType.Shared));
            boolean locksObtained = lockManager.add(locks);
            if (locksObtained) {
                List<File> files = IOUtils.list(source, recursively);
                List<File> filesFromRoot = new ArrayList<>();
                for (File file : files) {
                    filesFromRoot.add(file.toBuilder().path(OperationExecutor.getStoragePath(transaction, file.getPath())).build());
                }
                return filesFromRoot;
            } else {
                AFSExceptions.throwInstance(AFSExceptions.PathBusy, OperationName.List, source);
            }
        } else {
            AFSExceptions.throwInstance(AFSExceptions.PathNotDirectory, OperationName.List, source);
        }
        return null; // Unreachable statement
    }

    @Override
    public byte[] read(String source, long offset, int limit) throws Exception {
        source = getSafePath(OperationName.Read, source);
        validateOperationAndPaths(OperationName.Read, source, null);
        if (IOUtils.getFile(source).getDirectory()) {
            AFSExceptions.throwInstance(AFSExceptions.PathIsDirectory, OperationName.Read.name(), source);
        }
        List<Lock<UUID, String>> locks = List.of(new Lock<>(transaction.getUuid(), source, LockType.Shared));
        boolean locksObtained = lockManager.add(locks);
        if (locksObtained) {
            byte[] result = IOUtils.read(source, offset, limit);
            lockManager.remove(locks);
            return result;
        } else {
            AFSExceptions.throwInstance(AFSExceptions.PathBusy, OperationName.Read, source);
        }
        return null; // Unreachable statement
    }

    private final Set<String> written = new HashSet<>();

    @Override
    public boolean write(String source, long offset, byte[] data, byte[] md5Hash) throws Exception {
        source = getSafePath(OperationName.Write, source);
        WriteOperation operation = new WriteOperation(transaction.getUuid(), source, offset, data, md5Hash);
        boolean prepared = prepare(operation, source, null);
        if (prepared) {
            written.add(source);
        }
        return prepared;
    }

    private final Set<String> deleted = new HashSet<>();

    @Override
    public boolean delete(String source) throws Exception {
        source = getSafePath(OperationName.Delete, source);
        DeleteOperation operation = new DeleteOperation(transaction.getUuid(), source);
        boolean prepared = prepare(operation, source, null);
        if (prepared) {
            deleted.add(source);
        }
        return prepared;
    }

    private final Map<String, String> copiedSourceToTarget = new HashMap<>();
    private final Map<String, String> copiedTargetToSource = new HashMap<>();

    @Override
    public boolean copy(String source, String target) throws Exception {
        source = getSafePath(OperationName.Copy, source);
        target = getSafePath(OperationName.Copy, target);
        CopyOperation operation = new CopyOperation(transaction.getUuid(), source, target);
        boolean prepared = prepare(operation, source, target);
        if (prepared) {
            copiedSourceToTarget.put(source, target);
            copiedTargetToSource.put(target, source);
        }
        return prepared;
    }

    private final Map<String, String> movedSourceToTarget = new HashMap<>();
    private final Map<String, String> movedTargetToSource = new HashMap<>();

    @Override
    public boolean move(String source, String target) throws Exception {
        source = getSafePath(OperationName.Move, source);
        target = getSafePath(OperationName.Move, target);
        MoveOperation operation = new MoveOperation(transaction.getUuid(), source, target);
        boolean prepared = prepare(operation, source, target);
        if (prepared) {
            movedSourceToTarget.put(source, target);
            movedTargetToSource.put(target, source);
        }
        return prepared;
    }

    private static final String RELATIVE = "/../";
    private static final String ROOT = "/";

    private boolean prepare(Operation operation, String source, String target) throws Exception {
        validateOperationAndPaths(operation.getName(), source, target);
        boolean locksObtained = false;
        boolean prepared = false;
        try {
            locksObtained = lockManager.add(operation.getLocks());
            if (locksObtained) {
                prepared = operationExecutors.get(operation.getName()).prepare(transaction, operation);
            }
            if (prepared) {
                if (operation.getName() == OperationName.Write) {
                    transaction.getOperations().add(((WriteOperation) operation).toBuilder().data(null).md5Hash(null).build());
                } else {
                    transaction.getOperations().add(operation);
                }
            }
        } catch (Exception ex) {
            if (locksObtained) {
                lockManager.remove(operation.getLocks());
                throw ex;
            }
        }
        return prepared;
    }

    private String getSafePath(OperationName operationName, String source) {
        if (source.contains(RELATIVE)) {
            AFSExceptions.throwInstance(AFSExceptions.PathInStoreCantBeRelative, operationName.name(), source);
        }
        if (!source.startsWith(ROOT)) {
            AFSExceptions.throwInstance(AFSExceptions.PathNotStartWithRoot, operationName.name(), source);
        }
        return OperationExecutor.getRealPath(transaction, source);
    }

    private void validateOperationAndPaths(OperationName operationName, String finalSource, String finalTarget) {
        List<String> sourceSubPaths = null;
        if (finalSource != null) {
            sourceSubPaths = PathLockFinder.getParentSubPaths(finalSource);
        }
        List<String> targetSubPaths = null;
        if (finalTarget != null) {
            targetSubPaths = PathLockFinder.getParentSubPaths(finalTarget);
        }
        if (state != State.Begin) {
            AFSExceptions.throwInstance(AFSExceptions.OperationNotAddedDueToState, operationName.name(), state, State.Begin);
        }
        for (String source:safe(sourceSubPaths)) {
            if (deleted.contains(source)) {
                AFSExceptions.throwInstance(AFSExceptions.PathCantBeOperatedAfterDeleted, operationName.name(), source);
            }
        }
        for (String target:safe(targetSubPaths)) {
            if (deleted.contains(target)) {
                AFSExceptions.throwInstance(AFSExceptions.PathCantBeOperatedAfterDeleted, operationName.name(), target);
            }
        }
        for (String source:safe(sourceSubPaths)) {
            if (movedSourceToTarget.containsKey(source)) {
                AFSExceptions.throwInstance(AFSExceptions.PathCantBeOperatedAfterMoved, operationName.name(), source);
            }
        }
        for (String target:safe(targetSubPaths)) {
            if (movedSourceToTarget.containsKey(target)) {
                AFSExceptions.throwInstance(AFSExceptions.PathCantBeOperatedAfterMoved, operationName.name(), target);
            }
        }
        for (String source:safe(sourceSubPaths)) {
            if (movedTargetToSource.containsKey(source)) {
                AFSExceptions.throwInstance(AFSExceptions.PathCantBeOperatedAfterMoved, operationName.name(), source);
            }
        }
        for (String target:safe(targetSubPaths)) {
            if (movedTargetToSource.containsKey(target)) {
                AFSExceptions.throwInstance(AFSExceptions.PathCantBeOperatedAfterMoved, operationName.name(), target);
            }
        }
        for (String source:safe(sourceSubPaths)) {
            if (copiedTargetToSource.containsKey(source)) {
                AFSExceptions.throwInstance(AFSExceptions.PathCantBeOperatedAfterCopied, operationName.name(), source);
            }
        }
    }

    @Override
    public boolean isTwoPhaseCommit() {
        return state == State.Prepare;
    }
}
