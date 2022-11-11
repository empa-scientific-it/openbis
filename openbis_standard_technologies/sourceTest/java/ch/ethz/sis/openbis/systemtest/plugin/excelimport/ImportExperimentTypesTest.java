package ch.ethz.sis.openbis.systemtest.plugin.excelimport;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

@ContextConfiguration(locations = "classpath:applicationContext.xml")
@Transactional(transactionManager = "transaction-manager")
@Rollback
public class ImportExperimentTypesTest extends AbstractImportTest
{

    @Autowired
    private IApplicationServerInternalApi v3api;

    private static final String EXPERIMENT_TYPES_XLS = "experiment_types/normal_experiment.xls";

    private static final String EXPERIMENT_TYPES_UPDATE = "experiment_types/normal_experiment_update.xls";

    private static final String EXPERIMENT_NO_CODE = "experiment_types/no_code.xls";

    private static final String EXPERIMENT_WITH_VALIDATION_SCRIPT = "experiment_types/with_validation_script.xls";

    private static String FILES_DIR;

    @BeforeClass
    public void setupClass() throws IOException
    {
        String f = ImportExperimentTypesTest.class.getName().replace(".", "/");
        FILES_DIR = f.substring(0, f.length() - ImportExperimentTypesTest.class.getSimpleName().length()) + "/test_files/";
    }

    @Test
    @DirtiesContext
    public void testNormalExperimentTypesAreCreated() throws Exception
    {
        // the Excel contains internally managed property types which can be only manipulated by the system user
        String sessionToken = v3api.loginAsSystem();

        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENT_TYPES_XLS)));
        // WHEN
        ExperimentType collection = TestUtils.getExperimentType(v3api, sessionToken, "COLLECTION");
        List<String> propertyNames = Arrays.asList("$NAME", "DEFAULT_OBJECT_TYPE");
        List<PropertyAssignment> propertyAssignments = TestUtils.extractAndSortPropertyAssignmentsPerGivenPropertyName(collection, propertyNames);
        PropertyAssignment nameProperty = propertyAssignments.get(0);
        PropertyAssignment defaultObjectTypeProperty = propertyAssignments.get(1);
        // THEN
        assertEquals(collection.getCode(), "COLLECTION");
        assertEquals(collection.getPropertyAssignments().size(), 2);
        assertFalse(nameProperty.isMandatory());
        assertTrue(nameProperty.isShowInEditView());
        assertEquals(nameProperty.getSection(), "General information");
        assertEquals(nameProperty.getPropertyType().getLabel(), "Name");
        assertEquals(nameProperty.getPropertyType().getDataType(), DataType.VARCHAR);
        assertEquals(nameProperty.getPropertyType().getDescription(), "Name");
        assertEquals(nameProperty.getPlugin(), null);
        assertFalse(defaultObjectTypeProperty.isMandatory());
        assertTrue(defaultObjectTypeProperty.isShowInEditView());
        assertEquals(defaultObjectTypeProperty.getSection(), "General information");
        assertEquals(defaultObjectTypeProperty.getPropertyType().getLabel(), "Default");
        assertEquals(defaultObjectTypeProperty.getPropertyType().getDataType(), DataType.VARCHAR);
        assertEquals(defaultObjectTypeProperty.getPropertyType().getDescription(),
                "Enter the code of the object type for which the collection is used");
        assertEquals(defaultObjectTypeProperty.getPlugin(), null);
    }

    @Test
    @DirtiesContext
    public void testExperimentTypesUpdate() throws Exception
    {
        // the Excel contains internally managed property types which can be only manipulated by the system user
        String sessionToken = v3api.loginAsSystem();

        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENT_TYPES_XLS)));
        // WHEN
        TestUtils.createFrom(v3api, sessionToken, TestUtils.getDynamicPluginMap(), UpdateMode.UPDATE_IF_EXISTS,
                Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENT_TYPES_UPDATE)));
        ExperimentType collection = TestUtils.getExperimentType(v3api, sessionToken, "COLLECTION");
        List<String> propertyNames = Arrays.asList("$NAME", "DEFAULT_OBJECT_TYPE");
        List<PropertyAssignment> propertyAssignments = TestUtils.extractAndSortPropertyAssignmentsPerGivenPropertyName(collection, propertyNames);
        PropertyAssignment nameProperty = propertyAssignments.get(0);
        PropertyAssignment defaultObjectTypeProperty = propertyAssignments.get(1);
        // THEN
        // Property Assignment updates are not supported, no change here between updates.
        assertTrue(nameProperty.isMandatory());
        assertFalse(nameProperty.isShowInEditView());
        assertEquals(nameProperty.getSection(), "Comments");
        assertEquals(nameProperty.getPropertyType().getDescription(), "NameUpdateDescription");
        assertEquals(nameProperty.getPropertyType().getLabel(), "NameUpdate");
        assertEquals(nameProperty.getPropertyType().getDataType(), DataType.VARCHAR);
        assertEquals(nameProperty.getPlugin(), null);
        assertFalse(defaultObjectTypeProperty.isMandatory());
        assertTrue(defaultObjectTypeProperty.isShowInEditView());
        assertEquals(defaultObjectTypeProperty.getSection(), "General information");
        assertEquals(defaultObjectTypeProperty.getPropertyType().getLabel(), "Default");
        assertEquals(defaultObjectTypeProperty.getPropertyType().getDataType(), DataType.VARCHAR);
        assertEquals(defaultObjectTypeProperty.getPropertyType().getDescription(),
                "Enter the code of the object type for which the collection is used");
        assertEquals(defaultObjectTypeProperty.getPlugin(), null);
    }

    @Test
    @DirtiesContext
    public void testExperimentTypesWithValidationScript() throws IOException
    {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, TestUtils.getValidationPluginMap(),
                Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENT_WITH_VALIDATION_SCRIPT)));
        // WHEN
        ExperimentType collection = TestUtils.getExperimentType(v3api, sessionToken, "COLLECTION");
        // THEN
        assertEquals(collection.getValidationPlugin().getName().toUpperCase(), "COLLECTION.VALID");
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void shouldThrowExceptionIfNoSampleCode() throws IOException
    {
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENT_NO_CODE)));
    }

}
