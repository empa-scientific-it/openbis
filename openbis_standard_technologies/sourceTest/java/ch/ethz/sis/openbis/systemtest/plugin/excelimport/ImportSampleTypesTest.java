package ch.ethz.sis.openbis.systemtest.plugin.excelimport;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.PluginType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

@ContextConfiguration(locations = "classpath:applicationContext.xml")
@Transactional(transactionManager = "transaction-manager")
@Rollback
public class ImportSampleTypesTest extends AbstractImportTest {

    private static final String SAMPLE_TYPES_XLS = "sample_types/normal_samples.xls";

    private static final String SAMPLE_TYPES_XLS_DIFFERENT_PROPERTY_ASSIGN = "sample_types/normal_samples_v2.xls";

    private static final String SAMPLE_TYPES_WITH_DYNAMIC_SCRIPT = "sample_types/with_dynamic_script.xls";

    private static final String SAMPLE_TYPES_WITH_VALIDATION_SCRIPT = "sample_types/with_validation_script.xls";

    private static final String SAMPLE_TYPES_WITH_VOCABULARY = "sample_types/with_vocabulary_in_xls.xls";

    private static final String SAMPLE_TYPES_WITH_VOCABULARY_ON_SERVER = "sample_types/with_vocabulary_on_server.xls";

    private static final String VOCABULARY_DETECTION = "sample_types/vocabulary_detection.xls";

    private static final String SAMPLE_TYPES_WITH_AUTO_GENERATED_CODES = "sample_types/with_auto_generated_codes.xls";

    private static final String SAMPLE_TYPE_NO_CODE = "sample_types/no_code.xls";

    @Autowired
    private IApplicationServerInternalApi v3api;

    private static final String TEST_USER = "test";

    private static final String PASSWORD = "password";

    private static String FILES_DIR;

    @BeforeClass
    public void setupClass() throws IOException {
        String f = ImportSampleTypesTest.class.getName().replace(".", "/");
        FILES_DIR = f.substring(0, f.length() - ImportSampleTypesTest.class.getSimpleName().length()) + "/test_files/";
    }

    @Test
    @DirtiesContext
    public void testNormalSampleTypesAreCreated() throws IOException {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SAMPLE_TYPES_XLS)));
        // WHEN
        SampleType antibody = TestUtils.getSampleType(v3api, sessionToken, "ANTIBODY");
        // THEN
        assertFalse(antibody.isAutoGeneratedCode());
    }

    @Test
    @DirtiesContext
    public void testPropertyTypeAssignmentsFromNormalSampleTypesAreCreated() throws IOException {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SAMPLE_TYPES_XLS)));
        // WHEN
        SampleType antibody = TestUtils.getSampleType(v3api, sessionToken, "ANTIBODY");
        // THEN
        boolean allMandatory = antibody.getPropertyAssignments().stream().allMatch(propAssignment -> propAssignment.isMandatory() == true);
        boolean allShownInEditView =
                antibody.getPropertyAssignments().stream().allMatch(propAssignment -> propAssignment.isShowInEditView() == true);
        boolean generalInformationExists =
                antibody.getPropertyAssignments().stream().anyMatch(propAssignment -> propAssignment.getSection().equals("General information"));
        boolean someOtherSectionExists =
                antibody.getPropertyAssignments().stream().anyMatch(propAssignment -> propAssignment.getSection().equals("Some other section"));
        boolean threePropertyAssignments = antibody.getPropertyAssignments().size() == 3;
        assertTrue(threePropertyAssignments);
        assertTrue(generalInformationExists);
        assertTrue(someOtherSectionExists);
        assertTrue(allShownInEditView);
        assertTrue(allMandatory);
    }

    @Test
    @DirtiesContext
    public void testPropertyTypeAssignmentsFromNormalv2SampleTypesAreCreated() throws IOException {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SAMPLE_TYPES_XLS_DIFFERENT_PROPERTY_ASSIGN)));
        // WHEN
        SampleType antibody = TestUtils.getSampleType(v3api, sessionToken, "ANTIBODY");
        // THEN
        boolean allNotMandatory = antibody.getPropertyAssignments().stream().allMatch(propAssignment -> propAssignment.isMandatory() == false);
        boolean allNotShownInEditView =
                antibody.getPropertyAssignments().stream().allMatch(propAssignment -> propAssignment.isShowInEditView() == false);
        boolean threePropertyAssignments = antibody.getPropertyAssignments().size() == 3;
        assertTrue(threePropertyAssignments);
        assertTrue(allNotShownInEditView);
        assertTrue(allNotMandatory);
    }

    @Test
    @DirtiesContext
    public void testPropertyTypesFromNormalSampleTypesAreCreated() throws IOException {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SAMPLE_TYPES_XLS)));
        // WHEN
        SampleType antibody = TestUtils.getSampleType(v3api, sessionToken, "ANTIBODY");
        // THEN
        boolean namePropertyExists =
                antibody.getPropertyAssignments().stream().anyMatch(propAssignment -> propAssignment.getPropertyType().getCode().equals("$NAME"));
        boolean forWhatPropertyExists =
                antibody.getPropertyAssignments().stream().anyMatch(propAssignment -> propAssignment.getPropertyType().getCode().equals("FOR_WHAT"));
        boolean epitopePropertyExists =
                antibody.getPropertyAssignments().stream().anyMatch(propAssignment -> propAssignment.getPropertyType().getCode().equals("EPITOPE"));

        assertNotNull(antibody);
        assertTrue(namePropertyExists);
        assertTrue(forWhatPropertyExists);
        assertTrue(epitopePropertyExists);
    }

    @Test
    @DirtiesContext
    public void testSampleTypesWithPropertyHavingDynamicScript() throws IOException {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, TestUtils.getDynamicPluginMap(),
                Paths.get(FilenameUtils.concat(FILES_DIR, SAMPLE_TYPES_WITH_DYNAMIC_SCRIPT)));
        // WHEN
        SampleType antibody = TestUtils.getSampleType(v3api, sessionToken, "ANTIBODY");
        // THEN
        Plugin dynamicScript = antibody.getPropertyAssignments().get(0).getPlugin();
        assertNotNull(dynamicScript);
        assertEquals(dynamicScript.getName().toUpperCase(), "$NAME.DYNAMIC");
        assertEquals(dynamicScript.getScript(), TestUtils.getDynamicScript());
        assertEquals(dynamicScript.getPluginType(), PluginType.DYNAMIC_PROPERTY);
    }

    @Test
    @DirtiesContext
    public void testSampleTypesWithPropertyHavingValidationScript() throws IOException {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, TestUtils.getValidationPluginMap(),
                Paths.get(FilenameUtils.concat(FILES_DIR, SAMPLE_TYPES_WITH_VALIDATION_SCRIPT)));
        // WHEN
        SampleType antibody = TestUtils.getSampleType(v3api, sessionToken, "ANTIBODY");
        // THEN
        Plugin validationScript = antibody.getValidationPlugin();
        assertNotNull(validationScript);
        assertEquals(validationScript.getName().toUpperCase(), "ANTIBODY.VALID");
        assertEquals(validationScript.getScript(), TestUtils.getValidationScript());
        assertEquals(validationScript.getPluginType(), PluginType.ENTITY_VALIDATION);
    }

    @Test
    @DirtiesContext
    public void testSampleTypesWithVocabularyInXls() throws IOException {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SAMPLE_TYPES_WITH_VOCABULARY)));
        // WHEN
        SampleType antibody = TestUtils.getSampleType(v3api, sessionToken, "ANTIBODY");
        // THEN
        PropertyAssignment propertyAssignment = antibody.getPropertyAssignments().get(0);
        assertNotNull(propertyAssignment);
        assertEquals(propertyAssignment.getPropertyType().getVocabulary().getCode(), "DETECTION");
    }

    @Test
    @DirtiesContext
    public void testSampleTypesWithVocabularyOnServer() throws IOException {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, VOCABULARY_DETECTION)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SAMPLE_TYPES_WITH_VOCABULARY_ON_SERVER)));
        // WHEN
        SampleType antibody = TestUtils.getSampleType(v3api, sessionToken, "ANTIBODY");
        // THEN
        PropertyAssignment propertyAssignment = antibody.getPropertyAssignments().get(0);
        assertNotNull(propertyAssignment);
        assertEquals(propertyAssignment.getPropertyType().getVocabulary().getCode(), "DETECTION");
    }

    @Test
    @DirtiesContext
    public void testSampleTypesWithAutoGeneratedCodeAttribute() throws IOException {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SAMPLE_TYPES_WITH_AUTO_GENERATED_CODES)));
        // WHEN
        SampleType antibody = TestUtils.getSampleType(v3api, sessionToken, "SECONDBODY");
        // THEN
        assertTrue(antibody.isAutoGeneratedCode());
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void shouldThrowExceptionIfNoSampleCode() throws IOException {
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SAMPLE_TYPE_NO_CODE)));
    }

}
