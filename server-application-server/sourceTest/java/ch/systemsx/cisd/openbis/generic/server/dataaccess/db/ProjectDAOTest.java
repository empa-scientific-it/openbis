/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IProjectDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.IdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * Test cases for {@link ProjectDAO}.
 * 
 * @author Izabela Adamczyk
 */
@Test(groups =
{ "db", "project" })
public class ProjectDAOTest extends AbstractDAOTest
{

    private static final String DESCRIPTION_NEW_PROJECT = "New project.";

    private static final String NONEXISTENT = "nonexistent";

    public static final String NOE = "NOE";

    public static final String TESTPROJ = "TESTPROJ";

    public static final String TEST_PROJECT = "TEST-PROJECT";

    public static final String NEMO = "NEMO";

    public static final String DEFAULT = "DEFAULT";

    public static final String PROJECT_TO_DELETE = "PROJECT-TO-DELETE";

    static final String[] PRELOADED_PROJECTS =
    { DEFAULT, NEMO, NOE, NOE, PROJECT_TO_DELETE, TEST_PROJECT, TESTPROJ };

    @Test
    public void testListProjects()
    {
        final List<ProjectPE> projects = daoFactory.getProjectDAO().listProjects();
        Collections.sort(projects);
        for (int i = 0; i < projects.size(); i++)
        {
            assertEquals(PRELOADED_PROJECTS[i], projects.get(i).getCode());
        }
        assertEquals(PRELOADED_PROJECTS.length, projects.size());
    }

    @Test(dependsOnMethods = "testListProjects")
    public void testListProjectsFromGroup()
    {
        final List<ProjectPE> allProjects = daoFactory.getProjectDAO().listProjects();
        Collections.sort(allProjects);
        final ProjectPE defaultProject = allProjects.get(0);
        assertEquals(DEFAULT, defaultProject.getCode());
        final List<ProjectPE> groupProjects =
                daoFactory.getProjectDAO().listProjects(defaultProject.getSpace());
        assertEquals(3, groupProjects.size());
        Collections.sort(groupProjects);
        assertEquals(DEFAULT, groupProjects.get(0).getCode());
        assertEquals(NEMO, groupProjects.get(1).getCode());
        assertEquals(NOE, groupProjects.get(2).getCode());
    }

    @Test(dependsOnMethods = "testListProjects")
    public void testListProjectsFromAnotherGroup()
    {
        final List<ProjectPE> allProjects = daoFactory.getProjectDAO().listProjects();
        Collections.sort(allProjects);
        final ProjectPE testProject = allProjects.get(6);
        assertEquals(testProject.getCode(), TESTPROJ);
        final List<ProjectPE> groupProjects =
                daoFactory.getProjectDAO().listProjects(testProject.getSpace());
        assertEquals(1, groupProjects.size());
        Collections.sort(groupProjects);
        assertEquals(groupProjects.get(0).getCode(), TESTPROJ);
    }

    @Test
    public void testTryFindProject() throws Exception
    {
        final List<ProjectPE> allProjects = daoFactory.getProjectDAO().listProjects();
        Collections.sort(allProjects);
        final ProjectPE templateProject = allProjects.get(6);

        assertEquals(templateProject.getCode(), TESTPROJ);

        ProjectPE found =
                daoFactory.getProjectDAO().tryFindProject(templateProject.getSpace().getCode(), templateProject.getCode());
        assertEquals(TESTPROJ, found.getCode());
        assertEquals(templateProject.getSpace().getCode(), found.getSpace().getCode());
    }

    @Test
    public void testTryFindProjectNonexistent() throws Exception
    {
        final List<ProjectPE> allProjects = daoFactory.getProjectDAO().listProjects();
        Collections.sort(allProjects);
        final ProjectPE templateProject = allProjects.get(6);
        assertEquals(templateProject.getCode(), TESTPROJ);

        AssertJUnit.assertNull(daoFactory.getProjectDAO().tryFindProject(
                templateProject.getSpace().getCode(), NONEXISTENT));

        AssertJUnit.assertNull(daoFactory.getProjectDAO().tryFindProject(NONEXISTENT,
                templateProject.getCode()));
    }

    @Test
    public void testTryFindProjects() throws Exception
    {
        final List<ProjectPE> allProjects = daoFactory.getProjectDAO().listProjects();
        Collections.sort(allProjects);

        final ProjectPE noe = allProjects.get(2);
        final ProjectPE testproj = allProjects.get(6);

        assertEquals(noe.getCode(), NOE);
        assertEquals(testproj.getCode(), TESTPROJ);

        List<ProjectPE> found = daoFactory.getProjectDAO().tryFindProjects(
                Arrays.asList(IdentifierHelper.createProjectIdentifier(noe), IdentifierHelper.createFullProjectIdentifier(testproj)));

        Collections.sort(found);

        assertEquals(2, found.size());
        assertEquals(noe.getCode(), found.get(0).getCode());
        assertEquals(testproj.getCode(), found.get(1).getCode());
    }

    @Test
    public void testTryFindProjectsNonexistent() throws Exception
    {
        final List<ProjectPE> allProjects = daoFactory.getProjectDAO().listProjects();
        Collections.sort(allProjects);

        final ProjectPE noe = allProjects.get(2);

        assertEquals(noe.getCode(), NOE);

        List<ProjectPE> found = daoFactory.getProjectDAO().tryFindProjects(
                Arrays.asList(IdentifierHelper.createProjectIdentifier(noe), new ProjectIdentifier(NONEXISTENT, NONEXISTENT)));

        assertEquals(1, found.size());
        assertEquals(noe.getCode(), found.get(0).getCode());
    }

    @Test
    public void testTryGetByPermID() throws Exception
    {
        ProjectPE found = daoFactory.getProjectDAO().tryGetByPermID("20120814110011738-105");
        assertEquals(TEST_PROJECT, found.getCode());
    }

    @Test
    public void testTryGetByPermIDNonexistent() throws Exception
    {
        ProjectPE found = daoFactory.getProjectDAO().tryGetByPermID("UNKNOWN-PERM-ID");
        assertNull(found);
    }

    @Test
    public void testCreateProject() throws Exception
    {
        final List<ProjectPE> allProjects = daoFactory.getProjectDAO().listProjects();
        Collections.sort(allProjects);
        final ProjectPE templateProject = allProjects.get(6);
        assertEquals(templateProject.getCode(), TESTPROJ);
        AssertJUnit.assertNull(daoFactory.getProjectDAO().tryFindProject(
                templateProject.getSpace().getCode(), NONEXISTENT));
        final ProjectPE newProject =
                prepareProject(templateProject.getSpace(), NONEXISTENT, DESCRIPTION_NEW_PROJECT,
                        getSystemPerson());
        daoFactory.getProjectDAO().createProject(newProject, getTestPerson());
        final ProjectPE registeredProject =
                daoFactory.getProjectDAO().tryFindProject(
                        templateProject.getSpace().getCode(), NONEXISTENT);
        AssertJUnit.assertNotNull(registeredProject);
        assertEquals(registeredProject.getDescription(), DESCRIPTION_NEW_PROJECT);
        assertEquals(registeredProject.getSpace(), templateProject.getSpace());
        assertEquals(registeredProject.getProjectLeader(), getSystemPerson());
    }

    private ProjectPE prepareProject(SpacePE group, String code, String description, PersonPE leader)
    {
        final ProjectPE result = new ProjectPE();
        result.setCode(code);
        result.setPermId("20120814111307034-1");
        result.setDescription(description);
        result.setSpace(group);
        result.setProjectLeader(leader);
        final PersonPE systemPerson = getSystemPerson();
        result.setRegistrator(systemPerson);
        return result;
    }

    @Test
    public final void testDelete()
    {
        final IProjectDAO projectDAO = daoFactory.getProjectDAO();
        final ProjectPE deletedProject = findProject(TESTPROJ, "TESTGROUP");

        // Deleted project should have all collections which prevent it from deletion empty.
        assertTrue(deletedProject.getAttachments().isEmpty());
        assertTrue(deletedProject.getExperiments().isEmpty());

        // delete
        projectDAO.delete(deletedProject);

        // test successful deletion of project
        assertNull(projectDAO.tryGetByTechId(TechId.create(deletedProject)));

        // deleted project had objects connected that should not have been deleted:
        // - a group
        SpacePE group = deletedProject.getSpace();
        assertNotNull(group);
        assertNotNull(daoFactory.getSpaceDAO().tryGetByTechId(
                new TechId(HibernateUtils.getId(group))));
    }

    @Test(expectedExceptions = DataIntegrityViolationException.class)
    public final void testDeleteFail()
    {
        final IProjectDAO projectDAO = daoFactory.getProjectDAO();
        final ProjectPE deletedProject = findProject("NEMO", "CISD");

        // Deleted project should have experiments which prevent it from deletion.
        assertFalse(deletedProject.getExperiments().isEmpty());

        // delete
        projectDAO.delete(deletedProject);
    }

    public final ProjectPE findProject(String projectCode, String groupCode)
    {
        final IProjectDAO projectDAO = daoFactory.getProjectDAO();
        final ProjectPE project = projectDAO.tryFindProject(groupCode, projectCode);
        assertNotNull(project);

        return project;
    }

}
