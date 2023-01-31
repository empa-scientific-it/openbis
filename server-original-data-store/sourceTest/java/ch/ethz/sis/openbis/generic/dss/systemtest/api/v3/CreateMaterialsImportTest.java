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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.delete.MaterialDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;

/**
 * @author pkupczyk
 */
public class CreateMaterialsImportTest extends ObjectsImportTest
{

    @Test(dataProvider = FALSE_TRUE_PROVIDER)
    public void testCreate(boolean async) throws Exception
    {
        String sessionToken = as.login(TEST_USER, PASSWORD);

        MaterialPermId materialPermId = new MaterialPermId("TEST-IMPORT-" + UUID.randomUUID().toString(), "VIRUS");

        try
        {
            ImportFile file = new ImportFile("code", "DESCRIPTION");
            file.addLine(materialPermId.getCode(), "imported description");
            uploadFiles(sessionToken, TEST_UPLOAD_KEY, file.toString());
            assertUploadedFiles(sessionToken, file.toString());

            Material material = getObject(sessionToken, materialPermId);
            assertNull(material);

            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put(PARAM_UPLOAD_KEY, TEST_UPLOAD_KEY);
            parameters.put(PARAM_TYPE_CODE, materialPermId.getTypeCode());
            parameters.put(PARAM_UPDATE_EXISTING, false);
            parameters.put(PARAM_ASYNC, async);

            if (async)
            {
                parameters.put(PARAM_USER_EMAIL, TEST_EMAIL);
            }

            long timestamp = getTimestampAndWaitASecond();
            String message = executeImport(sessionToken, "createMaterials", parameters);

            material = getObject(sessionToken, materialPermId, timestamp, DEFAULT_TIMEOUT);
            assertEquals("imported description", material.getProperty("DESCRIPTION"));

            if (async)
            {
                assertEquals("When the import is complete the confirmation or failure report will be sent by email.", message);
                assertEmail(timestamp, TEST_EMAIL, "Material Batch Registration successfully performed");
            } else
            {
                assertEquals("Registration/update of 1 material(s) is complete.", message);
                assertNoEmails(timestamp);
            }

            assertUploadedFiles(sessionToken);

        } finally
        {
            MaterialDeletionOptions options = new MaterialDeletionOptions();
            options.setReason("cleanup");
            as.deleteMaterials(sessionToken, Arrays.asList(materialPermId), options);
        }
    }

}
