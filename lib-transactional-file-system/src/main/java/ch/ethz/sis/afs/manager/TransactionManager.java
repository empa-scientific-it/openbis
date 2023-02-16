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

import ch.ethz.sis.afs.api.dto.File;
import ch.ethz.sis.shared.io.IOUtils;
import ch.ethz.sis.shared.json.JSONObjectMapper;
import ch.ethz.sis.shared.log.LogManager;
import ch.ethz.sis.shared.log.Logger;
import ch.ethz.sis.afs.dto.Transaction;
import ch.ethz.sis.afs.manager.operation.OperationExecutor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static ch.ethz.sis.afs.exception.AFSExceptions.FileSystemNotSupported;
import static ch.ethz.sis.afs.exception.AFSExceptions.PathsOnDifferentVolumes;
import static ch.ethz.sis.afs.exception.AFSExceptions.throwInstance;

public class TransactionManager {

    private static final Logger logger = LogManager.getLogger(TransactionManager.class);

    private LockManager<UUID, String> lockManager;
    private JSONObjectMapper jsonObjectMapper;
    private String writeAheadLogRoot;
    private String storageRoot;
    private RecoveredTransactions recoveredTransactions;

    public TransactionManager(JSONObjectMapper jsonObjectMapper,
                              String writeAheadLogRoot,
                              String storageRoot) throws IOException {
        this.lockManager = new LockManager<>(new PathLockFinder());
        this.jsonObjectMapper = jsonObjectMapper;
        this.writeAheadLogRoot = writeAheadLogRoot;
        this.storageRoot = storageRoot;
        this.recoveredTransactions = new RecoveredTransactions();

        boolean isFileSystemSupported = IOUtils.isFileSystemSupported();
        if (!isFileSystemSupported) {
            throwInstance(FileSystemNotSupported);
        }
        // Create missing directories
        if (writeAheadLogRoot != null && !IOUtils.exists(writeAheadLogRoot)) {
            logger.info(String.format("Commit log root directory missing, it will be created at the configured path: %s", writeAheadLogRoot));
            IOUtils.createDirectories(writeAheadLogRoot);
        }
        if (storageRoot != null && !IOUtils.exists(storageRoot)) {
            logger.info(String.format("Storage root directory missing, it will be created at the configured path: %s", storageRoot));
            IOUtils.createDirectories(storageRoot);
        }
        //
        boolean isSameVolume = IOUtils.isSameVolume(writeAheadLogRoot, storageRoot);
        if (!isSameVolume) {
            throwInstance(PathsOnDifferentVolumes);
        }
    }

    public void reCommitTransactionsAfterCrash() throws Exception {
        logger.info("Transactions recovery on the file system started");
        List<File> transactionCommitLogDirs = IOUtils.list(writeAheadLogRoot, Boolean.FALSE);
        logger.info(String.format("Transactions to recover found %d", transactionCommitLogDirs.size()));
        for (File transactionCommitLogDir : transactionCommitLogDirs) {
            try {
                logger.info(String.format("Transaction checking directory %s", transactionCommitLogDir.getPath()));
                String transactionLogPrepared = OperationExecutor.getTransactionLog(transactionCommitLogDir, false);
                boolean canBeRecovered = IOUtils.exists(transactionLogPrepared);
                String transactionLogCommitted = OperationExecutor.getTransactionLog(transactionCommitLogDir, true);
                boolean canBeCommitted = IOUtils.exists(transactionLogCommitted);

                if (canBeCommitted) {
                    logger.info(String.format("Transaction log to be loaded %s", transactionLogCommitted));
                    byte[] transactionLogBytes = IOUtils.readFully(transactionLogCommitted);
                    Transaction transaction = jsonObjectMapper.readValue(new ByteArrayInputStream(transactionLogBytes), Transaction.class);
                    logger.info(String.format("Transaction loaded %s", transactionLogCommitted));
                    TransactionConnection transactionConnection = new TransactionConnection(lockManager, jsonObjectMapper, transaction);
                    logger.info(String.format("Transaction %s to be committed from recovery", transaction.getUuid().toString()));
                    transactionConnection.commit();
                    logger.info(String.format("Transaction %s committed", transaction.getUuid().toString()));
                } else if (canBeRecovered) {
                    logger.info(String.format("Transaction log to be loaded %s", transactionLogPrepared));
                    byte[] transactionLogBytes = IOUtils.readFully(transactionLogPrepared);
                    Transaction transaction = jsonObjectMapper.readValue(new ByteArrayInputStream(transactionLogBytes), Transaction.class);
                    logger.info(String.format("Transaction loaded %s", transactionLogPrepared));
                    // The connection is created just to hold the locks again, can be discarded afterwards and the transaction will wait to be recovered
                    TransactionConnection transactionConnection = new TransactionConnection(lockManager, jsonObjectMapper, transaction);
                    recoveredTransactions.addRecovered(transaction);
                    logger.info(String.format("Transaction %s waiting to be committed/rollback holding locks", transaction.getUuid().toString()));
                } else {
                    logger.info(String.format("Transaction directory didn't have a commit log %s", transactionCommitLogDir.getPath()));
                    IOUtils.delete(transactionCommitLogDir.getPath());
                    logger.info(String.format("Transaction directory deleted %s", transactionCommitLogDir.getPath()));
                }
            } catch (Exception ex) {
                logger.info(String.format("Transaction from directory %s failed recovery process", transactionCommitLogDir.getPath()));
                logger.catching(ex);
            }
        }
        logger.info(String.format("Transactions to recover finished"));
    }

    public TransactionConnection getTransactionConnection() {
        return new TransactionConnection(lockManager, jsonObjectMapper, writeAheadLogRoot, storageRoot, recoveredTransactions);
    }

}
