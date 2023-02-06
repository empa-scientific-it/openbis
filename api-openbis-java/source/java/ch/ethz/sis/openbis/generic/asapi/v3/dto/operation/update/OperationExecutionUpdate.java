/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.update;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IObjectUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.id.IOperationExecutionId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.operation.update.OperationExecutionUpdate")
public class OperationExecutionUpdate implements IUpdate, IObjectUpdate<IOperationExecutionId>
{

    private static final long serialVersionUID = 1L;

    @JsonProperty
    private IOperationExecutionId executionId;

    @JsonProperty
    private FieldUpdateValue<String> description = new FieldUpdateValue<String>();

    @JsonProperty
    private boolean deleteSummaryRequested;

    @JsonProperty
    private boolean deleteDetailsRequested;

    @Override
    @JsonIgnore
    public IOperationExecutionId getObjectId()
    {
        return getExecutionId();
    }

    @JsonIgnore
    public IOperationExecutionId getExecutionId()
    {
        return executionId;
    }

    @JsonIgnore
    public void setExecutionId(IOperationExecutionId executionId)
    {
        this.executionId = executionId;
    }

    @JsonIgnore
    public void setDescription(String description)
    {
        this.description.setValue(description);
    }

    @JsonIgnore
    public FieldUpdateValue<String> getDescription()
    {
        return description;
    }

    @JsonIgnore
    public void deleteSummary()
    {
        this.deleteSummaryRequested = true;
    }

    @JsonIgnore
    public boolean isDeleteSummary()
    {
        return deleteSummaryRequested;
    }

    @JsonIgnore
    public void deleteDetails()
    {
        this.deleteDetailsRequested = true;
    }

    @JsonIgnore
    public boolean isDeleteDetails()
    {
        return deleteDetailsRequested;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("executionId", executionId).toString();
    }

}