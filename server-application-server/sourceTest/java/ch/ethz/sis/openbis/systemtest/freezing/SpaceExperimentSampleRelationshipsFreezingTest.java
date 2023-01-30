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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.UpdateExperimentsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.IOperationExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.SynchronousOperationExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.UpdateSamplesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.update.SpaceUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.update.UpdateSpacesOperation;

/**
 * @author Franz-Josef Elmer
 */
public class SpaceExperimentSampleRelationshipsFreezingTest extends FreezingTest
{
    private static final String PREFIX = "SESRFT-";

    private static final String SPACE_1 = PREFIX + "S1";

    private static final String EXPERIMENT_1 = PREFIX + "E1";

    private static final String SPACE_OF_SAMPLE = PREFIX + "SPACE-SAMP";

    private static final String EXP_OF_SAMPLE = PREFIX + "EXP-SAMP";

    private static final String SHARED_SAMPLE = PREFIX + "SAMP-SHARED";

    private static final String SPACE_SAMPLE = PREFIX + "SAMP-SPACE";

    private static final String EXPE_SAMPLE = PREFIX + "SAMP-EXPE";

    private SpacePermId space1;

    private SpacePermId spaceOfSample;

    private ExperimentPermId experiment1;

    private ExperimentPermId expOfSample;

    private SamplePermId sharedSample;

    private SamplePermId spaceSample;

    private SamplePermId experimentSample;

    @BeforeMethod
    public void createExamples()
    {
        List<SpacePermId> spaces = v3api.createSpaces(systemSessionToken, Arrays.asList(space(SPACE_1), space(SPACE_OF_SAMPLE)));
        space1 = spaces.get(0);
        spaceOfSample = spaces.get(1);

        List<ProjectPermId> projects = v3api.createProjects(systemSessionToken, Arrays.asList(project(spaceOfSample, "P")));
        ProjectPermId p = projects.get(0);

        List<ExperimentPermId> experiments = v3api.createExperiments(systemSessionToken,
                Arrays.asList(experiment(p, EXPERIMENT_1), experiment(p, EXP_OF_SAMPLE)));
        experiment1 = experiments.get(0);
        expOfSample = experiments.get(1);

        SampleCreation p1 = cellPlate(SHARED_SAMPLE);
        p1.setSpaceId(null);
        SampleCreation p2 = cellPlate(SPACE_SAMPLE);
        p2.setSpaceId(spaceOfSample);
        SampleCreation p3 = cellPlate(EXPE_SAMPLE);
        p3.setSpaceId(spaceOfSample);
        p3.setExperimentId(expOfSample);
        List<SamplePermId> samples = v3api.createSamples(systemSessionToken, Arrays.asList(p1, p2, p3));

        sharedSample = samples.get(0);
        spaceSample = samples.get(1);
        experimentSample = samples.get(2);
    }

    @Test(dataProvider = "liquidSampleSpaceRelations")
    public void testValidAddSharedSampleToSpace(FrozenFlags frozenFlagsOfSharedSample, FrozenFlags frozenFlagsOfSpace)
    {
        // Given
        setFrozenFlagsForSamples(frozenFlagsOfSharedSample, sharedSample);
        setFrozenFlagsForSpaces(frozenFlagsOfSpace, space1);
        assertEquals(getSample(sharedSample).getSpace(), null);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sharedSample);
        sampleUpdate.setSpaceId(space1);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(sharedSample).getSpace().getCode(), SPACE_1);
    }

    @Test(dataProvider = "liquidSampleSpaceRelations")
    public void testValidRemoveSampleFromSpace(FrozenFlags frozenFlagsOfSample, FrozenFlags frozenFlagsOfSpace)
    {
        // Given
        setFrozenFlagsForSamples(frozenFlagsOfSample, spaceSample);
        setFrozenFlagsForSpaces(frozenFlagsOfSpace, spaceOfSample);
        assertEquals(getSample(spaceSample).getSpace().getCode(), SPACE_OF_SAMPLE);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(spaceSample);
        sampleUpdate.setSpaceId(null);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(spaceSample).getSpace(), null);
    }

    @DataProvider(name = "liquidSampleSpaceRelations")
    public static Object[][] liquidSampleSpaceRelations()
    {
        List<FrozenFlags> combinationsForSample = new FrozenFlags(true).createAllCombinations();
        List<FrozenFlags> combinationsForSpace = new FrozenFlags(true).freezeForProject().createAllCombinations();
        combinationsForSpace.add(new FrozenFlags(false).freezeForSample());
        return asCartesianProduct(combinationsForSample, combinationsForSpace);
    }

    @Test(dataProvider = "liquidSampleSpaceSpaceRelations")
    public void testValidMoveSpaceSampleToSpace(FrozenFlags frozenFlagsOfSample, FrozenFlags frozenFlagsOfOldSpace,
            FrozenFlags frozenFlagsOfNewSpace)
    {
        // Given
        setFrozenFlagsForSamples(frozenFlagsOfSample, spaceSample);
        setFrozenFlagsForSpaces(frozenFlagsOfOldSpace, spaceOfSample);
        setFrozenFlagsForSpaces(frozenFlagsOfNewSpace, space1);
        assertEquals(getSample(spaceSample).getSpace().getCode(), SPACE_OF_SAMPLE);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(spaceSample);
        sampleUpdate.setSpaceId(space1);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(spaceSample).getSpace().getCode(), SPACE_1);
    }

    @DataProvider(name = "liquidSampleSpaceSpaceRelations")
    public static Object[][] liquidSampleSpaceSpaceRelations()
    {
        List<FrozenFlags> combinationsForSample = new FrozenFlags(true).createAllCombinations();
        List<FrozenFlags> combinationsForSpace = new FrozenFlags(true).freezeForProject().createAllCombinations();
        combinationsForSpace.add(new FrozenFlags(false).freezeForSample());
        return asCartesianProduct(combinationsForSample, combinationsForSpace, combinationsForSpace);
    }

    @Test(dataProvider = "frozenSampleSpaceRelations")
    public void testInvalidAddSharedSampleToSpace(FrozenFlags frozenFlagsOfSharedSample, FrozenFlags frozenFlagsOfSpace)
    {
        // Given
        setFrozenFlagsForSamples(frozenFlagsOfSharedSample, sharedSample);
        setFrozenFlagsForSpaces(frozenFlagsOfSpace, space1);
        assertEquals(getSample(sharedSample).getSpace(), null);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sharedSample);
        sampleUpdate.setSpaceId(space1);

        // When
        assertUserFailureException(Void -> v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate)),
                // Then
                "ERROR: Operation SET SPACE is not allowed because space " + SPACE_1 + " is frozen for sample "
                        + SHARED_SAMPLE + ".");
    }

    @Test(dataProvider = "frozenSampleSpaceRelations")
    public void testInvalidAddSharedSampleToSpaceAfterMelting(FrozenFlags frozenFlagsOfSharedSample, FrozenFlags frozenFlagsOfSpace)
    {
        // Given
        setFrozenFlagsForSamples(frozenFlagsOfSharedSample, sharedSample);
        setFrozenFlagsForSpaces(frozenFlagsOfSpace, space1);
        setFrozenFlagsForSpaces(frozenFlagsOfSpace.clone().melt(), space1);
        assertEquals(getSample(sharedSample).getSpace(), null);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sharedSample);
        sampleUpdate.setSpaceId(space1);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(sharedSample).getSpace().getCode(), SPACE_1);
    }

    @Test(dataProvider = "frozenSampleSpaceRelations")
    public void testInvalidRemoveSampleFromSpace(FrozenFlags frozenFlagsOfSample, FrozenFlags frozenFlagsOfSpace)
    {
        // Given
        setFrozenFlagsForSamples(frozenFlagsOfSample, spaceSample);
        setFrozenFlagsForSpaces(frozenFlagsOfSpace, spaceOfSample);
        assertEquals(getSample(spaceSample).getSpace().getCode(), SPACE_OF_SAMPLE);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(spaceSample);
        sampleUpdate.setSpaceId(null);

        // When
        assertUserFailureException(Void -> v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate)),
                // Then
                "ERROR: Operation REMOVE SPACE is not allowed because space " + SPACE_OF_SAMPLE + " is frozen for sample "
                        + SPACE_SAMPLE + ".");
    }

    @Test(dataProvider = "frozenSampleSpaceRelations")
    public void testInvalidRemoveSampleFromSpaceAfterMelting(FrozenFlags frozenFlagsOfSample, FrozenFlags frozenFlagsOfSpace)
    {
        // Given
        setFrozenFlagsForSamples(frozenFlagsOfSample, spaceSample);
        setFrozenFlagsForSpaces(frozenFlagsOfSpace, spaceOfSample);
        setFrozenFlagsForSpaces(frozenFlagsOfSpace.clone().melt(), spaceOfSample);
        assertEquals(getSample(spaceSample).getSpace().getCode(), SPACE_OF_SAMPLE);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(spaceSample);
        sampleUpdate.setSpaceId(null);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(spaceSample).getSpace(), null);
    }

    @DataProvider(name = "frozenSampleSpaceRelations")
    public static Object[][] frozenSampleSpaceRelations()
    {
        List<FrozenFlags> combinationsForSample = new FrozenFlags(true).createAllCombinations();
        return asCartesianProduct(combinationsForSample, Arrays.asList(new FrozenFlags(true).freezeForSample()));
    }

    @Test(dataProvider = "frozenSampleSpaceSpaceRelations")
    public void testInvalidMoveSpaceSampleToSpace(FrozenFlags frozenFlagsOfSample, FrozenFlags frozenFlagsOfOldSpace,
            FrozenFlags frozenFlagsOfNewSpace)
    {
        // Given
        setFrozenFlagsForSamples(frozenFlagsOfSample, spaceSample);
        setFrozenFlagsForSpaces(frozenFlagsOfOldSpace, spaceOfSample);
        setFrozenFlagsForSpaces(frozenFlagsOfNewSpace, space1);
        assertEquals(getSample(spaceSample).getSpace().getCode(), SPACE_OF_SAMPLE);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(spaceSample);
        sampleUpdate.setSpaceId(space1);
        boolean frozen = frozenFlagsOfNewSpace.isFrozen() && frozenFlagsOfNewSpace.isFrozenForSample();
        String type = frozen ? "SET" : "REMOVE";
        String space = frozen ? SPACE_1 : SPACE_OF_SAMPLE;

        // When
        assertUserFailureException(Void -> v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate)),
                // Then
                "ERROR: Operation " + type + " SPACE is not allowed because space " + space + " is frozen for sample "
                        + SPACE_SAMPLE + ".");
    }

    @Test(dataProvider = "frozenSampleSpaceSpaceRelations")
    public void testInvalidMoveSpaceSampleToSpaceAfterMelting(FrozenFlags frozenFlagsOfSample, FrozenFlags frozenFlagsOfOldSpace,
            FrozenFlags frozenFlagsOfNewSpace)
    {
        // Given
        setFrozenFlagsForSamples(frozenFlagsOfSample, spaceSample);
        setFrozenFlagsForSpaces(frozenFlagsOfOldSpace, spaceOfSample);
        setFrozenFlagsForSpaces(frozenFlagsOfNewSpace, space1);
        setFrozenFlagsForSpaces(frozenFlagsOfOldSpace.clone().melt(), spaceOfSample);
        setFrozenFlagsForSpaces(frozenFlagsOfNewSpace.clone().melt(), space1);
        assertEquals(getSample(spaceSample).getSpace().getCode(), SPACE_OF_SAMPLE);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(spaceSample);
        sampleUpdate.setSpaceId(space1);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(spaceSample).getSpace().getCode(), SPACE_1);
    }

    @DataProvider(name = "frozenSampleSpaceSpaceRelations")
    public static Object[][] frozenSampleSpaceSpaceRelations()
    {
        List<FrozenFlags> combinationsForSample = new FrozenFlags(true).createAllCombinations();
        List<FrozenFlags> combinationsForFrozenSpace = Arrays.asList(new FrozenFlags(true).freezeForSample());
        List<FrozenFlags> combinationsForLiquidSpace = new FrozenFlags(true).freezeForProject().createAllCombinations();
        combinationsForLiquidSpace.add(new FrozenFlags(false).freezeForSample());
        return merge(asCartesianProduct(combinationsForSample, combinationsForFrozenSpace, combinationsForLiquidSpace),
                asCartesianProduct(combinationsForSample, combinationsForLiquidSpace, combinationsForFrozenSpace),
                asCartesianProduct(combinationsForSample, combinationsForFrozenSpace, combinationsForFrozenSpace));
    }

    @Test(dataProvider = "liquidSampleExperimentRelations")
    public void testValidAddSpaceSampleToExperiment(FrozenFlags frozenFlagsOfSample, FrozenFlags frozenFlagsOfExperiment)
    {
        // Given
        setFrozenFlagsForSamples(frozenFlagsOfSample, spaceSample);
        setFrozenFlagForSpaces(true, spaceOfSample);
        setFrozenFlagsForExperiments(frozenFlagsOfExperiment, experiment1);
        assertEquals(getSample(spaceSample).getExperiment(), null);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(spaceSample);
        sampleUpdate.setExperimentId(experiment1);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(spaceSample).getExperiment().getCode(), EXPERIMENT_1);
    }

    @Test(dataProvider = "liquidSampleExperimentRelations")
    public void testValidRemoveSampleFromExperiment(FrozenFlags frozenFlagsOfSample, FrozenFlags frozenFlagsOfExperiment)
    {
        // Given
        setFrozenFlagsForSamples(frozenFlagsOfSample, experimentSample);
        setFrozenFlagsForExperiments(frozenFlagsOfExperiment, expOfSample);
        assertEquals(getSample(experimentSample).getExperiment().getCode(), EXP_OF_SAMPLE);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(experimentSample);
        sampleUpdate.setExperimentId(null);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(experimentSample).getExperiment(), null);
    }

    @DataProvider(name = "liquidSampleExperimentRelations")
    public static Object[][] liquidSampleExperimentRelations()
    {
        List<FrozenFlags> combinationsForSample = new FrozenFlags(true).createAllCombinations();
        List<FrozenFlags> combinationsForExperiment = new FrozenFlags(true).freezeForDataSet().createAllCombinations();
        combinationsForExperiment.add(new FrozenFlags(false).freezeForDataSet());
        return asCartesianProduct(combinationsForSample, combinationsForExperiment);
    }

    @Test(dataProvider = "liquidSampleExperimentExperimentRelations")
    public void testValidMoveExperimentSampleToExperiment(FrozenFlags frozenFlagsOfSample,
            FrozenFlags frozenFlagsOfOldExperiment, FrozenFlags frozenFlagsOfNewExperiment)
    {
        // Given
        setFrozenFlagsForSamples(frozenFlagsOfSample, experimentSample);
        setFrozenFlagsForExperiments(frozenFlagsOfOldExperiment, expOfSample);
        setFrozenFlagsForExperiments(frozenFlagsOfNewExperiment, experiment1);
        assertEquals(getSample(experimentSample).getExperiment().getCode(), EXP_OF_SAMPLE);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(experimentSample);
        sampleUpdate.setExperimentId(experiment1);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(experimentSample).getExperiment().getCode(), EXPERIMENT_1);
    }

    @DataProvider(name = "liquidSampleExperimentExperimentRelations")
    public static Object[][] liquidSampleExperimentExperimentRelations()
    {
        List<FrozenFlags> combinationsForSample = new FrozenFlags(true).createAllCombinations();
        List<FrozenFlags> combinationsForExperiment = new FrozenFlags(true).freezeForDataSet().createAllCombinations();
        combinationsForExperiment.add(new FrozenFlags(false).freezeForSample());
        return asCartesianProduct(combinationsForSample, combinationsForExperiment, combinationsForExperiment);
    }

    @Test(dataProvider = "frozenSampleExperimentRelations")
    public void testInvalidAddSharedSampleToExperiment(FrozenFlags frozenFlagsOfSample, FrozenFlags frozenFlagsOfExperiment)
    {
        // Given
        setFrozenFlagsForSamples(frozenFlagsOfSample, spaceSample);
        setFrozenFlagForSpaces(true, spaceOfSample);
        setFrozenFlagsForExperiments(frozenFlagsOfExperiment, experiment1);
        assertEquals(getSample(spaceSample).getExperiment(), null);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(spaceSample);
        sampleUpdate.setExperimentId(experiment1);

        // When
        assertUserFailureException(Void -> v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate)),
                // Then
                "ERROR: Operation SET EXPERIMENT is not allowed because experiment " + EXPERIMENT_1 + " is frozen for sample "
                        + SPACE_SAMPLE + ".");
    }

    @Test(dataProvider = "frozenSampleExperimentRelations")
    public void testInvalidAddSharedSampleToExperimentAfterMelting(FrozenFlags frozenFlagsOfSample, FrozenFlags frozenFlagsOfExperiment)
    {
        // Given
        setFrozenFlagsForSamples(frozenFlagsOfSample, spaceSample);
        setFrozenFlagForSpaces(true, spaceOfSample);
        setFrozenFlagsForExperiments(frozenFlagsOfExperiment, experiment1);
        setFrozenFlagsForExperiments(frozenFlagsOfExperiment.clone().melt(), experiment1);
        assertEquals(getSample(spaceSample).getExperiment(), null);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(spaceSample);
        sampleUpdate.setExperimentId(experiment1);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(spaceSample).getExperiment().getCode(), EXPERIMENT_1);
    }

    @Test(dataProvider = "frozenSampleExperimentRelations")
    public void testInvalidRemoveSampleFromExperiment(FrozenFlags frozenFlagsOfSample, FrozenFlags frozenFlagsOfExperiment)
    {
        // Given
        setFrozenFlagsForSamples(frozenFlagsOfSample, experimentSample);
        setFrozenFlagsForExperiments(frozenFlagsOfExperiment, expOfSample);
        assertEquals(getSample(experimentSample).getExperiment().getCode(), EXP_OF_SAMPLE);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(experimentSample);
        sampleUpdate.setExperimentId(null);

        // When
        assertUserFailureException(Void -> v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate)),
                // Then
                "ERROR: Operation REMOVE EXPERIMENT is not allowed because experiment " + EXP_OF_SAMPLE + " is frozen for sample "
                        + EXPE_SAMPLE + ".");
    }

    @Test(dataProvider = "frozenSampleExperimentRelations")
    public void testInvalidRemoveSampleFromExperimentAfterMelting(FrozenFlags frozenFlagsOfSample, FrozenFlags frozenFlagsOfExperiment)
    {
        // Given
        setFrozenFlagsForSamples(frozenFlagsOfSample, experimentSample);
        setFrozenFlagsForExperiments(frozenFlagsOfExperiment, expOfSample);
        setFrozenFlagsForExperiments(frozenFlagsOfExperiment.clone().melt(), expOfSample);
        assertEquals(getSample(experimentSample).getExperiment().getCode(), EXP_OF_SAMPLE);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(experimentSample);
        sampleUpdate.setExperimentId(null);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(experimentSample).getExperiment(), null);
    }

    @DataProvider(name = "frozenSampleExperimentRelations")
    public static Object[][] frozenSampleExperimentRelations()
    {
        List<FrozenFlags> combinationsForSample = new FrozenFlags(true).createAllCombinations();
        return asCartesianProduct(combinationsForSample, Arrays.asList(new FrozenFlags(true).freezeForSample()));
    }

    @Test(dataProvider = "frozenSampleExperimentExperimentRelations")
    public void testInvalidMoveExperimentSampleToExperiment(FrozenFlags frozenFlagsOfSample, FrozenFlags frozenFlagsOfOldExperiment,
            FrozenFlags frozenFlagsOfNewExperiment)
    {
        // Given
        setFrozenFlagsForSamples(frozenFlagsOfSample, experimentSample);
        setFrozenFlagsForExperiments(frozenFlagsOfOldExperiment, expOfSample);
        setFrozenFlagsForExperiments(frozenFlagsOfNewExperiment, experiment1);
        assertEquals(getSample(experimentSample).getExperiment().getCode(), EXP_OF_SAMPLE);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(experimentSample);
        sampleUpdate.setExperimentId(experiment1);
        boolean frozen = frozenFlagsOfNewExperiment.isFrozen() && frozenFlagsOfNewExperiment.isFrozenForSample();
        String type = frozen ? "SET" : "REMOVE";
        String space = frozen ? EXPERIMENT_1 : EXP_OF_SAMPLE;

        // When
        assertUserFailureException(Void -> v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate)),
                // Then
                "ERROR: Operation " + type + " EXPERIMENT is not allowed because experiment " + space + " is frozen for sample "
                        + EXPE_SAMPLE + ".");
    }

    @Test(dataProvider = "frozenSampleExperimentExperimentRelations")
    public void testInvalidMoveExperimentSampleToExperimentAfterMelting(FrozenFlags frozenFlagsOfSample, FrozenFlags frozenFlagsOfOldExperiment,
            FrozenFlags frozenFlagsOfNewExperiment)
    {
        // Given
        setFrozenFlagsForSamples(frozenFlagsOfSample, experimentSample);
        setFrozenFlagsForExperiments(frozenFlagsOfOldExperiment, expOfSample);
        setFrozenFlagsForExperiments(frozenFlagsOfNewExperiment, experiment1);
        setFrozenFlagsForExperiments(frozenFlagsOfOldExperiment.clone().melt(), expOfSample);
        setFrozenFlagsForExperiments(frozenFlagsOfNewExperiment.clone().melt(), experiment1);
        assertEquals(getSample(experimentSample).getExperiment().getCode(), EXP_OF_SAMPLE);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(experimentSample);
        sampleUpdate.setExperimentId(experiment1);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(experimentSample).getExperiment().getCode(), EXPERIMENT_1);
    }

    @DataProvider(name = "frozenSampleExperimentExperimentRelations")
    public static Object[][] frozenSampleExperimentExperimentRelations()
    {
        List<FrozenFlags> combinationsForSample = new FrozenFlags(true).createAllCombinations();
        List<FrozenFlags> combinationsForFrozenExperiment = Arrays.asList(new FrozenFlags(true).freezeForSample());
        List<FrozenFlags> combinationsForLiquidExperiment = new FrozenFlags(true).freezeForDataSet().createAllCombinations();
        combinationsForLiquidExperiment.add(new FrozenFlags(false).freezeForSample());
        return merge(asCartesianProduct(combinationsForSample, combinationsForFrozenExperiment, combinationsForLiquidExperiment),
                asCartesianProduct(combinationsForSample, combinationsForLiquidExperiment, combinationsForFrozenExperiment),
                asCartesianProduct(combinationsForSample, combinationsForFrozenExperiment, combinationsForFrozenExperiment));
    }

    @Test(dataProvider = "liquidSpace")
    public void testValidAddSampleToSpace(FrozenFlags frozenFlagsForSpace)
    {
        // Given
        setFrozenFlagsForSpaces(frozenFlagsForSpace, space1);
        SampleCreation sampleCreation = cellPlate(PREFIX + "S2");
        sampleCreation.setSpaceId(space1);

        // When
        SamplePermId id = v3api.createSamples(systemSessionToken, Arrays.asList(sampleCreation)).iterator().next();

        // Then
        assertEquals(getSample(id).getSpace().getCode(), SPACE_1);
    }

    @DataProvider(name = "liquidSpace")
    public static Object[][] liquidSpace()
    {
        List<FrozenFlags> combinationsForLiquidSpace = new FrozenFlags(true).freezeForProject().createAllCombinations();
        combinationsForLiquidSpace.add(new FrozenFlags(false).freezeForSample());
        return asCartesianProduct(combinationsForLiquidSpace);
    }

    @Test
    public void testInvalidAddSampleToSpace()
    {
        // Given
        setFrozenFlagsForSpaces(new FrozenFlags(true).freezeForSample(), space1);
        SampleCreation sampleCreation = cellPlate(PREFIX + "S2");
        sampleCreation.setSpaceId(space1);

        // When
        assertUserFailureException(Void -> v3api.createSamples(systemSessionToken, Arrays.asList(sampleCreation)),
                // Then
                "ERROR: Operation SET SPACE is not allowed because space " + SPACE_1 + " is frozen for sample "
                        + sampleCreation.getCode() + ".");
    }

    @Test
    public void testInvalidAddSampleToSpaceAfterMelting()
    {
        // Given
        setFrozenFlagsForSpaces(new FrozenFlags(true).freezeForSample(), space1);
        setFrozenFlagsForSpaces(new FrozenFlags(true).freezeForSample().melt(), space1);
        SampleCreation sampleCreation = cellPlate(PREFIX + "S2");
        sampleCreation.setSpaceId(space1);

        // When
        SamplePermId id = v3api.createSamples(systemSessionToken, Arrays.asList(sampleCreation)).iterator().next();

        // Then
        assertEquals(getSample(id).getSpace().getCode(), SPACE_1);
    }

    @Test(dataProvider = "liquidExperiment")
    public void testValidAddSampleToExperiment(FrozenFlags frozenFlagsForExperiment)
    {
        // Given
        setFrozenFlagsForExperiments(frozenFlagsForExperiment, experiment1);
        SampleCreation sampleCreation = cellPlate(PREFIX + "S2");
        sampleCreation.setSpaceId(spaceOfSample);
        sampleCreation.setExperimentId(experiment1);

        // When
        SamplePermId id = v3api.createSamples(systemSessionToken, Arrays.asList(sampleCreation)).iterator().next();

        // Then
        assertEquals(getSample(id).getExperiment().getCode(), EXPERIMENT_1);
    }

    @DataProvider(name = "liquidExperiment")
    public static Object[][] liquidExperiment()
    {
        List<FrozenFlags> combinationsForLiquidExperiment = new FrozenFlags(true).freezeForDataSet().createAllCombinations();
        combinationsForLiquidExperiment.add(new FrozenFlags(false).freezeForSample());
        return asCartesianProduct(combinationsForLiquidExperiment);
    }

    @Test
    public void testInvalidAddSampleToExperiment()
    {
        // Given
        setFrozenFlagsForExperiments(new FrozenFlags(true).freezeForSample(), experiment1);
        SampleCreation sampleCreation = cellPlate(PREFIX + "S2");
        sampleCreation.setSpaceId(spaceOfSample);
        sampleCreation.setExperimentId(experiment1);

        // When
        assertUserFailureException(Void -> v3api.createSamples(systemSessionToken, Arrays.asList(sampleCreation)),
                // Then
                "ERROR: Operation SET EXPERIMENT is not allowed because experiment " + EXPERIMENT_1 + " is frozen for sample "
                        + sampleCreation.getCode() + ".");
    }

    @Test
    public void testInvalidAddSampleToExperimentAfterMelting()
    {
        // Given
        setFrozenFlagsForExperiments(new FrozenFlags(true).freezeForSample(), experiment1);
        setFrozenFlagsForExperiments(new FrozenFlags(true).freezeForSample().melt(), experiment1);
        SampleCreation sampleCreation = cellPlate(PREFIX + "S2");
        sampleCreation.setSpaceId(spaceOfSample);
        sampleCreation.setExperimentId(experiment1);

        // When
        SamplePermId id = v3api.createSamples(systemSessionToken, Arrays.asList(sampleCreation)).iterator().next();

        // Then
        assertEquals(getSample(id).getExperiment().getCode(), EXPERIMENT_1);
    }

    @Test
    public void testFreezeSpaceAndSample()
    {
        // Given
        SpaceUpdate spaceUpdate = new SpaceUpdate();
        spaceUpdate.setSpaceId(spaceOfSample);
        spaceUpdate.freeze();
        spaceUpdate.freezeForSamples();
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(spaceSample);
        sampleUpdate.freeze();
        IOperationExecutionOptions op = new SynchronousOperationExecutionOptions();

        // When
        v3api.executeOperations(systemSessionToken, Arrays.asList(
                new UpdateSpacesOperation(spaceUpdate), new UpdateSamplesOperation(sampleUpdate)), op);

        // Then
        assertEquals(getSpace(spaceOfSample).isFrozen(), true);
        assertEquals(getSample(spaceSample).isFrozen(), true);
    }

    @Test
    public void testFreezeExperimentAndSample()
    {
        // Given
        ExperimentUpdate experimentUpdate = new ExperimentUpdate();
        experimentUpdate.setExperimentId(expOfSample);
        experimentUpdate.freeze();
        experimentUpdate.freezeForSamples();
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(experimentSample);
        sampleUpdate.freeze();
        IOperationExecutionOptions op = new SynchronousOperationExecutionOptions();

        // When
        v3api.executeOperations(systemSessionToken, Arrays.asList(
                new UpdateExperimentsOperation(experimentUpdate), new UpdateSamplesOperation(sampleUpdate)), op);

        // Then
        assertEquals(getExperiment(expOfSample).isFrozen(), true);
        assertEquals(getSample(experimentSample).isFrozen(), true);
    }
}
