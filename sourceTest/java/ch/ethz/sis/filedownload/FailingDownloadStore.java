/*
 * Copyright 2018 ETH Zuerich, CISD
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

package ch.ethz.sis.filedownload;

import java.nio.file.Path;

import ch.ethz.sis.filedownload.Chunk;
import ch.ethz.sis.filedownload.DownloadException;
import ch.ethz.sis.filedownload.DownloadSessionId;
import ch.ethz.sis.filedownload.IDownloadItemId;
import ch.ethz.sis.filedownload.IDownloadStore;
import ch.ethz.sis.filedownload.IUserSessionId;

/**
 * @author pkupczyk
 */
public class FailingDownloadStore implements IDownloadStore
{

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
        generator.maybeFail("IDownloadStore.getItemPath");
        return store.getItemPath(userSessionId, downloadSessionId, itemId);
    }

    @Override
    public void storeChunk(IUserSessionId userSessionId, DownloadSessionId downloadSessionId, Chunk chunk) throws DownloadException
    {
        generator.maybeFail("IDownloadStore.storeChunk");
        store.storeChunk(userSessionId, downloadSessionId, chunk);
    }

}
