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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.UpdateDataSetsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.UpdateExperimentsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.IOperationExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.SynchronousOperationExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.UpdateSamplesOperation;

/**
 * @author Franz-Josef Elmer
 */
public class ExperimentSampleDataSetRelationshipsFreezingTest extends FreezingTest
{
    private static final String PREFIX = "ESDSRFT-";

    private static final String SAMPLE_1 = PREFIX + "S1";

    private static final String EXPERIMENT_1 = PREFIX + "E1";

    private static final String EXP_OF_DATA_SET = PREFIX + "EXP-DS";

    private static final String SAMP_OF_DATA_SET = PREFIX + "SAMP-DS";

    private static final String DATA_SET_EXP = PREFIX + "DS-EXP";

    private static final String DATA_SET_SAMP = PREFIX + "DS-SAMP";

    private ExperimentPermId experiment1;

    private ExperimentPermId experimentOfDataSet;

    private SamplePermId sample1;

    private SamplePermId sampleOfDataSet;

    private DataSetPermId dataSetExp;

    private DataSetPermId dataSetSamp;

    @BeforeMethod
    public void createExamples()
    {
        List<ExperimentPermId> experiments = v3api.createExperiments(systemSessionToken,
                Arrays.asList(experiment(DEFAULT_PROJECT_ID, EXP_OF_DATA_SET),
                        experiment(DEFAULT_PROJECT_ID, EXPERIMENT_1)));
        experimentOfDataSet = experiments.get(0);
        experiment1 = experiments.get(1);
        SampleCreation s1 = cellPlate(SAMPLE_1);
        SampleCreation s2 = cellPlate(SAMP_OF_DATA_SET);
        List<SamplePermId> samples = v3api.createSamples(systemSessionToken, Arrays.asList(s1, s2));
        sample1 = samples.get(0);
        sampleOfDataSet = samples.get(1);

        DataSetCreation ds1 = physicalDataSet(DATA_SET_EXP);
        ds1.setExperimentId(experimentOfDataSet);
        DataSetCreation ds2 = physicalDataSet(DATA_SET_SAMP);
        ds2.setSampleId(sampleOfDataSet);
        List<DataSetPermId> dataSets = v3api.createDataSets(systemSessionToken, Arrays.asList(ds1, ds2));
        dataSetExp = dataSets.get(0);
        dataSetSamp = dataSets.get(1);
    }

    @Test(dataProvider = "liquidDataSetSampleSampleRelations")
    public void testValidMoveDataSetFromSampleToSample(FrozenFlags frozenFlagsForDataSet,
            FrozenFlags frozenFlagsForSampleOfDataSet, FrozenFlags frozenFlagsForNewSample)
    {
        // Given
        setFrozenFlagsForDataSets(frozenFlagsForDataSet, dataSetSamp);
        setFrozenFlagsForSamples(frozenFlagsForSampleOfDataSet, sampleOfDataSet);
        setFrozenFlagsForSamples(frozenFlagsForNewSample, sample1);
        assertEquals(getDataSet(dataSetSamp).getSample().getCode(), SAMP_OF_DATA_SET);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSetSamp);
        dataSetUpdate.setSampleId(sample1);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSetSamp).getSample().getCode(), SAMPLE_1);
    }

    @DataProvider(name = "liquidDataSetSampleSampleRelations")
    public static Object[][] liquidDataSetSampleSampleRelations()
    {
        List<FrozenFlags> combinationsDataSet = new FrozenFlags(true).createAllCombinations();
        List<FrozenFlags> combinationsSample =
                new FrozenFlags(true).freezeForChildren().freezeForParents().freezeForComponents().createAllCombinations();
        return asCartesianProduct(combinationsDataSet, combinationsSample, combinationsSample);
    }

    @Test(dataProvider = "frozenDataSetSampleSampleRelations")
    public void testInvalidMoveDataSetFromSampleToSample(FrozenFlags frozenFlagsForDataSet,
            FrozenFlags frozenFlagsForSampleOfDataSet, FrozenFlags frozenFlagsForNewSample)
    {
        // Given
        setFrozenFlagsForDataSets(frozenFlagsForDataSet, dataSetSamp);
        setFrozenFlagsForSamples(frozenFlagsForSampleOfDataSet, sampleOfDataSet);
        setFrozenFlagsForSamples(frozenFlagsForNewSample, sample1);
        assertEquals(getDataSet(dataSetSamp).getSample().getCode(), SAMP_OF_DATA_SET);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSetSamp);
        dataSetUpdate.setSampleId(sample1);
        String operation = frozenFlagsForNewSample.isFrozenForDataSet() ? "SET" : "REMOVE";
        String sampleOfErrorMessage = frozenFlagsForNewSample.isFrozenForDataSet() ? SAMPLE_1 : SAMP_OF_DATA_SET;

        // When
        assertUserFailureException(Void -> v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate)),
                // Then
                "ERROR: Operation " + operation + " SAMPLE is not allowed because sample " + sampleOfErrorMessage
                        + " is frozen for data set " + DATA_SET_SAMP + ".");
    }

    @Test(dataProvider = "frozenDataSetSampleSampleRelations")
    public void testInvalidMoveDataSetFromSampleToSampleAfterMelting(FrozenFlags frozenFlagsForDataSet,
            FrozenFlags frozenFlagsForSampleOfDataSet, FrozenFlags frozenFlagsForNewSample)
    {
        // Given
        setFrozenFlagsForDataSets(frozenFlagsForDataSet, dataSetSamp);
        setFrozenFlagsForSamples(frozenFlagsForSampleOfDataSet, sampleOfDataSet);
        setFrozenFlagsForSamples(frozenFlagsForNewSample, sample1);
        setFrozenFlagsForSamples(frozenFlagsForSampleOfDataSet.clone().melt(), sampleOfDataSet);
        setFrozenFlagsForSamples(frozenFlagsForNewSample.clone().melt(), sample1);
        assertEquals(getDataSet(dataSetSamp).getSample().getCode(), SAMP_OF_DATA_SET);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSetSamp);
        dataSetUpdate.setSampleId(sample1);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSetSamp).getSample().getCode(), SAMPLE_1);
    }

    @DataProvider(name = "frozenDataSetSampleSampleRelations")
    public static Object[][] frozenDataSetSampleSampleRelations()
    {
        List<FrozenFlags> combinationsDataSet = new FrozenFlags(true).createAllCombinations();
        List<FrozenFlags> combinationsLiquidSample =
                new FrozenFlags(true).freezeForChildren().freezeForParents().freezeForComponents().createAllCombinations();
        List<FrozenFlags> combinationsFrozenSample = Arrays.asList(new FrozenFlags(true).freezeForDataSet());
        return merge(asCartesianProduct(combinationsDataSet, combinationsFrozenSample, combinationsLiquidSample),
                asCartesianProduct(combinationsDataSet, combinationsLiquidSample, combinationsFrozenSample),
                asCartesianProduct(combinationsDataSet, combinationsFrozenSample, combinationsFrozenSample));
    }

    @Test(dataProvider = "liquidDataSetExperimentExperimentRelations")
    public void testValidMoveDataSetFromExperimentToExperiment(FrozenFlags frozenFlagsForDataSet,
            FrozenFlags frozenFlagsForExperimentOfDataSet, FrozenFlags frozenFlagsForNewExperiment)
    {
        // Given
        setFrozenFlagsForDataSets(frozenFlagsForDataSet, dataSetExp);
        setFrozenFlagsForExperiments(frozenFlagsForExperimentOfDataSet, experimentOfDataSet);
        setFrozenFlagsForExperiments(frozenFlagsForNewExperiment, experiment1);
        assertEquals(getDataSet(dataSetExp).getExperiment().getCode(), EXP_OF_DATA_SET);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSetExp);
        dataSetUpdate.setExperimentId(experiment1);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSetExp).getExperiment().getCode(), EXPERIMENT_1);
    }

    @DataProvider(name = "liquidDataSetExperimentExperimentRelations")
    public static Object[][] liquidDataSetExperimentExperimentRelations()
    {
        List<FrozenFlags> combinationsDataSet = new FrozenFlags(true).createAllCombinations();
        List<FrozenFlags> combinationsExperiment = new FrozenFlags(true).freezeForSample().createAllCombinations();
        return asCartesianProduct(combinationsDataSet, combinationsExperiment, combinationsExperiment);
    }

    @Test(dataProvider = "frozenDataSetExperimentExperimentRelations")
    public void testInvalidMoveDataSetFromExperimentToExperiment(FrozenFlags frozenFlagsForDataSet,
            FrozenFlags frozenFlagsForExperimentOfDataSet, FrozenFlags frozenFlagsForNewExperiment)
    {
        // Given
        setFrozenFlagsForDataSets(frozenFlagsForDataSet, dataSetExp);
        setFrozenFlagsForExperiments(frozenFlagsForExperimentOfDataSet, experimentOfDataSet);
        setFrozenFlagsForExperiments(frozenFlagsForNewExperiment, experiment1);
        assertEquals(getDataSet(dataSetExp).getExperiment().getCode(), EXP_OF_DATA_SET);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSetExp);
        dataSetUpdate.setExperimentId(experiment1);
        String operation = frozenFlagsForNewExperiment.isFrozenForDataSet() ? "SET" : "REMOVE";
        String experimentOfErrorMessage = frozenFlagsForNewExperiment.isFrozenForDataSet() ? EXPERIMENT_1 : EXP_OF_DATA_SET;

        // When
        assertUserFailureException(Void -> v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate)),
                // Then
                "ERROR: Operation " + operation + " EXPERIMENT is not allowed because experiment " + experimentOfErrorMessage
                        + " is frozen for data set " + DATA_SET_EXP + ".");
    }

    @Test(dataProvider = "frozenDataSetExperimentExperimentRelations")
    public void testInvalidMoveDataSetFromExperimentToExperimentAfterMelting(FrozenFlags frozenFlagsForDataSet,
            FrozenFlags frozenFlagsForExperimentOfDataSet, FrozenFlags frozenFlagsForNewExperiment)
    {
        // Given
        setFrozenFlagsForDataSets(frozenFlagsForDataSet, dataSetExp);
        setFrozenFlagsForExperiments(frozenFlagsForExperimentOfDataSet, experimentOfDataSet);
        setFrozenFlagsForExperiments(frozenFlagsForNewExperiment, experiment1);
        setFrozenFlagsForExperiments(frozenFlagsForExperimentOfDataSet.clone().melt(), experimentOfDataSet);
        setFrozenFlagsForExperiments(frozenFlagsForNewExperiment.clone().melt(), experiment1);
        assertEquals(getDataSet(dataSetExp).getExperiment().getCode(), EXP_OF_DATA_SET);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSetExp);
        dataSetUpdate.setExperimentId(experiment1);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSetExp).getExperiment().getCode(), EXPERIMENT_1);
    }

    @DataProvider(name = "frozenDataSetExperimentExperimentRelations")
    public static Object[][] frozenDataSetExperimentExperimentRelations()
    {
        List<FrozenFlags> combinationsDataSet = new FrozenFlags(true).createAllCombinations();
        List<FrozenFlags> combinationsLiquidExperiment =
                new FrozenFlags(true).freezeForSample().freezeForComponents().createAllCombinations();
        List<FrozenFlags> combinationsFrozenExperiment = Arrays.asList(new FrozenFlags(true).freezeForDataSet());
        return merge(asCartesianProduct(combinationsDataSet, combinationsFrozenExperiment, combinationsLiquidExperiment),
                asCartesianProduct(combinationsDataSet, combinationsLiquidExperiment, combinationsFrozenExperiment),
                asCartesianProduct(combinationsDataSet, combinationsFrozenExperiment, combinationsFrozenExperiment));
    }

    @Test(dataProvider = "liquidDataSetExperimentSampleRelations")
    public void testValidMoveDataSetFromExperimentToSample(FrozenFlags frozenFlagsForDataSet,
            FrozenFlags frozenFlagsForExperimentOfDataSet, FrozenFlags frozenFlagsForNewSample)
    {
        // Given
        setFrozenFlagsForDataSets(frozenFlagsForDataSet, dataSetExp);
        setFrozenFlagsForExperiments(frozenFlagsForExperimentOfDataSet, experimentOfDataSet);
        setFrozenFlagsForSamples(frozenFlagsForNewSample, sample1);
        assertEquals(getDataSet(dataSetExp).getExperiment().getCode(), EXP_OF_DATA_SET);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSetExp);
        dataSetUpdate.setSampleId(sample1);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        DataSet dataSet = getDataSet(dataSetExp);
        assertEquals(dataSet.getExperiment(), null);
        assertEquals(dataSet.getSample().getCode(), SAMPLE_1);
    }

    @DataProvider(name = "liquidDataSetExperimentSampleRelations")
    public static Object[][] liquidDataSetExperimentSampleRelations()
    {
        List<FrozenFlags> combinationsDataSet = new FrozenFlags(true).createAllCombinations();
        List<FrozenFlags> combinationsExperiment = new FrozenFlags(true).freezeForSample().createAllCombinations();
        List<FrozenFlags> combinationsSample =
                new FrozenFlags(true).freezeForChildren().freezeForParents().freezeForComponents().createAllCombinations();
        return asCartesianProduct(combinationsDataSet, combinationsExperiment, combinationsSample);
    }

    @Test(dataProvider = "frozenDataSetExperimentSampleRelations")
    public void testInvalidMoveDataSetFromExperimentToSample(FrozenFlags frozenFlagsForDataSet,
            FrozenFlags frozenFlagsForExperimentOfDataSet, FrozenFlags frozenFlagsForNewSample)
    {
        // Given
        setFrozenFlagsForDataSets(frozenFlagsForDataSet, dataSetExp);
        setFrozenFlagsForExperiments(frozenFlagsForExperimentOfDataSet, experimentOfDataSet);
        setFrozenFlagsForSamples(frozenFlagsForNewSample, sample1);
        assertEquals(getDataSet(dataSetExp).getExperiment().getCode(), EXP_OF_DATA_SET);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSetExp);
        dataSetUpdate.setSampleId(sample1);
        String operation = frozenFlagsForExperimentOfDataSet.isFrozenForDataSet() ? "REMOVE EXPERIMENT" : "SET SAMPLE";
        String type = frozenFlagsForExperimentOfDataSet.isFrozenForDataSet() ? "experiment" : "sample";
        String code = frozenFlagsForExperimentOfDataSet.isFrozenForDataSet() ? EXP_OF_DATA_SET : SAMPLE_1;

        // When
        assertUserFailureException(Void -> v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate)),
                // Then
                "ERROR: Operation " + operation + " is not allowed because " + type + " " + code
                        + " is frozen for data set " + DATA_SET_EXP + ".");
    }

    @Test(dataProvider = "frozenDataSetExperimentSampleRelations")
    public void testInvalidMoveDataSetFromExperimentToSampleAfterMelting(FrozenFlags frozenFlagsForDataSet,
            FrozenFlags frozenFlagsForExperimentOfDataSet, FrozenFlags frozenFlagsForNewSample)
    {
        // Given
        setFrozenFlagsForDataSets(frozenFlagsForDataSet, dataSetExp);
        setFrozenFlagsForExperiments(frozenFlagsForExperimentOfDataSet, experimentOfDataSet);
        setFrozenFlagsForSamples(frozenFlagsForNewSample, sample1);
        setFrozenFlagsForExperiments(frozenFlagsForExperimentOfDataSet.clone().melt(), experimentOfDataSet);
        setFrozenFlagsForSamples(frozenFlagsForNewSample.clone().melt(), sample1);
        assertEquals(getDataSet(dataSetExp).getExperiment().getCode(), EXP_OF_DATA_SET);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSetExp);
        dataSetUpdate.setSampleId(sample1);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        DataSet dataSet = getDataSet(dataSetExp);
        assertEquals(dataSet.getExperiment(), null);
        assertEquals(dataSet.getSample().getCode(), SAMPLE_1);
    }

    @DataProvider(name = "frozenDataSetExperimentSampleRelations")
    public static Object[][] frozenDataSetExperimentSampleRelations()
    {
        List<FrozenFlags> combinationsDataSet = new FrozenFlags(true).createAllCombinations();
        List<FrozenFlags> combinationsLiquidExperiment =
                new FrozenFlags(true).freezeForSample().freezeForComponents().createAllCombinations();
        List<FrozenFlags> combinationsFrozenExperiment = Arrays.asList(new FrozenFlags(true).freezeForDataSet());
        List<FrozenFlags> combinationsLiquidSample =
                new FrozenFlags(true).freezeForChildren().freezeForParents().freezeForComponents().createAllCombinations();
        List<FrozenFlags> combinationsFrozenSample = Arrays.asList(new FrozenFlags(true).freezeForDataSet());

        return merge(asCartesianProduct(combinationsDataSet, combinationsFrozenExperiment, combinationsLiquidSample),
                asCartesianProduct(combinationsDataSet, combinationsLiquidExperiment, combinationsFrozenSample),
                asCartesianProduct(combinationsDataSet, combinationsFrozenExperiment, combinationsFrozenSample));
    }

    @Test(dataProvider = "liquidSample")
    public void testValidAddDataSetToSample(FrozenFlags frozenFlagsForSample)
    {
        // Given
        setFrozenFlagsForSamples(frozenFlagsForSample, sample1);
        DataSetCreation dataSetCreation = physicalDataSet(PREFIX + "D2");
        dataSetCreation.setSampleId(sample1);

        // When
        DataSetPermId id = v3api.createDataSets(systemSessionToken, Arrays.asList(dataSetCreation)).iterator().next();

        // Then
        assertEquals(getDataSet(id).getSample().getCode(), SAMPLE_1);
    }

    @DataProvider(name = "liquidSample")
    public static Object[][] liquidSample()
    {
        List<FrozenFlags> combinationsForLiquidSample = new FrozenFlags(true).freezeForComponents().freezeForChildren()
                .freezeForParents().createAllCombinations();
        combinationsForLiquidSample.add(new FrozenFlags(false).freezeForDataSet());
        return asCartesianProduct(combinationsForLiquidSample);
    }

    @Test
    public void testInvalidAddDataSetToSample()
    {
        // Given
        setFrozenFlagsForSamples(new FrozenFlags(true).freezeForDataSet(), sample1);
        DataSetCreation dataSetCreation = physicalDataSet(PREFIX + "D2");
        dataSetCreation.setSampleId(sample1);

        // When
        assertUserFailureException(Void -> v3api.createDataSets(systemSessionToken, Arrays.asList(dataSetCreation)),
                // Then
                "ERROR: Operation SET SAMPLE is not allowed because sample " + SAMPLE_1 + " is frozen for data set "
                        + dataSetCreation.getCode() + ".");
    }

    @Test
    public void testInvalidAddDataSetToSampleAfterMelting()
    {
        // Given
        FrozenFlags frozenFlags = new FrozenFlags(true).freezeForComponents().freezeForChildren().freezeForParents();
        setFrozenFlagsForSamples(frozenFlags, sample1);
        setFrozenFlagsForSamples(frozenFlags.clone().melt(), sample1);
        DataSetCreation dataSetCreation = physicalDataSet(PREFIX + "D2");
        dataSetCreation.setSampleId(sample1);

        // When
        DataSetPermId id = v3api.createDataSets(systemSessionToken, Arrays.asList(dataSetCreation)).iterator().next();

        // Then
        assertEquals(getDataSet(id).getSample().getCode(), SAMPLE_1);
    }

    @Test(dataProvider = "liquidExperiment")
    public void testValidAddDataSetToExperiment(FrozenFlags frozenFlagsForExperiment)
    {
        // Given
        setFrozenFlagsForExperiments(frozenFlagsForExperiment, experiment1);
        DataSetCreation dataSetCreation = physicalDataSet(PREFIX + "D2");
        dataSetCreation.setExperimentId(experiment1);

        // When
        DataSetPermId id = v3api.createDataSets(systemSessionToken, Arrays.asList(dataSetCreation)).iterator().next();

        // Then
        assertEquals(getDataSet(id).getExperiment().getCode(), EXPERIMENT_1);
    }

    @DataProvider(name = "liquidExperiment")
    public static Object[][] liquidExperiment()
    {
        List<FrozenFlags> combinationsForLiquidExperiment = new FrozenFlags(true).freezeForSample().createAllCombinations();
        combinationsForLiquidExperiment.add(new FrozenFlags(false).freezeForDataSet());
        return asCartesianProduct(combinationsForLiquidExperiment);
    }

    @Test
    public void testInvalidAddDataSetToExperiment()
    {
        // Given
        setFrozenFlagsForExperiments(new FrozenFlags(true).freezeForDataSet(), experiment1);
        DataSetCreation dataSetCreation = physicalDataSet(PREFIX + "D2");
        dataSetCreation.setExperimentId(experiment1);

        // When
        assertUserFailureException(Void -> v3api.createDataSets(systemSessionToken, Arrays.asList(dataSetCreation)),
                // Then
                "ERROR: Operation SET EXPERIMENT is not allowed because experiment " + EXPERIMENT_1 + " is frozen for data set "
                        + dataSetCreation.getCode() + ".");
    }

    @Test
    public void testInvalidAddDataSetToExperimentAfterMelting()
    {
        // Given
        FrozenFlags frozenFlags = new FrozenFlags(true).freezeForDataSet();
        setFrozenFlagsForExperiments(frozenFlags, experiment1);
        setFrozenFlagsForExperiments(frozenFlags.clone().melt(), experiment1);
        DataSetCreation dataSetCreation = physicalDataSet(PREFIX + "D2");
        dataSetCreation.setExperimentId(experiment1);

        // When
        DataSetPermId id = v3api.createDataSets(systemSessionToken, Arrays.asList(dataSetCreation)).iterator().next();

        // Then
        assertEquals(getDataSet(id).getExperiment().getCode(), EXPERIMENT_1);
    }

    @Test
    public void testFreezeExperimentAndDataSet()
    {
        // Given
        ExperimentUpdate experimentUpdate = new ExperimentUpdate();
        experimentUpdate.setExperimentId(experimentOfDataSet);
        experimentUpdate.freeze();
        experimentUpdate.freezeForDataSets();
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSetExp);
        dataSetUpdate.freeze();
        IOperationExecutionOptions op = new SynchronousOperationExecutionOptions();

        // When
        v3api.executeOperations(systemSessionToken, Arrays.asList(
                new UpdateExperimentsOperation(experimentUpdate), new UpdateDataSetsOperation(dataSetUpdate)), op);

        // Then
        assertEquals(getExperiment(experimentOfDataSet).isFrozen(), true);
        assertEquals(getDataSet(dataSetExp).isFrozen(), true);
    }

    @Test
    public void testFreezeSampleAndDataSet()
    {
        // Given
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sampleOfDataSet);
        sampleUpdate.freeze();
        sampleUpdate.freezeForDataSets();
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSetSamp);
        dataSetUpdate.freeze();
        IOperationExecutionOptions op = new SynchronousOperationExecutionOptions();

        // When
        v3api.executeOperations(systemSessionToken, Arrays.asList(
                new UpdateSamplesOperation(sampleUpdate), new UpdateDataSetsOperation(dataSetUpdate)), op);

        // Then
        assertEquals(getSample(sampleOfDataSet).isFrozen(), true);
        assertEquals(getDataSet(dataSetSamp).isFrozen(), true);
    }
}
