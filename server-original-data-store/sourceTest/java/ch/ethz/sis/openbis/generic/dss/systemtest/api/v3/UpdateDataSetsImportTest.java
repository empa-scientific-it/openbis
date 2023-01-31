/*
 * Copyright 2018 ETH Zuerich, CISD
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.PhysicalDataCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DataSetDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.FileFormatTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.ProprietaryStorageFormatPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.RelativeLocationLocatorTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.DataStorePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;

/**
 * @author pkupczyk
 */
public class UpdateDataSetsImportTest extends ObjectsImportTest
{

    @Test(dataProvider = FALSE_TRUE_PROVIDER)
    public void testUpdate(boolean async) throws Exception
    {
        String sessionToken = as.login(TEST_USER, PASSWORD);
        String etlServerSessionToken = as.login(ETL_SERVER_USER, PASSWORD);

        DataSetPermId dataSetPermId = new DataSetPermId("TEST-IMPORT-" + UUID.randomUUID().toString());

        try
        {
            PhysicalDataCreation physicalCreation = new PhysicalDataCreation();
            physicalCreation.setLocation("test/location/" + dataSetPermId.getPermId());
            physicalCreation.setFileFormatTypeId(new FileFormatTypePermId("TIFF"));
            physicalCreation.setLocatorTypeId(new RelativeLocationLocatorTypePermId());
            physicalCreation.setStorageFormatId(new ProprietaryStorageFormatPermId());

            DataSetCreation creation = new DataSetCreation();
            creation.setCode(dataSetPermId.getPermId());
            creation.setDataSetKind(DataSetKind.PHYSICAL);
            creation.setTypeId(new EntityTypePermId("HCS_IMAGE"));
            creation.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));
            creation.setDataStoreId(new DataStorePermId("STANDARD"));
            creation.setPhysicalData(physicalCreation);
            creation.setProperty("COMMENT", "initial comment");

            DataSet dataSet = getObject(sessionToken, dataSetPermId);
            assertNull(dataSet);

            as.createDataSets(etlServerSessionToken, Arrays.asList(creation));

            dataSet = getObject(sessionToken, dataSetPermId);
            assertEquals("initial comment", dataSet.getProperty("COMMENT"));

            ImportFile file = new ImportFile("code", "COMMENT");
            file.addLine(dataSetPermId.getPermId(), "imported comment");
            uploadFiles(sessionToken, TEST_UPLOAD_KEY, file.toString());
            assertUploadedFiles(sessionToken, file.toString());

            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put(PARAM_UPLOAD_KEY, TEST_UPLOAD_KEY);
            parameters.put(PARAM_TYPE_CODE, "HCS_IMAGE");
            parameters.put(PARAM_ASYNC, async);

            if (async)
            {
                parameters.put(PARAM_USER_EMAIL, TEST_EMAIL);
            }

            long timestamp = getTimestampAndWaitASecond();
            String message = executeImport(sessionToken, "updateDataSets", parameters);

            dataSet = getObject(sessionToken, dataSetPermId, timestamp, DEFAULT_TIMEOUT);
            assertEquals("imported comment", dataSet.getProperty("COMMENT"));

            if (async)
            {
                assertEquals("When the import is complete the confirmation or failure report will be sent by email.", message);
                assertEmail(timestamp, TEST_EMAIL, "Data Set Batch Update successfully performed");
            } else
            {
                assertEquals("1 data set(s) found and registered.", message);
                assertNoEmails(timestamp);
            }

            assertUploadedFiles(sessionToken);

        } finally
        {
            DataSetDeletionOptions options = new DataSetDeletionOptions();
            options.setReason("cleanup");
            as.deleteDataSets(sessionToken, Arrays.asList(dataSetPermId), options);
        }
    }

}
