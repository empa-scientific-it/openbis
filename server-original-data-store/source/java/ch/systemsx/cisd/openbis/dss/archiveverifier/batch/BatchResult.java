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

package ch.systemsx.cisd.openbis.dss.archiveverifier.batch;

import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Result of a batch verification
 * 
 * @author anttil
 */
public class BatchResult
{
    SortedMap<String, DataSetArchiveVerificationResult> results;

    /**
     * @param dataSets
     */
    public BatchResult(String... dataSets)
    {
        this.results = new TreeMap<String, DataSetArchiveVerificationResult>();

        DataSetArchiveVerificationResult missingResult =
                new DataSetArchiveVerificationResult(VerificationErrorType.ERROR, "Internal error, dataset archive not verified");

        for (String dataSet : dataSets)
        {
            results.put(dataSet, missingResult);
        }
    }

    public int getExitCode()
    {
        ResultType max = ResultType.OK;
        for (DataSetArchiveVerificationResult result : results.values())
        {
            if (max.getExitCode() < result.getType().getExitCode())
            {
                max = result.getType();
            }
        }
        return max.getExitCode();
    }

    public void add(String dataSet, DataSetArchiveVerificationResult result)
    {
        results.put(dataSet, result);
    }

    public void add(String dataSet, Exception e)
    {
        results.put(dataSet, new DataSetArchiveVerificationResult(e));
    }

    public Collection<String> getDataSets()
    {
        return results.keySet();
    }

    public DataSetArchiveVerificationResult getResult(String dataSet)
    {
        return results.get(dataSet);
    }
}
