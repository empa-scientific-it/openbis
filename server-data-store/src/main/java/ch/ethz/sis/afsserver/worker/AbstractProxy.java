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
package ch.ethz.sis.afsserver.worker;

import ch.ethz.sis.afs.api.TransactionalFileSystem;
import ch.ethz.sis.afsapi.dto.File;
import ch.ethz.sis.afsserver.server.Worker;
import ch.ethz.sis.afsserver.server.performance.PerformanceAuditor;
import lombok.NonNull;

import java.util.List;
import java.util.UUID;

public abstract class AbstractProxy implements Worker<TransactionalFileSystem> {

    protected AbstractProxy nextProxy;
    protected WorkerContext workerContext;

    public AbstractProxy(AbstractProxy nextProxy) {
        this.nextProxy = nextProxy;
    }

    @Override
    public void createContext(PerformanceAuditor performanceAuditor) {
        setWorkerContext(new WorkerContext(performanceAuditor, null, null, null, null, false));
    }

    @Override
    public void cleanContext() {
        setWorkerContext(null);
    }

    private void setWorkerContext(WorkerContext workerContext) {
        this.workerContext = workerContext;
        if (nextProxy != null) {
            nextProxy.setWorkerContext(workerContext);
        }
    }

    @Override
    public void setConnection(TransactionalFileSystem connection) {
        workerContext.setConnection(connection);
    }

    @Override
    public TransactionalFileSystem getConnection() {
        if (workerContext != null) {
            return workerContext.getConnection();
        }
        return null;
    }

    @Override
    public void cleanConnection() throws Exception {
        if (workerContext != null &&
                workerContext.getConnection() != null &&
                workerContext.getTransactionId() != null &&
                !workerContext.getConnection().isTwoPhaseCommit()) { // 2PC can only be rolled back manually, maybe they are part of a bigger transaction
            rollback();
        }
    }

    @Override
    public void setSessionToken(String sessionToken) {
        workerContext.setSessionToken(sessionToken);
    }

    @Override
    public String getSessionToken() {
        return workerContext.getSessionToken();
    }

    @Override
    public void setTransactionManagerMode(boolean transactionManagerMode) {
        workerContext.setTransactionManagerMode(transactionManagerMode);
    }

    @Override
    public boolean isTransactionManagerMode() {
        return workerContext.isTransactionManagerMode();
    }

    @Override
    public void begin(UUID transactionId) throws Exception {
        nextProxy.begin(transactionId);
    }

    @Override
    public Boolean prepare() throws Exception {
        return nextProxy.prepare();
    }

    @Override
    public void commit() throws Exception {
        nextProxy.commit();
    }

    @Override
    public void rollback() throws Exception {
        nextProxy.rollback();
    }

    @Override
    public List<UUID> recover() throws Exception {
        return nextProxy.recover();
    }

    @Override
    public String login(@NonNull String userId, @NonNull String password) throws Exception {
        return nextProxy.login(userId, password);
    }

    @Override
    public Boolean isSessionValid() throws Exception {
        return nextProxy.isSessionValid();
    }

    @Override
    public Boolean logout() throws Exception {
        return nextProxy.logout();
    }

    @Override
    public List<File> list(@NonNull String sourceOwner, @NonNull String source, @NonNull Boolean recursively) throws Exception {
        return nextProxy.list(sourceOwner, source, recursively);
    }

    @Override
    public byte[] read(@NonNull String sourceOwner, @NonNull String source, @NonNull Long offset, @NonNull Integer limit) throws Exception {
        return nextProxy.read(sourceOwner, source, offset, limit);
    }

    @Override
    public Boolean write(@NonNull String sourceOwner, @NonNull String source, @NonNull Long offset, @NonNull byte[] data, @NonNull byte[] md5Hash) throws Exception {
        return nextProxy.write(sourceOwner, source, offset, data, md5Hash);
    }

    @Override
    public Boolean delete(@NonNull String sourceOwner, @NonNull String source) throws Exception {
        return nextProxy.delete(sourceOwner, source);
    }

    @Override
    public Boolean copy(@NonNull String sourceOwner, @NonNull String source, @NonNull String targetOwner, @NonNull String target) throws Exception {
        return nextProxy.copy(sourceOwner, source, targetOwner, target);
    }

    @Override
    public Boolean move(@NonNull String sourceOwner, @NonNull String source, @NonNull String targetOwner, @NonNull String target) throws Exception {
        return nextProxy.move(sourceOwner, source, targetOwner, target);
    }

}
