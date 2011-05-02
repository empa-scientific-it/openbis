/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import java.util.ArrayList;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.LogicalImageSeriesGrid.ImageSeriesPoint;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.LogicalImageSeriesGrid.LogicalImageSeriesViewerModel;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageChannelStack;

/**
 * Unit tests of {@link LogicalImageSeriesViewerModel}.
 * 
 * @author Tomasz Pylak
 */
public class LogicalImageSeriesViewerModelTest extends AssertJUnit
{

    @Test
    public void testModel()
    {
        Float times[] = new Float[]
            { 3.4f, null, 1.2f, 6.6f };
        Float depths[] = new Float[]
            { 66.6f, 33.4f, null, 11.2f };
        Integer series[] = new Integer[]
            { 1, 2, 3, null };

        LogicalImageSeriesViewerModel model = createModel(times, depths, series);
        
        assertEquals(false, model.isMatrixViewPossible());
        assertEquals(3, model.getNumberOfDepthLevels());
        assertEquals(3, model.getNumberOfTimepoints());

        List<ImageSeriesPoint> sortedPoints = model.getSortedPoints();
        ImageSeriesPoint firstPoint = new ImageSeriesPoint(null, null, null);
        assertEquals(firstPoint, sortedPoints.get(0));
        ImageSeriesPoint lastPoint = new ImageSeriesPoint(6.6f, 66.6f, 3);
        assertEquals(lastPoint, sortedPoints.get(sortedPoints.size() - 1));

        List<List<ImageChannelStack>> stackSeriesPoints = model.getSortedChannelStackSeriesPoints();
        List<ImageChannelStack> firstList = stackSeriesPoints.get(0);
        assertEquals(firstPoint, new ImageSeriesPoint(firstList.get(0)));

        List<ImageChannelStack> lastList = stackSeriesPoints.get(stackSeriesPoints.size() - 1);
        assertEquals(lastPoint, new ImageSeriesPoint(lastList.get(0)));
    }
    
    @Test
    public void testMatrixViewIsPossible()
    {
        Float times[] = new Float[]
            { 1.5f, 3f, 4.5f };
        Float depths[] = new Float[]
            { 2.5f, 5f };
        Integer series[] = new Integer[]
            { null };
        
        LogicalImageSeriesViewerModel model = createModel(times, depths, series);
        
        assertEquals(true, model.isMatrixViewPossible());
        assertEquals(3, model.getNumberOfTimepoints());
        assertEquals(2, model.getNumberOfDepthLevels());
    }
    
    @Test
    public void testMatrixViewIsNotPossibleBecauseOfSeriesNumbers()
    {
        Float times[] = new Float[]
            { 1.5f, 3f, 4.5f };
        Float depths[] = new Float[]
            { 2.5f, 5f };
        Integer series[] = new Integer[]
            { 1 };
        
        LogicalImageSeriesViewerModel model = createModel(times, depths, series);
        
        assertEquals(false, model.isMatrixViewPossible());
        assertEquals(3, model.getNumberOfTimepoints());
        assertEquals(2, model.getNumberOfDepthLevels());
    }
    
    @Test
    public void testMatrixViewIsNotPossibleBecauseOfMissingTimePoint()
    {
        List<ImageChannelStack> stacks = new ArrayList<ImageChannelStack>();
        stacks.add(stack(1, 1, null, 2.5f, null));
        stacks.add(stack(1, 1, 1f, 2.5f, null));
        
        LogicalImageSeriesViewerModel model = new LogicalImageSeriesViewerModel(stacks);
        
        assertEquals(false, model.isMatrixViewPossible());
        assertEquals(1, model.getNumberOfTimepoints());
        assertEquals(1, model.getNumberOfDepthLevels());
    }
    
    @Test
    public void testMatrixViewIsNotPossibleBecauseOfMissingDepth()
    {
        List<ImageChannelStack> stacks = new ArrayList<ImageChannelStack>();
        stacks.add(stack(1, 1, 1f, null, null));
        stacks.add(stack(1, 1, 1f, 2.5f, null));
        
        LogicalImageSeriesViewerModel model = new LogicalImageSeriesViewerModel(stacks);
        
        assertEquals(false, model.isMatrixViewPossible());
        assertEquals(1, model.getNumberOfTimepoints());
        assertEquals(1, model.getNumberOfDepthLevels());
    }
    
    @Test(groups = "broken")
    public void testMatrixViewIsNotPossibleBecauseOfSparseMatrix()
    {
        List<ImageChannelStack> stacks = new ArrayList<ImageChannelStack>();
        stacks.add(stack(1, 1, 1f, 1f, null));
        stacks.add(stack(1, 1, 1f, 2f, null));
        stacks.add(stack(1, 1, 2f, 2f, null));
        
        LogicalImageSeriesViewerModel model = new LogicalImageSeriesViewerModel(stacks);
        
        assertEquals(false, model.isMatrixViewPossible());
        assertEquals(2, model.getNumberOfTimepoints());
        assertEquals(2, model.getNumberOfDepthLevels());
    }

    private LogicalImageSeriesViewerModel createModel(Float[] times, Float[] depths,
            Integer[] series)
    {
        List<ImageChannelStack> stacks = new ArrayList<ImageChannelStack>();
        for (int time = 0; time < times.length; time++)
        {
            for (int depth = 0; depth < depths.length; depth++)
            {
                for (int seriesNum = 0; seriesNum < series.length; seriesNum++)
                {
                    stacks.add(stack(1, 1, times[time], depths[depth], series[seriesNum]));
                    stacks.add(stack(2, 2, times[time], depths[depth], series[seriesNum]));
                }
            }
        }
        return new LogicalImageSeriesViewerModel(stacks);
    }

    private static ImageChannelStack stack(int row, int col, Float tOrNull, Float zOrNull,
            Integer seriesNumberOrNull)
    {
        return new ImageChannelStack(0, row, col, tOrNull, zOrNull, seriesNumberOrNull);
    }
}
