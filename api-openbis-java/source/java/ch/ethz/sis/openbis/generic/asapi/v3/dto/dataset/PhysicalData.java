/*
 * Copyright 2014 ETH Zuerich, CISD
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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.ArchivingStatus;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.Complete;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.FileFormatType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.LocatorType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.StorageFormat;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.PhysicalDataFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.dataset.PhysicalData")
public class PhysicalData implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private PhysicalDataFetchOptions fetchOptions;

    @JsonProperty
    private String shareId;

    @JsonProperty
    private String location;

    @JsonProperty
    private Long size;

    @JsonProperty
    private StorageFormat storageFormat;

    @JsonProperty
    @Deprecated
    private FileFormatType fileFormatType;

    @JsonProperty
    private LocatorType locatorType;

    @JsonProperty
    private Complete complete;

    @JsonProperty
    private ArchivingStatus status;

    @JsonProperty
    private Boolean presentInArchive;

    @JsonProperty
    private Boolean storageConfirmation;

    @JsonProperty
    private Integer speedHint;

    @JsonProperty
    private Boolean archivingRequested;

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public PhysicalDataFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public void setFetchOptions(PhysicalDataFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getShareId()
    {
        return shareId;
    }

    // Method automatically generated with DtoGenerator
    public void setShareId(String shareId)
    {
        this.shareId = shareId;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getLocation()
    {
        return location;
    }

    // Method automatically generated with DtoGenerator
    public void setLocation(String location)
    {
        this.location = location;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Long getSize()
    {
        return size;
    }

    // Method automatically generated with DtoGenerator
    public void setSize(Long size)
    {
        this.size = size;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public StorageFormat getStorageFormat()
    {
        if (getFetchOptions() != null && getFetchOptions().hasStorageFormat())
        {
            return storageFormat;
        }
        else
        {
            throw new NotFetchedException("Storage format has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setStorageFormat(StorageFormat storageFormat)
    {
        this.storageFormat = storageFormat;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Deprecated
    public FileFormatType getFileFormatType()
    {
        if (getFetchOptions() != null && getFetchOptions().hasFileFormatType())
        {
            return fileFormatType;
        }
        else
        {
            throw new NotFetchedException("File Format Type has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    @Deprecated
    public void setFileFormatType(FileFormatType fileFormatType)
    {
        this.fileFormatType = fileFormatType;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public LocatorType getLocatorType()
    {
        if (getFetchOptions() != null && getFetchOptions().hasLocatorType())
        {
            return locatorType;
        }
        else
        {
            throw new NotFetchedException("Locator Type has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setLocatorType(LocatorType locatorType)
    {
        this.locatorType = locatorType;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Complete getComplete()
    {
        return complete;
    }

    // Method automatically generated with DtoGenerator
    public void setComplete(Complete complete)
    {
        this.complete = complete;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public ArchivingStatus getStatus()
    {
        return status;
    }

    // Method automatically generated with DtoGenerator
    public void setStatus(ArchivingStatus status)
    {
        this.status = status;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Boolean isPresentInArchive()
    {
        return presentInArchive;
    }

    // Method automatically generated with DtoGenerator
    public void setPresentInArchive(Boolean presentInArchive)
    {
        this.presentInArchive = presentInArchive;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Boolean isStorageConfirmation()
    {
        return storageConfirmation;
    }

    // Method automatically generated with DtoGenerator
    public void setStorageConfirmation(Boolean storageConfirmation)
    {
        this.storageConfirmation = storageConfirmation;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Integer getSpeedHint()
    {
        return speedHint;
    }

    // Method automatically generated with DtoGenerator
    public void setSpeedHint(Integer speedHint)
    {
        this.speedHint = speedHint;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Boolean isArchivingRequested()
    {
        return archivingRequested;
    }

    // Method automatically generated with DtoGenerator
    public void setArchivingRequested(Boolean archivingRequested)
    {
        this.archivingRequested = archivingRequested;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public String toString()
    {
        return "PhysicalData " + location;
    }

}
