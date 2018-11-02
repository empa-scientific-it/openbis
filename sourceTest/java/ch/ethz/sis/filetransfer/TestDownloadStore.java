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
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import ch.ethz.sis.filetransfer.Chunk;
import ch.ethz.sis.filetransfer.DownloadSessionId;
import ch.ethz.sis.filetransfer.IDownloadItemId;
import ch.ethz.sis.filetransfer.IDownloadStore;
import ch.ethz.sis.filetransfer.ILogger;
import ch.ethz.sis.filetransfer.IUserSessionId;
import ch.ethz.sis.filetransfer.LogLevel;

/**
 * @author pkupczyk
 */
public class TestDownloadStore implements IDownloadStore
{

    private ILogger logger;

    private Path storePath;

    public TestDownloadStore(ILogger logger, Path storePath)
    {
        this.logger = logger;
        this.storePath = storePath;
    }

    @Override
    public Path getItemPath(IUserSessionId userSessionId, DownloadSessionId downloadSessionId, IDownloadItemId itemId)
    {
        TestDownloadItemId testItemId = (TestDownloadItemId) itemId;
        return storePath.resolve(Paths.get(testItemId.getFilePath()).getFileName());
    }

    @Override
    public void storeChunk(IUserSessionId userSessionId, DownloadSessionId downloadSessionId, Chunk chunk)
    {
        try
        {
            Path path = getItemPath(userSessionId, downloadSessionId, chunk.getDownloadItemId());

            if (!path.toFile().exists())
            {
                path.toFile().createNewFile();
            }

            FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.WRITE);
            fileChannel.position(chunk.getFileOffset());

            InputStream stream = chunk.getPayload();
            int b;

            while ((b = stream.read()) != -1)
            {
                fileChannel.write((ByteBuffer) ByteBuffer.allocate(1).put((byte) b).rewind());
            }

            logger.log(getClass(), LogLevel.INFO, "Chunk " + chunk.getSequenceNumber() + " successfully stored");

        } catch (Exception e)
        {
            logger.log(getClass(), LogLevel.ERROR, "Chunk " + chunk.getSequenceNumber() + " couldn't be stored");
            throw new RuntimeException(e);
        }
    }

}
