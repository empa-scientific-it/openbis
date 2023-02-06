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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationExecutionError;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationExecutionProgress;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.fetchoptions.OperationExecutionDetailsFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.operation.OperationExecutionDetails")
public class OperationExecutionDetails implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private OperationExecutionDetailsFetchOptions fetchOptions;

    @JsonProperty
    private List<? extends IOperation> operations;

    @JsonProperty
    private IOperationExecutionProgress progress;

    @JsonProperty
    private IOperationExecutionError error;

    @JsonProperty
    private List<? extends IOperationResult> results;

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public OperationExecutionDetailsFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public void setFetchOptions(OperationExecutionDetailsFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public List<? extends IOperation> getOperations()
    {
        if (getFetchOptions() != null && getFetchOptions().hasOperations())
        {
            return operations;
        }
        else
        {
            throw new NotFetchedException("Operations have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setOperations(List<? extends IOperation> operations)
    {
        this.operations = operations;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public IOperationExecutionProgress getProgress()
    {
        if (getFetchOptions() != null && getFetchOptions().hasProgress())
        {
            return progress;
        }
        else
        {
            throw new NotFetchedException("Progress has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setProgress(IOperationExecutionProgress progress)
    {
        this.progress = progress;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public IOperationExecutionError getError()
    {
        if (getFetchOptions() != null && getFetchOptions().hasError())
        {
            return error;
        }
        else
        {
            throw new NotFetchedException("Error has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setError(IOperationExecutionError error)
    {
        this.error = error;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public List<? extends IOperationResult> getResults()
    {
        if (getFetchOptions() != null && getFetchOptions().hasResults())
        {
            return results;
        }
        else
        {
            throw new NotFetchedException("Results have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setResults(List<? extends IOperationResult> results)
    {
        this.results = results;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public String toString()
    {
        return "OperationExecutionDetails";
    }

}
