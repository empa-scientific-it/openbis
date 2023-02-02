/*
 * Copyright 2011 ETH Zuerich, CISD
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.generic.shared.basic.utils.GroupByMap;
import ch.systemsx.cisd.openbis.generic.shared.basic.utils.IGroupKeyExtractor;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.IWellData;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.MaterialIdFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.WellData;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialReplicaSummaryAggregationType;

/**
 * @author Tomasz Pylak
 */
public class WellReplicaSummaryCalculator
{
    /**
     * Calculates summaries and ranks for each group of well replicas (biological or technical). Usually a replica is determined by the material in
     * the well, e.g. gene or compound.
     * 
     * @param calculateDeviations if true then deviations for each aggregation will be calculated as well
     */
    public static List<MaterialIdFeatureVectorSummary> calculateReplicasFeatureVectorSummaries(
            List<? extends IWellData> wellDataList,
            MaterialReplicaSummaryAggregationType aggregationType, boolean calculateDeviations)
    {
        validate(wellDataList);
        WellReplicaSummaryCalculator calculator =
                new WellReplicaSummaryCalculator(wellDataList, aggregationType);
        List<MaterialIdFeatureVectorSummary> summaries =
                calculator.calculateReplicasFeatureVectorSummaries(calculateDeviations);
        return summaries;
    }

    /**
     * Calculates one aggregated number for each feature, taking all specified wells (which should b technical replicates) into account.
     */
    public static float[] calculateSummaryFeatureVector(
            List<? extends IWellData> techicalReplicaWells,
            MaterialReplicaSummaryAggregationType aggregationType)
    {
        validate(techicalReplicaWells);
        int numberOfFeatures = getNumberOfFeatures(techicalReplicaWells);
        return calculateSummaryFeatureVector(techicalReplicaWells, aggregationType,
                numberOfFeatures, false).getAggregates();
    }

    private static void validate(List<? extends IWellData> wellDataList)
    {
        int numberOfFeatures = getNumberOfFeatures(wellDataList);
        for (IWellData wellData : wellDataList)
        {
            if (wellData.getFeatureVector() == null)
            {
                throw new IllegalStateException(
                        String.format("No feature vector found for material "
                                + wellData.getReplicaMaterialId()));
            }
            if (wellData.getFeatureVector().length != numberOfFeatures)
            {
                throw new IllegalStateException(
                        String.format(
                                "Each well should have the same amount features, but some have %d and some have %d.",
                                numberOfFeatures, wellData.getFeatureVector().length));
            }
        }

    }

    private final GroupByMap<Long/* replica id */, IWellData> replicaToWellDataMap;

    private final MaterialReplicaSummaryAggregationType aggregationType;

    private final int numberOfFeatures;

    private WellReplicaSummaryCalculator(List<? extends IWellData> wellDataList,
            MaterialReplicaSummaryAggregationType aggregationType)
    {
        this.numberOfFeatures = getNumberOfFeatures(wellDataList);
        this.aggregationType = aggregationType;
        this.replicaToWellDataMap =
                GroupByMap.create(wellDataList, new IGroupKeyExtractor<Long, IWellData>()
                    {
                        @Override
                        public Long getKey(IWellData wellData)
                        {
                            return wellData.getReplicaMaterialId();
                        }
                    });
    }

    private List<MaterialIdFeatureVectorSummary> calculateReplicasFeatureVectorSummaries(
            boolean calculateDeviations)
    {
        Map<Long/* replica id */, SummaryFeatureVector> replicaToSummaryMap =
                calculateSummaryFeatures(calculateDeviations);
        Map<Long/* replica id */, int[]/* ranks for each feature */> ranks =
                calculateRanks(replicaToSummaryMap, numberOfFeatures);

        List<MaterialIdFeatureVectorSummary> summaries =
                new ArrayList<MaterialIdFeatureVectorSummary>();
        Set<Long> replicaIds = replicaToWellDataMap.getKeys();
        int numberOfReplicas = replicaIds.size();
        for (Long replicaId : replicaIds)
        {
            SummaryFeatureVector summaryFeatures = replicaToSummaryMap.get(replicaId);
            // array not empty, all materials are the same
            MaterialIdFeatureVectorSummary summary =
                    new MaterialIdFeatureVectorSummary(replicaId, summaryFeatures.getAggregates(),
                            summaryFeatures.tryGetDeviations(), ranks.get(replicaId),
                            numberOfReplicas);
            summaries.add(summary);
        }
        return summaries;
    }

    @Private
    static Map<Long/* replica id */, int[]/* ranks for each feature */> calculateRanks(
            Map<Long/* replica id */, SummaryFeatureVector> replicaToSummaryMap,
            int numberOfFeatures)
    {
        List<IWellData> summaryWellDataList = createSummaryWellData(replicaToSummaryMap);
        Map<Long, int[]> ranks = createEmptyRankingMap(replicaToSummaryMap, numberOfFeatures);
        for (int featureIx = 0; featureIx < numberOfFeatures; featureIx++)
        {
            sortBySelectedFeature(summaryWellDataList, featureIx);
            Float prevValue = null;
            int rank = 0;
            // browse sorted data and fix the rankings for a current feature
            for (int i = 0; i < summaryWellDataList.size(); i++)
            {
                IWellData rankWellData = summaryWellDataList.get(i);
                float value = getFeatureValue(rankWellData, featureIx);
                if (isDifferent(prevValue, value))
                {
                    rank = i + 1;
                }
                int[] replicaRanks = ranks.get(rankWellData.getReplicaMaterialId());
                replicaRanks[featureIx] = rank;
                prevValue = value;
            }
        }
        return ranks;
    }

    private static boolean isDifferent(Float prevValueOrNull, float value)
    {
        if (prevValueOrNull == null)
        {
            return true;
        }
        float prevValue = prevValueOrNull.floatValue();
        if (isNumerical(value) != isNumerical(prevValue))
        {
            return true;
        }
        if (isNumerical(value) == false)
        {
            // both are not numerical, we treat them in the same way
            return false;
        }
        return value != prevValue;
    }

    /** Wrap features summary by the {@link IWellData} interface. */
    private static List<IWellData> createSummaryWellData(
            Map<Long, SummaryFeatureVector> replicaToSummaryMap)
    {
        List<IWellData> summaryWellDataList = new ArrayList<IWellData>();
        for (Entry<Long, SummaryFeatureVector> entry : replicaToSummaryMap.entrySet())
        {
            Long replicaId = entry.getKey();
            float[] aggregates = entry.getValue().getAggregates();
            IWellData summaryWellData = new WellData(replicaId, aggregates, null);
            summaryWellDataList.add(summaryWellData);
        }
        return summaryWellDataList;
    }

    private static Map<Long, int[]> createEmptyRankingMap(
            Map<Long, SummaryFeatureVector> replicaToSummaryMap, int numberOfFeatures)
    {
        Map<Long, int[]> ranks = new HashMap<Long, int[]>();
        Set<Long> replicaIds = replicaToSummaryMap.keySet();
        for (Long replicaId : replicaIds)
        {
            ranks.put(replicaId, new int[numberOfFeatures]);
        }
        return ranks;
    }

    private Map<Long, SummaryFeatureVector> calculateSummaryFeatures(boolean calculateDeviations)
    {
        Map<Long/* replica id */, SummaryFeatureVector> replicaToSummaryMap =
                new HashMap<Long, SummaryFeatureVector>();
        Set<Long> replicaIds = replicaToWellDataMap.getKeys();
        for (Long replicaId : replicaIds)
        {
            List<IWellData> replicaWells = replicaToWellDataMap.getOrDie(replicaId);
            SummaryFeatureVector summary =
                    calculateSummaryFeatureVector(replicaWells, aggregationType, numberOfFeatures,
                            calculateDeviations);
            replicaToSummaryMap.put(replicaId, summary);
        }
        return replicaToSummaryMap;
    }

    @Private
    static class SummaryFeatureVector
    {
        private final float[] aggregate;

        private final float[] deviationsOrNull;

        public SummaryFeatureVector(float[] aggregate, float[] deviationsOrNull)
        {
            this.aggregate = aggregate;
            this.deviationsOrNull = deviationsOrNull;
        }

        public float[] getAggregates()
        {
            return aggregate;
        }

        /** null if not calculated */
        public float[] tryGetDeviations()
        {
            return deviationsOrNull;
        }
    }

    private static SummaryFeatureVector calculateSummaryFeatureVector(
            List<? extends IWellData> replicaWells,
            MaterialReplicaSummaryAggregationType aggregationType, int numberOfFeatures,
            boolean calculateDeviations)
    {
        switch (aggregationType)
        {
            case MEDIAN:
                return calculateMedianVector(replicaWells, numberOfFeatures, calculateDeviations);
            default:
                throw new IllegalStateException("Unhandled aggregation type: " + aggregationType);
        }
    }

    private static SummaryFeatureVector calculateMedianVector(
            List<? extends IWellData> replicaWells, int numberOfFeatures,
            boolean calculateDeviations)
    {
        float[] medians = new float[numberOfFeatures];
        for (int featureIx = 0; featureIx < numberOfFeatures; featureIx++)
        {
            medians[featureIx] = calculateMedian(replicaWells, featureIx);
        }
        float[] deviations = null;
        if (calculateDeviations)
        {
            deviations = calculateDeviations(replicaWells, numberOfFeatures, medians);
        }
        return new SummaryFeatureVector(medians, deviations);
    }

    private static float[] calculateDeviations(List<? extends IWellData> replicaWells,
            int numberOfFeatures, float[] medians)
    {
        float[] deviations = new float[numberOfFeatures];
        for (int featureIx = 0; featureIx < numberOfFeatures; featureIx++)
        {
            float median = medians[featureIx];
            deviations[featureIx] =
                    calculateMedianAbsoluteDeviation(median, replicaWells, featureIx);
        }
        return deviations;
    }

    private static void sortBySelectedFeature(List<IWellData> replicaWells, int featureIx)
    {
        Collections.sort(replicaWells, createSelectedFeatureAscendingComparator(featureIx));
    }

    private static int getNumberOfFeatures(List<? extends IWellData> replicaWells)
    {
        if (replicaWells.size() == 0)
        {
            return 0;
        } else
        {
            return replicaWells.get(0).getFeatureVector().length;
        }
    }

    // NOTE: we calculate the median from the values which are neither NaN nor infinity
    @Private
    static float calculateMedian(List<? extends IWellData> replicaWells, int featureIx)
    {
        List<Float> featureValues = new ArrayList<Float>();
        for (IWellData replicaWell : replicaWells)
        {
            float featureValue = getFeatureValue(replicaWell, featureIx);
            if (isNumerical(featureValue))
            {
                featureValues.add(featureValue);
            }
        }
        return calculateMedian(featureValues);
    }

    private static float calculateMedian(List<Float> numbers)
    {
        if (numbers.isEmpty())
        {
            return Float.NaN;
        }
        Collections.sort(numbers);
        int size = numbers.size();
        int index = size / 2;
        float median = numbers.get(index);
        if (size % 2 == 0)
        {
            median = (median + numbers.get(index - 1)) / 2;
        }
        return median;
    }

    private static float getFeatureValue(IWellData wellData, int featureIx)
    {
        return wellData.getFeatureVector()[featureIx];
    }

    @Private
    static float calculateMedianAbsoluteDeviation(float median,
            List<? extends IWellData> replicaWells, int featureIx)
    {
        if (isNumerical(median) == false)
        {
            return Float.NaN;
        }
        List<Float> absDeviations = new ArrayList<Float>();
        for (IWellData wellData : replicaWells)
        {
            float value = getFeatureValue(wellData, featureIx);
            if (isNumerical(value))
            {
                float absDev = Math.abs(value - median);
                absDeviations.add(absDev);
            }
        }
        return calculateMedian(absDeviations);
    }

    private static boolean isNumerical(float value)
    {
        return Float.isInfinite(value) == false && Float.isNaN(value) == false;
    }

    private static Comparator<IWellData> createSelectedFeatureAscendingComparator(
            final int featureIx)
    {
        return new Comparator<IWellData>()
            {
                @Override
                public int compare(IWellData w1, IWellData w2)
                {
                    float v1 = getFeatureValue(w1, featureIx);
                    float v2 = getFeatureValue(w2, featureIx);
                    if (isNumerical(v1) != isNumerical(v2))
                    {
                        // all non-numerical numbers are equal and should be at the end of the list
                        return isNumerical(v1) ? -1 : 1;
                    }
                    return Float.compare(v1, v2);
                }
            };
    }
}