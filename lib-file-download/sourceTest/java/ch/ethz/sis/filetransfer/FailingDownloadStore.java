/*
 * Copyright ETH 2018 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.filetransfer;

import java.nio.file.Path;

import ch.ethz.sis.filetransfer.Chunk;
import ch.ethz.sis.filetransfer.DownloadException;
import ch.ethz.sis.filetransfer.DownloadSessionId;
import ch.ethz.sis.filetransfer.IDownloadItemId;
import ch.ethz.sis.filetransfer.IDownloadStore;
import ch.ethz.sis.filetransfer.IUserSessionId;

/**
 * @author pkupczyk
 */
public class FailingDownloadStore implements IDownloadStore
{

    public static final String OPERATION_GET_ITEM_PATH = "IDownloadStore.getItemPath";

    public static final String OPERATION_STORE_CHUNK = "IDownloadStore.storeChunk";

    private IDownloadStore store;

    private FailureGenerator generator;

    public FailingDownloadStore(IDownloadStore store, FailureGenerator generator)
    {
        this.store = store;
        this.generator = generator;
    }

    @Override
    public Path getItemPath(IUserSessionId userSessionId, DownloadSessionId downloadSessionId, IDownloadItemId itemId) throws DownloadException
    {
        generator.maybeFail(OPERATION_GET_ITEM_PATH);
        return store.getItemPath(userSessionId, downloadSessionId, itemId);
    }

    @Override
    public void storeChunk(IUserSessionId userSessionId, DownloadSessionId downloadSessionId, Chunk chunk) throws DownloadException
    {
        generator.maybeFail(OPERATION_STORE_CHUNK);
        store.storeChunk(userSessionId, downloadSessionId, chunk);
    }

}
