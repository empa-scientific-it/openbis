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
package ch.ethz.sis.afsserver.worker.proxy;

import java.util.List;
import java.util.UUID;

import ch.ethz.sis.afsapi.dto.File;
import ch.ethz.sis.afsserver.exception.FSExceptions;
import ch.ethz.sis.afsserver.worker.AbstractProxy;
import ch.ethz.sis.afsserver.worker.providers.AuthenticationInfoProvider;
import lombok.NonNull;

public class AuthenticationProxy extends AbstractProxy {

    private AuthenticationInfoProvider authenticationInfoProvider;

    public AuthenticationProxy(AbstractProxy nextProxy, AuthenticationInfoProvider authenticationInfoProvider) {
        super(nextProxy);
        this.authenticationInfoProvider = authenticationInfoProvider;
    }

    //
    //
    //

    @Override
    public void begin(UUID transactionId) throws Exception {
        if (!workerContext.isTransactionManagerMode()) {
            validateSessionAvailable();
        }
        nextProxy.begin(transactionId);
    }

    @Override
    public Boolean prepare() throws Exception {
        if (workerContext.isTransactionManagerMode()) {
            validateSessionAvailable();
        } else {
            throw FSExceptions.PREPARE_REQUIRES_TM.getInstance();
        }
        return nextProxy.prepare();
    }

    @Override
    public void commit() throws Exception {
        if (!workerContext.isTransactionManagerMode()) {
            validateSessionAvailable();
        }
        nextProxy.commit();
    }

    @Override
    public void rollback() throws Exception {
        if (!workerContext.isTransactionManagerMode()) {
            validateSessionAvailable();
        }
        nextProxy.rollback();
    }

    @Override
    public List<UUID> recover() throws Exception {
        if (!workerContext.isTransactionManagerMode()) {
            throw FSExceptions.RECOVER_REQUIRES_TM.getInstance();
        }
        return nextProxy.recover();
    }

    //
    //
    //

    @Override
    public String login(@NonNull String userId, @NonNull String password) throws Exception {
        return authenticationInfoProvider.login(userId, password);
    }

    @Override
    public Boolean isSessionValid() throws Exception {
        return authenticationInfoProvider.isSessionValid(getSessionToken());
    }

    @Override
    public Boolean logout() throws Exception {
        return authenticationInfoProvider.logout(getSessionToken());
    }

    //
    //
    //

    @Override
    public List<File> list(@NonNull String owner, @NonNull String source, @NonNull Boolean recursively) throws Exception {
        validateSessionAvailable();
        return nextProxy.list(owner, source, recursively);
    }

    @Override
    public byte[] read(@NonNull String owner, @NonNull String source, @NonNull Long offset, @NonNull Integer limit) throws Exception {
        validateSessionAvailable();
        return nextProxy.read(owner, source, offset, limit);
    }

    @Override
    public Boolean write(@NonNull String owner, @NonNull String source, @NonNull Long offset, @NonNull byte[] data, @NonNull byte[] md5Hash) throws Exception {
        validateSessionAvailable();
        return nextProxy.write(owner, source, offset, data, md5Hash);
    }

    @Override
    public Boolean delete(@NonNull String owner, @NonNull String source) throws Exception {
        validateSessionAvailable();
        return nextProxy.delete(owner, source);
    }

    @Override
    public Boolean copy(@NonNull String sourceOwner, @NonNull String source, @NonNull String targetOwner, @NonNull String target) throws Exception {
        validateSessionAvailable();
        return nextProxy.copy(sourceOwner, source, targetOwner, target);
    }

    @Override
    public Boolean move(@NonNull String sourceOwner, @NonNull String source, @NonNull String targetOwner, @NonNull String target) throws Exception {
        validateSessionAvailable();
        return nextProxy.move(sourceOwner, source, targetOwner, target);
    }

    @Override
    public @NonNull Boolean create(@NonNull final String owner, @NonNull final String source, @NonNull final Boolean directory) throws Exception
    {
        validateSessionAvailable();
        return nextProxy.create(owner, source, directory);
    }

    //
    //
    //

    private void validateSessionAvailable() throws Exception {
        if (workerContext.getSessionExists() == null) {
            String sessionToken = workerContext.getSessionToken();
            boolean doSessionExists = authenticationInfoProvider.isSessionValid(sessionToken);
            workerContext.setSessionExists(doSessionExists);
        }
        if (!workerContext.getSessionExists()) {
            throw FSExceptions.SESSION_NOT_FOUND.getInstance(workerContext.getSessionToken());
        }
    }

}
