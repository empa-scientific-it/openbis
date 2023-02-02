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

package ch.systemsx.cisd.openbis.dss.screening.shared.api.internal;

import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.common.multiplexer.BatchHandlerAbstract;
import ch.systemsx.cisd.common.multiplexer.BatchesResults;
import ch.systemsx.cisd.common.multiplexer.IBatch;
import ch.systemsx.cisd.common.multiplexer.IBatchHandler;
import ch.systemsx.cisd.common.multiplexer.IBatchIdProvider;
import ch.systemsx.cisd.common.multiplexer.IMultiplexer;
import ch.systemsx.cisd.common.multiplexer.ThreadPoolMultiplexer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IDatasetIdentifier;

/**
 * @author pkupczyk
 */
public class DssServiceRpcScreeningMultiplexer implements IDssServiceRpcScreeningMultiplexer
{
    private final IMultiplexer multiplexer;

    private final IDssServiceRpcScreeningFactory dssServiceFactory;

    public DssServiceRpcScreeningMultiplexer(IMultiplexer multiplexer,
            IDssServiceRpcScreeningFactory dssServiceFactory)
    {
        if (multiplexer == null)
        {
            throw new IllegalArgumentException("Multiplexer cannot be null");
        }
        if (dssServiceFactory == null)
        {
            throw new IllegalArgumentException("Dss service factory cannot be null");
        }
        this.multiplexer = multiplexer;
        this.dssServiceFactory = dssServiceFactory;
    }

    @Override
    public <O extends IDatasetIdentifier, R> BatchesResults<String, R> process(
            final List<? extends O> objects,
            final IDssServiceRpcScreeningBatchHandler<O, R> screeningBatchHandler)
    {
        IBatchIdProvider<O, String> batchIdProvider = new IBatchIdProvider<O, String>()
            {
                @Override
                public String getBatchId(O object)
                {
                    return object.getDatastoreServerUrl();
                }
            };

        IBatchHandler<O, String, R> batchHandler = new BatchHandlerAbstract<O, String, R>()
            {
                @Override
                public List<R> processBatch(IBatch<O, String> batch)
                {
                    DssServiceRpcScreeningHolder dssService =
                            dssServiceFactory.createDssService(batch.getId());
                    return screeningBatchHandler.handle(dssService, batch.getObjects());
                }
            };

        return multiplexer.process(objects, batchIdProvider, batchHandler);
    }

    public static Map<String, List<IDatasetIdentifier>> getReferencesPerDataStore(
            List<IDatasetIdentifier> dataSetIdentifiers)
    {
        IBatchIdProvider<IDatasetIdentifier, String> batchIdProvider =
                new IBatchIdProvider<IDatasetIdentifier, String>()
                    {
                        @Override
                        public String getBatchId(IDatasetIdentifier object)
                        {
                            return object.getDatastoreServerUrl();
                        }
                    };

        return ThreadPoolMultiplexer.createBatchIdToObjectsMap(dataSetIdentifiers, batchIdProvider);
    }

}
