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

package ch.systemsx.cisd.openbis.dss.etl.dto.api;

/**
 * The intensity range in a distribution of pixel values for a given symmetric quantile value.
 *
 * @author Bernd Rinn
 */
public class IntensityRange
{
    final int blackPoint;

    final int whitePoint;

    public IntensityRange(int blackPoint, int whitePoint)
    {
        this.blackPoint = blackPoint;
        this.whitePoint = whitePoint;
    }

    /**
     * The minimal level (black point).
     */
    public int getBlackPoint()
    {
        return blackPoint;
    }

    /**
     * The maximal level (white point).
     */
    public int getWhitePoint()
    {
        return whitePoint;
    }

    @Override
    public String toString()
    {
        return "MinMax [minLevel=" + blackPoint + ", maxLevel=" + whitePoint + "]";
    }

}
