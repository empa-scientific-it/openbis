/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.generic.shared.api.v1;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.common.io.IOUtilities;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.util.JsonPropertyUtil;

/**
 * Represents information about a file stored in DSS.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
@SuppressWarnings("unused")
@JsonObject("FileInfoDssDTO")
public class FileInfoDssDTO implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String pathInDataSet;

    private String pathInListing;

    private boolean isDirectory;

    private long fileSize;

    private Integer crc32Checksum;

    public FileInfoDssDTO(String pathInDataSet, String pathInListing, boolean isDirectory,
            long fileSize)
    {
        this(pathInDataSet, pathInListing, isDirectory, fileSize, null);
    }

    public FileInfoDssDTO(String pathInDataSet, String pathInListing, boolean isDirectory,
            long fileSize, Integer crc32Checksum)
    {
        this.pathInDataSet = pathInDataSet;
        this.pathInListing = pathInListing;
        this.isDirectory = isDirectory;
        this.fileSize = fileSize;
        this.crc32Checksum = crc32Checksum;
    }

    /**
     * The path of this file in the data set (i.e., the root of the data set has a path of "").
     */
    public String getPathInDataSet()
    {
        return pathInDataSet;
    }

    /**
     * The path of this file relative to the path of the request that produced this FileInfoDss
     */
    public String getPathInListing()
    {
        return pathInListing;
    }

    /**
     * Return true if this FileInfo represents a folder.
     */
    @JsonProperty("isDirectory")
    public boolean isDirectory()
    {
        return isDirectory;
    }

    /**
     * Return the file size if this FileInfo represents a file. If this FileInfo represents a folder, the return value is negative.
     */
    @JsonIgnore
    public long getFileSize()
    {
        return fileSize;
    }

    /**
     * Return the CRC32 checksum, if it is available and <code>null</code> otherwise.
     * <p>
     * Note that the checksum will only be available when it is precomputed and available from some sort of database, i.e. if it is computationally
     * "cheap" to provide the checksum.
     */
    @JsonProperty("crc32Checksum")
    public Integer tryGetCrc32Checksum()
    {
        return crc32Checksum;
    }

    @Override
    public String toString()
    {
        ToStringBuilder sb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        sb.append(getPathInDataSet());
        sb.append(getPathInListing());
        sb.append(getFileSize());
        if (tryGetCrc32Checksum() != null)
        {
            sb.append(IOUtilities.crc32ToString(tryGetCrc32Checksum()));
        }
        return sb.toString();
    }

    //
    // JSON-RPC
    //
    private FileInfoDssDTO()
    {

    }

    private void setPathInDataSet(String pathInDataSet)
    {
        this.pathInDataSet = pathInDataSet;
    }

    private void setPathInListing(String pathInListing)
    {
        this.pathInListing = pathInListing;
    }

    @JsonProperty("isDirectory")
    private void setIsDirectory(boolean isDirectory)
    {
        this.isDirectory = isDirectory;
    }

    @JsonIgnore
    private void setFileSize(long fileSize)
    {
        this.fileSize = fileSize;
    }

    @JsonProperty("fileSize")
    private String getFileSizeAsString()
    {
        return JsonPropertyUtil.toStringOrNull(fileSize);
    }

    private void setFileSizeAsString(String fileSize)
    {
        this.fileSize = JsonPropertyUtil.toLongOrNull(fileSize);
    }

    private void setCrc32Checksum(int crc32Checksum)
    {
        this.crc32Checksum = crc32Checksum;
    }

}
