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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.operation;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IDescriptionHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPermIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.IOperationExecutionNotification;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionAvailability;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionDetails;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionState;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionSummary;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.fetchoptions.OperationExecutionFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.id.OperationExecutionPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.operation.OperationExecution")
public class OperationExecution implements Serializable, ICodeHolder, IDescriptionHolder, IPermIdHolder
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private OperationExecutionFetchOptions fetchOptions;

    @JsonProperty
    private OperationExecutionPermId permId;

    @JsonProperty
    private String code;

    @JsonProperty
    private OperationExecutionState state;

    @JsonProperty
    private Person owner;

    @JsonProperty
    private String description;

    @JsonProperty
    private IOperationExecutionNotification notification;

    @JsonProperty
    private OperationExecutionAvailability availability;

    @JsonProperty
    private Integer availabilityTime;

    @JsonProperty
    private OperationExecutionSummary summary;

    @JsonProperty
    private OperationExecutionAvailability summaryAvailability;

    @JsonProperty
    private Integer summaryAvailabilityTime;

    @JsonProperty
    private OperationExecutionDetails details;

    @JsonProperty
    private OperationExecutionAvailability detailsAvailability;

    @JsonProperty
    private Integer detailsAvailabilityTime;

    @JsonProperty
    private Date creationDate;

    @JsonProperty
    private Date startDate;

    @JsonProperty
    private Date finishDate;

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public OperationExecutionFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public void setFetchOptions(OperationExecutionFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public OperationExecutionPermId getPermId()
    {
        return permId;
    }

    // Method automatically generated with DtoGenerator
    public void setPermId(OperationExecutionPermId permId)
    {
        this.permId = permId;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public String getCode()
    {
        return code;
    }

    // Method automatically generated with DtoGenerator
    public void setCode(String code)
    {
        this.code = code;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public OperationExecutionState getState()
    {
        return state;
    }

    // Method automatically generated with DtoGenerator
    public void setState(OperationExecutionState state)
    {
        this.state = state;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Person getOwner()
    {
        if (getFetchOptions() != null && getFetchOptions().hasOwner())
        {
            return owner;
        }
        else
        {
            throw new NotFetchedException("Owner has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setOwner(Person owner)
    {
        this.owner = owner;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public String getDescription()
    {
        return description;
    }

    // Method automatically generated with DtoGenerator
    public void setDescription(String description)
    {
        this.description = description;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public IOperationExecutionNotification getNotification()
    {
        if (getFetchOptions() != null && getFetchOptions().hasNotification())
        {
            return notification;
        }
        else
        {
            throw new NotFetchedException("Notification has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setNotification(IOperationExecutionNotification notification)
    {
        this.notification = notification;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public OperationExecutionAvailability getAvailability()
    {
        return availability;
    }

    // Method automatically generated with DtoGenerator
    public void setAvailability(OperationExecutionAvailability availability)
    {
        this.availability = availability;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Integer getAvailabilityTime()
    {
        return availabilityTime;
    }

    // Method automatically generated with DtoGenerator
    public void setAvailabilityTime(Integer availabilityTime)
    {
        this.availabilityTime = availabilityTime;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public OperationExecutionSummary getSummary()
    {
        if (getFetchOptions() != null && getFetchOptions().hasSummary())
        {
            return summary;
        }
        else
        {
            throw new NotFetchedException("Summary has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setSummary(OperationExecutionSummary summary)
    {
        this.summary = summary;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public OperationExecutionAvailability getSummaryAvailability()
    {
        return summaryAvailability;
    }

    // Method automatically generated with DtoGenerator
    public void setSummaryAvailability(OperationExecutionAvailability summaryAvailability)
    {
        this.summaryAvailability = summaryAvailability;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Integer getSummaryAvailabilityTime()
    {
        return summaryAvailabilityTime;
    }

    // Method automatically generated with DtoGenerator
    public void setSummaryAvailabilityTime(Integer summaryAvailabilityTime)
    {
        this.summaryAvailabilityTime = summaryAvailabilityTime;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public OperationExecutionDetails getDetails()
    {
        if (getFetchOptions() != null && getFetchOptions().hasDetails())
        {
            return details;
        }
        else
        {
            throw new NotFetchedException("Details has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setDetails(OperationExecutionDetails details)
    {
        this.details = details;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public OperationExecutionAvailability getDetailsAvailability()
    {
        return detailsAvailability;
    }

    // Method automatically generated with DtoGenerator
    public void setDetailsAvailability(OperationExecutionAvailability detailsAvailability)
    {
        this.detailsAvailability = detailsAvailability;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Integer getDetailsAvailabilityTime()
    {
        return detailsAvailabilityTime;
    }

    // Method automatically generated with DtoGenerator
    public void setDetailsAvailabilityTime(Integer detailsAvailabilityTime)
    {
        this.detailsAvailabilityTime = detailsAvailabilityTime;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Date getCreationDate()
    {
        return creationDate;
    }

    // Method automatically generated with DtoGenerator
    public void setCreationDate(Date creationDate)
    {
        this.creationDate = creationDate;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Date getStartDate()
    {
        return startDate;
    }

    // Method automatically generated with DtoGenerator
    public void setStartDate(Date startDate)
    {
        this.startDate = startDate;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Date getFinishDate()
    {
        return finishDate;
    }

    // Method automatically generated with DtoGenerator
    public void setFinishDate(Date finishDate)
    {
        this.finishDate = finishDate;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public String toString()
    {
        return "OperationExecution code: " + code;
    }

}
