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

package ch.systemsx.cisd.openbis.plugin.screening.shared.dto;

import java.util.Arrays;

import ch.systemsx.cisd.base.convert.NativeTaggedArray;
import ch.systemsx.cisd.base.mdarray.MDFloatArray;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;

/**
 * A class for providing the values of one feature for screening plate. It uses a two-dimensional {@link MDFloatArray} in the back-end and provides
 * access by row and column as well as by 0-based cartesian coordinates.
 * <p>
 * The row-column coordinates are obtained from the cartesian coordinates by:
 * 
 * <pre>
 * row = dimY - y
 * col = x + 1
 * </pre>
 * 
 * @author Bernd Rinn
 */
public final class PlateFeatureValues
{
    private final MDFloatArray valueArray;

    private final Geometry geometry;

    /**
     * Constructs a new empty values object with the given plate <var>geometry</var>.
     * 
     * @param geometry
     */
    public PlateFeatureValues(final Geometry geometry)
    {
        this.geometry = geometry;
        this.valueArray = new MDFloatArray(geometry.getCartesianDimensions());
        Arrays.fill(valueArray.getAsFlatArray(), Float.NaN);
    }

    /**
     * Constructs a new values object from its persistent form.
     * 
     * @see PlateFeatureValues#toByteArray()
     */
    public PlateFeatureValues(final byte[] byteArray)
    {
        this.valueArray = NativeTaggedArray.tryToFloatArray(byteArray);
        if (valueArray == null || valueArray.rank() != 2)
        {
            throw new IllegalArgumentException(
                    "byteArray is not a valid two-dimensional tagged array.");
        }
        this.geometry = Geometry.createFromCartesianDimensions(valueArray.dimensions());
    }

    /**
     * Sets the <var>value</var> in this value object for the cartesian coordinates <var>(x,y)</var>.
     */
    public void setForCartesianCoordinates(float value, int x, int y)
    {
        valueArray.set(value, x, y);
    }

    /**
     * Returns the value in this value object for the cartesian coordinates <var>(x,y)</var>.
     */
    public float getForCartesianCoordinates(int x, int y)
    {
        return valueArray.get(x, y);
    }

    /**
     * Sets the <var>value</var> in this value object for the row-column coordinates <var>(row,col)</var>.
     */
    public void setForWellLocation(float value, int row, int col)
    {
        valueArray.set(value, WellLocationUtils.calcX(geometry, row, col), WellLocationUtils.calcY(
                geometry, row, col));
    }

    /**
     * Returns the <var>value</var> in this value object for the row-column coordinates <var>(row,col)</var>.
     */
    public float getForWellLocation(int row, int col)
    {
        return valueArray.get(WellLocationUtils.calcX(geometry, row, col), WellLocationUtils.calcY(
                geometry, row, col));
    }

    /**
     * Sets the <var>value</var> in this value object for the row-column coordinates <var>wellLocation</var>.
     */
    public void setForWellLocation(float value, WellLocation wellLocation)
    {
        valueArray.set(value, WellLocationUtils.calcX(geometry, wellLocation), WellLocationUtils
                .calcY(geometry, wellLocation));
    }

    /**
     * Returns the <var>value</var> in this value object for the row-column coordinates <var>wellLocation</var>.
     */
    public float getForWellLocation(WellLocation wellLocation)
    {
        return valueArray.get(WellLocationUtils.calcX(geometry, wellLocation), WellLocationUtils
                .calcY(geometry, wellLocation));
    }

    /**
     * Returns the geometry underlying this values object.
     */
    public Geometry getGeometry()
    {
        return geometry;
    }

    /**
     * Returns the value objects in its persistent form.
     */
    public byte[] toByteArray()
    {
        return NativeTaggedArray.toByteArray(valueArray);
    }

    //
    // Object
    //

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((valueArray == null) ? 0 : valueArray.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PlateFeatureValues other = (PlateFeatureValues) obj;
        if (valueArray == null)
        {
            if (other.valueArray != null)
                return false;
        } else if (!valueArray.equals(other.valueArray))
            return false;
        return true;
    }

}
