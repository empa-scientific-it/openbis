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
import java.util.HashSet;
import java.util.UUID;

/**
 * Runs archive verification for a list of datasets.
 * 
 * @author anttil
 */
public class SerialDataSetArchiveVerificationBatch implements IDataSetArchiveVerificationBatch
{

    private final IDataSetArchiveVerifier verifier;

    private final String[] dataSets;

    private BatchResult batchResult;

    public SerialDataSetArchiveVerificationBatch(IDataSetArchiveVerifier verifier, String... dataSets)
    {
        this(verifier, new HashSet<DataSetArchiveVerificationResult>(), dataSets);
    }

    public SerialDataSetArchiveVerificationBatch(IDataSetArchiveVerifier verifier, Collection<DataSetArchiveVerificationResult> initialResults,
            String... dataSets)
    {
        this.verifier = verifier;
        this.dataSets = dataSets;
        batchResult = new BatchResult(dataSets);
        for (DataSetArchiveVerificationResult initialResult : initialResults)
        {
            batchResult.add(UUID.randomUUID().toString(), initialResult);
        }
    }

    @Override
    public BatchResult run()
    {

        for (String dataSet : dataSets)
        {
            DataSetArchiveVerificationResult result = verifier.run(dataSet);

            try
            {
                batchResult.add(dataSet, result);
            } catch (Exception e)
            {
                batchResult.add(dataSet, e);
            }
        }
        return batchResult;
    }
}
