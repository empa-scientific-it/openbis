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
 * @author pkupczyk
 */
public class TestChunkProvider extends FileSystemChunkProvider
{

    public TestChunkProvider(ILogger logger, long chunkSize)
    {
        super(logger, chunkSize);
    }

    @Override
    public Path getFilePath(IDownloadItemId itemId)
    {
        TestDownloadItemId filePathId = (TestDownloadItemId) itemId;
        return filePathId.getFilePath();
    }

}
