/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.fetchoptions.DeletionFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.deletion.Deletion")
public class Deletion implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private DeletionFetchOptions fetchOptions;

    @JsonProperty
    private IDeletionId id;

    @JsonProperty
    private String reason;

    @JsonProperty
    private List<DeletedObject> deletedObjects;

    @JsonProperty
    private Date deletionDate;

    @JsonProperty
    private int totalSamplesCount;

    @JsonProperty
    private int totalDataSetsCount;

    @JsonProperty
    private int totalExperimentsCount;

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public DeletionFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public void setFetchOptions(DeletionFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public IDeletionId getId()
    {
        return id;
    }

    // Method automatically generated with DtoGenerator
    public void setId(IDeletionId id)
    {
        this.id = id;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getReason()
    {
        return reason;
    }

    // Method automatically generated with DtoGenerator
    public void setReason(String reason)
    {
        this.reason = reason;
    }

    @JsonIgnore
    public int getTotalSamplesCount()
    {
        return getIfItHasDeletedObjects(totalSamplesCount);
    }

    public void setTotalSamplesCount(int totalSamplesCount)
    {
        this.totalSamplesCount = totalSamplesCount;
    }

    @JsonIgnore
    public int getTotalDataSetsCount()
    {
        return getIfItHasDeletedObjects(totalDataSetsCount);
    }

    public void setTotalDataSetsCount(int totalDataSetsCount)
    {
        this.totalDataSetsCount = totalDataSetsCount;
    }

    @JsonIgnore
    public int getTotalExperimentsCount()
    {
        return getIfItHasDeletedObjects(totalExperimentsCount);
    }

    public void setTotalExperimentsCount(int totalExperimentsCount)
    {
        this.totalExperimentsCount = totalExperimentsCount;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public List<DeletedObject> getDeletedObjects()
    {
        return getIfItHasDeletedObjects(deletedObjects);
    }

    private <T> T getIfItHasDeletedObjects(T value)
    {
        if (getFetchOptions() != null && getFetchOptions().hasDeletedObjects())
        {
            return value;
        }
        else
        {
            throw new NotFetchedException("Deleted objects have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setDeletedObjects(List<DeletedObject> deletedObjects)
    {
        this.deletedObjects = deletedObjects;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Date getDeletionDate()
    {
        return deletionDate;
    }

    // Method automatically generated with DtoGenerator
    public void setDeletionDate(Date deletionDate)
    {
        this.deletionDate = deletionDate;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public String toString()
    {
        return "Deletion " + id;
    }

}
