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

/**
 * A serializer interface. A serializer is responsible for converting objects to be downloaded from a download stream into byte streams. Depending on
 * a use case actual implementations may decide to serialize downloaded data differently. A default implementation is provided by
 * {@link DefaultSerializerProvider} class.
 * 
 * @author pkupczyk
 */
public interface ISerializerProvider
{

    /**
     * Creates a chunk serializer.
     * 
     * @throws DownloadException In case of any problems
     */
    public IChunkSerializer createChunkSerializer() throws DownloadException;

}
