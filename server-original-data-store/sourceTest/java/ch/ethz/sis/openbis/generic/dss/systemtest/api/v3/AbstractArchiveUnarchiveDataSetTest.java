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

package ch.ethz.sis.openbis.generic.dss.systemtest.api.v3;

import java.util.Arrays;
import java.util.Map;

import org.apache.log4j.Logger;
import org.testng.annotations.BeforeClass;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.ArchivingStatus;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;

/**
 * @author pkupczyk
 */
public class AbstractArchiveUnarchiveDataSetTest extends AbstractFileTest
{

    private final Logger log = LogFactory.getLogger(LogCategory.OPERATION, getClass());

    protected IApplicationServerApi v3;

    @Override
    @BeforeClass
    public void beforeClass() throws Exception
    {
        super.beforeClass();
        v3 = ServiceProvider.getV3ApplicationService();
    }

    protected DataSet getDataSet(String sessionToken, DataSetPermId dataSetId)
    {
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withPhysicalData();
        return v3.getDataSets(sessionToken, Arrays.asList(dataSetId), fetchOptions).get(dataSetId);
    }

    protected void waitUntilDataSetStatus(String dataSetCodeToBeFound, ArchivingStatus expectedStatus)
    {
        final long timeoutMillis = 120 * 1000;
        final long finishMillis = System.currentTimeMillis() + timeoutMillis;
        final long intervalMillis = 500;

        String sessionToken = v3.login(TEST_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId(dataSetCodeToBeFound);
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withPhysicalData();

        ArchivingStatus actualStatus = null;

        while (System.currentTimeMillis() < finishMillis)
        {
            Map<IDataSetId, DataSet> dataSets = v3.getDataSets(sessionToken, Arrays.asList(dataSetId), fetchOptions);
            DataSet dataSet = dataSets.get(dataSetId);

            if (dataSet == null)
            {
                fail("Data set '" + dataSetCodeToBeFound + "' was not found.");
            } else
            {
                actualStatus = dataSet.getPhysicalData().getStatus();

                if (expectedStatus.equals(actualStatus))
                {
                    log.info("Stopped waiting. Dataset '" + dataSetCodeToBeFound + "' has reached the expected archiving status " + expectedStatus);
                    return;
                } else
                {
                    log.info("Waiting for data set '" + dataSetCodeToBeFound + "' to have archiving status " + expectedStatus
                            + ". Current archiving status is " + actualStatus);
                    try
                    {
                        Thread.sleep(intervalMillis);
                    } catch (InterruptedException e)
                    {

                    }
                }
            }
        }

        throw new RuntimeException(
                "Waited for data set '" + dataSetCodeToBeFound + "' to have archiving status '" + expectedStatus + "'. Timed out after "
                        + (timeoutMillis / 1000) + " second(s). Last status was " + actualStatus);
    }

    protected void assertDataSetStatus(String dataSetCodeToBeFound, ArchivingStatus expectedStatus)
    {
    }

}
