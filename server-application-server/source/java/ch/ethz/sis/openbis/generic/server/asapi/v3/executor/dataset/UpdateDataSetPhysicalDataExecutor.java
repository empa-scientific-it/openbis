/*
 * Copyright 2015 ETH Zuerich, CISD
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

import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.PhysicalDataUpdate;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;

/**
 * @author pkupczyk
 */
@Component
public class UpdateDataSetPhysicalDataExecutor implements IUpdateDataSetPhysicalDataExecutor
{

    @Autowired
    private IUpdateDataSetFileFormatTypeExecutor updateDataSetFileFormatTypeExecutor;

    @Override
    public void update(IOperationContext context, MapBatch<DataSetUpdate, DataPE> batch)
    {
        updateDataSetFileFormatTypeExecutor.update(context, batch);
        for (Entry<DataSetUpdate, DataPE> entry : batch.getObjects().entrySet()) {
            DataSetUpdate dataSetUpdate = entry.getKey();
            DataPE dataPE = entry.getValue();
            if (dataPE instanceof ExternalDataPE) {
                ExternalDataPE externalDataPE = (ExternalDataPE) dataPE;
                FieldUpdateValue<PhysicalDataUpdate> physicalData = dataSetUpdate.getPhysicalData();
                if (physicalData != null && physicalData.getValue() != null) {
                    FieldUpdateValue<Boolean> archivingRequested = physicalData.getValue().isArchivingRequested();
                    if (archivingRequested != null && archivingRequested.getValue() != null) {
                        externalDataPE.setArchivingRequested(archivingRequested.getValue());
                    }
                }
            }
        }
    }

}
