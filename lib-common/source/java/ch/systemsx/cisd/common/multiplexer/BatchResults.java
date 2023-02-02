/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.multiplexer;

import java.util.List;

/**
 * @author pkupczyk
 */
public class BatchResults<I, R> implements IBatchResults<I, R>
{

    private I batchId;

    private List<R> results;

    public BatchResults(I batchId, List<R> results)
    {
        this.batchId = batchId;
        this.results = results;
    }

    @Override
    public I getBatchId()
    {
        return batchId;
    }

    @Override
    public List<R> getResults()
    {
        return results;
    }

}
