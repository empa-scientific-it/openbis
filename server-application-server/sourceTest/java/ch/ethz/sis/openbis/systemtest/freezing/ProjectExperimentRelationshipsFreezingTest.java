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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.UpdateExperimentsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.IOperationExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.SynchronousOperationExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.update.ProjectUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.update.UpdateProjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;

/**
 * @author Franz-Josef Elmer
 */
public class ProjectExperimentRelationshipsFreezingTest extends FreezingTest
{
    private static final String PREFIX = "PERFT-";

    private static final String SPACE = PREFIX + "S1";

    private static final String PROJECT_1 = PREFIX + "P1";

    private static final String PROJECT_OF_EXP = PREFIX + "P-EXP";

    private static final String EXP = PREFIX + "EXP";

    private ProjectPermId project1;

    private ProjectPermId projectOfExperiment;

    private ExperimentPermId experiment;

    @BeforeMethod
    public void createExamples()
    {
        SpacePermId space = v3api.createSpaces(systemSessionToken, Arrays.asList(space(SPACE))).get(0);
        List<ProjectPermId> projects = v3api.createProjects(systemSessionToken,
                Arrays.asList(project(space, PROJECT_1), project(space, PROJECT_OF_EXP)));
        project1 = projects.get(0);
        projectOfExperiment = projects.get(1);
        experiment = v3api.createExperiments(systemSessionToken,
                Arrays.asList(experiment(projectOfExperiment, EXP))).get(0);
    }

    @Test(dataProvider = "liquidExperimentProjectProjectRelations")
    public void testValidMoveExperimentFromProjectToProject(FrozenFlags frozenFlagsForExperiment,
            FrozenFlags frozenFlagsForOldProject, FrozenFlags frozenFlagsForNewProject)
    {
        // Given
        setFrozenFlagsForExperiments(frozenFlagsForExperiment, experiment);
        setFrozenFlagsForProjects(frozenFlagsForOldProject, projectOfExperiment);
        setFrozenFlagsForProjects(frozenFlagsForNewProject, project1);
        assertEquals(getExperiment(experiment).getProject().getCode(), PROJECT_OF_EXP);
        ExperimentUpdate experimentUpdate = new ExperimentUpdate();
        experimentUpdate.setExperimentId(experiment);
        experimentUpdate.setProjectId(project1);

        // When
        v3api.updateExperiments(systemSessionToken, Arrays.asList(experimentUpdate));

        // Then
        assertEquals(getExperiment(experiment).getProject().getCode(), PROJECT_1);
    }

    @DataProvider(name = "liquidExperimentProjectProjectRelations")
    public static Object[][] liquidExperimentProjectProjectRelations()
    {
        List<FrozenFlags> combinationsForExperiment = new FrozenFlags(true).createAllCombinations();
        List<FrozenFlags> combinationsForProject = new FrozenFlags(true).freezeForSample().createAllCombinations();
        combinationsForProject.add(new FrozenFlags(false).freezeForExperiment());
        return asCartesianProduct(combinationsForExperiment, combinationsForProject, combinationsForProject);
    }

    @Test(dataProvider = "frozenExperimentProjectProjectRelations")
    public void testInvalidMoveExperimentFromProjectToProject(FrozenFlags frozenFlagsForExperiment,
            FrozenFlags frozenFlagsForOldProject, FrozenFlags frozenFlagsForNewProject)
    {
        // Given
        setFrozenFlagsForExperiments(frozenFlagsForExperiment, experiment);
        setFrozenFlagsForProjects(frozenFlagsForOldProject, projectOfExperiment);
        setFrozenFlagsForProjects(frozenFlagsForNewProject, project1);
        assertEquals(getExperiment(experiment).getProject().getCode(), PROJECT_OF_EXP);
        ExperimentUpdate experimentUpdate = new ExperimentUpdate();
        experimentUpdate.setExperimentId(experiment);
        experimentUpdate.setProjectId(project1);
        boolean frozen = frozenFlagsForNewProject.isFrozen() && frozenFlagsForNewProject.isFrozenForExperiment();
        String type = frozen ? "SET" : "REMOVE";
        String projectCode = frozen ? PROJECT_1 : PROJECT_OF_EXP;

        // When
        assertUserFailureException(Void -> v3api.updateExperiments(systemSessionToken, Arrays.asList(experimentUpdate)),
                // Then
                "ERROR: Operation " + type + " PROJECT is not allowed because project " + projectCode + " is frozen for experiment "
                        + EXP + ".");
    }

    @Test(dataProvider = "frozenExperimentProjectProjectRelations")
    public void testInvalidMoveExperimentFromProjectToProjectAfterMelting(FrozenFlags frozenFlagsForExperiment,
            FrozenFlags frozenFlagsForOldProject, FrozenFlags frozenFlagsForNewProject)
    {
        // Given
        setFrozenFlagsForExperiments(frozenFlagsForExperiment, experiment);
        setFrozenFlagsForProjects(frozenFlagsForOldProject, projectOfExperiment);
        setFrozenFlagsForProjects(frozenFlagsForNewProject, project1);
        setFrozenFlagsForProjects(frozenFlagsForOldProject.clone().melt(), projectOfExperiment);
        setFrozenFlagsForProjects(frozenFlagsForNewProject.clone().melt(), project1);
        assertEquals(getExperiment(experiment).getProject().getCode(), PROJECT_OF_EXP);
        ExperimentUpdate experimentUpdate = new ExperimentUpdate();
        experimentUpdate.setExperimentId(experiment);
        experimentUpdate.setProjectId(project1);

        // When
        v3api.updateExperiments(systemSessionToken, Arrays.asList(experimentUpdate));

        // Then
        assertEquals(getExperiment(experiment).getProject().getCode(), PROJECT_1);
    }

    @DataProvider(name = "frozenExperimentProjectProjectRelations")
    public static Object[][] frozenExperimentProjectProjectRelations()
    {
        List<FrozenFlags> combinationsForExperiment = new FrozenFlags(true).createAllCombinations();
        List<FrozenFlags> combinationsForLiquidProject = new FrozenFlags(true).freezeForSample().createAllCombinations();
        combinationsForLiquidProject.add(new FrozenFlags(false).freezeForExperiment());
        List<FrozenFlags> combinationsForFrozenProject = Arrays.asList(new FrozenFlags(true).freezeForExperiment());
        return merge(asCartesianProduct(combinationsForExperiment, combinationsForLiquidProject, combinationsForFrozenProject),
                asCartesianProduct(combinationsForExperiment, combinationsForFrozenProject, combinationsForLiquidProject),
                asCartesianProduct(combinationsForExperiment, combinationsForFrozenProject, combinationsForFrozenProject));
    }

    @Test(dataProvider = "liquidExperimentProjectRelations")
    public void testValidAddExperimentToProject(FrozenFlags frozenFlagsForProject)
    {
        // Given
        setFrozenFlagsForProjects(frozenFlagsForProject, project1);
        ExperimentCreation experimentCreation = experiment(project1, PREFIX + "E2");

        // When
        ExperimentPermId id = v3api.createExperiments(systemSessionToken, Arrays.asList(experimentCreation)).iterator().next();

        // Then
        assertEquals(getExperiment(id).getProject().getCode(), PROJECT_1);
    }

    @DataProvider(name = "liquidExperimentProjectRelations")
    public static Object[][] liquidExperimentProjectRelations()
    {
        List<FrozenFlags> combinationsForLiquidProject = new FrozenFlags(true).freezeForSample().createAllCombinations();
        combinationsForLiquidProject.add(new FrozenFlags(false).freezeForExperiment());
        return asCartesianProduct(combinationsForLiquidProject);
    }

    @Test
    public void testInvalidAddExperimentToProject()
    {
        // Given
        setFrozenFlagsForProjects(new FrozenFlags(true).freezeForExperiment(), project1);
        ExperimentCreation experimentCreation = experiment(project1, PREFIX + "E2");

        // When
        assertUserFailureException(Void -> v3api.createExperiments(systemSessionToken, Arrays.asList(experimentCreation)),
                // Then
                "ERROR: Operation SET PROJECT is not allowed because project " + PROJECT_1 + " is frozen for experiment "
                        + experimentCreation.getCode() + ".");
    }

    @Test
    public void testInvalidAddExperimentToProjectAfterMelting()
    {
        // Given
        setFrozenFlagsForProjects(new FrozenFlags(true).freezeForExperiment(), project1);
        setFrozenFlagsForProjects(new FrozenFlags(true).freezeForExperiment().melt(), project1);
        ExperimentCreation experimentCreation = experiment(project1, PREFIX + "E2");

        // When
        ExperimentPermId id = v3api.createExperiments(systemSessionToken, Arrays.asList(experimentCreation)).iterator().next();

        // Then
        assertEquals(getExperiment(id).getProject().getCode(), PROJECT_1);
    }

    @Test
    public void testFreezeProjectAndExperiment()
    {
        // Given
        ProjectUpdate projectUpdate = new ProjectUpdate();
        projectUpdate.setProjectId(projectOfExperiment);
        projectUpdate.freeze();
        projectUpdate.freezeForExperiments();
        ExperimentUpdate experimentUpdate = new ExperimentUpdate();
        experimentUpdate.setExperimentId(experiment);
        experimentUpdate.freeze();
        IOperationExecutionOptions op = new SynchronousOperationExecutionOptions();

        // When
        v3api.executeOperations(systemSessionToken, Arrays.asList(
                new UpdateProjectsOperation(projectUpdate), new UpdateExperimentsOperation(experimentUpdate)), op);

        // Then
        assertEquals(getExperiment(experiment).isFrozen(), true);
        assertEquals(getExperiment(experiment).getProject().isFrozen(), true);
    }
}
