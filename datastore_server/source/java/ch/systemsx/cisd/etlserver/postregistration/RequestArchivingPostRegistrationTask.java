/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.systemsx.cisd.etlserver.postregistration;

import java.util.*;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import org.apache.log4j.Logger;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.PhysicalDataUpdate;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;

/**
 * @author Franz-Josef Elmer
 */
public class RequestArchivingPostRegistrationTask extends AbstractPostRegistrationTaskForPhysicalDataSets
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            RequestArchivingPostRegistrationTask.class);

    public RequestArchivingPostRegistrationTask(Properties properties, IEncapsulatedOpenBISService service)
    {
        super(properties, service);
    }

    @Override
    protected IPostRegistrationTaskExecutor createExecutor(String dataSetCode)
    {
        return new IPostRegistrationTaskExecutor()
            {
                private boolean isAlreadyArchived() {
                    List<String> codeAsList = Collections.singletonList(dataSetCode);
                    List<AbstractExternalData> dataList = service.listDataSetsByCode(codeAsList);

                    if (dataList == null || dataList.isEmpty()) {
                        return false;
                    }

                    AbstractExternalData dataSet = dataList.get(0);
                    return dataSet instanceof PhysicalDataSet && ((PhysicalDataSet) dataSet).isPresentInArchive();
                }

                @Override
                public void execute()
                {
                    if (isAlreadyArchived()) {
                        operationLog.info("DataSet " + dataSetCode + " is already in archive.");
                        return;
                    }

                    DataSetUpdate dataSetUpdate = new DataSetUpdate();
                    dataSetUpdate.setDataSetId(new DataSetPermId(dataSetCode));
                    PhysicalDataUpdate physicalDataUpdate = new PhysicalDataUpdate();
                    physicalDataUpdate.setArchivingRequested(true);
                    dataSetUpdate.setPhysicalData(physicalDataUpdate);
                    getV3api().updateDataSets(service.getSessionToken(), Arrays.asList(dataSetUpdate));
                    operationLog.info("Archiving requested for data set " + dataSetCode);
                }

                @Override
                public ICleanupTask createCleanupTask()
                {
                    return new NoCleanupTask();
                }
            };
    }

}
