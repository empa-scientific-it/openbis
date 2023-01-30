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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

/**
 * A physical data set, i.e. one which stores files
 * 
 * @author Kaloyan Enimanev
 */
public class PhysicalDataSet extends AbstractExternalData implements IDatasetLocation
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private Boolean complete;

    private boolean presentInArchive;

    private String shareId;

    private String location;

    private DataSetArchivingStatus status;

    private int speedHint;

    private LocatorType locatorType;

    private FileFormatType fileFormatType;

    private boolean h5Folders;

    private boolean h5arFolders;

    private boolean archivingRequested;

    public PhysicalDataSet()
    {
        this(false);
    }

    public PhysicalDataSet(boolean isStub)
    {
        super(isStub);
    }

    @Override
    public PhysicalDataSet tryGetAsDataSet()
    {
        return this;
    }

    @Override
    public Long getDataSetSize()
    {
        return getSize();
    }

    public Boolean getComplete()
    {
        return complete;
    }

    public void setComplete(Boolean complete)
    {
        this.complete = complete;
    }

    public void setShareId(String shareId)
    {
        this.shareId = shareId;
    }

    public String getShareId()
    {
        return shareId;
    }

    public String getLocation()
    {
        return location;
    }

    public String getFullLocation()
    {
        if (shareId != null && status.isAvailable())
        {
            return shareId + "/" + location;
        } else
            return "-";
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    public DataSetArchivingStatus getStatus()
    {
        return status;
    }

    public void setStatus(DataSetArchivingStatus status)
    {
        this.status = status;
    }

    public boolean isPresentInArchive()
    {
        return presentInArchive;
    }

    public void setPresentInArchive(boolean presentInArchive)
    {
        this.presentInArchive = presentInArchive;
    }

    public int getSpeedHint()
    {
        return speedHint;
    }

    public void setSpeedHint(int speedHint)
    {
        this.speedHint = speedHint;
    }

    public LocatorType getLocatorType()
    {
        return locatorType;
    }

    public void setLocatorType(LocatorType locatorType)
    {
        this.locatorType = locatorType;
    }

    public FileFormatType getFileFormatType()
    {
        return fileFormatType;
    }

    public void setFileFormatType(FileFormatType fileFormatType)
    {
        this.fileFormatType = fileFormatType;
    }

    @Override
    public boolean isAvailable()
    {
        return getStatus().isAvailable();
    }

    // IDatasetLocation
    @Override
    public String getDataSetLocation()
    {
        return getLocation();
    }

    @Override
    public String getDataSetCode()
    {
        return getCode();
    }

    @Override
    public String getDataStoreUrl()
    {
        return this.getDataStore().getHostUrl();
    }

    @Override
    public String getDataStoreCode()
    {
        return getDataStore().getCode();
    }

    public boolean isH5Folders()
    {
        return h5Folders;
    }

    public void setH5Folders(boolean h5Folders)
    {
        this.h5Folders = h5Folders;
    }

    public boolean isH5arFolders()
    {
        return h5arFolders;
    }

    public void setH5arFolders(boolean h5arFolders)
    {
        this.h5arFolders = h5arFolders;
    }

    public boolean isArchivingRequested()
    {
        return archivingRequested;
    }

    public void setArchivingRequested(boolean archivingRequested)
    {
        this.archivingRequested = archivingRequested;
    }

}
