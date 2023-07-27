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
import ch.ethz.sis.afsserver.worker.AbstractProxy;
import ch.ethz.sis.shared.log.LogManager;
import ch.ethz.sis.shared.log.Logger;
import lombok.NonNull;

public class LogProxy extends AbstractProxy {

    private static final Logger logger = LogManager.getLogger(LogProxy.class);

    public LogProxy(AbstractProxy nextProxy) {
        super(nextProxy);
    }

    @Override
    public void begin(UUID transactionId) throws Exception {
        logger.traceAccess(null);
        nextProxy.begin(transactionId);
        logger.traceExit(null);
    }

    @Override
    public Boolean prepare() throws Exception {
        logger.traceAccess(null);
        return logger.traceExit(nextProxy.prepare());
    }

    @Override
    public void commit() throws Exception {
        logger.traceAccess(null);
        nextProxy.commit();
        logger.traceExit(null);
    }

    @Override
    public void rollback() throws Exception {
        logger.traceAccess(null);
        nextProxy.rollback();
        logger.traceExit(null);
    }

    @Override
    public List<UUID> recover() throws Exception {
        logger.traceAccess(null);
        return logger.traceExit(nextProxy.recover());
    }

    @Override
    public List<File> list(@NonNull String owner, @NonNull String source, @NonNull Boolean recursively) throws Exception {
        logger.traceAccess(null, owner, source, recursively);
        return logger.traceExit(nextProxy.list(owner, source, recursively));
    }

    @Override
    public byte[] read(@NonNull String owner, @NonNull String source, @NonNull Long offset, @NonNull Integer limit) throws Exception {
        logger.traceAccess(null, owner, source, offset, limit);
        return logger.traceExit(nextProxy.read(owner, source, offset, limit));
    }

    @Override
    public Boolean write(@NonNull String owner, @NonNull String source, @NonNull Long offset, @NonNull byte[] data, @NonNull byte[] md5Hash) throws Exception {
        logger.traceAccess(null, owner, source, offset, data.length, md5Hash.length);
        return logger.traceExit(nextProxy.write(owner, source, offset, data, md5Hash));
    }

    @Override
    public Boolean delete(@NonNull String owner, @NonNull String source) throws Exception {
        logger.traceAccess(null, owner, source);
        return logger.traceExit(nextProxy.delete(owner, source));
    }

    @Override
    public Boolean copy(@NonNull String sourceOwner, @NonNull String source, @NonNull String targetOwner, @NonNull String target) throws Exception {
        logger.traceAccess(null, sourceOwner, source, targetOwner, target);
        return logger.traceExit(nextProxy.copy(sourceOwner, source, targetOwner, target));
    }

    @Override
    public Boolean move(@NonNull String sourceOwner, @NonNull String source, @NonNull String targetOwner, @NonNull String target) throws Exception {
        logger.traceAccess(null, sourceOwner, source, targetOwner, target);
        return logger.traceExit(nextProxy.move(sourceOwner, source, targetOwner, target));
    }

    @Override
    public @NonNull Boolean create(@NonNull final String owner, @NonNull final String source, @NonNull final Boolean directory)
            throws Exception
    {
        logger.traceAccess(null, owner, source, directory);
        return logger.traceExit(nextProxy.create(owner, source, directory));
    }

}
