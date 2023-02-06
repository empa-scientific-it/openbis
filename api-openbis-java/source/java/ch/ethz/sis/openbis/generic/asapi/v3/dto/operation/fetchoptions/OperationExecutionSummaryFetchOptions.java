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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.EmptyFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptionsToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionSummary;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.operation.fetchoptions.OperationExecutionSummaryFetchOptions")
public class OperationExecutionSummaryFetchOptions extends FetchOptions<OperationExecutionSummary> implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private EmptyFetchOptions operations;

    @JsonProperty
    private EmptyFetchOptions progress;

    @JsonProperty
    private EmptyFetchOptions error;

    @JsonProperty
    private EmptyFetchOptions results;

    @JsonProperty
    private OperationExecutionSummarySortOptions sort;

    // Method automatically generated with DtoGenerator
    public EmptyFetchOptions withOperations()
    {
        if (operations == null)
        {
            operations = new EmptyFetchOptions();
        }
        return operations;
    }

    // Method automatically generated with DtoGenerator
    public EmptyFetchOptions withOperationsUsing(EmptyFetchOptions fetchOptions)
    {
        return operations = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasOperations()
    {
        return operations != null;
    }

    // Method automatically generated with DtoGenerator
    public EmptyFetchOptions withProgress()
    {
        if (progress == null)
        {
            progress = new EmptyFetchOptions();
        }
        return progress;
    }

    // Method automatically generated with DtoGenerator
    public EmptyFetchOptions withProgressUsing(EmptyFetchOptions fetchOptions)
    {
        return progress = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasProgress()
    {
        return progress != null;
    }

    // Method automatically generated with DtoGenerator
    public EmptyFetchOptions withError()
    {
        if (error == null)
        {
            error = new EmptyFetchOptions();
        }
        return error;
    }

    // Method automatically generated with DtoGenerator
    public EmptyFetchOptions withErrorUsing(EmptyFetchOptions fetchOptions)
    {
        return error = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasError()
    {
        return error != null;
    }

    // Method automatically generated with DtoGenerator
    public EmptyFetchOptions withResults()
    {
        if (results == null)
        {
            results = new EmptyFetchOptions();
        }
        return results;
    }

    // Method automatically generated with DtoGenerator
    public EmptyFetchOptions withResultsUsing(EmptyFetchOptions fetchOptions)
    {
        return results = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasResults()
    {
        return results != null;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public OperationExecutionSummarySortOptions sortBy()
    {
        if (sort == null)
        {
            sort = new OperationExecutionSummarySortOptions();
        }
        return sort;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public OperationExecutionSummarySortOptions getSortBy()
    {
        return sort;
    }
    @Override
    protected FetchOptionsToStringBuilder getFetchOptionsStringBuilder()
    {
        FetchOptionsToStringBuilder f = new FetchOptionsToStringBuilder("OperationExecutionSummary", this);
        f.addFetchOption("Operations", operations);
        f.addFetchOption("Progress", progress);
        f.addFetchOption("Error", error);
        f.addFetchOption("Results", results);
        return f;
    }

}
