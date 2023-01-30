/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.systemtest.freezing;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.IOperationExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.SynchronousOperationExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.update.ProjectUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.update.UpdateProjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.update.SpaceUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.update.UpdateSpacesOperation;

/**
 * @author Franz-Josef Elmer
 */
public class SpaceProjectRelationshipsFreezingTest extends FreezingTest
{
    private static final String PREFIX = "SPRFT-";

    private static final String SPACE_1 = PREFIX + "S1";

    private static final String SPACE_OF_PROJ = PREFIX + "S-PROJ";

    private static final String PROJECT = PREFIX + "PROJ";

    private SpacePermId space1;

    private SpacePermId spaceOfProject;

    private ProjectPermId project;

    @BeforeMethod
    public void createExamples()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        List<SpacePermId> spaces = v3api.createSpaces(sessionToken, Arrays.asList(space(SPACE_1), space(SPACE_OF_PROJ)));
        space1 = spaces.get(0);
        spaceOfProject = spaces.get(1);
        project = v3api.createProjects(sessionToken, Arrays.asList(project(spaceOfProject, PROJECT))).get(0);
        v3api.logout(sessionToken);
    }

    @Test(dataProvider = "liquidProjectSpaceSpaceRelations")
    public void testValidMoveProjectFromSpaceToSpace(FrozenFlags frozenFlagsForProject,
            FrozenFlags frozenFlagsForOldSpace, FrozenFlags frozenFlagsForNewSpace)
    {
        // Given
        setFrozenFlagsForProjects(frozenFlagsForProject, project);
        setFrozenFlagsForSpaces(frozenFlagsForOldSpace, spaceOfProject);
        setFrozenFlagsForSpaces(frozenFlagsForNewSpace, space1);
        assertEquals(getProject(project).getSpace().getCode(), SPACE_OF_PROJ);
        ProjectUpdate projectUpdate = new ProjectUpdate();
        projectUpdate.setProjectId(project);
        projectUpdate.setSpaceId(space1);

        // When
        v3api.updateProjects(systemSessionToken, Arrays.asList(projectUpdate));

        // Then
        assertEquals(getProject(project).getSpace().getCode(), SPACE_1);
    }

    @DataProvider(name = "liquidProjectSpaceSpaceRelations")
    public static Object[][] liquidProjectSpaceSpaceRelations()
    {
        List<FrozenFlags> combinationsForProject = new FrozenFlags(true).createAllCombinations();
        List<FrozenFlags> combinationsForSpace = new FrozenFlags(true).freezeForSample().createAllCombinations();
        combinationsForSpace.add(new FrozenFlags(false).freezeForProject());
        return asCartesianProduct(combinationsForProject, combinationsForSpace, combinationsForSpace);
    }

    @Test(dataProvider = "frozenProjectSpaceSpaceRelations")
    public void testInvalidMoveProjectFromSpaceToSpace(FrozenFlags frozenFlagsForProject,
            FrozenFlags frozenFlagsForOldSpace, FrozenFlags frozenFlagsForNewSpace)
    {
        // Given
        setFrozenFlagsForProjects(frozenFlagsForProject, project);
        setFrozenFlagsForSpaces(frozenFlagsForOldSpace, spaceOfProject);
        setFrozenFlagsForSpaces(frozenFlagsForNewSpace, space1);
        assertEquals(getProject(project).getSpace().getCode(), SPACE_OF_PROJ);
        ProjectUpdate projectUpdate = new ProjectUpdate();
        projectUpdate.setProjectId(project);
        projectUpdate.setSpaceId(space1);
        boolean frozen = frozenFlagsForNewSpace.isFrozen() && frozenFlagsForNewSpace.isFrozenForProject();
        String type = frozen ? "SET" : "REMOVE";
        String spaceCode = frozen ? SPACE_1 : SPACE_OF_PROJ;

        // When
        assertUserFailureException(Void -> v3api.updateProjects(systemSessionToken, Arrays.asList(projectUpdate)),
                // Then
                "ERROR: Operation " + type + " SPACE is not allowed because space " + spaceCode + " is frozen for project "
                        + PROJECT + ".");
    }

    @Test(dataProvider = "frozenProjectSpaceSpaceRelations")
    public void testInvalidMoveProjectFromSpaceToSpaceAfterMelting(FrozenFlags frozenFlagsForProject,
            FrozenFlags frozenFlagsForOldSpace, FrozenFlags frozenFlagsForNewSpace)
    {
        // Given
        setFrozenFlagsForProjects(frozenFlagsForProject, project);
        setFrozenFlagsForSpaces(frozenFlagsForOldSpace, spaceOfProject);
        setFrozenFlagsForSpaces(frozenFlagsForNewSpace, space1);
        setFrozenFlagsForSpaces(frozenFlagsForOldSpace.clone().melt(), spaceOfProject);
        setFrozenFlagsForSpaces(frozenFlagsForNewSpace.clone().melt(), space1);
        assertEquals(getProject(project).getSpace().getCode(), SPACE_OF_PROJ);
        ProjectUpdate projectUpdate = new ProjectUpdate();
        projectUpdate.setProjectId(project);
        projectUpdate.setSpaceId(space1);

        // When
        v3api.updateProjects(systemSessionToken, Arrays.asList(projectUpdate));

        // Then
        assertEquals(getProject(project).getSpace().getCode(), SPACE_1);
    }

    @DataProvider(name = "frozenProjectSpaceSpaceRelations")
    public static Object[][] frozenProjectSpaceSpaceRelations()
    {
        List<FrozenFlags> combinationsForProject = new FrozenFlags(true).createAllCombinations();
        List<FrozenFlags> combinationsForLiquidSpace = new FrozenFlags(true).freezeForSample().createAllCombinations();
        combinationsForLiquidSpace.add(new FrozenFlags(false).freezeForProject());
        List<FrozenFlags> combinationsForFrozenSpace = Arrays.asList(new FrozenFlags(true).freezeForProject());
        return merge(asCartesianProduct(combinationsForProject, combinationsForLiquidSpace, combinationsForFrozenSpace),
                asCartesianProduct(combinationsForProject, combinationsForFrozenSpace, combinationsForLiquidSpace),
                asCartesianProduct(combinationsForProject, combinationsForFrozenSpace, combinationsForFrozenSpace));
    }

    @Test(dataProvider = "liquidSpace")
    public void testValidAddProjectToSpace(FrozenFlags frozenFlagsForSpace)
    {
        // Given
        setFrozenFlagsForSpaces(frozenFlagsForSpace, space1);
        ProjectCreation projectCreation = new ProjectCreation();
        projectCreation.setSpaceId(space1);
        projectCreation.setCode(PREFIX + "-PROJ2");

        // When
        ProjectPermId id = v3api.createProjects(systemSessionToken, Arrays.asList(projectCreation)).iterator().next();

        // Then
        assertEquals(getProject(id).getSpace().getCode(), SPACE_1);
    }

    @DataProvider(name = "liquidSpace")
    public static Object[][] liquidSpace()
    {
        List<FrozenFlags> combinationsForLiquidSpace = new FrozenFlags(true).freezeForSample().createAllCombinations();
        combinationsForLiquidSpace.add(new FrozenFlags(false).freezeForProject());
        return asCartesianProduct(combinationsForLiquidSpace);
    }

    @Test
    public void testInvalidAddProjectToSpace()
    {
        // Given
        setFrozenFlagsForSpaces(new FrozenFlags(true).freezeForProject(), space1);
        ProjectCreation projectCreation = new ProjectCreation();
        projectCreation.setSpaceId(space1);
        projectCreation.setCode(PREFIX + "PROJ2");

        // When
        assertUserFailureException(Void -> v3api.createProjects(systemSessionToken, Arrays.asList(projectCreation)),
                // Then
                "ERROR: Operation SET SPACE is not allowed because space " + SPACE_1 + " is frozen for project "
                        + projectCreation.getCode() + ".");
    }

    @Test
    public void testInvalidAddProjectToSpaceAfterMelting()
    {
        // Given
        setFrozenFlagsForSpaces(new FrozenFlags(true).freezeForProject(), space1);
        setFrozenFlagsForSpaces(new FrozenFlags(true).freezeForProject().melt(), space1);
        ProjectCreation projectCreation = new ProjectCreation();
        projectCreation.setSpaceId(space1);
        projectCreation.setCode(PREFIX + "-PROJ2");

        // When
        ProjectPermId id = v3api.createProjects(systemSessionToken, Arrays.asList(projectCreation)).iterator().next();

        // Then
        assertEquals(getProject(id).getSpace().getCode(), SPACE_1);
    }

    @Test
    public void testFreezeSpaceAndProject()
    {
        // Given
        SpaceUpdate spaceUpdate = new SpaceUpdate();
        spaceUpdate.setSpaceId(spaceOfProject);
        spaceUpdate.freeze();
        spaceUpdate.freezeForProjects();
        ProjectUpdate projectUpdate = new ProjectUpdate();
        projectUpdate.setProjectId(project);
        projectUpdate.freeze();
        IOperationExecutionOptions op = new SynchronousOperationExecutionOptions();

        // When
        v3api.executeOperations(systemSessionToken, Arrays.asList(
                new UpdateSpacesOperation(spaceUpdate), new UpdateProjectsOperation(projectUpdate)), op);

        // Then
        assertEquals(getProject(project).getSpace().isFrozen(), true);
        assertEquals(getProject(project).isFrozen(), true);
    }
}
