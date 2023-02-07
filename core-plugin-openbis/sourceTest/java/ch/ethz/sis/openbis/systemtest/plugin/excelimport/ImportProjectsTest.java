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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

@ContextConfiguration(locations = "classpath:applicationContext.xml")
@Transactional(transactionManager = "transaction-manager")
@Rollback
public class ImportProjectsTest extends AbstractImportTest
{
    @Autowired
    private IApplicationServerInternalApi v3api;

    private static final String PROJECTS_XLS = "projects/with_spaces.xls";

    private static final String PROJECTS_NO_CODE = "projects/no_code.xls";

    private static final String PROJECTS_NO_DESCRIPTION = "projects/no_desc.xls";

    private static final String PROJECTS_NO_SPACE = "projects/no_space.xls";

    private static final String PROJECTS_WITH_SPACES_ON_SERVER = "projects/with_spaces_on_server.xls";

    private static final String PROJECTS_UPDATE = "projects/update.xls";

    private static final String SPACES = "projects/spaces.xls";

    private static String FILES_DIR;

    @BeforeClass
    public void setupClass() throws IOException
    {
        String f = ImportProjectsTest.class.getName().replace(".", "/");
        FILES_DIR = f.substring(0, f.length() - ImportProjectsTest.class.getSimpleName().length()) + "/test_files/";
    }

    @Test
    @DirtiesContext
    public void testProjectsAreCreated() throws IOException
    {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, PROJECTS_XLS)));
        // WHEN
        Project project = TestUtils.getProject(v3api, sessionToken, "TEST_PROJECT");
        // THEN
        assertEquals(project.getCode(), "TEST_PROJECT");
        assertEquals(project.getDescription(), "TEST");
        assertEquals(project.getSpace().getCode(), "TEST_SPACE");
    }

    @Test
    @DirtiesContext
    public void testExistProjectIsUpdated() throws IOException
    {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, UpdateMode.UPDATE_IF_EXISTS, Paths.get(FilenameUtils.concat(FILES_DIR, PROJECTS_XLS)));
        // WHEN
        TestUtils.createFrom(v3api, sessionToken, UpdateMode.UPDATE_IF_EXISTS, Paths.get(FilenameUtils.concat(FILES_DIR, PROJECTS_UPDATE)));
        Project project = TestUtils.getProject(v3api, sessionToken, "TEST_PROJECT", "TEST_SPACE2");
        // THEN
        assertEquals(project.getCode(), "TEST_PROJECT");
        assertEquals(project.getDescription(), "UPDATE");
        assertEquals(project.getSpace().getCode(), "TEST_SPACE2");
    }

    @Test
    @DirtiesContext
    public void testProjectsAreCreatedSecondProject() throws IOException
    {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, PROJECTS_XLS)));
        // WHEN
        Project project = TestUtils.getProject(v3api, sessionToken, "TEST_PROJECT2");
        // THEN
        assertEquals(project.getCode(), "TEST_PROJECT2");
        assertEquals(project.getDescription(), "description of another project");
        assertEquals(project.getSpace().getCode(), "TEST_SPACE2");
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void shouldThrowExceptionIfNoProjectCode() throws IOException
    {
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, PROJECTS_NO_CODE)));
    }

    @Test
    @DirtiesContext
    public void testProjectsAreCreatedNoDescription() throws IOException
    {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, PROJECTS_NO_DESCRIPTION)));
        // WHEN
        Project project = TestUtils.getProject(v3api, sessionToken, "TEST_PROJECT");
        // THEN
        assertEquals(project.getCode(), "TEST_PROJECT");
        assertEquals(project.getDescription(), null);
        assertEquals(project.getSpace().getCode(), "TEST_SPACE");
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void shouldThrowExceptionIfNoProjectSpace() throws IOException
    {
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, PROJECTS_NO_SPACE)));
    }

    @Test
    @DirtiesContext
    public void testProjectsAreCreatedSpaceOnServer() throws IOException
    {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SPACES)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, PROJECTS_WITH_SPACES_ON_SERVER)));
        // WHEN
        Project project = TestUtils.getProject(v3api, sessionToken, "TEST_PROJECT");
        // THEN
        assertEquals(project.getCode(), "TEST_PROJECT");
        assertEquals(project.getDescription(), "TEST");
        assertEquals(project.getSpace().getCode(), "TEST_SPACE");
    }

}
