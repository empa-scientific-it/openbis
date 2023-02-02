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

import java.util.ArrayList;
import java.util.List;

/**
 * @author pkupczyk
 */
public class BatchesResults<I, R> implements IBatchesResults<I, R>
{

    private List<IBatchResults<I, R>> batchesResults = new ArrayList<IBatchResults<I, R>>();

    public void addBatchResults(IBatchResults<I, R> batchResults)
    {
        if (batchResults != null)
        {
            batchesResults.add(batchResults);
        }
    }

    @Override
    public List<IBatchResults<I, R>> getBatchResults()
    {
        return batchesResults;
    }

    @Override
    public List<R> getMergedBatchResultsWithDuplicates()
    {
        List<R> results = new ArrayList<R>();

        for (IBatchResults<I, R> batchResults : batchesResults)
        {
            if (batchResults.getResults() != null)
            {
                results.addAll(batchResults.getResults());
            }
        }

        return results;
    }

    @Override
    public List<R> getMergedBatchResultsWithoutDuplicates()
    {
        List<R> results = new ArrayList<R>();

        for (IBatchResults<I, R> batchResults : batchesResults)
        {
            if (batchResults.getResults() != null)
            {
                for (R batchResult : batchResults.getResults())
                {
                    if (results.contains(batchResult) == false)
                    {
                        results.add(batchResult);
                    }
                }
            }
        }
        return results;
    }

}
