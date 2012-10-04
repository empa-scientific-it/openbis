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
 * A simple (non-container aka non-virtual) data set.
 * 
 * @author Kaloyan Enimanev
 */
public class DataSet extends ExternalData implements IDatasetLocation
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

    public DataSet()
    {
        this(false);
    }

    public DataSet(boolean isStub)
    {
        super(isStub);
    }

    @Override
    public DataSet tryGetAsDataSet()
    {
        return this;
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

}
