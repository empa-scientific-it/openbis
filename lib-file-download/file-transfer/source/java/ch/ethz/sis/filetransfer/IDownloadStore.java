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

package ch.ethz.sis.filetransfer;

import java.nio.file.Path;

/**
 * A download store interface. A download store is responsible for storing downloaded chunks and putting them together into files. Depending on a use
 * case an actual implementation may store files on a local file system, network drive or some other location. A store may also offer additional
 * functionality like a high-water-mark checking, automatic space recovery and more.
 * 
 * @author pkupczyk
 */
public interface IDownloadStore
{

    /**
     * Returns a path to a given downloaded item.
     */
    public Path getItemPath(IUserSessionId userSessionId, DownloadSessionId downloadSessionId, IDownloadItemId itemId) throws DownloadException;

    /**
     * Stores a chunk downloaded by a given user within the specified download session.
     * 
     * @throws DownloadException In case of any problems
     */
    public void storeChunk(IUserSessionId userSessionId, DownloadSessionId downloadSessionId, Chunk chunk) throws DownloadException;

}
