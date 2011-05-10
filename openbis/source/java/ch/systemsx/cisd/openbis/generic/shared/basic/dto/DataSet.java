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
public class DataSet extends ExternalData
{

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private Boolean complete;

    private String location;

    private DataSetArchivingStatus status;

    private int speedHint;

    private LocatorType locatorType;

    private FileFormatType fileFormatType;

    @Override
    public Boolean getComplete()
    {
        return complete;
    }

    @Override
    public void setComplete(Boolean complete)
    {
        this.complete = complete;
    }

    @Override
    public String getLocation()
    {
        return location;
    }

    @Override
    public void setLocation(String location)
    {
        this.location = location;
    }

    @Override
    public DataSetArchivingStatus getStatus()
    {
        return status;
    }

    @Override
    public void setStatus(DataSetArchivingStatus status)
    {
        this.status = status;
    }

    @Override
    public int getSpeedHint()
    {
        return speedHint;
    }

    @Override
    public void setSpeedHint(int speedHint)
    {
        this.speedHint = speedHint;
    }

    @Override
    public LocatorType getLocatorType()
    {
        return locatorType;
    }

    @Override
    public void setLocatorType(LocatorType locatorType)
    {
        this.locatorType = locatorType;
    }

    @Override
    public FileFormatType getFileFormatType()
    {
        return fileFormatType;
    }

    @Override
    public void setFileFormatType(FileFormatType fileFormatType)
    {
        this.fileFormatType = fileFormatType;
    }

    @Override
    public boolean isAvailable()
    {
        return getStatus().isAvailable();
    }

}
