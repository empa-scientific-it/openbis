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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.Complete;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.PhysicalDataCreation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.SetRelationProgress;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.common.types.BooleanOrUnknown;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;

/**
 * @author pkupczyk
 */
@Component
public class SetDataSetPhysicalDataExecutor implements ISetDataSetPhysicalDataExecutor
{

    @Autowired
    private ISetDataSetStorageFormatExecutor setDataSetStorageFormatExecutor;

    @Autowired
    private ISetDataSetFileFormatTypeExecutor setDataSetFileFormatTypeExecutor;

    @Autowired
    private ISetDataSetLocatorTypeExecutor setDataSetLocatorTypeExecutor;

    @Override
    public void set(final IOperationContext context, final MapBatch<DataSetCreation, DataPE> batch)
    {
        new MapBatchProcessor<DataSetCreation, DataPE>(context, batch)
            {
                @Override
                public void process(DataSetCreation creation, DataPE entity)
                {
                    PhysicalDataCreation physicalCreation = creation.getPhysicalData();

                    if (entity instanceof ExternalDataPE)
                    {
                        if (physicalCreation == null)
                        {
                            throw new UserFailureException("Physical data cannot be null for a physical data set.");
                        }
                        set(context, physicalCreation, (ExternalDataPE) entity);
                    } else
                    {
                        if (physicalCreation != null)
                        {
                            throw new UserFailureException("Physical data cannot be set for a non-physical data set.");
                        }
                    }
                }

                @Override
                public IProgress createProgress(DataSetCreation creation, DataPE entity, int objectIndex, int totalObjectCount)
                {
                    return new SetRelationProgress(entity, creation, "dataset-physicaldata", objectIndex, totalObjectCount);
                }
            };

        setDataSetStorageFormatExecutor.set(context, batch);
        setDataSetFileFormatTypeExecutor.set(context, batch);
        setDataSetLocatorTypeExecutor.set(context, batch);
    }

    private void set(IOperationContext context, PhysicalDataCreation physicalCreation, ExternalDataPE dataSet)
    {
        dataSet.setShareId(physicalCreation.getShareId());
        dataSet.setLocation(physicalCreation.getLocation());

        if (physicalCreation.getSize() != null && physicalCreation.getSize() < 0)
        {
            throw new UserFailureException("Physical data set size cannot be < 0.");
        }
        dataSet.setSize(physicalCreation.getSize());

        if (physicalCreation.getSpeedHint() != null)
        {
            dataSet.setSpeedHint(physicalCreation.getSpeedHint());
        }

        BooleanOrUnknown complete = BooleanOrUnknown.U;
        if (Complete.YES.equals(physicalCreation.getComplete()))
        {
            complete = BooleanOrUnknown.T;
        } else if (Complete.NO.equals(physicalCreation.getComplete()))
        {
            complete = BooleanOrUnknown.F;
        }
        dataSet.setComplete(complete);
        dataSet.setH5Folders(physicalCreation.isH5Folders());
        dataSet.setH5arFolders(physicalCreation.isH5arFolders());
        dataSet.setArchivingRequested(physicalCreation.isArchivingRequested());
    }

}
