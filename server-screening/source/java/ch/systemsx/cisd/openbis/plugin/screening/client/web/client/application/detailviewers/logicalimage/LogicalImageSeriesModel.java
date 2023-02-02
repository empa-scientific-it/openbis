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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.logicalimage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageChannelStack;

/**
 * @author pkupczyk
 */
class LogicalImageSeriesModel
{
    private final Map<LogicalImageSeriesPoint, List<ImageChannelStack>> channelStackImagesBySeries;

    private final List<LogicalImageSeriesPoint> sortedPoints;

    private final int numberOfTimepoints;

    private final int numberOfDepthLevels;

    private final boolean matrixViewPossible;

    private List<List<LogicalImageSeriesPoint>> matrix;

    public LogicalImageSeriesModel(List<ImageChannelStack> channelStackImages)
    {
        channelStackImagesBySeries =
                new TreeMap<LogicalImageSeriesPoint, List<ImageChannelStack>>();
        TreeMap<Float/* TimePoint */, Map<Float/* Depth */, LogicalImageSeriesPoint>> timePointDepthMatrix =
                new TreeMap<Float/* TimePoint */, Map<Float/* Depth */, LogicalImageSeriesPoint>>();
        boolean seriesNumberPresent = false;
        boolean timepointOrDepthNotPresent = false;
        for (ImageChannelStack ref : channelStackImages)
        {
            LogicalImageSeriesPoint point = new LogicalImageSeriesPoint(ref);
            List<ImageChannelStack> imageReferences = channelStackImagesBySeries.get(point);
            if (imageReferences == null)
            {
                imageReferences = new ArrayList<ImageChannelStack>();
                channelStackImagesBySeries.put(point, imageReferences);
            }
            imageReferences.add(ref);
            Integer seriesNumberOrNull = ref.tryGetSeriesNumber();
            if (seriesNumberOrNull != null)
            {
                seriesNumberPresent = true;
            }
            Float timepoint = ref.tryGetTimepoint();
            Float depth = ref.tryGetDepth();
            if (timepoint == null || depth == null)
            {
                timepointOrDepthNotPresent = true;
            } else
            {
                Map<Float, LogicalImageSeriesPoint> depthMap = timePointDepthMatrix.get(timepoint);
                if (depthMap == null)
                {
                    depthMap = new TreeMap<Float, LogicalImageSeriesPoint>();
                    timePointDepthMatrix.put(timepoint, depthMap);
                }
                depthMap.put(depth, point);
            }
        }
        sortedPoints = new ArrayList<LogicalImageSeriesPoint>(channelStackImagesBySeries.keySet());
        Collections.sort(sortedPoints);
        numberOfTimepoints = timePointDepthMatrix.size();
        Collection<Map<Float, LogicalImageSeriesPoint>> values = timePointDepthMatrix.values();
        matrix = new ArrayList<List<LogicalImageSeriesPoint>>();

        Set<Integer> depthMapSizes = new HashSet<Integer>();
        int depthLevelCount = 0;
        for (Map<Float, LogicalImageSeriesPoint> depthMap : values)
        {
            matrix.add(new ArrayList<LogicalImageSeriesPoint>(depthMap.values()));
            depthLevelCount = Math.max(depthLevelCount, depthMap.size());
            depthMapSizes.add(depthMap.size());
        }
        numberOfDepthLevels = depthLevelCount;
        matrixViewPossible =
                seriesNumberPresent == false && timepointOrDepthNotPresent == false
                        && depthMapSizes.size() == 1;
    }

    public LogicalImageSeriesPoint get(int timeIndex, int depthIndex)
    {
        return matrix.get(timeIndex).get(depthIndex);
    }

    public final int getNumberOfTimepoints()
    {
        return numberOfTimepoints;
    }

    public final int getNumberOfDepthLevels()
    {
        return numberOfDepthLevels;
    }

    public final boolean isMatrixViewPossible()
    {
        return matrixViewPossible;
    }

    public List<LogicalImageSeriesPoint> getSortedPoints()
    {
        return sortedPoints;
    }

    public List<List<ImageChannelStack>> getSortedChannelStackSeriesPoints()
    {
        List<List<ImageChannelStack>> sortedSeries = new ArrayList<List<ImageChannelStack>>();
        for (LogicalImageSeriesPoint point : sortedPoints)
        {
            List<ImageChannelStack> series = channelStackImagesBySeries.get(point);
            sortedSeries.add(series);
        }
        return sortedSeries;
    }
}
