/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.array;

import java.util.Arrays;

/**
 * A multi-dimensional <code>double</code> array.
 * 
 * @author Bernd Rinn
 */
public final class MDDoubleArray extends MDArray<Double>
{
    private final double[] flattenedArray;

    public MDDoubleArray(long[] dimensions)
    {
        this(new double[getLength(dimensions)], dimensions, true);
    }

    public MDDoubleArray(double[] flattenedArray, long[] dimensions)
    {
        this(flattenedArray, dimensions, true);
    }

    public MDDoubleArray(double[] flattenedArray, long[] dimensions, boolean checkDimensions)
    {
        this(flattenedArray, MDArray.toInt(dimensions), checkDimensions);
    }

    public MDDoubleArray(int[] dimensions)
    {
        this(new double[getLength(dimensions)], dimensions, true);
    }

    public MDDoubleArray(double[] flattenedArray, int[] dimensions)
    {
        this(flattenedArray, dimensions, true);
    }

    public MDDoubleArray(double[] flattenedArray, int[] shape, boolean checkDimensions)
    {
        super(shape);
        assert flattenedArray != null;

        if (checkDimensions)
        {
            final int expectedLength = getLength(shape);
            if (flattenedArray.length != expectedLength)
            {
                throw new IllegalArgumentException("Actual array length " + flattenedArray.length
                        + " does not match expected length " + expectedLength + ".");
            }
        }
        this.flattenedArray = flattenedArray;
    }

    @Override
    public int size()
    {
        return flattenedArray.length;
    }

    @Override
    public Double getAsObject(int[] indices)
    {
        return getValue(indices);
    }

    @Override
    public void setToObject(int[] indices, Double value)
    {
        setValue(indices, value);
    }

    /**
     * Returns the array in flattened form. Changes to the returned object will change the
     * multi-dimensional array directly.
     */
    public double[] getAsFlatArray()
    {
        return flattenedArray;
    }

    /**
     * Returns the value of array at the position defined by <var>indices</var>.
     */
    public double getValue(int[] indices)
    {
        return flattenedArray[computeIndex(indices)];
    }
    
    /**
     * Sets the <var>value</var> of array at the position defined by <var>indices</var>.
     */
    public void setValue(int[] indices, double value)
    {
        flattenedArray[computeIndex(indices)] = value;
    }

    //
    // Object
    //
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(flattenedArray);
        result = prime * result + Arrays.hashCode(shape);
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        MDDoubleArray other = (MDDoubleArray) obj;
        if (Arrays.equals(flattenedArray, other.flattenedArray) == false)
        {
            return false;
        }
        if (Arrays.equals(shape, other.shape) == false)
        {
            return false;
        }
        return true;
    }

}
