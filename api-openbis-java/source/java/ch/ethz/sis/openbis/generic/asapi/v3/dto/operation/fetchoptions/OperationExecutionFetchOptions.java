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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.fetchoptions;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptionsToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecution;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.fetchoptions.OperationExecutionDetailsFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.fetchoptions.OperationExecutionNotificationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.fetchoptions.OperationExecutionSummaryFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.operation.fetchoptions.OperationExecutionFetchOptions")
public class OperationExecutionFetchOptions extends FetchOptions<OperationExecution> implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private PersonFetchOptions owner;

    @JsonProperty
    private OperationExecutionNotificationFetchOptions notification;

    @JsonProperty
    private OperationExecutionSummaryFetchOptions summary;

    @JsonProperty
    private OperationExecutionDetailsFetchOptions details;

    @JsonProperty
    private OperationExecutionSortOptions sort;

    // Method automatically generated with DtoGenerator
    public PersonFetchOptions withOwner()
    {
        if (owner == null)
        {
            owner = new PersonFetchOptions();
        }
        return owner;
    }

    // Method automatically generated with DtoGenerator
    public PersonFetchOptions withOwnerUsing(PersonFetchOptions fetchOptions)
    {
        return owner = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasOwner()
    {
        return owner != null;
    }

    // Method automatically generated with DtoGenerator
    public OperationExecutionNotificationFetchOptions withNotification()
    {
        if (notification == null)
        {
            notification = new OperationExecutionNotificationFetchOptions();
        }
        return notification;
    }

    // Method automatically generated with DtoGenerator
    public OperationExecutionNotificationFetchOptions withNotificationUsing(OperationExecutionNotificationFetchOptions fetchOptions)
    {
        return notification = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasNotification()
    {
        return notification != null;
    }

    // Method automatically generated with DtoGenerator
    public OperationExecutionSummaryFetchOptions withSummary()
    {
        if (summary == null)
        {
            summary = new OperationExecutionSummaryFetchOptions();
        }
        return summary;
    }

    // Method automatically generated with DtoGenerator
    public OperationExecutionSummaryFetchOptions withSummaryUsing(OperationExecutionSummaryFetchOptions fetchOptions)
    {
        return summary = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasSummary()
    {
        return summary != null;
    }

    // Method automatically generated with DtoGenerator
    public OperationExecutionDetailsFetchOptions withDetails()
    {
        if (details == null)
        {
            details = new OperationExecutionDetailsFetchOptions();
        }
        return details;
    }

    // Method automatically generated with DtoGenerator
    public OperationExecutionDetailsFetchOptions withDetailsUsing(OperationExecutionDetailsFetchOptions fetchOptions)
    {
        return details = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasDetails()
    {
        return details != null;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public OperationExecutionSortOptions sortBy()
    {
        if (sort == null)
        {
            sort = new OperationExecutionSortOptions();
        }
        return sort;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public OperationExecutionSortOptions getSortBy()
    {
        return sort;
    }
    @Override
    protected FetchOptionsToStringBuilder getFetchOptionsStringBuilder()
    {
        FetchOptionsToStringBuilder f = new FetchOptionsToStringBuilder("OperationExecution", this);
        f.addFetchOption("Owner", owner);
        f.addFetchOption("Notification", notification);
        f.addFetchOption("Summary", summary);
        f.addFetchOption("Details", details);
        return f;
    }

}
