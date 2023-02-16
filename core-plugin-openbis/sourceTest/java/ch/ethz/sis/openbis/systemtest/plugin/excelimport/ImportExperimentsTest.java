/*
 * Copyright ETH 2018 - 2023 Zürich, Scientific IT Services
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

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

@ContextConfiguration(locations = "classpath:applicationContext.xml")
@Transactional(transactionManager = "transaction-manager")
@Rollback
public class ImportExperimentsTest extends AbstractImportTest
{
    @Autowired
    private IApplicationServerInternalApi v3api;

    private static final String TEST_USER = "test";

    private static final String PASSWORD = "password";

    private static final String EXPERIMENT_XLS = "experiments/all_inside.xls";

    private static final String EXPERIMENTS_ALL_ELSEWHERE = "experiments/all_elsewhere.xls";

    private static final String EXPERIMENTS_WITH_TYPE_ELSEWHERE = "experiments/experiment_type_elsewhere.xls";

    private static final String EXPERIMENTS_NO_CODE = "experiments/no_code.xls";

    private static final String EXPERIMENTS_WITH_NON_MANDATORY_PROPERTY_MISSING = "experiments/no_non_mandatory_property.xls";

    private static final String EXPERIMENTS_NO_PROJECT_ATTRIBUTE = "experiments/no_project.xls";

    private static final String EXPERIMENTS_WITH_SPACE_AND_PROJECT_ELSEWHERE = "experiments/space_and_project_elsewhere.xls";

    private static final String EXPERIMENTS_SPACE_ELSEWHERE = "experiments/space_elsewhere.xls";

    private static final String EXPERIMENTS_WITH_TYPE_AND_SPACE_ELSEWHERE = "experiments/type_and_space_elsewhere.xls";

    private static final String EXPERIMENTS_WITH_MANDATORY_PROPERTY_MISSING = "experiments/with_mandatory_property_missing.xls";

    private static final String EXPERIMENTS_WITH_MANDATORY_PROPERTY_PRESENT = "experiments/with_mandatory_property.xls";

    private static final String EXPERIMENTS_PROPERTIES_COLUMNS_AS_LABELS = "experiments/with_properties_as_labels.xls";

    private static final String EXPERIMENTS_PROPERTIES_COLUMNS_AS_LABELS_TYPE_ON_SERVER = "experiments/with_properties_as_labels_type_elsewhere.xls";

    private static final String SPACE = "experiments/space.xls";

    private static final String PROJECT = "experiments/project.xls";

    private static final String EXPERIMENT_TYPE = "experiments/experiment_type.xls";

    private static final String EXPERIMENT_UPDATE = "experiments/update.xls";

    private static String FILES_DIR;

    @BeforeClass
    public void setupClass() throws IOException
    {
        String f = ImportExperimentsTest.class.getName().replace(".", "/");
        FILES_DIR = f.substring(0, f.length() - ImportExperimentsTest.class.getSimpleName().length()) + "/test_files/";
    }

    @Test
    @DirtiesContext
    public void testExperimentsAreCreated() throws IOException
    {
        // the Excel contains internally managed property types which can be only manipulated by the system user
        String sessionToken = v3api.loginAsSystem();

        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENT_XLS)));
        // WHEN
        Experiment experiment = TestUtils.getExperiment(v3api, sessionToken, "TEST_EXPERIMENT", "TEST_PROJECT", "TEST_SPACE");
        // THEN
        assertEquals(experiment.getCode(), "TEST_EXPERIMENT");
        assertEquals(experiment.getProject().getCode(), "TEST_PROJECT");
        assertEquals(experiment.getProperties().get("$NAME"), "Value");
        assertEquals(experiment.getProperties().get("DEFAULT_OBJECT_TYPE"), "OBJECT_TYPE");
    }

    @Test
    @DirtiesContext
    public void testExperimentsAreCreatedSecondExperiment() throws IOException
    {
        // the Excel contains internally managed property types which can be only manipulated by the system user
        String sessionToken = v3api.loginAsSystem();

        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENT_XLS)));
        // WHEN
        Experiment experiment = TestUtils.getExperiment(v3api, sessionToken, "TEST_EXPERIMENT2", "TEST_PROJECT", "TEST_SPACE");
        // THEN
        assertEquals(experiment.getCode(), "TEST_EXPERIMENT2");
        assertEquals(experiment.getProject().getCode(), "TEST_PROJECT");
        assertEquals(experiment.getProperties().get("$NAME"), "Other Value");
        assertEquals(experiment.getProperties().get("DEFAULT_OBJECT_TYPE"), "Random string");
    }

    @Test
    @DirtiesContext
    public void testExperimentsAreCreatedWithEverythingOnServer() throws IOException
    {
        // the Excel contains internally managed property types which can be only manipulated by the system user
        String sessionToken = v3api.loginAsSystem();

        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENT_TYPE)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SPACE)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, PROJECT)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENTS_ALL_ELSEWHERE)));
        // WHEN
        Experiment experiment = TestUtils.getExperiment(v3api, sessionToken, "TEST_EXPERIMENT", "TEST_PROJECT", "TEST_SPACE");
        // THEN
        assertEquals(experiment.getCode(), "TEST_EXPERIMENT");
        assertEquals(experiment.getProject().getCode(), "TEST_PROJECT");
        assertEquals(experiment.getProperties().get("$NAME"), "Value");
        assertEquals(experiment.getProperties().get("DEFAULT_OBJECT_TYPE"), "OBJECT_TYPE");
    }

    @Test
    @DirtiesContext
    public void testExperimentsAreCreatedWithEverythingOnServerAndInXls() throws IOException
    {
        // the Excel contains internally managed property types which can be only manipulated by the system user
        String sessionToken = v3api.loginAsSystem();

        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENT_TYPE)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SPACE)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, PROJECT)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENT_XLS)));
        // WHEN
        Experiment experiment = TestUtils.getExperiment(v3api, sessionToken, "TEST_EXPERIMENT", "TEST_PROJECT", "TEST_SPACE");
        // THEN
        assertEquals(experiment.getCode(), "TEST_EXPERIMENT");
        assertEquals(experiment.getProject().getCode(), "TEST_PROJECT");
        assertEquals(experiment.getProperties().get("$NAME"), "Value");
        assertEquals(experiment.getProperties().get("DEFAULT_OBJECT_TYPE"), "OBJECT_TYPE");
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void shouldThrowExceptionIfExperimentTypeDoesntExist() throws IOException
    {
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SPACE)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, PROJECT)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENTS_ALL_ELSEWHERE)));
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void shouldThrowExceptionIfProjectDoesntExist() throws IOException
    {
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENT_TYPE)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SPACE)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENTS_ALL_ELSEWHERE)));
    }

    @Test
    @DirtiesContext
    public void testExperimentsAreCreatedWithTypeOnServer() throws IOException
    {
        // the Excel contains internally managed property types which can be only manipulated by the system user
        String sessionToken = v3api.loginAsSystem();

        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENT_TYPE)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENTS_WITH_TYPE_ELSEWHERE)));
        // WHEN
        Experiment experiment = TestUtils.getExperiment(v3api, sessionToken, "TEST_EXPERIMENT", "TEST_PROJECT", "TEST_SPACE");
        // THEN
        assertEquals(experiment.getCode(), "TEST_EXPERIMENT");
        assertEquals(experiment.getProject().getCode(), "TEST_PROJECT");
        assertEquals(experiment.getProperties().get("$NAME"), "Value");
        assertEquals(experiment.getProperties().get("DEFAULT_OBJECT_TYPE"), "OBJECT_TYPE");
    }

    @Test
    @DirtiesContext
    public void testExperimentsAreCreatedWithTypeOnServerAndInXls() throws IOException
    {
        // the Excel contains internally managed property types which can be only manipulated by the system user
        String sessionToken = v3api.loginAsSystem();

        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENT_TYPE)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SPACE)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, PROJECT)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENTS_WITH_TYPE_ELSEWHERE)));
        // WHEN
        Experiment experiment = TestUtils.getExperiment(v3api, sessionToken, "TEST_EXPERIMENT", "TEST_PROJECT", "TEST_SPACE");
        // THEN
        assertEquals(experiment.getCode(), "TEST_EXPERIMENT");
        assertEquals(experiment.getProject().getCode(), "TEST_PROJECT");
        assertEquals(experiment.getProperties().get("$NAME"), "Value");
        assertEquals(experiment.getProperties().get("DEFAULT_OBJECT_TYPE"), "OBJECT_TYPE");
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void shouldThrowExceptionIfExperimentNoCode() throws IOException
    {
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENTS_NO_CODE)));
    }

    @Test
    @DirtiesContext
    public void testExperimentsAreCreatedWhenNonMandatoryPropertiesAreNotProvided() throws IOException
    {
        // the Excel contains internally managed property types which can be only manipulated by the system user
        String sessionToken = v3api.loginAsSystem();

        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENTS_WITH_NON_MANDATORY_PROPERTY_MISSING)));
        // WHEN
        Experiment experiment = TestUtils.getExperiment(v3api, sessionToken, "TEST_EXPERIMENT", "TEST_PROJECT", "TEST_SPACE");
        // THEN
        assertEquals(experiment.getCode(), "TEST_EXPERIMENT");
        assertEquals(experiment.getProject().getCode(), "TEST_PROJECT");
        assertEquals(experiment.getProperties().get("$NAME"), null);
        assertEquals(experiment.getProperties().get("DEFAULT_OBJECT_TYPE"), "OBJECT_TYPE");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void shouldThrowExceptionIfExperimentNoProject() throws IOException
    {
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENTS_NO_PROJECT_ATTRIBUTE)));
    }

    @Test
    @DirtiesContext
    public void testExperimentsAreCreatedWithSpaceAndProjectOnServer() throws IOException
    {
        // the Excel contains internally managed property types which can be only manipulated by the system user
        String sessionToken = v3api.loginAsSystem();

        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SPACE)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, PROJECT)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENTS_WITH_SPACE_AND_PROJECT_ELSEWHERE)));
        // WHEN
        Experiment experiment = TestUtils.getExperiment(v3api, sessionToken, "TEST_EXPERIMENT", "TEST_PROJECT", "TEST_SPACE");
        // THEN
        assertEquals(experiment.getCode(), "TEST_EXPERIMENT");
        assertEquals(experiment.getProject().getCode(), "TEST_PROJECT");
        assertEquals(experiment.getProperties().get("$NAME"), "Value");
        assertEquals(experiment.getProperties().get("DEFAULT_OBJECT_TYPE"), "OBJECT_TYPE");
    }

    @Test
    @DirtiesContext
    public void testExperimentsAreCreatedWithSpaceOnServer() throws IOException
    {
        // the Excel contains internally managed property types which can be only manipulated by the system user
        String sessionToken = v3api.loginAsSystem();

        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SPACE)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENTS_SPACE_ELSEWHERE)));
        // WHEN
        Experiment experiment = TestUtils.getExperiment(v3api, sessionToken, "TEST_EXPERIMENT", "TEST_PROJECT", "TEST_SPACE");
        // THEN
        assertEquals(experiment.getCode(), "TEST_EXPERIMENT");
        assertEquals(experiment.getProject().getCode(), "TEST_PROJECT");
        assertEquals(experiment.getProperties().get("$NAME"), "Value");
        assertEquals(experiment.getProperties().get("DEFAULT_OBJECT_TYPE"), "OBJECT_TYPE");
    }

    @Test
    @DirtiesContext
    public void testExperimentsAreCreatedWithTypeAndSpaceOnServer() throws IOException
    {
        // the Excel contains internally managed property types which can be only manipulated by the system user
        String sessionToken = v3api.loginAsSystem();

        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENT_TYPE)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SPACE)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENTS_WITH_TYPE_AND_SPACE_ELSEWHERE)));
        // WHEN
        Experiment experiment = TestUtils.getExperiment(v3api, sessionToken, "TEST_EXPERIMENT", "TEST_PROJECT", "TEST_SPACE");
        // THEN
        assertEquals(experiment.getCode(), "TEST_EXPERIMENT");
        assertEquals(experiment.getProject().getCode(), "TEST_PROJECT");
        assertEquals(experiment.getProperties().get("$NAME"), "Value");
        assertEquals(experiment.getProperties().get("DEFAULT_OBJECT_TYPE"), "OBJECT_TYPE");
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void shouldThrowExceptionIfMandatoryPropertyMissing() throws IOException
    {
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENTS_WITH_MANDATORY_PROPERTY_MISSING)));
    }

    @Test
    @DirtiesContext
    public void testExperimentsAreCreatedIfMandatoryPropertyArePresent() throws IOException
    {
        // the Excel contains internally managed property types which can be only manipulated by the system user
        String sessionToken = v3api.loginAsSystem();

        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENTS_WITH_MANDATORY_PROPERTY_PRESENT)));
        // WHEN
        Experiment experiment = TestUtils.getExperiment(v3api, sessionToken, "TEST_EXPERIMENT", "TEST_PROJECT", "TEST_SPACE");
        // THEN
        assertEquals(experiment.getCode(), "TEST_EXPERIMENT");
        assertEquals(experiment.getProject().getCode(), "TEST_PROJECT");
        assertEquals(experiment.getProperties().get("$NAME"), "Value");
        assertEquals(experiment.getProperties().get("DEFAULT_OBJECT_TYPE"), "OBJECT_TYPE");
    }

    @Test
    @DirtiesContext
    public void testExperimentsAreCreatedWhenPropertiesAreAddressedByLabelsWithTypeInXls() throws IOException
    {
        // the Excel contains internally managed property types which can be only manipulated by the system user
        String sessionToken = v3api.loginAsSystem();

        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENTS_PROPERTIES_COLUMNS_AS_LABELS)));
        // WHEN
        Experiment experiment = TestUtils.getExperiment(v3api, sessionToken, "TEST_EXPERIMENT", "TEST_PROJECT", "TEST_SPACE");
        // THEN
        assertEquals(experiment.getCode(), "TEST_EXPERIMENT");
        assertEquals(experiment.getProject().getCode(), "TEST_PROJECT");
        assertEquals(experiment.getProperties().get("$NAME"), "Value");
        assertEquals(experiment.getProperties().get("DEFAULT_OBJECT_TYPE"), "OBJECT_TYPE");
    }

    @Test
    @DirtiesContext
    public void testExperimentsAreCreatedWhenPropertiesAreAddressedByLabelsWithTypeOnServer() throws IOException
    {
        // the Excel contains internally managed property types which can be only manipulated by the system user
        String sessionToken = v3api.loginAsSystem();

        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENT_TYPE)));
        TestUtils.createFrom(v3api, sessionToken,
                Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENTS_PROPERTIES_COLUMNS_AS_LABELS_TYPE_ON_SERVER)));
        // WHEN
        Experiment experiment = TestUtils.getExperiment(v3api, sessionToken, "TEST_EXPERIMENT", "TEST_PROJECT", "TEST_SPACE");
        // THEN
        assertEquals(experiment.getCode(), "TEST_EXPERIMENT");
        assertEquals(experiment.getProject().getCode(), "TEST_PROJECT");
        assertEquals(experiment.getProperties().get("$NAME"), "Value");
        assertEquals(experiment.getProperties().get("DEFAULT_OBJECT_TYPE"), "OBJECT_TYPE");
    }

//    @Test
//    @DirtiesContext
//    public void testExperimentsUpdate() throws Exception
//    {
//        // GIVEN
//        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENT_XLS)));
//        // WHEN
//        TestUtils.createFrom(v3api, sessionToken, TestUtils.getDynamicPluginMap(), UpdateMode.UPDATE_IF_EXISTS,
//                Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENT_UPDATE)));
//        Experiment experiment = TestUtils.getExperiment(v3api, sessionToken, "TEST_EXPERIMENT", "TEST_PROJECT", "TEST_SPACE");
//        // THEN
//        assertEquals(experiment.getProperties().get("$NAME"), "NameUpdate");
//        assertEquals(experiment.getProperties().get("DEFAULT_OBJECT_TYPE"), "DefaultObjectTypeUpdate");
//    }

}
