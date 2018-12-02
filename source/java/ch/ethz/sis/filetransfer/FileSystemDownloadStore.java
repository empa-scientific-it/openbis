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

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

import org.apache.commons.io.FileUtils;

/**
 * An abstract download store that stores chunks in a file system in a given directory. The store can handle chunks that represent files as well as
 * directories. The store has the following directory structure:
 * 
 * <pre>
 * [user-session-1-uuid]
 *      [download-session-11-uuid]
 *          [item-111-uuid]
 *              file
 *          [item-112-uuid]
 *              folder
 *                  folder
 *                      file
 *                  file
 *      [download-session-12-uuid]
 *          [item-121-uuid]
 *              folder
 *                  file
 * [user-session-2-uuid]
 *      [download-session-21-uuid]
 *          [item-211-uuid]
 *              file
 * </pre>
 * 
 * @author pkupczyk
 */
public class FileSystemDownloadStore implements IDownloadStore
{

    private ILogger logger;

    private Path storePath;

    public FileSystemDownloadStore(ILogger logger, Path storePath)
    {
        this.logger = logger;
        this.storePath = storePath;
    }

    private Path getItemDirectory(IUserSessionId userSessionId, DownloadSessionId downloadSessionId, IDownloadItemId itemId) throws DownloadException
    {
        String userDir = UUID.nameUUIDFromBytes(userSessionId.getId().getBytes()).toString();
        String downloadDir = UUID.nameUUIDFromBytes(downloadSessionId.getId().getBytes()).toString();
        String itemIdDir = UUID.nameUUIDFromBytes(itemId.getId().getBytes()).toString();

        return storePath.resolve(userDir).resolve(downloadDir).resolve(itemIdDir);
    }

    @Override
    public Path getItemPath(IUserSessionId userSessionId, DownloadSessionId downloadSessionId, IDownloadItemId itemId) throws DownloadException
    {
        Path itemDirectory = getItemDirectory(userSessionId, downloadSessionId, itemId);

        if (itemDirectory.toFile().exists())
        {
            File[] itemFiles = itemDirectory.toFile().listFiles();

            if (itemFiles.length > 0)
            {
                return itemFiles[0].toPath();
            }
        }

        throw new DownloadItemNotFoundException("Store does not contain any files for download item id: " + itemId);
    }

    private Path getChunkPath(IUserSessionId userSessionId, DownloadSessionId downloadSessionId, Chunk chunk) throws DownloadException
    {
        Path chunkPath = getItemDirectory(userSessionId, downloadSessionId, chunk.getDownloadItemId()).resolve(chunk.getFilePath());

        if (false == belongsToStore(chunkPath))
        {
            throw new DownloadException("Chunk path does not belong to the store. Chunk path: " + chunkPath + ", store path: " + storePath, false);
        }

        return chunkPath;
    }

    @Override
    public void storeChunk(IUserSessionId userSessionId, DownloadSessionId downloadSessionId, Chunk chunk) throws DownloadException
    {
        Path chunkPath = getChunkPath(userSessionId, downloadSessionId, chunk);

        if (chunk.isDirectory())
        {
            chunkPath.toFile().mkdirs();
        } else
        {
            chunkPath.toFile().getParentFile().mkdirs();

            ByteBuffer buffer = ByteBuffer.allocate((int) FileUtils.ONE_MB);
            int size = 0;

            try (
                    ReadableByteChannel chunkChannel = Channels.newChannel(chunk.getPayload());
                    FileChannel fileChannel = FileChannel.open(chunkPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE))
            {
                fileChannel.position(chunk.getFileOffset());

                while (chunkChannel.read(buffer) != -1)
                {
                    buffer.flip();
                    size += fileChannel.write(buffer);
                    buffer.clear();
                }

                logger.log(getClass(), LogLevel.INFO, "Chunk " + chunk.getSequenceNumber() + " successfully stored (size: " + size + ")");

            } catch (IOException e)
            {
                throw new DownloadException("Chunk " + chunk.getSequenceNumber() + " couldn't be stored", e, true);
            }
        }
    }

    private boolean belongsToStore(Path path)
    {
        while (path != null)
        {
            if (path.equals(storePath))
            {
                return true;
            } else
            {
                path = path.getParent();
            }
        }

        return false;
    }

}
