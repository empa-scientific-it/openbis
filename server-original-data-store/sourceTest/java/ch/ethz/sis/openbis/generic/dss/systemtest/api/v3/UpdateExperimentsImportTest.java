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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;

/**
 * @author pkupczyk
 */
public class UpdateExperimentsImportTest extends ObjectsImportTest
{

    @Test(dataProvider = FALSE_TRUE_PROVIDER)
    public void testUpdate(boolean async) throws Exception
    {
        String sessionToken = as.login(TEST_USER, PASSWORD);

        String experimentCode = "TEST-IMPORT-" + UUID.randomUUID().toString();
        ExperimentIdentifier experimentIdentifier = new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/" + experimentCode);

        try
        {
            ExperimentCreation creation = new ExperimentCreation();
            creation.setCode(experimentCode);
            creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
            creation.setProjectId(new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT"));
            creation.setProperty("DESCRIPTION", "initial value");

            Experiment experiment = getObject(sessionToken, experimentIdentifier);
            assertNull(experiment);

            as.createExperiments(sessionToken, Arrays.asList(creation));

            experiment = getObject(sessionToken, experimentIdentifier);
            assertEquals("initial value", experiment.getProperty("DESCRIPTION"));

            ImportFile file = new ImportFile("identifier", "DESCRIPTION");
            file.addLine(experimentIdentifier.getIdentifier(), "imported description");
            uploadFiles(sessionToken, TEST_UPLOAD_KEY, file.toString());
            assertUploadedFiles(sessionToken, file.toString());

            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put(PARAM_UPLOAD_KEY, TEST_UPLOAD_KEY);
            parameters.put(PARAM_TYPE_CODE, "SIRNA_HCS");
            parameters.put(PARAM_ASYNC, async);

            if (async)
            {
                parameters.put(PARAM_USER_EMAIL, TEST_EMAIL);
            }

            long timestamp = getTimestampAndWaitASecond();
            String message = executeImport(sessionToken, "updateExperiments", parameters);

            experiment = getObject(sessionToken, experimentIdentifier, timestamp, DEFAULT_TIMEOUT);
            assertEquals("imported description", experiment.getProperty("DESCRIPTION"));

            if (async)
            {
                assertEquals("When the import is complete the confirmation or failure report will be sent by email.", message);
                assertEmail(timestamp, TEST_EMAIL, "Experiment Batch Update successfully performed");
            } else
            {
                assertEquals("Update of 1 experiment(s) is complete.", message);
                assertNoEmails(timestamp);
            }

            assertUploadedFiles(sessionToken);

        } finally
        {
            ExperimentDeletionOptions options = new ExperimentDeletionOptions();
            options.setReason("cleanup");
            as.deleteExperiments(sessionToken, Arrays.asList(experimentIdentifier), options);
        }
    }

}
