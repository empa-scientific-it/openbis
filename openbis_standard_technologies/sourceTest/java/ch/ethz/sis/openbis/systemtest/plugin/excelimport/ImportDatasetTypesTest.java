package ch.ethz.sis.openbis.systemtest.plugin.excelimport;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

@ContextConfiguration(locations = "classpath:applicationContext.xml")
@Transactional(transactionManager = "transaction-manager")
@Rollback
public class ImportDatasetTypesTest extends AbstractImportTest
{

    private static final String DATASET_TYPES_XLS = "dataset_types/normal_dataset.xls";

    private static final String DATASET_NO_CODE = "dataset_types/no_code.xls";

    private static final String DATASET_WITH_VALIDATION_SCRIPT = "dataset_types/with_validation.xls";

    private static final String DATASET_WITHOUT_PROPERTIES = "dataset_types/no_properties.xls";

    private static final String DATASET_TYPES_UPDATE = "dataset_types/normal_dataset_update.xls";

    private static String FILES_DIR;

    @BeforeClass
    public void setupClass() throws IOException
    {
        String f = ImportDatasetTypesTest.class.getName().replace(".", "/");
        FILES_DIR = f.substring(0, f.length() - ImportDatasetTypesTest.class.getSimpleName().length()) + "/test_files/";
    }

    @Test
    @DirtiesContext
    public void testNormalDatasetTypesAreCreated() throws Exception
    {
        // the Excel contains internally managed property types which can be only manipulated by the system user
        sessionToken = v3api.loginAsSystem();

        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, DATASET_TYPES_XLS)));
        // WHEN
        DataSetType rawData = TestUtils.getDatasetType(v3api, sessionToken, "RAW_DATA");
        List<String> propertyNames = Arrays.asList("$NAME", "NOTES");
        List<PropertyAssignment> propertyAssignments = TestUtils.extractAndSortPropertyAssignmentsPerGivenPropertyName(rawData, propertyNames);
        PropertyAssignment nameProperty = propertyAssignments.get(0);
        PropertyAssignment notesProperty = propertyAssignments.get(1);
        // THEN
        assertEquals(rawData.getCode(), "RAW_DATA");
        assertEquals(rawData.getPropertyAssignments().size(), 2);
        assertFalse(nameProperty.isMandatory());
        assertTrue(nameProperty.isShowInEditView());
        assertEquals(nameProperty.getSection(), "General information");
        assertEquals(nameProperty.getPropertyType().getLabel(), "Name");
        assertEquals(nameProperty.getPropertyType().getDataType(), DataType.VARCHAR);
        assertEquals(nameProperty.getPropertyType().getDescription(), "Name");
        assertEquals(nameProperty.getPlugin(), null);
        assertFalse(notesProperty.isMandatory());
        assertTrue(notesProperty.isShowInEditView());
        assertEquals(notesProperty.getSection(), "Comments");
        assertEquals(notesProperty.getPropertyType().getLabel(), "Notes");
        assertEquals(notesProperty.getPropertyType().getDataType(), DataType.MULTILINE_VARCHAR);
        assertEquals(notesProperty.getPropertyType().getDescription(), "Notes");
        assertEquals(notesProperty.getPlugin(), null);
    }

    @Test
    @DirtiesContext
    public void testDatasetTypesWithoutPropertiesTypesAreCreated() throws IOException
    {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, DATASET_WITHOUT_PROPERTIES)));
        // WHEN
        DataSetType rawData = TestUtils.getDatasetType(v3api, sessionToken, "RAW_DATA");
        // THEN
        assertEquals(rawData.getCode(), "RAW_DATA");
        assertEquals(rawData.getPropertyAssignments().size(), 0);
    }

    @Test
    @DirtiesContext
    public void testDatasetTypesWithValidationScript() throws Exception
    {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, TestUtils.getValidationPluginMap(),
                Paths.get(FilenameUtils.concat(FILES_DIR, DATASET_WITH_VALIDATION_SCRIPT)));
        // WHEN
        DataSetType rawData = TestUtils.getDatasetType(v3api, sessionToken, "RAW_DATA");
        // THEN
        assertEquals(rawData.getValidationPlugin().getName().toUpperCase(), "RAW_DATA.VALID");
    }

    @Test
    @DirtiesContext
    public void testDatasetTypesUpdate() throws Exception
    {
        // the Excel contains internally managed property types which can be only manipulated by the system user
        sessionToken = v3api.loginAsSystem();

        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, DATASET_TYPES_XLS)));
        // WHEN
        TestUtils.createFrom(v3api, sessionToken, TestUtils.getDynamicPluginMap(), UpdateMode.UPDATE_IF_EXISTS,
                Paths.get(FilenameUtils.concat(FILES_DIR, DATASET_TYPES_UPDATE)));
        DataSetType rawData = TestUtils.getDatasetType(v3api, sessionToken, "RAW_DATA");
        List<String> propertyNames = Arrays.asList("$NAME", "NOTES");
        List<PropertyAssignment> propertyAssignments = TestUtils.extractAndSortPropertyAssignmentsPerGivenPropertyName(rawData, propertyNames);
        PropertyAssignment nameProperty = propertyAssignments.get(0);
        PropertyAssignment notesProperty = propertyAssignments.get(1);
        // THEN
        // Property Assignment updates are not supported, no change here between updates.
        assertTrue(nameProperty.isMandatory());
        assertFalse(nameProperty.isShowInEditView());
        assertEquals(nameProperty.getSection(), "Comments");
        assertEquals(nameProperty.getPropertyType().getLabel(), "NameUpdate");
        assertEquals(nameProperty.getPropertyType().getDataType(), DataType.VARCHAR);
        assertEquals(nameProperty.getPropertyType().getDescription(), "NameDescriptionUpdate");
        assertEquals(nameProperty.getPlugin(), null);
        assertFalse(notesProperty.isMandatory());
        assertTrue(notesProperty.isShowInEditView());
        assertEquals(notesProperty.getSection(), "Comments");
        assertEquals(notesProperty.getPropertyType().getLabel(), "Notes");
        assertEquals(notesProperty.getPropertyType().getDataType(), DataType.MULTILINE_VARCHAR);
        assertEquals(notesProperty.getPropertyType().getDescription(), "Notes");
        assertEquals(notesProperty.getPlugin(), null);
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void shouldThrowExceptionIfNoSampleCode() throws IOException
    {
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, DATASET_NO_CODE)));
    }

}
