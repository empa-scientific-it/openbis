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
import java.util.stream.Collectors;

import ch.ethz.sis.afsapi.dto.File;
import ch.ethz.sis.afsserver.worker.AbstractProxy;
import ch.ethz.sis.shared.io.IOUtils;
import lombok.NonNull;

public class ExecutorProxy extends AbstractProxy {

    public ExecutorProxy() {
        super(null);
    }

    //
    // Transaction Management
    //

    @Override
    public void begin(UUID transactionId) throws Exception {
        workerContext.setTransactionId(transactionId);
        workerContext.getConnection().begin(transactionId);
    }

    @Override
    public Boolean prepare() throws Exception {
        return workerContext.getConnection().prepare();
    }

    @Override
    public void commit() throws Exception {
        workerContext.getConnection().commit();
    }

    @Override
    public void rollback() throws Exception {
        workerContext.getConnection().rollback();
    }

    @Override
    public List<UUID> recover() throws Exception {
        return workerContext.getConnection().recover();
    }

    //
    // File System Operations
    //

    public String getPath(String owner, String source) {
        return String.join(""+IOUtils.PATH_SEPARATOR, "", owner.toString(), source);
    }

    @Override
    public List<File> list(String owner, String source, Boolean recursively) throws Exception {
        return workerContext.getConnection().list(getPath(owner, source), recursively)
                .stream()
                .map(this::convertToFile)
                .collect(Collectors.toList());
    }

    private File convertToFile(ch.ethz.sis.afs.api.dto.File file) {
        return new File(file.getPath(), file.getName(), file.getDirectory(), file.getSize(),
                file.getLastModifiedTime(), file.getCreationTime(), file.getLastAccessTime());
    }

    @Override
    public byte[] read(String owner, String source, Long offset, Integer limit) throws Exception {
        return workerContext.getConnection().read(getPath(owner, source), offset, limit);
    }

    @Override
    public Boolean write(String owner, String source, Long offset, byte[] data, byte[] md5Hash) throws Exception {
        return workerContext.getConnection().write(getPath(owner, source), offset, data, md5Hash);
    }

    @Override
    public Boolean delete(String owner, String source) throws Exception {
        return workerContext.getConnection().delete(getPath(owner, source));
    }

    @Override
    public Boolean copy(String sourceOwner, String source, String targetOwner, String target) throws Exception {
        return workerContext.getConnection().copy(getPath(sourceOwner, source), getPath(targetOwner, target));
    }

    @Override
    public Boolean move(String sourceOwner, String source, String targetOwner, String target) throws Exception {
        return workerContext.getConnection().move(getPath(sourceOwner, source), getPath(targetOwner, target));
    }

    @Override public @NonNull Boolean create(@NonNull final String owner, @NonNull final String source, @NonNull final Boolean directory)
            throws Exception
    {
        return workerContext.getConnection().create(source, directory);
    }
}
