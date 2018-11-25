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

import java.io.InputStream;

/**
 * A chunk serializer interface. A chunk serializer is responsible for converting a chunk (both a chunk metadata and a chunk payload) into byte
 * stream.
 * 
 * @author pkupczyk
 */
public interface IChunkSerializer
{

    public InputStream serialize(Chunk chunk) throws DownloadException;

}
