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
 * A deserializer interface. A deserializer is responsible for converting byte streams returned by a download server back into objects on the client
 * side. A deserializer is closely coupled with a corresponding serializer that created the byte streams (both have to use the same format of data). A
 * default implementation is provided by {@link DefaultDeserializerProvider} class.
 * 
 * @author pkupczyk
 */
public interface IDeserializerProvider
{

    /**
     * Creates a chunk deserializer.
     * 
     * @throws DownloadException In case of any problems
     */
    public IChunkDeserializer createChunkDeserializer() throws DownloadException;

}
