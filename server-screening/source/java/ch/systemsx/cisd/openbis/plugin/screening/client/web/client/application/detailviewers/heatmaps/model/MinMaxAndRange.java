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

import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.Range;

/**
 * Bean for minimum, maximum and range.
 *
 * @author Franz-Josef Elmer
 */
public class MinMaxAndRange
{
    private final float min;

    private final float max;

    private final Range range;

    public MinMaxAndRange(float min, float max, Range range)
    {
        this.min = min;
        this.max = max;
        this.range = range;
    }

    public float getMin()
    {
        return min;
    }

    public float getMax()
    {
        return max;
    }

    public Range getRange()
    {
        return range;
    }

}
