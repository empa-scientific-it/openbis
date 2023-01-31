/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Franz-Josef Elmer
 */
public class DataSetPathInfo
{
    private long id;

    private String fileName;

    private String relativePath;

    private boolean directory;

    private Date lastModified;

    private long sizeInBytes;

    private Integer checksumCRC32;

    private String checksum;

    private DataSetPathInfo parent;

    private List<DataSetPathInfo> children = new ArrayList<DataSetPathInfo>();

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setRelativePath(String relativePath)
    {
        this.relativePath = relativePath;
    }

    public String getRelativePath()
    {
        return relativePath;
    }

    public long getSizeInBytes()
    {
        return sizeInBytes;
    }

    public void setSizeInBytes(long sizeInBytes)
    {
        this.sizeInBytes = sizeInBytes;
    }

    public Integer getChecksumCRC32()
    {
        return checksumCRC32;
    }

    public void setChecksumCRC32(Integer checksumCRC32)
    {
        this.checksumCRC32 = checksumCRC32;
    }

    public String getChecksum()
    {
        return checksum;
    }

    public void setChecksum(String checksum)
    {
        this.checksum = checksum;
    }

    public Date getLastModified()
    {
        return lastModified;
    }

    public void setLastModified(Date lastModified)
    {
        this.lastModified = lastModified;
    }

    @Deprecated
    public DataSetPathInfo getParent()
    {
        return parent;
    }

    public void setParent(DataSetPathInfo parent)
    {
        this.parent = parent;
    }

    public void setDirectory(boolean directory)
    {
        this.directory = directory;
    }

    public boolean isDirectory()
    {
        return directory;
    }

    @Deprecated
    public List<DataSetPathInfo> getChildren()
    {
        return children;
    }

    @Deprecated
    public void addChild(DataSetPathInfo child)
    {
        children.add(child);
    }
}
