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

/**
 * @author pkupczyk
 */
public class OperationExecutionFSFetchOptions
{

    private boolean operations;

    private boolean progress;

    private boolean error;

    private boolean results;

    public void withOperations()
    {
        operations = true;
    }

    public boolean hasOperations()
    {
        return operations;
    }

    public void withProgress()
    {
        progress = true;
    }

    public boolean hasProgress()
    {
        return progress;
    }

    public void withError()
    {
        error = true;
    }

    public boolean hasError()
    {
        return error;
    }

    public void withResults()
    {
        results = true;
    }

    public boolean hasResults()
    {
        return results;
    }
}
