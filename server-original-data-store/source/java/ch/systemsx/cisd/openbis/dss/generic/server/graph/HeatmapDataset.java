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

package ch.systemsx.cisd.openbis.dss.generic.server.graph;

import org.jfree.data.DomainOrder;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYZDataset;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class HeatmapDataset implements XYZDataset
{
    private final DefaultXYZDataset wrappedDataset;

    private Range range;

    public HeatmapDataset(DefaultXYZDataset dataset)
    {
        wrappedDataset = dataset;
    }

    /**
     * The range of the z values
     */
    public Range getRange()
    {
        return range;
    }

    /**
     * Set the range of the z-values
     */
    public void setRange(Range range)
    {
        this.range = range;
    }

    // XYZDataset
    @Override
    public Number getZ(int series, int item)
    {
        return wrappedDataset.getZ(series, item);
    }

    @Override
    public double getZValue(int series, int item)
    {
        return wrappedDataset.getZValue(series, item);
    }

    @Override
    public DomainOrder getDomainOrder()
    {
        return wrappedDataset.getDomainOrder();
    }

    @Override
    public int getItemCount(int series)
    {
        return wrappedDataset.getItemCount(series);
    }

    @Override
    public Number getX(int series, int item)
    {
        return wrappedDataset.getX(series, item);
    }

    @Override
    public double getXValue(int series, int item)
    {
        return wrappedDataset.getXValue(series, item);
    }

    @Override
    public Number getY(int series, int item)
    {
        return wrappedDataset.getY(series, item);
    }

    @Override
    public double getYValue(int series, int item)
    {
        return wrappedDataset.getYValue(series, item);
    }

    @Override
    public int getSeriesCount()
    {
        return wrappedDataset.getSeriesCount();
    }

    // The JFreeChart interface does not use generics
    @Override
    public Comparable<?> getSeriesKey(int series)
    {
        return wrappedDataset.getSeriesKey(series);
    }

    @Override
    public int indexOf(@SuppressWarnings("rawtypes") Comparable seriesKey)
    {
        return wrappedDataset.indexOf(seriesKey);
    }

    @Override
    public void addChangeListener(DatasetChangeListener arg0)
    {
        wrappedDataset.addChangeListener(arg0);
    }

    @Override
    public DatasetGroup getGroup()
    {
        return wrappedDataset.getGroup();
    }

    @Override
    public void removeChangeListener(DatasetChangeListener arg0)
    {
        wrappedDataset.removeChangeListener(arg0);
    }

    @Override
    public void setGroup(DatasetGroup arg0)
    {
        wrappedDataset.setGroup(arg0);
    }
}
