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

import java.io.InputStream;

/**
 * @author pkupczyk
 */
public abstract class Chunk
{

    private int sequenceNumber;

    private IDownloadItemId downloadItemId;

    private boolean isDirectory;

    private String filePath;

    private long fileOffset;

    private int payloadLength;

    public Chunk(int sequenceNumber, IDownloadItemId downloadItemId, boolean isDirectory, String filePath, long fileOffset, int payloadLength)
    {
        this.sequenceNumber = sequenceNumber;
        this.downloadItemId = downloadItemId;
        this.isDirectory = isDirectory;
        this.filePath = filePath;
        this.fileOffset = fileOffset;
        this.payloadLength = payloadLength;
    }

    public int getSequenceNumber()
    {
        return sequenceNumber;
    }

    public IDownloadItemId getDownloadItemId()
    {
        return downloadItemId;
    }

    public boolean isDirectory()
    {
        return isDirectory;
    }

    public String getFilePath()
    {
        return filePath;
    }

    public long getFileOffset()
    {
        return fileOffset;
    }

    public abstract InputStream getPayload() throws DownloadException;

    public int getPayloadLength()
    {
        return payloadLength;
    }

}
