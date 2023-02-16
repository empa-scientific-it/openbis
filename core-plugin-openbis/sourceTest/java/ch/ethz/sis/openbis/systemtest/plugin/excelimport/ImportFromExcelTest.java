/*
 * Copyright ETH 2019 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.systemtest.plugin.excelimport;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.IOException;
import java.nio.file.Paths;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;

@ContextConfiguration(locations = "classpath:applicationContext.xml")
@Transactional(transactionManager = "transaction-manager")
@Rollback
public class ImportFromExcelTest extends AbstractImportTest
{

    @Autowired
    private IApplicationServerInternalApi v3api;

    private static final String WITH_BLANKS = "xls/with_blanks.xlsx";

    @BeforeClass
    public void setupClass() throws IOException
    {
        String f = ImportExperimentTypesTest.class.getName().replace(".", "/");
        FILES_DIR = f.substring(0, f.length() - ImportExperimentTypesTest.class.getSimpleName().length()) + "/test_files/";
    }

    @Test(expectedExceptions = UserFailureException.class)
    @DirtiesContext
    public void testFileWithManyBlankRowsWasParsed() throws Exception
    {
        // the Excel contains internally managed property types which can be only manipulated by the system user
        sessionToken = v3api.loginAsSystem();

        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, WITH_BLANKS)));

        ExperimentType propertyType = TestUtils.getExperimentType(v3api, sessionToken, "COLLECTION");
        assertNotNull(propertyType); // the last line on the first sheet was read
        assertEquals(propertyType.getPropertyAssignments().size(), 2);
        assertEquals(propertyType.getPropertyAssignments().get(1).getPropertyType().getCode(), "LAST_ROW_EXP_TYPE");

        Sample sample = TestUtils.getSample(v3api, sessionToken, "LAST_SAMPLE", "TEST_SPACE");
        assertNotNull(sample); // the last line on the second sheet was read
    }
}