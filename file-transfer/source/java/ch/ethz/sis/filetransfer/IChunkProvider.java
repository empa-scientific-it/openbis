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

import java.util.List;
import java.util.Map;

/**
 * A chunk provider interface. A chunk provider is responsible for providing chunks of items to be downloaded. Depending on a use case an actual
 * implementation may support different download item ids, use different chunk sizes and retrieve chunks from different locations.
 * 
 * @author pkupczyk
 */
public interface IChunkProvider
{

    /**
     * Creates chunks for the specified download item ids. Chunks that constitute one download item must have consecutive sequence numbers.
     * 
     * @throws DownloadItemNotFoundException In case no item can be found for a given item id
     */
    public Map<IDownloadItemId, List<Chunk>> getChunks(List<IDownloadItemId> itemIds) throws DownloadItemNotFoundException, DownloadException;

}
