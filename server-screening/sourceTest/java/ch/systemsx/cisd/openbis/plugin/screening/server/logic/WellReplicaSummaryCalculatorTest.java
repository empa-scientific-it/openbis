/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.plugin.screening.server.logic;

import static ch.systemsx.cisd.common.test.AssertionUtil.assertArraysEqual;
import static ch.systemsx.cisd.openbis.plugin.screening.server.logic.WellReplicaSummaryCalculator.calculateMedianAbsoluteDeviation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.WellReplicaSummaryCalculator.SummaryFeatureVector;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.IWellData;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.MaterialIdFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.WellData;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.WellExtendedData;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialReplicaSummaryAggregationType;

/**
 * Test of {@link WellReplicaSummaryCalculator}
 * 
 * @author Tomasz Pylak
 */
@Friend(toClasses = { WellReplicaSummaryCalculator.class, WellReplicaSummaryCalculator.SummaryFeatureVector.class })
public class WellReplicaSummaryCalculatorTest extends AssertJUnit
{
    @Test
    public void testGeneral()
    {
        List<IWellData> wellDataList = Arrays.asList(
                // ---- replicaId, [featureValues]
                createWellData(1, 2, 4),

                createWellData(2, 1, 10),

                createWellData(1, 1, 8),

                createWellData(2, 1, 100),

                createWellData(2, 3, 200));
        List<MaterialIdFeatureVectorSummary> summary = calculate(wellDataList);
        assertEquals(2, summary.size());
        int replica1Ix = summary.get(0).getMaterial() == 1 ? 0 : 1;

        MaterialIdFeatureVectorSummary repl1 = summary.get(replica1Ix);
        assertArraysEqual(new float[]
        { 1.5f, 6 }, repl1.getFeatureVectorSummary());
        assertArraysEqual(new float[]
        { 0.5f, 2 }, repl1.tryGetFeatureVectorDeviations());

        MaterialIdFeatureVectorSummary repl2 = summary.get(1 - replica1Ix);
        assertArraysEqual(new float[]
        { 1, 100 }, repl2.getFeatureVectorSummary());
        assertArraysEqual(new float[]
        { 0, 90 }, repl2.tryGetFeatureVectorDeviations());

        assertArraysEqual(new int[]
        { 2, 1 }, repl1.getFeatureVectorRanks());
        assertArraysEqual(new int[]
        { 1, 2 }, repl2.getFeatureVectorRanks());
    }

    @Test
    public void testNoWells()
    {
        assertEquals(0, calculate(new ArrayList<IWellData>()).size());
    }

    @Test
    public void testNoFeatures()
    {
        List<MaterialIdFeatureVectorSummary> summary =
                calculate(Arrays.asList(createWellData(0), createWellData(1)));
        assertEquals(2, summary.size());
        assertEquals(0, summary.get(0).getFeatureVectorSummary().length);
    }

    @Test
    public void testCalculateMedianForOddNumberOfValues()
    {
        List<IWellData> wellDataList = Arrays.asList(
                // ---- replicaId, [featureValues]
                createWellData(1, 0, Float.NaN),

                createWellData(1, 0, Float.NEGATIVE_INFINITY),

                createWellData(1, 0, 1), // 2

                createWellData(1, 0, 2), // 1

                createWellData(1, 0, 4), // 1

                createWellData(1, 0, 100), // 97

                createWellData(1, 0, Float.NaN),

                createWellData(1, 0, 3), // 0

                createWellData(1, 0, Float.POSITIVE_INFINITY));
        float median = WellReplicaSummaryCalculator.calculateMedian(wellDataList, 1);
        assertEquals(3.0f, median);

        float mad =
                WellReplicaSummaryCalculator.calculateMedianAbsoluteDeviation(median, wellDataList,
                        1);
        assertEquals(1.0f, mad);
    }

    @Test
    public void testCalculateMedianOneValue()
    {
        List<IWellData> wellDataList = Arrays.asList(createWellData(1, 0, 1));
        float median = WellReplicaSummaryCalculator.calculateMedian(wellDataList, 1);
        assertEquals(1f, median);
    }

    @Test
    public void testCalculateMedianForEvenNumberOfValues()
    {
        List<IWellData> wellDataList = Arrays.asList(
                // ---- replicaId, [featureValues]
                createWellData(1, 0, Float.NaN),

                createWellData(1, 0, Float.NEGATIVE_INFINITY),

                createWellData(1, 0, 1), // 1.5

                createWellData(1, 0, 3), // 0.5

                createWellData(1, 0, 2), // 0.5

                createWellData(1, 0, Float.NaN),

                createWellData(1, 0, 100), // 97.5

                createWellData(1, 0, Float.POSITIVE_INFINITY));
        float median = WellReplicaSummaryCalculator.calculateMedian(wellDataList, 1);
        assertEquals(2.5f, median);

        float mad =
                WellReplicaSummaryCalculator.calculateMedianAbsoluteDeviation(median, wellDataList,
                        1);
        assertEquals(1.0f, mad);
    }

    @Test
    public void testCalculateMedianAbsoluteDeviationMedianUnknown()
    {
        float mad = calculateMedianAbsoluteDeviation(Float.NaN, new ArrayList<IWellData>(), 0);
        assertTrue(Float.isNaN(mad));

        mad =
                calculateMedianAbsoluteDeviation(Float.NEGATIVE_INFINITY,
                        new ArrayList<IWellData>(), 0);
        assertTrue(Float.isNaN(mad));
    }

    @Test
    public void testRanks()
    {
        float[] oneFeatureSummaryValues = new float[]
        { 2, 1, 1, 2, Float.NaN, 3, Float.POSITIVE_INFINITY };
        int[] ranks = calculateOneFeatureRanks(oneFeatureSummaryValues);
        assertArraysEqual(new int[]
        { 3, 1, 1, 3, 6, 5, 6 }, ranks);
    }

    private int[] calculateOneFeatureRanks(float[] oneFeatureSummaryValues)
    {
        Map<Long, SummaryFeatureVector> map = new HashMap<Long, SummaryFeatureVector>();
        for (int i = 0; i < oneFeatureSummaryValues.length; i++)
        {
            map.put(Long.valueOf(i), new SummaryFeatureVector(new float[]
            { oneFeatureSummaryValues[i] }, null));
        }
        Map<Long, int[]> ranksMap = WellReplicaSummaryCalculator.calculateRanks(map, 1);
        int ranks[] = new int[oneFeatureSummaryValues.length];
        for (int i = 0; i < oneFeatureSummaryValues.length; i++)
        {
            ranks[i] = ranksMap.get(Long.valueOf(i))[0];
        }
        return ranks;
    }

    private static IWellData createWellData(long replicaId, float... featureValues)
    {
        Material material = new Material();
        material.setId(replicaId);
        return new WellExtendedData(new WellData(replicaId, featureValues, null), null);
    }

    private List<MaterialIdFeatureVectorSummary> calculate(List<IWellData> wellDataList)
    {
        return WellReplicaSummaryCalculator.calculateReplicasFeatureVectorSummaries(wellDataList,
                MaterialReplicaSummaryAggregationType.MEDIAN, true);
    }
}
