/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.model;

import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.Range;

/**
 * Calculates the range based on specified percentiles.
 *
 * @author Franz-Josef Elmer
 */
public class PercentileRangeCalculator implements IRangeCalculator
{
    private int minPercentile;

    private final int maxPercentile;

    public PercentileRangeCalculator(int minPercentile, int maxPercentile)
    {
        this.minPercentile = minPercentile;
        this.maxPercentile = maxPercentile;
    }

    @Override
    public Range calculate(List<Float> numbers)
    {
        Collections.sort(numbers);
        return new Range(pick(numbers, minPercentile), pick(numbers, maxPercentile));
    }

    private float pick(List<Float> numbers, int percentile)
    {
        int size = numbers.size();
        double rank = percentile * 0.01 * size - 0.5;
        int index1 = trim(Math.floor(rank), size);
        int index2 = trim(Math.ceil(rank), size);
        if (index1 == index2)
        {
            return numbers.get(index1);
        }
        float f1 = numbers.get(index1);
        float f2 = numbers.get(index2);
        return (float) (f1 * (index2 - rank) + f2 * (rank - index1));
    }

    private int trim(double index, int size)
    {
        return Math.max(0, Math.min(size - 1, (int) index));
    }

}
