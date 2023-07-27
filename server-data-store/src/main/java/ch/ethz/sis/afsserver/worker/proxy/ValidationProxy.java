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

import ch.ethz.sis.afsapi.dto.File;
import ch.ethz.sis.afsserver.exception.FSExceptions;
import ch.ethz.sis.afsserver.worker.AbstractProxy;
import lombok.NonNull;

public class ValidationProxy extends AbstractProxy {

    private int maxReadSizeInBytes;

    public ValidationProxy(AbstractProxy nextProxy, int maxReadSizeInBytes) {
        super(nextProxy);
        this.maxReadSizeInBytes = maxReadSizeInBytes;
    }


    @Override
    public List<File> list(String owner, String source, Boolean recursively) throws Exception {
        return nextProxy.list(owner, source, recursively);
    }

    @Override
    public byte[] read(String owner, String source, Long offset, Integer limit) throws Exception {
        validateReadSize(source, limit);
        return nextProxy.read(owner, source, offset, limit);
    }

    @Override
    public Boolean write(String owner, String source, Long offset, byte[] data, byte[] md5Hash) throws Exception {
        return nextProxy.write(owner, source, offset, data, md5Hash);
    }

    @Override
    public Boolean delete(String owner, String source) throws Exception {
        return nextProxy.delete(owner, source);
    }

    @Override
    public Boolean copy(String sourceOwner, String source, String targetOwner, String target) throws Exception {
        return nextProxy.copy(sourceOwner, source, targetOwner, target);
    }

    @Override
    public Boolean move(String sourceOwner, String source, String targetOwner, String target) throws Exception {
        return nextProxy.move(sourceOwner, source, targetOwner, target);
    }

    @Override
    public @NonNull Boolean create(@NonNull final String owner, @NonNull final String source, @NonNull final Boolean directory) throws Exception
    {
        return nextProxy.create(owner, source, directory);
    }

    private void validateReadSize(String source, Integer limit) {
        if (limit > maxReadSizeInBytes) {
            throw FSExceptions.MAX_READ_SIZE_EXCEEDED.getInstance(workerContext.getSessionToken(), limit, source, maxReadSizeInBytes);
        }
    }
}
