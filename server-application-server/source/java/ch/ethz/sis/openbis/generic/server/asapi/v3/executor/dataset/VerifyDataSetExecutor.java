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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.property.IVerifyEntityPropertyExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatch;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;

/**
 * @author pkupczyk
 */
@Component
public class VerifyDataSetExecutor implements IVerifyDataSetExecutor
{

    @Autowired
    private IMapDataSetByIdExecutor mapDataSetByIdExecutor;

    @Autowired
    private IVerifyDataSetSampleAndExperimentExecutor verifyDataSetSampleAndExperimentExecutor;

    @Autowired
    private IVerifyEntityPropertyExecutor verifyEntityPropertyExecutor;

    @Autowired
    private IVerifyDataSetContainersExecutor verifyDataSetContainersExecutor;

    @Autowired
    private IVerifyDataSetParentsExecutor verifyDataSetParentsExecutor;

    @Autowired
    private IVerifyDataSetContentCopyExecutor verifyDataSetContentCopyExecutor;

    @Override
    public void verify(IOperationContext context, CollectionBatch<? extends IDataSetId> dataSetIds)
    {
        if (dataSetIds != null && false == dataSetIds.isEmpty())
        {
            Map<IDataSetId, DataPE> map = mapDataSetByIdExecutor.map(context, dataSetIds.getObjects());

            CollectionBatch<DataPE> dataSets =
                    new CollectionBatch<DataPE>(dataSetIds.getBatchIndex(), dataSetIds.getFromObjectIndex(),
                            dataSetIds.getToObjectIndex(), map.values(), dataSetIds.getTotalObjectCount());

            verifyDataSetSampleAndExperimentExecutor.verify(context, dataSets);
            verifyEntityPropertyExecutor.verify(context, dataSets);
            verifyDataSetContainersExecutor.verify(context, dataSets);
            verifyDataSetParentsExecutor.verify(context, dataSets);
            verifyDataSetContentCopyExecutor.verify(context, dataSets);
        }
    }

}
