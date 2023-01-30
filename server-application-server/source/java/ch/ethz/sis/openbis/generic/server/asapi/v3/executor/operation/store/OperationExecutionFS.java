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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.store;

import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationExecutionError;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationExecutionProgress;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationResult;

/**
 * @author pkupczyk
 */
public class OperationExecutionFS
{

    private String relativePath;

    private List<? extends IOperation> operations;

    private IOperationExecutionProgress progress;

    private IOperationExecutionError error;

    private List<? extends IOperationResult> results;

    public void setRelativePath(String relativePath)
    {
        this.relativePath = relativePath;
    }

    public String getRelativePath()
    {
        return relativePath;
    }

    public void setOperations(List<? extends IOperation> operations)
    {
        this.operations = operations;
    }

    public List<? extends IOperation> getOperations()
    {
        return operations;
    }

    public void setProgress(IOperationExecutionProgress progress)
    {
        this.progress = progress;
    }

    public IOperationExecutionProgress getProgress()
    {
        return progress;
    }

    public void setError(IOperationExecutionError error)
    {
        this.error = error;
    }

    public IOperationExecutionError getError()
    {
        return error;
    }

    public void setResults(List<? extends IOperationResult> results)
    {
        this.results = results;
    }

    public List<? extends IOperationResult> getResults()
    {
        return results;
    }

}
